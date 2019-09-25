package sns.lando.test.system;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.jetbrains.annotations.NotNull;

import javax.jms.*;
import java.util.Optional;

public class ActiveMqConsumer implements ExceptionListener {

    private String endpoint;

    public ActiveMqConsumer(String endpoint) {
        this.endpoint = endpoint;
    }

    Optional<TextMessage> run(String queueName) {
        try {
            Connection connection = createConnection();

            Session session = createSession(connection);

            Destination destination = getTheQueue(queueName, session);

            MessageConsumer consumer = createQueueConsumer(session, destination);

            Message message = waitForMessage(consumer);

            if (message instanceof TextMessage)
                return Optional.of((TextMessage) message);
            else
                System.out.println("Received: " + message);

            tidyUp(connection, session, consumer);
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private Connection createConnection() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = createConnectionFactory();
        Connection connection = connectionFactory.createConnection();
        connection.start();
        connection.setExceptionListener(this);
        return connection;
    }

    @NotNull
    private ActiveMQConnectionFactory createConnectionFactory() {
        return new ActiveMQConnectionFactory(endpoint);
    }

    private Session createSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    private Queue getTheQueue(String queueName, Session session) throws JMSException {
        return session.createQueue(queueName);
    }

    private MessageConsumer createQueueConsumer(Session session, Destination destination) throws JMSException {
        return session.createConsumer(destination);
    }

    private Message waitForMessage(MessageConsumer consumer) throws JMSException {
        return consumer.receive(5000);
    }

    private void tidyUp(Connection connection, Session session, MessageConsumer consumer) throws JMSException {
        consumer.close();
        session.close();
        connection.close();
    }

    public synchronized void onException(JMSException ex) {
        System.out.println("JMS Exception occured.  Shutting down client.");
    }

}
