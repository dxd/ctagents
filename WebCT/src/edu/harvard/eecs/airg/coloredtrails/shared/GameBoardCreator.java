package edu.harvard.eecs.airg.coloredtrails.shared;

import edu.harvard.eecs.airg.coloredtrails.server.ServerGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.io.FileReader;
import java.util.Scanner;

import ctagents.FileLogger;

/**
 * <b>Description</b>
 * 
 * This class creates a game board based on an input file. This allows the user
 * to externally choose the different possiblities for the game board and play
 * with them without having to compile again.
 * <p>
 * The input file has to be named and located as follows :
 * lib/adminconfig/boardXml.txt (this can be changed in the future). The
 * different parameters are: a. colors b. number of rows c. number of columns d.
 * squareColors-> the user can manually determine the color of each square in
 * the board e. goal position f. number of players for this game g. set of chips
 * for first player h. starting position of first player i. set of chips for
 * next player j. starting position of next player The last two parameters
 * repeat up to the number of players.
 * 
 * Each of this parameters can be left empty in which case the system will use
 * its default values.
 * 
 * @author Yael Ejgenberg
 */
public class GameBoardCreator {
	private ServerGameStatus gs;
	private String logFile;

	/** Local random generator for creating chipsets */
	static Random localrand = new Random();

	public GameBoardCreator(ServerGameStatus gs, String logFile) {
		this.gs = gs;
		this.logFile = logFile;
	}

