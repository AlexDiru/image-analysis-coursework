package imageenchancement;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class OpenCVGSImage {

	/**
	 * Debug info?
	 */
	private static final boolean VERBOSE = false;
	
	/**
	 * Original image
	 */
	private Mat image;
	
	/**
	 * Modified image
	 */
	private Mat modImage;
	
	/**
	 * Constructor, loads the specified image from the file and stores the matrix
	 * @param file The filepath to the image
	 */
	public OpenCVGSImage(String file) {
		//Load image in grayscale to both image and modImage
		image = Highgui.imread(file,0); 
		modImage = image.clone();
		
		//Check image loaded
		if (image.empty()) {
			System.out.println("Image failed to load: " + file);
			System.exit(1);
		}
	}
	
	/**
	 * Calculate the average mean square error between two image matrices
	 * amse = (1/(M*N)) * sigma{x = 0 to M - 1}( sigma{y = 0 to N - 1}( |f(x,y) - f'(x,y)|^2))
	 * @param imageReference The first image
	 * @param imageModified The second image
	 */
	public static double averageMeanSquareErrors(Mat imageReference, Mat imageModified) { 
		double errors = 0.f;
		for (int x = 0; x < imageReference.width(); x++)
			for (int y = 0; y < imageReference.height(); y++)
				errors += getPixelDistance(imageReference.get(y, x),imageModified.get(y, x) );
		return errors / (double)(imageReference.width() * imageReference.height());
	}
	
	/**
	 * Different methods of applying the spatial domain algorithms
	 */
	public enum SpatialDomainMode {
		/**
		 * Takes the average of the neighbourhood and sets all pixels in the neighbourhood to that value
		 */
		AVERAGE,
		/**
		 * Takes the median of the neighbourhood and sets the centre pixel in the neighbourhood to that value
		 */
		MEDIAN,
		/**
		 * Takes the average of the neighbourhood and sets the centre pixel in the neighbourhood to that value
		 */
		AVERAGECENTER
	}
	
	/**
	 * Performs a power transform on modImage
	 * G(m,n) = c*f(m,n)^y
	 * @param c The constant value
	 * @param y The gamma factor
	 */
	public void powerTransformation(double c, double y) {
		for (int x = 0; x < modImage.width(); x ++)
			for (int py = 0; py < modImage.height(); py ++)
				modImage.put(py, x, new double[] { Math.pow(c * modImage.get(py,x)[0],y) });
	}
	
	/**
	 * Different methods of applying the frequency domain algorithms
	 */
	public enum FilterType {
		LOWPASS,
		HIGHPASS,
		BANDPASS
	}
	
	/**
	 * Generates a matrix to use for the frequency domain filters
	 * @param size The dimensions of the image of the filter to create
	 * @param radius The radius of the circle (for band pass, the outer radius)
	 * @param filterType The type of filter to create the image for
	 * @param bandpassRadius Only used for band pass, the inner radius
	 * @return The filter image matrix
	 */
	private static Mat generate01Circle(Size size, int radius, FilterType filterType, int bandpassRadius) {
		Mat m = Mat.zeros(size, 5);
		int cx = m.cols()/2;
		int cy = m.rows()/2;
		
		for (int y = 0; y < m.rows(); y++)
			for (int x = 0; x < m.cols(); x++)
				if ((cx - x) * (cx - x) + (cy - y) * (cy - y) <= radius*radius) {
					//Low pass uses a circle with 1 in, Band pass circle is 1
					if (filterType == FilterType.LOWPASS || filterType == FilterType.BANDPASS)
						m.put(y,x, new double[] { 1 });
					//High pass uses a circle with 0 in
					else if (filterType == FilterType.HIGHPASS)
						m.put(y,x, new double[] { 0 });
				} else {
					//Low pass outside is 0, Band pass outside is 0
					if (filterType == FilterType.LOWPASS || filterType == FilterType.BANDPASS)
						m.put(y,x, new double[] { 0 });
					//High pass outside is 1
					else if (filterType == FilterType.HIGHPASS)
						m.put(y,x, new double[] { 1 });
				}
		
		//Bandpass inner circle
		if (filterType == FilterType.BANDPASS) {
			for (int y = 0; y < m.rows(); y++)
				for (int x = 0; x < m.cols(); x++)
					if ((cx - x) * (cx - x) + (cy - y) * (cy - y) <= bandpassRadius * bandpassRadius)
						m.put(y,x, new double[] { 0 });
		}
		
		return m;
	}
	
	/**
	 * Converts a image used for a frequency domain filter so it's viewable by the user
	 * @param filterImage The matrix of the image to convert
	 * @return The viewable image's matrix
	 */
	private static Mat convertFilterImageToViewable(Mat filterImage) {
		//Convert all 1 values to 255 (white)
		//All 0 values can stay the same (black)
		for (int y = 0; y < filterImage.rows(); y++)
			for (int x = 0; x < filterImage.cols(); x++)
				if (filterImage.get(y,x)[0] == 1)
					filterImage.put(y,x, new double[] { 255 });
		return filterImage;
	}
	
	/**
	 * Rather than do regular multiplication, this multiplies the matrices directly
	 * i.e. C[y,x] = A[y,x] * B[y,x]
	 * @param a Parameter matrix
	 * @param b Parameter matrix
	 * @return Result matrix
	 */
	private static Mat directMultiplication(final Mat a, final Mat b) {
		Mat m = Mat.zeros(a.size(), a.type());
		for (int y = 0; y < a.rows(); y++)
			for (int x = 0; x < a.cols(); x++)
				m.put(y,x, new double[] { a.get(y,x)[0] * b.get(y,x)[0] });
		return m;
	}
	
	/**
	 * Performs a low pass filter on modImage
	 * @param radius The radius of the low pass circle
	 */
	public void lowPass(int radius) {
		DFT(FilterType.LOWPASS, radius, -1,false,false);
	}
	
	/**
	 * Performs a high pass filter on modImage
	 * @param radius The radius of the high pass circle
	 */
	public void highPass(int radius) {
		DFT(FilterType.HIGHPASS, radius, -1,false, false);
	}
	
	/**
	 * Performs a band pass filter on modImage
	 * @param outerRadius The outer radius of the band pass circle
	 * @param innerRadius The inner radius of the band pass circle
	 */
	public void bandPass(int outerRadius, int innerRadius) {
		DFT(FilterType.BANDPASS, outerRadius, innerRadius,false,false);
	}

	/**
	 * Performs a frequency domain algorithm on modImage
	 * Based on this tutorial http://docs.opencv.org/doc/tutorials/core/discrete_fourier_transform/discrete_fourier_transform.html
	 * @param ft The frequency domain algorithm to use
	 * @param radius1 The radius for low pass, high bass and band pass's outer radius
	 * @param radius2 The inner radius for band pass (ignored by the two other filters)
	 * @param showFrequencySpectrumInitial Show the frequency spectrum before the algorithm,
	 * @param showFrequencySpectrumAfter Show the frequency spectrum after the algorithm
	 */
	private void DFT(FilterType ft, int radius1, int radius2, boolean showFrequencySpectrumInitial, boolean showFrequencySpectrumAfter) {
		//Convert modImage matrix to double values (otherwise DFT won't work)
		modImage.convertTo(modImage, 5);

		//Generate the filter image
		Mat filterImage = generate01Circle(modImage.size(), radius1, ft, radius2);
		
		//When produced the frequency spectrum, OpenCV doesn't have its centre at the centre of the image
		//Thus we need to rearrange the quadrants of the filter image so they correspond to the quadrants of the spectrum
		filterImage = swapQuadrants(filterImage);
		
		//Show frequency spectrum in JFrame
		if (showFrequencySpectrumInitial) {
			//Clone modImage because we don't want to affect it
			Mat spectrum = modImage.clone();
			//Setup matrices to hold the real and imaginary parts produced by the DFT
			ArrayList<Mat> spectrumPlanes = new ArrayList<Mat>();
			spectrumPlanes.add(spectrum);
			spectrumPlanes.add(Mat.zeros(spectrum.size(),5));
			//Merge the planes into a matrix with 2 channels
			Core.merge(spectrumPlanes, spectrum);
			//Perform DFT on spectrum and store the result in itself
			Core.dft(spectrum, spectrum);
			//Clear spectrumPlanes so it can be reused
			spectrumPlanes.clear();
			//Split the frequency spectrum matrix into the real and imaginary parts
			Core.split(spectrum, spectrumPlanes);
			Mat sReal = spectrumPlanes.get(0); //Real part
			Mat sImag = spectrumPlanes.get(1); //Imaginary part
			//Calculate the magnitude as the magnitude as only the magnitude is useful since it contain all the image's geometric structure
			Core.magnitude(sReal, sImag, sReal);
			Mat sMag = sReal;
			//Compute log(M + 1) for each element in the matrix
			addScalar(sMag,1);
			Core.log(sMag,sMag);
			//Swap the quadrants so the frequency spectrum centre is in the centre of the image displayed to the user
			swapQuadrants(sMag);
			//Make the matrix values between 0 and 255 so the user can view them
			Core.normalize(sMag,sMag, 0, 255, Core.NORM_MINMAX);
			//Show the image
			showImageInJFrame(sMag);	
		}
		
		//Perform DFT on modImage store in itself
		//The flags are used so that the DFT function can be inversed
		Core.dft(modImage, modImage, Core.DFT_SCALE | Core.DFT_COMPLEX_OUTPUT,0);
		
		//Split the DFT result into real and imaginary parts
		ArrayList<Mat> planes = new ArrayList<Mat>();
		Core.split(modImage, planes);
		
		Mat real = planes.get(0);
		Mat imag = planes.get(1);
		Mat mag = new Mat(real.size(), real.type());
		Mat ang = Mat.zeros(real.size(), real.type());
		//Calculate the magnitude - have to use this function as Core.magnitude is reversible
		Core.cartToPolar(real, imag, mag, ang);
		//Multiply spectrum with the frequency image
		mag = directMultiplication(mag, filterImage);
		//Reverse the magnitude calculation
		Core.polarToCart(mag, ang, real, imag);
		
		//Merge real and imaginary parts back together
		planes.set(0,real);
		planes.set(1,imag);
		Core.merge(planes,modImage);
		
		//Inverse the DFT function
		Core.dft(modImage, modImage, Core.DFT_INVERSE | Core.DFT_REAL_OUTPUT,0);

		if (showFrequencySpectrumAfter) {
			Mat spectrum = modImage.clone();
			ArrayList<Mat> spectrumPlanes = new ArrayList<Mat>();
			spectrumPlanes.add(spectrum);
			spectrumPlanes.add(Mat.zeros(spectrum.size(),5));
			Core.merge(spectrumPlanes, spectrum);
			Core.dft(spectrum, spectrum);
			spectrumPlanes.clear();
			Core.split(spectrum, spectrumPlanes);
			Mat sReal = spectrumPlanes.get(0);
			Mat sImag = spectrumPlanes.get(1);
			Core.magnitude(sReal, sImag, sReal);
			Mat sMag = sReal;
			addScalar(sMag,1);
			Core.log(sMag,sMag);
			swapQuadrants(sMag);
			Core.normalize(sMag,sMag, 0, 255, Core.NORM_MINMAX);
			showImageInJFrame(sMag);	
		}
	}
	
	/**
	 * Swaps the quadrants of the images around
	 * Top left corner <-> Bottom right corner
	 * Top right corner <-> Bottom left corner
	 * @param sMag
	 * @return
	 */
	private static Mat swapQuadrants(Mat sMag) {
		int cx = sMag.cols()/2;
	    int cy = sMag.rows()/2;
	    Mat q0 = new Mat(sMag, new Rect(0, 0, cx, cy+1)); 
	    Mat q1 = new Mat(sMag, new Rect(cx, 0, cx, cy));  
	    Mat q2 = new Mat(sMag, new Rect(0, cy, cx, cy));  
	    Mat q3 = new Mat(sMag, new Rect(cx, cy, cx, cy+1));
	    Mat tmp = new Mat(q0.size(), q0.type());         
	    q0.copyTo(tmp);
	    q3.copyTo(q0);
	    tmp.copyTo(q3);

	    q1.copyTo(tmp);      
	    q2.copyTo(q1);
	    tmp.copyTo(q2);
	    return sMag;
	}
	
	/**
	 * Adds scalar to add the values in the matrix
	 * @param m
	 * @param scalar
	 */
	private static void addScalar(Mat m, final double scalar) {
		for (int y = 0; y < m.rows(); y++)
			for (int x = 0; x < m.cols(); x++) 
				m.put(y,x, new double[] { m.get(y,x)[0] + scalar } );
	}
	
	/**
	 * Spatial domain transforms
	 * @param maskWidth
	 * @param maskHeight
	 * @param sdm
	 */
	public void spatialDomain(int maskWidth, int maskHeight, SpatialDomainMode sdm) {
		
		for (int x = 0; x < modImage.width(); x ++)
			for (int y = 0; y < modImage.height(); y ++) {
				//Stop mask going out of image bounds
				if (x + maskWidth > modImage.width() || y + maskHeight > modImage.height())
					continue;
				
				Mat mask = modImage.submat(new Rect(x,y, maskWidth, maskHeight));
				int val = 0;
				if (sdm == SpatialDomainMode.AVERAGE) {
					val = getAverageValue(mask);
					setAllValues(mask, val);
				}
				else if (sdm == SpatialDomainMode.MEDIAN){
					val = getMedianValue(mask);
					//Set center
					mask.put(maskHeight/2, maskWidth/2, new double[] { val });
				} else if (sdm == SpatialDomainMode.AVERAGECENTER) {
					val = getAverageValue(mask);
					mask.put(maskHeight/2, maskWidth/2, new double[] { val });
				}
			}
		
	}
	
	//Subtract the blurred image from the unblurred image
		//Unsharpen mask
		public void sharpen(float sigmaX) {
			Mat blurred = modImage.clone();
			Imgproc.GaussianBlur(modImage, blurred, new Size(0,0), sigmaX);
			org.opencv.core.Core.addWeighted(modImage, 1.5, blurred, -0.5,0, modImage);
		}
	
	private static int getMedianValue(Mat mask) {
		double[] arr = new double[mask.width()*mask.height()];
		for (int x = 0; x < mask.width(); x++)
			for (int y = 0; y < mask.height(); y++)
				arr[y*mask.width() + x] = mask.get(y, x)[0];
		return getMedian(arr);
	}
	
	private static int getMedian(double[] arr) {
		Arrays.sort(arr);
		if (arr.length % 2 == 0)
			return (int)arr[arr.length/2];
		else
			return (int)((arr[arr.length/2 - 1] + arr[arr.length/2])/2);
	}
	
	private static int getAverageValue(Mat mask) {
		double total = 0;
		for (int x = 0; x < mask.width(); x++)
			for (int y = 0; y < mask.height(); y++)
				total += mask.get(y, x)[0];
		return (int)(total/(mask.width()*mask.height()));
	}
	
	private static void setAllValues(Mat mask, int value) {
		for (int x = 0; x < mask.width(); x++)
			for (int y = 0; y < mask.height(); y++)
				mask.put(y, x, new double[] { value });		
	}
	
	private static double getPixelDistance(double[] i1, double[] i2) {
		double error = (i1[0] - i2[0]) * (i1[0] - i2[0]);
		if (error > 0 && VERBOSE)
			System.out.println("Error of " + error + " at [" + i1[0]+"] [" +  i2[0] +"]");
		return error;
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

	public Mat getOriginalMatrix() {
		return image;
	}
	
	public Mat getModifiedMatrix() {
		return modImage;
	}
	
	public int getHeight() {
		return modImage.rows();
	}
	
	public int getWidth() {
		return modImage.cols();
	}
}