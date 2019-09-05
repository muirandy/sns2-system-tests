package sns.lando.system;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Testcontainers
public class AmqTestBase extends KafkaTestBase {

    protected static final String KEY_ACTIVE_MQ_JMX_ENDPOINT = "activemq.endpoint";
    private static final String ACTIVEMQ_IMAGE = "rmohr/activemq:latest";
    private static final int ACTIVE_MQ_JMS_PORT = 61616;
    private static final String ACTIVE_MQ_INCOMING_QUEUE = "ColliderToCujo";

    @Container
    protected static final GenericContainer ACTIVE_MQ_CONTAINER = new GenericContainer(ACTIVEMQ_IMAGE)
            .withNetwork(KAFKA_CONTAINER.getNetwork())
            .withExposedPorts(ACTIVE_MQ_JMS_PORT);

    protected String getActiveMqEndpoint() {
        return "tcp://localhost:" + readActiveMqPort();
    }

    protected String getActiveMqJmxEndpoint() {
        return "tcp://" + AmqSinkTestBase.ACTIVE_MQ_CONTAINER.getNetworkAliases().get(0) + ":61616";
    }

    private String readActiveMqPort() {
        return findExposedPortForInternalPort(ACTIVE_MQ_CONTAINER, ACTIVE_MQ_JMS_PORT);
    }

//    private String findExposedPortForInternalPort() {
//        Map<ExposedPort, Ports.Binding[]> bindings = getActiveMqBindings();
//        ExposedPort port = bindings.keySet().stream().filter(k -> ACTIVE_MQ_JMS_PORT == k.getPort())
//                                   .findFirst().get();
//
//        Ports.Binding[] exposedBinding = bindings.get(port);
//        Ports.Binding binding = exposedBinding[0];
//        return binding.getHostPortSpec();
//    }
//
//    protected Map<ExposedPort, Ports.Binding[]> getActiveMqBindings() {
//        return ACTIVE_MQ_CONTAINER.getContainerInfo().getNetworkSettings().getPorts().getBindings();
//    }

    protected void sendMessageToActiveMq(String traceyId, String payload) {
        ActiveMqProducer activeMqProducer = new ActiveMqProducer(getActiveMqEndpoint(), ACTIVE_MQ_INCOMING_QUEUE);
        activeMqProducer.start();
        activeMqProducer.write(payload, traceyId);
    }
}
