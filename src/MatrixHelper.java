import org.opencv.core.Mat;


public class MatrixHelper {
	public static boolean equals(Mat a, Mat b) {
		if (a.rows() != b.rows() || a.cols() != b.cols())
			return false;
		
		for (int y = 0; y < a.rows(); y++)
			for (int x = 0; x < a.cols(); x++)
				if ((int)a.get(y, x)[0] != (int)b.get(y, x)[0])
					return false;
		return true;
	}
}
