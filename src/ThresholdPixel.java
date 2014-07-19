import org.opencv.core.Mat;


/**
 * Runs the Pixel Threshold algorithm
 * @author Alex
 *
 */
public class ThresholdPixel {
	/**
	 * Used for running the algorithm concurrently, operates on a subsection of the matrix
	 * @param m Image to compress
	 * @param thresholdSq The threshold value squared
	 * @param startIndex The start y pixel to compress
	 * @param endIndex The end y pixel to compress
	 */
	public static void compressConcurrent(Mat m, final double thresholdSq, final int startIndex, final int endIndex) {
		for (int y = startIndex; y < endIndex; y++)
			for (int x = 0; x < m.cols() - 1; x+=2)
			if (getDistanceSquared(m.get(y, x), m.get(y, x+1)) < thresholdSq) {
				double[] average =  getAverage(m.get(y, x), m.get(y, x+1));
				m.put(y, x,average.clone());
				m.put(y, x+1, average.clone());
			}
	}
	
	/**
	 * Compresses a matrix given a threshold
	 * @param m The matrix to compress
	 * @param threshold The threshold value
	 * @return The compressed matrix
	 */
	public static Mat compress(final Mat m, final double threshold) {
		final double thresholdSq = threshold * threshold;
		final int quarterpoint = m.rows()/4;
		
		//A thread for a quarter of the matrix
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				compressConcurrent(m, thresholdSq, 0, quarterpoint);
			}
		});
		t1.start();
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				compressConcurrent(m, thresholdSq, quarterpoint, quarterpoint * 2);
			}
		});
		t2.start();
		Thread t3 = new Thread(new Runnable() {
			@Override
			public void run() {
				compressConcurrent(m, thresholdSq, quarterpoint * 2, quarterpoint * 3);
			}
		});
		t3.start();
		Thread t4 = new Thread(new Runnable() {
			@Override
			public void run() {
				compressConcurrent(m, thresholdSq, quarterpoint * 3, m.rows());
			}
		});
		t4.start();
		
		//Wait for threads to join
		try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return m;
	}
	
	/**
	 * Calculates the average between two pixels
	 * @param px1
	 * @param px2
	 * @return
	 */
	private static double[] getAverage(final double[] px1, final double[] px2) {
		return new double[] { avg(px1,px2,0), avg(px1,px2,1), avg(px1, px2,2) };
	}
	
	/**
	 * Calculates the average between two doubles
	 * @param px1 The first pixel
	 * @param px2 The second pixel
	 * @param index The channel to get the average of
	 * @return The average
	 */
	private static double avg(final double[] px1, final double[] px2, int index) {
		return (px1[index] + px2[index])/2;
	}
	
	/**
	 * Gets the Manhattan distance between two pixels
	 * @param px1
	 * @param px2
	 * @return
	 */
	private static double getDistanceSquared(final double[] px1, final double[] px2) {
		return (px1[0] - px2[0])*(px1[0] - px2[0]) + (px1[1] - px2[1])*(px1[1] - px2[1]) + (px1[2] - px2[2])*(px1[2] - px2[2]);
	}
}
