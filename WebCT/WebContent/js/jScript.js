/*
 * green - #00ff00
 * purple - #9b30ff
 * red = #ff0000
 * orange - #ffa500
 */

//json
/*
 var game = {
 // AFTER CHANGE VALUES CONVERT TO INTEGER WITH PARSE INT!!
 "rows" : 4,
 "cols" : 4,
 "numOfGolals" : 4,
 "numOfPlayer" : 2,
 "numGoals" : 4,
 "numOfColors" : 4,
 "players" : [ {
 "id" : 1,
 "icon" : "me.gif",
 "name" : "hen",
 "isme" : "true",
 "age" : 23,
 "posX" : 2,
 "posY" : 1,
 "cards" : [ {
 "color" : "00ff00",
 "sum" : 1
 }, {
 "color" : "9b30ff",
 "sum" : 2
 }, {
 "color" : "ff0000",
 "sum" : 3
 }, {
 "color" : "ffa500",
 "sum" : 1
 } ],
 "RevelationChips" : [ {
 "color" : "00ff00",
 "sum" : 1
 }, {
 "color" : "9b30ff",
 "sum" : 2
 }, {
 "color" : "ff0000",
 "sum" : 3
 }, {
 "color" : "ffa500",
 "sum" : 1
 } ],
 "goalsId" : [ {
 "id" : 3
 }, {
 "id" : 5
 } ]
 }, {
 "id" : 2,
 "icon" : "them1.gif",
 "name" : "shay",
 "isme" : "false",
 "age" : 32,
 "posX" : 1,
 "posY" : 1,
 "cards" : [ {
 "color" : "00ff00",
 "sum" : 4
 }, {
 "color" : "9b30ff",
 "sum" : 5
 }, {
 "color" : "ff0000",
 "sum" : 1
 }, {
 "color" : "ffa500",
 "sum" : 2
 } ],
 "goalsId" : [ {
 "id" : 4
 }, {
 "id" : 2
 } ]
 }, ],
 "goals" : [ {
 "id" : 2,
 "name" : "gola1",
 "posX" : 3,
 "posY" : 1
 }, {
 "id" : 3,
 "name" : "gola1",
 "posX" : 2,
 "posY" : 3
 }, {
 "id" : 4,
 "name" : "goalAA",
 "posX" : 2,
 "posY" : 1
 }, {
 "id" : 5,
 "name" : "goalBB",
 "posX" : 3,
 "posY" : 2
 } ],
 "borderColors" : [ {
 "color" : "ffa500",
 "posX" : 0,
 "posY" : 0
 }, {
 "color" : "ff0000",
 "posX" : 0,
 "posY" : 1
 }, {
 "color" : "00ff00",
 "posX" : 1,
 "posY" : 1
 }, {
 "color" : "9b30ff",
 "posX" : 1,
 "posY" : 0
 }, {
 "color" : "ff0000",
 "posX" : 2,
 "posY" : 0
 }, {
 "color" : "ffa500",
 "posX" : 2,
 "posY" : 1
 }, {
 "color" : "ffa500",
 "posX" : 2,
 "posY" : 2
 }, {
 "color" : "ff0000",
 "posX" : 0,
 "posY" : 2
 }, {
 "color" : "ff0000",
 "posX" : 1,
 "posY" : 2
 }, {
 "color" : "ffa500",
 "posX" : 0,
 "posY" : 3
 }, {
 "color" : "ffa500",
 "posX" : 1,
 "posY" : 3
 }, {
 "color" : "ff0000",
 "posX" : 2,
 "posY" : 3
 }, {
 "color" : "ff0000",
 "posX" : 3,
 "posY" : 0
 }, {
 "color" : "00ff00",
 "posX" : 3,
 "posY" : 1
 }, {
 "color" : "ffa500",
 "posX" : 3,
 "posY" : 2
 }, {
 "color" : "9b30ff",
 "posX" : 3,
 "posY" : 3
 }

 ],
 "colors" : [ {
 "name" : "00ff00"
 }, {
 "name" : "9b30ff"
 }, {
 "name" : "ff0000"
 }, {
 "name" : "ffa500"
 } ],
 // times

 "CommunicationPhaseTimeConst" : "00:60",
 "ExchangePhaseTimeConst" : "00:20",
 "MovementPhaseTimeConst" : "00:17",
 "FeedBackPhaseTimeConst" : "00:19",
 // set cards to player
 setSumChips : function(playerID, color, sum) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 for ( var j = 0; j < game.players[i].cards.length; j++)
 if (game.players[i].cards[j].color == color)
 game.players[i].cards[j].sum = sum;
 }
 },
 getPlayerName : function(playerId) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerId) {
 return game.players[i].name;
 }
 },
 // getPlayerId
 getPlayerId : function(serial) {
 return game.players[serial].id;
 },
 getNumOfPlayer : function() {
 return game.numOfPlayer;
 },
 // get cards
 getSumChips : function(playerID, color) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 for ( var j = 0; j < game.players[i].cards.length; j++)
 if (game.players[i].cards[j].color == color)
 return game.players[i].cards[j].sum;
 }
 },
 getGoalPositionX : function(goalID) {
 for ( var i = 0; i < this.numGoals; i++)
 if (game.goals[i].id == goalID) {
 return game.goals[i].posX;
 }
 },
 getGoalPositionY : function(goalID) {
 for ( var i = 0; i < this.numGoals; i++)
 if (game.goals[i].id == goalID) {
 return game.goals[i].posY;
 }
 },
 getPlayerPositionX : function(playerID) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 return game.players[i].posX;
 }
 },

 getPlayerPositionY : function(playerID) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 return game.players[i].posY;
 }
 },
 getPlayerSerial : function(playerID) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 return i;
 }
 },
 getPlayerName : function(playerID) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 return game.players[i].name;
 }
 },
 getPlayerIcon : function(playerID) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 return game.players[i].icon;
 }
 },
 getisPlayerMe : function(playerID) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 return game.players[i].isme;
 }
 },
 getNumOfColors : function() {
 return game.numOfColors;
 },
 setPlayerPosition : function(playerID, x, y) {
 for ( var i = 0; i < playerNumTotal; i++)
 if (game.players[i].id == playerID) {
 game.players[i].posY = y;
 game.players[i].posX = x;
 }
 },
 getSizeRows : function() {
 return this.rows;
 },
 getSizeCols : function() {
 return this.cols;
 },
 getColorCell : function(posX, posY) {
 for ( var i = 0; i < this.borderColors.length; i++)
 if ((this.borderColors[i].posX == posX)
 && (this.borderColors[i].posY == posY)) {
 return this.borderColors[i].color;
 }
 }
 };
 // end json
 */
 

// times
var numTurns;
var CommunicationPhaseTimeConst;
var RevelationPhaseTimeConst;
var MovementPhaseTimeConst;
var FeedBackPhaseTimeConst;

// for the history grid
var recId;

// set temp times
var CommunicationPhaseTime;
var RevelationPhaseTime;
var MovementPhaseTime;
var FeedBackPhaseTime;

// customize board size
var numOfRows;
var numOfColumns;

// player num MUST be because IE8 and fire
var playerNumTotal;

// get num of colors
var colorNumTotal;

// number of game phases
var numOfPhases;

// card in ID historyProArea
var historyProAreaCard;

// the current time displayed on the progress bar
var pValue;
// the current phase of the game
var currentPhase;
// the current phase index in phases json array
var currentPhaseIndex;
// the time of the current phase of the game
var currentPhaseTime;
// revelation chips flag (true/false)
var isRevelationEnabled;
// my role
var lastRole;
//indicate if a phase was changed
var phaseChanged;
// revelation goal flag (true/false) indicating whether goal revelation phase occured
var goalRevelationPhaseEnded;
// if the goal revelation submitted
var isGoalRevelationSubmitted;
//indicate whether there is a pending proposal and its message id
//id = -1 indicates that there is no pending message
var pendingProposalMsgID;

function Init() {
	// times
	numTurns = 3;
	// CommunicationPhaseTimeConst = game.CommunicationPhaseTimeConst;
	// RevelationPhaseTimeConst = game.RevelationPhaseTimeConst;
	// MovementPhaseTimeConst = game.MovementPhaseTimeConst;
	// FeedBackPhaseTimeConst = game.FeedBackPhaseTimeConst;

	// for the history grid
	recId = 100;

	// set temp times
	// CommunicationPhaseTime = CommunicationPhaseTimeConst;
	// RevelationPhaseTime = RevelationPhaseTimeConst;
	// MovementPhaseTime = MovementPhaseTimeConst;
	// FeedBackPhaseTime = FeedBackPhaseTimeConst;

	// customize board size
	numOfRows = game.getSizeRows();
	numOfColumns = game.getSizeCols();

	// player num MUST be because IE8 and fire
	playerNumTotal = game.getNumOfPlayer();

	//gil
	//alert("Init - playerNumTotal = game.getNumOfPlayer();");
	//gilend
	
	
	// get num of colors
	colorNumTotal = game.getNumOfColors();

	// number of game phases
	numOfPhases = game.numOfPhases;
	
	// card in ID historyProArea
	historyProAreaCard = "history";

	// the current time displayed on the progress bar
	pValue = 0;
	// the current phase of the game
	currentPhase = null;
	// the current phase index in phases json array
	currentPhaseIndex = 0;
	// the time of the current phase of the game
	currentPhaseTime = 0;
	
	// set revelation flag
	isRevelationEnabled = game.isRevelationEnabled;	
	
	// set role
	lastRole = game.role;
	
	//set goal revelation phase flag
	goalRevelationPhaseEnded = false;
	
	phaseChanged = true;
	//set goal revelation submit flag
	isGoalRevelationSubmitted = false;
	//set pending proposal flag
	pendingProposalMsgID = -1;
}

