


import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;

import java.util.Scanner;

import java.io.FileReader;

/**
 * This configuration implements much of the setup and functionality.
 * Including automatic exchange after accept an offer, and automatic movement.
 */
public class InputFileConfig extends TheAutomatConfig {

	/**
		Start a new game
	*/
	public void run() {
		System.out.println("Let the game begin...");
		
		System.out.println("game id= " + gs.getGameId());
		
		// set game palette
		gs.addColorToGamePalette("RGBRed");
		gs.addColorToGamePalette("RGBGreen");
		gs.addColorToGamePalette("purple1");

		try {
		
			FileReader fr = new FileReader( "lib/adminconfig/board_"+0+".txt" );
			Scanner in = new Scanner( fr );
			
			// assign game-board colors
			setBoard(gs.getGamePalette(), in );
			
			GamePalette gp = gs.getGamePalette();
			
			//for all the players
			for( PlayerStatus player : gs.getPlayers() ) {
				player.setTeamId(3);	// set teams for players
				player.setChips( getChipSet( gp, in ) );
				player.setPosition( getPosition( in ) );
			}
		
		} catch( Exception e ) {
			e.printStackTrace();
		}
				
		// set up phase sequence
		ServerPhases ph = new ServerPhases(this);
		ph.addPhase("Communication Phase");
		ph.addPhase("Exchange Phase", 10);
		ph.addPhase("Movement Phase", 10);
		ph.addPhase("Feedback Phase", 10);
		
		ph.setLoop(phaseLoop);
		gs.setPhases(ph);
		
		if(ph.getCurrentPhaseName().equals("Communication Phase")) {
			gs.getPlayerByPerGameId(0).setCommunicationAllowed(true);
			gs.getPlayerByPerGameId(1).setCommunicationAllowed(true);
		}
		
		gs.setInitialized();  // will generate GAME_INITIALIZED message
	}
		
		
	/**
		Places random colors on specified board
	*/
	protected void setBoard(GamePalette gp, Scanner in ) throws Exception {
		
		System.out.println( "-------- Creating Board --------" );
		int row = in.nextInt();
		int col = in.nextInt();
		in.nextLine(); //clears out any comments
		Board board = new Board(row,col);
		Square[][] squares = new Square[row][col];
		for (int r = 0; r < row; r++) {
			for (int c = 0; c < col; c++) {
		      squares[r][c] = new Square();
		      squares[r][c].setColor( gp.get( in.nextInt() ) );
			}
		}
		board.setSquares(squares);
		
		board.setGoal( getPosition( in ), true); // goal location
		gs.setBoard(board);
	}
	
	protected ChipSet getChipSet( GamePalette gp, Scanner in ) {
		ChipSet cs = new ChipSet();
		int counter = 0;
		while( in.hasNextInt() ) {
			cs.add( gp.get(counter), in.nextInt() );
			counter++;
		}
		in.nextLine();
		return cs;
	}
	
	protected RowCol getPosition( Scanner in ) {
		int row = in.nextInt();
		int col = in.nextInt();
		in.nextLine();
		return new RowCol( row, col );
	}
}