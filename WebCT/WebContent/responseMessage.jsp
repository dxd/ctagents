
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscussionDiscourseMessage"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.shared.discourse.BasicProposalDiscourseMessage"%>
<%@page import="webapp.*"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="java.util.logging.*"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	

	String jsonMsg = request.getParameter("response");
	SimpleLog.write("jsonMsg: " + jsonMsg);

	if (jsonMsg != null) {
		JSONObject object = JSONObject.fromObject(jsonMsg);
		String msgID = object.optString("msgID");
		
		SimpleLog.write("response message, Msg ID: " + msgID);
		
		String userResponse = object.optString("accept");
		
		SimpleLog.write("response message, user response: " + userResponse);

		String id = session.getAttribute("id").toString();
		
		SimpleLog.write("response message, session id: " + id);
		
		WebAgentAdaptor adaptor = ClinetControler.GetClinet(id);

		BasicProposalDiscourseMessage propasel = adaptor.MessagesPendingResponse
				.get(msgID);
		// Create the message for response
		BasicProposalDiscussionDiscourseMessage responseForMsg = new BasicProposalDiscussionDiscourseMessage(
				propasel);
		if (userResponse.equals("1")) {
			responseForMsg.acceptOffer();
		} else {
			responseForMsg.rejectOffer();
		}
		adaptor.client.communication
				.sendDiscourseRequest(responseForMsg);
		
		SimpleLog.write("response message, response for msg: " + responseForMsg);
	}
%>
