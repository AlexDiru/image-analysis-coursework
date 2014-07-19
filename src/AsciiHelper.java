import java.io.UnsupportedEncodingException;


public class AsciiHelper {

	/**
     * Converts a string of ASCII text to a string of 0s and 1s
     * Modified from: http://www.coderanch.com/t/567086/java/java/Ascii-binary-binary-ascii
	 * @param c ASCII char to convert
	 * @return Binary string
	 */
	public static String toBinary(char c) {
		 byte[] bytes = new String(c + "").getBytes();  
	        StringBuilder binary = new StringBuilder();  
	        for (byte b : bytes) {  
	           int val = b;  
	           for (int i = 0; i < 8; i++) {  
	              binary.append((val & 128) == 0 ? 0 : 1);  
	              val <<= 1;  
	           }  
	        }  
	        return binary.toString();  
	}
	
	/**
	 * Converts an ASCII string to binary
	 * @param s
	 * @return
	 */
	public static String toBinary(String s) {
		StringBuilder binary = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			binary.append(toBinary(s.charAt(i)));
		try {
			return new String(binary.toString().getBytes("ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] toBinaryBytes(String s) {
		StringBuilder binary = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			binary.append(toBinary(s.charAt(i)));
		try {
			return binary.toString().getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	/**
	 * Converts binary string to ascii string
	 * @param b
	 * @return
	 */
	public static String fromBinaryBytes(byte[] b) {
		StringBuilder ascii = new StringBuilder();
		for (int i = 0; i < b.length; i += 8) {
			String binary = "";
			for (int j = i; j < (i + 8 > b.length ? b.length : i + 8); j++)
				binary += (char)b[j];
			ascii.append(fromBinaryChar(binary));
		}
		return ascii.toString();
	}
	
	/**
	 * Converts binary string to ascii string
	 * @param b
	 * @return
	 */
	public static String fromBinary(String b) {
		StringBuilder ascii = new StringBuilder();
		for (int i = 0; i < b.length(); i += 8) {
			byte c = fromBinaryByte(b.substring(i, i + 8 > b.length() ? b.length() : i + 8 ));
			try {
				ascii.append(new String(new byte[] { c }, "ISO-8859-1"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		return ascii.toString();
	}

	/**
	 * Converts binary string to ascii character
	 * @param b
	 * @return
	 */
	private static char fromBinaryChar(String b) {
    	return (char)Integer.parseInt(b, 2);
	}
	
	/**
	 * Converts binary string to ascii byte
	 * @param b
	 * @return
	 */
	private static byte fromBinaryByte(String b) {
    	return (byte)Integer.parseInt(b, 2);
	}

	/**
	 * Converts binary string to ascii bytes
	 * @param b
	 * @return
	 */
	public static byte[] fromBinaryBytes(String b) {
		byte[] bytes = new byte[b.length()];
		for (int i = 0; i < b.length(); i += 8) {
			bytes[i] = fromBinaryByte(b.substring(i, i + 8 > b.length() ? b.length() : i + 8 ));
		}
		return bytes;
	}
	
}
