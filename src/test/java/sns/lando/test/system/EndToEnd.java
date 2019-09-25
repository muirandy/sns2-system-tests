package sns.lando.test.system;

import com.github.dockerjava.api.model.Container;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class EndToEnd {
    protected static final String KAFKA_BROKER_DOCKER_IMAGE_NAME = "confluentinc/cp-enterprise-kafka:5.3.0";

    private static final File dockerComposeFile = new File(EndToEnd.class.getClassLoader().getResource("docker-compose.yml").getFile());
    private static final File endToEndDockerComposeFile = new File(EndToEnd.class.getClassLoader().getResource("docker-compose-end-to-end.yml").getFile());

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(endToEndDockerComposeFile, dockerComposeFile)
                    .waitingFor("broker_1", Wait.forLogMessage(".*started.*\\n", 1))
                    .waitingFor("control-center_1", Wait.forLogMessage(".*INFO Starting Health Check.*\\n", 1).withStartupTimeout(Duration.ofSeconds(120)))
            .withLocalCompose(true);

    private boolean firstRun = true;
    private String traceyId = UUID.randomUUID().toString();
    private int orderId = Math.abs(new Random().nextInt());
    private Long serviceId = new Random().nextLong();

    private Long switchServiceId = Math.abs(new Random().nextLong());
    private TestEnvironment testEnvironment;

    @Before
    public void setup() {
        testEnvironment = new IsolatedEnvironment(serviceId, switchServiceId);

        if (firstRun)
            doItAll();
        firstRun = false;
    }

    private void doItAll() {
        String zookeeperEndpoint = getZookeeperEndpoint();
        String kafkaBrokerEndpoint = getInternalNetworkKafkaBrokerEndpoint();
        String kafkaBrokerContainerId = getContainerIdFromImage(KAFKA_BROKER_DOCKER_IMAGE_NAME);
        String ksqlCliContainerId = getContainerIdFromImage("confluentinc/cp-ksql-cli:5.3.0");
        String connectServerEndpoint = getConnectServerEndpoint();
        String activeMqEndpoint = getActiveMqInternalEndpoint();
        String elasticSearchInternalNetworkUrl = getElasticSearchInternalNetworkUrl();

        runShellScript("kafka/doItAll.sh",
                zookeeperEndpoint,
                kafkaBrokerEndpoint,
                kafkaBrokerContainerId,
                ksqlCliContainerId,
                connectServerEndpoint,
                activeMqEndpoint,
                elasticSearchInternalNetworkUrl
        );
    }

    private String getZookeeperEndpoint() {
        return "zookeeper:2181";
    }

    private String getInternalNetworkKafkaBrokerEndpoint() {
        return "broker:29092";
    }

    private String getContainerIdFromImage(String imageName) {
        Container container = getContainer(imageName).get();
        return container.getId();
    }

    private Optional<Container> getContainer(String imageName) {
        List<Container> containers = DockerClientFactory.instance().client().listContainersCmd().exec();
        return containers.stream()
                         .filter(c -> c.getImage().contains(imageName))
                         .findAny();
    }

    private String getConnectServerEndpoint() {
        return "localhost:8083/connectors";
    }

    private String getActiveMqInternalEndpoint() {
        return "tcp://activemq:61616";
    }

    private String getElasticSearchInternalNetworkUrl() {
        return "elasticsearch:9200";
    }

    private void runShellScript(String scriptName, String... scriptArguments) {
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

    @Test
    public void endToEnd() {
        givenExistingVoipService();
        whenOperatorIssuesModifyVoiceFeaturesRequest();
        assertFeaturesChangedOnSwitch();
    }

    private void whenOperatorIssuesModifyVoiceFeaturesRequest() {
        testEnvironment.writeMessageOntoActiveMq(traceyId, orderId);
    }

    private void givenExistingVoipService() {
        testEnvironment.givenExistingVoipService();
    }


    private void assertFeaturesChangedOnSwitch() {
        testEnvironment.assertFeaturesChangedOnSwitch();
    }

}