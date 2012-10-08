//split, close DAPI, duplicate MAPS and VGluT.
run("Split Channels");
selectWindow("1. MAP2-VGluT-DAPI.jpg (blue)");
close();
selectWindow("1. MAP2-VGluT-DAPI.jpg (green)");
run("Duplicate...", "title=[1. MAP2-VGluT-DAPI.jpg (green)-1]");
selectWindow("1. MAP2-VGluT-DAPI.jpg (red)");
run("Duplicate...", "title=[1. MAP2-VGluT-DAPI.jpg (red)-1]");

//MAP2 -> soma
selectWindow("1. MAP2-VGluT-DAPI.jpg (green)");
run("Subtract Background...", "rolling=3 create");
setAutoThreshold("Default dark");
//run("Threshold...");
setThreshold(42, 255);
run("Convert to Mask");

// MAP2 -> dendrite
//subtract soma
selectWindow("1. MAP2-VGluT-DAPI.jpg (green)");
run("Copy");
selectWindow("1. MAP2-VGluT-DAPI.jpg (green)-1");
setPasteMode("Subtract");
run("Paste");
//denoise, enhance
run("Despeckle");
run("Enhance Local Contrast (CLAHE)", "blocksize=127 histogram=256 maximum=3 mask=*None* fast_(less_accurate)");
//run("Threshold...");
setAutoThreshold("Default dark");
setThreshold(23, 255);
run("Convert to Mask");
//denoise, dilate
run("Despeckle");
run("Dilate");

//synpase channel:
//remove soma (from previous copy)
selectWindow("1. MAP2-VGluT-DAPI.jpg (red)");
run("Paste");
//remove dendrite
selectWindow("1. MAP2-VGluT-DAPI.jpg (green)-1");
run("Copy");
selectWindow("1. MAP2-VGluT-DAPI.jpg (red)");
setPasteMode("AND");
run("Paste");
// segment
run("Robust Automatic Threshold Selection", "noise=25 lambda=3 min=54");
//count
run("Invert LUT");
run("Set Measurements...", "area center redirect=[1. MAP2-VGluT-DAPI.jpg (red)-1] decimal=3");
run("Analyze Particles...", "size=0-120 circularity=0.00-1.00 show=[Bare Outlines] display exclude summarize");
