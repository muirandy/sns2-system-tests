package sns.lando.system;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SystemShould extends AmqSinkTestBase {

    private static final String SERVICES_TOPIC = "services";

    @Before
    public void setUp() {
        configureActiveMqSinkConnector();
    }

    @Test
    public void t() {
        createNewModifyFeaturesRequest();

        checkKnitwareRequestHasBeenSent();
    }

    protected void createNewModifyFeaturesRequest() {
        createAnExistingVoipService();
        modifyVoipFeatures();
    }

    protected void checkKnitwareRequestHasBeenSent() {
        assertJmsMessageArrivedOnOutputMqQueue();
    }

    private void createAnExistingVoipService() {
        createVoipServiceDirectlyInKafka();
        createSwitchServiceDirectlyInKafka();
    }

    private void createVoipServiceDirectlyInKafka() {
        try {
            KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaPropertiesForProducer());
            kafkaProducer.send(createProducerRecord(SERVICES_TOPIC, buildServiceKey(), buildServiceMessage())).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//        lluStreamMessagesTopicProducer.flush()
//        lluStreamMessagesTopicProducer.close()
    }

    private String buildServiceKey() {
        return null;
    }

    private String buildServiceMessage() {
        return null;
    }

    private void createSwitchServiceDirectlyInKafka() {

    }

    private void modifyVoipFeatures() {

    }

    private void assertJmsMessageArrivedOnOutputMqQueue() {
        ActiveMqConsumer consumer = new ActiveMqConsumer(readActiveMqPort());
        String messageFromActiveMqQueue = consumer.run();
        assertEquals(expectedKnitwareMessage(), messageFromActiveMqQueue);
    }

    private String expectedKnitwareMessage() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<switchServiceModificationInstruction switchServiceId=\"1\" netstreamCorrelationId=\"${orderId}\">"
                + "<features>"
                + "<callerDisplay active=\"true\"/>"
                + "<ringBack active=\"true\"/>"
                + "<chooseToRefuse active=\"true\"/>"
                + "</features>"
                + "</switchServiceModificationInstruction>";
    }

}
