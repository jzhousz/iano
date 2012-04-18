package annotool.analysis;

import ij.ImagePlus;
import ij.process.ImageProcessor;

public class RegionGrow {
	int width, height;
	
	public void growRegion(int[][] guide, int[][] blob, int width, int height, int replaceThreshold) {
		this.width = width;
		this.height = height;
		
		int x, y, nX, nY, arrayIndex = 0, replacedCount;
		
		int loop = 0;
		
		while(true) {
			loop++;
			replacedCount = 0;
			for(y = 0; y < height; y++) {
				for(x = 0; x < width; x++) {
					if(blob[x][y] == 0) {
						//For each black pixel in blob, check neighbors in guide. If guide is black, set corresponding blob pixels to black
						for (nY = y - 1; nY <= y + 1; nY++) {
							for (nX = x - 1; nX <= x + 1; nX++) {
								if(withinBounds(nX, nY)) {
									if(guide[nX][nY] == 0) {
										blob[nX][nY] = 0;
										replacedCount++;
									}
								}
							}
						}
					}
					arrayIndex++;
				}
			}
			
			if(replacedCount < replaceThreshold || loop > 1000)
				break;
		}
	}
	
	private boolean withinBounds(int x, int y) {
	    return (x >= 0 && x < width && y >= 0 && y < height);
	}
	
	//Calculates the 1D array index for given x and y
	private int offset(int x, int y) {
	    return x + y * width;
	}
	
	public static void main(String[] args) {
		RegionGrow rg = new RegionGrow();
		
		ImagePlus imp = new ImagePlus("test/blob.tif");
		ImageProcessor blobip = imp.getProcessor();
		ImagePlus imp2 = new ImagePlus("test/guide.tif");
		ImageProcessor guideip = imp2.getProcessor();
		
		int[][] blob = blobip.getIntArray();
		int[][] guide = guideip.getIntArray();
		
		int width = blobip.getWidth();
		int height = blobip.getHeight();
		
		rg.growRegion(guide, blob, width, height, 30);
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				blobip.set(x, y, blob[x][y]);
			}
		}
		
		imp.show();
	}
}
