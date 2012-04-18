package annotool.analysis;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/*
 * Based on/Derived from ImageJ Plugin Hough_Circles by Hemerson Pistori (pistori at ec.ucdb.br) and Eduardo Rocha Costa (eduardo.rocha at poli.usp.br)
 */

public class HoughTransform {
	public int radiusMin;  // Find circles with radius grater or equal radiusMin
    public int radiusMax;  // Find circles with radius less or equal radiusMax
    public int radiusInc;  // Increment used to go from radiusMin to radiusMax
    
    byte imageValues[]; // Raw image (returned by ip.getPixels())
    double houghValues[][][]; // Hough Space Values
    public int width; // Hough Space width (depends on image width)
    public int height;  // Hough Space heigh (depends on image height)
    public int depth;  // Hough Space depth (depends on radius interval)
    public int offset; // Image Width
    public int offx;   // ROI x offset
    public int offy;   // ROI y offset
    
    int lut[][][]; // LookUp Table for rsin e rcos values
    
	public HoughTransform(int width, int height, int offset, int offx, int offy) {
		this(10, 20, 2, width, height, offset, offx, offy);
	}
	
	public HoughTransform(int radiusMin, int radiusMax, int radiusInc,
			int width, int height, int offset, int offx, int offy) {
		this.radiusMin = radiusMin;
		this.radiusMax = radiusMax;
		this.radiusInc = radiusInc;
		this.width = width;
		this.height = height;
		this.offset = offset;
		this.offx = offx;
		this.offy = offy;
		
		this.depth = ((radiusMax-radiusMin)/radiusInc)+1;
	}
	
	public ImageProcessor run(ImageProcessor ip) throws Exception {
		if(ip instanceof ByteProcessor)
			imageValues = (byte[])ip.getPixels();
		else
			throw new Exception("Only byte image supported.");
		
		houghTransform();
		
		// Create image View for Hough Transform.
        ImageProcessor newip = new ByteProcessor(width, height);
        byte[] newpixels = (byte[])newip.getPixels();
        createHoughPixels(newpixels);
        
        return newip;
	}
    
    private void houghTransform () {

        int lutSize = buildLookUpTable();

        houghValues = new double[width][height][depth];

        int k = width - 1;
        int l = height - 1;

        for(int y = 1; y < l; y++) {
            for(int x = 1; x < k; x++) {
                for(int radius = radiusMin;radius <= radiusMax;radius = radius+radiusInc) {
                    if( imageValues[(x+offx)+(y+offy)*offset] != 0 )  {// Edge pixel found
                        int indexR=(radius-radiusMin)/radiusInc;
                        for(int i = 0; i < lutSize; i++) {

                            int a = x + lut[1][i][indexR]; 
                            int b = y + lut[0][i][indexR]; 
                            if((b >= 0) & (b < height) & (a >= 0) & (a < width)) {
                                houghValues[a][b][indexR] += 1;
                            }
                        }

                    }
                }
            }

        }

    }
    
    
    /** The parametric equation for a circle centered at (a,b) with
    radius r is:

	a = x - r*cos(theta)
	b = y - r*sin(theta)
	
	In order to speed calculations, we first construct a lookup
	table (lut) containing the rcos(theta) and rsin(theta) values, for
	theta varying from 0 to 2*PI with increments equal to
	1/8*r. As of now, a fixed increment is being used for all
	different radius (1/8*radiusMin). This should be corrected in
	the future.
	
	Return value = Number of angles for each radius
	   
	*/
	private int buildLookUpTable() {

        int i = 0;
        int incDen = Math.round (8F * radiusMin);  // increment denominator

        lut = new int[2][incDen][depth];

        for(int radius = radiusMin;radius <= radiusMax;radius = radius+radiusInc) {
            i = 0;
            for(int incNun = 0; incNun < incDen; incNun++) {
                double angle = (2*Math.PI * (double)incNun) / (double)incDen;
                int indexR = (radius-radiusMin)/radiusInc;
                int rcos = (int)Math.round ((double)radius * Math.cos (angle));
                int rsin = (int)Math.round ((double)radius * Math.sin (angle));
                if((i == 0) | (rcos != lut[0][i][indexR]) & (rsin != lut[1][i][indexR])) {
                    lut[0][i][indexR] = rcos;
                    lut[1][i][indexR] = rsin;
                    i++;
                }
            }
        }

        return i;
    }
	
	// Convert Values in Hough Space to an 8-Bit Image Space.
    private void createHoughPixels (byte houghPixels[]) {
        double d = -1D;
        for(int j = 0; j < height; j++) {
            for(int k = 0; k < width; k++)
                if(houghValues[k][j][0] > d) {
                    d = houghValues[k][j][0];
                }

        }

        for(int l = 0; l < height; l++) {
            for(int i = 0; i < width; i++) {
                houghPixels[i + l * width] = (byte) Math.round ((houghValues[i][l][0] * 255D) / d);
            }

        }
    }
}
