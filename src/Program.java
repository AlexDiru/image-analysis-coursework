import imageenchancement.DogImageRestoration;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.opencv.core.Core;

public class Program {
	public static void main(String[] args) {
		//Load the OpenCV library 
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
		//imgenh();
    	imgcmp();
		
	}
	
	/**
	 * Image compression assignment
	 */
	public static void imgcmp() {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {	
                GUI ex = new GUI();
                ex.setVisible(true);
            }
        });
	}
	
	/**
	 * Image enhancement assignment
	 */
	public static void imgenh() {
		DogImageRestoration.run();
	}
}
