package edu.harvard.eecs.airg.coloredtrails.agent;


import java.util.TimerTask;
import java.util.Timer;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import edu.harvard.eecs.airg.coloredtrails.server.ColoredTrailsServer;
import javax.jms.*;

/**
 * Created by IntelliJ IDEA.
 * User: rani
 * Date: May 15, 2008
 * Time: 9:21:57 AM
 */
public class HeartBeat {
        private Logger log = Logger.getRootLogger();
        private Timer timer; 
        
        public class HeartBeatSender extends TimerTask{
            //Communication communication;
            MessageProducer producer;
            Session session;
            String clientName;
            
            public HeartBeatSender(MessageProducer producer, Session session, String clientName){
                //this.communication = communication;
                this.producer = producer;
                this.clientName = clientName;
                this.session = session;
            }
            
            public void run() {
                try {
                    //log.debug("HB");
                    //communication.sendHeartBeat();
                    TextMessage heartbeat = session.createTextMessage();
                    heartbeat.setStringProperty(ColoredTrailsServer.MSGTYPE, ColoredTrailsServer.SERVER_MSG);
                    heartbeat.setStringProperty(ColoredTrailsServer.CLIENT_NAME, clientName);
                    heartbeat.setStringProperty(ColoredTrailsServer.COMMAND, ColoredTrailsServer.HEARTBEAT);
                    producer.send(heartbeat);
                } catch (JMSException ex) {
                    java.util.logging.Logger.getLogger(HeartBeat.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        public HeartBeat(MessageProducer producer, Session session, String clientName){
            log.debug("Starting HB");
            HeartBeatSender hb = new HeartBeatSender(producer, session, clientName);
            timer = new Timer("Heartbeat Timer");
            timer.scheduleAtFixedRate(hb, 0, ColoredTrailsServer.hbInterval);
        }
        
        public void close(){
            timer.cancel();
        }

}
