package annotool.analysis;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JFileChooser;

import annotool.AnnOutputPanel;

public class SynapseStatsOld implements Runnable {
	private File synapseFile = null;
	private File[] neuronFiles = null;
	
	private int width, height, depth;
	
	private Thread thread = null;
	
	private AnnOutputPanel pnlStatus = null;
	
	static final double FINE_TH = 4.0;
	static final double MEDIUM_TH = 22.0;
	
	static final double SEARCH_TH = 25; //Radius threshold for proximity search
	
	boolean[] 	fineMap = null,
				mediumMap = null,
				thickMap = null;	
		
	public SynapseStatsOld(File synapseFile, File[] neuronFiles, AnnOutputPanel pnlStatus, 
						int width, int height, int depth) {
		this.synapseFile = synapseFile;
		this.neuronFiles = neuronFiles;
		this.pnlStatus = pnlStatus;
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		int size = width * height * depth;
		fineMap = new boolean[size];
		mediumMap = new boolean[size];
		thickMap = new boolean[size];
	}

	@Override
	public void run() {
		if(synapseFile == null || neuronFiles == null) {
			pnlStatus.setOutput("Files not specified!");
			thread = null;
			return;
		}
		

		Scanner scanner = null;
		String line = null;
		double 	totalLength = 0,
				fineLength = 0,
				mediumLength = 0,
				thickLength = 0;
		double x, y, z, radius;
		double 	segmentLength;
		
		int intX, intY, intZ, r;
		double dx, dy, dz;
		int num;
		
		double xPrev, yPrev, zPrev;
		int numPrev, parent, parentPrev;
		
		for(File neuronFile : neuronFiles) {
			//Read neuron file
			try {
				scanner = new Scanner(neuronFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				pnlStatus.setOutput("Exception: File not found!");
				thread = null;
				return;
			}
			
			scanner.nextLine();
			
			
			xPrev = 0; yPrev = 0; zPrev = 0;
			numPrev = -2; parentPrev = -2;	//-2 as initial value because -1 is still a valid parent		
			
			while(scanner.hasNextLine()) {
				line = scanner.nextLine();
				line.trim();
				
				if(line.startsWith("#") || line.isEmpty())
					continue;
				
				String[] parts = line.split(" ");
				
				//Throw exception for invalid format
				if(parts.length < 7) {
					pnlStatus.setOutput("Invalid neruon node file format. Each data line must have seven parts.");
					thread = null;
					return;
				}
				
				try {				
					num = Integer.parseInt(parts[0]);
					//parts[1] is type
					x = Double.parseDouble(parts[2]);
					y = Double.parseDouble(parts[3]);
					z = Double.parseDouble(parts[4]);
					radius = Double.parseDouble(parts[5]);
					parent = Integer.parseInt(parts[6]);
				} catch(NumberFormatException ex) {
					pnlStatus.setOutput("Invalid data format encountered in the neuron nodes file.");
					thread = null;
					return;
				}
				
				intX = (int) Math.round(x);
				intY = (int) Math.round(y);
				intZ = (int) Math.round(z);
				r = (int) Math.round(radius);
				
				int zOffset, yOffset, index;
				for(int k = (intZ - r); k < (intZ + r); k++) {
					zOffset = k * width * height;
					for(int j = (intY - r); j < (intY + r); j++) {
						yOffset = j * width;
						for(int i = (intX - r); i < (intX + r); i++) {
							index = zOffset + yOffset + i;
							if(isWithinBounds(i, j, k)) {
								if(radius > MEDIUM_TH)
									thickMap[index] = true;
								else if(radius > FINE_TH)
									mediumMap[index] = true;
								else
									fineMap[index] = true;
							}
						}
					}
				}
				
				if(parent == numPrev || parentPrev == num) {
					dx = x - xPrev;
					dy = y - yPrev;
					dz = z - zPrev;
					
					segmentLength = Math.sqrt(dx*dx + dy*dy + dz*dz);
					
					if(radius > MEDIUM_TH)
						thickLength += segmentLength;
					else if(radius > FINE_TH)
						mediumLength += segmentLength;
					else
						fineLength += segmentLength;
					
					totalLength += segmentLength;
				}
				//else {
					//pnlStatus.setOutput("Connection broken at node " + num);
				//}
				
				xPrev = x;
				yPrev = y;
				zPrev = z;
				numPrev = num;
				parentPrev = parent;			
			}
			scanner.close();
		}
		
		pnlStatus.setOutput("Total Length: " + totalLength);
		pnlStatus.setOutput("Thick Length: " + thickLength);
		pnlStatus.setOutput("Medium Length: " + mediumLength);
		pnlStatus.setOutput("Fine Length: " + fineLength);
		
		
		//Visualize fine branches
		int tempzoff, tempyoff, tempindex;
		
		//int depth = 59; //Hardcoded depth
		ImageStack stack = new ImageStack(width, height);
		
		for(int tempz = 0; tempz < depth; tempz++) {
			tempzoff = tempz * width * height;
			
			ImageProcessor ip = new ColorProcessor(width, height);
			//ip.or(255);			
			ip.setColor(new Color(255, 0, 0));
			
			for(int tempy = 0; tempy < height; tempy++) {
				tempyoff = tempy * width;
				for(int tempx = 0; tempx < width; tempx++) {
					tempindex = tempzoff + tempyoff + tempx;
					if(mediumMap[tempindex])
						ip.drawDot(tempx, tempy);
				}
			}
			
			//Add processor to stack
			stack.addSlice("", ip);
		}
		
		
		ImagePlus imp = new ImagePlus("Fine map", stack);
		
		JFileChooser fileChooser = new JFileChooser();
		if(fileChooser.showSaveDialog(pnlStatus) != JFileChooser.CANCEL_OPTION)
			ij.IJ.save(imp, fileChooser.getSelectedFile().getPath());
		
		
		int synapseTotal = 0,
			synapseOnFine = 0,
			synapseOnMedium = 0,
			synapseOnThick = 0;
		
		//Read the synapse file
		try {
			scanner = new Scanner(synapseFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			pnlStatus.setOutput("Exception: Synapse file not found!");
			thread = null;
			return;
		}
		
		while(scanner.hasNext()) {
			line = scanner.nextLine();
			line.trim();
			if(line.isEmpty())
				continue;
			
			String[] parts = line.split(",");
			if(parts.length < 3) {
				pnlStatus.setOutput("Invalid format encountered in the synapse file. Each line must be x,y,z");
				thread = null;
				return;
			}
			
			try {
				x = Double.parseDouble(parts[0]);
				y = Double.parseDouble(parts[1]);
				z = Double.parseDouble(parts[2]);
			} catch(NumberFormatException ex) {
				pnlStatus.setOutput("Invalid data encountered in the synapse position file.");
				thread = null;
				return;
			}
			
			intX = (int) Math.round(x);
			intY = (int) Math.round(y);
			intZ = (int) Math.round(z);
			
			/*int index = intZ * width * height + intY * width + intX;
			
			if(fineMap[index])
				synapseOnFine++;
			if(mediumMap[index])
				synapseOnMedium++;
			if(thickMap[index])
				synapseOnThick++;*/
			
			synapseTotal++;
			
			//Proximity search
			int incr = 0;
			int px, py, pz;
			int zOffset, yOffset, index;
			
			int startX, endX,
				startY, endY,
				startZ, endZ;
			
			boolean done = false;
			
			while(incr < SEARCH_TH && !done) {
				startX = intX - incr; endX = intX + incr;
				startY = intY - incr; endY = intY + incr;
				startZ = intZ - incr; endZ = intZ + incr;
				
				for(pz = startZ; pz < endZ; pz++) {
					zOffset = pz * width * height;
					for(py = startY; py < endY; py++) {
						yOffset = py * width;
						for(px = startX; px < endX; px++) {
							if(isWithinBounds(px, py, pz)) {
								//Only process pixels that are on the surface of the search area because the inner ones are already done
								if(px == startX || px == endX || py == startY || py == endY || pz == startZ || pz == endZ) {
									index = zOffset + yOffset + px;
									
									if(fineMap[index]) {
										synapseOnFine++;
										done = true;
										break;
									}
									if(mediumMap[index]) {
										synapseOnMedium++;
										done = true;
										break;
									}
									if(thickMap[index]) {
										synapseOnThick++;
										done = true;
										break;
									}
								}
							}
						}//End of px
						if(done)
							break;
					}//End of py
					if(done)
						break;
				}//End of pz
				
				incr++;
			}
		}
		
		scanner.close();
		
		pnlStatus.setOutput("--------------------------------------------------");
		
		pnlStatus.setOutput("Synapse Count: " + synapseTotal);
		pnlStatus.setOutput("Synapse on fine branches: " + synapseOnFine);
		pnlStatus.setOutput("Synapse on medium branches: " + synapseOnMedium);
		pnlStatus.setOutput("Synapse on thick branches: " + synapseOnThick);
		
		pnlStatus.setOutput("--------------------------------------------------");

		if(totalLength != 0)
			pnlStatus.setOutput("Total synapse density: " + synapseTotal / totalLength);
		if(fineLength != 0)
			pnlStatus.setOutput("Synapse density on fine branches: " + synapseOnFine / fineLength);
		if(mediumLength != 0)
			pnlStatus.setOutput("Synapse density on medium length branches: " + synapseOnMedium / mediumLength);
		if(thickLength != 0)
			pnlStatus.setOutput("Synapse density on thickest length branches: " + synapseOnThick / thickLength);
		
		
		thread = null;
	}
	
	public void calcStats() {
		if(thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	private boolean isWithinBounds(int x, int y, int z) {
		return (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth);
	}
}
