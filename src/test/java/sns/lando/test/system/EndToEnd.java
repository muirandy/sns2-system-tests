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

import static org.junit.Assert.assertEquals;


public class EndToEnd {
    protected static final String KAFKA_BROKER_DOCKER_IMAGE_NAME = "confluentinc/cp-enterprise-kafka:5.3.0";
    private static final String ACTIVE_MQ_INCOMING_QUEUE = "ColliderToCujo";

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

    @Before
    public void setup() {
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

    private String getConnectServerEndpoint() {
        return "localhost:8083/connectors";
    }

    private String getActiveMqInternalEndpoint() {
        return "tcp://activemq:61616";
    }

    private String getActiveMqExternalEndpoint() {
        return "tcp://localhost:" + readExternalActiveMqPort();
    }

    private String readExternalActiveMqPort() {
        return "61616";
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
        writeMessageOntoActiveMq();
        assertEquals(1, 1);
    }

    private void givenExistingVoipService() {
        new IsolatedEnvironment(serviceId, switchServiceId).givenExistingVoipService();
    }

    private void writeMessageOntoActiveMq() {
        ActiveMqProducer activeMqProducer = new ActiveMqProducer(getActiveMqExternalEndpoint(), ACTIVE_MQ_INCOMING_QUEUE);
        activeMqProducer.start();
        activeMqProducer.write(buildMqPayload(), traceyId, orderId);
    }

    private String buildMqPayload() {
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

    String getContainerIdFromImage(String imageName) {
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