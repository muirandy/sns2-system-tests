package sns.lando.test.system;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

class ActiveMqProducer {
    private final String activeMqEndpoint;
    private final String activeMqQueueName;
    private Connection connection;

    ActiveMqProducer(String activeMqEndpoint, String activeMqQueueName) {
        this.activeMqEndpoint = activeMqEndpoint;
        this.activeMqQueueName = activeMqQueueName;
    }

    public void start() {
        ActiveMQConnectionFactory factory = createConnectionFactory(activeMqEndpoint);
        try {
            connection = factory.createConnection();
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    ActiveMQConnectionFactory createConnectionFactory(String activeMqConnectionString) {
        return new ActiveMQConnectionFactory(activeMqConnectionString);
    }

    void stop() {
        try {
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    void write(String message, String traceyId, int orderId) {
        ActiveMQConnectionFactory factory = createConnectionFactory(activeMqEndpoint);
        try {
            //            Connection connection = factory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(activeMqQueueName);
            MessageProducer producer = session.createProducer(queue);
            TextMessage textMessage = session.createTextMessage(message);
            textMessage.setStringProperty("TRACEY_ID", traceyId);
            textMessage.setStringProperty("JMSXGroupID", "" + orderId);
            producer.send(textMessage);
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
