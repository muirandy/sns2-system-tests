package sns.lando.system;

import com.eclipsesource.json.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class AmqSinkTestBase extends AmqTestBase {

    private static final String ACTIVE_MQ_QUEUE_NAME = "TEST.FOO";
    private static final String CONNECTOR_NAME ="banana";
    private static final String CONNECTOR_CLASS =
            "com.aimyourtechnology.kafka.connect.activemq.connector.ActiveMqSinkConnector";
    private static final String KEY_ACTIVE_MQ_QUEUE_NAME = "activemq.queue";
    private static final String KEY_CONNECTOR_CLASS = "connector.class";
    private static final String KEY_CONNECTOR_NAME = "name";
    private static final String KEY_CONFIG = "config";

    private static final String INPUT_TOPIC = "modify.op.msgs";

    private static final String MESSAGE_CONTENT = "A message";
    private static final String STANDARD_KAFKA_CONNECT_TOPICS_KEY = "topics";

    private static final String KEY_KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";


    protected void configureActiveMqSinkConnector() {
        HttpPost httpPost = new HttpPost(getUriForConnectEndpoint());
        HttpEntity httpEntity = new StringEntity(getPayload(), APPLICATION_JSON);

        httpPost.setEntity(httpEntity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpClient.execute(httpPost).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPayload() {
        JsonObject config = new JsonObject()
                .add(KEY_CONNECTOR_CLASS, CONNECTOR_CLASS)
                .add(KEY_ACTIVE_MQ_JMX_ENDPOINT, getActiveMqJmxEndpoint())
                .add(KEY_ACTIVE_MQ_QUEUE_NAME, ACTIVE_MQ_QUEUE_NAME)
                .add(STANDARD_KAFKA_CONNECT_TOPICS_KEY, INPUT_TOPIC)
                .add(KEY_KAFKA_BOOTSTRAP_SERVERS, getKafkaBootstrapServers());
        return new JsonObject()
                .add(KEY_CONNECTOR_NAME, CONNECTOR_NAME)
                .add(KEY_CONFIG, config)
                .toString();
    }

}
