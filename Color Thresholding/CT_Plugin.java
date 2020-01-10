import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import ij.io.FileSaver;
import ij.blob.*;

public class CT_Plugin implements PlugIn {

	private ManyBlobs blobs;

	private ImagePlus binaryimage;
	private String filename;
	private String title_name;
	private ImagePlus inputimg;
	
	public int count(ImagePlus imp, double lowbnd) {
		blobs = new ManyBlobs(imp); // Extended ArrayList
        	
    		blobs.findConnectedComponents(); // Start the Connected Component Algorithm
		
		ManyBlobs filteredBlobs = blobs.filterBlobs(lowbnd, 1000000, Blob.GETENCLOSEDAREA);
		return filteredBlobs.size();
	}

	public int enclosedArea(ImagePlus imp, double lowbnd) {
	
		blobs = new ManyBlobs(imp); // Extended ArrayList
        	
    		blobs.findConnectedComponents(); // Start the Connected Component Algorithm
		
		ManyBlobs filteredBlobs = blobs.filterBlobs(lowbnd, 1000000, Blob.GETENCLOSEDAREA);
		int totalarea = 0;
		for(int i=0; i<filteredBlobs.size(); i++){
			totalarea+=filteredBlobs.get(i).getEnclosedArea();}
		return totalarea;
	}	

	public ImagePlus dual_threshold(ImagePlus img, ImagePlus satImg, int minthr, int maxthr){
		ImageProcessor ip= img.getProcessor();
		ImageProcessor ip_sat = satImg.getProcessor();

		for (int r=0; r< img.getHeight(); r++) {
			for(int c=0; c< img.getWidth(); c++) {
				if(ip.getPixel(c,r) >= minthr && ip.getPixel(c,r) < maxthr && ip_sat.getPixel(c,r)>90){
					ip.putPixel(c,r,255);}
		       		else{
					ip.putPixel(c,r,0);}
			}
		}
		ImagePlus bin = new ImagePlus("binary_Image", ip);
		return bin;
	}


	public void color_threshold(ImagePlus inputImg,  String title_name){
		int back4 = 0; 
		int back3 = 0;
		int back2 = 0;
		int back1 = 0;
		int count = 0;
		int arr[] = new int[7];
		int i = 0;
		
		int hist[]=new int[256];
		//inputImg.show();
		ImagePlus img = new Duplicator().run(inputImg);        // make a temp image to work with
		ImageConverter iConv = new ImageConverter(img);        // this object will convert color models
		iConv.convertToHSB();       // converts to hsb, which is enough like HSI to serve the purpose
		ImageStack stack = img.getImageStack();        // gets the three result planes as an ImageJ "stack"
		ImageProcessor hue = stack.getProcessor(1);        // get an image processor to access the hue plane
		ImageProcessor sat = stack.getProcessor(2);        // get an image processor to access the saturation plane
		ImageProcessor inte = stack.getProcessor(3); 

		ImagePlus hueImg_o = new ImagePlus("Hue", hue);
		ImagePlus satImg = new ImagePlus("Saturation", sat);
		ImagePlus inteImg = new ImagePlus("Intensity", inte);

		ImagePlus hueImg = new Duplicator().run(hueImg_o);
	
		ImageProcessor imp = hueImg.getProcessor();
		double height = imp.getHeight();
		double width = imp.getWidth();
		double area =  height * width;
		
		for (int r=0; r< imp.getHeight(); r++) {
			for(int c=0; c< imp.getWidth(); c++) {
				hist[imp.getPixel(c,r)]++;}
			}

		for(int j = 0; j<256-4; j++){
			if (j<4){
				if(j==0){
					back4 = 255-3;
					back3 = 255-2;
					back2 = 255-1;
					back1 = 255;}
				if(j==1){
					back4 = 255-2;
					back3 = 255-1;	
					back2 = 255;
					back1 = j-1;}
				if(j==2){
					back4 = 255-1;
					back3 = 255;
					back2 = j-2;
					back1 = j-1;}
				if(j==3){
					back4 = 255;
					back3 = j-3;
					back2 = j-2;
					back1 = j-1;}
			}
			else{
				back4 = j-4;
				back3 = j-3;
				back2 = j-2;
				back1 = j-1;
			 }

			if( (hist[back4]<hist[j] && hist[j]>hist[j+4]) && (hist[back3]<hist[j] && hist[j]>hist[j+3]) && (hist[back2]<hist[j] && hist[j]>hist[j+2]) && (hist[back1]<hist[j] && hist[j]>hist[j+1])){
				if(hist[j] > 0.0113 * area){
					count =  count + 1;
					arr[i] = j;
					i = i+1;
					//IJ.log("histo is"+hist[j]+" index is" +j);
				}			
			}	
		}
		if(title_name.equals("mandms.png")){
			IJ.log("Total number of colors: "+count);
			IJ.log("For Red, Lower Threshold is: " +0 +" and Upper Threshold is: " +(arr[1]+0)/2);
			IJ.log("For Orange, Lower Threshold is: " +(arr[1]+0)/2 +" and Upper Threshold is: " +(arr[1]+arr[2])/2);
			IJ.log("For Brown, Lower Threshold is: " +(arr[1]+0)/2 +" and Upper Threshold is: " +(arr[1]+arr[2])/2);
			IJ.log("For Yellow, Lower Threshold is: " +(arr[2]+arr[3])/2 + " and Upper Threshold is: " +(arr[3]+arr[4])/2);
			IJ.log("For Green, Lower Threshold is: " +(arr[3]+arr[4])/2 + " and Upper Threshold is: " +(arr[4]+arr[5])/2);
			IJ.log("For Blue, Lower Threshold is: " +(arr[4]+arr[5])/2 + " and Upper Threshold is: " +(arr[5]+255)/2);
		}
		else if(title_name.equals("mandms2.png")){
			IJ.log("Total number of colors: "+count);			
			
			IJ.log("For Orange, Lower Threshold is: " +(arr[1]+0)/2 +" and Upper Threshold is: " +(arr[1]+arr[2])/2);
			IJ.log("For Yellow, Lower Threshold is: " +(arr[1]+arr[2])/2 + " and Upper Threshold is: " +(arr[2]+arr[3])/2);
			IJ.log("For Green, Lower Threshold is: " +(arr[2]+arr[3])/2 + " and Upper Threshold is: " +(arr[3]+arr[4])/2);
			IJ.log("For Blue, Lower Threshold is: " +(arr[3]+arr[4])/2 + " and Upper Threshold is: " +(arr[4]+arr[5])/2);
			IJ.log("For Red, Lower Threshold is: " +(arr[4]+arr[5])/2 + " and Upper Threshold is: " +(arr[5]+255)/2);
			IJ.log("For Brown, Lower Threshold is: " +(arr[4]+arr[5])/2 + " and Upper Threshold is: " +(arr[5]+255)/2);
		}
		else{
			IJ.log("Total number of colors: "+count);
			IJ.log("For Red, Lower Threshold is: " +0 +" and Upper Threshold is: " +(arr[1]+0)/2);
			IJ.log("For Yellow, Lower Threshold is: " +(0+arr[1])/2 + " and Upper Threshold is: " +(arr[1]+arr[2])/2);
			IJ.log("For Green, Lower Threshold is: " +(arr[1]+arr[2])/2 + " and Upper Threshold is: " +(arr[2]+arr[3])/2);
			IJ.log("For Blue, Lower Threshold is: " +(arr[2]+arr[3])/2 + " and Upper Threshold is: " +(arr[3]+255)/2);
		}		
	}

