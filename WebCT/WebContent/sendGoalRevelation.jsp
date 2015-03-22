
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.discourse.ChipsRevelationDiscourseMessage"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.types.CTStateContainer"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus"%>
<%@page import="java.sql.RowId"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.client.ClientPlayerStatus"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Random"%>


<%@page import="webapp.WebAgentAdaptor"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage"%>
<%@page import="webapp.ColorConverter"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="java.util.logging.*"%>

<%@page import="webapp.*"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus"%><%@ page
	language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%	

	//this will send A discourse message to the server
	String jsonMsg = request.getParameter("json");
	//out.print(ClinetControler.GetClinet("1234").getUpdates());
	SimpleLog.write("send Goal Revelation, jsonMsg: " + jsonMsg);

	if (jsonMsg != null) {
		JSONObject object = JSONObject.fromObject(jsonMsg);
		// System.out.print(object);
		String playerID = object.optString("player");
		//String recipientID = object.optString("recipient");
		
		SimpleLog.write("send Goal Revelation, player ID: " + playerID);

		//ChipSet RevelationChipSet = new ChipSet();

		//JSONArray arr = object.optJSONArray("chips");

		//SimpleLog.write("send Revelation, chips array: " + arr);
		//System.out.print(arr.size() + "");
/*
		for (int i = 0; i < arr.size(); i++) {
			JSONObject chips = JSONObject.fromObject(arr.optString(i));
			String type = chips.optString("req");
			//fill the recived chipe set
			if (type.equalsIgnoreCase("revelation")) {
				RevelationChipSet.add(ColorConverter
						.getServerColor(chips.optString("color")),
						chips.optInt("sum"));
			}

		}
*/
		//conect to session and send the message
		String id = session.getAttribute("id").toString();

		//generate msg ID
		Random rnd = new Random();
		int msgID = rnd.nextInt(10000);

	/*	//create revelation message 
		ChipsRevelationDiscourseMessage crdm = new ChipsRevelationDiscourseMessage(
				Integer.parseInt(playerID),
				Integer.parseInt(recipientID),
				rnd.nextInt(100000), RevelationChipSet);
	*/
		//create goal revelation message to myself
		BasicProposalDiscourseMessage proposal = new 
		BasicProposalDiscourseMessage(Integer.parseInt(playerID),Integer.parseInt(playerID),-1,new ChipSet(),new ChipSet());
	
		//send goal revelation message (to myself)
		ClinetControler.GetClinet(id).client.communication.sendDiscourseRequest(proposal);
		
		System.out.println("senGoalRevelation.jsp proposal = "+proposal.toString());
		SimpleLog.write("send Goal Revelation, session id: " + id);

	}
%>