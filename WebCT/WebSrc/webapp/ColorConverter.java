package webapp;

public class ColorConverter {
	public static String getClientColor(String c) {

//		if (c.equals("RGBGreen")) {
//			return "BFFF7F"; /* "00ff00"; */
//		}
//		if (c.equals("RGBRed")) {
//			return "FF7F7F"; /* "ff0000"; */
//		}
//		if (c.equals("purple1")) {
//			return "BF7FFF"; /* "9b30ff"; */
//		}
//		if (c.equals("orange1")) {
//			return "7FFFFF"; /* "ffa500"; */
//		}
		
		//moran
		if (c.equals("RGBGreen")) {
			return "00FF00"; /* "00ff00"; */
		}
		if (c.equals("RGBRed")) {
			return "FF0000"; /* "ff0000"; */
		}
		if (c.equals("purple1")) {
			return "9B30FF"; /* "9b30ff"; */
		}
		if (c.equals("orange1")) {
			return "FFA500"; /* "ffa500"; */
		}
		if (c.equals("CTRed")) {
			return "FF7F7F"; 
		}
		if (c.equals("CTGreen")) {
			return "BFFF7F"; 
		}
		if (c.equals("CTPurple")) {
			return "BF7FFF"; 
		}
		if (c.equals("grey78")) {
			return "C7C7C7";
		}
		if (c.equals("CTOrange")) {
			return "FF9622";
		}
		if (c.equals("CTDkGreen")) {
			return "7FBF7F";
		}
		//moranend
		return "";
	}

	public static String getServerColor(String c) {
//		if (c.equals("BFFF7F")) {
//			return "RGBGreen";
//		}
//		if (c.equals("FF7F7F")) {
//			return "RGBRed";
//		}
//		if (c.equals("BF7FFF")) {
//			return "purple1";
//		}
//		if (c.equals("7FFFFF")) {
//			return "orange1";
//		}
		
		//moran
		if (c.equals("00FF00")) {
			return "RGBGreen";
		}
		if (c.equals("FF0000")) {
			return "RGBRed";
		}
		if (c.equals("9B30FF")) {
			return "purple1";
		}
		if (c.equals("FFA500")) {
			return "orange1";
		}
		if (c.equals("FF7F7F")) {
			return "CTRed"; 
		}
		if (c.equals("BFFF7F")) {
			return "CTGreen"; 
		}
		if (c.equals("BF7FFF")) {
			return "CTPurple"; 
		}
		if (c.equals("C7C7C7")) {
			return "grey78";
		}
		if (c.equals("FF9622")) {
			return "CTOrange";
		}
		if (c.equals("7FBF7F")) {
			return "CTDkGreen";
		}
		//moranend
		return "";
	}
}