// parse query string from url
function getQueryVariable(variable) {
	var query = window.location.search.substring(1);
	var vars = query.split("&");
	for ( var i = 0; i < vars.length; i++) {
		var pair = vars[i].split("=");
		if (pair[0] == variable) {
			return pair[1];
		}
	}
}
// END parse query string from url

// CREATE TABLE GAME BOARD
function createGameBoard() {
	
	//alert("Inside createGameBoard");
	
	// create table
	var drops = new Array();
	var theTable = document.getElementById("GameBoardTable");
	for ( var i = 0; i < numOfRows; i++) {
		var newRow = theTable.insertRow(theTable.rows.length);
		newRow.id = newRow.uniqueID;
		var newCell;
		for ( var j = 0; j < numOfColumns; j++) {
			// create column
			newCell = newRow.insertCell(j);
			newCell.id = "CellRow" + i + "Column" + j;
		// newCell.innerText = i+", "+j;
			newCell.bgColor = "white";
			newCell.ondblclick = move; //function() { alert(i+'-'+j);move(i,j); };
			// create div
			var newDiv = document.createElement("div");
			newDiv.className = "cssDDContainer";
			newDiv.id = "DivRow" + i + "Column" + j; // id of new div
			
			var cell = document.getElementById("CellRow" + i + "Column" + j);
			cell.appendChild(newDiv);
		}
	}

	var playerContainer;
	var playerId;
	for ( var i = 0; i < playerNumTotal; i++) {
		playerId = game.getPlayerId(i);
		var newDiv = document.createElement("div");
		playerContainer = document.getElementById("DivRow"
				+ game.getPlayerPositionX(playerId) + "Column"
				+ game.getPlayerPositionY(playerId));
		// newDiv.className = "cssDDItem";
		newDiv.id = "player" + playerId;
		
		
		if (game.getisPlayerMe(playerId) == "true"){
			newDiv.innerHTML = '<img src="img/' + game.getBigPlayerIcon(playerId) + '"/>';
		}
		else
		{
			newDiv.innerHTML = '<img src="img/' + game.getPlayerIcon(playerId) + '"/>';
		}
		playerContainer.appendChild(newDiv);
		var divElem = document.getElementById("player" + playerId);
		// create a div empty that can be drag
		// if (game.getisPlayerMe(playerId) == "true")
		// divElem.dd = new Ext.dd.DDProxy("player" + playerId);
	}
}
// END CREATE GAME BOARD

function move(e)
	{
	var target = e.target || e.srcElement;
	
	var posx = target.id.substring(6, 7);
	var posy = target.id.substring(13, 14);
	//alert(posx+posy);
	var stringJ = "{\"posx\" : \"" + posx + "\", \"posy\" : \"" + posy + "\"}";

		jQuery.ajax({
			type : "post",
			url : "move.jsp",
			data : "json=" + stringJ,
			success : function(msg) {
		   // alert(msg);
			}
		});

	}
// ADD COLORS TO BOARD
function addColors() {
	for ( var i = 0; i < game.borderColors.length; i++) {
		var posX = game.borderColors[i].posX;
		var posY = game.borderColors[i].posY;
		
		// jQuery("#DivRow" + posX + "Column" + posY).removeClass();
		jQuery("#DivRow" + posX + "Column" + posY).css({
			'background-color' : '#' + game.borderColors[i].color
		});
	}
}
// END ADD COLORS TO BOARD


// CREATE GOALS
function createGoals() {
	
	/*
	//alert("Inside  createGoals");
	var goalSquare;
	for ( var i = 0; i < game.numOfGolals; i++) {
		var newDiv = document.createElement("div");
		newDiv.innerHTML = '<img src="img/flag2.png">';
		goalSquare = document.getElementById("DivRow" + game.goals[i].posX
				+ "Column" + game.goals[i].posY);
		goalSquare.appendChild(newDiv);
		
		//alert("Goal " + "DivRow" + game.goals[i].posX	+ "Column" + game.goals[i].posY);
	}
	*/
	////////////////gil
	var playerID = game.getMe();
	var goalSquare;
	
	
	if (game.numOfGolals ==1 )  //Bargain Only and WebCTRevelation
	{
		 var newDiv = document.createElement("div");
		 newDiv.innerHTML = '<img src="img/goal.gif">';
		 goalSquare = document.getElementById("DivRow" + game.goals[0].posX
                 + "Column" + game.goals[0].posY);
		 goalSquare.appendChild(newDiv); 
	}
	else  //Signaling
	{
		var isMeRevealed = true;
		
		for(var i=0; i< game.numOfGolals&&isMeRevealed; i++)
		{
			if(game.goals[i].type >= 10 && game.goals[i].type%10 == playerID)
			{
				isMeRevealed = false;
			}
		}
		
	    for ( var i = 0; i < game.numOfGolals; i++) 
	    {
	    	var newDiv = document.createElement("div");
          
	    	if(game.goals[i].type%10 == playerID) //my goal
	    	{
	    		if(isMeRevealed)
	    		{
	    			newDiv.innerHTML = '<img src="img/goalA.gif">';
	    		}
	    		else
	    		{
	    			if(game.goals[i].type >= 10 && game.goals[i].type <20){  //g 50 me
	                       newDiv.innerHTML = '<img src="img/goalA_belief50.gif">';
	               }
	    			else
	    			{
	    				if(game.goals[i].type >= 20)
	    				{
	    					newDiv.innerHTML = '<img src="img/beliefMe50.gif">';
	    				}
	    				else
	    				{
	    					continue;
	    				}
	    					
	    			}
	    			
	    		}
	    		
	    	}
	    	else  //opponent's goal
	    	{
	    		if(game.goals[i].type >= 10)
	    		{
	    			newDiv.innerHTML = '<img src="img/possibleG50.gif">';
	    		}
	    		else
	    		{
	    			newDiv.innerHTML = '<img src="img/goalB.gif">';
	    		}
	    	}
	    	 goalSquare = document.getElementById("DivRow" + game.goals[i].posX
                     + "Column" + game.goals[i].posY);
	    	 goalSquare.appendChild(newDiv);     
           
	    }//for
		
		
		
		
	}//else
	
	
	
	////////////////gilend
	
	
	
	
	/*
	var playerID = game.getMe();
    var possibleGoals = false;
    for( var i = 0; i < game.numOfGolals && possibleGoals == false; i++)
    {
            if(game.goals[i].type >= 10) //&& game.goals[i].type%10 == playerID)
            {
                    possibleGoals = true;
            }
    }
    
    var goalSquare;
    for ( var i = 0; i < game.numOfGolals; i++) {
            if(possibleGoals == false || game.goals[i].type >= 10)
            {
                    var newDiv = document.createElement("div");
                    if(possibleGoals == false){ //bargain only
                            newDiv.innerHTML = '<img src="img/goal.gif">';
                    }
               else if(game.goals[i].type%10 != playerID){ // possiblity 50 rival
                            newDiv.innerHTML = '<img src="img/possibleG50.gif">';
                    }
               else if(game.goals[i].type >= 10 && game.goals[i].type <20){  //g 50 me
                       newDiv.innerHTML = '<img src="img/goalA_belief50.gif">';
               }
               else{ // possible 50 me
                       newDiv.innerHTML = '<img src="img/beliefMe50.gif">';
               }
                    goalSquare = document.getElementById("DivRow" + game.goals[i].posX
                                    + "Column" + game.goals[i].posY);
                    goalSquare.appendChild(newDiv);        
            }   
    }
    */
}
// END CREATE GOLAS

