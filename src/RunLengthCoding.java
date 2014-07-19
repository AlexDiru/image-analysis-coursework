import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;


public class RunLengthCoding {
	
	/**
	 * Encodes a RGB image
	 * @param list The list of all the pixels at run length 1
	 * @return List of all the pixels at various run lengths
	 */
	private static List<PixelData> encodeRGB(final List<PixelData> list) {
		List<PixelData> newList = new ArrayList<PixelData>();
		Iterator<PixelData> iter = list.iterator();
		PixelData current = iter.next();
		newList.add(current);
	
		while (iter.hasNext()) {
			PixelData pd = iter.next();
			if (current.equals(pd)) {
				current.increaseRunLength();
			} else {
				current = pd;
				newList.add(current);
			}
		}
		return newList;
	}
	
	/**
	 * Encodes a grayscale image
	 * @param list The list of all the pixels at run length 1
	 * @return List of all the pixels at various run lengths
	 */
	private static List<PixelDataGrayscale> encodeGrayscale(List<PixelDataGrayscale> list) {
		for (int i = 0; i < list.size(); i++)
			if (i < list.size() - 1)
				if (list.get(i).equals(list.get(i+1))) {
					list.get(i).increaseRunLength();
					list.remove(i + 1);
					i--;
				}
		return list;
	}
	
	/**
	 * Encodes an image
	 * @param image The image to encode
	 * @return The encoded data
	 */
	public static String encode(Mat image, RunLengthDirection direction) {
		//Differentiate between encoding grayscale and RGB images
		if (image.channels() == 1)
			return pixelDataToStringGrayscale(image,encodeGrayscale(PixelDataGrayscale.readImage(image)));
		else if (image.channels() == 3)
			return pixelDataToStringRGB(image, encodeRGB(PixelData.readImage(image, direction)));
		return null;
	}
	
	/**
	 * Converts a list of pixels including their various run lengths to a string of data
	 * @param image The image the pixel data is composed from
	 * @param pixelData The list of pixel datawith various run lengths
	 * @return The text representation of the pixel data
	 */
	private static String pixelDataToStringRGB(Mat image, List<PixelData> pixelData) {
		StringBuilder data = new StringBuilder();
		data.append(image.rows()).append("x").append(image.cols()).append("\n");
		for (PixelData pd : pixelData)
			data.append(pd.toString());
		return data.toString();
	}
	
	private static String pixelDataToStringGrayscale(Mat image,List<PixelDataGrayscale> pixelData) {
		StringBuilder data = new StringBuilder();
		data.append(image.rows()).append("x").append(image.cols()).append("\n");
		for (PixelDataGrayscale pd : pixelData)
			data.append(pd.toString());
		return data.toString();
	}
	
