package webapp;

public class GeneralFunction {
	public static String parseTime(int seconds)
	{
		 String finalMin="";
		 String finalSeconds="";
		 int min=0;
		 
		 while (seconds>=60)
		 {
			seconds= seconds-60;
			min=min+1;
		 }
		 
		 finalMin=min+"";
		 finalSeconds=seconds+"";
			 
		 if (min<10)
			 finalMin="0"+min;
		 if (seconds<10)
			 finalSeconds="0"+seconds;

		 return finalMin+":"+finalSeconds;
	}
}
