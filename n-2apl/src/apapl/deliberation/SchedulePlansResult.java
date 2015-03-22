package apapl.deliberation;

import apapl.plans.*;
import apapl.*;
import apapl.program.*;
import apapl.data.*;
import apapl.deliberation.DeliberationResult.InfoMessage;

import java.util.*;

public class SchedulePlansResult extends DeliberationResult
{

  	/**
  	 * Constructs a result object.
  	 * 
  	 * 
  	 */
  	public SchedulePlansResult(  )
	{

	}
  	
	/**
	 * The name of the deliberation step this result belongs to.
	 * 
	 * @return the name of this step
	 */
	public String stepName()
	{
	  return("Schedule Plans");
	}
	
	/**
	 * Lists the information about this step in text format. In particular,
	 * it lists which plans have been generated.
	 * 
	 * @return a list of InfoMessage objects pertaining to the information
	 */
	public LinkedList<InfoMessage> listInfo()
	{
	  LinkedList<InfoMessage> info = new LinkedList<InfoMessage>();

	  return( info );
	}
	
	/**
	* Checks whether the execution of the step this result belongs to has changed
	* the state of the module.
	*
	* @return true if the module has been changed while executing this step, false
	*		otherwise
	*/
	public boolean moduleChanged()
	{
		return false; //TODO
	}
}
