These are sketches for the Processing graphics software sketchbook found at https://processing.org
It is a Java-based, lightweight, all-in-one IDE for creating computer graphics.
It can be run without installation anywhere Java is installed.

*** For Research!
"Software prototyping and data visualization are two of the most important areas for Processing developers. 
Research labs inside technology companies like Google and Intel have used Processing 
for prototyping new interfaces and services."
***

The processing website contains detailed tutorials, examples, and reference materials 
that can help to explain these sketches.

Most importantly, Processing is built as a Java-like language designed for non-programmers 
(specifically artists and designers) to be able to get an basic understanding of computer graphics 
and generate interesting visuals.

As a programmer, this means that Processing is extremely easy to pick up and work with, 
and creating relatively advanced graphics can be done very quickly!


---------------------------------------------------------------------------------------------------------
Processing sketches must have a specific environment configured, but the good news is that the default 
behavior for opening a sketch in an improper environment is to create one for it.

Just move these sketches into the User/Documents/Processing folder created when you set up Processing, or 
simply copy the contents of a sketch to a new one and save it in the default location. 
When opened, it will create a folder for it automatically.

Processing sketches that perform File I/O require a specific (but simple) folder arrangement as well.

[Documents/Processing] (or wherever your sketch is)
 |
 |__[sketch] (folder)
     -[sketch].pde
     -Data (folder)
	|
	|__[I/O data files]

There must be a folder called "Data" in the sketch folder.


--------------------------------------------------------------------------------------------------------
Processing Tips:

	Most helpful pages to look at on Processing.org:
		-https://processing.org/tutorials/overview/ 	(Basic rundown of the language API)
		-https://processing.org/tutorials/p3d/ 		(overview of 3D openGL render)
		-https://processing.org/reference/		(the main reference page)



NOTES for THESE SKETCHES:
	-These were hastily done prototypes for visualization, and arent the cleanest, clearest, or most efficient
	 use of Processing's 3D capabilities. At best they should serve as a simple example.

	-These sketches rely on file I/O from synapse and neurite files, with some caveats:

		Processing has built in table creation tools that rely on an expected formatting.

		In this case, the sketches will build tables from comma separated files where the first line
		is a column header. vaa3D .swc files almost match this format already, but the first line 
		must be edited to match the expected header. Specifically:

		"##n,type,x,y,z,radius,parent"
		
		should be used as the header line for .swc files
		
		
		synapse files need a little adjustment as well.

		"x,y,z,radius,shape,name,comment,red,green,blue"
		
		Should be used as the header. 
		

	
		Obviously the names for file dependencies need to be changed as well.

	-The sample Kibra004.swc and synapses004.swc files can be place in a Data folder to run these 
	 sketches and see the output.