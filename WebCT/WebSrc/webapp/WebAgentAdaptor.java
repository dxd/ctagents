package webapp;

import java.lang.reflect.Array;
import java.sql.Date;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import ctgui.original.ActionHistory.Chip;
import ctgui.original.discoursehandlers.BasicProposalDiscourseHandler;
import ctgui.original.discoursehandlers.BasicProposalDiscussionDiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;
import edu.harvard.eecs.airg.coloredtrails.agent.events.DiscourseMessageEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameEndedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameInitializedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.PhasesAdvancedEventListener;
import edu.harvard.eecs.airg.coloredtrails.agent.events.PlayersUpdatedEventListener;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.client.ui.discourse.DiscourseHandler;
import edu.harvard.eecs.airg.coloredtrails.server.ServerData;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.ChipsRevelationDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.DiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.discourse.TransferDiscourseMessage;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Board;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Goal;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Square;

//this class contain the implementation of a single web user the user will instantiate a client impl and will interact with his events
public class WebAgentAdaptor implements GameStartEventListener,
		GameEndedEventListener, GameInitializedEventListener,
		DiscourseMessageEventListener, Observer, PhasesAdvancedEventListener {

	// the interface that will be used to connect to the server
	public ColoredTrailsClientImpl client;
	// game status indicator
	public boolean game_started = false;
	public boolean game_initialized = false;
	public boolean game_ended = false;
	public boolean Phase_completed = false;
	public boolean FirstPhase = true;
	public ClientGameStatus GameStat;
	public String ID = "";
	public Hashtable<String, PlayerStatus> LastUpdatePlyerStatus;
	public ArrayList<BasicProposalDiscourseMessage> LastUpdateProposaleMessages;
	public ArrayList<TransferDiscourseMessage> LastUpdateTransfer;
	
	private int initialNumOfGoals = 0;
	private ArrayList<DiscourseHandler> discourseHandlers = new ArrayList<DiscourseHandler>();
	public ArrayList<BasicProposalDiscussionDiscourseMessage> LastUpdateResponsMessages;
	public Hashtable<String, BasicProposalDiscourseMessage> MessagesPendingResponse;
	public ArrayList<ChipsRevelationDiscourseMessage> LastUpdateRevelationChips;
	
	Logger logger;
	//Hashtable<Integer, JSONArray> lastRevelationChips;
	TimeWatch timer;

	// boolean revelationChipsUpdate = false;

	/*
	 * create a new clinet with the IP and PIN and connect it to the server
	 */
	public WebAgentAdaptor(String IP, String Pin) {
		// init Logger
		logger = Logger.getLogger(WebAgentAdaptor.class.getName());

		// init lastRevelatinChips
		//lastRevelationChips = new Hashtable<Integer, JSONArray>();

		// discourse handler for making proposals
		discourseHandlers.add(new BasicProposalDiscourseHandler());
		// discourse handler for talking about proposals
		discourseHandlers.add(new BasicProposalDiscussionDiscourseHandler());

		client = new ColoredTrailsClientImpl();
		ID = "1234" + Pin;
		client.setPin(Pin);
		// client.communication.setDiscourseHandlers(discourseHandlers);

		// subscribe to events
		client.addGameStartEventListener(this);
		client.addGameInitializedEventListener(this);
		client.addGameEndedEventListener(this);
		client.addDiscourseMessageEventListener(this);
		client.addPhasesAdvancedEventListener(this);

		client.start();
	}

	/*
	 * This method will be invoked when the Game has ended
	 */
	public void gameEnded() {
		System.out.println("Game Ended");
		// System.exit(0);
	}

	/*
	 * this method will create the json message for a specific chipSet
	 */
	public static JSONArray GetJsonFromChipSet(ChipSet chips) {
		JSONArray arr = new JSONArray();
		// Iterate on chip colors to add chips to messages
		Object[] colors = chips.getColors().toArray();

		for (int j = 0; j < colors.length; j++) {
			// add the received chip set to message
			JSONObject colorMsg = new JSONObject();
			String color = ColorConverter.getClientColor((String) colors[j]);
			colorMsg.put("color", color);
			colorMsg.put("sum",
					new Integer(chips.getNumChips((String) colors[j])));
			arr.add(colorMsg);

		}

		return arr;
	}

	public static JSONObject GetJsonFromPosition(RowCol pos) {

		// add the received chip set to message
		JSONObject jsonPos = new JSONObject();

		jsonPos.put("x", new Integer(pos.col));
		jsonPos.put("y", new Integer(pos.row));
		return jsonPos;
	}

	/*
	 * get update will reflect the changes to the game since the last update to
	 * a json format also he will initialize the last updated messages
	 */
	public String getUpdates() {
		// create the message object
		JSONObject object = new JSONObject();
		// got messages to update the client
		JSONArray requestsArray = new JSONArray();
		JSONArray responseArray = new JSONArray();
		if ((LastUpdateProposaleMessages != null)
				&& (LastUpdateProposaleMessages.size() != 0)) {

			// create message array

			for (int i = 0; i < LastUpdateProposaleMessages.size(); i++) {
				// check if message is response message

				Random r = new Random();
				int id = r.nextInt(1000000);
				// add the request message
				BasicProposalDiscourseMessage msg = LastUpdateProposaleMessages
						.get(i);
				JSONObject jsonMsg = new JSONObject();
				jsonMsg.put("SenderID", new Integer(msg.getFromPerGameId()));
				jsonMsg.put("MessageId", new Integer(id));
				jsonMsg.put("ReceivedChips", WebAgentAdaptor
						.GetJsonFromChipSet(msg.getChipsSentByProposer()));
				jsonMsg.put("SentChips", WebAgentAdaptor.GetJsonFromChipSet(msg
						.getChipsSentByResponder()));
				requestsArray.add(jsonMsg);

				// add to pending messages
				if (MessagesPendingResponse == null) {
					MessagesPendingResponse = new Hashtable<String, BasicProposalDiscourseMessage>();
				}

				MessagesPendingResponse.put(id + "", msg);

			}
			// initialize the message Queue for the next update
			LastUpdateProposaleMessages = new ArrayList<BasicProposalDiscourseMessage>();
		}
		
		
		//gil
		//System.out.println("Web Agent Adaptor: " + GameStat.getMyPlayer().isRevelationAllowed());
		object.put("isGoalRevelationAllowed", GameStat.getMyPlayer().isRevelationAllowed());
		object.put("numOfGoals", GameStat.getBoard().getGoals().size());
		//gilend
		object.put("isEnded", GameStat.isEnded());
		object.put("role",
				GameStat.getMyPlayer().getRole().equals("Responder") ? 1 : 0);
		// object.put("CurrentPhase",
		// GameStat.getPhases().getCurrentPhaseName());
		// object.put("PhaseSecsLeft",
		// GameStat.getPhases().getCurrentSecsLeft());
		object.put("CurrentPhase", client.getGameStatus().getPhases()
				.getCurrentPhaseName());
		object.put("PhaseSecsLeft", client.getGameStatus().getPhases()
				.getPhaseDuration()
				- (int) (timer.time(TimeUnit.SECONDS)));

		object.put("PhaseComplete", Phase_completed);

		
		// revelation chips
		JSONArray RevelationArray = new JSONArray();

		if (LastUpdateRevelationChips != null 
				&& LastUpdateRevelationChips.size() != 0) {
			
			for (int i = 0; i < LastUpdateRevelationChips.size(); i++) {
				ChipsRevelationDiscourseMessage crdm = LastUpdateRevelationChips.get(i);
				
				JSONObject jsonRevelationMsg = new JSONObject();
				
				jsonRevelationMsg.put("playerID", crdm.getFromPerGameId());
				jsonRevelationMsg.put("recipient", crdm.getToPerGameId());
				jsonRevelationMsg.put("chips", GetJsonFromChipSet(crdm.getRevelationChips()));
				
				RevelationArray.add(jsonRevelationMsg);
				
				LastUpdateRevelationChips.remove(i);
			}
		}
		
		object.put("RevelationChips", RevelationArray);

		
		// bordercolors update (board pallet)
		JSONArray borderColorsArray = new JSONArray();

		Board br = GameStat.getBoard();
		for (int i = 0; i < br.getRows(); i++) {
			for (int j = 0; j < br.getCols(); j++) {
				Square s = br.getSquare(i, j);
				JSONObject jsonBorderColors = new JSONObject();
				jsonBorderColors.put("color",
						ColorConverter.getClientColor(s.getColor()));
				jsonBorderColors.put("posX", i);
				jsonBorderColors.put("posY", j);
				borderColorsArray.add(jsonBorderColors);
			}
		}

		object.put("BorderColors", borderColorsArray);

		//gil - goals array
		JSONArray goalsArray = new JSONArray();
		
		if(initialNumOfGoals!=br.getGoals().size())
		{
			
			
			
			Set<Goal> goalsSet = new HashSet<Goal>();
			goalsSet = br.getGoals();
			Iterator<Goal> it = goalsSet.iterator();
					
			
			while(it.hasNext())
			{
				Goal g = it.next();
				
				JSONObject jsonGoal = new JSONObject();
							
				RowCol cur = g.getLocation();
				jsonGoal.put("id", g.getID());
				jsonGoal.put("type", g.getType());
				jsonGoal.put("posX",  cur.row);
				jsonGoal.put("posY", cur.col);
				
				goalsArray.add(jsonGoal);
			}
		
		}
		
		object.put("Goals", goalsArray);
		//gilend
		
		
		Phase_completed = false;
		object.put("msgs", requestsArray);
		if ((LastUpdateResponsMessages != null)
				&& (LastUpdateResponsMessages.size() != 0)) {

			// create message array

			for (int i = 0; i < LastUpdateResponsMessages.size(); i++) {
				// check if message is response message

				BasicProposalDiscussionDiscourseMessage msg = LastUpdateResponsMessages
						.get(i);

				JSONObject jsonMsg = new JSONObject();

				jsonMsg.put("SenderID", new Integer(msg.getFromPerGameId()));
				jsonMsg.put("ReceiverID", new Integer(msg.getToPerGameId()));
				jsonMsg.put("MessagesID", new Integer(msg.getMessageId()));
				if (msg.accepted()) {
					jsonMsg.put("response", "Accept");
				} else {
					jsonMsg.put("response", "Reject");
				}
				jsonMsg.put("ReceivedChips", WebAgentAdaptor
						.GetJsonFromChipSet(msg.getChipsSentByProposer()));
				jsonMsg.put("SentChips", WebAgentAdaptor.GetJsonFromChipSet(msg
						.getChipsSentByResponder()));
				responseArray.add(jsonMsg);
			}
		}
		object.put("response", responseArray);
		LastUpdateResponsMessages = new ArrayList<BasicProposalDiscussionDiscourseMessage>();
		// got Transfer messages to update the client
		JSONArray TransferArray = new JSONArray();
		if ((LastUpdateTransfer != null) && (LastUpdateTransfer.size() != 0)) {

			// create message array

			for (int i = 0; i < LastUpdateTransfer.size(); i++) {
				TransferDiscourseMessage msg = LastUpdateTransfer.get(i);
				JSONObject jsonMsg = new JSONObject();
				jsonMsg.put("SenderID", new Integer(msg.getFromPerGameId()));
				jsonMsg.put("MessageId", new Integer(msg.getMessageId()));
				jsonMsg.put("ReceivedChips", WebAgentAdaptor
						.GetJsonFromChipSet(msg.getTransferredChips()));
				TransferArray.add(jsonMsg);

			}

			// LastUpdateTransfer = new ArrayList<TransferDiscourseMessage>();
		}
		object.put("transfer", TransferArray);

		// get board changes
		Set<PlayerStatus> currentPlayerStatus = GameStat.getPlayers();
		JSONArray MoveArray = new JSONArray();
		JSONArray ChipsArray = new JSONArray();

		if (LastUpdatePlyerStatus != null) {
			for (PlayerStatus player : currentPlayerStatus) {
				// get last updated player statuss
				PlayerStatus lastPlayer = LastUpdatePlyerStatus.get(player
						.getPerGameId() + "");
				// check if there was any changes made to the chipSet of the
				// player
				if (!player.getChips().equals(lastPlayer.getChips())) {
					JSONObject jsonMsg = new JSONObject();
					jsonMsg.put("playerID", new Integer(player.getPerGameId()));
					jsonMsg.put("chips", WebAgentAdaptor
							.GetJsonFromChipSet(player.getChips()));
					ChipsArray.add(jsonMsg);
				}
				if (!player.getPosition().equals(lastPlayer.getPosition())) {

					JSONObject jsonMsg = new JSONObject();
					jsonMsg.put("playerID", new Integer(player.getPerGameId()));
					jsonMsg.put("position", WebAgentAdaptor
							.GetJsonFromPosition(player.getPosition()));
					MoveArray.add(jsonMsg);
				}

			}
		}
		object.put("chipsChange", ChipsArray);
		object.put("moveChange", MoveArray);
		// Update the last updated items to the current
		LastUpdatePlyerStatus = new Hashtable<String, PlayerStatus>();
		for (PlayerStatus player : currentPlayerStatus) {
			LastUpdatePlyerStatus.put(player.getPerGameId() + "",
					(PlayerStatus) player.clone());
		}

		return (object.toString());

		// return("update Method");
	}

	public void gameStarted() {
		
		System.out.println("Game Started");
		game_started = true;
		System.out.println("we have "
				+ client.getGameStatus().getBoard().getGoals().size()
				+ " goals");
		GameStat = client.getGameStatus();
	}

	public Boolean SendDiscorseRequest(DiscourseMessage dm) {
		if (client.isCommunicationAllowed()) {
			client.communication.sendDiscourseRequest(dm);
			return true;

		}
		return false;

	}

	public void gameInitialized() {
		System.out.println("Game Initialized");
		game_initialized = true;

		ClientGameStatus cgs = client.getGameStatus();
		String phaseName = cgs.getPhases().getCurrentPhaseName();
		System.out.println("AGENT " + client.getPin()
				+ ": current phase name: " + phaseName);
		System.out.println("we have "
				+ client.getGameStatus().getBoard().getGoals().size()
				+ " goals");
	}

	public String getClientName() {
		return client.getPin();
	}

	public void setClientName(String name) {
		client.setPin(name);
	}

	public void start() {
		client.start();
	}

	@Override
	public void onReceipt(DiscourseMessage dm) {
		// TODO Auto-generated method stub

		if (dm instanceof BasicProposalDiscourseMessage) {
			// Message received is a proposal message
			if (dm instanceof BasicProposalDiscussionDiscourseMessage) {
				if (LastUpdateResponsMessages == null) {
					LastUpdateResponsMessages = new ArrayList<BasicProposalDiscussionDiscourseMessage>();
				}
				BasicProposalDiscussionDiscourseMessage proposal = (BasicProposalDiscussionDiscourseMessage) dm;
				LastUpdateResponsMessages.add(proposal);
				return;
			} else {
				// the message is a basic proposal

				if (LastUpdateProposaleMessages == null) {
					LastUpdateProposaleMessages = new ArrayList<BasicProposalDiscourseMessage>();
				}
				BasicProposalDiscourseMessage proposal = (BasicProposalDiscourseMessage) dm;

				LastUpdateProposaleMessages.add(proposal);
				return;
			}
		}

		if (dm instanceof TransferDiscourseMessage) {
			// Message received is a proposal message
			TransferDiscourseMessage transfer = (TransferDiscourseMessage) dm;
			if (LastUpdateTransfer == null) {
				LastUpdateTransfer = new ArrayList<TransferDiscourseMessage>();
			}
			LastUpdateTransfer.add(transfer);
			return;
		}

		if (dm instanceof ChipsRevelationDiscourseMessage) {
			ChipsRevelationDiscourseMessage crdm = (ChipsRevelationDiscourseMessage)dm;
			
			if (LastUpdateRevelationChips == null) {
				LastUpdateRevelationChips = new ArrayList<ChipsRevelationDiscourseMessage>();
			}
			LastUpdateRevelationChips.add(crdm);
			return;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void phaseAdvanced(Phases ph) {
		// TODO Auto-generated method stub
		System.out.println(ph.getCurrentPhaseName() + " "
				+ ph.getPhaseDuration());

		timer = TimeWatch.start();

		if (FirstPhase) {
			FirstPhase = false;
		} else {
			Phase_completed = true;
		}

	}

	private boolean isRevChipsAllZero(ChipSet revChips) {
		Object[] colors = revChips.getColors().toArray();

		for (int i = 0; i < colors.length; i++) {
			if (revChips.getNumChips(colors[i].toString()) != 0) {
				return false;
			}
		}

		return true;
	}
}
