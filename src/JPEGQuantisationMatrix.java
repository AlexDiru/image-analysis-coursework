import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;


public class JPEGQuantisationMatrix {

	//From: https://www.google.co.uk/url?sa=t&rct=j&q=&esrc=s&source=web&cd=5&ved=0CF4QFjAE&url=http%3A%2F%2Fwww.eee.bham.ac.uk%2Fspannm%2FTeaching%2520docs%2FEE1F2%2FLecture%25206%2520The%2520DCT%2520and%2520JPEG%2520Compression.pptx&ei=clKeUr3xFeWK7AaU5oDYBg&usg=AFQjCNF_TnynyT0ouirw4qbQc9_zJd-12w&sig2=4RNKTaWKx9zTEJX95f3VNA&bvm=bv.57155469,d.ZGU
	/**
	 * Quantisation matrix at ratio of 50
	 */
	private static final double[][] matrixArr = new double[][]
			{ 	new double[] { 16, 11, 10, 16, 24, 40, 51, 61 },
				new double[] { 12, 12, 14, 19, 26, 58, 60, 55 },
				new double[] { 14, 13, 16, 24, 40, 57, 69, 56 },
				new double[] { 14, 17, 22, 29, 51, 87, 80, 62 },
				new double[] { 18, 22, 37, 56, 68, 109, 103, 77 },
				new double[] { 24, 35, 55, 64, 81, 104, 113, 92 },
				new double[] { 49, 64, 78, 87, 103, 121, 120, 101},
				new double[] { 72, 92, 95, 98, 112, 100, 103, 99}
			
			};
	
	/**
	 * Creates an OpenCV matrix from a 2d array of doubles
	 * @param arr
	 * @return
	 */
	private static Mat createMatrix(double[][] arr) {
		Mat matrix = new Mat(new Size(8,8), 0);
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 8; x++)
				matrix.put(y, x, arr[y][x]);
		return matrix;
	}
	
	/**
	 * Returns a quantisation matrix scaled to a given ratio
	 * @param ratio
	 * @return
	 */
	public static Mat getMatrix(double ratio) {
		if (ratio == 50) {
			//Default
			return createMatrix(matrixArr);
		}
		
		//Scale if not default
		
		//Copy array (.clone() method doesn't work for some reason...... Remember this cos I wasted 30 mins debugging something simple.. JAVA AFHEUHFIABSFAHI)
		double[][] mutableArr = new double[8][];
		for (int y = 0; y < 8; y++){ 
			mutableArr[y] = new double[8];
			for (int x = 0; x < 8; x++) {
				mutableArr[y][x] = matrixArr[y][x];
			}
		}
		
		//Compression formula to get scale
		double scale = 0;
		if (ratio > 50)
			scale = (double)((double)(100 - ratio)/(double)(50));
		else if (ratio < 50)
			scale = (double)((double)(50)/ratio);
		
		//Scale quantisation matrix
		for (int y = 0; y < mutableArr.length; y++)
			for (int x = 0; x < mutableArr[y].length; x++)
				mutableArr[y][x] = mutableArr[y][x] * scale;

		//Convert double array to OpenCV matrix
		return createMatrix(mutableArr);
	}
}
