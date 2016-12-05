import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.BasicStroke;
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
	private BufferedImage workingImg;
	private BufferedImage currImg;
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
	private Rectangle border;
	private Rectangle trueBorder;
	private Point corner1=null;
	private Point corner2=null;
	private Graphics2D currGraphics;

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
		
		saveImage = new JButton("Reset Image");
		spanel.add(saveImage);
		saveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reset();
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
		
		imgPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                pictureMousePressed(evt);
            }
            
			public void mouseReleased(java.awt.event.MouseEvent evt) {
                pictureMouseReleased(evt);
            }
        });
        imgPane.setVisible(true);
		
		
	}
	private void pictureMousePressed(MouseEvent evt) {
		Point p = evt.getPoint();
		corner1 = corner2;
		corner2 = p;
		if (corner1 != null) drawSelection();
		System.out.println(p);
		
	}
	private void pictureMouseReleased(MouseEvent evt) {
		// TODO Auto-generated method stub
		
	}
	private void findLengthActionMan(ActionEvent evt){
			System.out.println("do I even get here?");
	        JTextField field1 = new JTextField("5");
	        JTextField field2 = new JTextField("20");
	        JTextField field3 = new JTextField(".5");
	        String[] items = {"Adaptive","Adaptive+ImageStd","Adaptive+WindowStd","Stardard"};
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
	            System.out.println(
	            		combo.getSelectedIndex()
	            	+" " +field1.getText()
	                + " " + field2.getText()
	                +" " + field3.getText());
	            findLengthActionManual(
	            		combo.getSelectedIndex()+1,
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
		border = new Rectangle(0, 0, imgPane.getWidth(), imgPane.getHeight());
		corner1 = null;
		corner2 = null;
		changeShownImg(0);
		currGraphics.draw(border);
		turnOnButtons();
		calcTrueRectangle();
		
		
	}
	
	private void drawInitialBorder(){
		
		System.out.println(border);
		//currGraphics.drawRect(border.x, border.y, border.width, border.height);
		currGraphics.draw(border);
	}
	
	private void drawSelection(){
		System.out.println("Drawing Selection");
		//currGraphics.clearRect(border.x, border.y, border.width, border.height);

		border = new Rectangle(
				Math.min(corner1.x, corner2.x), 
				Math.min(corner1.y, corner2.y),
				Math.abs(corner1.x-corner2.x),
				Math.abs(corner1.y-corner2.y)
				);
				
		changeShownImg(currIndex);
		currGraphics.draw(border);
		calcTrueRectangle();
		//drawImg(workingImg);
		//imgPane.repaint();
	}
	
	private void calcTrueRectangle(){
		
		int w = imgPane.getWidth(), h =imgPane.getHeight(),
				tw = imgs.get(currIndex).getWidth(), th = imgs.get(currIndex).getHeight();
		trueBorder= new Rectangle(
				(int)(((double)tw*border.x)/w),
				(int)(((double)th*border.y)/h),
				(int)(((double)tw*border.width)/w),
				(int)(((double)th*border.height)/h)
				);
				
	}
	
	private void turnOnButtons(){
		btnFindLength.setEnabled(true);
		btnFindLengthMan.setEnabled(true);
		prevImage.setEnabled(true);
		nextImage.setEnabled(true);
		saveImage.setEnabled(true);
	}
	
	private void drawImg(BufferedImage image){
		//updateWorkingImg(image);
		currImg = getScaledImage(image);
		currGraphics = currImg.createGraphics();
		ImageIcon icon2 = new ImageIcon(currImg); 
        imgPane.setIcon(icon2);
        imgPane.setHorizontalAlignment(SwingConstants.CENTER);
        imgPane.setVerticalAlignment(SwingConstants.CENTER);
        imgPane.setVisible(true);

	}
	
	private void updateWorkingImg(BufferedImage image){
		/*int w = border.width;
		int h = border.height;
		workingImg = new BufferedImage (w,h, BufferedImage.TYPE_INT_ARGB);
		for(int i=0; i<w; i++){
			for (int j =0; j<h; j++){
				image.get
			}
		}
		*/
		ColorModel cm = image.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		Raster r =image.getData(border);
		workingImg = new BufferedImage(cm,
				r.createCompatibleWritableRaster(),
				isAlphaPremultiplied,
				null);
		
		

	}
	
	
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
		imageProcessor ip = new imageProcessor(originalImg,trueBorder);
		lengthDisp.setText("Sperm Length is :"+((int) ip.getCellLength())+"um");
		imgs = ip.ims;
		titles = ip.titles;
		numImages = imgs.size();
		System.out.println(numImages);
		changeShownImg(numImages-1);
	}
	
	private void findLengthActionManual (int which, int w, int a, double r){
		imageProcessor ip = new imageProcessor(originalImg, trueBorder);
		
		lengthDisp.setText("Sperm Length is : "+((int) ip.getCellLengthManual(which,w,a,r)+"um"));
		imgs = ip.ims;
		titles = ip.titles;
		numImages = imgs.size();
		changeShownImg(numImages-1);
	}

}
