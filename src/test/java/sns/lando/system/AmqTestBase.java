package sns.lando.system;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.Map;

public class AmqTestBase extends KafkaTestBase {

    protected static final String KEY_ACTIVE_MQ_JMX_ENDPOINT = "activemq.endpoint";
    private static final String ACTIVEMQ_IMAGE = "rmohr/activemq:latest";
    private static final int ACTIVE_MQ_JMS_PORT = 61616;

    @Container
    protected static final GenericContainer ACTIVE_MQ_CONTAINER = new GenericContainer(ACTIVEMQ_IMAGE)
            .withNetwork(KAFKA_CONTAINER.getNetwork())
            .withExposedPorts(ACTIVE_MQ_JMS_PORT);

    protected Map<ExposedPort, Ports.Binding[]> getActiveMqBindings() {
        return ACTIVE_MQ_CONTAINER.getContainerInfo().getNetworkSettings().getPorts().getBindings();
    }

    protected String readActiveMqPort() {
        return findExposedPortForInternalPort();
    }

    private String findExposedPortForInternalPort() {
        Map<ExposedPort, Ports.Binding[]> bindings = getActiveMqBindings();
        ExposedPort port = bindings.keySet().stream().filter(k -> ACTIVE_MQ_JMS_PORT == k.getPort())
                                   .findFirst().get();

        Ports.Binding[] exposedBinding = bindings.get(port);
        Ports.Binding binding = exposedBinding[0];
        return binding.getHostPortSpec();
    }
}