	/**
	 * Decodes the compressed string to extract the run lengths then decompresses that into an OpenCV matrix
	 * @param source The compressed string
	 * @param direction Direction of run length
	 * @return The created image
	 */
    public static Mat decodeRGB(String source, RunLengthDirection direction) {
    	try {
    		String[] lines = source.split("\n");
    		int xSize = Integer.parseInt(lines[0].split("x")[0]);
    		int ySize = Integer.parseInt(lines[0].split("x")[1]);
    		
    		String[] entries = lines[1].split(";");
    		List<PixelData> list = new ArrayList<PixelData>();
    		for (String entry : entries) {
    			//Parse the left numbers as the run length
    			int runLengthEndIndex = 0;
    			for (int i = 0; i < entry.length(); i++)
    				if (Character.isLetter(entry.charAt(i)) || entry.charAt(i) == '-')
    					break;
    				else
    					runLengthEndIndex++;
    			
    			int runLength = Integer.parseInt(entry.substring(0, runLengthEndIndex));
    			String[] pixelValues = StringManip.replaceLettersWithNumbers(entry.substring(runLengthEndIndex)).split(",");
    			
    			try {
	    			int red = Integer.parseInt(pixelValues[0]);
	    			int green = Integer.parseInt(pixelValues[1]);
	    			int blue = Integer.parseInt(pixelValues[2]);
	    			
	    			list.add(new PixelData(red, green, blue, runLength));
    			} catch (ArrayIndexOutOfBoundsException ex) {
    				ex.printStackTrace();
    				System.out.println(entry.substring(runLengthEndIndex));
    			}
    		}
    		
    		//Expand all run lengths with new items
    		List<PixelData> newList = new ArrayList<PixelData>();
    		for (int i = 0; i < list.size(); i++)
    			if (list.get(i).getRunLength() == 1) {
    				newList.add(list.get(i));
    			}else if (list.get(i).getRunLength() > 1) {
    				for (int r = 0; r < list.get(i).getRunLength(); r++)
    					newList.add(new PixelData(list.get(i).getRed(), list.get(i).getGreen(), list.get(i).getBlue()));
    			} 

    		System.out.println(list.size() + " " + newList.size());
    		list = newList;
    					
    		//Remake matrix
    		Mat image = new Mat(new Size(ySize, xSize), CvType.CV_32FC3);
    		int y = 0;
    		int x = 0;
    		
    		if (direction == RunLengthDirection.HORIZONTAL) {
	    		while (y < image.rows()) {
	    			PixelData pixel = list.get(y * image.cols() + x);
	    			image.put(y,x, new double[] { pixel.getRed(), pixel.getGreen(), pixel.getBlue()});
	    			
	    			x++;
	    			if (x >= image.cols()) {
	    				x = 0;
	    				y++;
	    			}
	    		}
    		} else if (direction == RunLengthDirection.VERTICAL) {
    			while (x < image.cols()) {
	    			PixelData pixel = list.get(x * image.rows() + y);
	    			image.put(y,x, new double[] { pixel.getRed(), pixel.getGreen(), pixel.getBlue()});
	    			
	    			y++;
	    			if (y >= image.rows()) {
	    				y = 0;
	    				x++;
	    			}
	    		}
    		}

    		return image;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
		return null;
    }
	
    public static Mat decodeGrayscale(String source) {
    	try {
    		String[] lines = source.split("\n");
    		int xSize = Integer.parseInt(lines[0].split("x")[0]);
    		int ySize = Integer.parseInt(lines[0].split("x")[1]);
    		
    		String[] entries = lines[1].split(";");
    		List<PixelDataGrayscale> list = new ArrayList<PixelDataGrayscale>();
    		for (String entry : entries) {
    			//Parse the left numbers as the run length
    			int runLengthEndIndex = 0;
    			for (int i = 0; i < entry.length(); i++)
    				if (Character.isLetter(entry.charAt(i)))
    					break;
    				else
    					runLengthEndIndex++;
    			
    			int runLength = Integer.parseInt(entry.substring(0, runLengthEndIndex));
    			int pixelValue = Integer.parseInt(StringManip.replaceLettersWithNumbers(entry.substring(runLengthEndIndex)));
    			
    			list.add(new PixelDataGrayscale(pixelValue, runLength));
    		}
    		
    		//Expand all run lengths with new items
    		for (int i = 0; i < list.size(); i++)
    			if (list.get(i).getRunLength() > 1) {
    				for (int r = 0; r < list.get(i).getRunLength() - 1; r++)
    					list.add(i + 1, new PixelDataGrayscale(list.get(i).getValue()));
    				i += list.get(i).getRunLength() - 1;
    			}
    					
    		//Remake matrix
    		Mat image = new Mat(new Size(ySize, xSize), 0);
    		int y = 0;
    		int x = 0;
    		while (y < image.rows()) {
    			image.put(y,x, new double[] { list.get(y * image.cols() + x).getValue() });
    			
    			x++;
    			if (x >= image.cols()) {
    				x = 0;
    				y++;
    			}
    		}

    		return image;
    	} catch (Exception ex) {
    		System.out.println("cannot decode corrupt data");
    	}
		return null;
    }
}
