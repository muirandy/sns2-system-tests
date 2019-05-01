package sns.lando.service;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public abstract class KsqlServiceTestBase {
    @Container
    protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer("5.2.1").withEmbeddedZookeeper()
                                                                                       .waitingFor(Wait.forLogMessage(".*started.*\\n", 1));
    private static final String ENV_KEY_KAFKA_BROKER_SERVER = "KAFKA_BROKER_SERVER";
    private static final String ENV_KEY_KAFKA_BROKER_PORT = "KAFKA_BROKER_PORT";
    private static final String ENV_KEY_KSQL_BOOTSTRAP_SERVERS = "KSQL_BOOTSTRAP_SERVERS";
    private static final String ENV_KEY_KSQL_KSQL_SERVICE_ID = "KSQL_KSQL_SERVICE_ID";
    private static final String ENV_KEY_KSQL_KSQL_QUERIES_FILE = "KSQL_KSQL_QUERIES_FILE";
    private static final String ENV_KEY_KSQL_KSQL_SINK_REPLICAS = "KSQL_KSQL_SINK_REPLICAS";
    private static final String ENV_KEY_KSQL_KSQL_SINK_PARTITIONS = "KSQL_KSQL_SINK_PARTITIONS";
    private static final String ENV_KEY_KSQL_KSQL_STREAMS_REPLICATION_FACTOR = "KSQL_KSQL_STREAMS_REPLICATION_FACTOR";
    private static final String KAFKA_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String KSQL_SCRIPT_CONTAINER_PATH = "/opt/kafka-ksql/scripts/ksqlScript.sql";
    @Container
    protected GenericContainer ksqlContainer = new GenericContainer("sns2-system-tests_ksql-server:latest")
            .withEnv(calculateKsqlEnvProperties())
            .withNetwork(KAFKA_CONTAINER.getNetwork())
            .withClasspathResourceMapping(getPathToKsqlScript(), KSQL_SCRIPT_CONTAINER_PATH, BindMode.READ_ONLY)
            .waitingFor(Wait.forHttp("http://localhost:8088/"))
            .waitingFor(Wait.forLogMessage(".*INFO Server up and running.*\\n", 1));

    protected abstract String getPathToKsqlScript();

    private Map<String, String> calculateKsqlEnvProperties() {
        createTopics();

        Map<String, String> envProperties = new HashMap<>();
        envProperties.put(ENV_KEY_KSQL_KSQL_SERVICE_ID, this.getClass().getName());
        envProperties.put(ENV_KEY_KSQL_KSQL_QUERIES_FILE, KSQL_SCRIPT_CONTAINER_PATH);
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

        CreateTopicsResult createTopicsResult = adminClient.createTopics(getTopics(), new CreateTopicsOptions().timeoutMs(1000));
        Map<String, KafkaFuture<Void>> futureResults = createTopicsResult.values();
        futureResults.values().forEach(f -> {
            try {
                f.get(5000, TimeUnit.MILLISECONDS);
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

    protected static Properties getKafkaProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        props.put("acks", "all");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ModifyVoiceParserShould.class.getName());
        return props;
    }

    protected List<NewTopic> getTopics() {
        return getTopicNames().stream()
                              .map(n -> new NewTopic(n, 1, (short) 1))
                              .collect(Collectors.toList());
    }

    protected abstract List<String> getTopicNames();

    @BeforeEach
    public void setup() {
        assertTrue(KAFKA_CONTAINER.isRunning());
        assertTrue(ksqlContainer.isRunning());
        waitForDockerEnvironment();
    }

    private void waitForDockerEnvironment() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        //        writeContainerLogsToStdOut();
    }

    private void writeContainerLogsToStdOut() {
        System.out.println("Kafka Logs = " + KAFKA_CONTAINER.getLogs());
        System.out.println("Converter Logs = " + ksqlContainer.getLogs());
    }

    protected void writeStringToInputTopic() throws InterruptedException, ExecutionException {
        new KafkaProducer<String, String>(getKafkaProperties()).send(createProducerRecord()).get();
    }

    protected ProducerRecord createProducerRecord() {
        return new ProducerRecord(getInputTopicName(), createKeyForInputMessage(), createInputMessage());
    }

    protected abstract String getInputTopicName();

    protected abstract String createKeyForInputMessage();

    protected abstract String createInputMessage();

}
