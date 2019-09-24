package sns.lando.test.system;

import com.github.dockerjava.api.model.Container;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.testcontainers.DockerClientFactory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public abstract class KafkaActor {
    protected static final String KAFKA_BROKER_DOCKER_IMAGE_NAME = "confluentinc/cp-enterprise-kafka:5.3.0";
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

    protected void sendMessageToKafkaTopic(String topic, String key, String value) {
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

    protected Properties kafkaPropertiesForProducer() {
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

    protected ProducerRecord createProducerRecord(String topicName, String key, String value) {
        return new ProducerRecord(topicName, key, value);
    }

    protected String getContainerIdFromImage(String imageName) {
        Container container = getContainer(imageName).get();
        return container.getId();
    }

    private Optional<Container> getContainer(String imageName) {
        List<Container> containers = DockerClientFactory.instance().client().listContainersCmd().exec();
        return containers.stream()
                         .filter(c -> c.getImage().contains(imageName))
                         .findAny();
    }
}
