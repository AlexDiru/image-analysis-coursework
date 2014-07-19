import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

/**
 * Stores the run length for grayscale pixels
 * @author Alex
 *
 */
public class PixelDataGrayscale {
	/**
	 * Value of the pixel
	 */
	private int val;
	
	/**
	 * The run length
	 */
	private int runLength;
	
	/**
	 * Converts an image to a list of pixel data all with run length 1
	 * @param image
	 * @return
	 */
	public static List<PixelDataGrayscale> readImage(Mat image) {
		List<PixelDataGrayscale> list = new ArrayList<PixelDataGrayscale>();
		for (int y = 0; y < image.rows(); y++)
			for (int x = 0; x < image.cols(); x++)
				list.add(new PixelDataGrayscale(image, x, y));
		return list;
	}
	
	/**
	 * Given a matrix and a coordinate, set this object to the values found
	 * @param image
	 * @param x
	 * @param y
	 */
	public PixelDataGrayscale(Mat image, int x, int y) {
		this((int)image.get(y, x)[0]);
	}

	public PixelDataGrayscale(int val) {
		this.val = val;
		runLength = 1; 
	}

	public PixelDataGrayscale(int val, int runLength) {
		this.val = val;
		this.runLength = runLength; 
	}
	
	public boolean equals(PixelDataGrayscale other) {
		return val == other.val;
	}
	
	@Override
	public String toString() {
		return runLength + StringManip.replaceNumbersWithLetters(val + ";");
	}

	public void increaseRunLength() {
		runLength++;
	}

	public int getRunLength() {
		return runLength;
	}

	public int getValue() {
		return val;
	}
}