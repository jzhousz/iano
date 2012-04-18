package synapse.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFileChooser;

public class SynapseStats implements Runnable {
	private File synapseFile = null;
	private File[] neuronFiles = null; // Neuron reconstruction data can be
										// loaded from multiple files

	private int width, height, depth;

	private Thread thread = null;

	private AnnOutputPanel pnlStatus = null;

	static final double MIN_TH = 3.6;
	static final double MAX_TH = 16.0;

	static final double SEARCH_TH = 25; // Radius threshold for proximity search

	static final double DENSITY_FACTOR = 1 / (2 * Math.PI);

	static final int BINS = 6;

	float[] radiusMap = null;

	int colorPool[] = { 16777215, 3092479, 11730948, 15002703, 1240038,
			15007979, 64, 8388672, 16744448, 8388863 };

	JFileChooser fileChooser = new JFileChooser();

	public SynapseStats(File synapseFile, File[] neuronFiles,
			AnnOutputPanel pnlStatus, int width, int height, int depth) {
		this.synapseFile = synapseFile;
		this.neuronFiles = neuronFiles;
		this.pnlStatus = pnlStatus;
		this.width = width;
		this.height = height;
		this.depth = depth;

		// Initialize radius map to all -1 as default
		int size = width * height * depth;
		radiusMap = new float[size];

		int offset1, offset2, index;
		for (int z = 0; z < depth; z++) {
			offset1 = z * width * height;
			for (int y = 0; y < height; y++) {
				offset2 = y * width;
				for (int x = 0; x < width; x++) {
					index = offset1 + offset2 + x;
					radiusMap[index] = -1;
				}
			}
		}
	}