// update the progress bar
function updateProgressBar() {
	
	//gil
	//alert("updateProgressBar started");
	//gilend
	
	
	// check if game has ended
	if (game.isEnded == true) {
		self.location = "ended.jsp";
	}
	
	
	// check phase ended or game just started
	if (phaseChanged == true) {
	
		
		
		lastRole = game.role;
		
		if (currentPhase == null) {
			currentPhase = game.phases[currentPhaseIndex].name;
		}
		else
		{
			/*
			if (currentPhaseIndex == (game.phases.length - 1)) 
			{
				currentPhaseIndex = 0;
			}
			else
			{
				currentPhaseIndex++;	
			}
			*/
			currentPhaseIndex = game.phasesIndex[currentPhase];
			
		}
		
		//alert("current phase = "+ currentPhase);
		//alert("phaseHashNameIndex = "+ game.phasesIndex[currentPhase]);
		
		phaseChanged = false;
		
		//check whether goal revelation phase occured
			if(goalRevelationPhaseEnded == true)
			{
				if(isGoalRevelationSubmitted == false)
				{
					clearProposalTableArea();
					loadProposalsTable();
				}
				goalRevelationPhaseEnded = false;
			}

		
		// call update to get most recent changes from server
		UpdateServer();
		
//		for (var i = 0; i < game.phases.length; i++) {
//			if (currentPhase == game.phases[i].name) {
//				//alert(i);
//				currentPhaseIndex = i;
//			}
//		}
		
		
		currentPhaseTime = game.phases[currentPhaseIndex].duration;
		if (currentPhaseIndex == 0) {
			pValue = game.phases[currentPhaseIndex].duration;
		}
			
	
		
		// clear first row at proposals table
		clearMessagesUI();
		
		switch (currentPhase) {
		case "Revelation Phase":
			//gil
			//goalRevelation
			if(game.getIsGoalRevelationAllowed())
			{
				clearProposalTableArea();
				loadGoalRevelationProposalsTable();
				//revelationArea();
				//gilend
				goalRevelationPhaseEnded = true;
				// notify
				SetHeaderMsg('This is revelation phase, choose whether to reveal your to your opponent');
			}
			else //chipRevelation
			{
				chipRevelationArea();
				SetHeaderMsg('This is revelation phase, choose how many of each chip color the opponent will see');
			}
			
			break;
		case "Communication Phase":
			// if communication phase -> build proposal area for players
			
			UpdateServer();			
			proposalArea();
			// notify			
			SetHeaderMsg(game.role == 1 ? 'this is Communication phase, you are the Receiver' : 'this is Communication phase, you are the Proposer');		
			break;
			
		case "Counter Offer Phase":
			// if communication phase -> build proposal area for players
			UpdateServer();			
			proposalArea();
			// notify
			SetHeaderMsg(game.role == 1 ? 'this is Counter Offer phase, you are the Receiver' : 'this is Counter Offer phase, you are the Proposer');
			break;	
		case "Movement Phase":	
			SetHeaderMsg('auto movement... please wait');
			break;
		case "Feedback Phase":			
			SetHeaderMsg('please fill Feedback report');
			break;
		default:			
			break;		
		}
	} else {
		// check if an update to server require a counter offer
		UpdateServer();
		if (game.role != lastRole) {			
			clearMessagesUI();
			proposalArea();
			lastRole = game.role;
			SetHeaderMsg(game.role == 1 ? 'couter offer, you are the Receiver' : 'counter offer, you are the Proposer');
		}
	}
	
	pValue = pValue - 1;

	$("#progressbar").progressbar({
		value : pValue / currentPhaseTime * 100
	});

	var divPhasesInnerHTML = "";
	for (var i = 0; i < game.phases.length; i++) {
		if (i == currentPhaseIndex) {
			divPhasesInnerHTML += '<b>' + game.phases[i].name + '</b> <br />';
		} else {
			divPhasesInnerHTML += game.phases[i].name + '<br />';
		}
	}
	
	var progressbar = $("#progressbar");
	var progressBarPossition = progressbar.position();
	$("#TimeLeft").css('top', '\'' + progressBarPossition.top + '\'px');
	
	document.getElementById("TimeLeft").innerHTML = currentPhase.substring(0, currentPhase.length - 5) + ' - ' 
			+ secondsToTime(pValue);
	
	document.getElementById("divPhases").innerHTML = divPhasesInnerHTML;
	
	
	if(pValue<2&& pendingProposalMsgID > -1)
	{
		alert("pValue - "+pValue + " pendingProposalMsgID =  "+pendingProposalMsgID);
		sendResponseAcceptReject(pendingProposalMsgID, 0);
	}
}
// END update the progress bar

// notification area
function SetHeaderMsg(textMsg) {
	$("#header").html(textMsg);
}

// clear message interface UI (first row in grid)
function clearMessagesUI() {
	removeElemFromDOM(document.getElementById('divTableMsgType'));
	removeElemFromDOM(document.getElementById('divTableSender'));
	removeElemFromDOM(document.getElementById('divTableReceiver'));
	removeElemFromDOM(document.getElementById('divTableSend'));
	removeElemFromDOM(document.getElementById('divTableReceive'));
	removeElemFromDOM(document.getElementById('divButtonPropose'));
	removeElemFromDOM(document.getElementById('notifyContainer'));	
}
// END clear message interface UI (first row in grid)

function removeElemFromDOM(elem) {
	while (elem.hasChildNodes()) {
		elem.removeChild(elem.lastChild);
	}
}

// Player Chips Area
function PlayerChips() {
	var theTable = document.getElementById("PlayerChips");

	
	
	for ( var i = 0; i < playerNumTotal; i++) {

		var newCell;

		// create chip row
		var newRow = theTable.insertRow(theTable.rows.length);

		// player name
		newCell = newRow.insertCell(0);
		newCell.innerHTML = '<img src="img/' + game.players[i].icon + '"/>';
		for ( var j = 0; j < game.players[i].cards.length; j++) {

			// create column
			newCell = newRow.insertCell(j + 1);
			newCell.id = "CellRowChips" + i + "ColumnChips" + j;
			newCell.className = "containerForChip";
			
			// create div
			var newDiv = document.createElement("div");
			// isme
			var isme = game.players[i].isme;
			// color sum chips
			var sumChips;

			// if player isme or not revelation game then show server chips
			// else show revelation chips
			if (isme == "true" || isRevelationEnabled == "false") {
				sumChips = game.players[i].cards[j].sum;
			}
			else {
				//sumChips = game.players[i].RevelationChips[j].sum;
				sumChips = "-";
			}
			
			
			newDiv.id = "playerId" + game.players[i].id + "Color"
					+ game.players[i].cards[j].color;
			newDiv.innerHTML = sumChips;
			newDiv.style.background = "#" + game.players[i].cards[j].color;

			var cell = document.getElementById("CellRowChips" + i
					+ "ColumnChips" + j);

			cell.appendChild(newDiv);
			
		}
	}
	
	
}
// end player chips area

// create Revelation area
function chipRevelationArea() {
	
	var playerId;
	var playerIdToSend;
	
	var divRevelationArea = document.createElement("div");
	divRevelationArea.setAttribute('id', 'revelationArea');		
	
	for (var i = 0; i < playerNumTotal; i++) {
		// populate data only for me (player)
		playerId = game.getPlayerId(i);
		if (game.getisPlayerMe(playerId) == "true") {
			playerIdToSend = playerId;
			for (var j = 0; j < game.players[i].cards.length; j++) {
				var cont = document.createElement("select");
				cont.setAttribute('id', 'SelectRevelationPlayerId'
						+ game.players[i].id + 'Color'
						+ game.players[i].cards[j].color);
				cont.style.background = "#"
					+ game.players[i].cards[j].color;
				
				divRevelationArea.appendChild(cont);
			}
		}
	}
	
	// add a button for submit
	var cont = document.createElement("div");
	cont.innerHTML = "<button id='buttonSubmitRevelation' onclick='buttonSubmitRevelation_click(" + playerIdToSend + ");'>Submit</button>";
	document.getElementById('divButtonPropose').appendChild(cont);	
	
	// append div into Messages grid
	document.getElementById("divTableSend").appendChild(divRevelationArea);
	document.getElementById("divTableMsgType").innerHTML = 'Revelation';
	document.getElementById("divTableSender").innerHTML = "<img src='img/me.gif'/>";
	
	InsertIntoPlayersIconsSelect();
	InsertIntoRevelationSelect();
}
// END create Revelation area

//Reset the proposal table area before/after changes where made by goal revelation phase
function clearProposalTableArea(){
	document.getElementById('proposals').innerHTML = '';
	var tableProposals = document.createElement('table');
	tableProposals.setAttribute('id', 'tblProposals');	
	document.getElementById('proposals').appendChild(tableProposals);

}

function loadGoalRevelationProposalsTable() {
	
	
	jQuery("#tblProposals").jqGrid(
			{
				datatype : "local",
				height : 200,
				colNames : ['MsgType', 'Sender', 'Receiver', 'Message',  'Response' ],
				colModel : [ {
					name : 'MsgType',
					index : 'MsgType',
					width : 90,
					sortable : false
				}, {
					name : 'Sender',
					index : 'Sender',
					width : 75,
					sortable : false
				},{
					name : 'Receiver',
					index : 'Receiver',
					width : 75,
					sortable : false
				},{
					name : 'Message',
					index : 'Message',
					width : 400,
					sortable : false
				},{
					name : 'Response',
					index : 'Response',
					width : 150,
					sortable : false
				} ],
				multiselect : false,
				hoverrows : false				
			});

	var defaultData = {
			// Id : MessageId,
			// Proposer : playerName,
			// Proposer : '<img height=25px width=25px src="img/me.gif"/>',
			// Receiver : '<img height=25px width=25px src="img/'
			// + game.getPlayerIcon(SenderID) + '"/>',
			// Proposer : SenderID,
			// Receiver : SenderID == 0 ? 1 : 0,
			MsgType : "<div id='divTableMsgType'></div>",
			Sender : "<div id='divTableSender'></div>",
			Receiver : "<div id='divTableReceiver'></div>",
			Message : "<div id='divTableMessage'></div>", 
			Response : "<div id='divButtonPropose'></div>"
		};
	
	jQuery("#tblProposals").jqGrid('addRowData', 0, defaultData);
	
	// add a button for submit
	var cont = document.createElement("div");
	
	var playerIdToSend = game.getMe();
	cont.innerHTML = "<button id='buttonSubmitGoalRevelation' onclick='buttonSubmitGoalRevelation_click(" + playerIdToSend + ");'>Submit</button>";
	document.getElementById('divButtonPropose').appendChild(cont);	
	
	// append div into Messages grid
	document.getElementById("divTableMessage").innerHTML = 'If you wish to reveal your goal to the oponent';
	document.getElementById("divTableMsgType").innerHTML = 'Revelation';
	document.getElementById("divTableSender").innerHTML = "<img src='img/me.gif'/>";
	
	InsertIntoPlayersIconsSelect();
	
}
// end Proposals table


