# spermLength


This application is used to calculate the length of sperm cells in a 2D images. Attempts to isolate the sperm body and use a given conversation factor to determine the length.

Usage Procedure:

	- Use "Load Image" button to import an image into the application. 
	- For standard use, simply click "Find Length" to determine the length of the cell. The length will be listed below the image and you can use the previous and back button to see how the image changed through each step of processing. 
	- If you would like to isolate a particular part of the image if there was an artifact or some other aspect of the image that was messing up computation you can select a region of the image by clicking on it twice. This builds a box between the two clicks that acts as the region of interest. NOTE: THERE IS NO DRAGGING FEATURE FOR SELECTING THE REGION OF INTEREST. 
	- For manual length computation, Click the "Find Length Manual" button. This allows the user to input the parameters for the algorithm
	
		- There are 4 possible Thresholding Algorithms that can be used. "Adaptive" is the default and refers to the windowed adaptive procedure explained in the report. "Adaptive+ImageStd" means that we add the image standard deviation to the mean of the window we are looking at to get rid of a little noise. "Adaptive+WindowStd" adds the standard deviation of the pixels in the window. "Standard" refers to the standard thresholding method we learned in class
		- Threshold Param is the input to the threshold algorithm that was choosen. For the three adaptive methods it is the size of the window that the algorithm considers for each pixel. For standard it is the grayscale value of the threshold (between 0 and 255). If you give the standard algorithm an input value of -1 it uses the mean of the image as the threshold. 
		- "Thinning param (abs)"  refers to the absolute iteration number parameter for the thinning algorithm we discussed in class.
		- "Thinning param (rel)" refers to the relative paramter value for the thinning parameter we discussed in class. 
