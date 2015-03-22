package apapl.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import apapl.plans.PlanSeq;

public class Schedule {
	
	private LinkedList<PlanSeq> atomic;
	private LinkedList<PlanSeq> nonatomic;
	
	private LinkedList<PlanSeq> allplans;
	private ArrayList<PlanSeq> newplans;
	
	public Schedule(){
		atomic = new LinkedList<PlanSeq>();
		nonatomic = new LinkedList<PlanSeq>();
	}
	
	public LinkedList<PlanSeq> getAtomic() {
		return this.atomic;
	}
	
	public List<PlanSeq> getNonAtomic() {
		return this.nonatomic;
	}
	public List<PlanSeq> getAllPlans() {
		return this.allplans;
	}
	
	public Schedule clone()
	{
		Schedule s = new Schedule();
		
		for(PlanSeq p : atomic)
		{
			s.atomic.add(p.clone());
		}
		for(PlanSeq p : nonatomic)
		{
			s.nonatomic.add(p.clone());
		}
		for(PlanSeq p : allplans)
		{
			s.allplans.add(p.clone());
		}
		
		return s;
	}

	public void setAtomic(LinkedList<PlanSeq> newSchedule) {
		this.atomic = newSchedule;	
	}

	public LinkedList<PlanSeq> getSchedule() {

		LinkedList<PlanSeq> plans = nonatomic;
		sortByStart(atomic);
		if (atomic.size() > 0)
			plans.add(atomic.getFirst());
		
		
		return plans;
	}

	private void sortByStart(LinkedList<PlanSeq> plans) {
		
		Collections.sort(plans, new Comparator<PlanSeq>(){
	           public int compare (PlanSeq p1, PlanSeq p2){
	               return p1.getExecStart().compareTo(p2.getExecStart());
	           }
	       });

	}

	public void remove(PlanSeq ps) {
		atomic.remove(ps);
		nonatomic.remove(ps);
	}


}