//Send revelation details to the sever
function buttonSubmitGoalRevelation_click(playerId) {
	
	
	
	var ddIcons = document.getElementById('playersIconsDropDown');
	var recipientID = ddIcons.options[ddIcons.selectedIndex].value;
	//playerId - my id
	//ddIcons - the player that I want to reveal my goal to
	isGoalRevelationSubmitted = true;
	sendGoalRevelation(playerId);
	clearProposalTableArea();
	loadProposalsTable();
	// rowID = num of rows in proposals grid
	var rowID = jQuery("#tblProposals").jqGrid('getGridParam', 'records');
	//alert("rowID = "+rowID);
	
	addRecordToTable("GoalRevelation", playerId,recipientID, rowID, "","");
	
	clearMessagesUI();
	$("#"+"0").hide();
	
}
// END Send revelation details to the sever

function sendGoalRevelation(playerIDSend)
{
	var stringJ = "{\"player\" : \"" + playerIDSend + "\" }";

	jQuery.ajax({
		type : "post",
		url : "sendGoalRevelation.jsp",
		data : "json=" + stringJ,
		success : function(msg) {
			// alert(msg);
		}
	});
/*
	
*/
}



// Send revelation details to the sever
function buttonSubmitRevelation_click(playerId) {
	
	var ddIcons = document.getElementById('playersIconsDropDown');			

	sendChipRevelation(playerId, ddIcons.options[ddIcons.selectedIndex].value);
	
	clearMessagesUI();
	$("#"+"0").hide();	
}
// END Send revelation details to the sever

// send Receive proposal area
function proposalArea() {


	
	var rowSelectSend = 0;
	var rowColorSend = 2;
	var rowSelectReceive = 1;
	var rowColorReceive = 3;
	var playerId;
	var tab;

	row = new Array();
	cell = new Array();

	row_num = playerNumTotal;

	// tab = document.createElement('table');
	// tab.setAttribute('id', 'tableSend');

	// tbo = document.createElement('tbody');

	for (c = 0; c < row_num; c++) {
		// populate data only for me (player)
		playerId = game.getPlayerId(c);
		if (game.getisPlayerMe(playerId) == "true") {
			// number of colors for each player
			cell_num = game.players[c].cards.length;

			/* each player has 2 rows of data (send/receive) */
			for (i = 0; i < 2; i++) {
				tab = document.createElement('table');
				tab.setAttribute('id', i == rowSelectReceive ? 'tableReceive' : 'tableSend');
				tbo = document.createElement('tbody');

				row[i] = document.createElement('tr');
				for (k = 1; k < cell_num + 1; k++) {					
						cell[k] = document.createElement('td');
						switch (i) {
						case rowSelectSend:
							cell[k].setAttribute('id',
									'ProporseSendCellRowChips' + (i + (c * 4))
											+ 'ProposeSendColumnChips' + k);
							var cont = document.createElement("select");
							cont.setAttribute('id', 'SelectSendPlayerId'
									+ game.players[c].id + 'Color'
									+ game.players[c].cards[k - 1].color);
							cont.style.background = "#"
								+ game.players[c].cards[k - 1].color;
							break;
						case rowColorSend:
							cell[k].setAttribute('id',
									'ProporseSendCellRowChips' + (i + (c * 4))
											+ 'ProposeSendColumnChips' + k);
							var cont = document.createElement("div");
							cont
									.setAttribute(
											'id',
											'ProporseSendColorCellRowChips'
													+ (i + (c * 4))
													+ 'ProposeSendColorColumnChips'
													+ k);
							cont.innerHTML = "&nbsp;";
							cont.style.background = "#"
									+ game.players[c].cards[k - 1].color;
							break;
						case rowSelectReceive:
							cell[k].setAttribute('id',
									'ProporseReceiveCellRowChips'
											+ (i + (c * 4))
											+ 'ProposeReceiveColumnChips' + k);
							var cont = document.createElement("select");
							cont.setAttribute('id', 'SelectReceivePlayerId'
									+ game.players[c].id + 'Color'
									+ game.players[c].cards[k - 1].color);
							cont.style.background = "#"
								+ game.players[c].cards[k - 1].color;
							break;
						case rowColorReceive:
							cell[k].setAttribute('id',
									'ProporseReceiveCellRowChips'
											+ (i + (c * 4))
											+ 'ProposeReceiveColumnChips' + k);
							var cont = document.createElement("div");
							cont.setAttribute('id',
									'ProporseReceiveColorCellRowChips'
											+ (i + (c * 4))
											+ 'ProposeReceiveColorColumnChips'
											+ k);
							cont.innerHTML = "&nbsp;";
							cont.style.background = "#"
									+ game.players[c].cards[k - 1].color;
							break;
						}
					
					// append to father element
					cell[k].appendChild(cont);
					row[i].appendChild(cell[k]);
				}
				tbo.appendChild(row[i]);
				tab.appendChild(tbo);				
				// document.getElementById('ProposalsArea').appendChild(tab);
				document.getElementById(i == rowSelectReceive ? 'divTableReceive' : 'divTableSend').appendChild(tab);
			}
		}
	}
	

	// add a button for submit
	var cont = document.createElement("div");
	cont.innerHTML = "<div><button id='buttonSubmitProposal' onclick='buttonSubmitProposal_click();'>Submit Proposal</button></div>";

	
	document.getElementById('divButtonPropose').appendChild(cont);
	document.getElementById("divTableSender").innerHTML = "<img src='img/me.gif'/>";

	
	// populate data
	InsertIntoSendSelect();
	InsertIntoReceiveSelect();
	InsertIntoPlayersIconsSelect();

	// show proposal area only for proposer
	if (game.role == 1) { // 1 = Receiver
		clearMessagesUI();		
	}
	else{
		// make proposal area visible
		$("#"+"0").show();
	}
		
}
// END send Receive proposal area

// send proposal button
function buttonSubmitProposal_click() {
	
	for ( var i = 0; i < playerNumTotal; i++) {
		if (game.players[i].isme == 'true') {
			var ddIcons = document.getElementById('playersIconsDropDown');	
			sendProposal(game.getPlayerId(i), ddIcons.options[ddIcons.selectedIndex].value);
		}
	}

	clearMessagesUI();
	$("#"+"0").hide();	
	
}

// INSERT INTO players Icons Drop Down
function InsertIntoPlayersIconsSelect() {
	
	
	var playerId;	
	var playersIconsDropDown = document.createElement('select');
	
	playersIconsDropDown.setAttribute('id', 'playersIconsDropDown');
	playersIconsDropDown.style.width = '70px';
	document.getElementById('divTableReceiver').appendChild(playersIconsDropDown);
	
	for ( var i = 0; i < playerNumTotal; i++) {
		// get player ID
		playerId = game.getPlayerId(i);
		// populate data only for all players except me
		if (game.getisPlayerMe(playerId) != "true") {
				// clear dropdown before inserting
				removeAllOption("playersIconsDropDown");
				// insert player img to dropdown
				appendOptionLast(playerId, playerId, "playersIconsDropDown", 'img/' + game.getPlayerIcon(playerId));							
		}
	}
	
	document.getElementById('divTableReceiver').appendChild(playersIconsDropDown);
	$("#playersIconsDropDown").msDropDown();
}
// END INSERT INTO players Icons Drop Down

// INSERT INTO revelation select
function InsertIntoRevelationSelect() {
	var playerId;
	var color;
	var sumChips;

	for ( var i = 0; i < playerNumTotal; i++) {
		// populate data only for me (player)
		playerId = game.getPlayerId(i);
		if (game.getisPlayerMe(playerId) == "true") {
			for ( var j = 0; j < colorNumTotal; j++) {
				playerId = game.getPlayerId(i);
				color = game.colors[j].name;
				removeAllOption("SelectRevelationPlayerId" + playerId + "Color"
						+ color);
				sumChips = game.getSumChips(playerId, color);
				for ( var k = 0; k <= sumChips; k++) {
					appendOptionLast(k, k, "SelectRevelationPlayerId" + playerId
							+ "Color" + color, '');
				}
			}
		}
	}
}
// END INSERT INTO revelation select

