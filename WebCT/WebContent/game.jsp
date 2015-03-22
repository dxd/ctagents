<%@page import="webapp.WebCTConfiguration"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.Phases"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set" %>
<%@page import="java.util.HashSet" %>
<%@page import="java.util.Iterator" %>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.GamePhase"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page
	import="edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus"%>
<%@page import="webapp.listen"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.Board"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.Goal"%>
<%@page import="java.util.ArrayList"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.Square"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.client.*"%>
<%@page import="webapp.ColorConverter"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet"%>
<%@page import="webapp.ClinetControler"%>
<%@page import="webapp.GeneralFunction"%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Color Trails - Web Application</title>

<link rel="stylesheet" type="text/css"
	href="css/pepper-grinder/jquery-ui-1.8.9.custom.css" />
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css" />
<link rel="stylesheet" type="text/css" href="css/StyleSheet.css" />
<link rel="stylesheet" type="text/css" href="css/ui.notify.css" />
<link rel="stylesheet" type="text/css" href="css/dd.css" />

<script type="text/javascript" src="js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.9.custom.min.js"></script>
<script type="text/javascript" src="js/grid.locale-en.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/jquery.notify.js"></script>
<script type="text/javascript" src="js/jquery.dd.js"></script>
<script type="text/javascript" src="js/jScript.js"></script>

<script type="text/javascript">
//json
   <%
	String id =  session.getAttribute("id").toString();    
   	ClientGameStatus gs = ClinetControler.GetClinet(id).GameStat;
   
   	
	Board br = gs.getBoard();
	ArrayList<ClientPlayerStatus> players =  gs.getClientPlayers();	
	
	//gil
	System.out.println("game.jsp - first part");
	System.out.println("br.getGoals().size() = "+br.getGoals().size());
	System.out.println("br.getGoalLocations().size() = "+br.getGoalLocations().size());
	//gilend
	
   %>
