import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ctagents.FileLogger;

import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.server.ServerGameStatus;
import edu.harvard.eecs.airg.coloredtrails.server.ServerPhases;
import edu.harvard.eecs.airg.coloredtrails.shared.Constants;
import edu.harvard.eecs.airg.coloredtrails.shared.GameBoardCreator;
import edu.harvard.eecs.airg.coloredtrails.shared.PlayerConnection;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameConfigDetailsRunnable;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Goal;
import edu.harvard.eecs.airg.coloredtrails.shared.types.HistoryLog;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PhaseChangeHandler;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PhasesLength;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;
import edu.harvard.eecs.airg.coloredtrails.shared.types.SignalingArguments;

/**
 * This configuration implements a game in which the goals are not visible at
 * the beginning but allows the players to reveal them in the first phase. After
 * the revelation phase the players get one opportunity to negotiate (the first
 * proposer is randomly picked), afterwards an automatic exchange and automatic
 * movement are made.
 * 
 * @author Yael Blumberg
 * 
 */
public class SignalingConfig extends GameConfigDetailsRunnable implements
		PhaseChangeHandler {

	private static String HEADLINES = "file,game time,board type,pin,no nego score,score,reached Goal,reveled,"
			+ "isProposerFirst,send,receive,prop score,resp score,accepted";

	private static String LOG_PREFIX = "signalingConfig_";
	private static PrintWriter csvLog;

	private static final long serialVersionUID = 7646331751840102838L;
	private int proposerID = 0;
	private int responderID = 1;
	private int firstProposer;
	private int[] noNegotiationScore;
	private boolean isOfferAccepted = false;
	private String proposalPhase = "";
	private String logName;
	private String folderName;
	private String boardType = "";

	private Date startTime;

	// The scoring function used for players in the game.
	private Scoring s = new Scoring(100, -10, 5);

	private String[] offers;

	// permission flags
	private boolean ProposerCommunicationAllowed = false;
	private boolean ProposerRevelationAllowed = false;
	private boolean ProposerTransfersAllowed = false;
	private boolean ProposerMovesAllowed = false;
	private boolean ResponderCommunicationAllowed = false;
	private boolean ResponderRevelationAllowed = false;
	private boolean ResponderTransfersAllowed = false;
	private boolean ResponderMovesAllowed = false;

	private boolean[] goalVisibility = { false, false };

	private void resetPermissionFlags() {
		ProposerCommunicationAllowed = false;
		ProposerRevelationAllowed = false;
		ProposerTransfersAllowed = false;
		ProposerMovesAllowed = false;
		ResponderCommunicationAllowed = false;
		ResponderRevelationAllowed = false;
		ResponderTransfersAllowed = false;
		ResponderMovesAllowed = false;
	}

	private void setPermissions() {
		PlayerStatus Responder = gs.getPlayerByPerGameId(responderID);
		PlayerStatus Proposer = gs.getPlayerByPerGameId(proposerID);

		Responder.setCommunicationAllowed(ResponderCommunicationAllowed);
		Responder.setRevelationAllowed(ResponderRevelationAllowed);
		Responder.setTransfersAllowed(ResponderTransfersAllowed);
		Responder.setMovesAllowed(ResponderMovesAllowed);

		Proposer.setCommunicationAllowed(ProposerCommunicationAllowed);
		Proposer.setRevelationAllowed(ProposerRevelationAllowed);
		Proposer.setTransfersAllowed(ProposerTransfersAllowed);
		Proposer.setMovesAllowed(ProposerMovesAllowed);
	}

	/*
	 * this method swaps the roles of the proposer and the responder in the
	 * counter offer
	 */
	public void swapRoles() {
		int tempID = proposerID;
		proposerID = responderID;
		responderID = tempID;
		gs.getPlayerByPerGameId(proposerID).setRole("Proposer");
		gs.getPlayerByPerGameId(responderID).setRole("Responder");
	}

	/*
	 * Returns score of specified player, according to player's current state
	 */
	public int getPlayerScore(PlayerStatus ps) {
		// signalingBestUse sbu=new signalingBestUse(gs, ps);
		// return sbu.getScore();

		RowCol gpos = gs.getBoard().getGoalLocations(ps.getPerGameId()).get(0);
		// get first goal in list
		return (int) Math.floor(gs.getScoring().score(ps, gpos));
	}

	/*
	 * This method sets the games configuration and prepares the board to the
	 * beginning of the game
	 */
	public void run() {

		// Getting the current time - the time the game started
		startTime = new Date();

		String timeStemp = getTimeStamp();
		logName = LOG_PREFIX + timeStemp;
		folderName = timeStemp;

		// getLog().writeln("Starting the game configuration");

		String csvName;

		// Making sure there are only 2 players
		if (gs.getPlayers().size() > 2) {
			return;
		}

		offers = new String[2];
		int[] playerPins = new int[2];

		// Use the perGameId as the ref index for the pin ... gives the
		// controller order
		for (PlayerStatus ps : gs.getPlayers()) {
			playerPins[ps.getPerGameId()] = ps.getPin();
		}

		// If data was passed by the controller, using it.
		// If not - creating the default board
		if (gs.getDataFromController() != null) {

			// Getting the arguments passed by the controller
			SignalingArguments args = (SignalingArguments) gs
					.getDataFromController();

			folderName = args.experimentId;
			csvName = folderName + "/" + LOG_PREFIX + args.experimentId
					+ ".csv";

			getLog().writeln("Starting the game configuration");

			loadGame(args.gameToPlay.get(0), playerPins);

			// Printing the boards data
			getLog().writeln("board loaded.");
			for (PlayerStatus ps : gs.getPlayers()) {
				getLog().writeln("player " + ps.getPin());
				getLog().writeln(" position: " + ps.getPosition());
				getLog().writeln(
						" goal position: "
								+ gs.getBoard().getGoalLocations(
										ps.getPerGameId()));
				getLog().writeln("chips: " + ps.getChips());
			}
		} else {

			getLog().writeln("Starting the game configuration");

			// If the game is stand alone the csv will contain its data only
			csvName = folderName + "/" + logName + ".csv";

			// Setting up the game board
			GameBoardCreator gameBoardCreator = new GameBoardCreator(gs,
					logName);
			gameBoardCreator.createGameBoard("signalingBoard.txt");

			getLog().writeln("game board created");
		}

		// Getting the boards type
		if (gs.getBoard().get("BOARD_TYPE") != null) {
			boardType = gs.getBoard().get("BOARD_TYPE").toString();
			gs.update(gs, "BOARD_CHANGED");
		}

		// Opening the csv file.
		// If there is already a file with the same name in this directory don't
		// override it, but append the new data to the existing one. Otherwise,
		// create new file and write to it.
		try {
			File checkFile = new File(csvName);

			if (checkFile.exists()) {
				FileOutputStream fop = new FileOutputStream(checkFile, true);
				csvLog = new PrintWriter(fop);
			} else {
				csvLog = new PrintWriter(csvName);
				csvLog.println(HEADLINES);
				csvLog.flush();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// If the game manager didn't set the roles at the configuration file,
		// randomly deciding which player is the first proposer
		if ((gs.getPlayerByPerGameId(0).getRole().length() == 0)) {
			Random rand = new Random();

			double n = rand.nextDouble();

			if (n > 0.5) {
				proposerID = 1;
				responderID = 0;
			}

			getLog().writeln(
					"First proposer is: "
							+ gs.getPlayerByPerGameId(proposerID).getPin());
		} else if (gs.getPlayerByPerGameId(0).getRole().contains("Responder")) {
			proposerID = 1;
			responderID = 0;
		}

		// Saving the first proposers id
		firstProposer = proposerID;

		// Hiding the roles for now, so the revelation won't be affected
		gs.getPlayerByPerGameId(proposerID).setRole("");
		gs.getPlayerByPerGameId(responderID).setRole("");

		// Setting the goals as invisible
		for (PlayerStatus ps : gs.getPlayers()) {
			ps.setGoalVisible(false);
		}

		// Getting the boards type
		if (gs.getBoard().get("BOARD_TYPE") != null) {
			boardType = gs.getBoard().get("BOARD_TYPE").toString();
		}

		// Opening the csv file.
		// If there is already a file with the same name in this directory don't
		// override it, but append the new data to the existing one. Otherwise,
		// create new file and write to it.
		try {
			File checkFile = new File(csvName);

			if (checkFile.exists()) {
				FileOutputStream fop = new FileOutputStream(checkFile, true);
				csvLog = new PrintWriter(fop);
			} else {
				csvLog = new PrintWriter(csvName);
				csvLog.println(HEADLINES);
				csvLog.flush();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Getting the phases length from lib/adminconfig/PhaseTimes.txt
		PhasesLength phL = new PhasesLength();
		phL.getPhasesTimes();

		// set up phase sequence
		ServerPhases ph = new ServerPhases(this);
		ph.addPhase(ServerPhases.STRATEGY_PH, phL.stratPrepPhTime);
		ph.addPhase(ServerPhases.REVELATION_PH, phL.revelationPhTime);
		ph.addPhase(ServerPhases.COMM_PH, phL.commPhTime);
		ph.addPhase(ServerPhases.COUNTER_PH, phL.counterOfferPhTime);
		ph.addPhase(ServerPhases.FEEDBACK_PH, phL.fdbckPhTime);
		ph.setLoop(false);
		gs.setPhases(ph);

		gs.setScoring(s);

		// Calculating the scores before negotiation
		noNegotiationScore = new int[gs.getPlayers().size()];

		for (PlayerStatus ps : gs.getPlayers()) {
			noNegotiationScore[ps.getPerGameId()] = getPlayerScore(ps);
		}

		gs.sendArbitraryMessage(Constants.NEWROUND);

		// will generate GAME_INITIALIZED message
		gs.setInitialized();
	}

	/*
	 * Called by server when a phase begins
	 */
	public void beginPhase(String phaseName) {
		getLog().writeln("A New Phase Began: " + phaseName);

		resetPermissionFlags();

		// FYI - for the first phase it won't work from here
		if (phaseName.equals(ServerPhases.REVELATION_PH)) {
			ProposerRevelationAllowed = true;
			ResponderRevelationAllowed = true;
		} else if (phaseName.equals(ServerPhases.COMM_PH)) {
			ProposerCommunicationAllowed = true;
			
		} else if (phaseName.equals(ServerPhases.COUNTER_PH)) {
			ProposerCommunicationAllowed = true;
		} else if (phaseName.equals(ServerPhases.FEEDBACK_PH)) {
			//endGame();
			doAutomaticMovement(gs.getScoring());
			//doAutomaticMovement(gs.getScoring());
			System.out.println("GIL DOING AUTO MOVEMENT");
		}

		setPermissions();
	}

	/*
	 * Called by server when a phase ends
	 */
	public void endPhase(String phaseName) {
		getLog().writeln("A Phase Ended: " + phaseName);

		if (phaseName.equals(ServerPhases.REVELATION_PH)) {

			// If one of the players reveled its goal - change the board
			if (goalVisibility[0] || goalVisibility[1]) {

				gs.getPlayerByPerGameId(0).setGoalVisible(goalVisibility[0]);
				gs.getPlayerByPerGameId(1).setGoalVisible(goalVisibility[1]);
				gs.update(gs, "BOARD_CHANGED");
			}

			// Reveling the roles of each player
			gs.getPlayerByPerGameId(proposerID).setRole("Proposer");
			gs.getPlayerByPerGameId(responderID).setRole("Responder");

		} else if (phaseName.equals(ServerPhases.COMM_PH)) {

			// If the offer was not accepted preparing the roles to the next
			// phase
			if (!isOfferAccepted) {

				resetPermissionFlags();
				swapRoles();
				getLog().writeln(
						"Roles swaped. proposer is "
								+ gs.getPlayerByPerGameId(proposerID).getPin());
			}

			gs.sendArbitraryMessage(Constants.NEWPHASE);
		} else if (phaseName.equals(ServerPhases.COUNTER_PH)) {
			gs.sendArbitraryMessage(Constants.NEWPHASE);
		}
	 else if (phaseName.equals(ServerPhases.FEEDBACK_PH)) {
		endGame();
	}
	}

	public boolean doDiscourse(DiscourseMessage dm) {
		getLog().writeln("Received communication message ");
		getLog().writeln(
				"From player: "
						+ gs.getPlayerByPerGameId(dm.getFromPerGameId())
								.getPin());
		getLog().writeln(
				"To player: "
						+ gs.getPlayerByPerGameId(dm.getToPerGameId())
								.getPin());
		getLog().writeln("Message type: " + dm.getMsgType());

		boolean validDiscourse = true;
		
		if (dm instanceof BasicProposalDiscussionDiscourseMessage) {
			
			// Sending the message to the client
			validDiscourse = gs.doDiscourse(dm);				

			String curPhase = gs.getPhases().getCurrentPhase().getName();

			// If the response was received in the feedback phase, ignore it
			if (!curPhase.equals(ServerPhases.FEEDBACK_PH)) {

				BasicProposalDiscussionDiscourseMessage bpddm = (BasicProposalDiscussionDiscourseMessage) dm;

				// Performing automatic exchange of chips upon acceptance of a
				// proposal
				if (bpddm.accepted()) {
					getLog().writeln(
							"We have an accepted offer, chips before: ");
					for (PlayerStatus player : gs.getPlayers()) {
						getLog().writeln("player pin: " + player.getPin());
						getLog().writeln("player chips: " + player.getChips());
					}

					doAutomaticChipTransfer(bpddm);

					getLog().writeln(" Chips After: ");
					for (PlayerStatus player : gs.getPlayers()) {
						getLog().writeln("player pin: " + player.getPin());
						getLog().writeln("player chips: " + player.getChips());
					}

					// If the offer was accepted in the first offer phase,
					// signaling
					// the game to end
					isOfferAccepted = true;
					offers[bpddm.getProposerID()] += ",TRUE";
				} else {
					getLog().writeln("Offer rejected");
					offers[bpddm.getProposerID()] += ",FALSE";
				}

				// If the offer was accepted, ending the game
				if (isOfferAccepted) {
					gs.getPhases().advanceToPhase(ServerPhases.FEEDBACK_PH);

					// Checking that this reject was a respond to a proposal
					// that was made in this phase
					// (to prevent a bug where the response was received after
					// the time allocated to the
					// communication phase elapsed and a advance phase command
					// was already given)
				} else if ((curPhase.equals(proposalPhase))) {
					gs.getPhases().advancePhase();
				}
			}
		} else if (dm instanceof BasicProposalDiscourseMessage) {

			BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) dm;

			// Saving the current phase name
			proposalPhase = gs.getPhases().getCurrentPhase().getName();

			// Getting the offers details
			ChipSet ChipsSentByResponder = proposal.getChipsSentByResponder();
			ChipSet ChipsSentByProposer = proposal.getChipsSentByProposer();

			// Sending blank offer to oneself considered a goal revelation
			// ... so reveal goal and return
			if (ChipsSentByProposer.getNumChips() == 0
					&& ChipsSentByResponder.getNumChips() == 0
					&& proposal.getProposerID() == proposal.getResponderID()) {

				// blank offer sent to self as request to reveal goal
				getLog().writeln(
						"Revealing goal of player "
								+ gs.getPlayerByPerGameId(
										proposal.getProposerID()).getPin());

				goalVisibility[((BasicProposalDiscourseMessage) dm)
						.getProposerID()] = true;

				// Advancing to the next level only if the two players reveled
				// their goals.
				// Otherwise this will be performed at the end of the timeout
				if (goalVisibility[0] && goalVisibility[1]) {

					gs.getPlayerByPerGameId(0).setGoalVisible(true);
					gs.getPlayerByPerGameId(1).setGoalVisible(true);
					gs.update(gs, "BOARD_CHANGED");
					resetPermissionFlags();
					ProposerCommunicationAllowed = true;
					setPermissions();
					gs.getPhases().advancePhase();
				}
				
			} else {

				// Checking that the offer is valid (all players have the needed
				// chips)
				ChipSet ResponderChips = gs.getPlayerByPerGameId(responderID)
						.getChips();
				ChipSet ProposerChips = gs.getPlayerByPerGameId(proposerID)
						.getChips();

				boolean isValid = (ResponderChips
						.contains(ChipsSentByResponder))
						&& (ProposerChips.contains(ChipsSentByProposer));

				if (!isValid) {
					// invalid offer ... drop and pass
					getLog().writeln("Dropping invalid offer");

					// If the current proposer actually sent this invalid offer
					// (and its not a proposal that was meant to be sent in the
					// previous phase) then -
					// If the is the first offer, letting the other player make
					// a new offer. If it is the counter offer - ending the game
					if (proposal.getProposerID() == proposerID) {
						gs.getPhases().advancePhase();
					}

					return false;
				}
				
				// Sending the message to the client
				validDiscourse = gs.doDiscourse(dm);

				// Calculating the offers resulting score for each side by
				// simulating the game status
				ServerGameStatus simulateGs = copyGameStatus(gs);

				// Transfering the chips
				ChipSet futureCS = ChipSet.addChipSets(ProposerChips,
						ChipsSentByResponder);
				futureCS = ChipSet.subChipSets(futureCS, ChipsSentByProposer);
				simulateGs.getPlayerByPerGameId(proposerID).setChips(futureCS);

				futureCS = ChipSet.addChipSets(ResponderChips,
						ChipsSentByProposer);
				futureCS = ChipSet.subChipSets(futureCS, ChipsSentByResponder);
				simulateGs.getPlayerByPerGameId(responderID).setChips(futureCS);

				// Moving the players with their new chips
				doAutomaticMovement(simulateGs, s);

				// Calculating the score
				int propScore = getPlayerScore(simulateGs
						.getPlayerByPerGameId(proposerID));
				int respScore = getPlayerScore(simulateGs
						.getPlayerByPerGameId(responderID));

				// Saving the offer
				offers[proposerID] = ChipsSentByProposer.toString() + ","
						+ ChipsSentByResponder.toString() + "," + propScore
						+ "," + respScore;

				getLog().writeln(
						"Chips to send by proposer: " + ChipsSentByProposer);
				getLog().writeln(
						"Chips to send by responder: " + ChipsSentByResponder);
				getLog().writeln(
						"If the offer will be accepted, proposers score will be: "
								+ propScore + " and responders score "
								+ respScore);

				// Disable proposing by ANYONE until offer has been rejected or
				// queried
				resetPermissionFlags();
				setPermissions();
			}
		}

		return validDiscourse;
	}

	private void endGame() {

		// At the end of the game, moving the players
		//doAutomaticMovement(gs.getScoring());

		// calculate scores after all players have moved
		for (PlayerStatus ps : gs.getPlayers()) {
			int score = getPlayerScore(ps);
			ps.setScore(score);
			getLog().writeln("Player " + ps.getPin() + " score: " + score);
		}

		// Writing the results of the game to the csv file
		writeToCsv(new Date());
		
		gs.getPlayerByPerGameId(responderID).setRole("GameEnded");
		gs.getPlayerByPerGameId(proposerID).setRole("GameEnded");
		gs.setEnded();

		// Copying the agents logs to the game folder
		File dir = new File(System.getProperty("user.dir"));
		File[] files = dir.listFiles();
		
		for (int i = 0; i < files.length; ++i) {
			if (files[i].isFile()
					&& files[i].getName().startsWith("CT_DbgLog_Signaling")) {
				
				int triesNo = 0;
				getLog().writeln("Moving file: " + files[i].getName());
				
				// Trying to move the file until success - waiting for the agent
				// to close and release the file..
				while ((!files[i].renameTo(new File(folderName, files[i]
						.getName()))) && triesNo < 10000) {
					triesNo++;
				}
				
				getLog().writeln("Tried to close " + files[i].getName() + " " + triesNo + " times");
			}
		}
	}

	/*
	 * Filter the game board from the unraveled goals before it is sent out to
	 * player;
	 */
	public Board filterBoard(Board serverboard, int perGameId) {
		// If the player revealed his goal, he dosen't need to see its possible
		// goals, so removing them
		if (gs.getPlayerByPerGameId(perGameId).isGoalVisible()) {
			for (Goal goal : serverboard.getGoals()) {

				// A possible goal is represented by a two digit number that its
				// ones is equal to the players perGameID
				if ((goal.getType() >= Goal.POSSIBLE_GOAL_TYPE)
						&& (goal.getType() % 10 == perGameId)) {
					serverboard.getSquare(goal.getLocation()).removeGoal(goal);
				}
			}
		}
		// Searching for the opponents
		for (PlayerStatus ps : gs.getPlayers()) {
			if (ps.getPerGameId() != perGameId) {

				int opponentId = ps.getPerGameId();
				getLog().writeln("Player "
								+ perGameId
								+ " can"
								+ (gs.getPlayerByPerGameId(opponentId)
										.isGoalVisible() ? "" : "not")
								+ " see Goal of Player " + opponentId);

				if (!gs.getPlayerByPerGameId(opponentId).isGoalVisible()) {

					Map<Goal, RowCol> forRemoval = new HashMap<Goal, RowCol>();
					for (RowCol rcGoal : serverboard
							.getGoalLocations(opponentId)) {
						getLog().writeln("Hiding Goal of type " + opponentId
										+ " at " + rcGoal + " from Player "
										+ perGameId);
						forRemoval.put(serverboard.getSquare(rcGoal).getGoal(
								opponentId), rcGoal);
					}
					for (Goal gRemove : forRemoval.keySet()) {
						serverboard.getSquare(forRemoval.get(gRemove))
								.removeGoal(gRemove);
					}
					// If the goal is visible, need to hide the possible goals
				} else {

					for (Goal goal : serverboard.getGoals()) {

						// A possible goal is represented by a two digit number
						// that its
						// ones is equal to the players perGameID
						if ((goal.getType() >= Goal.POSSIBLE_GOAL_TYPE)
								&& (goal.getType() % 10 == opponentId)) {
							serverboard.getSquare(goal.getLocation())
									.removeGoal(goal);
						}
					}
				}
			}
		}

		// Letting the client know the type of the board
		serverboard.set("BOARD_TYPE", gs.getBoard().get("BOARD_TYPE"));

		return serverboard;
	}

	/*
	 * This method gets a game status object and copies its content to the
	 * original gs matching the players data to the known pins.
	 */
	private void loadGame(GameStatus igs, int[] playerPins) {

		System.out.println("Loading game config " + igs.getGameId());

		// Setting the game's players
		gs.setPlayers(igs.getPlayers());

		// Setting the pins to the relevant perGameId
		gs.getPlayerByPerGameId(0).setPin(playerPins[0]);
		gs.getPlayerByPerGameId(1).setPin(playerPins[1]);

		for (PlayerStatus ps : gs.getPlayers()) {
			for (PlayerConnection pc : ServerData.getInstance().getPlayers()) {
				if (ps.getPin() == pc.getPin()) {
					ps.set("pc", pc);
				}
			}
		}

		gs.setGamePalette(igs.getGamePalette());
		gs.setScoring(igs.getScoring());
		gs.setBoard(igs.getBoard());
		gs.getBoard().set("BOARD_TYPE", igs.get("BOARD_TYPE"));

		for (PlayerStatus ps : gs.getPlayers()) {
			getLog().writeln(
					"Player:  pin: " + ps.getPin() + " pergameId: "
							+ ps.getPerGameId());
		}
	}

	/*
	 * Returns a string representing the current time. Used to name the log file
	 */
	private String getTimeStamp() {
		int dayMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int month = Calendar.getInstance().get(Calendar.MONTH);
		// January = 0, December =11 --> Add 1 to obtain the right name
		month++;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int min = Calendar.getInstance().get(Calendar.MINUTE);
		int sec = Calendar.getInstance().get(Calendar.SECOND);
		return (dayMonth + "_" + month + "_" + year + "_" + hour + "_" + min
				+ "_" + sec);
	}

	private FileLogger getLog() {
		return FileLogger.getInstance(logName, folderName);
	}

	// HEADLINES =
	// "file,game time,pin,score,reached
	// Goal,reveled,isProposerFirst,send,receive,accepted";
	private void writeToCsv(Date endTime) {

		// Calculating the games length
		long diffSec = (endTime.getTime() - startTime.getTime()) / 1000;
		long diffMin = diffSec / 60;
		long diffSecOfMin = diffSec % 60;
		String gameLength = diffMin + "mins " + diffSecOfMin + "secs";

		// Collecting the players data
		String firstPlayer = getPlayersData(0);
		String secondPlayer = getPlayersData(1);

		// Writing first players data
		csvLog.print(logName + ",");
		csvLog.println(gameLength + "," + boardType + "," + firstPlayer);
		// Writing second players data
		csvLog.print(logName + ",");
		csvLog.println(gameLength + "," + boardType + "," + secondPlayer);
		csvLog.flush();
	}

	private String getPlayersData(int gameID) {

		PlayerStatus ps = gs.getPlayerByPerGameId(gameID);
		boolean reachedGoal = true;
		boolean isFirstProposer = true;

		// Checking if the player is in the same position as his goal
		if (!ps.getPosition().equals(
				gs.getBoard().getGoalLocations(gameID).get(0))) {
			reachedGoal = false;
		}

		// Checking if this is the first proposer
		if (gameID != firstProposer) {
			isFirstProposer = false;
		}

		String data = ps.getPin() + "," + noNegotiationScore[gameID] + ","
				+ ps.getScore() + "," + reachedGoal + "," + ps.isGoalVisible()
				+ "," + isFirstProposer + ","
				+ (offers[gameID] == null ? "" : offers[gameID]);

		return data;
	}

	/*
	 * Creates a copy of a game status with copies of the players objects.
	 */
	private ServerGameStatus copyGameStatus(ServerGameStatus gs) {
		ServerGameStatus gsCopy = new ServerGameStatus();
		Set<PlayerStatus> newPlayers = new HashSet<PlayerStatus>();

		for (PlayerStatus ps : gs.getPlayers()) {
			PlayerStatus psNew = new PlayerStatus(ps);
			newPlayers.add(psNew);
		}
		gsCopy.setPlayers(newPlayers);

		Board b = new Board(gs.getBoard());
		gsCopy.setBoard(b);

		gsCopy.getBoard().set("BOARD_TYPE", gs.getBoard().get("BOARD_TYPE"));
		gsCopy.setHistoryLog(new HistoryLog());

		Phases p = new Phases(gs.getPhases());
		gsCopy.setPhases(p);

		return gsCopy;
	}
}