// INSERT INTO Players Chips Send
function InsertIntoSendSelect() {
	var playerId;
	var color;
	var sumChips;
	
	for ( var i = 0; i < playerNumTotal; i++) {
		// populate data only for me (player)
		playerId = game.getPlayerId(i);
		if (game.getisPlayerMe(playerId) == "true") {
			for ( var j = 0; j < colorNumTotal; j++) {
				playerId = game.getPlayerId(i);
				color = game.colors[j].name;
				removeAllOption("SelectSendPlayerId" + playerId + "Color"
						+ color);
				sumChips = game.getSumChips(playerId, color);
				for ( var k = 0; k <= sumChips; k++) {
					appendOptionLast(k, k, "SelectSendPlayerId" + playerId
							+ "Color" + color, '');
				}
			}
		}
	}
}
// END INSERT INTO Players Chips Send

// INSERT INTO Players Chips Receive
function InsertIntoReceiveSelect() {
	var playerId;
	var playerIdOpponent;
	var color;
	var sumChips = 0;

	for ( var i = 0; i < playerNumTotal; i++) {
		// populate data only for me (player)
		playerId = game.getPlayerId(i);
		if (game.getisPlayerMe(playerId) == "true") {
			for ( var j = 0; j < colorNumTotal; j++) {
				playerId = game.getPlayerId(i);
				color = game.colors[j].name;

				removeAllOption("SelectReceivePlayerId" + playerId + "Color"
						+ color);

				/* for each player add the chips of the opponents */
				for ( var pl = 0; pl < playerNumTotal; pl++) {
					playerIdOpponent = game.getPlayerId(pl);
					if (playerId != playerIdOpponent) {
						sumChips += isRevelationEnabled == "true" ? game.getSumRevelationChips(playerIdOpponent, color) : game.getSumChips(playerIdOpponent, color);
					}
				}
				for ( var k = 0; k <= sumChips; k++) {
					appendOptionLast(k, k, "SelectReceivePlayerId" + playerId
							+ "Color" + color, '');
				}
				sumChips = 0;
			}
		}
	}
}
// END INSERT INTO Players Chips Receive

// insert value at end of Select element
function appendOptionLast(text, value, IDselect, title) {
	var elOptNew = document.createElement('option');
	elOptNew.text = text;
	elOptNew.value = value;
	elOptNew.title = title == null ? "" : title;
	var elSel = document.getElementById(IDselect);

	try {
		elSel.add(elOptNew, null); // standards compliant; doesn't work in IE
	} catch (ex) {
		elSel.add(elOptNew); // IE only
	}
}


// remove all data from Select element
function removeAllOption(IDselect) {
	var elSel = document.getElementById(IDselect);
	var i;
	for (i = elSel.length - 1; i >= 0; i--) {

		elSel.remove(i);
	}
}

// Proposals table
function loadProposalsTable() {
	jQuery("#tblProposals").jqGrid(
			{
				datatype : "local",
				height : 200,
				colNames : ['MsgType', 'Sender', 'Receiver', 'Send', 'Receive', 'Response' ],
				colModel : [ {
					name : 'MsgType',
					index : 'MsgType',
					width : 90,
					sortable : false
				}, {
					name : 'Sender',
					index : 'Sender',
					width : 75,
					sortable : false
				},{
					name : 'Receiver',
					index : 'Receiver',
					width : 75,
					sortable : false
				},{
					name : 'Send',
					index : 'Send',
					width : 200,
					sortable : false
				}, {
					name : 'Receive',
					index : 'Receive',
					width : 200,
					sortable : false
				}, {
					name : 'Response',
					index : 'Response',
					width : 150,
					sortable : false
				} ],
				multiselect : false,
				hoverrows : false				
			});

	var defaultData = {
			// Id : MessageId,
			// Proposer : playerName,
			// Proposer : '<img height=25px width=25px src="img/me.gif"/>',
			// Receiver : '<img height=25px width=25px src="img/'
			// + game.getPlayerIcon(SenderID) + '"/>',
			// Proposer : SenderID,
			// Receiver : SenderID == 0 ? 1 : 0,
			MsgType : "<div id='divTableMsgType'></div>",
			Sender : "<div id='divTableSender'></div>",
			Receiver : "<div id='divTableReceiver'></div>",
			Send : "<div id='divTableSend'></div>",
			Receive : "<div id='divTableReceive'></div>", 
			Response : "<div id='divButtonPropose'></div>"
		};
	
	jQuery("#tblProposals").jqGrid('addRowData', 0, defaultData);
}
// end Proposals table

// about window
function LoadAbout() {

	document.getElementById("dialog").innerHTML = '<h1>Color Trails</h1> <br />'
			+ 'Version 2.1 - THE WEB VERSION <br /> '
			+ 'Shay Libo & Hen Barshak <br />'
			+ 'Dr. Kobi Gal <br />'
			+ 'All rights reserved <br />' + 'Color trails <br />';
	document.getElementById("dialog").align = "center";

	$(function() {
		$("#dialog").dialog();
	});
}
// end about window

// this function Receive "01:03" return "63"
function getSeconds(timeForm) {

	var time3 = timeForm.split(":");
	// change second fro 09 to 9
	if (time3[1].charAt(0) == 0)
		time3[1] = time3[1].charAt(1);

	// change minutes from 07 to 7
	if (time3[0].charAt(0) == 0)
		time3[0] = time3[0].charAt(1);

	var seconds = parseInt(time3[0]) * 60 + parseInt(time3[1]);
	seconds = parseInt(seconds);
	return seconds;

}
// end getSeconds

// this function Receives "120" return "2:00"
function secondsToTime(seconds) {
	var min = 0;
	while (seconds >= 60) {
		seconds = parseInt(seconds) - 60;
		min = min + 1;
	}
	if (parseInt(min) < 10)
		min = "0" + min;
	if (parseInt(seconds) < 10)
		seconds = "0" + seconds;

	return min + ":" + seconds;

}

/* Add a msg to grid */
function addRecordToTable(MsgType, SenderID, ReceiverID, msgID, ReceivedChips, SentChips) {
	var ReceivedChips1 = "<div><table><tr>";
	var SentChips1 = "<div><table><tr>";
	var playerName = game.getPlayerName(SenderID);
	// iterate over msg.ReceivedColor
	
	
	if(MsgType != "GoalRevelation")
	{
		for ( var j = 0; j < SentChips.length; j++) {
			SentChips1 = SentChips1 + '<td class="proposalsSendReceiveCol" style="background-color :#'
					+ SentChips[j].color + ';"><b>'+" " + SentChips[j].sum + " "+'</b></td>';
		}
		SentChips1 = SentChips1 + "</tr></table></div>";

	}
	
		
	// only if msg type is Proposal iterate over recieved chips
	if (MsgType == "Proposal") {
		for ( var j = 0; j < ReceivedChips.length; j++) {
			ReceivedChips1 = ReceivedChips1 + '<td class="proposalsSendReceiveCol" style="background-color :#'
					+ ReceivedChips[j].color + ';"><b>'+" " + ReceivedChips[j].sum + " "+'</b></td>';
		}
		ReceivedChips1 = ReceivedChips1 + "</tr></table></div>";
	}

	var AcceptRejectButtonsHTML = '<div id="msgToAcceptReject'
			+ msgID
			+ '"><table><tr><td><input onclick="sendResponseAcceptReject('
			+ msgID
			+ ',1)" type="button" id="acceptResponseButton'
			+ msgID
			+ '" value="Accept"></td><td><input type="button" id="rejectResponseButton'
			+ msgID + '" value="Reject" onclick="sendResponseAcceptReject('
			+ msgID + ',0)"></td><td></td></tr></table></div>';

	var showButtons = game.role == 1 ? AcceptRejectButtonsHTML
			: '<div id="msgToAcceptReject' + msgID + '"></div>';

	var defaultData = {
		MsgType : MsgType,
		Sender : game.getisPlayerMe(SenderID) == "true" ? "<img src='img/me.gif'/>" : "<img src='img/" + game.getPlayerIcon(SenderID) + "'/>",
		Receiver : game.getisPlayerMe(SenderID) == "false" ? "<img src='img/me.gif'/>" : "<img src='img/" + game.getPlayerIcon(ReceiverID) + "'/>",	
		Send : MsgType != "GoalRevelation" ? SentChips1  : "<div></div>",
		Receive : MsgType == "Proposal" ? ReceivedChips1 : "<div></div>", 
		Response : MsgType == "Proposal" ? showButtons : "<div></div>"
	};

	// rowID = num of rows in proposals grid
	var rowID = jQuery("#tblProposals").jqGrid('getGridParam', 'records');
	
		
	// alert('send: ' + SentChips1);
	// alert('Receive: ' + ReceivedChips1);
	
	
	jQuery("#tblProposals").jqGrid('addRowData', rowID, defaultData);
	
	//the popup window for received proposal
	if(game.role == 1)
	{
		var newDiv = $(document.createElement('div')); 
		newDiv.html('Incoming proposal received!');
		newDiv.dialog({ title: "Proposal Received" });
		setTimeout(function(){newDiv.dialog('close');},2500);
	}
	
}