	public void createGameBoard(String name) {
		try {
			FileReader fr = new FileReader("lib/adminconfig/" + name);
			Scanner in = new Scanner(fr);
			int rows = 0;
			int cols = 0;
			Board board = null;
			int numOfPlayers = 0;
			int countWhile = 0;
			FileLogger.getInstance(logFile).writeln("Creating board");
			while (in.hasNextLine()) {
				String str = in.next();
				FileLogger.getInstance(logFile).writeln(str);
				System.out.println("str = in.next() results in: " + str
						+ "countWhile = " + countWhile);

				if (str.compareTo("<boardtype>") == 0) {
					gs.set("BOARD_TYPE", in.next());
					FileLogger.getInstance(logFile).writeln(
							"board type is:" + gs.get("BOARD_TYPE"));
					in.nextLine(); // clear </boardtype> tag
				} else if (str.compareTo("<colors>") == 0) {
					System.out.println("Calling setPalette");
					setPalette(in);
				} else if (str.compareTo("<rows>") == 0) {

					rows = in.nextInt(); // get actual number of rows
					in.nextLine(); // clear </rows> tag
					FileLogger.getInstance(logFile).writeln("" + rows);
				} else if (str.compareTo("<columns>") == 0) {

					cols = in.nextInt(); // get actual number of columns
					in.nextLine(); // clear </rows> tag
					FileLogger.getInstance(logFile).writeln("" + cols);
				} else if (str.compareTo("<squaresColors>") == 0) {
					System.out.println("calling setBoard");
					board = setBoard(gs.getGamePalette(), in, rows, cols);
				} else if (str.compareTo("<goalPosition>") == 0) {
					System.out.println("calling setGoal");
					if (board != null) {
						if (in.hasNextInt()) {
							board.setGoal(getPosition(in), true);
						} else {
							board.setGoal(getRandPosition(rows, cols), true);
							in.nextLine();
						}
						FileLogger.getInstance(logFile).writeln(
								"Goal position: "
										+ board.getGoalLocations().get(0)
												.toString());
					}
				} else if (str.compareTo("<firstPlayerGoalPosition>") == 0) {
					System.out.println("calling setGoal");
					if (board != null) {
						if (in.hasNextInt()) {
							board.setGoal(new Goal(0, getPosition(in)), true);
						} else {
							board.setGoal(new Goal(0, getRandPosition(rows,
									cols)), true);
							in.nextLine();
						}

						FileLogger.getInstance(logFile).writeln(
								"first player's goal position: "
										+ board.getGoalLocations(0).get(0)
												.toString());

					}
				} else if (str.compareTo("<secondPlayerGoalPosition>") == 0) {
					System.out.println("calling setGoal");
					if (board != null) {
						if (in.hasNextInt()) {
							board.setGoal(new Goal(1, getPosition(in)), true);
						} else {
							board.setGoal(new Goal(1, getRandPosition(rows,
									cols)), true);
							in.nextLine();
						}

						FileLogger.getInstance(logFile).writeln(
								"second player's goal position: "
										+ board.getGoalLocations(1).get(0)
												.toString());
					}

				} else if (str.compareTo("<firstVisible>") == 0) {
					if (in.hasNextBoolean()) {
						boolean isVisble = in.nextBoolean();
						gs.getPlayerByPerGameId(0).setGoalVisible(isVisble);
						FileLogger.getInstance(logFile).writeln(
								String.valueOf(isVisble));
					}
				} else if (str.compareTo("<secondVisible>") == 0) {
					if (in.hasNextBoolean()) {
						boolean isVisble = in.nextBoolean();
						gs.getPlayerByPerGameId(1).setGoalVisible(isVisble);
						FileLogger.getInstance(logFile).writeln(
								String.valueOf(isVisble));
					}
				} else if (str.compareTo("<firstPlayerPossibleGoal1>") == 0) {
					if (in.hasNextInt()) {
						int prob = in.nextInt();
						Goal g = new Goal(10, getPosition(in), prob);
						// Using the addGoal and not the setGoal of the board in
						// order to keep the information about the probability
						board.getSquare(g.getLocation()).addGoal(g);

						FileLogger.getInstance(logFile).writeln(
								"first player's possible goal1 position: "
										+ g.getLocation().toString());
					}
				} else if (str.compareTo("<firstPlayerPossibleGoal2>") == 0) {
					if (in.hasNextInt()) {
						int prob = in.nextInt();
						Goal g = new Goal(20, getPosition(in), prob);
						board.getSquare(g.getLocation()).addGoal(g);

						FileLogger.getInstance(logFile).writeln(
								"first player's possible goal2 position: "
										+ g.getLocation().toString());
					}
				} else if (str.compareTo("<secondPlayerPossibleGoal1>") == 0) {
					if (in.hasNextInt()) {
						int prob = in.nextInt();
						Goal g = new Goal(11, getPosition(in), prob);
						board.getSquare(g.getLocation()).addGoal(g);

						FileLogger.getInstance(logFile).writeln(
								"second player's possible goal1 position: "
										+ g.getLocation().toString());
					}
				} else if (str.compareTo("<secondPlayerPossibleGoal2>") == 0) {
					if (in.hasNextInt()) {
						int prob = in.nextInt();
						Goal g = new Goal(21, getPosition(in), prob);
						board.getSquare(g.getLocation()).addGoal(g);

						FileLogger.getInstance(logFile).writeln(
								"second player's possible goal2 position: "
										+ g.getLocation().toString());
					}
				} else if (str.compareTo("<numberOfPlayers>") == 0) {

					numOfPlayers = in.nextInt(); // get number of players
					in.nextLine(); // clear </numberOfPlayers> tag
					FileLogger.getInstance(logFile).writeln("" + numOfPlayers);
				} else if (str.compareTo("<firstPlayerChips>") == 0) {
					setPlayersChips(numOfPlayers, gs.getGamePalette(), in,
							rows, cols);
				} else if (str.compareTo("<firstProposer>") == 0) {
					if (in.hasNextInt()) {
						// Subtracting one since the input is 1 or 2
						int proposerNo = in.nextInt() - 1;
						FileLogger.getInstance(logFile).writeln(
								"player "
										+ gs.getPlayerByPerGameId(proposerNo)
												.getPin()
										+ " is the first proposer");

						gs.getPlayerByPerGameId(proposerNo).setRole(
								"ProposerPre");
						gs.getPlayerByPerGameId(1 - proposerNo).setRole(
								"ResponderPre");
					}
				}

				countWhile++;
			}
			System.out.println("Finished reading input file");
			board.toString();
			gs.setBoard(board);
			System.out.println("gs.setBoard(board) DONE");
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Places random colors on specified board
	 */
	protected Board setRandBoard(GamePalette gp, int rows, int cols) {
		Board board = new Board(rows, cols);
		Square[][] squares = new Square[board.getRows()][board.getCols()];

		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getCols(); j++) {
				squares[i][j] = new Square();
				squares[i][j].setColor(gp.getRandomColor());
			}
		}
		board.setSquares(squares);
		// board.setGoal(new RowCol(2, 3), true); // goal location
		// gs.setBoard(board);
		return board;
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
	 * Places random colors on specified board
	 */
	protected void setPalette(Scanner in) {
		GamePalette gp = new GamePalette();
		// If input id <colors> </colors> then we give default values
		// else we use the values defined in the board configuration file.

		String color = in.next();
		if (color.compareTo("</colors>") == 0) {
			// default values:
			gp.add("CTRed");
			gp.add("CTGreen");
			gp.add("CTPurple");
		} else {
			// the token that was read before the if has to be be a color.
			// (there is no check for errors, at the moment
			// we assume that the input file is written properly!)
			gp.add(color);
			System.out.println("color: " + color);
			while ((color = in.next()).compareTo("</colors>") != 0) {
				System.out.println("color: " + color);
				gp.add(color);
			}
		}
		in.nextLine();
		gs.setGamePalette(gp);
		System.out.println(gp.toString());
		FileLogger.getInstance(logFile).writeln(gp.toString());
		System.out.println("Finished reading colors");
	}

	protected Board setBoard(GamePalette gp, Scanner in, int row, int col)
			throws Exception {

		System.out.println("-------- Creating Board --------");
		System.out.println("Sanity: " + gp.toString());

		Board board;
		if (in.hasNextInt()) {
			board = new Board(row, col);
			Square[][] squares = new Square[row][col];
			for (int r = 0; r < row; r++) {
				String colors = new String();
				for (int c = 0; c < col; c++) {
					squares[r][c] = new Square();
					String color = gp.get(in.nextInt());
					squares[r][c].setColor(color);
					colors += color;
					colors += " ";
					System.out.println("setting color: " + color
							+ " at (x,y) = (" + r + ", " + c + ")");
				}
				FileLogger.getInstance(logFile).writeln(colors);
			}
			board.setSquares(squares);
			System.out.println("finished setting squares");
		} else
			// if there are no square colors defined, that is, the input is
			// <squareColors> </squareColors>
			// then choose randomly.
			board = setRandBoard(gp, row, col);
		// filelogger.getInstance(logFile).writeln(board.toString());
		return board;
	}

	protected ChipSet getChipSet(GamePalette gp, Scanner in) {
		System.out.println("In getChipSet");
		ChipSet cs = new ChipSet();
		int counter = 0;

		// in.next(); //clear <...PlayerChips>
		while (in.hasNextInt()) {
			System.out.println("counter: " + counter);
			// System.out.println("nextInt: " +in.nextInt());
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

	protected RowCol getRandPosition(int rows, int cols) {
		// Board board = gs.getBoard();
		// System.out.println("in getRandPosition, rows = " + board.getRows() +
		// " cols = " + board.getCols());

		int row = localrand.nextInt(rows);
		int col = localrand.nextInt(cols);
		System.out.println("in getRandPosition, position = (" + row + ", "
				+ col + ")");
		return new RowCol(row, col);
	}

	class PlayerStatusComparator implements Comparator<Object> {

		public int compare(Object o1, Object o2) {
			PlayerStatus u1 = (PlayerStatus) o1;
			PlayerStatus u2 = (PlayerStatus) o2;
			int val;
			if (u1.getPin() > u2.getPin())
				val = 1;
			else if (u1.getPin() < u2.getPin())
				val = -1;
			else
				val = 0;
			return val;
		}
	}

	protected void setPlayersChips(int numberOfPlayers, GamePalette gp,
			Scanner in, int rows, int cols) {
		if (gs.getPlayers().size() != numberOfPlayers)
			System.out
					.println("Num of players in game board configuration ("
							+ numberOfPlayers
							+ ") differs from number of players defined for this game ("
							+ gs.getPlayers().size() + ")");
		// for all the players

		ArrayList<PlayerStatus> playersArray = new ArrayList<PlayerStatus>();
		for (PlayerStatus player : gs.getPlayers()) {
			playersArray.add(player);
		}

		Collections.sort(playersArray, new PlayerStatusComparator());

		for (PlayerStatus player : playersArray) {
			System.out.println("Setting chips for player: " + player.getPin());
			FileLogger.getInstance(logFile).writeln(
					"Setting chips for player: " + player.getPin());
			player.setTeamId(3); // set teams for players
			if (in.hasNextInt()) // A set of chips was specified in the input
				// file, use it!
				player.setChips(getChipSet(gp, in));
			else {
				// No set of chips was specified in the input file for this
				// player,
				// create a random set of chips.
				player.setChips(getRandChipSet(gp));
				in.nextLine();
			}
			FileLogger.getInstance(logFile).writeln(
					player.getChips().toString());
			in.next(); // discard tittle ...PlayerStartingPos
			if (in.hasNextInt())
				player.setPosition(getPosition(in));
			else {
				player.setPosition(getRandPosition(rows, cols));
				in.nextLine();
			}
			FileLogger.getInstance(logFile).writeln(
					"Player position: " + player.getPosition().toString());
			if (in.hasNext())
				in.next(); // discard ...PlayerChips
		}
	}
}