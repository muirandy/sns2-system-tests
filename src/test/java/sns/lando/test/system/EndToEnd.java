package sns.lando.test.system;

import com.github.dockerjava.api.model.Container;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


//@Testcontainers
public class EndToEnd {
    private static final int ZOOKEEPER_PORT = 2181;
    private static final int KAFKA_PORT = 9092;
    private static final int SCHEMA_REGISTRY_PORT = 8081;
    private static final int CONNECT_PORT = 8083;
    private static final int KSQL_PORT = 8088;

    private static final File file = new File(EndToEnd.class.getClassLoader().getResource("docker-compose.yml").getFile());
    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(file)
                    .waitingFor("broker_1", Wait.forLogMessage(".*started.*\\n", 1))
                    .waitingFor("control-center_1", Wait.forLogMessage(".*INFO Starting Health Check.*\\n", 1).withStartupTimeout(Duration.ofSeconds(120)))
//                    .waitingFor("ksql-server_1", Wait.forLogMessage(".*INFO Server up and running.*\\n", 1))
//                    .withExposedService("zookeeper_1", ZOOKEEPER_PORT, new WaitAllStrategy().withStartupTimeout(Duration.ofSeconds(10)))
//                    .withExposedService("broker_1", KAFKA_PORT)
//                    .withExposedService("broker", KAFKA_PORT, Wait.forListeningPort())
//                    .withExposedService("broker", KAFKA_PORT, Wait.forLogMessage(".*started.*\\n", 1))
//                    .withExposedService("schema-registry_1", SCHEMA_REGISTRY_PORT, new WaitAllStrategy().withStartupTimeout(Duration.ofSeconds(10)))
//                    .withExposedService("connect_1", CONNECT_PORT, new WaitAllStrategy().withStartupTimeout(Duration.ofSeconds(10)))
//                    .withExposedService("ksql-server_1", KSQL_PORT, new WaitAllStrategy().withStartupTimeout(Duration.ofSeconds(10)))
//.withLogConsumer()
            .withLocalCompose(true);

    @Test
    public void t() {
        //File connectorScript = new File(getClass().getClassLoader().getResource(scriptName).getFile());
        check();
    }

    private void check() {
        String zookeeperEndpoint = getZookeeperEndpoint();
        String kafkaBrokerEndpoint = getInternalNetworkKafkaBrokerEndpoint();
        String kafkaBrokerContainerId = getContainerIdFromImage("confluentinc/cp-enterprise-kafka:5.3.0");
        String ksqlCliContainerId = getContainerIdFromImage("confluentinc/cp-ksql-cli:5.3.0");
        String connectServerEndpoint = getConnectServerEndpoint();
        String activeMqEndpoint = getActiveMqEndpoint();
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
        assertEquals(1, 1);
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

    private String getActiveMqEndpoint() {
        return "tcp://localhost:61616";
    }

    private String getElasticSearchInternalNetworkUrl() {
        return "elasticsearch:9200";
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
}
