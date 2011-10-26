import java.awt.Font;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.util.HashMap;

//Based on imagej plugin 3D Object Counter
//http://rsbweb.nih.gov/ij/plugins/track/objects.html

public class ObjectDetection {
	int width, height;
	int threshold = 128;		//Just so that the image does not have to be black and white
	int[] tag;
	
	public int[] run(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();
		
		int[] imgData = new int[width*height];
		tag = new int[width*height];
		boolean[] isObjPixel = new boolean[width*height];
		
		int x, y;
		int value;
		int arrayIndex;
		
		//Load the image in a one dimension array
        arrayIndex = 0;
        for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				value = ip.getPixel(x, y);
				if(value < threshold)
					isObjPixel[arrayIndex] = true;
				else
					isObjPixel[arrayIndex] = false;
				
				imgData[arrayIndex] = value;
				arrayIndex++;
			}
		}
		
		int tagvois;
	    int ID = 1;
	    arrayIndex = 0;
	    int minTag;
	    int i, offset;
	    
	    int nX = -1, nY = -1;
		
	    //First ID attribution
	    for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				if(isObjPixel[arrayIndex]) {
					tag[arrayIndex] = ID;
					minTag = ID;
					
					i = 0;
                    //Find the minimum tag in the neighbors pixels
					for (nY = y - 1; nY <= y + 1; nY++) {
						for (nX = x - 1; nX <= x + 1; nX++) {
							if(withinBounds(nX, nY)) {
								offset = offset(nX, nY);
								if(isObjPixel[offset]) {
									i++;						//If neighbor pixel is object, increment neighbor object pixel count
									tagvois = tag[offset];
									if (tagvois != 0 && tagvois < minTag) 	//If any neighbor object pixel is already tagged, use that
										minTag = tagvois;
								}
							}
						}
					}
					
                    //if (i != 9) surf[arrayIndex]=true;
                    
                    tag[arrayIndex] = minTag;
                    if (minTag == ID){					//If current pixel was given the new tag, increment tag number for next run
                        ID++;
                    }
				}
				arrayIndex++;
			}
		}
	    
	    //Minimization of IDs = connection of structures
	    arrayIndex = 0;
	    for(y = 0; y < height; y++) {
			for(x = 0; x < width; x++) {
				if(isObjPixel[arrayIndex]) {
					minTag = tag[arrayIndex];
					
					//Find the minimum tag in the neighbors pixels
					for (nY = y - 1; nY <= y + 1; nY++) {
						for (nX = x - 1; nX <= x + 1; nX++) {
							if(withinBounds(nX, nY)) {
								offset = offset(nX, nY);
								if(isObjPixel[offset]) {
									tagvois = tag[offset];
									if (tagvois != 0 && tagvois < minTag)
										minTag = tagvois;
								}
							}
						}
					}
					
					//Replacing tag by the minimum tag found
					for (nY = y - 1; nY <= y + 1; nY++) {
						for (nX = x - 1; nX <= x + 1; nX++) {
							if(withinBounds(nX, nY)) {
								offset = offset(nX, nY);
								if(isObjPixel[offset]) {
									tagvois = tag[offset];
									if (tagvois != 0 && tagvois != minTag)
										replacetag(tagvois,minTag);
								}
							}
						}
					}
				}
				arrayIndex++;
			}
	    }
		
		return tag;
	}
	
	private boolean withinBounds(int x, int y) {
	    return (x >= 0 && x < width && y >= 0 && y < height);
	}
	
	//Calculates the 1D array index for given x and y
	private int offset(int x, int y) {
	    return x + y * width;
	}
	
	public void replacetag(int m, int n){
	    for (int i=0; i < tag.length; i++) 
	    	if (tag[i] == m) tag[i] = n;
	}
	
	//Gets the hashmap with tags as key and pixel count as values
	//Only tags with pixels above or equal to threshold are returned
	public HashMap<Integer, Integer> getTagCount(int threshold) {
		//Key: ID, value: pixel count
		HashMap<Integer, Integer> tagCount = new HashMap<Integer, Integer>();
		
		int arrayIndex = 0;
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(tag[arrayIndex] != 0) {
					int count = 0;
					if(tagCount.containsKey(tag[arrayIndex])) {
						count = tagCount.get(tag[arrayIndex]);
					}
					
					count++;
					tagCount.put(tag[arrayIndex], count);
				}
				
				arrayIndex++;
			}
		}
		
		java.util.ArrayList<Integer> tagToRemove = new java.util.ArrayList<Integer>();
		//Eliminate tags with lower pixel count
		for(Integer key : tagCount.keySet()) {
			if(tagCount.get(key) < threshold)
				tagToRemove.add(key);
		}
		for(Integer key :tagToRemove)
			tagCount.remove(key);
		
		return tagCount;
	}
	
	public void markObjects(ImageProcessor ip) {
		if (height != ip.getHeight() || width != ip.getWidth())
			return;
		
		//Key: ID, value: pixel count
		HashMap<Integer, Integer> tagCount = new HashMap<Integer, Integer>();
		
		int arrayIndex = 0;
		Font font = new Font("SansSerif", Font.PLAIN, 12);
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(tag[arrayIndex] != 0) {
					int count = 0;
					if(tagCount.containsKey(tag[arrayIndex])) {
						count = tagCount.get(tag[arrayIndex]);
					}
					
					count++;
					tagCount.put(tag[arrayIndex], count);
				}
				
				arrayIndex++;
			}
		}
		
		//Mark only objects covering more than 50 pixels
		arrayIndex = 0;
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(tag[arrayIndex] != 0 && tagCount.containsKey(tag[arrayIndex])) {
					int count = tagCount.get(tag[arrayIndex]);
					if(count > 400)
						//ip.drawString("" + tag[arrayIndex], x, y);
						ip.drawPixel(x, y);
					else
						tagCount.remove(tag[arrayIndex]);				
				}
				arrayIndex++;
			}
		}
	}
}
