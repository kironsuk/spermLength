import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dimension;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class flyspermGUI {

	private JFrame frame;
	private BufferedImage originalImg;
	private ArrayList<BufferedImage> imgs;
	private ArrayList<String> titles;
	private JLabel title;
	private double calculatedLength;
	private JLabel lengthDisp;
	private int numImages;
	private int currIndex;
	private JFileChooser fileChooser;
	private JLabel imgPane;
	private JButton btnFindLength;
	private JButton btnFindLengthMan;
	private JButton nextImage;
	private JButton prevImage;
	private JButton saveImage;

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
		frame.setBounds(50, 50, 1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		//Top Panel
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();

		
		
		JButton btnLoadImage = new JButton("Load Image");;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(btnLoadImage, c);
		btnLoadImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImageAction(evt);
            }
        });
		
		
		btnFindLength = new JButton("Find Length");
		c.gridx = 1;
		c.gridy = 0;
		panel.add(btnFindLength, c);
		btnFindLength.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findLengthAction(evt);
            }
        });
		btnFindLength.setEnabled(false);
		
		btnFindLengthMan = new JButton("Find Length Manual");
		c.gridx = 2;
		c.gridy = 0;
		panel.add(btnFindLengthMan, c);
		btnFindLengthMan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findLengthActionMan(evt);
            }
        });
		btnFindLengthMan.setEnabled(false);
		
		
		title = new JLabel();
		//panel.add(lblTitle);
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 2;
		panel.add(title,c);
		
		
		
		
		
		
		
		JPanel spanel = new JPanel();
		frame.getContentPane().add(spanel, BorderLayout.SOUTH);
		spanel.setLayout(new GridBagLayout());
		
		
		nextImage = new JButton("Next");
		spanel.add(nextImage);
		nextImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextImage(evt);
            }
        });
		c.gridwidth=1;
		c.gridx = 2;
		c.gridy = 1;
		spanel.add(nextImage,c);
		nextImage.setEnabled(false);
		
		
		prevImage = new JButton("Previous");
		spanel.add(prevImage);
		prevImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevImage(evt);
            }
        });
		c.gridx = 0;
		c.gridy = 1;
		spanel.add(prevImage,c);
		prevImage.setEnabled(false);
		
		saveImage = new JButton("Save Image");
		spanel.add(saveImage);
		saveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //saveImage(evt);
            }
        });
		c.gridx = 1;
		c.gridy = 1;
		spanel.add(saveImage,c);
		saveImage.setEnabled(false);
		
		lengthDisp = new JLabel("Sperm Length:");
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		spanel.add(lengthDisp,c);
		
		
		
		imgPane = new JLabel();;
		frame.getContentPane().add(imgPane, BorderLayout.CENTER);
        imgPane.setVisible(true);
		
		
	}
	
	private void findLengthActionMan(ActionEvent evt){
			System.out.println("do I even get here?");
	        JTextField field1 = new JTextField("5");
	        JTextField field2 = new JTextField("5");
	        JTextField field3 = new JTextField(".5");
	        String[] items = {"Adaptive", "Stardard"};
	        JComboBox combo = new JComboBox(items);
	        
	        JPanel p = new JPanel(new GridLayout(4,2));
	        p.add(new JLabel("Threshold Type:"));
	        p.add(combo);
	        p.add(new JLabel("Threshold Param:"));
	        p.add(field1);
	        p.add(new JLabel("Thinning Param (abs):"));
	        p.add(field2);
	        p.add(new JLabel("Thinning Param (rel):"));
	        p.add(field3);
	        int result = JOptionPane.showConfirmDialog(null, p, "Manual Input",
	            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	        if (result == JOptionPane.OK_OPTION) {
	            System.out.println(field1.getText()
	                + " " + field2.getText()
	                +" " + field3.getText());
	            findLengthActionManual(
	            		combo.getSelectedItem().toString().equals(new String("Adaptive")),
	            		Integer.parseInt(field1.getText()),
	            		Integer.parseInt(field2.getText()),
	            		Double.parseDouble( field3.getText()));
	        } else {
	            System.out.println("Cancelled");
	        }
		
	}
	
	private void loadImageAction(ActionEvent evt){
        int returnVal = fileChooser.showOpenDialog(frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                originalImg = ImageIO.read(file);
                } 
            catch (IOException ex) {
                System.out.println("problem accessing file"+
                        file.getAbsolutePath());
                }  
            reset();
        } 
        else {
            System.out.println("File access cancelled by user.");
        }
		
	}
	
	private void reset(){
		titles = new ArrayList<String>();
		imgs = new ArrayList<BufferedImage>();
		imgs.add(originalImg);
		titles.add("Original Image");
		numImages = 1;
		changeShownImg(0);
		turnOnButtons();
	}
	
	private void turnOnButtons(){
		btnFindLength.setEnabled(true);
		btnFindLengthMan.setEnabled(true);
		prevImage.setEnabled(true);
		nextImage.setEnabled(true);
		saveImage.setEnabled(true);
	}
	
	private void drawImg(BufferedImage image){
		ImageIcon icon2 = new ImageIcon(getScaledImage(image)); 
        imgPane.setIcon(icon2);
        imgPane.setHorizontalAlignment(SwingConstants.CENTER);
        imgPane.setVerticalAlignment(SwingConstants.CENTER);
        imgPane.setVisible(true);

	}
	
	/*
	private void drawTiledImg(BufferedImage image){
		ImageIcon icon2 = new ImageIcon(getScaledImage(image,600,600)); 
		JLabel jl = new JLabel("", icon2, JLabel.CENTER);
        imgPane.add(jl);
        imgPane.setVisible(true);

	}
	*/
	
	private BufferedImage getScaledImage(BufferedImage srcImg){
		Dimension d = imgPane.getSize();
		int w = d.width-50;
		int h = d.height-20;
	    BufferedImage resizedImg = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	private void changeShownImg(int index){
		if (index>=numImages){
			drawImg(originalImg);
			currIndex = 0;
			title.setText("Original Image");
		}
		else{
			drawImg(imgs.get(index));
			currIndex = index;
			title.setText(titles.get(index));
		}
	}
	
	private void nextImage(ActionEvent evt){
		if (currIndex+1 >=numImages){
			changeShownImg(0);
		}else{
			changeShownImg(currIndex+1);
		}
	}
	
	private void prevImage(ActionEvent evt){
		if (currIndex==0){
			changeShownImg(numImages-1);
		}else{
			changeShownImg(currIndex-1);
		}
	}
	
	private void findLengthAction (ActionEvent evt){
		imageProcessor ip = new imageProcessor(originalImg);
		lengthDisp.setText("Sperm Length is :"+((int) ip.getCellLength()));
		imgs = ip.ims;
		titles = ip.titles;
		numImages = imgs.size();
		System.out.println(numImages);
		changeShownImg(numImages-1);
	}
	
	private void findLengthActionManual (boolean which, int w, int a, double r){
		System.out.println("got here ok");
		imageProcessor ip = new imageProcessor(originalImg);
		
		lengthDisp.setText("Sperm Length is : "+((int) ip.getCellLengthManual(which,w,a,r)+"um"));
		imgs = ip.ims;
		titles = ip.titles;
		numImages = imgs.size();
		changeShownImg(numImages-1);
	}

}
