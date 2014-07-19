import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;


public class MatrixHelperTest extends TestCase{
	@Test
	public void testEquals() {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Mat mat = new Mat(new Size(2,2),0);
		mat.put(0, 0, new double[] { 2 });
		mat.put(0, 1, new double[] { 4 });
		mat.put(1, 0, new double[] { 5 });
		mat.put(1, 1, new double[] { 80 });
		
		Mat mat2 = new Mat(new Size(2,2),0);
		mat2.put(0, 0, new double[] { 2 });
		mat2.put(0, 1, new double[] { 4 });
		mat2.put(1, 0, new double[] { 5 });
		mat2.put(1, 1, new double[] { 80 });
		
		Mat mat3 = new Mat(new Size(2,2),0);
		mat3.put(0, 0, new double[] { 2 });
		mat3.put(0, 1, new double[] { 4 });
		mat3.put(1, 0, new double[] { 6 });
		mat3.put(1, 1, new double[] { 80 });

		Mat mat4 = new Mat(new Size(1,1),0);
		mat4.put(0, 0, new double[] { 2 });

		Assert.assertTrue(MatrixHelper.equals(mat2, mat));
		Assert.assertTrue(!MatrixHelper.equals(mat3, mat));
		Assert.assertTrue(!MatrixHelper.equals(mat3, mat2));
		Assert.assertTrue(!MatrixHelper.equals(mat3, mat4));	
	}
}
