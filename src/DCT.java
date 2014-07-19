import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;


public class DCT {
	
	/**
	 * Takes an image (must have height and width a multiple of 8) and performs DCT compression at aratio using a given quantisation matrix
	 * @param m The image to quantise (must have height and width a multiple of 8)
	 * @param q The quantisation matrix is use
	 * @param ratio The 	
	 */
	private static void dctSubMatrix(Mat m, Mat q, double ratio) {
		//Split colour image into planes and dct each
		List<Mat> colourPlanes = new ArrayList<Mat>();
		Core.split(m, colourPlanes);
		for (int i = 0; i < 3; i++) {
			Core.dct(colourPlanes.get(i),colourPlanes.get(i));
			if (ratio != -1)
				divide(colourPlanes.get(i), q);
		}
		Core.merge(colourPlanes, m);
	}
	
	/**
	 * Used to run the DCT concurrently, performs the DCT algorithm on a set of blocks between given boundaries
	 * @param m Image to compress
	 * @param q Quantisation matrix
	 * @param ratio The compression ratio to use -1 -> 99
	 * @param start The start index of the pixels to DCT
	 * @param end The end index of the pixels
	 */
	private static void dctConcurrent(Mat m, Mat q, double ratio, int start, int end) {
		for (int y = start; y < end; y += 8)
			for (int x = 0; x < m.cols(); x += 8)
				dctSubMatrix(m.submat(y, y + 8, x, x + 8), q, ratio);
	}
	
	/**
	 * Encodes an image using the DCT algorithm
	 * @param image Image to compress
	 * @param ratio The compression ratio to use -1 -> 99
	 * @return Encoded image
	 */
	public static Mat encode(Mat image, final double ratio) {
		//The matrix size needs to be divisable by 8 to be divided by the quantisation matrix
		int xBorder = 8 - (image.cols() % 8);
		int yBorder = 8 - (image.rows() % 8);
		Size sizeWithBorders = new Size(image.cols() + xBorder, image.rows() + yBorder);
		
		//Create the image with borders
		final Mat typeImage = Mat.zeros(sizeWithBorders, CvType.CV_32FC3);
		for (int y = 0; y < image.rows(); y++)
			for (int x = 0; x < image.cols(); x++)
				typeImage.put(y, x, new double[] { image.get(y, x)[0],image.get(y, x)[1],image.get(y, x)[2] });
		
		//Divide by the quantisation matrix - multithreaded - each thread works on a quarter of the image
		final Mat quantisationMatrix = JPEGQuantisationMatrix.getMatrix(ratio);
		final int quarterpoint = (((typeImage.rows()/8)/4)*8);
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				dctConcurrent(typeImage, quantisationMatrix, ratio, 0, quarterpoint);
			}
		});
		t1.start();
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				dctConcurrent(typeImage, quantisationMatrix, ratio, quarterpoint, quarterpoint * 2);
			}
		});
		t2.start();
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				dctConcurrent(typeImage, quantisationMatrix, ratio, quarterpoint * 2, quarterpoint * 3);
			}
		});
		t3.start();
		Thread t4 = new Thread(new Runnable() {
			@Override
			public void run() {
				dctConcurrent(typeImage, quantisationMatrix, ratio, quarterpoint * 3, typeImage.rows());
			}
		});
		t4.start();
		
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Mat compressedImage = new Mat(image.size(), CvType.CV_32FC3);
		
		//Remove borders
		for (int y = 0; y < image.rows(); y++)
			for (int x = 0; x < image.cols(); x++) {
				compressedImage.put(y, x, new double[] { typeImage.get(y, x)[0],typeImage.get(y, x)[1],typeImage.get(y, x)[2] });
			}
		
		return compressedImage;
	}
	
	/**
	 * Returns the image to the spatial domain when decompressing
	 * @param image The image to decompress
	 * @param ratio The ratio used to compress the image
	 * @return The decompressed image
	 */
	public static Mat decode(final Mat image, double ratio) {
		//The matrix size needs to be divisable by 8 to be divided by the quantisation matrix
		int xBorder = 8 - (image.cols() % 8);
		int yBorder = 8 - (image.rows() % 8);
		Size sizeWithBorders = new Size(image.cols() + xBorder, image.rows() + yBorder);
		
		//Create the image with borders
		Mat typeImage = Mat.zeros(sizeWithBorders, CvType.CV_32FC3);
		for (int y = 0; y < image.rows(); y++)
			for (int x = 0; x < image.cols(); x++) {
				typeImage.put(y, x, new double[] { image.get(y, x)[0],image.get(y, x)[1],image.get(y, x)[2]});
			}
		
		
		//Inverse DCT each quadrant
		for (int y = 0; y < typeImage.rows(); y += 8)
			for (int x = 0; x < typeImage.cols(); x += 8) {
				Mat dctInverseSubMatrix = typeImage.submat(y, y + 8, x, x + 8);
				List<Mat> colourPlanes = new ArrayList<Mat>();
				Core.split(dctInverseSubMatrix, colourPlanes);
				for (int i = 0; i < 3; i++) {
					if (ratio != -1)
						multiply(colourPlanes.get(i), JPEGQuantisationMatrix.getMatrix(ratio));
					Core.idct(colourPlanes.get(i), colourPlanes.get(i));
				}
				Core.merge(colourPlanes, dctInverseSubMatrix);
			}
				
		Mat dctImage = new Mat(image.size(), image.type());
		for (int y = 0; y < image.rows(); y++)
			for (int x = 0; x < image.cols(); x++)
				dctImage.put(y, x, new double[] { typeImage.get(y,x)[0], typeImage.get(y,x)[2], typeImage.get(y,x)[1]});
		
		return dctImage;
	}
	
	/**
	 * Divides a matrix by the other
	 * C[i,j] = A[i,j] / B[i,j]
	 * @param a Numerator
	 * @param b Denominator
	 */
	private static void divide(Mat a, final Mat b) {
		for (int y = 0; y < a.rows(); y++)
			for (int x = 0; x < a.cols(); x++)
				a.put(y,x, new double[] { a.get(y,x)[0]/b.get(y,x)[0] });
	}
	
	/**
	 * Multiples a matrix by the other
	 * C[i,j] = A[i,j] * B[i,j]
	 * @param a 
	 * @param b
	 */
	private static void multiply(Mat a, final Mat b) {
		for (int y = 0; y < a.rows(); y++)
			for (int x = 0; x < a.cols(); x++)
				a.put(y,x, new double[] { a.get(y,x)[0]*b.get(y,x)[0] });
	}
}
