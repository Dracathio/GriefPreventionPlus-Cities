package net.kaikk.mc.gppcities;

public class Utils {
	private Utils() {}
	public static String mergeStringArrayFromIndex(String[] arrayString, int i) {
		StringBuilder sb = new StringBuilder();

		for(;i<arrayString.length;i++){
			sb.append(arrayString[i]);
			sb.append(' ');
		}

		if (sb.length()!=0) {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
}

