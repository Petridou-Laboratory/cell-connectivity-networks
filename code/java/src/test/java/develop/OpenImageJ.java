package develop;

import net.imagej.ImageJ;
import net.imagej.patcher.LegacyInjector;

public class OpenImageJ
{
	static {
		LegacyInjector.preinit();
	}

	public static void main( String... args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
	}
}
