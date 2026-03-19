/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2021 - 2022 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.graph;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.*;
import ij.macro.Variable;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;
import inra.ijpb.plugins.AnalyzeRegions;
import net.imagej.patcher.LegacyInjector;
import org.apache.commons.io.FilenameUtils;
import org.scijava.ItemVisibility;
import org.scijava.Priority;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.apache.log4j.helpers.Loader.getResource;

@Plugin(type = Command.class, name = "Graph", label = "GraphCreator/Editor", menuPath = "Plugins>Graphs>Create and Edit Graph", selectable = true, priority = Priority.HIGH)
public class GraphCommand extends InteractiveCommand
{
    @Parameter(label="", visibility=ItemVisibility.MESSAGE)
    private final String messageTitle= "<html>" +
            "<table><tr valign='top'><td>" +
            "<h2>Graph creation/editing</h2>" +
            "<a href='https://git.embl.de/grp-cba/camilla-autorino-zebrafish-blastoderm-connectivity-analysis'>https://git.embl.de/grp-cba/camilla-autorino-zebrafish-blastoderm-connectivity-analysis</a>" +
            "</td><td>&nbsp;&nbsp;<img src='"+getResource("images/cell-connectivity.jpg")+"' width='100' height='100'></img><td>" +
            "</tr></table>" +
            "</html>";
    public static final String LABEL_SEPARATOR = "_";
    public void drawLabelNames() {
        Overlay overlay = null;
        ImageCanvas canvas = intensityImage.getCanvas();
        if ( canvas != null )
            overlay = canvas.getShowAllList();
        if ( overlay != null ) {
            overlay.drawLabels(true);
            overlay.drawNames(true);
            Analyzer.drawLabels(true);
            intensityImage.draw();
            Prefs.useNamesAsLabels = true; // maybe not needed?
        }
    }

    static {
        LegacyInjector.preinit();
        IJ.log("\fUse 'Load images and create graph' to create a new graph\n\fUse 'Load images and graph' to load an existing graph");
        IJ.log(" NOTE: Please use the updated label mask to modify the graph");
    }

    @Parameter(label = "Label mask image")
    public File labelMaskFile;

    @Parameter(label = "Intensity image")
    public File intensityImageFile;

    @Parameter(visibility=ItemVisibility.MESSAGE, label="<html><b><hr width='100'>Create graph<hr width='100'></b></html>")
    private final String createGraphMessage = "<html><hr width='1000'></html>";

    @Parameter(label = "Load images and create graph", callback = "createGraph")
    public Button createGraphButton;

    @Parameter(visibility=ItemVisibility.MESSAGE, label="<html><b><hr width='200'>Load graph<hr width='200'></b></html>")
    private final String loadGraphMessage = "<html><hr width='1000'></html>";

    @Parameter(label = "Graph rois file")
    public File graphFile;

//    @Parameter (visibility = ItemVisibility.MESSAGE, persist = false)
//    public String displayMessage0 = " NOTE: load updated label mask to modify the graph ";

    @Parameter(label = "Load images and graph", callback = "loadGraph")
    public Button loadGraphButton;

    @Parameter(visibility=ItemVisibility.MESSAGE, label="<html><b><hr width='200'>Edit graph<hr width='200'></b></html>")
    private final String editGraphMessage = "<html><hr width='1000'></html>";

    @Parameter (visibility = ItemVisibility.MESSAGE, persist = false)
    public String displayMessage = "<html><b>!!!! DO NOT USE ROI Manager buttons/checkboxes to edit graph !!!!</b></html> ";

    @Parameter (visibility = ItemVisibility.MESSAGE, persist = false)
    public String displayMessage1 = " >>>> Please use the buttons below to edit graph edges >>>> ";

    @Parameter(label = "Add edge", callback = "addEdge", description = "Add new edge (select Line tool first)")
    public Button addEdgeButton;

    @Parameter(label = "Delete edge", callback = "deleteEdge", description = "Delete edge (click on the edge to be deleted)")
    public Button deleteEdgeButton;

    @Parameter(label = "Add cell", callback = "addCell", description = "Add new cell (select freehand tool first and draw the boundary of the cell to be added)")
    public Button addCellButton;

    @Parameter(label = "Show overlay", callback = "updateLabelMaskOverlay", required = false, description = "Shows overlay of the cytoplasm label mask")
    public Boolean labelMaskOverlayCheckBox = false;

    @Parameter(label = "Results directory", style = "directory")
    public File resultsDirectory;

    @Parameter(label = "Save results (Links+Centroids+LabelMask+Graph)", callback = "createAndSaveGraphResults", persist = false, required = false)
    public Button createAndSaveGraphResultsButton;


    private ImagePlus labelMask;
    private ImagePlus intensityImage;


    public void run()
    {
    }

