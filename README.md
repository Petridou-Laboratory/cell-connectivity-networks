# cell-connectivity-networks
Fiji plugin to reconstruct connectivity networks based on label adjacency from a cell segmentation label mask
The plugin uses the MorpholibJ function “Region Adjacency Graph” (Legland, D., Arganda-Carreras, I. & Andrey, P. MorphoLibJ: integrated library and plugins for mathematical morphology with ImageJ. Bioinformatics 32, 3532–3534 (2016)) to identify label adjacency.

### Requirements:
1. Fiji (https://imagej.net/software/fiji/downloads)
1. Fiji MorpholibJ [plugin](https://imagej.net/plugins/morpholibj)

### Installation:
- Download Fiji and MorpholibJ plugin.
- Download the `ij-graph.jar` file from plugin folder of this repository and copy it in your local Fiji jar folder.

### Usage:
To use **cell-connectivity-networks** plugin, launch it from `Plugins>Graphs>Create and Edit Graph`.

### Input:
1. 2D (xy) Label mask image
1. Intensity image (xy) (Confocal section from same Label mask image ROI)
1. Optional: previously generated graph

### Output:
1. Text file with adjacency label pairs
1. Text file with label centroids XY coordinates
1. ROI set with edges
1. Edited label mask
1. Log file (showing changes done to links and labels)

### Functionalities:
1. Create adjacency graph (Create graph: `Load images and create graph`)
1. Option to add labels in Fiji (`Add cell` - Trace new cell outlining the boundaries with the Fiji polygon tool - making sure to not overlap an already existing label)
1. Option to toggle label mask overlay (`Show overlay` checkbox)
1. Possibility to edit the graphs: add missing edges or delete wrong edges from the GUI, note: do not use the ROI manager for this (`Add edge`, `Delete edge`)
1. Possibility to upload a previously generated graph ROI file to re-edit (Load graph: `Load images and graph`).
1. Saving of all output results in a defined directory (`Save results (Links+Centroids+LabelMask+Graph`)

### Minimal data
The repository contains minimal data to validate the plugin:
- input
  1. Original confocal microscopy tiff file, 3 channels (membrane marker, interstitial fluid, nuclear marker)
  1. Cropped region converted to RGB (not necessary to convert to RGB)
  1. Segmentation label mask of the same ROI obtained using [Cellpose 3](https://cellpose.readthedocs.io/en/v3.1.1.1/)
- output
  1. "data/output" folder containing results for this image

### Usage example
1. Open the plugin `Graph Creator/Editor`
2. Load the [Label mask image](https://github.com/Petridou-Laboratory/cell-connectivity-networks/blob/main/data/input/zebrafish_sample_cp_masks.png)  
3. Load the [Intensity image](https://github.com/Petridou-Laboratory/cell-connectivity-networks/blob/main/data/input/zebrafish_sample_intensity_image.tif)
4. Click on `Load images and create graph`








The ID centroid coordinated and the adjacency list can be used for rigidity percolation theory network analysis or other topological analysis. 

Main contributor: Arif Khan - Bioimage Analysis Support Team (BAST) of Data Science Center at the European Molecular Biology Laboratory (EMBL) Heidelberg

Code developed for the Petridou Group at the European Molecular Biology Laboratory (EMBL) Heidelberg for the following [publication](https://www.nature.com/articles/s41567-026-03276-6)
