# cell-connectivity-networks
FIJI plugin to reconstruct connectivity networks based on label adjacency from a cell segmentation mask
The plugin uses the MorpholibJ function “Region Adjacency Graph” (Legland, D., Arganda-Carreras, I. & Andrey, P. MorphoLibJ: integrated library and plugins for mathematical morphology with ImageJ. Bioinformatics 32, 3532–3534 (2016)) to identify label adjacency. To use the plugin, download the ij-graph.jar file and copy it in the Fiji jar folder. Launch it from Plugins>Graphs>Create and Edit Graph. Make sure to use with MorpholibJ plugin (IJPB plugin site should be added to the list of update sites. Installation instructions [here](https://imagej.net/plugins/morpholibj)) 

### Dependencies:
1. MorpholibJ plugin

### Input files:
1. Label mask image (cell segmentation (e.g. with Cellpose))
1. Intensity image (Confocal section from same Label mask image ROI)
1. Optional: previously generated graph

### Output files:
1. Adjacency list (text file with adjacency label pairs)
1. ID (text file with label centroids XY coordinates)
1. edgeSet.zip (ROI set with edges)
1. labelMask (label mask with additional labels)
1. Log file (changes done to links ans labels)

### Functionalities:
1. Create adjacency graph (Create graph: Load images and create graph)
1. Option to add labels in Fiji (Add cell - via polygon)
1. Option to toggle label mask overlay (Show overlay)
1. Possibility to edit the graphs: add missing edges or delete wrong edges from the GUI, note: do not use the ROI manager for this (Add edge, Delete edge)
1. Possibility to upload a previously generated graph ROI file to re-edit (Load graph: Load images and graph).
1. Saving of all output results in a defined directory (Save results (Links+Centroids+LabelMask+Graph)

### Minimal data
The repository contains minimal data to validate the plugin:
1. Original confocal microscopy tiff file, 3 channels (membrane marker, interstitial fluid, nuclear marker)
2. Cropped region converted to RGB (not necessary to convert to RGB)
3. Label mask segmentation of the same ROI done with [Cellpose 3](https://cellpose.readthedocs.io/en/v3.1.1.1/)
4. results folder containing results for this image

 
The ID centroid coordinated and the adjacency list can be used for rigidity percolation theory netowrk analysis or other topological analysis. 

Main contributor: Arif Kahn (EMBL, CBA - Center for Bioimage Analysis)

