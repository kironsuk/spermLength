

import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;


public class imageProcessor {


	public String fileName;
	public BufferedImage im;
	public BufferedImage imOriginal;
	public BufferedImage imPostThreshold;
	public BufferedImage imPostRemoval;
	public BufferedImage imPostSkeleton;
	public int width,height,averageBrightness;
	public double stdDevBrightness;
	public int currentContrast=0;
	public int drawLength=0;
	public LinkedList<Point> highlightList;
	public LinkedList<Point> queue;
	public LinkedList<Integer> targetPixels;
	public ArrayList<Point> thinnedCells;
	public ArrayList<Point> cells1;
	public ArrayList<Point> cells2;
	public ArrayList<Integer[]> cells3;
	public int[][] label;
	public int[][] grayscaleArray;
	public boolean[][] visitedArray;
	public boolean[][] resultArray;
	public boolean[][] binary2dArr; 
	public Color color;


	public imageProcessor(BufferedImage i) {
		this.im=i;
		this.imOriginal=deepCopy(i);
		this.width=i.getWidth();
		this.height=i.getHeight();
		// TODO Auto-generated constructor stub
	}

	/*
	 ** Automatically computes the length of the sperm cell
	 ** Combines the thresholding, labelling and thinning methods 
	 */
	public double getCellLength() {
		getGrayscaleArray();
		adaptiveThresholding(5);
		imPostThreshold = array2Img();
		keepBiggest(labelImage());
		imPostRemoval = array2Img();
		contouring();
		contourThinning(5,0.5);
		//imPostSkeleton = getHighlightedImage(highlightList);
		return thinnedCells.size()/3.06;
	}


	/*
	 ** Computes the average pixel value of the grayscale image
	 */
	public int getAvgBrightness() {
		int res=0;
		for (int i=0;i<width;i++) {
			int sum=0;
			for (int j=0;j<height;j++) {
				sum+=grayscaleArray[i][j];
			}
			res+=sum/height;
		}
		return res/width;
	} 

