

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
import java.lang.reflect.Array;
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
import java.util.Arrays;
import java.util.List;


public class imageProcessor {


	public String fileName;
	public BufferedImage im;
	public BufferedImage imOriginal;
	public BufferedImage imPostThreshold;
	public BufferedImage imPostRemoval;
	public BufferedImage imPostSkeleton;
	public ArrayList<BufferedImage> ims;
	public ArrayList<String> titles;
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
	public Rectangle border;


	public imageProcessor(BufferedImage i) {
		this.im=i;
		this.imOriginal=i;
		this.width=i.getWidth();
		this.height=i.getHeight();
		this.border = new Rectangle(0,0,this.width,this.height);
		// TODO Auto-generated constructor stub
	}
	public imageProcessor(BufferedImage i, Rectangle r) {
		this.im=i;
		this.imOriginal=i;
		this.width=i.getWidth();
		this.height=i.getHeight();
		this.border = r;
		// TODO Auto-generated constructor stub
	}

	/*
	 ** Automatically computes the length of the sperm cell
	 ** Combines the thresholding, labelling and thinning methods 
	 */
	public double getCellLength() {
		return getCellLengthManual(3, 8, 20,.5);
	}
	
	
	/*
	 * Gets the Cell length with input parameters to determine how the algorithm works
	 */
	public double getCellLengthManual(int whichThres, int thresParam, int absolute, double relative) {
		titles = new ArrayList<String>();
		ims = new ArrayList<BufferedImage>();
		ims.add(im);
		titles.add("Original Image");
		
		//increaseContrast();
		
		getGrayscaleArray();
		ims.add(getGrayScaleImage());
		titles.add("Grayscale Image");
		
		
		
		switch (whichThres) {

		case 1://adaptive just mean
			adaptiveThresholding(thresParam);
			break;
		case 2:
			adaptiveThresholdingPlusStd(thresParam);
			break;
		case 3:
			adaptiveThresholdingPlusWindowStd(thresParam);
			break;
		case 4:
			threshold(thresParam);
			break;
		default:
			adaptiveThresholding(thresParam);

		}
		
		ims.add(array2Img());
		titles.add("Thresholded Image");
		
		
		keepBiggest(labelImage());
		ims.add(array2Img());
		titles.add("Only Biggest Connected Component");
		
		contouring();
		contourThinning(absolute,relative);
		
		ims.add(makeSkeletonImage(thinnedCells));
		titles.add("Resultant Skeleton");
		
		ims.add(getHighlightedImage(thinnedCells));
		titles.add("Final Skeleton Highlighted on Original");
		
		return thinnedCells.size()/3.06;
	}


	/*
	 ** Computes the average pixel value of the grayscale image
	 */
	public double getAvgGrayscale() {
		int total=0;
		for (int i=1;i<width-1;i++) {
			for (int j=1;j<height-1;j++) {
				total+=grayscaleArray[i][j];
			}
		}
		return total/(width*height);
	} 

