import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;


public class CompressionTest extends TestCase {

	@Test
	public void testCompressionAndDecompression() {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
		Mat mat = new Mat(new Size(3,3),0);
		mat.put(0, 0, new double[] { 2 });
		mat.put(0, 1, new double[] { 4 });
		mat.put(0, 2, new double[] { 80 });
		mat.put(1, 0, new double[] { 80 });
		mat.put(1, 1, new double[] { 80 });
		mat.put(1, 2, new double[] { 80 });
		mat.put(2, 0, new double[] { 3 });
		mat.put(2, 1, new double[] { 2 });
		mat.put(2, 2, new double[] { 1 });
		
		Compression c = new Compression(null);
		c.setImage(mat.clone());
		c.compress(true, -1);
		c.decompress();
		Assert.assertTrue(MatrixHelper.equals(c.getImage(), mat));
	}
}
