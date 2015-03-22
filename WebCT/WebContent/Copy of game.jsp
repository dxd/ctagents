<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page
	import="edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus"%>
<%@page import="webapp.listen"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.Board"%>
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
<script type="text/javascript" src="js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.9.custom.min.js"></script>
<script type="text/javascript" src="js/grid.locale-en.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/jScript.js"></script>

<script type="text/javascript">
//json
   <%
	String id =  session.getAttribute("id").toString();    
   	ClientGameStatus gs = ClinetControler.GetClinet(id).GameStat;
	Board br = gs.getBoard();
	ArrayList<ClientPlayerStatus> players =  gs.getClientPlayers();
   %>
var game = { 
				//AFTER CHANGE VALUES CONVERT TO INTEGER WITH PARSE INT!!
					"rows":<%out.print(br.getRows());%>,
					"cols":<%out.print(br.getCols());%>,
					"numOfGolals":<%out.print( br.getGoals().size());%>,
					"numOfPlayer":<%out.print(players.size());%>,
					"numGoals":<%out.print( br.getGoals().size());%>,
					"isEnded":<%out.print(gs.isEnded() == true ? "true" : "false");%>,
					"numOfColors":4, 
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
							}
							else
							{
								out.print(	"\"isme\" : \"false\",\n");
								out.print(	"\"icon\" : \"them"+(player.getPerGameId()+1)+".gif\",\n");
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
							
							//get user goals
							
							out.print(	"\"goalsId\" : [\n");
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
						}
					
					
						%>
						
					], 
					"goals": 
					[ 
						<%
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
						%>
						
					  
					],
					"borderColors": 
					[  
						<% 
						for(int i=0;i<br.getRows();i++)
						{
							for(int j=0;j <br.getCols();j++)
							{
								Square s = br.getSquare(j,i);
								out.print(	"{ \"color\" : \""+ColorConverter.getClientColor(s.getColor())+"\", \"posX\"  : "+j+",  \"posY\"  : "+i+"}" );
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
					//times    
		 
					"CommunicationPhaseTimeConst":"<% out.print(GeneralFunction.parseTime(gs.getPhases().getPhaseDuration("Communication Phase")));%>" ,
					"ExchangePhaseTimeConst":"<% out.print(GeneralFunction.parseTime(gs.getPhases().getPhaseDuration("Exchange Phase")));%>" ,
					"MovementPhaseTimeConst":"<% out.print(GeneralFunction.parseTime(gs.getPhases().getPhaseDuration("Movement Phase")));%>" ,
					"FeedBackPhaseTimeConst":"<% out.print(GeneralFunction.parseTime(gs.getPhases().getPhaseDuration("Feedback Phase")));%>" ,
					//set cards to player  
					setSumChips: function(playerID,color,sum)
					{
					for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{
							 for(var j=0;j<game.players[i].cards.length;j++)
								if (game.players[i].cards[j].color==color)
									game.players[i].cards[j].sum=sum;
							}
					},
					//getPlayerId 
					getPlayerId: function(serial)
					{
						return game.players[serial].id;
					},
					getNumOfPlayer: function()
					{
						return game.numOfPlayer;
					},
					//get cards
					getSumChips: function(playerID,color)
					{
					for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{
							 for(var j=0;j<game.players[i].cards.length;j++)
								if (game.players[i].cards[j].color==color)
									return game.players[i].cards[j].sum;
							}
					},
					getGoalPositionX: function(goalID)
					{
						for (var i=0;i<this.numGoals;i++)
						if (game.goals[i].id==goalID)
							{
							 	return game.goals[i].posX;
							}
					},
					getGoalPositionY: function(goalID)
					{
						for (var i=0;i<this.numGoals;i++)
						if (game.goals[i].id==goalID)
							{
							 	return game.goals[i].posY;
							}
					},
					getPlayerPositionX: function(playerID)
					{
						for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{
							 	return game.players[i].posX;
							}
					},
					
					getPlayerPositionY: function(playerID)
					{
						for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{
							 	return game.players[i].posY;
							}
					},
					getisPlayerMe: function(playerID)
                    {
                        for (var i=0;i<playerNumTotal;i++)
                        if (game.players[i].id==playerID)
                            {
                                 return game.players[i].isme;
                            }
                    },
					getPlayerSerial: function(playerID)
					{
						for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{
							 	return i;
							}
					},
					getPlayerName: function(playerID)
					{
						for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{
							 	return game.players[i].name;
							}
					}
					,
					getPlayerIcon: function(playerID)
					{  
						for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{  
								return game.players[i].icon;
							}
					}
					,
					getNumOfColors: function()
					{
						return game.numOfColors;
					},
					setIsEnded: function(isEnded)
					{
						game.isEnded = isEnded;
					},
					setRole: function(role)
					{
						game.role = role;
					},
					setPlayerPosition: function(playerID,x,y)
					{
						for (var i=0;i<playerNumTotal;i++)
						if (game.players[i].id==playerID)
							{
							 	game.players[i].posY=y;
								game.players[i].posX=x;
							}
					},
					getSizeRows: function()
					{
						return this.rows;
					},
					getSizeCols: function()
					{
						return this.cols;
					},
					getColorCell:function(posX,posY)
					{
					for (var i=0;i<this.borderColors.length;i++)
							if ((this.borderColors[i].posX==posX)&&(this.borderColors[i].posY==posY))
								{
									return this.borderColors[i].color;
								}
					}
};
//end 

</script>

</head>
<body>
<div id="container">
<div id="header">
<h1>Color Trails - Web Application</h1>
</div>
<div id="navigation">
<ul>
	<li><a
		href="http://viki.eecs.harvard.edu/confluence/display/coloredtrailshome/Colored+Trails+Homepage">Home</a></li>
	<li><a href="javascript:LoadAbout()">About</a></li>
	<li><a href="#">Contact us</a></li>
</ul>
</div>
<div id="content-container">
<div id="section-navigation">
<table border="1" width="150">
	<tr>
		<td width="150">
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
		<td width="150">		
		<div id="CommunicationPhase"></div>
		<div id="ExchangePhase"></div>
		<div id="MovementPhase"></div>
		<div id="FeedbackPhase"></div>
		</td>
	<tr>
	<tr>
		<td width="150">		
		<table width="150" id="PlayerChips">
		</table>
		</td>
	</tr>
	<tr>
		<td><b>Player Name</b></td>
		<td><b>Gola ID</b></td>
		<td><b>MAX STEPS</b></td>
		<td></td>
	</tr>
	<tr>
		<td align=left><select id="playerSelect"
			onchange="getPlayersGoal()"
			style="width: 90px; margin: 0px 10px 5px 0px;">
			<option value=0>choose player</option>
		</select>
		</td>
		
		<td><select id="goalSelect" 
			style="width: 60px; margin: 0px 10px 5px 0px;">
			<option value=0>select</option>
		</select></td>
		<td><select id="numStepsPathFinder"
			style="width: 40px">
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
		<tr>
		<td>
		<button onclick="findPath()">Path Finder</button>
		</td>
	</tr>
</table>
<div id="pathFinderContainer" ></div>
	<!--end proposal area--> <!-- Source point for window animations. -->
	<div id="divSource" class="cssSource"></div>
</div>
	
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
<div id="ProposalsArea"></div>
<table id="chipsProposalPlayerCustom"></table>
</div>
<div id="footer">Copyright © WebCT - BGU, 2011</div>
</div>


<div id="dialog" title="About WebCT"></div>

<script type="text/javascript">
	createGameBoard();
	addColors();
	createGoals();
	PlayerChips();
	loadProposalsTable();
	//proposalArea();
	insertIntoPlayerSelect();
	insertIntoGoalSelect();
	setInterval("UpdateServer()", 5000);
</script>

</body>
</html>