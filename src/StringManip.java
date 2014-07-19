import java.io.UnsupportedEncodingException;


public class StringManip {

	/**
	 * Replaces occurrences of 0 - 9 in a string with A - J
	 * @param data The string to replace the occurrences from
	 * @return The edited string
	 */
	public static String replaceNumbersWithLetters(final String data) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < data.length(); i++)
			if (Character.isDigit(data.charAt(i)))
				out.append((char)((int)data.charAt(i) + 17));
			else
				out.append(data.charAt(i));
		return out.toString();
	}

	/**
	 * Replaces the occurrences of A - Z in a string with 0 - 9
	 * @param data THe string to replace the occurrences from
	 * @return The edited String
	 */
	public static String replaceLettersWithNumbers(final String data) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < data.length(); i++)
			if (isUppercaseAtoJ(data.charAt(i)))
				out.append((char)((int)data.charAt(i) - 17));
			else
				out.append(data.charAt(i));
		return out.toString();
	}
	
	/**
	 * Checks if a character is A - J 
	 * @param c The character to check
	 * @return Is A - J
	 */
	private static boolean isUppercaseAtoJ(char c) {
		return c >= 'A' && c <= 'J';
	}
	
	public static boolean compare(String a, String b) {
		if (a.length() != b.length())
			return false;
		
		try {
			byte[] ab = a.getBytes("ISO-8859-1");
			byte[] bb = b.getBytes("ISO-8859-1");

			for (int i = 0; i < ab.length; i++)
				if (ab != bb)
					return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		
		
		return true;
	}
}
