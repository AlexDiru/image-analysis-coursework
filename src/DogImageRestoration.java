
public class DogImageRestoration {
	public static void run() {
		//Load images
		OpenCVImage dogOriginal = new OpenCVImage("C:/Users/Alex/Desktop/dogOriginal.bmp");
		OpenCVImage dogDistorted = new OpenCVImage("C:/Users/Alex/Desktop/dogDistorted.bmp");
		
		System.out.println("Original MSE: " + OpenCVImage.averageMeanSquareErrors(dogOriginal.getOriginalMatrix(), dogDistorted.getOriginalMatrix()));
		
	}
}