    private void createGraph()
    {
        labelMask = IJ.openImage(labelMaskFile.toString());
        IJ.log("Create graph editor ...");
        AdjacencyGraphCreator graphCreator = new AdjacencyGraphCreator( labelMask );
        Overlay adjacencyGraphOverlay = graphCreator.createAdjacencyGraphOverlay();

        intensityImage = IJ.openImage(intensityImageFile.toString());
        RoiManager rm = RoiManager.getRoiManager();
        showGraph(adjacencyGraphOverlay, intensityImage, rm);
        rm.runCommand("Associate", "false");
        rm.runCommand("Centered", "false");
        rm.runCommand("UseNames", "true");
        labelMaskOverlayCheckBox = false;
        IJ.setTool("line");
        IJ.run(intensityImage, "Select None", "");
        drawLabelNames();
    }

    private void loadGraph()
    {
        if (intensityImage != null) {
            intensityImage.close();
        }
        intensityImage = IJ.openImage(intensityImageFile.toString());
        labelMask = IJ.openImage(labelMaskFile.toString());
        intensityImage.show();
        RoiManager existingRoiManager = RoiManager.getRoiManager();
        existingRoiManager.close();
        RoiManager rm = new RoiManager();
        IJ.log("Loading a saved graph i.e. edges as rois ...");
        String saveNameLinks = graphFile.toString();
        rm.runCommand("open", saveNameLinks);
        IJ.log("Graph loaded : " + saveNameLinks);
        Overlay overlay = new Overlay();
        for (int nRoi  = 0; nRoi  < rm.getCount(); nRoi ++) {
            overlay.add(rm.getRoi(nRoi));
            overlay.setLabelFontSize(14, "bold");
        }
        showGraph(overlay, intensityImage, rm);
        labelMaskOverlayCheckBox = false;
        IJ.setTool("line");
        IJ.run(intensityImage, "Select None", "");
        drawLabelNames();
    }

    private void addEdge()
    {
        if (intensityImage == null) {
            IJ.showMessage("Run 'Load images and create graph' or 'Load images and graph' to use this option");
            return;
        }
        final Roi roi = intensityImage.getRoi();
        if (roi == null) {
            IJ.showMessage("Draw a line first using line tool to create an edge");
            return;
        }
        if (IJ.getToolName() != "line") {
            IJ.showMessage("Select line tool first, and draw line to add edge");
            return;
        }
        int label1 = labelMask.getProcessor().getPixel(((Line) roi).x1, ((Line) roi).y1);
        int label2 = labelMask.getProcessor().getPixel(((Line) roi).x2, ((Line) roi).y2);
        roi.setStrokeColor( Color.RED);
        roi.setStrokeWidth(3.0F);
        RoiManager rm = RoiManager.getRoiManager();
        roi.setName(label1 + LABEL_SEPARATOR + label2);
        rm.addRoi(roi);
        IJ.log("New edge added : " +  label1 + LABEL_SEPARATOR + label2);
        IJ.run(intensityImage, "Select None", "");
        drawLabelNames();
    }


    private void deleteEdge()
    {
        if (intensityImage == null) {
            IJ.showMessage("Run 'Load images and create graph' or 'Load images and graph' to use this option");
            return;
        }
        final Roi roi = intensityImage.getRoi();
        if (roi == null) {
            IJ.showMessage("Only existing edges could be deleted");
            return;
        }
        if (roi.getName() == null) {
            IJ.showMessage("Select an existing edge first");
            return;
        }

        RoiManager rm = RoiManager.getRoiManager();
        rm.runCommand(intensityImage,"Delete");
        IJ.log("Existing edge deleted : " + roi.getName());
        IJ.run(intensityImage, "Select None", "");
        drawLabelNames();
    }

    private void addCell()
    {
        if (intensityImage == null) {
            IJ.showMessage("Run 'Load images and create graph' or 'Load images and graph' to use this option");
            return;
        }
        Roi roi = intensityImage.getRoi();
        if (roi == null) {
            IJ.showMessage("Draw cell boundaries first using freehand tool");
            return;
        }

        double maxLabelNumber = ImageStatistics.getStatistics(labelMask.getProcessor()).max;
        int newLabelIndex = (int)maxLabelNumber + 1;
        labelMask.setRoi(roi);
        ImageStatistics imageStatistics = labelMask.getStatistics();
        double overlapDrawingMaskLabel = imageStatistics.max; // label of the blob that overlaps with hand drawing
        if (overlapDrawingMaskLabel>0){
            IJ.showMessage("Cell was not added. Selection overlapped with already detected labels");
            IJ.run(labelMask, "Select None", "");
            return;
        }
        else{

            IJ.run(labelMask, "Set...", "value=" + newLabelIndex);
            IJ.log("New cell added  with label: " + newLabelIndex);
            // Remove roi from image
            IJ.run(labelMask, "Select None", "");
            // Update label mask overlay
            updateLabelMaskOverlay();
            IJ.setTool("freehand");
        }
    }

