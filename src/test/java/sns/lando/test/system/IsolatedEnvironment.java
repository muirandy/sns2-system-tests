package sns.lando.test.system;

import com.eclipsesource.json.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.xmlunit.assertj.XmlAssert;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class IsolatedEnvironment implements TestEnvironment {
    private static final String ACTIVE_MQ_INCOMING_QUEUE = "ColliderToCujo";
    private static final String ACTIVE_MQ_OUTGOING_QUEUE = "HaloToKnitware";
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    private String directoryNumber = "011" + new Random().nextInt();
    private Long serviceId;
    private Long switchServiceId;
    private int orderId;

    IsolatedEnvironment(Long serviceId, Long switchServiceId, int orderId) {
        this.serviceId = serviceId;
        this.switchServiceId = switchServiceId;
        this.orderId = orderId;
    }

    @Override
    public void givenExistingVoipService() {
        writeDirectlyToServiceTopic();
        writeDirectlyToSwitchServiceTopic();
    }

    private void writeDirectlyToServiceTopic() {
        sendMessageToKafkaTopic("services", "" + serviceId, createServicePayload());
    }

    private void sendMessageToKafkaTopic(String topic, String key, String value) {
        try {
            getStringStringKafkaProducer().send(createProducerRecord(topic, key, value)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private KafkaProducer<String, String> getStringStringKafkaProducer() {
        return new KafkaProducer<>(kafkaPropertiesForProducer());
    }

    private Properties kafkaPropertiesForProducer() {
        Properties props = new Properties();
        props.put("acks", "all");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getExternalBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        return props;
    }

    private String getExternalBootstrapServers() {
        return "localhost:9092";
    }

    private ProducerRecord createProducerRecord(String topicName, String key, String value) {
        return new ProducerRecord(topicName, key, value);
    }

    private String createServicePayload() {
        return new JsonObject()
                .add("serviceId", serviceId)
                .add("serviceSpecCode", "VoipService")
                .add("directoryNumber", directoryNumber)
                .toString();
    }

    private void writeDirectlyToSwitchServiceTopic() {
        sendMessageToKafkaTopic("voip-switch-services", "" + serviceId, createSwitchServicePayload());
    }

    private String createSwitchServicePayload() {
        return new JsonObject()
                .add("serviceId", serviceId)
                .add("switchServiceId", switchServiceId)
                .toString();
    }

    @Override
    public void writeMessageOntoActiveMq(String traceyId, int orderId) {
        ActiveMqProducer activeMqProducer = new ActiveMqProducer(getActiveMqExternalEndpoint(), ACTIVE_MQ_INCOMING_QUEUE);
        activeMqProducer.start();
        activeMqProducer.write(buildMqPayload(orderId), traceyId, orderId);
    }

    private String getActiveMqExternalEndpoint() {
        return "tcp://localhost:61616";
    }

    private String buildMqPayload(int orderId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
                + "<transaction receivedDate=\"2019-09-04T11:36:24\" operatorId=\"sky\" operatorTransactionId=\"op_trans_id_095025_228\" operatorIssuedDate=\"2011-06-01T09:51:12\"> \n"
                + "  <instruction version=\"1\" type=\"PlaceOrder\"> \n"
                + "    <order> \n"
                + "      <type>modify</type> \n"
                + "      <operatorOrderId>VoipModify_PUXGAN</operatorOrderId> \n"
                + "      <orderId>" + orderId + "</orderId> \n"
                + "    </order> \n"
                + "    <modifyFeaturesInstruction serviceId=\"" + serviceId + "\" operatorOrderId=\"VoipModify_PUXGAN\" operatorNotes=\"Test: successfullyModifyVoiceFeature\"> \n"
                + "      <transactionHeader receivedDate=\"2019-09-04T11:36:24\" operatorId=\"sky\" operatorIssuedDate=\"2011-06-01T09:51:12\"/> \n"
                + "      <features> \n"
                + "        <feature code=\"CallWaiting\"/> \n"
                + "        <feature code=\"ThreeWayCalling\"/> \n"
                + "      </features> \n"
                + "    </modifyFeaturesInstruction> \n"
                + "  </instruction> \n"
                + "</transaction> \n";
    }

    @Override
    public void assertFeaturesChangedOnSwitch() {
        ActiveMqConsumer activeMqConsumer = new ActiveMqConsumer(getActiveMqExternalEndpoint());
        Optional<TextMessage> activeMqResult = activeMqConsumer.run(ACTIVE_MQ_OUTGOING_QUEUE);
        failIfEmpty(activeMqResult);
        TextMessage textMessage = activeMqResult.get();
        try {
            assertPayload(textMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
            fail("Unexpected Exception:" + e);
        }
    }

    private void failIfEmpty(Optional<TextMessage> activeMqResult) {
        assertTrue("No message found on MQ Queue " + ACTIVE_MQ_OUTGOING_QUEUE, activeMqResult.isPresent());
    }

    private void assertPayload(String payload) {
        XmlAssert.assertThat(payload).and(expectedPayload())
                 .ignoreWhitespace()
                 .areIdentical();
    }

    private String expectedPayload() {
        return format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<switchServiceModificationInstruction switchServiceId=\"%s\" netstreamCorrelationId=\"%s\">"
                + "<features>"
                + "<CallWaiting active=\"true\"/>"
                + "<ThreeWayCalling active=\"true\"/>"
                + "</features>"
                + "</switchServiceModificationInstruction>", switchServiceId, orderId)
                ;
    }

}
