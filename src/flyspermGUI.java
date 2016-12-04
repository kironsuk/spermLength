import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

public class flyspermGUI {

	private JFrame frame;
	private BufferedImage img;
	private BufferedImage originalImg;
	private JFileChooser fileChooser;
	private JLabel imgPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					flyspermGUI window = new flyspermGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public flyspermGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		fileChooser = new JFileChooser();
		
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		
		
		JButton btnLoadImage = new JButton("Load Image");
		panel.add(btnLoadImage);
		btnLoadImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImageAction(evt);
            }
        });
		
		JButton btnFindLength = new JButton("Find Length");
		panel.add(btnFindLength);
		btnFindLength.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findLengthAction(evt);
            }
        });
		
		imgPane = new JLabel();
		frame.getContentPane().add(imgPane, BorderLayout.CENTER);
		
		
	}
	
	private void loadImageAction(ActionEvent evt){
        int returnVal = fileChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // instantiate the cell image
            //imageProcessor c = imageProcessor.getCellImageFromFile(file.getAbsolutePath());
            try {
                originalImg = ImageIO.read(file);
                img = getScaledImage(originalImg,600,600);
                } 
            catch (IOException ex) {
                System.out.println("problem accessing file"+
                        file.getAbsolutePath());
                }
            drawImg();
        } 
        else {
            System.out.println("File access cancelled by user.");
        }
		
	}
	
	private void drawImg(){
		ImageIcon icon2 = new ImageIcon(getScaledImage(img,600,600)); 
        imgPane.setIcon(icon2);
        imgPane.setHorizontalAlignment(SwingConstants.CENTER);
        imgPane.setVerticalAlignment(SwingConstants.CENTER);
        imgPane.setVisible(true);
	}
	
	private BufferedImage getScaledImage(BufferedImage srcImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	private void findLengthAction (ActionEvent evt){
		imageProcessor ip = new imageProcessor(originalImg);
		System.out.println(ip.getCellLength());
		img = ip.array2Img();
		drawImg();
	}

}
