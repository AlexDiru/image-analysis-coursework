package imageenchancement;



public class DogImageRestoration {
	public static void run() {
		enhance();
	}
	
	/**
	 * Tests for the best low pass values
	 */
	private static void testBestLowPass() {
		OpenCVGSImage dogOriginal = new OpenCVGSImage("C:/Users/Alex/Desktop/dogOriginal.bmp");
		
		int maxRadius = dogOriginal.getHeight() < dogOriginal.getWidth() ? dogOriginal.getHeight() : dogOriginal.getWidth();
		maxRadius--;
		maxRadius /= 2;
		
		double minLowPass = Double.MAX_VALUE;
		int minRadius = -1;
		
		for (int r = 0; r < maxRadius; r++) {
			OpenCVGSImage dogDistorted = new OpenCVGSImage("C:/Users/Alex/Desktop/dogDistorted.bmp");
			dogDistorted.lowPass(r);
			double mse = OpenCVGSImage.averageMeanSquareErrors(dogOriginal.getOriginalMatrix(), dogDistorted.getModifiedMatrix());
			
			if (mse < minLowPass) {
				minLowPass = mse;
				minRadius = r;
			}
		}
		
		System.out.println("Best Low Pass Radius is " + minRadius + " at " + minLowPass);
	}
	
	/**
	 * Tests for the best high pass values
	 */
	private static void testBestHighPass() {
		OpenCVGSImage dogOriginal = new OpenCVGSImage("C:/Users/Alex/Desktop/dogOriginal.bmp");
		
		int maxRadius = dogOriginal.getHeight() < dogOriginal.getWidth() ? dogOriginal.getHeight() : dogOriginal.getWidth();
		maxRadius--;
		maxRadius /= 2;
		
		double minLowPass = Double.MAX_VALUE;
		int minRadius = -1;
		
		for (int r = 0; r < maxRadius; r++) {
			OpenCVGSImage dogDistorted = new OpenCVGSImage("C:/Users/Alex/Desktop/dogDistorted.bmp");
			dogDistorted.highPass(r);
			double mse = OpenCVGSImage.averageMeanSquareErrors(dogOriginal.getOriginalMatrix(), dogDistorted.getModifiedMatrix());
			
			if (mse < minLowPass) {
				minLowPass = mse;
				minRadius = r;
			}
		}
		
		System.out.println("Best High Pass Radius is " + minRadius + " at " + minLowPass);
	}
	
	/**
	 * Tests for the best band pass values
	 */
	private static void testBestBandPass() {
		OpenCVGSImage dogOriginal = new OpenCVGSImage("C:/Users/Alex/Desktop/dogOriginal.bmp");
		
		int maxRadius = dogOriginal.getHeight() < dogOriginal.getWidth() ? dogOriginal.getHeight() : dogOriginal.getWidth();
		maxRadius--;
		maxRadius /= 2;
		
		double minLowPass = Double.MAX_VALUE;
		int minRadius1 = -1;
		int minRadius2 = -1;
		
		for (int r1 = 0; r1 < maxRadius; r1++) {
			for (int r2 = 0; r2 < r1; r2++) {
				OpenCVGSImage dogDistorted = new OpenCVGSImage("C:/Users/Alex/Desktop/dogDistorted.bmp");
				dogDistorted.bandPass(r1,r2);
				double mse = OpenCVGSImage.averageMeanSquareErrors(dogOriginal.getOriginalMatrix(), dogDistorted.getModifiedMatrix());
				
				if (mse < minLowPass) {
					minLowPass = mse;
					minRadius1 = r1;
					minRadius2 = r2;
				}
			}
		}
		
		System.out.println("Best Band Pass Radius is " + minRadius1 + " and  " + minRadius2 + " at " + minLowPass);
	}
	
	/**
	 * Run the enhancement algorithms
	 */
	private static void enhance() {
		
		//testBestLowPass();
		//testBestHighPass();
		//testBestBandPass();
		
		boolean useLowPass = false;
		
		OpenCVGSImage dogOriginal = new OpenCVGSImage("C:/Users/Alex/Desktop/dogOriginal.bmp");
		OpenCVGSImage dogDistorted2 = new OpenCVGSImage("C:/Users/Alex/Desktop/dogDistorted.bmp");

		if (useLowPass) {
			dogDistorted2.lowPass(53);
			dogDistorted2.powerTransformation(0.09999998,1.733);
			dogDistorted2.spatialDomain(3, 3, OpenCVGSImage.SpatialDomainMode.AVERAGECENTER); 
			dogDistorted2.spatialDomain(3, 3, OpenCVGSImage.SpatialDomainMode.MEDIAN);
		} else {
			dogDistorted2.sharpen(4f); //Reduces errors
			dogDistorted2.spatialDomain(3, 3, OpenCVGSImage.SpatialDomainMode.AVERAGE); 
			dogDistorted2.spatialDomain(3, 3, OpenCVGSImage.SpatialDomainMode.MEDIAN);
			dogDistorted2.powerTransformation(0.1,1.8);
			dogDistorted2.spatialDomain(2, 2, OpenCVGSImage.SpatialDomainMode.AVERAGE); 
			dogDistorted2.spatialDomain(3, 3, OpenCVGSImage.SpatialDomainMode.MEDIAN);
			dogDistorted2.sharpen(4f); 
		}
		
		//dogDistorted2.showOriginalImage();
		//dogDistorted2.showModifiedImage();
		//dogOriginal.showOriginalImage();
		
		System.out.println(OpenCVGSImage.averageMeanSquareErrors(dogOriginal.getOriginalMatrix(), dogDistorted2.getOriginalMatrix()));
		System.out.println(OpenCVGSImage.averageMeanSquareErrors(dogOriginal.getOriginalMatrix(), dogDistorted2.getModifiedMatrix()));
		
		//Colour the image 
		OpenCVImage colour = new OpenCVImage(dogDistorted2);
		colour.showOriginalImage();
		colour.addColour();
		colour.showModifiedImage();
		
	}
}
