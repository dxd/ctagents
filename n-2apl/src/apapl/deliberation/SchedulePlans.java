package apapl.deliberation;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import apapl.APLModule;
import apapl.NoRuleException;
import apapl.SubstList;
import apapl.data.APLFunction;
import apapl.data.Goal;
import apapl.data.Literal;
import apapl.data.Prohibition;
import apapl.data.Term;
import apapl.plans.BeliefUpdateAction;
import apapl.plans.ChunkPlan;
import apapl.plans.Plan;
import apapl.plans.PlanResult;
import apapl.plans.PlanSeq;
import apapl.program.BeliefUpdate;
import apapl.program.BeliefUpdates;
import apapl.program.Beliefbase;
import apapl.program.Goalbase;
import apapl.program.PGrule;
import apapl.program.PGrulebase;
import apapl.program.Planbase;
import apapl.program.Prohibitionbase;
import apapl.program.Schedule;

public class SchedulePlans implements DeliberationStep {

	APLModule module;

	public DeliberationResult execute(APLModule module) {

		

		Planbase planbase = module.getPlanbase();
		// Planbase atomicplans = module.getAtomicPlanbase();
		Prohibitionbase prohibitions = module.getProhibitionbase();

		schedulePlans(planbase, prohibitions, module.getAtomic());
		// scheduleAtomicPlans(atomicplans, prohibitions, bu);
		//module.setNewAtomic();

		this.module = module;
		return new SchedulePlansResult();
	}

	private void schedulePlans(Planbase planbase, Prohibitionbase prohibitions, PlanSeq atomic) {

		ArrayList<PlanSeq> newAtomic = new ArrayList<PlanSeq>();

		ArrayList<PlanSeq> newNonAtomic = new ArrayList<PlanSeq>();

		planbase.sortPlansPriority();

		for (PlanSeq ps : planbase) {

			//atomic

			ArrayList<PlanSeq> tempAtomic = new ArrayList<PlanSeq>();

			if (ps.isAtomic()) {

				if (ps == atomic) {
					if (ps.getExecStart() == null)
						ps.setExecStart(null);
					tempAtomic = newAtomic;
				}
				else {
					Date ne = new Date();
					tempAtomic = new ArrayList<PlanSeq>();

					for (PlanSeq p : newAtomic) {
						if (p.getDeadline().getTime() <= ps.getDeadline()
								.getTime()) 
						{
							tempAtomic.add(p);
							ne = new Date(Math.max(new Date(p.getDeadline()
									.getTime() - p.getExecStart().getTime())
							.getTime(), ne.getTime()));
						} else 
						{
							p.setExecStart(new Date(p.getExecStart().getTime()
									+ ps.getDuration()));
							tempAtomic.add(p);
						}
					}

					ps.setExecStart(ne);
				}

				boolean pass = true;

				if (!violatesProhibitions(ps, prohibitions))
				{
					for (PlanSeq p1 : tempAtomic) {
						Date now = new Date();
						long rt = 0;

						for (PlanSeq p2 : tempAtomic) {
							if (!p2.equals(p1)
									&& p2.getExecStart().getTime() <= p1
									.getExecStart().getTime()) {
								rt += p2.getDeadline().getTime()
										- p2.getExecStart().getTime();
							}
						}

						if (now.getTime() + rt <= p1.getDeadline().getTime()) {
							pass = false; //TODO algorithm not working
							break;
						}
					}
				}
				else 
				{
					pass = false;
					ps.setProhibited();
				}

				if (pass)
					newAtomic.add(ps);


				//non atomic
			} else {
				Date now = new Date();

				if (passNorms(ps, prohibitions)) {
					if (ps.getExecStart() != null) {
						if (ps.getDuration() + ps.getExecStart().getTime() <= ps.getDeadline().getTime())
							newNonAtomic.add(ps);
					}
					else if (ps.getDeadline() != null && now.getTime() + ps.getDuration() <= ps.getDeadline().getTime()) {						
						ps.setExecStart(null);
						newNonAtomic.add(ps);						
					}
					ps.setExecStart(null); //TODO remove
					newNonAtomic.add(ps);	
				}


			}
		}

		//planbase.removePlans();

		for (PlanSeq ps1 : newNonAtomic) {
			planbase.addPlan(ps1);
		}
		boolean first = true;
		for (PlanSeq ps1 : newAtomic) {
			if (!ps1.getProhibited() && first)
			{
				module.setAtomic(ps1);
				first = false;
			}
			planbase.addPlan(ps1);

		}

	}


	public String toString() {
		return "Schedule Plans";
	}

	private boolean passNorms(PlanSeq ps, Prohibitionbase prohibitions) {

		if (violatesProhibitions(ps, prohibitions))
			return false;

		Date deadline = ps.getDeadline();
		if (deadline == null || deadline.getTime() == Long.MAX_VALUE)
			return true;

		Date started = ps.getExecStart();
		Date now = new Date();
		if (started == null) {
			if (ps.getDuration() + now.getTime() > deadline.getTime())
				return false;
		} else {
			long executed = now.getTime() - started.getTime();
			if (ps.getDuration() - executed > deadline.getTime()
					- now.getTime())
				return false;
		}

		return true;
	}

	private boolean violatesProhibitions(PlanSeq ps,
			Prohibitionbase prohibitions) {

		ArrayList<Prohibition> hpp = prohibitions.getHigher(ps.getPriority());

		if (hpp != null) {
			for (Prohibition p : hpp) {
				try {
					if (existIn(ps, p))
						return true;
				} catch (NoRuleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private boolean existIn(PlanSeq ps, Prohibition pp) throws NoRuleException {

		for (Plan plan : ps.getPlans())
		{
			//plan.evaluateArguments();
			if (plan instanceof BeliefUpdateAction) {
				APLFunction p = ((BeliefUpdateAction) plan).getPlan();
				SubstList<Term> theta = new SubstList<Term>();
				Beliefbase beliefbase = module.getBeliefbase();
				BeliefUpdate c;
				c = module.getBeliefUpdates().selectBeliefUpdate(p,beliefbase,theta);

				if (c == null)
					continue;

				for (Literal l : c.getPost()) {
					Literal lcopy = l.clone();
					lcopy.applySubstitution(theta);
					Literal pl = pp.getProhibition();
					//System.out.println(lcopy.toString() +"  "+pl.toString());
					if (lcopy.equals(pl))
						return true;
				}
			}
		}
		return false;
	}

}