	@Override
	public void run() {
		if (synapseFile == null || neuronFiles == null) {
			pnlStatus.setOutput("Files not specified!");
			thread = null;
			return;
		}

		Scanner scanner = null;
		String line = null;
		double totalLength = 0;

		double[] binLength = new double[BINS];

		double[] density = new double[BINS];
		double[] binThreshold = new double[BINS - 1];
		double binSize = (MAX_TH - MIN_TH) / (BINS - 2);

		// Initialize BINS - 1 thresholds for BINS bins
		binThreshold[0] = MIN_TH;
		binThreshold[BINS - 2] = MAX_TH;
		for (int i = 1; i < BINS - 2; i++) {
			binThreshold[i] = binThreshold[i - 1] + binSize;
		}

		// Initialize length and density to zero
		for (int i = 0; i < BINS; i++) {
			binLength[i] = 0;
			density[0] = 0;
		}

		double x, y, z, radius;
		double segmentLength;

		int intX, intY, intZ, r, rz;
		double dx, dy, dz;
		int num;

		double xPrev, yPrev, zPrev;
		int numPrev, parent, parentPrev;

		for (File neuronFile : neuronFiles) {
			// Read neuron file
			try {
				scanner = new Scanner(neuronFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				pnlStatus.setOutput("Exception: File not found!");
				thread = null;
				return;
			}

			scanner.nextLine();

			xPrev = 0;
			yPrev = 0;
			zPrev = 0;
			numPrev = -2;
			parentPrev = -2; // -2 as initial value because -1 is still a valid
								// parent

			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				line.trim();

				if (line.startsWith("#") || line.isEmpty())
					continue;

				String[] parts = line.split(" ");

				// Throw exception for invalid format
				if (parts.length < 7) {
					pnlStatus
							.setOutput("Invalid neruon node file format. Each data line must have seven parts.");
					thread = null;
					return;
				}

				try {
					num = Integer.parseInt(parts[0]);
					// parts[1] is type
					x = Double.parseDouble(parts[2]);
					y = Double.parseDouble(parts[3]);
					z = Double.parseDouble(parts[4]);
					radius = Double.parseDouble(parts[5]);
					parent = Integer.parseInt(parts[6]);
				} catch (NumberFormatException ex) {
					pnlStatus
							.setOutput("Invalid data format encountered in the neuron nodes file.");
					thread = null;
					return;
				}

				intX = (int) Math.round(x);
				intY = (int) Math.round(y);
				intZ = (int) Math.round(z);
				r = (int) Math.round(radius);

				int zOffset, yOffset, index;

				rz = r / 3 + 1;

				for (int k = (intZ - rz); k < (intZ + rz); k++) {
					zOffset = k * width * height;
					for (int j = (intY - r); j < (intY + r); j++) {
						yOffset = j * width;
						for (int i = (intX - r); i < (intX + r); i++) {
							index = zOffset + yOffset + i;
							if (isWithinBounds(i, j, k)) {
								radiusMap[index] = (float) radius;
							}
						}
					}
				}

				if (parent == numPrev || parentPrev == num) {
					dx = x - xPrev;
					dy = y - yPrev;
					dz = z - zPrev;

					segmentLength = Math.sqrt(dx * dx + dy * dy + dz * dz);

					boolean isFine = true;
					for (int i = BINS - 2; i >= 0; i--) {
						if (radius > binThreshold[i]) {
							binLength[i + 1] += segmentLength;
							isFine = false;
							break;
						}
					}
					if (isFine)
						binLength[0] += segmentLength;

					totalLength += segmentLength;
				}

				xPrev = x;
				yPrev = y;
				zPrev = z;
				numPrev = num;
				parentPrev = parent;
			}
			scanner.close();
		}

		pnlStatus.setOutput("Total Length: " + totalLength);

		for (int i = 0; i < BINS; i++)
			pnlStatus.setOutput("Length for bin " + (i + 1) + ": "
					+ binLength[i]);

		int synapseTotal = 0;

		int[] color = new int[BINS];
		for (int i = 0; i < BINS; i++) {
			color[i] = colorPool[i % 10];
		}

		int[] synapseCount = new int[BINS];
		for (int i = 0; i < BINS; i++)
			synapseCount[i] = 0;

		// Read the synapse file
		try {
			scanner = new Scanner(synapseFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			pnlStatus.setOutput("Exception: Synapse file not found!");
			thread = null;
			return;
		}

		BufferedWriter writer = null;
		if (fileChooser.showSaveDialog(pnlStatus) == JFileChooser.CANCEL_OPTION)
			return;
		try {
			writer = new BufferedWriter(new FileWriter(
					fileChooser.getSelectedFile()));
			while (scanner.hasNext()) {

				line = scanner.nextLine();
				line.trim();
				if (line.isEmpty())
					continue;

				String[] parts = line.split(",");
				if (parts.length < 3) {
					pnlStatus.setOutput("Invalid format encountered in the synapse file. Each line must be x,y,z");
					thread = null;
					return;
				}

				try {
					x = Double.parseDouble(parts[0]);
					y = Double.parseDouble(parts[1]);
					z = Double.parseDouble(parts[2]);
				} catch (NumberFormatException ex) {
					pnlStatus.setOutput("Invalid data encountered in the synapse position file.");
					thread = null;
					return;
				}

				intX = (int) Math.round(x);
				intY = (int) Math.round(y);
				intZ = (int) Math.round(z);

				synapseTotal++;

				// Proximity search
				int incr = 0;
				int px, py, pz;
				int zOffset, yOffset, index;

				int startX, endX, startY, endY, startZ, endZ;

				boolean done = false;

				float currRadius;

				while (incr < SEARCH_TH && !done) {
					startX = intX - incr;
					endX = intX + incr;
					startY = intY - incr;
					endY = intY + incr;
					startZ = intZ - incr;
					endZ = intZ + incr;

					for (pz = startZ; pz < endZ; pz++) {
						zOffset = pz * width * height;
						for (py = startY; py < endY; py++) {
							yOffset = py * width;
							for (px = startX; px < endX; px++) {
								if (isWithinBounds(px, py, pz)) {
									// Only process pixels that are on the
									// surface of the search area because the
									// inner ones are already done
									if (px == startX || px == endX
											|| py == startY || py == endY
											|| pz == startZ || pz == endZ) {
										index = zOffset + yOffset + px;

										currRadius = radiusMap[index];
										if (currRadius != -1) {
											boolean isFine = true;
											for (int i = BINS - 2; i >= 0; i--) {
												if (currRadius > binThreshold[i]) {
													synapseCount[i + 1]++;
													density[i + 1] += DENSITY_FACTOR
															/ currRadius;

													writer.write((intX - 1)
															+ " "
															+ (height - intY)
															+ " " + (intZ - 1)
															+ " " + (i + 2));
													writer.newLine();

													isFine = false;
													break;
												}
											}
											if (isFine) {
												synapseCount[0]++;
												density[0] += DENSITY_FACTOR
														/ currRadius;

												writer.write((intX - 1) + " "
														+ (height - intY) + " "
														+ (intZ - 1) + " 1");
												writer.newLine();
											}

											done = true;
											break;
										}
									}
								}
							}// End of px
							if (done)
								break;
						}// End of py
						if (done)
							break;
					}// End of pz

					incr++;
				}
			}

			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		scanner.close();

		// ColorLabel legend = new ColorLabel(color);

		pnlStatus
				.setOutput("--------------------------------------------------");

		pnlStatus.setOutput("Synapse Count: " + synapseTotal);
		for (int i = 0; i < BINS; i++)
			pnlStatus.setOutput("Synapse count for bin " + (i + 1) + ": "
					+ synapseCount[i]);

		pnlStatus
				.setOutput("--------------------------------------------------");
		pnlStatus.setOutput("Density over length:");
		pnlStatus
				.setOutput("--------------------------------------------------");
		if (totalLength != 0)
			pnlStatus.setOutput("Total synapse density: " + synapseTotal
					/ totalLength);
		for (int i = 0; i < BINS; i++)
			if (binLength[i] != 0)
				pnlStatus.setOutput("Density for bin " + (i + 1) + ": "
						+ synapseCount[i] / binLength[i]);
			else
				pnlStatus.setOutput("Density for bin " + (i + 1)
						+ ": [BIN LENGTH 0]");

		pnlStatus
				.setOutput("--------------------------------------------------");
		pnlStatus.setOutput("Density over surface:");
		pnlStatus
				.setOutput("--------------------------------------------------");

		if (totalLength != 0) {
			double densitySum = 0;
			for (int i = 0; i < BINS; i++)
				densitySum += density[i];

			pnlStatus.setOutput("Total synapse density: " + densitySum
					/ totalLength);
		}
		for (int i = 0; i < BINS; i++)
			if (binLength[i] != 0)
				pnlStatus.setOutput("Density for bin " + (i + 1) + ": "
						+ density[i] / binLength[i]);
			else
				pnlStatus.setOutput("Density for bin " + (i + 1)
						+ ": [BIN LENGTH 0]");

		thread = null;
	}

	public void calcStats() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	private boolean isWithinBounds(int x, int y, int z) {
		return (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth);
	}
}
