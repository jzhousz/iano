/*
    Synapse Counter for MAP2-VGluT like images
    
    Assumption: 
    1. MAP2: red;  VGluT: green.
    2. The file format is a 2D (1-slice) 3-channel RGB file that Fiji recognizes.
       e.g. PSD file can be a 3-slice file in Fiji. Need to convert first.
    
   Adjustable parameters:
   1. Soma mask: background subtraction radius: 3.0;  threshold when binarizing the subtracted soma: 42; (maybe need to dilate 1-2 times)
   2. Dendrite mask:  threshold when binarize enhanced dendrite: 23;   Number of dilation: 1
   3. Synapse segmetation: RATS: default; 
   4. Synpase counting: min/max size of synapse   1/120
*/
////////////////parameters//////////////////////////////
// soma
backgroundSubtractionRadius = 3;
somaThreshold = 42;
// dendrite
dendriteThreshold = 23;
// counting
maxSynpaseSize = 120;
dilateSomaTimes = 0;
dilateDendriteTimes = 1;
/////////////////////////////////////////////////////

title = getTitle();
title_MAP = title + " (green)";
title_Synapse = title + " (red)";
title_MAP_dup = title_MAP + " dup";
title_Synapse_dup = title_Synapse + " dup";

//split, close DAPI, duplicate MAPS and VGluT.
run("Split Channels");
selectWindow(title_MAP);
run("Duplicate...", "title=[" + title_MAP_dup + "]");
selectWindow(title_Synapse);
run("Duplicate...", "title=[" + title_Synapse_dup + "]");

//MAP2 -> soma
selectWindow(title_MAP);
run("Subtract Background...", "rolling=" + backgroundSubtractionRadius + " create");
setAutoThreshold("Default dark");
//run("Threshold...");
setThreshold(somaThreshold, 255);
run("Convert to Mask");
for (i = 0; i < dilateSomaTimes; i++) {
        run("Dilate");
}

// MAP2 -> dendrite
//subtract soma
selectWindow(title_MAP);
run("Copy");
selectWindow(title_MAP_dup);
setPasteMode("Subtract");
run("Paste");
//denoise, enhance
run("Despeckle");
run("Enhance Local Contrast (CLAHE)", "blocksize=127 histogram=256 maximum=3 mask=*None* fast_(less_accurate)");
//run("Threshold...");
setAutoThreshold("Default dark");
setThreshold(dendriteThreshold, 255);
run("Convert to Mask");
//denoise, dilate
run("Despeckle");
for (i = 0; i < dilateDendriteTimes; i++) {
        run("Dilate");
}

//synpase channel:
//remove soma (from previous copy)
selectWindow(title_MAP);
run("Copy");
selectWindow(title_Synapse);
setPasteMode("Subtract");
run("Paste");
//remove dendrite
selectWindow(title_MAP_dup);
run("Copy");
selectWindow(title_Synapse);
setPasteMode("AND");
run("Paste");
// segment
run("Robust Automatic Threshold Selection", "noise=25 lambda=3 min=54");
//count
run("Invert LUT");
run("Set Measurements...", "area center redirect=[" + title_Synapse_dup + "] decimal=3");
run("Analyze Particles...", "size=0-" + maxSynpaseSize + " circularity=0.00-1.00 show=[Bare Outlines] display exclude summarize");

//To be done:  convert to  mm or mm^2.
