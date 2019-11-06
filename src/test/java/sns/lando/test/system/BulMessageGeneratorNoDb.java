package sns.lando.test.system;

import com.eclipsesource.json.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class BulMessageGeneratorNoDb {
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String ACTIVE_MQ_INCOMING_QUEUE = "ColliderToCujo";

    private String directoryNumber;
    private Long serviceId;
    private Long switchServiceId;
    private int orderId;
    private String traceyId;
    private ActiveMqProducer activeMqProducer;

    public static void main(String[] args) {
        BulMessageGeneratorNoDb generator = new BulMessageGeneratorNoDb();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                    System.out.println("Shutting down ...");
                    generator.stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });
        generator.run();
    }


    private void run() {
        start();
        while (true) {
            createNewServiceIds();
            createExistingVoipService();
            randomDelay();
            writeMessageOntoActiveMq();
        }
    }

    private void start() {
        activeMqProducer = new ActiveMqProducer(getActiveMqExternalEndpoint(), ACTIVE_MQ_INCOMING_QUEUE);
        activeMqProducer.start();
    }

    private void stop() {
        activeMqProducer.stop();
    }

    private void randomDelay() {
        int start = 100;
        int end = 2000;

        int delay = start + Math.abs(new Random().nextInt(end - start));

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createNewServiceIds() {
        serviceId = Math.abs(new Random().nextLong());
        switchServiceId = Math.abs(new Random().nextLong());
        orderId = Math.abs(new Random().nextInt());
        directoryNumber = "011" + new Random().nextInt();
        traceyId = "" + Math.abs(new Random().nextLong());
    }

    public void createExistingVoipService() {
        writeDirectlyToServiceTopic();
        writeDirectlyToSwitchServiceTopic();
    }

    private void writeDirectlyToServiceTopic() {
        sendMessageToKafkaTopic("services", "" + serviceId, createServicePayload());
    }

    private String createServicePayload() {
        return new JsonObject()
                .add("serviceId", serviceId)
                .add("serviceSpecCode", "VoipService")
                .add("directoryNumber", directoryNumber)
                .toString();
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

    private void writeDirectlyToSwitchServiceTopic() {
        sendMessageToKafkaTopic("voip-switch-services", "" + serviceId, createSwitchServicePayload());
    }

    private String createSwitchServicePayload() {
        return new JsonObject()
                .add("serviceId", serviceId)
                .add("switchServiceId", switchServiceId)
                .toString();
    }

    public void writeMessageOntoActiveMq() {

        String payload = buildMqPayload();
        activeMqProducer.write(payload, traceyId, orderId);
    }

    private String getActiveMqExternalEndpoint() {
        return "tcp://localhost:61616";
    }

    private String buildMqPayload() {
        String mqDate = getMqDate();
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
                + "<transaction receivedDate=\"" + mqDate + "\" operatorId=\"sky\" operatorTransactionId=\"op_trans_id_095025_228\" operatorIssuedDate=\"" + mqDate + "\"> \n"
                + "  <instruction version=\"1\" type=\"PlaceOrder\"> \n"
                + "    <order> \n"
                + "      <type>modify</type> \n"
                + "      <operatorOrderId>VoipModify_PUXGAN</operatorOrderId> \n"
                + "      <orderId>" + orderId + "</orderId> \n"
                + "    </order> \n"
                + "    <modifyFeaturesInstruction serviceId=\"" + serviceId + "\" operatorOrderId=\"VoipModify_PUXGAN\" operatorNotes=\"Test: successfullyModifyVoiceFeature\"> \n"
                + "      <transactionHeader receivedDate=\"" + mqDate + "\" operatorId=\"sky\" operatorIssuedDate=\"" + mqDate + "\"/> \n"
                + "      <features> \n"
                + "        <feature code=\"CallWaiting\"/> \n"
                + "        <feature code=\"ThreeWayCalling\"/> \n"
                + "      </features> \n"
                + "    </modifyFeaturesInstruction> \n"
                + "  </instruction> \n"
                + "</transaction> \n";
    }

    private String getMqDate() {
        return "2019-06-01T09:51:12";
    }
}
