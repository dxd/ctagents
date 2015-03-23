

import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.ChipsRevelationDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;

import java.util.Random;
import java.util.Scanner;

import java.io.File;
import java.io.FileReader;



/**
 * This configuration implements much of the setup and functionality. Including
 * automatic exchange after accept an offer, and automatic movement.
 */
public class NormsAgentsConfig extends GameConfigDetailsRunnable implements
		PhaseChangeHandler {

	/**
	 * The scoring function used for players in the game. 100 for a player
	 * reaching the goal, -10 per unit distance if the player does not reach the
	 * goal, 5 for each chip remaining after the player has reached the goal or
	 * cannot move any farther towards the goal.
	 */
	Scoring s = new Scoring(100, -10, 5);

	/**
	 * indicated the current file number that data is taken from
	 */
	int CurrentInputFileIndex = 0;

	// used for switching roles.
	int ProposerID = 0;
	int ResponderID = 1;

	//calculated automatically in run time
	int numberOfConfigFiles = 0;
	/**
	 * Returns score of specified player, according to player's current state
	 */
	public int getPlayerScore(PlayerStatus ps) {
		RowCol gpos = gs.getBoard().getGoalLocations().get(0); // get first goal
																// in list
		return (int) Math.floor(s.score(ps, gpos)); // should change to double
													// at some point
	}
	
	/**
	 * Called by GameConfigDetailsRunnable methods when calculation and
	 * assignment of player scores is desired
	 */
	protected void assignScores() {
		for (PlayerStatus ps : gs.getPlayers()) {
			ps.setScore(getPlayerScore(ps));
			System.out.println("Player: " + ps.getPin() + "  Score: "
					+ ps.getScore());
		}
	}

	
	/**
	 * this method swaps the roles of the proposer and the responder in the
	 * counter offer
	 */
	public void swapRoles() {
		int tempID = ProposerID;
		ProposerID = ResponderID;
		ResponderID = tempID;
		gs.getPlayerByPerGameId(ProposerID).setRole("Proposer");
		gs.getPlayerByPerGameId(ResponderID).setRole("Responder");
	}
	
	
	/**
	 * Start a new game
	 */
	public void run() {
		System.out.println("Let the game begin...");

		System.out.println("game id= " + gs.getGameId());

		GamePalette gp = new GamePalette();
		gp.add("RGBRed");
		gp.add("RGBGreen");
		gp.add("purple1");
		gp.add("orange1");
		gs.setGamePalette(gp);
				
		//count number of config files to read
		String pathToConfigFiles = "lib/adminconfig";
		File file = new File(pathToConfigFiles);
		if (file.isDirectory()) {
			File[] configFiles = file.listFiles();
			
			for (int i = 0; i < configFiles.length; i++) {
				if (configFiles[i].getName().contains("board_") &&
						configFiles[i].getName().endsWith(".txt")) {
					numberOfConfigFiles++;
				}
			}
		}
		
		// set up phase sequence
		ServerPhases ph = new ServerPhases(this);
		for (int i = 0; i < numberOfConfigFiles; i++) {
			ph.addPhase("Communication Phase", 60);
			ph.addPhase("Movement Phase", 60);
		}
		ph.addPhase("Feedback Phase", 60);

		ph.setLoop(false);
		gs.setPhases(ph);

		gs.setInitialized(); // will generate GAME_INITIALIZED message
	}

	/**
	 * Places random colors on specified board
	 */
	protected void setBoard(GamePalette gp, Scanner in) throws Exception {

		System.out.println("-------- Creating Board --------");
		int row = in.nextInt();
		int col = in.nextInt();
		in.nextLine(); // clears out any comments
		Board board = new Board(row, col);
		Square[][] squares = new Square[row][col];
		for (int r = 0; r < row; r++) {
			for (int c = 0; c < col; c++) {
				squares[r][c] = new Square();
				squares[r][c].setColor(gp.get(in.nextInt()));
			}
		}
		board.setSquares(squares);

		board.setGoal(getPosition(in), true); // goal location
		gs.setBoard(board);
	}

	protected ChipSet getChipSet(GamePalette gp, Scanner in) {
		ChipSet cs = new ChipSet();
		int counter = 0;
		while (in.hasNextInt()) {
			cs.add(gp.get(counter), in.nextInt());
			counter++;
		}
		in.nextLine();
		return cs;
	}

	protected RowCol getPosition(Scanner in) {
		int row = in.nextInt();
		int col = in.nextInt();
		in.nextLine();
		return new RowCol(row, col);
	}

	@Override
	public void beginPhase(String phasename) {
		// TODO Auto-generated method stub

		if (phasename.equals("Communication Phase")) {			

			try {

				FileReader fr = new FileReader("lib/adminconfig/board_"
						+ CurrentInputFileIndex + ".txt");
				Scanner in = new Scanner(fr);

				// assign game-board colors
				setBoard(gs.getGamePalette(), in);

				// for all the players
				for (PlayerStatus player : gs.getPlayers()) {
					//player.setTeamId(3); // set teams for players
					player.setChips(getChipSet(gs.getGamePalette(), in));
					player.setPosition(getPosition(in));
				}
				
				
				

			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("A New Phase Began: " + phasename);

			boolean ProposerCommunicationAllowed = false;
			boolean ProposerTransfersAllowed = false;
			boolean ProposerMovesAllowed = true;
			boolean ResponderCommunicationAllowed = false;
			boolean ResponderTransfersAllowed = false;
			boolean ResponderMovesAllowed = true;

			// FYI - for the first phase it won't work from here
			if (phasename.equals("Communication Phase")) {
				ProposerCommunicationAllowed = true;
			} else if (phasename.equals("Revelation Phase")) {

			} else if (phasename.equals("Movement Phase")) {

			} else if (phasename.equals("Feedback Phase")) {

			}
			
			
			//switch rolls on new boards
			swapRoles();
			
			PlayerStatus Responder = gs.getPlayerByPerGameId(ResponderID);
			PlayerStatus Proposer = gs.getPlayerByPerGameId(ProposerID);
			
			Responder.setCommunicationAllowed(ResponderCommunicationAllowed);
			Responder.setTransfersAllowed(ResponderTransfersAllowed);
			Responder.setMovesAllowed(ResponderMovesAllowed);

			Proposer.setCommunicationAllowed(ProposerCommunicationAllowed);
			Proposer.setTransfersAllowed(ProposerTransfersAllowed);
			Proposer.setMovesAllowed(ProposerMovesAllowed);
		}
		else if (phasename.equals("Movement Phase")) {	

			System.out.println("A New Phase Began: " + phasename);

			boolean ProposerCommunicationAllowed = false;
			boolean ProposerTransfersAllowed = false;
			boolean ProposerMovesAllowed = true;
			boolean ResponderCommunicationAllowed = false;
			boolean ResponderTransfersAllowed = false;
			boolean ResponderMovesAllowed = true;

			// FYI - for the first phase it won't work from here
			if (phasename.equals("Communication Phase")) {
				ProposerCommunicationAllowed = true;
			} else if (phasename.equals("Revelation Phase")) {

			} else if (phasename.equals("Movement Phase")) {

			} else if (phasename.equals("Feedback Phase")) {

			}
			
			
			PlayerStatus Responder = gs.getPlayerByPerGameId(ResponderID);
			PlayerStatus Proposer = gs.getPlayerByPerGameId(ProposerID);
			
			Responder.setCommunicationAllowed(ResponderCommunicationAllowed);
			Responder.setTransfersAllowed(ResponderTransfersAllowed);
			Responder.setMovesAllowed(ResponderMovesAllowed);

			Proposer.setCommunicationAllowed(ProposerCommunicationAllowed);
			Proposer.setTransfersAllowed(ProposerTransfersAllowed);
			Proposer.setMovesAllowed(ProposerMovesAllowed);
		}
	}

	@Override
	public void endPhase(String phaseName) {
		// TODO Auto-generated method stub
		

		if (phaseName.equals("Feedback Phase")) {
			// end game
			gs.setEnded();
		} else
		if (phaseName.equals("Communication Phase")) {
			CurrentInputFileIndex++;
				//doAutomaticMovement(s);

				// calculate scores after all players have moved
				// (e.g, in case a player's score depends on others' locations)
				assignScores();
			}
	}
	
	// Override super method do discourse in order to make an automatic transfer
	// after accept a proposal
	public boolean doDiscourse(DiscourseMessage dm) {
		System.out.println("Received Discourse Message ");
		System.out.println("Class: " + dm.getClass());
		System.out.println("From: " + dm.getFromPerGameId());
		System.out.println("From: " + dm.getToPerGameId());

		// Sending the message to the client
		boolean result = gs.doDiscourse(dm);

		// ### automatic exchange of chips upon acceptance of a proposal ###
		if (dm instanceof BasicProposalDiscussionDiscourseMessage) {
			System.out
					.println("---- BasicProposalDiscussionDiscourseMessage ----");

			BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;

			if (bpddm.accepted()) {				
				// print the chips before the transfer
				System.out.println("We have an accepted offer, chips before: ");
				for (PlayerStatus player : gs.getPlayers()) {
					System.out.println("player pin: " + player.getPin());
					System.out.println("player chips: " + player.getChips());
				}
				// transfer the chips between the players
				doAutomaticChipTransfer(bpddm);

				// print the chips after the transfer
				System.out.println("chips after: ");
				for (PlayerStatus player : gs.getPlayers()) {
					System.out.println("player pin: " + player.getPin());
					System.out.println("player chips: " + player.getChips());
				}
			} else {
				System.out.println("Offer rejected");
				//System.out.println("swaping roles for counter offer");
				//swapRoles();
			}
		} else if (dm instanceof ChipsRevelationDiscourseMessage) {
			System.out.println("---- ChipsRevelationDiscourseMessage ----");

			ChipsRevelationDiscourseMessage crdm = (ChipsRevelationDiscourseMessage) dm;
			ChipSet revelationChips = crdm.getRevelationChips();

			// get sender ID from the message
			int SenderID = crdm.getFromPerGameId();

			System.out.println("player pin: "
					+ gs.getPlayerByPerGameId(SenderID).getPin());
			System.out.println("playerPerGameId" + SenderID
					+ " updated his revelation chips");
			System.out.println(revelationChips.toString());

			// set the player revelation chips
			gs.getPlayerByPerGameId(SenderID).setRevelationChips(
					revelationChips);
			gs.update(gs, "PLAYER_CHANGED");

		}

		return result;
	}

}