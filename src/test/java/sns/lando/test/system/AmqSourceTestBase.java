package sns.lando.test.system;

import com.eclipsesource.json.JsonObject;

public class AmqSourceTestBase extends AmqSinkTestBase {

    private static final String CONNECTOR_CLASS =
            "io.confluent.connect.activemq.ActiveMQSourceConnector";
    private static final String KEY_CONNECTOR_CLASS = "connector.class";
    private static final String KEY_CONNECTOR_NAME = "name";
    private static final String KEY_CONFIG = "config";


    protected void configureActiveMqSourceConnector() {
        createKafkaConnector(getAmqConnectorPayload());
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
