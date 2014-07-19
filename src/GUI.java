

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.JTextField;

public class GUI extends JFrame {

	private JPanel contentPane;
	private JPanel panelCompressed;
	private ImageIcon uncompressedImageIcon;
	private JPanel panelUncompressed;
	private JButton btnLoadImage;
	private Compression compression;
	private JButton btnSaveCompressedImage;
	private JButton btnNewButton;
	private JLabel lblCompressionRatio;
	private JButton btnCompressLossy;
	private JLabel lblNewLabel;
	private JTextField txtDCTRatio;
	private JButton btnA5;

	/**
	 * Create the frame.
	 */
	public GUI() {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}  catch (Exception e) {
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 920, 578);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		panelUncompressed = new JPanel();
		panelUncompressed.setBounds(10, 165, 356, 364);
		uncompressedImageIcon = new ImageIcon();
		panelUncompressed.add(new JLabel(uncompressedImageIcon));
		contentPane.add(panelUncompressed);
		
		panelCompressed = new JPanel();
		panelCompressed.setBounds(538, 165, 356, 364);
		contentPane.add(panelCompressed);
		
		JButton btnLoadCompressedImage = new JButton("Load Compressed Image");
		final GUI gui = this;
		btnLoadCompressedImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setCurrentDirectory(new File("C:/Users/Alex/Desktop"));
						if (fileChooser.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
							Compression.decompressFile(gui, fileChooser.getSelectedFile().toString());
						}
					}
				}).start();
			}
		});
		btnLoadCompressedImage.setBounds(10, 11, 175, 23);
		contentPane.add(btnLoadCompressedImage);
		
		btnLoadImage = new JButton("Load Image");
		btnLoadImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setCurrentDirectory(new File("C:/Users/Alex/Desktop"));
						if (fileChooser.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
							compression = new Compression(gui);
							compression.loadUncompressed(fileChooser.getSelectedFile().toString());
						}
					
				
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								panelUncompressed.removeAll();
								panelUncompressed.add(new JLabel(createImageIcon(compression.getImage())));
								compression.showUncompressedImage();
								gui.revalidate();
							}
						});
					}
				}).start();
			}
		});
		btnLoadImage.setBounds(10, 45, 175, 23);
		contentPane.add(btnLoadImage);
		
		JButton btnCompressPixelPerfect = new JButton("Compress Lossless");
		btnCompressPixelPerfect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						compression.compress(true, -1);
						showCompressedImage();
					}
				}).start();
			}
		});
		btnCompressPixelPerfect.setBounds(376, 184, 152, 63);
		contentPane.add(btnCompressPixelPerfect);
		
		btnSaveCompressedImage = new JButton("Save Compressed Image");
		btnSaveCompressedImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setCurrentDirectory(new File("C:/Users/Alex/Desktop"));
						if (fileChooser.showSaveDialog(gui) == JFileChooser.APPROVE_OPTION) {
							compression.saveTo(fileChooser.getSelectedFile().toString() + ".alex");
						}
					}
				}).start();
			}
		});
		btnSaveCompressedImage.setBounds(646, 11, 175, 23);
		contentPane.add(btnSaveCompressedImage);
		
		btnNewButton = new JButton("Get Compression Ratio");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						final String compressionDetails = compression.getCompressionDetails();

						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								lblCompressionRatio.setText(compressionDetails);
							}
						});
					}
				}).start();
			}
		});
		btnNewButton.setBounds(376, 418, 152, 23);
		contentPane.add(btnNewButton);
		
		lblCompressionRatio = new JLabel("");
		lblCompressionRatio.setBounds(376, 460, 152, 69);
		contentPane.add(lblCompressionRatio);
		
		
		//**** EASY TESTING

		
		compression = new Compression(this);
		compression.loadUncompressed("C:/Users/Alex/Desktop/catSmall.jpg");
		panelUncompressed.removeAll();
		Mat m = compression.getImage();
		//m = Compression.dct(m, 50);
		//m = Compression.dctInverse(m);
		panelUncompressed.add(new JLabel(createImageIcon(m)));
		
		btnCompressLossy = new JButton("Compress Lossy");
		btnCompressLossy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						compression.compress(false, Double.parseDouble(txtDCTRatio.getText()));
						showCompressedImage();
					}
				}).start();
			}
		});
		btnCompressLossy.setBounds(376, 254, 152, 63);
		contentPane.add(btnCompressLossy);
		
		lblNewLabel = new JLabel("DCT Ratio:");
		lblNewLabel.setBounds(376, 322, 52, 14);
		contentPane.add(lblNewLabel);
		
		txtDCTRatio = new JTextField();
		txtDCTRatio.setText("50");
		txtDCTRatio.setBounds(442, 319, 86, 20);
		contentPane.add(txtDCTRatio);
		txtDCTRatio.setColumns(10);
		
		JButton btnA1 = new JButton("A1");
		btnA1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runCompressionAlgorithm(1);
			}
		});
		btnA1.setBounds(277, 11, 45, 23);
		contentPane.add(btnA1);
		
		JButton btnA2 = new JButton("A2");
		btnA2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runCompressionAlgorithm(2);
			}
		});
		btnA2.setBounds(277, 45, 45, 23);
		contentPane.add(btnA2);
		
		JButton btnA3 = new JButton("A3");
		btnA3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runCompressionAlgorithm(3);
			}
		});
		btnA3.setBounds(332, 11, 45, 23);
		contentPane.add(btnA3);
		
		JButton btnA4 = new JButton("A4");
		btnA4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runCompressionAlgorithm(4);
			}
		});
		btnA4.setBounds(332, 45, 45, 23);
		contentPane.add(btnA4);
		
		btnA5 = new JButton("A5");
		btnA5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runCompressionAlgorithm(5);
			}
		});
		btnA5.setBounds(387, 11, 45, 23);
		contentPane.add(btnA5);
	}
	
	/**
	 * Shows the compressed image on the GUI
	 */
	private void showCompressedImage() {
		final GUI gui = this;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				panelCompressed.removeAll();
				panelCompressed.add(new JLabel(createImageIcon(compression.getImage())));
				gui.revalidate();
			}
		});
	}
	
	/**
	 * Runs a compression algorithm specified in the report
	 * @param index Which compression algorithm to use 1 - 5
	 */
	private void runCompressionAlgorithm(final int index) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (index == 1)
					compression.a1compress();
				else if (index == 2)
					compression.a2compress();
				else if (index == 3)
					compression.a3compress();
				else if (index == 4)
					compression.a4compress();
				else if (index == 5)
					compression.a5compress();
				showCompressedImage();
			}
		}).start();	
	}
	
	/**
	 * Creates and image icon from an OpenCV matrix
	 * @param image
	 * @return
	 */
	public static ImageIcon createImageIcon(final Mat image) {
		//Resize the image
		Imgproc.resize(image, image, new Size(image.cols(), image.rows()));
		
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
        return new ImageIcon(bufImage);
	}

	public JPanel getCompressedImagePanel() {
		return panelCompressed;
	}
	
	public JPanel getUncompressedImagePanel() {
		return panelUncompressed;
	}
	
	public ImageIcon getUncompressedImageIcon() {
		return uncompressedImageIcon;
	}
}
