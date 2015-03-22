
<%@page import="java.io.Console"%>
<%@page import="webapp.*"%>
<%@page import="java.util.logging.*"%>
<%@page
	import="edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus"%>
	<%@page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	%>
<%	

	String id = session.getAttribute("id").toString();
	String msg = ClinetControler.GetClinet(id).getUpdates();
	
	SimpleLog.write("getupdate, session id: " + id + ", update from server: " + msg);
	
	out.print(msg);
	
	//gil
	//System.out.print(msg);
	//gilend
%>