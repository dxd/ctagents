package edu.harvard.eecs.airg.coloredtrails.shared.discourse;

import java.util.List;
import java.util.ArrayList;


public class ScoringMessage extends DiscourseMessage {

    protected List<Integer> scores;

    public ScoringMessage( int fromPerGameId, int toPerGameId, Integer... s ) {
        this.fromPerGameId = fromPerGameId;
        this.toPerGameId = toPerGameId;
        this.messageId = -1;
        msgType = "ScoringMessage";

        scores = new ArrayList<Integer>( );
        for( int score : s ) {
            scores.add( score );
        }
    }

    public int getScore( int playerID ) {
        try {
            if( playerID < 0 || playerID >= scores.size() )
                throw new Exception( "Player ID is out of bounds." );
        }
        catch( Exception e ) {
            e.printStackTrace();
            return - Integer.MAX_VALUE;
        }

        return scores.get( playerID );
    }

    public String toString() {
        String str = "Scoring Message\n";
        for( Integer score : scores ) {
            str += "\t" + score;
        }
        return str;
    }
}
