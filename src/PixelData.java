import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

/**
 * Stores run length pixel data for RGB pixels
 * @author Alex
 *
 */
public class PixelData {
	/**
	 * Red channel val
	 */
	private int red;
	
	/**
	 * Blue channel val
	 */
	private int blue;
	
	/**
	 * Green channel val
	 */
	private int green;

	/**
	 * Run length of this pixel
	 */
	private int runLength;
	
	/**
	 * Converts an OpenCV matrix into a list of pixel data all with run length 1
	 * @param image
	 * @param direction Calculate run length horizontally or vertically
	 * @return
	 */
	public static List<PixelData> readImage(Mat image, RunLengthDirection direction) {
		List<PixelData> list = new ArrayList<PixelData>();
		
		if (direction == RunLengthDirection.HORIZONTAL)
			for (int y = 0; y < image.rows(); y++)
				for (int x = 0; x < image.cols(); x++)
					list.add(new PixelData(image, x, y));
		else if (direction == RunLengthDirection.VERTICAL)
			for (int x = 0; x < image.cols(); x++)
				for (int y = 0; y < image.rows(); y++)
					list.add(new PixelData(image, x, y));
			
		return list;
	}
	
	/**
	 * Gets the pixel data given a matrix and coordinates
	 * @param image
	 * @param x
	 * @param y
	 */
	public PixelData(Mat image, int x, int y) {
		this((int)image.get(y, x)[0], (int)image.get(y, x)[1], (int)image.get(y,x)[2]);
	}
	
	public PixelData(int red, int green, int blue) {
		this.red = red;
		this.blue = blue;
		this.green = green;
		runLength = 1; 
	}

	public PixelData(int red, int green, int blue, int runLength) {
		this.red = red;
		this.blue = blue;
		this.green = green;
		this.runLength = runLength;
	}
	
	public boolean equals(PixelData other) {
		return red == other.red && blue == other.blue && green == other.green;
	}
	
	@Override
	public String toString() {
		return runLength + StringManip.replaceNumbersWithLetters(red + "," + blue + "," + green + ";");
	}

	public void increaseRunLength() {
		runLength++;
	}

	public int getRunLength() {
		return runLength;
	}

	public int getRed() {
		return red;
	}
	
	public int getBlue() {
		return blue;
	}
	
	public int getGreen() {
		return green;
	}
}