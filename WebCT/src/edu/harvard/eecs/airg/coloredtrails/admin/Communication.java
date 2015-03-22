package edu.harvard.eecs.airg.coloredtrails.admin;

import javax.jms.*;

/**
 * Created by IntelliJ IDEA.
 * User: rani
 * Date: Dec 14, 2007
 * Time: 10:47:19 AM
 */
public class Communication {
    private static Communication instance = null;
    protected Communication(){

    }
    public static Communication getInstance(){
        if (instance == null){
            instance = new Communication();
        }
        return instance;
    }

    public MessageProducer getProducer() {
        return producer;
    }

    public void setProducer(MessageProducer producer) {
        this.producer = producer;
    }

    public MessageConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(MessageConsumer consumer) {
        this.consumer = consumer;
    }

    /* JMS data structures */
    private MessageProducer producer = null;
    private MessageConsumer consumer = null;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    private Session session = null;
    private Topic topic = null;
    private Connection connection = null;

}
