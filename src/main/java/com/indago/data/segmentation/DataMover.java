package com.indago.data.segmentation;

import java.util.List;

import io.scif.img.ImgIOException;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * @author jug
 *
 */
public class DataMover {

	public static < T extends Type< T >> void copy( final RandomAccessible< T > source, final IterableInterval< T > target ) {
		// create a cursor that automatically localizes itself on every move
		final Cursor< T > targetCursor = target.localizingCursor();
		final RandomAccess< T > sourceRandomAccess = source.randomAccess();

		// iterate over the input cursor
		while ( targetCursor.hasNext() ) {
			// move input cursor forward
			targetCursor.fwd();

			// set the output cursor to the position of the input cursor
			sourceRandomAccess.setPosition( targetCursor );

			// set the value of this pixel of the output image, every Type
			// supports T.set( T type )
			targetCursor.get().set( sourceRandomAccess.get() );
		}
	}

	public static < T1 extends Type< T1 >, T2 extends Type< T2 >> void copy( final RandomAccessible< T1 > source, final IterableInterval< T2 > target, final Converter< T1, T2 > converter ) {
		// create a cursor that automatically localizes itself on every move
		final Cursor< T2 > targetCursor = target.localizingCursor();
		final RandomAccess< T1 > sourceRandomAccess = source.randomAccess();

		// iterate over the input cursor
		while ( targetCursor.hasNext() ) {
			// move input cursor forward
			targetCursor.fwd();

			// set the output cursor to the position of the input cursor
			sourceRandomAccess.setPosition( targetCursor );

			// set converted value
			converter.convert( sourceRandomAccess.get(), targetCursor.get() );
		}
	}

	public static < T extends Type< T >> void copy( final RandomAccessible< T > source, final RandomAccessibleInterval< T > target ) {
		copy( source, Views.iterable( target ) );
	}

	public static < T extends NativeType< T >> Img< T > createEmptyArrayImgLike( final RandomAccessibleInterval< ? > blueprint, final T type ) {
		final long[] dims = new long[ blueprint.numDimensions() ];
		for ( int i = 0; i < blueprint.numDimensions(); i++ ) {
			dims[ i ] = blueprint.dimension( i );
		}
		final Img< T > ret = new ArrayImgFactory< T >().create( dims, type );
		return ret;
	}

	public static < T extends RealType< T > & NativeType< T > > Img< T > stackThemAsFrames( final List< Img< T >> imageList ) throws ImgIOException, IncompatibleTypeException, Exception {

		Img< T > stack = null;

		final long width = imageList.get( 0 ).dimension( 0 );
		final long height = imageList.get( 0 ).dimension( 1 );
		final long channels = imageList.get( 0 ).dimension( 2 );
		final long frames = imageList.size();

		stack = new ArrayImgFactory< T >().create( new long[] { width, height, channels, frames }, imageList.get( 0 ).firstElement() );

		// Add images to stack...
		int i = 0;
		for ( final RandomAccessible< T > image : imageList ) {
			final RandomAccessibleInterval< T > viewZSlize = Views.hyperSlice( stack, 3, i );

			for ( int c = 0; c < channels; c++ ) {
				final RandomAccessibleInterval< T > viewChannel = Views.hyperSlice( viewZSlize, 2, c );
				final IterableInterval< T > iterChannel = Views.iterable( viewChannel );

				if ( image.numDimensions() < 3 ) {
					if ( c > 0 ) { throw new ImgIOException( "Not all images to be loaded contain the same number of color channels!" ); }
					DataMover.copy( image, iterChannel );
				} else {
					DataMover.copy( Views.hyperSlice( image, 2, c ), iterChannel );
				}
			}
			i++;
		}

		return stack;
	}

