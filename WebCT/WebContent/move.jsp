
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol"%>
<%@page import="webapp.WebAgentAdaptor"%>
<%@page import="webapp.ClinetControler"%><%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

 <% 
 String id =  session.getAttribute("id").toString(); 
 WebAgentAdaptor player =ClinetControler.GetClinet(id);

 String jsonMsg =  request.getParameter("json");  // out.print(ClinetControler.GetClinet("1234").getUpdates());
 if	(jsonMsg != null)
 {
	 JSONObject object=  JSONObject.fromObject(jsonMsg);
	 RowCol pos = new RowCol(object.optInt("posx"),object.optInt("posy")); 
	 boolean  res = true; //
	
	 PlayerStatus ps =player.client.getGameStatus().getMyPlayer() ;
     if (!ps.getChips().contains(new ChipSet(player.client.getGameStatus().getBoard().getSquare(pos).getColor()))) {
         /* player doesn't have the chips */
         res = false;
     }

     if (!RowCol.areNeighbors(ps.getPosition(), pos)) {
         /* only allow moves to adjacent squares for the moment */
         res = false;
     }

     if (!ps.areMovesAllowed()) {
         /* don't move if moves NOT allowed */
         res =  false;
     }
     
	 JSONObject JsonRes = new JSONObject();
	 if (res)
	 { 
		 player.client.communication.sendMoveRequest(pos);
		 JsonRes.put("result",new Integer(1));
	 }
	 else
	 {
		 JsonRes.put("result",new Integer(0));
	 }
	 out.print(JsonRes.toString());
			   
 }
 else
 {
	 out.print("{\"result\":0}");

 }
 %> 
