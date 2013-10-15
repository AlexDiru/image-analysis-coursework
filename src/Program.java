import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;


public class Program {
	
	public static void main(String[] args) {
		//Load the OpenCV library 
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
		DogImageRestoration.run();
	}

}
