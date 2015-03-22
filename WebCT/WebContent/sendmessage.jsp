
<%@page import="java.sql.RowId"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.client.ClientPlayerStatus"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Random"%>


<%@page import="webapp.*"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage"%>
<%@page import="webapp.ColorConverter"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="java.util.logging.*"%>

<%@page import="webapp.ClinetControler"%>
<%@page import="webapp.listen"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus"%><%@ page
	language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	
	
	//this will send A discourse message to the server
	String jsonMsg = request.getParameter("number");
	//out.print(ClinetControler.GetClinet("1234").getUpdates());
	SimpleLog.write("send message, jsonMsg: " + jsonMsg);

	if (jsonMsg != null) {
		JSONObject object = JSONObject.fromObject(jsonMsg);
		// System.out.print(object);
		String playerID = object.optString("player");
		String recipientID = object.optString("recipient");
		String rowID = object.optString("rowID");
		
		SimpleLog.write("send message, player ID: " + playerID);
		SimpleLog.write("send message, recipient ID: " + recipientID);
		SimpleLog.write("send message, row ID: " + rowID);
		
		ChipSet SendChipSet = new ChipSet();
		ChipSet ReciveChipSet = new ChipSet();
		JSONArray arr = object.optJSONArray("chips");
		
		SimpleLog.write("send message, chips array: " + arr);
		System.out.print(arr.size() + "");
		
		for (int i = 0; i < arr.size(); i++) {
			JSONObject chips = JSONObject.fromObject(arr.optString(i));
			String type = chips.optString("req");
			//fill the recived chipe set
			if (type.equalsIgnoreCase("receive")) {
				ReciveChipSet.add(ColorConverter.getServerColor(chips
						.optString("color")), chips.optInt("sum"));
			} else { //else fill the send chipe set
				SendChipSet.add(ColorConverter.getServerColor(chips
						.optString("color")), chips.optInt("sum"));
			}

		}
		//conect to session and send the message
		String id = session.getAttribute("id").toString();
		
		SimpleLog.write("send message, session id: " + id);
		
		int ProposerID = -1;
		int RecieverID = -1;

		ProposerID = Integer.parseInt(playerID);
		RecieverID = Integer.parseInt(recipientID);

		//generate msg ID
		Random rnd = new Random();
		int msgID = rnd.nextInt(10000);

		WebAgentAdaptor adaptor = ClinetControler.GetClinet(id);
		BasicProposalDiscourseMessage dm = new BasicProposalDiscourseMessage(
				ProposerID, RecieverID, msgID, SendChipSet,
				ReciveChipSet);
		adaptor.client.communication.sendDiscourseRequest(dm);
		
		SimpleLog.write("send message, DiscourseMessage: " + dm.toString());

		//for debug
		SimpleLog.write("send message, Proposer ID: " + ProposerID);
		SimpleLog.write("send message, Reciever ID: " + RecieverID);
		SimpleLog.write("send message, Msg ID: " + msgID);
		SimpleLog.write("send message, row ID: " + rowID);
		SimpleLog.write("send message, ReciveChipSet: " + ReciveChipSet.toString());
		SimpleLog.write("send message, SendChipSet: " + SendChipSet.toString());

		out.print(ReciveChipSet.toString());
		out.print(SendChipSet.toString());
	}
%>