var game = { 
				//AFTER CHANGE VALUES CONVERT TO INTEGER WITH PARSE INT!!
				//"numOfGolals":<--%out.print( br.getGoals().size());%>,
				//"numGoals":<--%out.print( br.getGoals().size());%>,
				
				
					"rows":<%out.print(br.getRows());%>,
					"cols":<%out.print(br.getCols());%>,
					"numOfGolals":<%out.print( br.getGoals().size());%>,
					"numOfPlayer":<%out.print(players.size());%>,
					"numOfPhases":<%out.print(gs.getPhases().getNumPhases());%>,
					"numGoals":<%out.print(  br.getGoals().size());%>,
					"isEnded":<%out.print(gs.isEnded() == true ? "true" : "false");%>,
					"isRevelationEnabled":<%out.print(WebCTConfiguration.getIsChipRevelationGame() == true ? "\"true\"" : "\"false\"");%>,
					"isGoalRevelationAllowed":<%out.print(gs.getMyPlayer().isRevelationAllowed() == true ? "true" : "false");%>,
					"showPathFinder":<%out.print(WebCTConfiguration.getShowPathFinder() == true ? "\"true\"" : "\"false\"");%>,
					"numOfColors":<% out.print(gs.getBoard().getColors().size()); %>, 
					"role":<%out.print( gs.getMyPlayer().getRole().equals("Responder") ? 1 : 0 );
					System.out.print( gs.getPlayerByPerGameId(gs.getPerGameId()).getRole() );%>,
					"players" : 
					[  

					 <%
						int playerID  = gs.getPerGameId();
						for(int i =0;i< players.size();i++)
						{
							ClientPlayerStatus player = players.get(i);
							out.print(	"{\"id\" : "+player.getPerGameId()+",\n");
							out.print(	"\"name\" : \"Player "+player.getPin()+"\",\n");//TO DO  need to deside if to add this filed
							out.print(	"\"age\" : 23,\n");//TO DO  need to deside if to add this filed	
							
							if(playerID == player.getPerGameId())
							{
								out.print(	"\"isme\" : \"true\",\n");
								out.print(	"\"icon\" : \"me.gif\",\n");
								out.print(	"\"bigIcon\" : \"meBig.gif\",\n");
							}
							else
							{
								if(player.getPin()<100)
								{
									out.print(	"\"isme\" : \"false\",\n");
									//out.print(	"\"icon\" : \"comp-icon"+(player.getPerGameId()+1)+".gif\",\n"); //for multiple computer players
									out.print(	"\"icon\" : \"computerRGBBlue.gif\",\n");//currently supports only this icon for computer players
									
								}
								else
								{
									out.print(	"\"isme\" : \"false\",\n");
									out.print(	"\"icon\" : \"them"+(player.getPerGameId()+1)+".gif\",\n");
								}
							}
							//Get user possition
							out.print(	"\"posX\" : "+player.getPosition().row+",\n");
							out.print(	"\"posY\" : "+player.getPosition().col+",\n");
							
							//getUser Chips
							ChipSet cs = player.getChips();
							out.print(	"\"cards\" : [\n");
							Object[] colors = br.getColors().toArray();
							for(int j =0;j< br.getColors().size();j++)
							{
								out.print(	"{\"color\" : \""+ColorConverter.getClientColor(colors[j].toString())+"\" ,\"sum\":"+cs.getNumChips(colors[j].toString())+"}\n");
								if(j != colors.length-1)
								{
									out.println(",");
								}
							}
							out.print("],\n");
							
							//gil
							System.out.println(" game.jsp - //getUser revelation Chips");
							//gilend
							
							
							//getUser revelation Chips
							ChipSet revelationChips = player.getRevelationChips();							
							out.print(	"\"RevelationChips\" : [\n");				
							colors = br.getColors().toArray();
							for(int j =0;j< br.getColors().size();j++)
							{
								out.print(	"{\"color\" : \""+ColorConverter.getClientColor(colors[j].toString())+"\" ,\"sum\":"+revelationChips.getNumChips(colors[j].toString())+"}\n");
								if(j != colors.length-1)
								{
									out.println(",");
								}
							}
							out.print("],\n");
	
							//gil
							System.out.println(" game.jsp - //get user goals");
							//gilend
							
						
							//get user goals  - for the pathfinder							
/* 							out.print(	"\"goalsId\" : [\n");
							ArrayList<RowCol> goals =  br.getGoalLocations();
							for(int j=0;j<goals.size();j++)
							{
								RowCol cur = goals.get(j);
								out.println("{  \"id\":"+j+"}");
								if(j != goals.size()-1)
								{
									out.println(",");
								}
							}
							out.print("]\n");
							
							out.print("}");
							if(i != players.size()-1)
							{
								out.println(",");
							}
							 
							
							//gil - player goals
							out.print(	"\"goalsId\" : [\n");
 
							Set<Goal> goalsSet = new HashSet<Goal>();
							goalsSet = br.getGoals();
							Iterator<Goal> it = goalsSet.iterator();
													
							while(it.hasNext())
							{
								Goal g = it.next();
								
								RowCol cur = g.getLocation();
								out.println("{  \"type\":"+g.getType()+"}");
									
								//gil
								System.out.println("{  \"type\":"+g.getType()+"}");
								//gilend
								
								if(it.hasNext())
								{
									out.println(",");
								}
							}
							
							out.print("]\n");
							
							*/
							out.print("}");
							if(i != players.size()-1)
							{
								out.println(",");
							}
							//gilend	
							
						}

						%>
						
					], 
					"goals": 
					[ 
						<%
						
						/*
						ArrayList<RowCol> goals =  br.getGoalLocations();
						for(int i=0;i<goals.size();i++)
						{
							RowCol cur = goals.get(i);
							out.println("{  \"id\":"+i+",\"name\" : \"gola"+i+"\", \"posX\"  : "+cur.row+", \"posY\": "+cur.col+"}");
							if(i != goals.size()-1)
							{
								out.println(",");
							}
						}
						
						*/
						
						//gil - player goals
						Set<Goal> goalsSet = new HashSet<Goal>();
						goalsSet = br.getGoals();
						Iterator<Goal> it = goalsSet.iterator();
						
						
						
						while(it.hasNext())
						{
							Goal g = it.next();
							
							RowCol cur = g.getLocation();
														
							out.println("{   \"id\" : \" " + g.getID() + " \" ,\"type\" : " + g.getType() +",\"posX\" : " + cur.row + ", \"posY\" : " + cur.col + "}");
							
							
							if(it.hasNext())
							{
								out.println(",");
							}
							
							
							System.out.println("P.ID: " +id+ "G : "   +g.toString());
						}
						
						//gilend			
						
						
						
						%>
						
					  
					],
					"borderColors": 
					[  
						<% 
						
						System.out.println("br.getRows()"+br.getRows());
						System.out.println("br.getCols()"+br.getCols());
						
						for(int i=0;i<br.getRows();i++)
						{
							for(int j=0;j <br.getCols();j++)
							{
								Square s = br.getSquare(i,j);
								
								System.out.println("Inside for"+"i= "+i+", j= "+j);
										
								System.out.println("s.hasGoal() = "+ s.hasGoal());		
										
								System.out.println("s.getColor() = "+s.getColor());
										
								System.out.println("{ \"color\" : \""+ColorConverter.getClientColor(s.getColor())+"\", \"posX\"  : "+i+",  \"posY\"  : "+j+"}" );
								
										
								out.print(	"{ \"color\" : \""+ColorConverter.getClientColor(s.getColor())+"\", \"posX\"  : "+i+",  \"posY\"  : "+j+"}" );
								if((i == br.getRows()-1)&&(j == br.getCols()-1))
								{
									
								}
								else
								{
									out.println(",");
								}
							}
						}
						%>										
					]
					,
					"colors": 
					[ 
						 <%
							Object[] colors = br.getColors().toArray();
							for(int i =0;i< br.getColors().size();i++)
							{
								out.print(	"{\"name\" : \""+ColorConverter.getClientColor(colors[i].toString())+"\"}");
								if(i != colors.length-1)
								{
									out.println(",");
								}
							}
						
						
							%>
							 
					],			
					//phases
					"phases":
						[						 
						 <%						 
						 int countPhases = 0;
						 int numOfPhases = gs.getPhases().getPhaseSequence().size();
						 for (GamePhase gp : gs.getPhases().getPhaseSequence())	{
							 countPhases++;
							 out.print("{\"name\" : \"" + gp.getName() + "\", \"duration\" : \"" + gp.getDuration() + "\"}");
							 if (countPhases != numOfPhases) {
								 out.print(",");
							 }
						 }							  						  
						 %>
						],
						
						//phases keypair <phase name, phase index>
						"phasesIndex":
							{						 
							 <%						 
							 int countPhaseIndex = 0;
							 for (GamePhase gp : gs.getPhases().getPhaseSequence())	{
								 
								 out.print("\"" +gp.getName()+ "\" : " + countPhaseIndex);
								 countPhaseIndex++;
								 if (countPhaseIndex != numOfPhases) {
									 out.print(",");
								 }
							 }							  						  
							 %>
							},
						
						
		//set cards to player  
		setSumChips : function(playerID, color, sum) {
			for ( var i = 0; i < playerNumTotal; i++)
				if (game.players[i].id == playerID) {
					for ( var j = 0; j < game.players[i].cards.length; j++)
						if (game.players[i].cards[j].color == color)
							game.players[i].cards[j].sum = sum;
				}
		},
		//set Revelation Chips to player  
		setSumRevelationChips : function(playerID, color, sum) {
			for ( var i = 0; i < playerNumTotal; i++)
				if (game.players[i].id == playerID) {
					for ( var j = 0; j < game.players[i].RevelationChips.length; j++)
						if (game.players[i].RevelationChips[j].color == color)
							game.players[i].RevelationChips[j].sum = sum;
				}
		},
		//getPlayerId 
		getPlayerId : function(serial) {
			return game.players[serial].id;
		},
		getNumOfPlayer : function() {
			return game.numOfPlayer;
		},
		//get cards
		getSumChips : function(playerID, color) {
			for ( var i = 0; i < playerNumTotal; i++)
				if (game.players[i].id == playerID) {
					for ( var j = 0; j < game.players[i].cards.length; j++)
						if (game.players[i].cards[j].color == color)
							return game.players[i].cards[j].sum;
				}
		},
		//get cards
		getSumRevelationChips : function(playerID, color) {
			for ( var i = 0; i < playerNumTotal; i++)
				if (game.players[i].id == playerID) {
					for ( var j = 0; j < game.players[i].RevelationChips.length; j++)
						if (game.players[i].RevelationChips[j].color == color)
							return game.players[i].RevelationChips[j].sum;
				}
		},
		getGoalPositionX : function(goalType) {
			for ( var i = 0; i < this.numGoals; i++)
				if (game.goals[i].type == goalType) {
					return game.goals[i].posX;
				}
		},
		getGoalPositionY : function(goalType) {
			for ( var i = 0; i < this.numGoals; i++)
				if (game.goals[i].type == goalType) {
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
		getisPlayerMe : function(playerID) {
			for ( var i = 0; i < playerNumTotal; i++)
				if (game.players[i].id == playerID) {
					return game.players[i].isme;
				}
		},
		getMe : function() {
			for ( var i = 0; i < playerNumTotal; i++)
				if (game.players[i].isme == "true") {
					return game.players[i].id;
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
		getBigPlayerIcon : function(playerID) {
			for ( var i = 0; i < playerNumTotal; i++)
				if (game.players[i].id == playerID) {
					return game.players[i].bigIcon;
				}
		},
		getNumOfColors : function() {
			return game.numOfColors;
		},
		setIsEnded : function(isEnded) {
			game.isEnded = isEnded;
		},
		setRole : function(role) {
			game.role = role;
		},
		setIsGoalRevelationAllowed : function(isGoalRevelationAllowed) {
			game.isGoalRevelationAllowed = isGoalRevelationAllowed;
			
		},
		
		getIsGoalRevelationAllowed : function() {
			return game.isGoalRevelationAllowed;
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
		},
		setColorCell : function(color, posX, posY) {
			for ( var i = 0; i < this.borderColors.length; i++)
				if ((this.borderColors[i].posX == posX)
						&& (this.borderColors[i].posY == posY)) {
					this.borderColors[i].color = color;
				}
		}
	};
	//end
</script>

</head>
<body>
<div id="container">
<div id="header"></div>
<div id="navigation">
<ul>

</ul>
</div>
<div id="content-container">
<div id="section-navigation">
<table border="1">
	<tr>
		<td>
		<div id="TimeLeft"></div>
		<div id="progressbar"><script>
	$("#progressbar").progressbar({
		value : 100
	});
	Init(game);
	setInterval("updateProgressBar()", 1000);
</script></div>
		</td>
	</tr>
	<tr>
		<td>
		<div id="divPhases"></div>		
		</td>
	</tr>
	<tr>
		<td>
		<table id="PlayerChips">
		</table>
		</td>
	</tr>
	<tr>
		<td>
		<div id="divPathFinder" style="visibility: hidden;">
		<table>
			<tr>
				<td >Player:<div> <select id="playerSelect"
					onchange="insertIntoGoalSelect()"
					style="width: 120px ; margin:0px 10px 5px 0px">
					<option value=0>choose player</option>
				</select> </div> </td>
			</tr>
			<tr>
				<td>Goal: <select id="goalSelect"
					style="width: 60px; margin: 0px 10px 5px 0px;">
					<option value=0>select</option>
				</select></td>
			</tr>
			<tr>
				<td>Steps: <select id="numStepsPathFinder" style="width: 40px">
					<option value=1>1</option>
					<option value=2>2</option>
					<option value=3>3</option>
					<option value=4>4</option>
					<option value=5>5</option>
					<option value=6>6</option>
					<option value=7>7</option>
					<option value=8>8</option>
					<option value=9>9</option>
					<option value=10>10</option>
				</select></td>
			</tr>
			<tr>
				<td>
				<button onclick="findPath()">Path Finder</button>
				</td>
			</tr>
		</table>
		</div>
		</td>
	</tr>
</table>
<div id="pathFinderContainer"></div>
<!--end proposal area--> <!-- Source point for window animations. -->
<div id="divSource" class="cssSource"></div>
</div>


<div id="content">
<div id="GameBoard">
<table id="GameBoardTable">
	<tr>
		<td></td>
	</tr>
</table>
</div>
<div id="proposals">
<table id="tblProposals"></table>
</div>
</div>
<div id="aside">
	<div id="notifyContainer"></div>
</div>
</div>
</div>

<div id="dialog" title="About WebCT"></div>



<script type="text/javascript">
	createGameBoard();
	addColors();
	createGoals();
	PlayerChips();
	
	
	//gil
	loadProposalsTable();
	//loadRevelationProposalsTable();
	//gilend
	//proposalArea();
	if (game.showPathFinder == "true") {
		document.getElementById("divPathFinder").style.visibility='visible';
		CreatePathFinder();
	}
	
	//gil
	//setInterval("UpdateServer()", 5000);
	//gilend
</script>

</body>
</html>