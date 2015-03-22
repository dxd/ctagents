package ctagents.recipagents;

import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhaseWaiter {

    Phases phases = null;
    Random localrand;
    
    public PhaseWaiter(Phases ph) {
        phases = ph;
        localrand = new Random();
    }
    
    public void doWait(int minSeconds, int maxSeconds){
        
        if( maxSeconds > (phases.getCurrentPhase().getDuration() - 5 ))
            maxSeconds = phases.getCurrentPhase().getDuration() - 10;
        
        int waitTime = localrand.nextInt(maxSeconds - minSeconds) + minSeconds;
        
        
        if(phases.getCurrentSecsElapsed() > waitTime){
            return;
        }else{
            System.out.println("Need to wait " + (waitTime - phases.getCurrentSecsElapsed()) + " more seconds");
            try {
                Thread.sleep( (waitTime - phases.getCurrentSecsElapsed()) * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PhaseWaiter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}