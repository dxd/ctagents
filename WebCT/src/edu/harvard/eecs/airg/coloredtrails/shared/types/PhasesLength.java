package edu.harvard.eecs.airg.coloredtrails.shared.types;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class PhasesLength{
	public int stratPrepPhTime;
	public int revelationPhTime;
	public int commPhTime;
	public int counterOfferPhTime;
	public int exchngPhTime;
	public int movPhTime;
	public int fdbckPhTime;

	public PhasesLength() {
		stratPrepPhTime =0;
		revelationPhTime = 0;
		commPhTime = 0;
		exchngPhTime = 0;
		movPhTime = 0;
		fdbckPhTime = 0;
		getPhasesTimes();
		System.out.println("Created new PhasesLength");
	
	}
	
	public int getStratTime() {return stratPrepPhTime;}
	
	public void getPhasesTimes()
	{
		System.out.println("Just entered getPhasesTimes");
	
		try{
			BufferedReader bf = new BufferedReader(new FileReader("lib/adminconfig/PhaseTimes.txt"));
			String line;
			String[] tokens;
			while((line = bf.readLine()) != null){
				//System.out.println(line);
				if (line.startsWith("strategy")) {
					tokens = line.split("=");
					String tok = tokens[1].trim();
					//System.out.println("tok = " +tok);
					
					stratPrepPhTime = new Integer(tok).intValue();
					//System.out.println("In getPhasesTimes, stratPrepPhTime = " + stratPrepPhTime);
				}
				else if (line.startsWith("revelation")) {
					tokens = line.split("=");
					String tok = tokens[1].trim();
					//System.out.println("tok = " +tok);
					revelationPhTime = new Integer(tok).intValue();
				}
				else if (line.startsWith("communication")) {
					tokens = line.split("=");
					String tok = tokens[1].trim();
					//System.out.println("tok = " +tok);
					commPhTime = new Integer(tok).intValue();
					//System.out.println("In getPhasesTimes, commPhTime = " + commPhTime);
				}
				else if (line.startsWith("counter")) {
					tokens = line.split("=");
					String tok = tokens[1].trim();
					//System.out.println("tok = " +tok);
					counterOfferPhTime = new Integer(tok).intValue();
					//System.out.println("In getPhasesTimes, commPhTime = " + commPhTime);
				}
				else if (line.startsWith("exchange")) {
					tokens = line.split("=");
					String tok = tokens[1].trim();
					//System.out.println("tok = " +tok);
					exchngPhTime = new Integer(tok).intValue();
					//System.out.println("In getPhasesTimes, exchngPhTime = " + exchngPhTime);
				}
				else if (line.startsWith("movement")) {
					tokens = line.split("=");
					String tok = tokens[1].trim();
					//System.out.println("tok = " +tok);
					movPhTime = new Integer(tok).intValue();
					//System.out.println("In getPhasesTimes, movPhTime = " + movPhTime);
				}
				else if (line.startsWith("feedback")) {
					tokens = line.split("=");
					String tok = tokens[1].trim();
					//System.out.println("tok = " +tok);
					fdbckPhTime = new Integer(tok).intValue();
					//System.out.println("In getPhasesTimes, fdbckPhTime = " + fdbckPhTime);
				}			
			}

			bf.close();
		}catch(IOException e) {
			System.err.println(e.getMessage());
		}

	}

}