// Change Player Chips after ajax update
function UpdatePlayerChips(o) {
	for ( var i = 0; i < o.chipsChange.length; i++) {
		for ( var j = 0; j < o.chipsChange[i].chips.length; j++) {
			var playerID = o.chipsChange[i].playerID;
			var colorCell = o.chipsChange[i].chips[j].color;				
			var total = o.chipsChange[i].chips[j].sum;
			
			game.setSumChips(playerID, colorCell, total);
			
			if (game.getisPlayerMe(playerID) == "true" || isRevelationEnabled == "false") {
				jQuery("#" + "playerId" + playerID + "Color" + colorCell).html(
						total);								
			}
		}
	}
}
// END change player Chips

function updateGoals(o)
{
		
	removeGoals();
	
	for ( var i = 0; i < o.Goals.length; i++) 
	{
		game.goals[i].id = o.Goals[i].id;
		game.goals[i].type = o.Goals[i].type;
		game.goals[i].posX = o.Goals[i].posX;
		game.goals[i].posY = o.Goals[i].posY;
	}
	
	if(o.Goals.length < game.numOfGolals)
	{
		game.goals.splice(o.Goals.length,game.numOfGolals-o.Goals.length);
	}
	
	game.numOfGolals = o.Goals.length;
	game.numGoals = o.Goals.length;
	
	createGoals();
	
	updatePathFinderGoals();
	
	
	
	
}


function removeGoals(){

    var goalSquare;
    
    for ( var i = 0; i < game.numOfGolals; i++) {
       if(game.goals[i].type >= 10){
               goalSquare = document.getElementById("DivRow" + game.goals[i].posX + "Column" + game.goals[i].posY);
           goalSquare.removeChild(goalSquare.lastChild);
            
       }
    }
    
    //alert("Finished removing goals");
}



// Change Player Revelation Chips after ajax update
function UpdatePlayerRevelationChips(o) {
	
	if (currentPhase == "Revelation Phase") {
		for ( var i = 0; i < o.RevelationChips.length; i++) {
			var playerID = o.RevelationChips[i].playerID;
			var recipient = o.RevelationChips[i].recipient;
			
			for ( var j = 0; j < o.RevelationChips[i].chips.length; j++) {
				
				var colorCell = o.RevelationChips[i].chips[j].color;				
				var total = o.RevelationChips[i].chips[j].sum;
				
				game.setSumRevelationChips(playerID, colorCell, total);
				
				if (game.getisPlayerMe(playerID) == "false" && isRevelationEnabled == "true") {	
					jQuery("#" + "playerId" + playerID + "Color" + colorCell).html(
							total);
				}
			}
			
			if (isRevelationEnabled == "true") {
				var rowID = jQuery("#tblProposals").jqGrid('getGridParam', 'records');
				addRecordToTable("Revelation", playerID, recipient, rowID, "", o.RevelationChips[i].chips);
			}
		}
	}
}
// END Change Player Revelation Chips after ajax update

// Change BorderColors (game pallet) after ajax update
function UpdateBorderColors(o) {
	
	for ( var i = 0; i < o.BorderColors.length; i++) {		
			var color = o.BorderColors[i].color;
			var posX = o.BorderColors[i].posX;				
			var posY = o.BorderColors[i].posY;
			
			game.setColorCell(color, posX, posY);								
	}
	
	// rebuild game bord colors
	addColors();
}
// END Change BorderColors (game pallet) after ajax update

// Change Player POS
function UpdatePlayerPosOnBoard(o) {
	for ( var i = 0; i < o.moveChange.length; i++) {
		var playerID = o.moveChange[i].playerID;
		var xpos = o.moveChange[i].position.y;
		var ypos = o.moveChange[i].position.x;
		// alert(playerID);
		// alert(xpos);
		// alert(ypos);
		MoveElementPosition(playerID, xpos, ypos);
	}
}
// END change player pos

// add data to proposals table
function InsertIntoProposalsTable(o) {

	for ( var i = 0; i < o.msgs.length; i++) {
		// alert("msg: " + o.msgs[i]);
		addRecordToTable("Proposal", o.msgs[i].SenderID, o.msgs[i].ReceiverID, o.msgs[i].MessageId,
				o.msgs[i].SentChips, o.msgs[i].ReceivedChips);
		
		pendingProposalMsgID =  o.msgs[i].MessageId;
	}
}
// END add data to proposals table in Receiver

// update response for proposal at proposer side
function UpdateResponseForProposal(o) {
	// rowID = num of rows in proposals grid
	
		
	var rowID = jQuery("#tblProposals").jqGrid('getGridParam', 'records');
	
	for ( var i = 0; i < o.response.length; i++) {
		// jQuery("#tblProposals").jqGrid('setCell', rowID - 1, 4,
		// o.response[i].response);
		document.getElementById("msgToAcceptReject" + (rowID - 1)).innerHTML = o.response[i].response;
	}
}
// END update response for proposal at proposer side

// move player pos on the game board
function MoveElementPosition(playerId, toX, toY) {
	var cellColor = game.getColorCell(toX, toY);
	// substract num chips
	var numChips = game.getSumChips(playerId, cellColor);
	game.setSumChips(playerId, cellColor, numChips - 1);
	// update div numOf chips
	jQuery("#" + "playerId" + playerId + "Color" + cellColor)
			.html(numChips - 1);
	// move
	var newDiv = document.getElementById("player" + playerId);
	var cell = document.getElementById("DivRow" + toX + "Column" + toY);
	cell.appendChild(newDiv);
};
// end move player pos on the game board



/** ********************** Path Finder *********************** */

// create the path finder UI on the page
function CreatePathFinder() {
	insertIntoPlayerSelect();
	insertIntoGoalSelect();
}

// Calculate player's path
function findPath() {
	var playerId = jQuery("#playerSelect").val();
	var goalId = jQuery("#goalSelect").val();
	var maxSteps = parseInt(jQuery("#numStepsPathFinder").val());

	var x = parseInt(game.getPlayerPositionX(playerId));
	var y = parseInt(game.getPlayerPositionY(playerId));

	var gX = parseInt(game.getGoalPositionX(goalId));
	var gY = parseInt(game.getGoalPositionY(goalId));

	var path = this.factorial2(x, y, gX, gY, "", 0, maxSteps);
	// var path=this.factorial2(2,1,2,3,"",0,4);
	// alert(path);
	var paths = path.split("&");

	// order
	var l1;
	var l2;
	var temp;
	// alert(paths.length);
	for ( var i = 0; i < paths.length; i++)
		for ( var j = 0; j < paths.length; j++) {
			l1 = paths[i].split("|").length;
			l2 = paths[j].split("|").length;

			if ((i != j) && (l1 < l2)) {
				temp = paths[i];
				paths[i] = paths[j];
				paths[j] = temp;

			}

		}

	// remove table from container
	if (document.getElementById('pathFindeTable')) {
		var container1 = document.getElementById("pathFinderContainer");
		var tbl1 = document.getElementById("pathFindeTable");
		container1.removeChild(tbl1);
	}

	// append new tbl
	createTable();
	var numOfMoves;
	var missing;
	var require;
	appendRowIntoTable("", "moves", "requichips", "missing chips");
	for ( var j = 0; j < paths.length; j++) {
		// get num of moves
		numOfMoves = paths[j].split("|").length - 1;
		// get requid chip
		require = this.getRequireChips(paths[j]);
		// get missing chips
		missing = this.getMissingChips(paths[j]);
		if (numOfMoves > 0)
			appendRowIntoTable(paths[j], numOfMoves, require, missing);
	}

};
// end findpath function

// assign goals to players
function getPlayersGoal() {
	// alert();
	removeAllOption("goalSelect");
	// appendOptionLast("Choose Goal",0,"goalSelect");
	var dropdownIndex = document.getElementById('playerSelect').selectedIndex;
	var goalType;
	var playerName = document.getElementById('playerSelect')[dropdownIndex].text;
	var playerId = document.getElementById('playerSelect')[dropdownIndex].value;
	var playerSerial = game.getPlayerSerial(playerId);
	var sumOfAppend = 0;
	//for ( var j = 0; j < game.players[playerSerial].goalsId.length; j++) {
	for ( var j = 0; j < game.goals.length; j++) {
		goalType = game.goals[j].type;
		sumOfAppend++;
		appendOptionLast(goalType, goalType, "goalSelect", '');

	}
	
	
};
// end function

// Fill players into playersSelect DropDownList
function insertIntoPlayerSelect()
{
	
	
	var playerId;
	var playerName;
	removeAllOption("playerSelect");
	playerId = game.getMe();
	playerName = game.getPlayerName(playerId);
	appendOptionLast(playerName, playerId, "playerSelect", 'img/' + game.getPlayerIcon(playerId));
	
	
	for ( var i = 0; i < playerNumTotal; i++) {
		playerId = game.getPlayerId(i);
		if(game.getisPlayerMe(playerId) != "true")
		{
			var found = false;
			for(var j =0 ; j < game.numOfGolals &&!found ; j++)
			{
				if(game.goals[j].type%10 == playerId)
				{
					playerName = game.getPlayerName(playerId);
					appendOptionLast(playerName, playerId, "playerSelect", 'img/' + game.getPlayerIcon(playerId));
					found =true;
				}
			}
			
		}
	}
	
	$("#playerSelect").msDropDown();
	
	
	//gil
	//    var playersIconsDropDown = document.getElementById('playerSelect_title');
	//	
	//	playersIconsDropDown.style.width = '120px';
	//	playersIconsDropDown.style.height = '30px';
	//	
	//gilend
	
}
// End of insertIntoPlayerSelect function