	private String mmcount_hue(ImagePlus inputImg, String thiscolor, int minthr, int maxthr, double lowerbnd, String title_name){
		ImagePlus img = new Duplicator().run(inputImg);        // make a temp image to work with
		ImageConverter iConv = new ImageConverter(img);        // this object will convert color models
		iConv.convertToHSB();       // converts to hsb, which is enough like HSI to serve the purpose
		ImageStack stack = img.getImageStack();        // gets the three result planes as an ImageJ "stack"
		ImageProcessor hue = stack.getProcessor(1);        // get an image processor to access the hue plane
		ImageProcessor sat = stack.getProcessor(2);        // get an image processor to access the saturation plane
		ImageProcessor inte = stack.getProcessor(3); 

		ImagePlus hueImg_o = new ImagePlus("Hue", hue);
		ImagePlus satImg = new ImagePlus("Saturation", sat);
		ImagePlus inteImg = new ImagePlus("Intensity", inte);

		ImagePlus hueImg = new Duplicator().run(hueImg_o);
		//hueImg_o.show();
		//satImg.show();
		//inteImg.show();
		//hueImg.show();					

		binaryimage = dual_threshold(hueImg, satImg, minthr, maxthr);
			
		binaryimage.show();	
		//IJ.run("Dilate");
	
		return (thiscolor+": "+count(binaryimage, lowerbnd)+"M&Ms, total area "+enclosedArea(binaryimage, lowerbnd));
		//return(thiscolor);
	}


	public void run(String arg) {
		//ImagePlus inputimg = IJ.getImage();
	
		filename = "C:\\Users\\Ashin\\Downloads\\colored_balls.png";  
		inputimg = IJ.openImage(filename); 
		title_name = inputimg.getTitle();
		
		IJ.log("Image : " +title_name);
		IJ.log(mmcount_hue(inputimg, "RED", 0, 19, 105, title_name));
		IJ.log(mmcount_hue(inputimg, "BLUE", 130, 195, 13, title_name));
		IJ.log(mmcount_hue(inputimg, "GREEN", 49, 106, 10, title_name));
		IJ.log(mmcount_hue(inputimg, "YELLOW", 17, 49, 11, title_name));

		filename = "C:\\Users\\Ashin\\Downloads\\mandms.png";  
		inputimg = IJ.openImage(filename); 
		title_name = inputimg.getTitle();
		IJ.log("Image : " +title_name);
		color_threshold(inputimg, title_name);
		
		filename = "C:\\Users\\Ashin\\Downloads\\mandms2.png";
		inputimg = IJ.openImage(filename); 
		title_name = inputimg.getTitle();
		IJ.log("Image : " +title_name);
		color_threshold(inputimg, title_name);

		filename = "C:\\Users\\Ashin\\Downloads\\colored_balls.png";  
		inputimg = IJ.openImage(filename); 
		title_name = inputimg.getTitle();
		IJ.log("Image : " +title_name);
		color_threshold(inputimg, title_name);
	}
}
