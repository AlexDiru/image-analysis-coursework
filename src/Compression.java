import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class Compression {
	
	/**
	 * GUI
	 */
	private GUI gui;
	
	/**
	 * Uncompressed image
	 */
	private Mat originalImage;
	
	/**
	 * Compressed image (what is saved to file)
	 */
	private Mat image;
	
	/**
	 * String representation of compressed image
	 */
	private String data;
	
	/**
	 * Byte representation of compressed image
	 */
	private byte[] dataBytes;
	
	public Compression(GUI gui) {
		this.gui = gui;
	}
	
	/**
	 * Loads an uncompressed image (i.e. bitmap)
	 * @param filePath
	 */
	public void loadUncompressed(String filePath) {
		image = Highgui.imread(filePath,Highgui.IMREAD_COLOR);
		originalImage = image.clone();
	}
	
	/**
	 * Manually sets the image to be compressed
	 * @param image
	 */
	public void setImage(Mat image) {
		this.image = image;
	}
	
	/**
	 * Compresses the image
	 * @param pixelPerfect Lossy or lossless compression
	 * @param dctRatio Quantisation scale value (-1 means no quantisation matrix used)
	 */
	public void compress(boolean pixelPerfect, double dctRatio) {
		image = originalImage.clone();
		
		if (!pixelPerfect)
			image = DCT.encode(originalImage, dctRatio);
		else {
			dctRatio = -1;
			image = DCT.encode(originalImage, dctRatio);
		}

		System.out.println("Compressed DCT");
		
		data = dctRatio + "," + RunLengthCoding.encode(image, RunLengthDirection.HORIZONTAL);
		System.out.println("Compressed Run Length");
		
		data = HuffmanCoding.encode(data);
		System.out.println("Compressed Huffman");
		
		dataBytes = InfDef.encode(data);
		System.out.println("Compressed ZLIB");
	}
	
	/**
	 * Compression algorithm A1 (in report)
	 */
	public void a1compress() {
		image = originalImage.clone();
		image = DCT.encode(image, 50);
		data = 50 + "," + RunLengthCoding.encode(image, RunLengthDirection.HORIZONTAL);
		data = HuffmanCoding.encode(data);
		dataBytes = InfDef.encode(data);
	}
	
	/**
	 * Compression algorithm A2 (in report)
	 */
	public void a2compress() {
		image = originalImage.clone();
		image = ThresholdPixel.compress(image, 100);
		data = RunLengthCoding.encode(image, RunLengthDirection.HORIZONTAL);
		data = HuffmanCoding.encode(data);
		dataBytes = InfDef.encode(data);
	}
	
	/**
	 * Compression algorithm A3 (in report)
	 */
	public void a3compress() {
		image = originalImage.clone();
		image = DCT.encode(image, 50);
		data = 50 + "," + RunLengthCoding.encode(image, RunLengthDirection.HORIZONTAL);
		dataBytes = InfDef.encode(data);
	}
	
	/**
	 * Compression algorithm A4 (in report)
	 */
	public void a4compress() {
		image = originalImage.clone();
		image = DCT.encode(image, -1);
		data = RunLengthCoding.encode(image, RunLengthDirection.HORIZONTAL);
		data = HuffmanCoding.encode(data);
		dataBytes = InfDef.encode(data);
	}

	/**
	 * Compression algorithm A5 (in report)
	 */
	public void a5compress() {
		image = originalImage.clone();
		image = DCT.encode(image, -1);
		data = RunLengthCoding.encode(image, RunLengthDirection.HORIZONTAL);
		dataBytes = InfDef.encode(data);
	}

	public void decompress() {
		data = InfDef.decode(dataBytes);
		System.out.println("Decompressed ZLIB");
		
		data = HuffmanCoding.decode(data);
		System.out.println("Decompressed Huffman");
		
		//Get DCT ratio
		String dctRatioStr = data.split(",")[0];
		double dctRatio = Double.parseDouble(dctRatioStr);
		data = data.substring(dctRatioStr.length() + 1, data.length());
		
		image = RunLengthCoding.decodeRGB(data, RunLengthDirection.HORIZONTAL);
		System.out.println("Decompressed Run Length");
		
		
		image = DCT.decode(image,dctRatio);
		System.out.println("Decompressed DCT");
		
		showImageInJFrame(image);
	}
	
	/**
	 * Show an image in a JFrame so the user can see it
	 * @param image
	 */
	private static void showImageInJFrame(final Mat image) {
		showImageInJFrame(image, image.width(), image.height());
	}
	
	/**
	 * Code modified from http://stackoverflow.com/questions/16494916/equivalent-method-for-imshow-in-opencv-java-build
	 * Shows an image in a JFrame
	 * @param image Image to show
	 * @param xSize Image width
	 * @param ySize Image height
	 */
	private static void showImageInJFrame(final Mat image, int xSize, int ySize) {
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
	
	/**
	 * Saves the image to a file without compression
	 * @param filePath
	 */
	public void saveOriginalTo(String filePath) {
		StringBuilder data = new StringBuilder();
		data.append(originalImage.rows()).append("x").append(originalImage.cols()).append("\n");
		for (int y = 0; y < originalImage.rows(); y++)
			for (int x = 0; x < originalImage.cols(); x++)
				data.append(originalImage.get(y, x)[0]).append(";");
		FileHelper.writeAllLines(filePath, Arrays.asList(data.toString()));
	}
	
	/**
	 * Gets the compression ratio between the original image and compressed image
	 * @return
	 */
	public String getCompressionDetails() {
		try {
			//Save both images temporarily
			//Save original image
			String tempPath = "C:/Users/Alex/Desktop/temp_temp_temp_temp.txt";
			saveOriginalTo(tempPath);
			long originalSize = FileHelper.getFileSize(tempPath);
			new File(tempPath).delete();
			
			//Save cmopress image
			saveTo(tempPath);
			long compressedSize = FileHelper.getFileSize(tempPath);
			new File(tempPath).delete();
			
			//Html formatted data for the JLabel
			return "<html><body>Original Size: " + originalSize + "<br>Compressed Size: " + compressedSize + "<br>Ratio: " + ((float)(compressedSize)/originalSize) + "</body></html>";
		} catch (NullPointerException ex) {
			return "Compress the image first";
		}
	}

	/**
	 * Saves the compressed data to a specified file
	 * @param filePath
	 */
	public void saveTo(String filePath) {
		FileHelper.writeBytes(filePath, dataBytes);
	}

	/**
	 * Loads the compressed data from a file
	 * @param filePath
	 */
	public void loadCompressed(String filePath) {
		dataBytes = FileHelper.readBytes(filePath);
	}

	/**
	 * Returns the compressed image
	 * @return The compressed image
	 */
	public Mat getImage() {
		return image;
	}

	/**
	 * Decompresses a compressed image and displays it in the GUI
	 * @param gui
	 * @param cmpFile
	 * @return
	 */
	public static Compression decompressFile(GUI gui, String cmpFile) {
		Compression c = new Compression(gui);
		c.loadCompressed(cmpFile);
		c.decompress();
		return c;
	}

	public void showUncompressedImage() {
		Compression.showImageInJFrame(image);
	}
}
