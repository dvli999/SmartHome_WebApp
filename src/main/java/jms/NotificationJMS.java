package jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class NotificationJMS {
    public static void envoyer(String msg) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = session.createQueue("ALERTE_CONSO");
            MessageProducer producer = session.createProducer(dest);
            TextMessage text = session.createTextMessage(msg);
            producer.send(text);
            producer.close();
            session.close();
            connection.close();
            System.out.println("JMS: message envoyé.");
        } catch (Exception e) { e.printStackTrace(); }
    }
}