// Fill goals into goalSelect DropDownList
function insertIntoGoalSelect()
{
	
	removeAllOption("goalSelect");
	// appendOptionLast("Choose Goal",0,"goalSelect");
	var dropdownIndex = document.getElementById('playerSelect').selectedIndex;
	var goalType;
	//var playerName = document.getElementById('playerSelect')[dropdownIndex].text;
	var playerId = document.getElementById('playerSelect')[dropdownIndex].value;
	//var playerSerial=game.getPlayerSerial(playerId); 
	var goalNum=0;
	//alert("insertIntoGoalSelect -revelation game.numOfGolals "+game.numOfGolals);
	
	for(var j =0 ; j < game.numOfGolals; j++)
	{
		goalType=game.goals[j].type;
		
		//gil
		//alert(" goalType " +goalType);
		//gilend
		
		if(goalType%10 == playerId)
		{
			goalNum++;
			appendOptionLast(goalNum,goalType,"goalSelect", '');
		}				
	}	
	
	
}
// End of insertIntoGoalSelect function

// Finds all paths from initial location to goal according to max number of
// steps entered
function factorial2(x,y,gX,gY,path,steps,maxSteps)
{
	if ((x==gX)&&(y==gY))
	{
		
		var duplicate=this.hasDuplicate(path);
		if (duplicate==false)
			return path+x+","+y+"&";
		if (duplicate==true)
			return "";
	
	}
    if (steps >= maxSteps)
    {
        // return path+"<br>";
		return "";
    }
	else if ((x<0)||(y<0)||(x>=numOfRows)||(y>=numOfColumns))
	{
		return "";
	}
    else
	{
	return this.factorial2(x+1,y,gX,gY,path+""+x+','+y+"|",steps+1,maxSteps)+this.factorial2(x,y+1,gX,gY,path+""+x+','+y+"|",steps+1,maxSteps)+this.factorial2(x-1,y,gX,gY,path+""+x+','+y+"|",steps+1,maxSteps)+this.factorial2(x,y-1,gX,gY,path+""+x+','+y+"|",steps+1,maxSteps);
	}
}
// End of function

// check if a path already exists
function hasDuplicate(str)
{
	// var str="0,0|1,0|2,0|0,0|1,0|2,0|0,0|1,0|0,0|1,0|0,0";
	// check for duplicate if has not send
		var hasD=false;
		var px1; 
		var py1; 
		var co1;
		var px2;
		var py2;
		var co2;
		var ar=str.split("|");
		for (var i=0;i<ar.length-1;i++)
		{
			co1=ar[i].split(",");
			px1=co1[0];
			py1=co1[1];
			for (var j=0;j<ar.length-1;j++)
				{	
					co2=ar[j].split(",");
					px2=co2[0];
					py2=co2[1];
					/*
					 * document.write("i="+i+" "+"j="+j+"<br>");
					 * document.write("x="+px2+" "+"x="+px1+"<br>");
					 * document.write("y="+py2+" "+"y="+py1+"<br>");
					 * document.write("<br>");
					 */
					if ((px1==px2)&&(py1==py2)&&(i!=j))
					{
						return true;
					}
				}
		}
		return hasD;
}
// End of function

// creates a table to contain and present the paths to the goal
function createTable()
{
	var container=document.getElementById("pathFinderContainer");
    var tbl     = document.createElement("table");
	 tbl.id="pathFindeTable";
	 tbl.setAttribute("border", "2");
	 container.appendChild(tbl);
}
// end of function

// Append row with a path to the table
function appendRowIntoTable(path,moves,missing,required)
{
		
  // creates a <table> element and a <tbody> element
  var tbl     = document.getElementById("pathFindeTable");
  var tblBody = document.createElement("tbody");
  var row = document.createElement("tr");
  // row.setAttribute('id',path);
	
	// show path on board
	if (moves!="moves")
	{
		// on Mouse Over Event
		row.onmouseover = function ()
		{ 
			var paths=path.split("|");
			var p;
			var x;
			var y;
			for (var k=0;k<paths.length;k++)
			{
				// current div
				p=paths[k].split(",");
				x=p[0];
				y=p[1];
				var div3=document.getElementById("DivRow" + x+"Column"+y);
				div3.className =  "cssDDContainer2";
				// specify the arrow class
						// privious div
						var xPriv;
						var yPriv;
						var xPrivPriv;
						var yPrivPriv;
						if (k>0)
						{
							pPriv=paths[k-1].split(",");
							xPriv=pPriv[0];
							yPriv=pPriv[1];
						}
						if(k>2)
						{ 
							pPrivPriv=paths[k-2].split(",");
							xPrivPriv=pPrivPriv[0];
							yPrivPriv=pPrivPriv[1];
						}
						if ((xPriv==x)&&(y>yPriv))
						{
								var div3=document.getElementById("DivRow" + xPriv+"Column"+yPriv);
								div3.className =  "rigth";
						}
						else if((xPriv==x)&&(y<yPriv))
						{
							var div3=document.getElementById("DivRow" + xPriv+"Column"+yPriv);
								div3.className =  "left";
						}
						else if((xPriv>x)&&(y==yPriv))
						{
						
							var div3=document.getElementById("DivRow" + xPriv+"Column"+yPriv);
								div3.className =  "up";
						}
						else if((xPriv<x)&&(y==yPriv))
						{
							// alert("x="+x+"y="+y+"xpriv="+xPriv+"ypriv="+yPriv+"xprivpriv="+xPrivPriv+"yprivpriv="+yPrivPriv);
							var div3=document.getElementById("DivRow" + xPriv+"Column"+yPriv);
								div3.className =  "down";
						}
						else
						{
						var div3=document.getElementById("DivRow" + x+"Column"+y);
						div3.className =  "cssDDContainer2";
						}
						// alert("k="+parseInt(k+1)+"pa="+paths.length);
				// end
			}
		};
		
		// on ouse Out event
		row.onmouseout = function ()
		{ 
			var paths=path.split("|");
			var p;
			var x;
			var y;
			for (var k=0;k<paths.length;k++)
			{
				p=paths[k].split(",");
				x=p[0];
				y=p[1];
				var div3=document.getElementById("DivRow" + x+"Column"+y);
				div3.className =  "cssDDContainer";
			}
		};
	}
	// end show path on board
	
	
	// column 1
  var cell = document.createElement("td");
	var cellText = document.createTextNode(moves);
  cell.appendChild(cellText);
  row.appendChild(cell);
	
	// column 2
  var cell = document.createElement("td");
	cell.innerHTML=missing;
	// var cellText = document.createTextNode(missing);
  // cell.appendChild(cellText);
  row.appendChild(cell);
	
	// column 3
  var cell = document.createElement("td");
	cell.innerHTML=required;
  row.appendChild(cell);
	
  tblBody.appendChild(row);
  tbl.appendChild(tblBody);
	
 
}
// End of appendRowIntoTable function

// Gets the required chips needed for path
function getRequireChips(path)
{

	var px;
	var py;
	var pos=path.split("|");
	var cor;
	var col;
	var arr=new Array(game.numOfColors);
	// init arra
	for (var k=0;k<game.numOfColors;k++)
	{
		arr[k]=0;
	}
	// start from "1" because the start position
	for(var j=1;j<pos.length;j++)
	{
		cor=pos[j].split(",");
		px=cor[0];
		py=cor[1];
		if ((px!="")&&(py!=""))
		{
			col=game.getColorCell(px,py);
			for (var i=0;i<game.numOfColors;i++)
						if (game.colors[i].name==col)
							{
								arr[i]=arr[i]+1;
							}
		}
	}

	// return the str with color like <table><tr><td style="background-color
	// :#FFF62D;">1</td><td style="background-color
	// :#BF7FFF;">3</td></tr></table>
	var req;
	var str="<table><tr>";
	for (var i=0;i<game.numOfColors;i++)
							{
								if ((game.colors[i].name=="#BFFF7F")||(game.colors[i].name=="#FF7F7F"))
									req="<font style='color:black'>"+arr[i]+"</font>";
								else
									req=arr[i];
								str=str+'<td style="background-color :#'+game.colors[i].name+';">'+req+'</td>';
							}
		str=str+"</tr></table>";
		return str;
}
// End of function - return the require chips

