package imageenchancement;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class OpenCVImage {

	/**
	 * Grayscale image input
	 */
	private Mat grayscale;
	
	/**
	 * Original colour format image
	 */
	private Mat image;
	
	/**
	 * Modified image
	 */
	private Mat modImage;
	
	/**
	 * Converts an input grayscale image to a colour image
	 * @param image
	 */
	public OpenCVImage(OpenCVGSImage grayScaleImage) {
		//8 = CV_GRAY2RGB
		grayscale = grayScaleImage.getModifiedMatrix();
		image = new Mat(grayScaleImage.getModifiedMatrix().size(),5);
		Imgproc.cvtColor(grayScaleImage.getModifiedMatrix(), image, Imgproc.COLOR_GRAY2RGB);
		modImage = image.clone();
	}
	
	/**
	 * Show an image in a JFrame so the user can see it
	 * @param image
	 */
	private static void showImageInJFrame(Mat image) {
		showImageInJFrame(image, image.width(), image.height());
	}
	
	/**
	 * Shows an image in a JFrame
	 * Code modified from http://stackoverflow.com/questions/16494916/equivalent-method-for-imshow-in-opencv-java-build
	 * @param image Image to show
	 * @param xSize Frame width
	 * @param ySize Frame height
	 */
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

	public Mat getOriginalMatrix() {
		return image;
	}
	
	public Mat getModifiedMatrix() {
		return modImage;
	}

	/**
	 * Get the contours of an image
	 * @param grayscale The grayscale image to get the contours of
	 * @param colourLevel The threshold colour level
	 * @return List of contours
	 */
	private List<MatOfPoint> getContours(Mat grayscale, int colourLevel) {
		Imgproc.threshold(grayscale, grayscale, colourLevel, 150, Imgproc.THRESH_BINARY);
		grayscale.convertTo(grayscale, CvType.CV_8UC1);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat optionalHierarchy = new Mat();
		Imgproc.findContours(grayscale, contours, optionalHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
		return contours;
	}
	
	/**
	 * Adds colour to the dog image
	 */
	public void addColour() {
		
		//Get contours
		List<MatOfPoint> contours = getContours(grayscale, 120);
		modImage.convertTo(modImage, CvType.CV_32FC3);
		//Image with colour
		Mat colouredImage = Mat.zeros(modImage.size(), modImage.type());
		//Image to display contours
		Mat contouredImage = Mat.zeros(modImage.size(), modImage.type());
		
		//Iterate through contours
		for (int i = contours.size() - 1; i >= 0; i--) {
			Imgproc.drawContours(contouredImage, contours, i, new Scalar(255,255,255));
			
			//Manually colouring contours
			List<Point> pixels = contours.get(i).toList();
			MatOfPoint2f contour = new MatOfPoint2f();
			contour.fromList(pixels);
			
			//Test each contour for the pixel inside it, then colour the contour accordingly
			if (Imgproc.pointPolygonTest(contour, DOG_POINT, false) > 0) {
				for (int y = 0; y < modImage.cols(); y++)
					for (int x = 0; x < modImage.rows(); x++)
						if (Imgproc.pointPolygonTest(contour, new Point(x,y), false) > 0)
							colouredImage.put(y,x, tendTowardsColour(modImage.get(y,x), DOG_COLOUR));
			} else if (Imgproc.pointPolygonTest(contour, NOSE_POINT, false) > 0) {
				for (int y = 0; y < modImage.cols(); y++)
					for (int x = 0; x < modImage.rows(); x++)
						if (Imgproc.pointPolygonTest(contour, new Point(x,y), false) > 0)
							colouredImage.put(y,x, tendTowardsColour(modImage.get(y,x), NOSE_COLOUR));
			} else if (Imgproc.pointPolygonTest(contour, LEYE_POINT, false) > 0 || Imgproc.pointPolygonTest(contour, REYE_POINT, false) > 0) {
				for (int y = 0; y < modImage.cols(); y++)
					for (int x = 0; x < modImage.rows(); x++)
						if (Imgproc.pointPolygonTest(contour, new Point(x,y), false) > 0)
							colouredImage.put(y,x, tendTowardsColour(modImage.get(y,x), EYE_COLOUR));
			} else if (Imgproc.pointPolygonTest(contour, OVEN_POINT, false) > 0) {
				for (int y = 0; y < modImage.cols(); y++)
					for (int x = 0; x < modImage.rows(); x++)
						if (Imgproc.pointPolygonTest(contour, new Point(x,y), false) > 0)
							colouredImage.put(y,x, tendTowardsColour(modImage.get(y,x), OVEN_COLOUR));
			} else if (Imgproc.pointPolygonTest(contour, LCOL_POINT, false) > 0 || Imgproc.pointPolygonTest(contour, MCOL_POINT, false) > 0 || Imgproc.pointPolygonTest(contour, RCOL_POINT, false) > 0) {
				for (int y = 0; y < modImage.cols(); y++)
					for (int x = 0; x < modImage.rows(); x++)
						if (Imgproc.pointPolygonTest(contour, new Point(x,y), false) > 0)
							colouredImage.put(y,x, tendTowardsColour(modImage.get(y,x), COLLAR_COLOUR));
			}
		}
	
		
		
		//Atmosphere colour
		for (int y = 0; y < colouredImage.rows(); y++)
			for (int x = 0; x < colouredImage.cols(); x++)
				if (colouredImage.get(y,x)[0] == 0 &&colouredImage.get(y,x)[1] == 0 && colouredImage.get(y,x)[2] == 0)
					colouredImage.put(y,x, tendTowardsColour(modImage.get(y,x), ATMOSPHERE_COLOUR, 0.35f));
		

		//HSI
		Imgproc.cvtColor(colouredImage, colouredImage, Imgproc.COLOR_BGR2HSV);
		ArrayList<Mat> hsv = new ArrayList<Mat>();
		Core.split(colouredImage, hsv);
		Mat hue = hsv.get(0);
		Mat sat = hsv.get(1);
		Mat val = hsv.get(2);
		int h = -10;
		int s = 0;
		int v = -10;
		
		//Hue
		for (int y = 0; y < hue.rows(); y++)
			for (int x = 0; x < hue.cols(); x++) {
				double hv = hue.get(y,x)[0] + h;
				if (hv < 0)
					hv = 0;
				if (hv > 179)
					hv = 179;
				hue.put(y, x, new double[] { hv });
			}
		
		//Sat
		for (int y = 0; y < sat.rows(); y++)
			for (int x = 0; x < sat.cols(); x++) {
				double hv = sat.get(y,x)[0] + s;
				if (hv < 0)
					hv = 0;
				if (hv > 255)
					hv = 255;
				sat.put(y, x, new double[] { hv });
			}
		
		//Val
		for (int y = 0; y < val.rows(); y++)
			for (int x = 0; x < val.cols(); x++) {
				double hv = val.get(y,x)[0] + v;
				if (hv < 0)
					hv = 0;
				if (hv > 255)
					hv = 255;
				val.put(y, x, new double[] { hv });
			}
		Core.merge(hsv, colouredImage);
		Imgproc.cvtColor(colouredImage, colouredImage, Imgproc.COLOR_HSV2BGR);
		
		showImageInJFrame(contouredImage);
		
		modImage = colouredImage;
	}
	
	/**
	 * Tends a colour towards another colour by a factor
	 * @param originalColour
	 * @param colour
	 * @param factor
	 * @return New colour
	 */
	private static double[] tendTowardsColour(final double[] originalColour, final double[] colour, float factor) {
		double red = originalColour[0] + ((colour[2] - originalColour[0]) * factor);
		double blue = originalColour[1] + ((colour[1] - originalColour[1]) * factor);
		double green = originalColour[2] + ((colour[0] - originalColour[2]) * factor);
		return new double[] { red, blue, green };
	}
	
	/**
	 * Tends a colour towards another colour
	 * @param originalColour
	 * @param colour
	 * @return
	 */
	private static double[] tendTowardsColour(final double[] originalColour, final double[] colour) {
		return tendTowardsColour(originalColour, colour, 0.5f);
	}
	
	//The pixels that correspond to parts of the image and the colour they should be (manual work)
	private static final Point DOG_POINT = new Point(180,209);
	private static final double[] DOG_COLOUR = new double[] { 221,172,4 };
	private static final Point NOSE_POINT = new Point(174,137);
	private static final double[] NOSE_COLOUR = new double[] { 15,10,12 };
	private static final Point LEYE_POINT = new Point(146,91);
	private static final Point REYE_POINT = new Point(198,88);
	private static final double[] EYE_COLOUR = new double[] { 15,15,45 };
	private static final Point OVEN_POINT = new Point(307,340);
	private static final double[] OVEN_COLOUR = new double[] { 240,240,240 };
	private static final Point LCOL_POINT = new Point(135,156);
	private static final Point MCOL_POINT = new Point(189,166);
	private static final Point RCOL_POINT = new Point(207,156);
	private static final double[] COLLAR_COLOUR = new double[] { 240,20,20 };
	private static final double[] ATMOSPHERE_COLOUR = new double[] { 220,220,220 };
}	
