package ctMW;

public class ClockTicker implements Runnable{
	EnvCT env;
	
	public ClockTicker(EnvCT env){
		this.env = env;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(500);
				env.updateClock(1);
			} catch (InterruptedException e) { 
				e.printStackTrace();
			}
		}
	}
}
