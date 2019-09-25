package sns.lando.test.system;

import com.eclipsesource.json.JsonObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

class IsolatedEnvironment {
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    private String directoryNumber = "011" + new Random().nextInt();
    private Long serviceId;
    private Long switchServiceId;

    IsolatedEnvironment(Long serviceId, Long switchServiceId) {
        this.serviceId = serviceId;
        this.switchServiceId = switchServiceId;
    }

    void givenExistingVoipService() {
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
                .toString()
                ;
    }

}
