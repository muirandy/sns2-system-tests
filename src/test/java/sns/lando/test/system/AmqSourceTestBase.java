package sns.lando.test.system;

import com.eclipsesource.json.JsonObject;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AmqSourceTestBase extends AmqSinkTestBase {

    private static final String CONNECTOR_CLASS =
            "io.confluent.connect.activemq.ActiveMQSourceConnector";
    private static final String KEY_CONNECTOR_CLASS = "connector.class";
    private static final String KEY_CONNECTOR_NAME = "name";
    private static final String KEY_CONFIG = "config";


    protected void configureActiveMqSourceConnector() {
        runConnectScript("kafka-connect/createActiveMqSourceConnector.sh",
                getKafkaBootstrapServersByIp(),
                getServerForConnectEndpoint(),
                getActiveMqEndpoint()
                );
        createKafkaConnector(getAmqConnectorPayload());
    }

    private void runConnectScript(String scriptName, String... scriptArguments) {
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

    private String getAmqConnectorPayload() {
        JsonObject config = new JsonObject()
                .add(KEY_CONNECTOR_CLASS, CONNECTOR_CLASS)
                .add("confluent.topic", "incoming.activemq")
                .add("kafka.topic", "incoming.activemq")
                .add("activemq.url", getActiveMqJmxEndpoint())
                .add("jms.destination.name", "ColliderToCujo")
                .add("jms.destination.type", "queue")
                .add("confluent.topic.bootstrap.servers", getKafkaBootstrapServers())
                .add("confluent.topic.replication.factor", 1)
                .add("transforms", "Rename")
                .add("transforms.Rename.type", "org.apache.kafka.connect.transforms.ReplaceField$Value")
                .add("transforms.Rename.renames", "properties:jmsProperties");
        return new JsonObject()
                .add(KEY_CONNECTOR_NAME, "activeMqSourceConnector")
                .add(KEY_CONFIG, config)
                .toString();
    }
}