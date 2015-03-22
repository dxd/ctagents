package webapp;


import java.util.Enumeration;
import java.util.Hashtable;

public class ClinetControler {
public static Hashtable<String, WebAgentAdaptor> ClinteList; 
public static String AddUserClinet(String IP, String Pin)
{
	//create a new session with the server With IP and Pin 
	//if current session exist or 
	WebAgentAdaptor agent = new WebAgentAdaptor(IP,Pin);
	if(ClinteList == null)
	{
		ClinteList = new Hashtable<String, WebAgentAdaptor>();
	}
	ClinteList.put(agent.ID, agent);
	return agent.ID;
	
}
public static WebAgentAdaptor GetClinet(String SessionID)
{
	return ClinteList.get(SessionID);
}
}
