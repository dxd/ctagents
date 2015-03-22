package apapl.plans;

import apapl.data.APLIdent;
import apapl.data.Prohibition;
import apapl.data.Term;
import apapl.ModuleAccessException;
import apapl.UnboundedVarException;
import apapl.APLModule;
import apapl.program.Goalbase;
import apapl.program.Beliefbase;
import apapl.program.Prohibitionbase;
import apapl.data.Goal;
import apapl.plans.Plan;
import apapl.SubstList;

import java.util.ArrayList;

/**
 * An action to update the goal base.
 */
public class PAction extends ModulePlan
{

	private Prohibition p;
	private String action;
	
	public PAction(APLIdent moduleId, String action, Prohibition p)
	{
		this.moduleId = moduleId;
		this.p = p;
		this.action = action.toLowerCase().trim();
		//System.out.println("PAction created:   "+p);
	}
	
	
	public synchronized PlanResult execute(APLModule module)
	{
		APLModule updatedModule;
		//System.out.println(action);
		
		if (moduleId != null) {
			try {
				if (moduleId == null)
					updatedModule = module;
				else
					updatedModule = module.getMas().getModule(module, moduleId.getName());
			} catch (ModuleAccessException e) {
				return new PlanResult(this, PlanResult.FAILED, "Module is not accessible: " + e.getMessage());
			}
		}
		else {
			updatedModule = module;
		}		
		
		int r = PlanResult.FAILED;
		
		Prohibitionbase prohibitions = updatedModule.getProhibitionbase();
		try {
			p.unvar();
		} catch (UnboundedVarException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (action.equals("aprohibition")) {
			//p.evaluate();
			prohibitions.add(p);
			//System.out.println("PAction:   "+p);
			parent.removeFirst();
			module.assignPriorities();
			r = PlanResult.SUCCEEDED;
		}
		
				
		return new PlanResult(this, r) ;
	}
	
	public String toString()
	{
		return ( moduleId == null ? "" : (moduleId + "." ) ) + action + "(" + p.toString(false) + ")";
	}
	
	public PAction clone()
	{
		return new PAction(moduleId, new String(action), p.clone());
	}
	
	public void applySubstitution(SubstList<Term> theta)
	{
		p.applySubstitution(theta);
	}
	
	public String toRTF(int t)
	{		
		return ( moduleId == null ? "" : (moduleId + "." ) ) + "\\cf4 " + action + "\\cf0 (" + p.toRTF(true)+ ")";
	}
	
	public void freshVars(ArrayList<String> unfresh, ArrayList<String> own, ArrayList<ArrayList<String>> changes)
	{
		p.freshVars(unfresh,own,changes);
	}
	
	public ArrayList<String> getVariables()
	{
		return p.getVariables();
	}
	
	public String getAction()
	{
		return action;
	}
	
	public Prohibition getProhibition()
	{
		return p;
	}
	
	public void setProhibition(Prohibition p)
	{
		this.p = p;
	}


	@Override
	public Term getPlanDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
}

