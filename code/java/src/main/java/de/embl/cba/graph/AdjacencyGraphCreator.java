package de.embl.cba.graph;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import inra.ijpb.label.LabelImages;
import inra.ijpb.label.RegionAdjacencyGraph;
import inra.ijpb.measure.region2d.Centroid;
import net.imagej.patcher.LegacyInjector;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static de.embl.cba.graph.GraphCommand.LABEL_SEPARATOR;

public class AdjacencyGraphCreator
{
	static {
		LegacyInjector.preinit();
	}

	private final ImagePlus labelMask;

	public AdjacencyGraphCreator( ImagePlus labelMask )
	{
		this.labelMask = labelMask;
	}

	public Overlay createAdjacencyGraphOverlay()
	{

		final Set< RegionAdjacencyGraph.LabelPair > labelPairs = RegionAdjacencyGraph.computeAdjacencies( labelMask );
		ImageProcessor image = labelMask.getProcessor();
		int[] labels = LabelImages.findAllLabels(image);
		Map<Integer, Integer> labelMap = LabelImages.mapLabelIndices(labels);
		double[][] centroids = Centroid.centroids(image, labels);
		Overlay overlay = new Overlay();
		Iterator<RegionAdjacencyGraph.LabelPair> labelPairIterator = labelPairs.iterator();

		while(labelPairIterator.hasNext()) {
			RegionAdjacencyGraph.LabelPair pair = labelPairIterator.next();
			int ind1 = labelMap.get(pair.label1);
			int ind2 = labelMap.get(pair.label2);
			int x1 = (int)centroids[ind1][0];
			int y1 = (int)centroids[ind1][1];
			int x2 = (int)centroids[ind2][0];
			int y2 = (int)centroids[ind2][1];
			Roi roi = new Line(x1, y1, x2, y2);
			roi.setStrokeColor( Color.PINK);
			roi.setName(pair.label1 + LABEL_SEPARATOR + pair.label2);
			roi.setStrokeWidth(3.0F);
			overlay.add(roi);
			overlay.setLabelFontSize(14, "bold");
		}

		return overlay;
	}
}
