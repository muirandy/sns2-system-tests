package sns.lando.service;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class ModifyVoiceParserShould {
    private static final String ENV_KEY_KAFKA_BROKER_SERVER = "KAFKA_BROKER_SERVER";
    private static final String ENV_KEY_KAFKA_BROKER_PORT = "KAFKA_BROKER_PORT";
    private static final String ENV_KEY_KSQL_BOOTSTRAP_SERVERS = "KSQL_BOOTSTRAP_SERVERS";
    private static final String ENV_KEY_KSQL_KSQL_SERVICE_ID = "KSQL_KSQL_SERVICE_ID";
    private static final String ENV_KEY_KSQL_KSQL_QUERIES_FILE = "KSQL_KSQL_QUERIES_FILE";
    private static final String ENV_KEY_KSQL_KSQL_SINK_REPLICAS = "KSQL_KSQL_SINK_REPLICAS";
    private static final String ENV_KEY_KSQL_KSQL_SINK_PARTITIONS = "KSQL_KSQL_SINK_PARTITIONS";
    private static final String ENV_KEY_KSQL_KSQL_STREAMS_REPLICATION_FACTOR = "KSQL_KSQL_STREAMS_REPLICATION_FACTOR";

    private static final String INPUT_TOPIC = "modify.op.msgs";
    private static final String OUTPUT_TOPIC = "RAW_VOIP_INSTRUCTIONS";

    private static final String KAFKA_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer("5.2.1").withEmbeddedZookeeper()
                                                                                     .waitingFor(Wait.forLogMessage(".*started.*\\n", 1));

    @Container
    private GenericContainer ksqlContainer = new GenericContainer("sns2-system-tests_kafka-ksql:latest")
            .withEnv(calculateKsqlEnvProperties())
            .withNetwork(KAFKA_CONTAINER.getNetwork())
            .withClasspathResourceMapping("kafka-ksql/scripts/modifyVoice.sql", "/opt/kafka-ksql/scripts/modifyVoice.sql", BindMode.READ_ONLY)
            .waitingFor(Wait.forHttp("http://localhost:8088/"))
            .waitingFor(Wait.forLogMessage(".*INFO Server up and running.*\\n", 1));
    private String orderId = generateRandomString();

    private KafkaConsumer<String, String> consumer;

    @BeforeEach
    public void setup() {
        assertTrue(KAFKA_CONTAINER.isRunning());
        //        createTopics();
        //        startupKsql();
        assertTrue(ksqlContainer.isRunning());
    }

    private void startupKsql() {
        try {
            ksqlContainer = new GenericContainer("sns2-system-tests_kafka-ksql:latest")
                    .withEnv(calculateKsqlEnvProperties())
                    .withNetwork(KAFKA_CONTAINER.getNetwork())
                    .withClasspathResourceMapping("kafka-ksql/scripts/modifyVoice.sql", "/opt/kafka-ksql/scripts/modifyVoice.sql", BindMode.READ_ONLY)
                    .waitingFor(Wait.forHttp("http://localhost:8088/"))
                    .waitingFor(Wait.forLogMessage(".*INFO Server up and running.*\\n", 1));
            ksqlContainer.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private Map<String, String> calculateKsqlEnvProperties() {
        createTopics();

        Map<String, String> envProperties = new HashMap<>();
        envProperties.put(ENV_KEY_KSQL_KSQL_SERVICE_ID, "ksql-server-modifyParserShould");
        envProperties.put(ENV_KEY_KSQL_KSQL_QUERIES_FILE, "/opt/kafka-ksql/scripts/modifyVoice.sql");
        envProperties.put(ENV_KEY_KSQL_BOOTSTRAP_SERVERS, KAFKA_CONTAINER.getNetworkAliases().get(0) + ":9092");
        envProperties.put(ENV_KEY_KAFKA_BROKER_SERVER, KAFKA_CONTAINER.getNetworkAliases().get(0));
        envProperties.put(ENV_KEY_KAFKA_BROKER_PORT, "" + 9092);
        envProperties.put(ENV_KEY_KSQL_KSQL_SINK_REPLICAS, "1");
        envProperties.put(ENV_KEY_KSQL_KSQL_SINK_PARTITIONS, "1");
        envProperties.put(ENV_KEY_KSQL_KSQL_STREAMS_REPLICATION_FACTOR, "1");
        envProperties.put("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "2");
        envProperties.put("ksql.sink.partitions", "1");
        envProperties.put("KSQL_LISTENERS", "http://0.0.0.0:8088");

        return envProperties;
    }

    private void createTopics() {
        AdminClient adminClient = AdminClient.create(getKafkaProperties());
        NewTopic xmlTopic = new NewTopic(INPUT_TOPIC, 1, (short) 1);
        NewTopic jsonTopic = new NewTopic(OUTPUT_TOPIC, 1, (short) 1);

        List<NewTopic> newTopics = new ArrayList<>();
        newTopics.add(xmlTopic);
        newTopics.add(jsonTopic);

        CreateTopicsResult createTopicsResult = adminClient.createTopics(newTopics, new CreateTopicsOptions().timeoutMs(10000));
        Map<String, KafkaFuture<Void>> futureResults = createTopicsResult.values();
        futureResults.values().forEach(f -> {
            try {
                f.get(10000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        });
        adminClient.close();
    }

    private static Properties getKafkaProperties() {
        String bootstrapServers = KAFKA_CONTAINER.getBootstrapServers();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put("acks", "all");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ModifyVoiceParserShould.class.getName());
        return props;
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Kafka Logs = " + KAFKA_CONTAINER.getLogs());
        System.out.println("Converter Logs = " + ksqlContainer.getLogs());
        ksqlContainer.stop();
    }

    @Test
    public void convertsAnyXmlToJson() throws ExecutionException, InterruptedException {

        Thread.sleep(1000);

        writeXmlToInputTopic();

        assertKafkaMessageEquals();
    }

    private void writeXmlToInputTopic() throws InterruptedException, ExecutionException {
        new KafkaProducer<String, String>(getKafkaProperties()).send(createKafkaProducerRecord(orderId)).get();
    }

    @NotNull
    private ProducerRecord createKafkaProducerRecord(String orderId) {
        return new ProducerRecord(INPUT_TOPIC, orderId, createMessage(orderId));
    }

    private String createMessage(String orderId) {
        return String.format(
                "{\n" +
                        "  \"transaction\":{\n" +
                        "    \"operatorIssuedDate\":\"2011-06-01T09:51:12\",\n" +
                        "    \"operatorTransactionId\":\"op_trans_id_095025_228\",\n" +
                        "    \"operatorId\":\"sky\",\n" +
                        "    \"receivedDate\":\"2018-11-15T10:29:07\",\n" +
                        "    \"instruction\":{\n" +
                        "      \"type\":\"PlaceOrder\",\n" +
                        "      \"version\":\"1\",\n" +
                        "      \"order\":{\n" +
                        "        \"type\":\"modify\",\n" +
                        "        \"operatorOrderId\":\"SogeaVoipModify_-9156304217878863645\",\n" +
                        "        \"operatorNotes\":\"Test: notes\",\n" +
                        "        \"orderId\":\"%s\"\n" +
                        "      },\n" +
                        "      \"modifyFeaturesInstruction\":{\n" +
                        "        \"operatorNotes\":\"Test: addThenRemoveStaticIpToAnFttcService\",\n" +
                        "        \"operatorOrderId\":\"SogeaVoipModify_-9156304217878863645\",\n" +
                        "        \"serviceId\":\"31642339\",\n" +
                        "        \"features\":{\n" +
                        "          \"feature\":[{\n" +
                        "            \"code\":\"CallerDisplay\"\n" +
                        "          },{\n" +
                        "            \"code\":\"RingBack\"\n" +
                        "          },{\n" +
                        "            \"code\":\"ChooseToRefuse\"\n" +
                        "          }]\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"traceId\":\"27baecd155217dcb\"\n" +
                        "}\n"
                , orderId
        );
    }

    private void assertKafkaMessageEquals() {
        ConsumerRecords<String, String> recs = pollForResults();
        assertFalse(recs.isEmpty());

        Spliterator<ConsumerRecord<String, String>> spliterator = Spliterators.spliteratorUnknownSize(recs.iterator(), 0);
        Stream<ConsumerRecord<String, String>> consumerRecordStream = StreamSupport.stream(spliterator, false);
        Optional<ConsumerRecord<String, String>> expectedConsumerRecord = consumerRecordStream.filter(cr -> foundExpectedRecord(cr.key()))
                                                                                              .findAny();
        expectedConsumerRecord.ifPresent(cr -> assertRecordValueJson(cr));
        if (!expectedConsumerRecord.isPresent())
            fail("Did not find expected record");
    }

    private ConsumerRecords<String, String> pollForResults() {
        consumer = createKafkaConsumer();
        Duration duration = Duration.ofSeconds(8);
        return consumer.poll(duration);
    }

    @NotNull
    private KafkaConsumer<String, String> createKafkaConsumer() {
        Properties props = getKafkaProperties();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(OUTPUT_TOPIC));
        Duration immediately = Duration.ofSeconds(0);
        consumer.poll(immediately);
        consumer.seekToBeginning(consumer.assignment());
        return consumer;
    }

    private boolean foundExpectedRecord(String key) {
        return orderId.equals(key);
    }

    private void assertRecordValueJson(ConsumerRecord<String, String> consumerRecord) {
        String value = consumerRecord.value();
        String expectedValue = formatExpectedValue(orderId);
        assertJsonEquals(expectedValue, value);
    }

    private String formatExpectedValue(String orderId) {
        return String.format(
                "{\"TRACE_ID\":\"${json-unit.ignore}\",\"OPERATOR_ID\":\"sky\",\"OPERATOR_ORDER_ID\":\"SogeaVoipModify_-9156304217878863645\",\"ORDER_ID\":\"%s\",\"SERVICE_ID\":\"31642339\",\"FEATURES\":[{\"code\":\"CallerDisplay\"},{\"code\":\"RingBack\"},{\"code\":\"ChooseToRefuse\"}]}",
                orderId
        );
    }

    @NotNull
    private String generateRandomString() {
        return String.valueOf(new Random().nextLong());
    }

}
