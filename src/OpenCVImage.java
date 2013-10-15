import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class OpenCVImage {

	private Mat image;
	private Mat modImage; //Modified image
	
	public OpenCVImage(String file) {
		image = Highgui.imread(file);
		modImage = image.clone();
		
		//Check image loaded
		if (image.empty()) {
			System.out.println("Image failed to load: " + file);
			System.exit(1);
		}
	}
	
	//amse = (1/(M*N)) * sigma{x = 0 to M - 1}( sigma{y = 0 to N - 1}( |f(x,y) - f'(x,y)|^2))
	//From lecture notes (Lecture 1, page 34)
	public static double averageMeanSquareErrors(Mat imageReference, Mat imageModified) { 
		double errors = 0.f;
		for (int x = 0; x < imageReference.width(); x++)
			for (int y = 0; y < imageReference.height(); y++)
				errors += getPixelDistance(imageReference.get(y, x),imageModified.get(y, x) );
		return errors * (1.f / (double)(imageReference.width() * imageReference.height()));
	}
	
	private static double getPixelDistance(double[] i1, double[] i2) {
		return (i1[0] - i2[0]) * (i1[0] - i2[0]) + (i1[1] - i2[1]) * (i1[1] - i2[1]) +(i1[2] - i2[2]) * (i1[2] - i2[2]) ;
	}
	
	private static void showImageInJFrame(Mat image) {
		showImageInJFrame(image, image.width(), image.height());
	}
	
	//Code modified from http://stackoverflow.com/questions/16494916/equivalent-method-for-imshow-in-opencv-java-build
	private static void showImageInJFrame(Mat image, int xSize, int ySize) {
		//Resize the image
		Imgproc.resize(image, image, new Size(xSize,ySize));
		
		//Convert the image to a byte array
	    MatOfByte matOfByte = new MatOfByte();
	    Highgui.imencode(".jpg", image, matOfByte);
	    byte[] byteArray = matOfByte.toArray();
	    
	    //Convert the byte array to a buffered image
	    BufferedImage bufImage = null;
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	    } catch (Exception e) {
	        System.out.println("Failed to convert org.opencv.core.Mat to java.awt.image.BufferedImage");
	        System.exit(1);
	    }
	    
	    //Create the frame
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
        frame.pack();
        frame.setVisible(true);
	}
	
	public void showOriginalImage() {
		showImageInJFrame(image);
	}
	
	public void showModifiedImage() {
		showImageInJFrame(modImage);
	}

	//http://docs.opencv.org/doc/tutorials/core/basic_linear_transform/basic_linear_transform.html
	//g(y,x) = a*f(y,x) + b
	private void setGainAndBrightness(float alpha, float beta) {
		//Check for valid values of alpha and beta
		for (int y = 0; y < image.rows(); y++)
			for (int x = 0; x < image.cols(); x++) {
				//Perform a*f(y,x) + b on each colour value of the pixel
				double[] pixelData = new double[3];
				for (int c = 0; c < 3; c++)
					pixelData[c] = alpha * image.get(y,x)[c] + beta;
				modImage.put(y, x, pixelData);
			}
	}

	public void setContrast(float contrast) {
		 setGainAndBrightness(contrast,0f);
	}
	
	public void setBrightness(float brightness) {
		setGainAndBrightness(1f,brightness);
	}
	
	public void denoise() {
		org.opencv.photo.Photo.fastNlMeansDenoising(modImage, modImage);
	}

	//Subtract the blurred image from the unblurred image
	//Unsharpen mask
	public void sharpen(float sigmaX) {
		Mat blurred = modImage.clone();
		Imgproc.GaussianBlur(modImage, blurred, new Size(0,0), sigmaX);
		org.opencv.core.Core.addWeighted(modImage, 1.5, blurred, -0.5,0, modImage);
	}

	public Mat getOriginalMatrix() {
		return image;
	}
	
	public Mat getModifiedMatrix() {
		return modImage;
	}
}