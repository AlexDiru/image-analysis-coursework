

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;

public class FileHelper {
	
	/**
	 * Read all lines of a file and return to list
	 * @param filePath
	 * @return
	 */
	public static List<String> readAllLines(String filePath) {
		try {
			return (List<String>)FileUtils.readLines(new File(filePath));
		} catch (IOException e) {
			throw new FileDoesntExistException(filePath);
		}
	}

	/**
	 * Write a list of lines to a file
	 * @param filePath
	 * @param lines
	 */
	public static void writeAllLines(String filePath, List<String> lines) {
		try {
			FileUtils.write(new File(filePath), lines.get(0));
		} catch (IOException e) {
			throw new FileDoesntExistException(filePath);
		}
	}
	
	/**
	 * Writes a line of data to a file
	 * @param filePath
	 * @param line
	 */
	public static void writeLine(String filePath, String line) {
		try {
			DataOutputStream os = new DataOutputStream(new FileOutputStream(filePath));
			os.write(line.getBytes());
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Gets the size (in bytes) of a file
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath) {
		return new File(filePath).length();
	}
	
	@SuppressWarnings("resource")
	/**
	 * Read all the bytes from a file and return them as a string
	 * @param filePath
	 * @return
	 */
	public static String readAll(String filePath) { 
		DataInputStream is = null;
		try {
			is = new DataInputStream(new FileInputStream(filePath));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Byte> bytes = new ArrayList<Byte>();
		byte[] buf = new byte[8192];
		int nread;
		try {
			while ((nread = is.read(buf)) >= 0) {
			 	for (int i = 0; i < nread; i++)
			 		bytes.add(buf[i]);
			}
			
			byte[] bytesArr = new byte[bytes.size()];
			for (int i = 0; i < bytes.size(); i++)
				bytesArr[i] = bytes.get(i);
			return new String(bytesArr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reads all the bytes from a file and returns them as a byte array
	 * @param filePath
	 * @return
	 */
	public static byte[] readBytes(String filePath) {
		try {
			return IOUtils.toByteArray(new FileInputStream(new File(filePath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Writes an array of bytes to a file
	 * @param filePath
	 * @param dataBytes
	 */
	public static void writeBytes(String filePath, byte[] dataBytes) {
		try {
			IOUtils.write(dataBytes, new FileOutputStream(new File(filePath)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Custom exception
	 * @author Alex
	 *
	 */
	private static class FileDoesntExistException extends NotImplementedException {
		public FileDoesntExistException(String message) {
			super(message);
		}
	}
}
