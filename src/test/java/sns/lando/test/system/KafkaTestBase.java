package sns.lando.test.system;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

@Testcontainers
public abstract class KafkaTestBase {
    private static final String KAFKA_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String KAFKA_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";


    @Container
    protected static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer("5.3.0").withEmbeddedZookeeper()
                                                                                       .withNetworkAliases("broker")
                                                                                       .waitingFor(Wait.forLogMessage(".*started.*\\n", 1));

    @Container
    protected GenericContainer kafkaConnectContainer = new GenericContainer(
            new ImageFromDockerfile()
//                    .withFileFromClasspath("confluentinc-kafka-connect-activemq-5.3.0.zip", "kafka-connect/confluentinc-kafka-connect-activemq-5.3.0.zip")
                    .withFileFromClasspath("Dockerfile", "kafka-connect/Dockerfile"))
            .withEnv(calculateConnectEnvProperties())
            .withNetwork(KAFKA_CONTAINER.getNetwork())
            .waitingFor(Wait.forLogMessage(".*Finished starting connectors and tasks.*\\n", 1));

    private File getDockerfileFile() {
        return new File("kafka-connect/Dockerfile");
    }

    private Map<String, String> calculateConnectEnvProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("CONNECT_BOOTSTRAP_SERVERS", getKafkaBootstrapServers());
        properties.put("CONNECT_GROUP_ID", "service-test-connect-group");
        properties.put("CONNECT_REST_PORT", "8083");
        properties.put("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1");
        properties.put("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1");
        properties.put("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1");
        properties.put("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.storage.StringConverter");
        properties.put("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.storage.StringConverter");
        properties.put("CONNECT_INTERNAL_KEY_CONVERTER", "org.apache.kafka.connect.json.JsonConverter");
        properties.put("CONNECT_INTERNAL_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter");
        properties.put("CONNECT_KEY_CONVERTER_SCHEMAS_ENABLE", "false");
        properties.put("CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE", "false");
        properties.put("CONNECT_CONFIG_STORAGE_TOPIC", "docker-connect-configs");
        properties.put("CONNECT_OFFSET_STORAGE_TOPIC", "docker-connect-offsets");
        properties.put("CONNECT_STATUS_STORAGE_TOPIC", "docker-connect-status");
        properties.put("CONNECT_REST_ADVERTISED_HOST_NAME", "connect");
        properties.put("CONNECT_PLUGIN_PATH", "/usr/share/java");

//        createKafkaTopics();

        return properties;
    }

    protected String getKafkaBootstrapServers() {
        return KAFKA_CONTAINER.getNetworkAliases().get(0) + ":9092";
    }

    protected String getKafkaBootstrapServersByIp() {
        return KAFKA_CONTAINER.getContainerIpAddress() + ":9092";
    }

    protected String getUriForConnectEndpoint() {
        return "http://" + getServerForConnectEndpoint();
    }

    protected String getServerForConnectEndpoint() {
        String port = findExposedPortForInternalPort(kafkaConnectContainer, 8083);
        return "localhost:" + port + "/connectors";
    }

    protected String findExposedPortForInternalPort(GenericContainer container, int internalPort) {
        Map<ExposedPort, Ports.Binding[]> bindings = getContainerBindings(container);
        ExposedPort port = bindings.keySet().stream().filter(k -> internalPort == k.getPort())
                                   .findFirst().get();

        Ports.Binding[] exposedBinding = bindings.get(port);
        Ports.Binding binding = exposedBinding[0];
        return binding.getHostPortSpec();
    }

    private Map<ExposedPort, Ports.Binding[]> getContainerBindings(GenericContainer container) {
        return container.getContainerInfo().getNetworkSettings().getPorts().getBindings();
    }

    @BeforeEach
    void setUp() {

    }

    protected static Properties kafkaPropertiesForProducer() {
        Properties props = new Properties();
        props.put("acks", "all");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KAFKA_SERIALIZER);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KAFKA_DESERIALIZER);
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, ActiveMqSinkServiceTest.class.getName());
        return props;
    }

    protected <K,V> ProducerRecord createProducerRecord(String topicName, K key, V message) {
        return new ProducerRecord<K,V>(topicName, key, message);
    }

    protected void createKafkaConnector(String kafkaConnectorPayload) {
        HttpPost httpPost = new HttpPost(getUriForConnectEndpoint());
        HttpEntity httpEntity = new StringEntity(kafkaConnectorPayload, APPLICATION_JSON);

        httpPost.setEntity(httpEntity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpClient.execute(httpPost).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void runConnectScript(String scriptName, String... scriptArguments) {
        runShellScript(scriptName, scriptArguments);
    }

    private void runShellScript(String scriptName, String[] scriptArguments) {
        File connectorScript = new File(getClass().getClassLoader().getResource(scriptName).getFile());
        String absolutePath = connectorScript.getAbsolutePath();

        String arguments = Arrays.stream(scriptArguments)
                                 .collect(Collectors.joining(" "));

        String cmd = absolutePath + " " + arguments;
        try {
            Process process = Runtime.getRuntime().exec(cmd);

            InputStream cmdStdErr = process.getErrorStream();
            InputStream cmdStdOut = process.getInputStream();

            process.waitFor();

            writeOutStreamToConsole(cmdStdOut);
            writeOutStreamToConsole(cmdStdErr);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void writeOutStreamToConsole(InputStream cmdStdOut) throws IOException {
        String line;
        BufferedReader stdOut = new BufferedReader(new InputStreamReader(cmdStdOut));
        while ((line = stdOut.readLine()) != null)
            System.out.println(line);

        cmdStdOut.close();
    }
}