	@SuppressWarnings( "unchecked" )
	public static < ST extends RealType< ST >, TT extends NativeType< TT > > void convertAndCopy(
			final RandomAccessible< ST > source,
			final Img< TT > target ) throws Exception {
		final ST sourceType = source.randomAccess().get();
		final TT targetType = target.firstElement();

		// if source and target are of same type -> use copy since convert is not needed...
		if ( sourceType.getClass().isInstance( targetType ) ) {
			DataMover.copy( source, ( IterableInterval< ST > ) target );
			return;
		}

		// implemented conversion cases follow here...

		boolean throwException = false;
		if ( sourceType instanceof FloatType ) {

			if ( targetType instanceof DoubleType ) { // FloatType --> DoubleType
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				final int v;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );

					( ( DoubleType ) targetCursor.get() ).set( ( ( FloatType ) sourceRandomAccess.get() ).getRealDouble() );
				}
			} else // FloatType --> IntType
			if ( targetType instanceof IntType ) {
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				final int v;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );

					( ( IntType ) targetCursor.get() ).set( Math.round( ( ( FloatType ) sourceRandomAccess.get() ).getRealFloat() ) );
				}
			} else // FloatType --> ARGBType
			if ( targetType instanceof ARGBType ) {
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				int v;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );
					try {
						v = Math.round( ( ( FloatType ) sourceRandomAccess.get() ).get() * 255 );
					} catch ( final ArrayIndexOutOfBoundsException e ) {
						v = 255; // If image-sizes do not match we pad with white pixels...
					}
					if ( v > 255 ) { throw new Exception( "TODO: in this case (source in not within [0,1]) I did not finish the code!!! Now would likely be a good time... ;)" ); }
					( ( ARGBType ) targetCursor.get() ).set( ARGBType.rgba( v, v, v, 255 ) );
				}
			} else {
				throwException = true;
			}

		} else if ( sourceType instanceof UnsignedShortType ) {

			// UnsignedShortType --> FloatType
			if ( targetType instanceof FloatType ) {
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				final int v;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );

					( ( FloatType ) targetCursor.get() ).set( ( ( UnsignedShortType ) sourceRandomAccess.get() ).getRealFloat() );
				}
			} else if ( targetType instanceof DoubleType ) {
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				final int v;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );

					( ( DoubleType ) targetCursor.get() ).set( ( ( UnsignedShortType ) sourceRandomAccess.get() ).getRealDouble() );
				}
			} else // UnsignedShortType --> IntType
			if ( targetType instanceof IntType ) {
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				final int v;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );

					( ( IntType ) targetCursor.get() ).set( ( ( UnsignedShortType ) sourceRandomAccess.get() ).get() );
				}
			} else if ( targetType instanceof ARGBType ) {
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				int v;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );
					try {
						v =
								Math.round( ( ( UnsignedShortType ) sourceRandomAccess.get() ).getRealFloat() * 255 );
					} catch ( final ArrayIndexOutOfBoundsException e ) {
						v = 255; // If image-sizes do not match we pad with white pixels...
					}
					if ( v > 255 ) { throw new Exception( "TODO: in this case (source in not within [0,1]) I did not finish the code!!! Now would likely be a good time... ;)" ); }
					( ( ARGBType ) targetCursor.get() ).set( ARGBType.rgba( v, v, v, 255 ) );
				}
			} else {
				throwException = true;
			}
		} else if ( sourceType instanceof ARGBType ) {

			// ARGBType --> FloatType
			if ( targetType instanceof ARGBType ) {
				final Cursor< TT > targetCursor = target.localizingCursor();
				final RandomAccess< ST > sourceRandomAccess = source.randomAccess();
				double v;
				int intRGB;
				while ( targetCursor.hasNext() ) {
					targetCursor.fwd();
					sourceRandomAccess.setPosition( targetCursor );
					intRGB = ( ( ARGBType ) sourceRandomAccess.get() ).get();
					v =
							0.2989 * ARGBType.red( intRGB ) + 0.5870 * ARGBType.green( intRGB ) + 0.1140 * ARGBType.blue( intRGB );
					v /= 255;
					( ( ARGBType ) targetCursor.get() ).set( ARGBType.rgba( v, v, v, 255 ) );
				}
			} else {
				throwException = true;
			}
		} else {
			throwException = true;
		}

		if ( throwException )
			throw new Exception( "Convertion from " + sourceType.getClass().toString() + " to " + targetType.getClass() + " not implemented!" );
	}
}
