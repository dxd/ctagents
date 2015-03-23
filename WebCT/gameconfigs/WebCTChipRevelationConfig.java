
import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.server.ServerGameStatus;
import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.GlobalColorMap;
import edu.harvard.eecs.airg.coloredtrails.shared.Utility;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.ChipsRevelationDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;
import edu.harvard.eecs.airg.coloredtrails.alglib.BestUse;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This configuration implements much of the setup and functionality. Including
 * automatic exchange after accept an offer, and automatic movement.
 * 
 * In this configuration the game has 2 rounds. if a proposal is made and
 * accepted -> game over else, a second round is started and the players switch
 * roles.
 * 
 * also, this game has a chip revelation phase. in this phase, every player can
 * choose how much chips (from his bank) the other player will see. the
 * revelation works only on in the WebCT client.
 */
public class WebCTChipRevelationConfig extends GameConfigDetailsRunnable
		implements PhaseChangeHandler {
	/**
	 * The scoring function used for players in the game. 100 for a player
	 * reaching the goal, -10 per unit distance if the player does not reach the
	 * goal, 5 for each chip remaining after the player has reached the goal or
	 * cannot move any farther towards teh goal.
	 */
	Scoring s = new Scoring(100, -10, 5);

	/** Local random generator for creating chipsets */
	static Random localrand = new Random();

	/** determines if there will be automatic movement */
	boolean automaticMovement = false;

	/** determines if there will be chips revelation or not */
	boolean EnableChipsRevelation = true;

	/**
	 * determines if the chips will automatically transfer after a proposal has
	 * been accepted
	 */
	boolean automaticChipTransfer = true;
	/** determines if the phases will loop. */
	boolean phaseLoop = true;

	// Number of rounds to play, each round we switch roles
	int NumRounds = 2;
	int Round = 0;

	/**
	 * if proposal was accepted -> game over
	 */
	boolean wasProposalAccepted = false;

	// used for switching roles.
	int ProposerID = 0;
	int ResponderID = 1;

	int numOfRevelationsCommited = 0;

	Hashtable<Integer, ChipSet> playersRevChipsets;

	String CurrentPhaseName = null;
	boolean ended = false;

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
	 * check if a goal was reached. if true, game will end.
	 * 
	 * @return true if goal was reached, else false
	 */
	public boolean goalReached() {
		boolean result = false;

		for (int i = 0; i < gs.getPlayers().size(); i++) {
			if (gs.getBoard().getGoalLocations().get(0).col == gs
					.getPlayerByPerGameId(i).getPosition().col
					&& gs.getBoard().getGoalLocations().get(0).row == gs
							.getPlayerByPerGameId(i).getPosition().row) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Called by server when a phase begins
	 */
	public void beginPhase(String phasename) {

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

	/**
	 * Called by server when a phase ends
	 */
	public void endPhase(String phasename) {
		System.out.println("A Phase Ended: " + phasename);

		// if end of feedback phase, then end the game
		if (phasename.equals("Feedback Phase")) {
			// check if one of the players reached the goal or if the second
			// round ended.
			// if so end game. else continue to 2nd round.
			if (Round == (NumRounds - 1) || wasProposalAccepted
					|| goalReached()) {
				System.out.println("Ending game");
				assignScores();
				((ServerPhases) gs.getPhases()).setLoop(false);
				System.out
						.println("Loop status: " + gs.getPhases().getIsLoop());

				gs.setEnded();
				return;
			} else {
				swapRoles();
			}

			Round++;
		}
		// ### automatic movement ###
		else if (phasename.equals("Communication Phase")) {
			if (automaticMovement) {
				doAutomaticMovement(s);

				// calculate scores after all players have moved
				// (e.g, in case a player's score depends on others' locations)
				assignScores();
			}
		}
		/*
		else if (phasename.equals("Revelation Phase")) {
			int key;
			Set<Integer> keys = playersRevChipsets.keySet();
			Iterator<Integer> itr = keys.iterator();
			while (itr.hasNext()) {
				key = itr.next();
				gs.getPlayerByPerGameId(key).setRevelationChips(
						playersRevChipsets.get(key));
				gs.update(gs, "PLAYER_CHANGED");
			}

		}*/
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

			if (automaticChipTransfer && bpddm.accepted()) {
				wasProposalAccepted = true;
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

				// advance to next phase
				gs.getPhases().advancePhase();
			} else {
				System.out.println("Offer rejected");
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
			playersRevChipsets.put(SenderID, revelationChips);

			gs.getPlayerByPerGameId(SenderID).setRevelationChips(
					revelationChips);
			gs.update(gs, "PLAYER_CHANGED");
			
			numOfRevelationsCommited++;

			if (numOfRevelationsCommited == gs.getPlayers().size()) {				
				//gs.getPhases().advancePhase();
			}
		}

		return result;
	}

	/**
	 * Start a new game and set up all of the game specifications.
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
		// set game palette
		// gs.addColorToGamePalette("RGBRed");
		// gs.addColorToGamePalette("RGBGreen");
		// gs.addColorToGamePalette("purple1");
		// gs.addColorToGamePalette("orange1");

		playersRevChipsets = new Hashtable<Integer, ChipSet>();

		// for all the players
		for (PlayerStatus player : gs.getPlayers()) {
			// set chips for players
			ChipSet cs = getRandChipSet(gs.getGamePalette());
			player.setChips(cs);

			// set revelation chips for players
			ChipSet revelationChips = getZeroSumsChipSet(gs.getGamePalette());
			System.out.println("revelation chips: "
					+ revelationChips.toString());
			player.setRevelationChips(revelationChips);

			System.out.println("player pin: " + player.getPin());
			System.out.println("player chips: " + player.getChips());
			// set teams for players
			// player.setTeamId(3);
		}

		// assign game-board colors
		setRandBoard(gs.getGamePalette());

		// assign player piece and goal locations
		gs.getPlayerByPerGameId(0).setPosition(new RowCol(1, 1)); // player 1's
																	// location
		gs.getPlayerByPerGameId(1).setPosition(new RowCol(2, 1)); // player 2's
																	// location
		// Set roles
		gs.getPlayerByPerGameId(ResponderID).setRole("Responder");
		gs.getPlayerByPerGameId(ProposerID).setRole("Proposer");

		// set up phase sequence
		ServerPhases ph = new ServerPhases(this);
		ph.addPhase("Revelation Phase", 60);
		ph.addPhase("Communication Phase", 60);
		ph.addPhase("Movement Phase", 60);
		ph.addPhase("Feedback Phase", 10);

		gs.setScoring(this.s);

		ph.setLoop(phaseLoop);
		gs.setPhases(ph);

		gs.setInitialized(); // will generate GAME_INITIALIZED message
	}

	/**
	 * Places random colors on specified board
	 */
	protected void setRandBoard(GamePalette gp) {
		Board board = new Board(4, 4);
		Square[][] squares = new Square[board.getRows()][board.getCols()];

		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {
				squares[i][j] = new Square();
				squares[i][j].setColor(gp.getRandomColor());
			}
		}
		board.setSquares(squares);
		board.setGoal(new RowCol(2, 3), true); // goal location player 0
		// board.setGoal(new RowCol(2, 3), true,1); // goal location player 1
		gs.setBoard(board);
	}

	/**
	 * Generates random chipset for specified player
	 * <p>
	 * add total #chips as parameter
	 */
	protected static ChipSet getRandChipSet(GamePalette gp) {
		ChipSet chipset = new ChipSet();

		for (String color : gp.getColors())
			chipset.set(color, localrand.nextInt(4));

		return chipset;
	}

	/**
	 * Generates chipset with zero values for revelation chips
	 */
	protected static ChipSet getZeroSumsChipSet(GamePalette gp) {
		ChipSet chipset = new ChipSet();

		for (String color : gp.getColors())
			chipset.set(color, 0);

		return chipset;
	}
}