    private void createAndSaveGraphResults() throws IOException {

        if (intensityImage == null) {
            IJ.showMessage("Run 'Load images and create graph' or 'Load images and graph' to use this option");
            return;
        }
        IJ.run("Set Measurements...", "centroid redirect=None decimal=3");
        RoiManager rm = RoiManager.getRoiManager();
        int numEdges = rm.getCount();
        int[] indices = IntStream.range(0,numEdges).toArray();
        ResultsTable rtEdges = new ResultsTable();
        for (int i = 0; i < numEdges; ++i){
            rm.select(indices[i]);
            String currentEdgeName = rm.getName(rm.getSelectedIndex());
            int idx = currentEdgeName.lastIndexOf('_');
            String label1 = currentEdgeName.substring(0, idx);
            String label2 = currentEdgeName.substring(idx+1, currentEdgeName.length());
            rtEdges.addRow();
            rtEdges.setValue("Label1", i, Double.parseDouble(label1));
            rtEdges.setValue("Label2", i, Double.parseDouble(label2));
        }
        AnalyzeRegions analyzeRegions = new AnalyzeRegions();
        ResultsTable resultsTable = analyzeRegions.process(labelMask);
        Variable[] centroidX = resultsTable.getColumnAsVariables("Centroid.X");
        Variable[] centroidY = resultsTable.getColumnAsVariables("Centroid.Y");
        ResultsTable rtCellCentroids = new ResultsTable();
        rtCellCentroids.setColumn("Centroid.X", centroidX);
        rtCellCentroids.setColumn("Centroid.Y", centroidY);
        String intensityImageName = FilenameUtils.removeExtension(intensityImage.getTitle());
        String tableSaveName = resultsDirectory.toString() + File.separator + "Adj_List_" + intensityImageName + ".txt";
        String rawTableSaveName = resultsDirectory.toString() + File.separator + "ID_" + intensityImageName + ".txt";
        String logSaveName = resultsDirectory.toString() + File.separator + "Log_" + intensityImageName + ".txt";
        String saveNameMask = resultsDirectory.toString() + File.separator + intensityImageName + "-labelMask";
        String saveNameLinks = resultsDirectory.toString() + File.separator + intensityImageName + "-edgeSet.zip";
        rtEdges.saveAs(tableSaveName);
        rm.runCommand("save", saveNameLinks);
        IJ.run(intensityImage, "Select All", "");
        IJ.log("Links saved as : " + tableSaveName);
        rtCellCentroids.saveAs(rawTableSaveName);
        IJ.log("Cell centroids saved as : " + tableSaveName);
        IJ.saveAs(labelMask, "Tiff", saveNameMask);
        IJ.log("Label mask saved as : " + saveNameMask + ".tif");
        IJ.log("Edges saved as : " + saveNameLinks);
        IJ.selectWindow("Log");
        IJ.saveAs("Text", logSaveName);
    }

    private void updateLabelMaskOverlay()
    {
        if (intensityImage == null) {
            IJ.showMessage("Run 'Create adjacency graph' to use this option");
            return;
        }

        intensityImage.setHideOverlay(true);
        if(labelMaskOverlayCheckBox){ // not a class!
            IJ.run(labelMask, "3-3-2 RGB", "");
            int x = intensityImage.getWidth() / 2 - labelMask.getWidth() / 2;
            int y = intensityImage.getHeight() / 2 - labelMask.getHeight() / 2;
            Roi roi = new ImageRoi(x, y, labelMask.getProcessor());
            Overlay overlay = new Overlay();
            overlay.add(roi);
            ((ImageRoi)roi).setOpacity(0.7D);
            ((ImageRoi)roi).setZeroTransparent(true);
            Overlay imageOverlay = intensityImage.getOverlay();
            intensityImage.setOverlay(overlay);
            intensityImage.show();
        }
    }

    private void showGraph(Overlay adjacencyGraphOverlay, ImagePlus intensityImage, RoiManager roiManager)
    {
        intensityImage.setOverlay( adjacencyGraphOverlay );
        intensityImage.show();
        roiManager.setOverlay(intensityImage.getOverlay());
        intensityImage.setOverlay(null);
        RoiManager rm = RoiManager.getRoiManager();
        int numEdges = rm.getCount();
        int[] indices = IntStream.range(0,numEdges).toArray();
        for (int i = 0; i < numEdges; ++i){
            rm.select(indices[i]);
            final Roi roi = intensityImage.getRoi();
            String edgeName = roi.getName();
            roiManager.runCommand("Rename", edgeName);
        }
        IJ.log("Please use `Delete edge` button of the 'Create and Edit Graph' UI to delete edges ");
        IJ.run(intensityImage, "Select None", "");
    }

}
