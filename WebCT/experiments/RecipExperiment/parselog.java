package RecipExperiment;

import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import edu.harvard.eecs.airg.coloredtrails.shared.logobject;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.util.Set;

public class parselog{
    public static void main(String[] args){
        
        ArrayList<logobject> objects = new ArrayList<logobject>();
        
        
        String filename = "";
        if(args.length > 0){
            for(int i = 0; i < args.length; i++)
                System.out.println(i + ": " + args[i]);
            filename = args[0];
        } else {
            return;
        }
        
        try {
            ObjectInputStream oIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
            logobject o;
            Boolean read = true;
            while(read){
                try {
                    o = (logobject) oIn.readObject();
                    if(o.getDescriptor().equals("done"))
                        read = false;
                    else
                        objects.add(o);
                } catch (IOException ex) {
                    Logger.getLogger(parselog.class.getName()).log(Level.SEVERE, null, ex);
                    oIn.close();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(parselog.class.getName()).log(Level.SEVERE, null, ex);
                }
                //System.out.println("obect read");
            }
            oIn.close();
        } catch (IOException ex) {
            Logger.getLogger(parselog.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<ArrayList<logobject>> roundsplit = new ArrayList<ArrayList<logobject>>();
        boolean ingame = false;
        ArrayList<logobject> templist = new ArrayList<logobject>();
        ArrayList<logobject> metadata = new ArrayList<logobject>();
        
        for(logobject o : objects){
            System.out.println(o.getTimestamp() + ": object: " + o.getDescriptor() + " " + o.getObject().getClass());
            
            
            
            if((o.getDescriptor().length() > 4) && o.getDescriptor().substring(0, 5).equals("round")){
                System.out.println("new round found! " + o.getDescriptor());
                
                if(ingame == false){
                    ingame = true;
                    templist = new ArrayList<logobject>();
                } else {
                    roundsplit.add(templist);
                    templist = new ArrayList<logobject>();
                }
                
                
            }
            
            if(ingame)
                templist.add(o);
            else
                metadata.add(o);
            
        }
        roundsplit.add(templist);
        
        String s;
        
        ChipSet offer = null;
        GameStatus gs = null;
        Boolean accepted = false;
        Set<PlayerStatus> Players = null;
        double nnP = 0, nnR = 0, offerP = 0, offerR = 0;
        
        int i = 0;
        for(ArrayList<logobject> l : roundsplit){
            offer = null;
            gs = null;
            accepted = false;
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("In round: " + i);
            
            for(logobject o : l){
//                System.out.println(o.getTimestamp() + ": object: " + o.getDescriptor() + " " + o.getObject().getClass());
                s = o.getDescriptor();
                if(s.contains("nnP")){
//                    System.out.println("nnP: " + (Double)o.getObject() );
                    nnP = (Double)o.getObject();
                    
                } else if( s.contains("nnR") ) {
//                    System.out.println("nnR: " + (Double)o.getObject() );
                    nnR = (Double)o.getObject();
                    
                } else if( s.contains("offerP") ) {
//                    System.out.println("scoreP: " + (Double)o.getObject() );
                    offerP = (Double)o.getObject();
                    
                } else if( s.equals("offerR") ) {
//                    System.out.println("scoreR: " + (Double)o.getObject() );
                    offerR = (Double)o.getObject();
                    
                } else if( s.contains("offerresponse") ) {
                    System.out.println("Offer accepted: " + (Boolean)o.getObject() );
                    if( (Boolean)o.getObject() )
                        accepted = true;
                    
                } else if( s.equals("offer") ) {
                    offer = (ChipSet) o.getObject();
                    
                } else if( s.equals("round" + i) ) { 
                    gs = (GameStatus) o.getObject();
                } else if( s.equals("board") ) {
                    System.out.println(o.getObject().toString());
                }
            }
            System.out.println("gs: " + gs);
            System.out.println(gs.getBoard().toString());
            for(PlayerStatus ps : gs.getPlayers() ) {
                System.out.println("Player: " + ps.getPerGameId() + " " + ps.getRole() + " pin: " + ps.getPin() + " " + ps.getPosition());
                System.out.println("chipset: " + ps.getChips() );
//                for(String ss : ps.getFeatures())
//                    System.out.println("key: " + ss);
//                PlayerConnection pc = (PlayerConnection)ps.get("pc");
//                System.out.println(pc.toString());
//                System.out.println( "test of chips for: " +  (ChipSet)ps.get("chips"));
            }
            System.out.println("offer: " + offer + " accepted: " + accepted);
            System.out.println("nnP " + nnP + " nnR " + nnR + " offerP " + offerP + " offerR " + offerR);
            i++;
        }
        
    }
}