// Calculates the missing chips the player needs for a path to the goal
function getMissingChips(path)
{

	var px;
	var py;
	var pos=path.split("|");
	var cor;
	var col;
	var arr=new Array(game.numOfColors);
	
	// init arra
	for (var k=0;k<game.numOfColors;k++)
	{
		arr[k]=0;
	}
	// start from "1" because the start position
	for(var j=1;j<pos.length;j++)
	{
		cor=pos[j].split(",");
		px=cor[0];
		py=cor[1];
		if ((px!="")&&(py!=""))
		{
			col=game.getColorCell(px,py);
			for (var i=0;i<game.numOfColors;i++)
						if (game.colors[i].name==col)
							{
								arr[i]=arr[i]+1;
							}
		}
	}
	
	var dropdownIndex = document.getElementById('playerSelect').selectedIndex;
	var colorName;
	var colorQuan;
	var playerId = document.getElementById('playerSelect')[dropdownIndex].value;
	var playerSerial=game.getPlayerSerial(playerId); 
	// substract
	for(var i=0;i<game.numOfColors;i++)
	{
		colorName=game.players[playerSerial].cards[i].color;
		colorQuan=game.players[playerSerial].cards[i].sum;
		for (var j=0;j<game.numOfColors;j++)
						if (game.colors[j].name==colorName)
							{
								arr[j]=arr[j]-parseInt(colorQuan);
						}
		
	}	
	
	
	for(var i=0;i<game.numOfColors;i++)
	{
		for (var i=0;i<game.numOfColors;i++)
						if (arr[i]<0)
							{
								arr[i]=0;
							}
		
	}	
	
	// return the str with color like <table><tr><td style="background-color
	// :#FFF62D;">1</td><td style="background-color
	// :#BF7FFF;">3</td></tr></table>
	var req;
	var str="<table><tr>";
	for (var i=0;i<game.numOfColors;i++)
							{
								if ((game.colors[i].name=="#BFFF7F")||(game.colors[i].name=="#FF7F7F"))
									req="<font style='color:black'>"+arr[i]+"</font>";
								else
									req=arr[i];
								str=str+'<td style="background-color :#'+game.colors[i].name+';">'+req+'</td>';
							}
		str=str+"</tr></table>";
		return str;
}
// End getMissingChips function


//updates the goals of the pathfinder after number of goals changes
function updatePathFinderGoals()
{
	insertIntoGoalSelect();
	
	// remove table from container
	if (document.getElementById('pathFindeTable')) {
		var container1 = document.getElementById("pathFinderContainer");
		var tbl1 = document.getElementById("pathFindeTable");
		container1.removeChild(tbl1);
	}

}


/** ********************** END of Path Finder *********************** */



/** ********************** AJAX *********************** */

// send data to server and update game window with changes
function UpdateServer() {
	
	
	jQuery.post('getupdate.jsp', function(data) {
		var o = jQuery.parseJSON(data);		
		
		// Update game isEnded
		game.setIsEnded(o.isEnded);
		// Update player role
		game.setRole(o.role);
		
		//gil
		// Update goal revelation
		game.setIsGoalRevelationAllowed(o.isGoalRevelationAllowed);
		//gilend
		
		//update current phase
		if (currentPhase != o.CurrentPhase) {
			phaseChanged = true;
			
			// Update Current Phase Time
			pValue = o.PhaseSecsLeft;
			// Update Current phase name
			currentPhase = o.CurrentPhase;
			if(game.numOfGolals!= o.numOfGoals)
			{
				//alert("Num of Goals Changed! = "+  o.numOfGoals);
				updateGoals(o);
			}
			
		} else {
			phaseChanged = false;
		}

	
		
	
		
		InsertIntoProposalsTable(o);
		UpdateResponseForProposal(o);
		UpdatePlayerPosOnBoard(o);
		UpdatePlayerChips(o);
		UpdatePlayerRevelationChips(o);
		UpdateBorderColors(o);
	});
}

// this function send proposal
function sendProposal(playerIDSend, playerIDReceive) {

	var SentChips = new Array();
	var sendRequest = new Array();
	var ReceivedChips = new Array();

	jQuery("select[id^=SelectSendPlayerId" + playerIDSend + "]").each(
			function() {
				idO = this.id;
				var arr = idO.split("Color");
				var color = arr[1];
				var ddlist = document.getElementById(this.id);
				var sum = ddlist.options[ddlist.selectedIndex].value;
	
				var json = "{ \"sum\" : " + sum + ", \"color\" : \"" + color 
					+ "\" ,\"playConId\" : \"" + playerIDSend
					+ "\", \"req\" :  \"Send\" }";
				
				var o = jQuery.parseJSON(json);
				// put json into sendRequest
				
				SentChips.push(o);
				sendRequest.push(o);

			});
	
	jQuery("select[id^=SelectReceivePlayerId" + playerIDSend + "]").each(
			function() {
				idO = this.id;
				var arr = idO.split("Color");
				var color = arr[1];
				var ddlist = document.getElementById(this.id);
				var sum = ddlist.options[ddlist.selectedIndex].value;
				// prepare json
				var json = "{ \"sum\" : " + sum + ", \"color\" : \"" + color
						+ "\", \"playConId\" : \"" + playerIDSend
						+ "\", \"req\" : \"receive\" }";
				var o = jQuery.parseJSON(json);
				// put json into sendRequest
				
								
				sendRequest.push(o);
				ReceivedChips.push(o);

			});


	var stringJ = "{\"player\" : \"" + playerIDSend + "\" , \"recipient\" : \"" + playerIDReceive + "\" , \"chips\" : [";
	for ( var i = 0; i < sendRequest.length; i++) {
		stringJ = stringJ + "{ \"sum\" : " + sendRequest[i].sum
				+ ", \"color\" : \"" + sendRequest[i].color
				+ "\", \"req\" : \"" + sendRequest[i].req + "\" }";
		// alert(sendRequest[i].req);
		if (i != sendRequest.length - 1) {
			stringJ = stringJ + ",";
		}
	}
	stringJ = stringJ + "]}";

	jQuery.ajax({
		type : "post",
		url : "sendmessage.jsp",
		data : "number=" + stringJ,
		success : function(msg) {
			// alert(msg);
		}
	});

		
	if(typeof console === "undefined") {
	    console = { log: function() { } };
	}
	else
	{
		console.log(game);
	}
	
	
	// rowID = num of rows in proposals grid
	var rowID = jQuery("#tblProposals").jqGrid('getGridParam', 'records');
	addRecordToTable("Proposal", playerIDSend, playerIDReceive, rowID, ReceivedChips, SentChips);
	
}
// end send proposal

// send response to proposal
// 1 accept
// 0 reject
function sendResponseAcceptReject(msgID, accept)
{
	
	//alert("sendResponseAcceptReject msgID : " +  msgID);
	
	var stringJ = "{msgID:\"" + msgID + "\" ,accept:\"" + accept + "\"}";
	// alert(stringJ);
	jQuery.ajax({
		type : "post",
		url : "responseMessage.jsp",
		data : "response=" + stringJ,
		success : function(msg) {
			// alert(msg);
		}
	});

	if (accept == "0")
		document.getElementById("msgToAcceptReject" + msgID).innerHTML = "Reject";
	if (accept == "1")
		document.getElementById("msgToAcceptReject" + msgID).innerHTML = "Accept";
	
	//the player responded to the proposal
	pendingProposalMsgID = -1;
	
}
// end send response to proposal


// send login information to server
function login(pin) {	
	// alert(stringJ);
	jQuery.post('login.jsp?pin=' + pin, function(data) {
		// alert(jQuery.trim(data));
		if (jQuery.trim(data) == "true") {
			window.location = 'game.jsp';
		}
	});
}
// END send login information to server

// send revelation in ajax to server
function sendChipRevelation(playerIDSend, recipientID) {
	var sendRevelation = new Array();
	
	jQuery("select[id^=SelectRevelationPlayerId" + playerIDSend + "]").each(
			function() {
				idO = this.id;
				var arr = idO.split("Color");
				var color = arr[1];
				var ddlist = document.getElementById(this.id);
				var sum = ddlist.options[ddlist.selectedIndex].value;
				// prepare json
				var json = "{ \"sum\" : \" " + sum + " \", \"color\" : \""
						+ color + "\" ,\"playConId\" : \"" + playerIDSend
						+ "\", \"req\" :  \"Revelation\" }";
				// alert(json);
				var o = jQuery.parseJSON(json);
				
				// put json into sendRequest
				sendRevelation.push(o);

			});
	
	var stringJ = "{\"player\" : \"" + playerIDSend + "\" , \"recipient\" : \"" + recipientID + "\" , \"chips\" : [";
	for ( var i = 0; i < sendRevelation.length; i++) {
		stringJ = stringJ + "{ \"sum\" : " + sendRevelation[i].sum
				+ ", \"color\" : \"" + sendRevelation[i].color
				+ "\", \"req\" : \"" + sendRevelation[i].req + "\" }";
		// alert(sendRequest[i].req);
		if (i != sendRevelation.length - 1) {
			stringJ = stringJ + ",";
		}
	}
	stringJ = stringJ + "]}";

	jQuery.ajax({
		type : "post",
		url : "sendRevelation.jsp",
		data : "json=" + stringJ,
		success : function(msg) {
			// alert(msg);
		}
	});

	// rowID = num of rows in proposals grid
	var rowID = jQuery("#tblProposals").jqGrid('getGridParam', 'records');
	addRecordToTable("ChipRevelation", playerIDSend, recipientID, rowID, "",
			sendRevelation);

}
// END send revelation in ajax to server