	/*
	 ** Computes the standard deviation of the grayscale image 
	 */
	public double getStdDevBrightness() {
		double res=0;
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				int c=grayscaleArray[i][j];
				res+=(averageBrightness-c)*(averageBrightness-c);
			}
		}
		return Math.sqrt(res/width/height);
	}

	/*
	 ** Converts the original image into grayscale 
	 */
	public void getGrayscaleArray() {
		grayscaleArray=new int[width][height];
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				grayscaleArray[i][j]=new Color(im.getRGB(i,j)).getRed();
			}
		}
	}

	/*
	 ** Labels all the connected components of object pixels 
	 ** Returns number of components
	 */
	public int labelImage() {
		visitedArray=new boolean[width][height];
		label=new int[width][height];
		int count=1,x,y;
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				if (visitedArray[i][j]) continue;
				queue=new LinkedList<Point>();
				queue.add(new Point(i,j));
				//Flooding aka bfs
				while (!queue.isEmpty()) {
					Point p = queue.pop();
					x=p.x;
					y=p.y;
					//check out of bounds
					if (x<0||y<0||x>=width||y>=height)
						continue;
					//check if pixel has already been visited
					if (visitedArray[x][y])
						continue;
					//set visited flag to true	
					visitedArray[x][y]=true;
					//end of tree
					if (!binary2dArr[x][y]){
						label[x][y] = 0; //background
						continue;
					}
					label[x][y]=count;
					//might add points outside border
					queue.addLast(new Point(x,y-1));
					queue.addLast(new Point(x,y+1));
					queue.addLast(new Point(x-1,y));
					queue.addLast(new Point(x+1,y));
				}
				count++;
			}
		}
		return count;
	}

	/*
	 ** Only keep biggest Component
	 */
	public void keepBiggest(int numObjects) {
		int[] countArray = new int[numObjects+1];
		//iterate through grid to update count array
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				countArray[label[i][j]]++;
			}
		}

		int maxIndex=0;
		int maxSize=0;
		for (int i=1;i<numObjects;i++) {
			if (countArray[i]>maxSize) {
				maxIndex=i; 
				maxSize=countArray[i];
			}
		}
		//set everything to false that isn't maxindex
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				if (label[i][j]!=maxIndex) binary2dArr[i][j]=false;
			}
		}



	}

	/*
	 ** Sets the boolean array isCell which separates object and background pixels
	 ** The adaptive threshold method uses the mean of pixels within 
	 ** the given window size as the threshold
	 */
	public void adaptiveThresholding(int window) {
		binary2dArr = new boolean[width][height];
		for (int i=1;i<width-1;i++) {
			for (int j=1;j<height-1;j++) {
				int sum=0,count=0;
				for (int k=-window;k<=window;k++) {
					if (i+k>=width) break;
					if (i+k<0) continue;
					for (int l=-window;l<=window;l++) {
						if (j+l>=height) break;
						if (j+l<0) continue;
						sum+=grayscaleArray[i+k][j+l];
						count++;
					}
				}
				if (grayscaleArray[i][j]*count>sum)
					binary2dArr[i][j]=true;
			}
		}
	}

	/*
	 ** Creates a cell complex from object pixels
	 ** cells1: points,  cells2: lines, cells3: boxes 
	 */
	public void contouring() {
		cells1=new ArrayList<Point>();
		cells2=new ArrayList<Point>();
		cells3=new ArrayList<Integer[]>();
		int[][] cellId = new int[width][height];
		int[][] hLineId = new int[width-1][height-1];
		int[][] vLineId = new int[width-1][height-1];
		int cid=1, lid=1;
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				if (binary2dArr[i][j]) {
					cells1.add(new Point(i,j));
					cellId[i][j]=cid;
					cid++;
				}
			}
		}
		for (int i=0;i<width-1;i++) {
			for (int j=0;j<height-1;j++) {
				if (binary2dArr[i][j]&&binary2dArr[i][j+1]) {
					cells2.add(new Point(cellId[i][j],cellId[i][j+1]));
					vLineId[i][j]=lid;
					lid++;
				}
				if (binary2dArr[i][j]&&binary2dArr[i+1][j]) {
					cells2.add(new Point(cellId[i][j],cellId[i+1][j]));
					hLineId[i][j]=lid;
					lid++;
				}
			}
		}
		for (int i=0;i<width-2;i++) {
			for (int j=0;j<height-2;j++) {
				if (hLineId[i][j]>0&&hLineId[i][j+1]>0&&vLineId[i][j]>0&&vLineId[i+1][j]>0) {
					Integer[] cellSquare = new Integer[4];
					cellSquare[0]=hLineId[i][j];
					cellSquare[1]=hLineId[i][j+1];
					cellSquare[2]=vLineId[i][j];
					cellSquare[3]=vLineId[i+1][j];
					cells3.add(cellSquare);
				}
			}
		}
	}

	/*
	 ** Removes cells from the cell complex to obtained a thinned contour
	 ** tabs: Absolute threshold
	 ** trel: Relative threshold 
	 */
	public void contourThinning(double tabs,double trel) {
		boolean[] isRemoved1 = new boolean[cells1.size()];
		boolean[] isRemoved2 = new boolean[cells2.size()];
		boolean[] isRemoved3 = new boolean[cells3.size()];
		boolean hasSimpleCells=true;
		int thinningIteration=0;
		int[] isolationIteration2 = new int[cells2.size()];
		while (hasSimpleCells) {
			hasSimpleCells=false;
			thinningIteration++;
			int[] parents1 = new int[cells1.size()];
			int[] parents2 = new int[cells2.size()];
			for (int i=0;i<cells2.size();i++) {
				if (!isRemoved2[i]) {
					parents1[cells2.get(i).x-1]++;
					parents1[cells2.get(i).y-1]++;
				}
			}
			for (int i=0;i<cells3.size();i++) {
				if (!isRemoved3[i]) {
					Integer[] in = cells3.get(i);
					parents2[in[0]-1]++;
					parents2[in[1]-1]++;
					parents2[in[2]-1]++;
					parents2[in[3]-1]++;
				}
			}
			for (int i=0;i<cells2.size();i++) {
				if (!isRemoved2[i]) {
					if (parents2[i]==0&&(thinningIteration-isolationIteration2[i])>tabs&&(1-1.0*isolationIteration2[i]/thinningIteration)>trel) {
					} else {
						if (parents1[cells2.get(i).x-1]==1&&!isRemoved1[cells2.get(i).x-1]) {
							isRemoved2[i]=true;
							isRemoved1[cells2.get(i).x-1]=true;
							hasSimpleCells=true;
						} else  if (parents1[cells2.get(i).y-1]==1&&!isRemoved1[cells2.get(i).y-1]) {
							isRemoved2[i]=true;
							isRemoved1[cells2.get(i).y-1]=true;
							hasSimpleCells=true;
						}

					}
				}
			}
			for (int i=0;i<cells3.size();i++) {
				if (!isRemoved3[i]) {
					Integer[] in = cells3.get(i);
					for (int j=0;j<4;j++) {
						if (parents2[in[j]-1]==1&&!isRemoved2[in[j]-1]) {
							isRemoved3[i]=true;
							isRemoved2[in[j]-1]=true;
							hasSimpleCells=true;
							break;
						}
					}
				}
			}
			for (int i=0;i<cells2.size();i++) {
				if (!isRemoved2[i]&&parents2[i]==0&&isolationIteration2[i]==0) {
					isolationIteration2[i]=thinningIteration;
				}
			}
		}
		thinnedCells = new ArrayList<Point>();
		for (int i=0;i<cells1.size();i++) {
			if (!isRemoved1[i]) {
				thinnedCells.add(cells1.get(i));
			}
		}
		cells1=thinnedCells;
	}


	/*
	 **  Computes the length of the sperm cell based in users drawing
	 */
	public double getDrawingLength() {
		double res = drawLength/3.06;
		return res;
	}

	/*
	 ** Changes the brightness of the image based on its difference from the mean
	 ** mean: mean of brightness of all pixels
	 ** stddev: standard deviation of brightness of all pixels
	 ** factor: how much darker/brighter to make the image
	 */
	public BufferedImage changeBrightness(BufferedImage bi,int mean, int stddev,double factor) {
		int w = bi.getWidth(),h = bi.getHeight();
		BufferedImage res = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		for (int i=0;i<w;i++) {
			for (int j=0;j<h;j++) {
				int v = new Color(bi.getRGB(i,j)).getRed();
				v=(int)(mean+(v-mean)*factor);
				v=Math.max(Math.min(v,255),0);
				res.setRGB(i,j,new Color(v,v,v).getRGB());
			}
		}
		return res;
	}

	/*
	 ** Increases the contrast of the current image
	 ** Calls changeBrightness internally 
	 */
	public void increaseContrast() {
		if (grayscaleArray==null) getGrayscaleArray();
		if (averageBrightness==0) averageBrightness=getAvgBrightness();
		if (stdDevBrightness==0) stdDevBrightness=getStdDevBrightness();
		currentContrast++;
		if (currentContrast>=0)
			im=changeBrightness(imOriginal,averageBrightness,(int)stdDevBrightness,1+currentContrast);
		else
			im=changeBrightness(imOriginal,averageBrightness,(int)stdDevBrightness,-1.0/currentContrast);
	}

	/*
	 ** Decreases the contrast of the current image
	 ** Calls changeBrightness internally 
	 */
	public void decreaseContrast() {
		if (grayscaleArray==null) getGrayscaleArray();
		if (averageBrightness==0) averageBrightness=getAvgBrightness();
		if (stdDevBrightness==0) stdDevBrightness=getStdDevBrightness();
		currentContrast--;
		if (currentContrast>=0)
			im=changeBrightness(imOriginal,averageBrightness,(int)stdDevBrightness,1+currentContrast);
		else
			im=changeBrightness(imOriginal,averageBrightness,(int)stdDevBrightness,-1.0/currentContrast);
	}

	/*
	 ** Draws a line segment on the given image
	 ** p1: start point
	 ** p2: end point
	 ** zoomFactor: scale of p1 and p2 relative to size of image
	 ** stroke: width of line
	 ** c: color of line
	 */
	public void drawLine(BufferedImage i,Point p1, Point p2, double zoomFactor, int stroke, Color c) {
		zoomFactor=1.0*width/(532.0*Math.pow(1.1,zoomFactor));
		Graphics2D g = i.createGraphics();
		g.setColor(c);
		double x1 = (p1.x)*zoomFactor;
		double y1 = (p1.y)*zoomFactor;
		double x2 = (p2.x)*zoomFactor;
		double y2 = (p2.y)*zoomFactor;
		int x,y,w,h;
		if (x1<x2) {
			x=(int)(x1-zoomFactor*stroke);
			w=(int)(x2-x1+2*zoomFactor*stroke);
		} else {
			x=(int)(x2-zoomFactor*stroke);
			w=(int)(x1-x2+2*zoomFactor*stroke);
		}
		if (y1<y2) {
			y=(int)(y1-zoomFactor*stroke);
			h=(int)(y2-y1+2*zoomFactor*stroke);
		} else {
			y=(int)(y2-zoomFactor*stroke);
			h=(int)(y1-y2+2*zoomFactor*stroke);
		}
		g.fill(new Rectangle(x,y,w,h));
		g.dispose();
		drawLength+=zoomFactor*stroke;
	}



	/*
	 ** Saves the given image i to a file of name s
	 */
	public void saveImage(BufferedImage i,String s) {
		try {                
			ImageIO.write(i,"jpg",new File(s));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}

	/*
	 ** Highlights a list of points of interest on the current image 
	 */
	public BufferedImage getHighlightedImage(List<Point> pointList) {
		BufferedImage newImage = deepCopy(im);
		int red = Color.RED.getRGB();
		for (Point p: pointList) {
			newImage.setRGB(p.x,p.y,red);
		}
		return newImage;
	}

	/*
	 ** Performs a deep copy of a buffered image 
	 */
	public BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	/*
	 * Turns the binary array to a buffered image
	 */
	public BufferedImage array2Img (){
		BufferedImage b = new BufferedImage(width, height, 3);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {

				if (binary2dArr[x][y]){
					color = Color.WHITE;
				}else{
					color = Color.BLACK;
				}
				b.setRGB(x, y, color.getRGB());
			}
		}
		return b;
	}

	/*
	 ** Clears all user-drawn lines on the current image 
	 */
	public void clearDrawing() {
		BufferedImage im2 = deepCopy(imOriginal);
		im=imOriginal;
		imOriginal=im2;
		drawLength=0;
	}


	/*
	 ** Creates a CellImage object from an image file at path s 
	 */
	public static imageProcessor getCellImageFromFile(String s) {

		try {                
			BufferedImage image = ImageIO.read(new File(s));
			imageProcessor ci = new imageProcessor(image);
			ci.fileName=s;
			ci.imOriginal=ci.deepCopy(ci.im);
			return ci;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/*
	 * Main
	 */
	public static void main(String[] args) {
		System.out.println("micrometers");
		/*
		File imgsrc = new File( "sperm/easy/24708.1_1 at 20X.jpg");
		//File imgsrc = new File( "duke_skateboard.jpg");
		BufferedImage img = null;
		try {
			img = ImageIO.read(imgsrc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		imageProcessor imageTest = new imageProcessor(img);
		//Graphics g = imageTest.im.getGraphics();
		//g.drawImage(imageTest.im, 0, 0,imageTest.width,imageTest.height, null);

		System.out.println(imageTest.getCellLength());

		int w = img.getWidth(null);
		int h = img.getHeight(null);


		BufferedImage bi = new
		    BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		JLabel picLabel = new JLabel(new ImageIcon(img));


		JFrame frame = new JFrame();
		//frame.getContentPane().setLayout(new FlowLayout());		
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(400,800));// changed it to preferredSize, Thanks!
		frame.getContentPane().add( jp );// adding to content pane will work here. Please read the comment bellow.
		frame.getContentPane().add(new JLabel(new ImageIcon(img)));
		frame.pack();
		frame.setVisible(true);
		//frame.setDefaultCloseOp


		Graphics g = bi.getGraphics();
		g.drawImage(img, 0, 0,null);

		JFrame frame = buildFrame();

		final BufferedImage imgf = img;

		JPanel pane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imgf, 0, 0, null);
            }
        };

        frame.add(pane);
		 */
		System.out.println("Where did the image go??");
	}

}
