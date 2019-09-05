package sns.lando.system;

import com.eclipsesource.json.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SystemShould extends AmqSinkTestBase {

    private static final String SERVICES_TOPIC = "services";
    private static final String SWITCH_SERVICES_TOPIC = "voip-switch-services";

    private Integer voipServiceId = 100;
    private String directoryNumber = "0123456001";
    private Integer voipSwitchServiceId;

    @Before
    public void setUp() {
        createNewIdsForTest();
        configureActiveMqSinkConnector();
    }

    @After
    private void createNewIdsForTest() {
        voipServiceId++;
        voipSwitchServiceId++;
        directoryNumber = calculateNextDirectoryNumber();
    }

    private String calculateNextDirectoryNumber() {
        int current = Integer.getInteger(directoryNumber);
        current++;
        return "0" + current;
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
            getStringStringKafkaProducer().send(createProducerRecord(SERVICES_TOPIC, buildServiceKey(), buildServiceMessage())).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//        lluStreamMessagesTopicProducer.flush()
//        lluStreamMessagesTopicProducer.close()
    }

    @NotNull
    private KafkaProducer<String, String> getStringStringKafkaProducer() {
        return new KafkaProducer<>(kafkaPropertiesForProducer());
    }

    private String buildServiceKey() {
        return "" + voipServiceId;
    }

    //{"serviceId":31642339,"serviceSpecCode":"VoipService","directoryNumber":"01202000095"}
    private String buildServiceMessage() {
        return new JsonObject()
                .add("serviceId", voipServiceId)
                .add("serviceSpecCode", "VoipService")
                .add("directoryNumber", directoryNumber)
                .toString();
    }

    private void createSwitchServiceDirectlyInKafka() {
        try {
            getStringStringKafkaProducer().send(createProducerRecord(SWITCH_SERVICES_TOPIC, buildServiceKey(), buildSwitchServiceMessage())).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private String buildSwitchServiceMessage() {
        return new JsonObject()
                .add("serviceId", voipServiceId)
                .add("switchServiceId", voipSwitchServiceId)
                .toString();
    }

    private void modifyVoipFeatures() {
        writeModifyVoipFeaturesInstructionToActiveMq();
    }

    private void writeModifyVoipFeaturesInstructionToActiveMq() {

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
