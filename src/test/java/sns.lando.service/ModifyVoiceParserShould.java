package sns.lando.service;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
public class ModifyVoiceParserShould extends KsqlServiceTestBase {

    private static final String INPUT_TOPIC = "modify.op.msgs";
    private static final String OUTPUT_TOPIC = "RAW_VOIP_INSTRUCTIONS";

    @Override
    protected String getPathToKsqlScript() {
        return "kafka-ksql/scripts/modifyVoice.sql";
    }

    private String orderId = generateRandomString();

    private KafkaConsumer<String, String> consumer;

    @Override
    protected List<NewTopic> getTopicNames() {
        NewTopic xmlTopic = new NewTopic(ModifyVoiceParserShould.INPUT_TOPIC, 1, (short) 1);
        NewTopic jsonTopic = new NewTopic(ModifyVoiceParserShould.OUTPUT_TOPIC, 1, (short) 1);

        List<NewTopic> newTopics = new ArrayList<>();
        newTopics.add(xmlTopic);
        newTopics.add(jsonTopic);
        return newTopics;
    }

    @Test
    public void convertsAnyXmlToJson() throws ExecutionException, InterruptedException {
        writeXmlToInputTopic();

        assertKafkaMessageEquals();
    }

    private void writeXmlToInputTopic() throws InterruptedException, ExecutionException {
        new KafkaProducer<String, String>(getKafkaProperties()).send(createKafkaProducerRecord(orderId)).get();
    }

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
        Duration duration = Duration.ofSeconds(1);
        return consumer.poll(duration);
    }

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

    private String generateRandomString() {
        return String.valueOf(new Random().nextLong());
    }

}