package annotool.analysis;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.HashMap;

//Based on imagej plugin 3D Object Counter
//http://rsbweb.nih.gov/ij/plugins/track/objects.html

public class ObjectDetection {
	int width, height;
	int threshold = 128;		//The image does not have to be black and white
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
	
	/**
	 * Get the largest detected object tag
	 * @return
	 */
	public int getLargestObject() {
		int maxCount = -1;
		int maxTag = -1;
		HashMap<Integer, Integer> tagCount = getTagCount();
		for(Integer key : tagCount.keySet()) {
			int value = tagCount.get(key);
			if(value > maxCount) {
				maxCount = value;
				maxTag = key;
			}
		}
		
		return maxTag;
	}
	
	/**
	 * Gives object tags and count for detected objects that are larger than "pixelThreshold" supplied
	 * 
	 * @param pixelThreshold
	 * @return
	 */
	public ArrayList<Integer> getLargeObjects(int pixelThreshold) {
		HashMap<Integer, Integer> tagCount = getTagCount();
		
		ArrayList<Integer> largeObjectTags = new java.util.ArrayList<Integer>();
		//Get tags that are within threshold
		for(Integer key : tagCount.keySet()) {
			if(tagCount.get(key) >= threshold)
				largeObjectTags.add(key);
		}		
		return largeObjectTags;
	}
	
	//Gets the hashmap with tags as key and pixel count as values
	private HashMap<Integer, Integer> getTagCount() {
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
		
		return tagCount;
	}
	
	public void markObjects(ImageProcessor ip) {
		if (height != ip.getHeight() || width != ip.getWidth())
			return;
		
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
		
		//Mark only objects covering more than 50 pixels
		arrayIndex = 0;
		for(int y=0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				if(tag[arrayIndex] != 0 && tagCount.containsKey(tag[arrayIndex])) {
					int count = tagCount.get(tag[arrayIndex]);
					if(count > 50)
						//ip.drawString("" + tag[arrayIndex], x, y);
						ip.set(x, y, 0);
					else
						tagCount.remove(tag[arrayIndex]);				
				}
				arrayIndex++;
			}
		}
	}
	
	public static void main(String[] args) {
		JunctionDetector jd = new JunctionDetector();
		ImagePlus binaryIMP = new ImagePlus("test/binary.tif");
		ImageProcessor binaryIP = binaryIMP.getProcessor();
		
		ImagePlus axonIMP = new ImagePlus("test/axon-alt.tif");
		ImageProcessor axonIP = axonIMP.getProcessor();
		
		DilateToEdge.axonRun(new ij.process.BinaryProcessor((ij.process.ByteProcessor)binaryIP), axonIP);
		//System.out.println(jd.isJunction(new Point(317, 331), new ij.process.BinaryProcessor((ij.process.ByteProcessor)binaryIP)));
		
		axonIMP.show();
		
		/*ObjectDetection od = new ObjectDetection();
		ImagePlus imp = new ImagePlus("test/Axon.tif");
		ImageProcessor ip = imp.getProcessor();
		od.run(ip);
		ImageProcessor resultIP = ip.createProcessor(ip.getWidth(), ip.getHeight());
		resultIP.or(255);
		od.markObjects(resultIP);
		imp.show();
		new ImagePlus("Result", resultIP).show();*/
	}
}
