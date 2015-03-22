<%@page import="webapp.SimpleLog"%>
<%@page import="org.apache.derby.impl.load.Import"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@page import="java.util.logging.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Color Trails - Game Over</title>
</head>
<body>
<%	
	if (session.getAttribute("id") != null) {		
		SimpleLog.write("session id: " + session.getAttribute("id") + " was removed from session");
		session.removeAttribute("id");
	}
	
	SimpleLog.write("game ended");
%>
<h2>Color Trails On-Line</h2>
<br />
<br />
<h4>Thanks you for playing Color Trails</h4>
<h4>Have a good day :-)</h4>
</body>
</html>