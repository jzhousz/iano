Process Flow:

1. Run object counter 3D 2 Channel using appropriate values for object channel threshold, backbone threshold, proximity check range. Check appropriate boxes for object channel and backbone channel. Leave "color by proximity" unchecked.

2. Save the output statistics in file.

3. Open the excel file and copy center x, center y and center z columns to another sheet and save this sheet as csv file.

4. Run the program, select synapse file from step 3(csv) using the appropriate button. Select "ImageJ to Vaa3D" and click run to convert csv file to vaa3d co-ordinates. Save the output.

5. Select synapse file from 4.

6. Select one or more neuron files exported from Vaa3D using appropriate button.

7. Check "Calculate Stats" and run.

8. The program will calculate statistics and also ask for save path to save the output file which can be used for visualization later in ImageJ.

9. Particle visualization with proximity: Re-run imagej plugin same as in step 1 but this time with "Color by proximity" checked. When prompted for file input, select output file from step 8. Also, make sure the "show particles" is checked. This will display the detected particles color coded by their proximity to branches of different width.