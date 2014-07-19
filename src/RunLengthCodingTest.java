import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;


public class RunLengthCodingTest extends TestCase {

	@Test
	public void testEncodeDecode() {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Mat mat = new Mat(new Size(2,2),0);
		mat.put(0, 0, new double[] { 2 });
		mat.put(0, 1, new double[] { 4 });
		mat.put(1, 0, new double[] { 5 });
		mat.put(1, 1, new double[] { 80 });
		

		Mat mat2 = new Mat(new Size(3,3),0);
		mat2.put(0, 0, new double[] { 2 });
		mat2.put(0, 1, new double[] { 4 });
		mat2.put(0, 2, new double[] { 80 });
		mat2.put(1, 0, new double[] { 80 });
		mat2.put(1, 1, new double[] { 80 });
		mat2.put(1, 2, new double[] { 80 });
		mat2.put(2, 0, new double[] { 3 });
		mat2.put(2, 1, new double[] { 2 });
		mat2.put(2, 2, new double[] { 1 });

		Assert.assertTrue(MatrixHelper.equals(RunLengthCoding.decodeGrayscale(RunLengthCoding.encode(mat, RunLengthDirection.HORIZONTAL)),mat));
		Assert.assertTrue(MatrixHelper.equals(RunLengthCoding.decodeGrayscale(RunLengthCoding.encode(mat2, RunLengthDirection.HORIZONTAL)),mat2));
		Assert.assertTrue(!MatrixHelper.equals(RunLengthCoding.decodeGrayscale(RunLengthCoding.encode(mat, RunLengthDirection.HORIZONTAL)),mat2));
		Assert.assertTrue(MatrixHelper.equals(RunLengthCoding.decodeGrayscale(RunLengthCoding.encode(mat, RunLengthDirection.VERTICAL)),mat));
		Assert.assertTrue(MatrixHelper.equals(RunLengthCoding.decodeGrayscale(RunLengthCoding.encode(mat2, RunLengthDirection.VERTICAL)),mat2));
		Assert.assertTrue(!MatrixHelper.equals(RunLengthCoding.decodeGrayscale(RunLengthCoding.encode(mat, RunLengthDirection.VERTICAL)),mat2));
	}
}
