package edu.harvard.eecs.airg.coloredtrails.server;

/**
 * Created by IntelliJ IDEA.
 * User: rani
 * Date: May 22, 2008
 * Time: 12:20:00 PM
 */
public class ClientState extends Thread {

//    public void setTick(boolean tick) {
//        this.tick = tick;
//    }

    
    //Some synchronized stuff here?
    
    private EClientState state;
    CtrlCommands ctrlCommands;
    int myPin = -1;
    private long failTime;
    private long lastTime;

    public enum EClientState{
        ACTIVE,
        INACTIVE,
        FAILED,
        RESUMED,
    }

    public void updateState(){
//        System.out.println("update");
        if((System.currentTimeMillis() - 8*ColoredTrailsServer.hbInterval) > lastTime){
            if(state == EClientState.ACTIVE){
                state = EClientState.FAILED;
                failTime = System.currentTimeMillis();
                System.out.println("Client " + myPin + " disconnected!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                ctrlCommands.clientDisconnected(myPin);
            }
        }
        else {
            switch (state){
                case ACTIVE: state = EClientState.ACTIVE;
                case INACTIVE: state = EClientState.ACTIVE;
                case FAILED: state = EClientState.RESUMED;
                case RESUMED: state = EClientState.ACTIVE;
            }
        }
        ServerData.getInstance().getPlayerConnection(myPin).setEClientState(state);
        
    }

    public ClientState(CtrlCommands ctrlCommands, int myPin){
         state   = EClientState.INACTIVE;
         this.ctrlCommands = ctrlCommands;
         this.myPin = myPin;
         //System.out.println("new client");
    }

    public void heartBeatReceived(){
        lastTime = System.currentTimeMillis();
        updateState();
    }

    public void run(){

        while (true){
            try{
                this.sleep(ColoredTrailsServer.hbInterval);
            }
            catch (InterruptedException ex){
                ex.printStackTrace();
            }
            // woke up, check the tick
            updateState();
        }
    }
    
    public EClientState getEClientState(){
        return(state);
    }
}

