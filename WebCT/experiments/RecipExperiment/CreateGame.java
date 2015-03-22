package RecipExperiment;

import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CreateGame {

    int numRows = 4;
    int numCols = 4;
    
    int ProposerID = 0;
    int ResponderID = 1;
    
    Random localrand = new Random();
    
    GameStatus gs;
    
    Scoring s = new Scoring(100, -15, 10, 50);
    GamePalette gp;
    PlayerStatus p1;
    PlayerStatus p2;
    Board b;
    
    public ArrayList<GameStatus> getGames(int n){
        ArrayList<GameStatus> games = new ArrayList<GameStatus>();
        for( int i = 0; i < n; i++ ){
            games.add(getGame());
        }
        return(games);
    }
    
    public GameStatus getGame(){
        gs = new GameStatus();
        gs.setScoring(s);
        
        GamePalette gp = new GamePalette();
        gp.add("CTRed");
        gp.add("CTGreen");
        gp.add("CTBlue");
        gp.add("CTPurple");
        gs.setGamePalette(gp);
        
        HashSet<PlayerStatus> pps = new HashSet<PlayerStatus>();
        p1 = new PlayerStatus();
        p2 = new PlayerStatus();
        
        pps.add(p1);
        pps.add(p2);
        
        for( PlayerStatus player : pps )
            player.setChips( getRandChipSet( gp ) );
        
        p1.setPosition(new RowCol(localrand.nextInt(4), localrand.nextInt(4)));
        p2.setPosition(new RowCol(localrand.nextInt(4), localrand.nextInt(4)));
        
        p1.setPerGameId(0);
        p2.setPerGameId(1);
        
        gs.setPlayers( pps);
        
//        for(PlayerStatus p : gs.getPlayers())
//            System.out.println(p.toString());
        
        Board b;
        do {
            b = getRandBoard(gp);
        } while(!checkwinnable(gs, b, pps, s));
        gs.setBoard(b);
        
        return(gs);
    }

    protected static ChipSet getRandChipSet( GamePalette gp ) {
        ChipSet chipset = new ChipSet();
        for( String color : gp.getColors() )
            chipset.set( color, 0 );
        //int maxchips = localrand.nextInt(4);
        for(int i = 0; i < 4; i++)
            chipset.add(gp.getRandomColor(), 1);
        return chipset;
    }
    
    private Board getRandBoard(GamePalette gp){
        Board board = getRandomBoard(gp, numRows, numCols);
        RowCol goalpos;
        do{
            goalpos = new RowCol(localrand.nextInt(board.getRows()), localrand.nextInt(board.getCols()));
            if((!goalpos.equals(gs.getPlayerByPerGameId(ProposerID).getPosition()) ) && (!goalpos.equals(gs.getPlayerByPerGameId(ResponderID).getPosition()) ))                
                break;
        }while (true);
        board.setGoal(goalpos, true);
        return(board);
    }
    
    private boolean checkwinnable(GameStatus gs, Board b, Set<PlayerStatus> pps, Scoring s){
        //ChipSet together = ChipSet.addChipSets(gs.getPlayerByPerGameId(ProposerID).getChips(), gs.getPlayerByPerGameId(ResponderID).getChips());
        ChipSet together = new ChipSet();
        for(PlayerStatus ps : pps)
            together = ChipSet.addChipSets(together, ps.getChips());
        
        if(together.getNumChips() == 0)
            return(false);
        
        
        //Check to make sure that both players cannot reach the goal
        if( ((new BestUse(b, gs.getPlayerByPerGameId(0), s, 0)).getBestState().getPosition().dist(b.getGoalLocations().get(0)) == 0) 
                && ((new BestUse(b, gs.getPlayerByPerGameId(1), s, 0)).getBestState().getPosition().dist(b.getGoalLocations().get(0)) == 0) ){
            return(false);
        }
        
        
        //Check to make sure that some offer will get to the goal
        PlayerStatus tempps = new PlayerStatus();
        
        tempps.setChips(together);

        for(PlayerStatus ps : pps){
            tempps.setPosition(ps.getPosition());
            //if the best use of the combined chips is 0 distance from the goal, then we have reached it
            if((new BestUse(b, tempps, s, 0)).getBestState().getPosition().dist(b.getGoalLocations().get(0)) == 0){
                //System.out.println("======== we have a game: "  + gs.getGameId() + "   " + together.getNumChips() + " chips=======");
                return(true);    
            }
        }
        return(false);
    }
    
    public static Board getRandomBoard(GamePalette gp, int numRows, int numCols){
        Board board = new Board(numRows, numCols);
        Square[][] squares = new Square[board.getRows()][board.getCols()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getCols(); j++) {
                squares[i][j] = new Square();
                squares[i][j].setColor(gp.getRandomColor());
            }
        }
        board.setSquares(squares);
        return(board);
    }

}