	/*
	 ** Computes the standard deviation of the grayscale image 
	 */
	public double getStdDevGrayscale(double avg) {
		double total=0;
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				int val=grayscaleArray[i][j];
				total+=(avg-val)*(avg-val);
			}
		}
		return Math.sqrt(total/(width*height));
	}
	
	/*
	 ** Converts the original image into grayscale
	 */
	public void getGrayscaleArray() {
		grayscaleArray=new int[width][height];
		Point p = new Point();
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				grayscaleArray[i][j]=new Color(im.getRGB(i,j)).getRed();
			}
		}
	}

	/*
	 ** Converts the original image into grayscale, sets everything outside Border to black
	 */
	public void getGrayscaleArrayBordered() {
		grayscaleArray=new int[width][height];
		Point p = new Point();
		for (int i=0;i<width;i++) {
			for (int j=0;j<height;j++) {
				p = new Point(i,j);
				if(border.contains(p)){
				    grayscaleArray[i][j]=new Color(im.getRGB(i,j)).getRed();
				}else{
					grayscaleArray[i][j]=0;
				}
			}
		}
	}
	
	public BufferedImage getGrayScaleImage(){
		BufferedImage b = new BufferedImage(width, height, 3);
		int val;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				val = grayscaleArray[x][y];
				color = new Color(val, val, val, 255);
				b.setRGB(x, y, color.getRGB());
			}
		}
		return b;
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
		 ** Only keep kth biggest Component
		 */
		public void keepkthBiggest(int k, int numObjects) {
			int[] countArray = new int[numObjects+1];
			//iterate through grid to update count array
			for (int i=0;i<width;i++) {
				for (int j=0;j<height;j++) {
					countArray[label[i][j]]++;
				}
			}
			
			int [] sorted = countArray;
			Arrays.sort(sorted);
			int kthindex=0;
			int numElements=sorted[numObjects-k];
			System.out.println(numElements);
			for (int i=1;i<numObjects;i++) {
				if (countArray[i]==numElements) {
					kthindex = i;
				}
			}
			System.out.println(kthindex);
			//set everything to false that isn't index
			for (int i=0;i<width;i++) {
				for (int j=0;j<height;j++) {
					if (label[i][j]!=kthindex) binary2dArr[i][j]=false;
				}
			}



	}
	/*
	 * Standard Thresholding. 
	 * Makes binary2arr where everyone above threshold input is positive and everything below is negative.
	 * passing a value of -1 will indicate using the mean of the image.  
	 */
	public void threshold(int thres){
		if (thres==-1){
			thres = (int) getAvgGrayscale();
		}
		Point p = new Point();
		binary2dArr = new boolean[width][height];
		for (int i=1;i<width-1;i++) {
			for (int j=1;j<height-1;j++) {
				p = new Point(i,j);
				if(border.contains(p)){
					binary2dArr[i][j]=(grayscaleArray[i][j]>=thres);
				}
				else{
					binary2dArr[i][j] = false;
				}
			}
		}
	}
	

	/*
	 * Uses the mean of the pixels in the box defined by the window size around the pixel as the threshold.
	 * The idea here is to the continuous line of a sperm cell one object. 
	 */
	public void adaptiveThresholding(int window) {
		binary2dArr = new boolean[width][height];
		Point p = new Point();
		for (int i=1;i<width-1;i++) {
			for (int j=1;j<height-1;j++) {
				p = new Point(i,j);
				if (!border.contains(p)){
					binary2dArr[i][j]=false;
					continue;
				}
				double sum=0;
				int count=0;
				for (int k=-window;k<=window;k++) {
					if (i+k<0) continue;
					if (i+k>=width) break;
					for (int l=-window;l<=window;l++) {
						if (j+l<0) continue;
						if (j+l>=height) break;
						sum+=grayscaleArray[i+k][j+l];
						count++;
					}
				}
				if (grayscaleArray[i][j]>sum/count)
					binary2dArr[i][j]=true;
			}
		}
	}
	/*
	 * Uses the mean plus std of image of the pixels in the box defined by the window size around the pixel as the threshold.
	 * The idea here is to the continuous line of a sperm cell one object. 
	 */
	public void adaptiveThresholdingPlusStd(int window) {
		binary2dArr = new boolean[width][height];
		double std = getStdDevGrayscale(getAvgGrayscale());
		for (int i=1;i<width-1;i++) {
			for (int j=1;j<height-1;j++) {
				double sum=0;
				int count=0;
				for (int k=-window;k<=window;k++) {
					if (i+k<0) continue;
					if (i+k>=width) break;
					for (int l=-window;l<=window;l++) {
						if (j+l<0) continue;
						if (j+l>=height) break;
						sum+=grayscaleArray[i+k][j+l];
						count++;
					}
				}
				if (grayscaleArray[i][j]>(sum/count+std))
					binary2dArr[i][j]=true;
			}
		}
	}
	
	/*
	 * Uses the mean plus std of window of the pixels in the box defined by the window size around the pixel as the threshold.
	 * The idea here is to the continuous line of a sperm cell one object. 
	 */
	public void adaptiveThresholdingPlusWindowStd(int window) {
		binary2dArr = new boolean[width][height];
		ArrayList<Integer> samples = null;
		double std;
		for (int i=1;i<width-1;i++) {
			for (int j=1;j<height-1;j++) {
				samples = new ArrayList<Integer>();
				double sum=0;
				int count=0;
				for (int k=-window;k<=window;k++) {
					if (i+k<0) continue;
					if (i+k>=width) break;
					for (int l=-window;l<=window;l++) {
						if (j+l<0) continue;
						if (j+l>=height) break;
						sum+=grayscaleArray[i+k][j+l];
						samples.add(grayscaleArray[i+k][j+l]);
						count++;
					}
				}
				int variance=0;
				double mean =sum/count;
				for (int val : samples){
					variance += (val-mean)*(val-mean);
				}
				std = Math.sqrt(variance);
				if (grayscaleArray[i][j]>(sum/count+std))
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
		return drawLength/3.06;
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
		if (averageBrightness==0) averageBrightness=(int) getAvgGrayscale();
		if (stdDevBrightness==0) stdDevBrightness=getStdDevGrayscale(averageBrightness);
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
		if (averageBrightness==0) averageBrightness=(int) getAvgGrayscale();
		if (stdDevBrightness==0) stdDevBrightness=getStdDevGrayscale(averageBrightness);
		currentContrast--;
		if (currentContrast>=0)
			im=changeBrightness(imOriginal,averageBrightness,(int)stdDevBrightness,1+currentContrast);
		else
			im=changeBrightness(imOriginal,averageBrightness,(int)stdDevBrightness,-1.0/currentContrast);
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
		BufferedImage newImage = im;
		int blue = Color.BLUE.getRGB();
		for (Point p: pointList) {
			newImage.setRGB(p.x,p.y,blue);
		}
		return newImage;
	}
	
	/*
	 * Returns image with just Skeleton
	 */
	public BufferedImage makeSkeletonImage(List<Point> pointlist){
		BufferedImage b = new BufferedImage(width, height, 3);
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
					color = Color.BLACK;
				b.setRGB(x, y, color.getRGB());
			}
		}
		int white = Color.WHITE.getRGB();
		for (Point p: pointlist) {
			b.setRGB(p.x,p.y,white);
		}
		return b;
	}

	/*
	 ** Performs a deep copy of a buffered image 
	 
	public BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	*/

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
	 ** Creates a CellImage object from an image file at path s 
	 */
	public static imageProcessor getImageProcessorFromFile(String s) {

		try {                
			BufferedImage image = ImageIO.read(new File(s));
			imageProcessor ip = new imageProcessor(image);
			ip.fileName=s;
			ip.imOriginal=ip.im;
			return ip;
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/*
	 * Main
	 */
	public static void main(String[] args) {
		imageProcessor ip = getImageProcessorFromFile("sperm/easy/24708.1_2 at 20X.jpg");
		System.out.println("starting to think");
		System.out.println("Length is "+ip.getCellLengthManual(0,5,1000,.6));
	}

}
