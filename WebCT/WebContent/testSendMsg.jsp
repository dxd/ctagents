<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
	String jsonMsg = request.getParameter("number");
	java.io.FileOutputStream o; // declare a file output object
	java.io.PrintStream p; // declare a print stream object
	if (jsonMsg != null) {
		try {
			// Create a new file output stream
			// connected to "myfile.txt"
			o = new java.io.FileOutputStream("c:\\myfile.txt");

			// Connect print stream to the output stream
			p = new java.io.PrintStream(o);

			p.println(jsonMsg);

			p.close();
		} catch (Exception e) {
			System.err.println("Error writing to file");
		}
	} else {
		out.print("test");
	}
%>
</body>
</html>