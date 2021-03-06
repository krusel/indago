package com.indago.data.segmentation.visualization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.display.projector.RandomAccessibleProjector2D;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.Type;

public class VisualizeLabeling {

	public static < C extends Type< C >, L > void colorLabels( final ImgLabeling< L, ? > labeling, final Iterator< C > colors, final RandomAccessibleInterval< C > regionsImg ) {
		final HashMap< Set< ? >, C > colorTable = new HashMap< Set< ? >, C >();
		final LabelingMapping< ? > mapping = labeling.getMapping();
		final int numLists = mapping.numSets();
		for ( int i = 0; i < numLists; ++i ) {
			final Set< ? > list = mapping.labelsAtIndex( i );
			colorTable.put( list, colors.next() );
		}

		new RandomAccessibleProjector2D< LabelingType< L >, C >( 0, 1, labeling, regionsImg, new LabelingTypeConverter< C >( colorTable ) ).map();
	}

	public static class LabelingTypeConverter< T extends Type< T > > implements Converter< LabelingType< ? >, T > {

		private final HashMap< Set< ? >, T > colorTable;

		public LabelingTypeConverter( final HashMap< Set< ? >, T > colorTable ) {
			this.colorTable = colorTable;
		}

		@Override
		public void convert( final LabelingType< ? > input, final T output ) {
			final T t = colorTable.get( input );
			if ( t != null ) output.set( t );
		}
	}
}
