package sns.lando.test.system;

import com.eclipsesource.json.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class SystemShould extends AmqSinkTestBase {

    private static final String SERVICES_TOPIC = "services";
    private static final String SWITCH_SERVICES_TOPIC = "voip-switch-services";

    private Integer voipServiceId = 101;
    private String directoryNumber = "0123456001";
    private Integer voipSwitchServiceId = 5001;
    private String traceyId = UUID.randomUUID().toString();

    @BeforeEach
    public void setUp() {
        super.setUp();
        configureActiveMqSinkConnector();
    }

//    @AfterEach
//    public void createNewIdsForTest() {
//        writeContainerLogsToStdOut();
//        voipServiceId++;
//        voipSwitchServiceId++;
//        directoryNumber = calculateNextDirectoryNumber();
//    }

    private String calculateNextDirectoryNumber() {
        Integer dn = Integer.getInteger(directoryNumber);
        return "0" + 1 + dn;
    }

    @Test
    public void endToEnd() {
        createNewModifyFeaturesRequest();

        checkKnitwareRequestHasBeenSent();
    }

    protected void createNewModifyFeaturesRequest() {
        createAnExistingVoipService();
        modifyVoipFeatures();
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

    protected void checkKnitwareRequestHasBeenSent() {
        assertJmsMessageArrivedOnOutputMqQueue();
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
        String mqPayload = buildMqPayload();
        sendMessageToActiveMq(traceyId, mqPayload);
    }

    private String buildMqPayload() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
                + "<transaction receivedDate=\"2019-09-04T11:36:24\" operatorId=\"sky\" operatorTransactionId=\"op_trans_id_095025_228\" operatorIssuedDate=\"2011-06-01T09:51:12\"> \n"
                + "  <instruction version=\"1\" type=\"PlaceOrder\"> \n"
                + "    <order> \n"
                + "      <type>modify</type> \n"
                + "      <operatorOrderId>VoipModify_PUXGAN</operatorOrderId> \n"
                + "      <orderId>38214522</orderId> \n"
                + "    </order> \n"
                + "    <modifyFeaturesInstruction serviceId=\"34803720\" operatorOrderId=\"VoipModify_PUXGAN\" operatorNotes=\"Test: successfullyModifyVoiceFeature\"> \n"
                + "      <transactionHeader receivedDate=\"2019-09-04T11:36:24\" operatorId=\"sky\" operatorIssuedDate=\"2011-06-01T09:51:12\"/> \n"
                + "      <features> \n"
                + "        <feature code=\"CallWaiting\"/> \n"
                + "        <feature code=\"ThreeWayCalling\"/> \n"
                + "      </features> \n"
                + "    </modifyFeaturesInstruction> \n"
                + "  </instruction> \n"
                + "</transaction> \n";
    }

    private void assertJmsMessageArrivedOnOutputMqQueue() {
        ActiveMqConsumer consumer = new ActiveMqConsumer(getActiveMqJmxEndpoint());
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

    private void writeContainerLogsToStdOut() {
        System.out.println("Kafka Logs = " + KAFKA_CONTAINER.getLogs());
        System.out.println("Active MQ Logs = " + ACTIVE_MQ_CONTAINER.getLogs());
        System.out.println("Kafka Connect Logs = " + kafkaConnectContainer.getLogs());
    }

}
