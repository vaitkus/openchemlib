package com.actelion.research.gui.hidpi;

import com.actelion.research.gui.LookAndFeelHelper;
import com.actelion.research.gui.generic.GenericImage;
import com.actelion.research.util.ColorHelper;
import com.actelion.research.util.Platform;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class HiDPIHelper {
	private static final int[] ICON_SPOT_COLOR = {   // original spot colors used in icon images (bright L&F)
			0x00503CB4, 0x00000000 };

	private static final int[] DARK_LAF_SPOT_COLOR = {   // default replacement spot colors for dark L&F)
			0x00B4A0FF, 0x00E0E0E0 };

	// This is an Apple only solution and needs to be adapted to support high-res displays of other vendors
	private static float sRetinaFactor = -1f;
	private static float sUIScaleFactor = -1f;
	private static int[] sSpotColor = null;

	/**
	 * Macintosh retina display support for Java 7 and newer.
	 *
	 * @return 1.0 on standard resolution devices and 2.0 for retina screens
	 */
	public static float getRetinaScaleFactor() {
		if (!Platform.isMacintosh())
			return 1f;

		/* with Apple-Java-6 this was:
		Object sContentScaleFactorObject = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.contentScaleFactor");
		private static final float sRetinaFactor = (sContentScaleFactorObject == null) ? 1f : ((Float)sContentScaleFactorObject).floatValue();
		*/

		if (sRetinaFactor != -1f)
			return sRetinaFactor;

		sRetinaFactor = 1f;

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice device = env.getDefaultScreenDevice();

		if (System.getProperty("java.version").startsWith("1.")) {
			try {
				Field field = device.getClass().getDeclaredField("scale");
				if (field != null) {
					field.setAccessible(true);
					Object scale = field.get(device);

					if (scale instanceof Integer)
						sRetinaFactor = (Integer) scale;
					else
						System.out.println("Unexpected content scale (not 1 nor 2): " + scale.toString());
					}
				}
			catch (Throwable e) {}
			}
//		else {
/*	the above code gives WARNING under Java 9:
			WARNING: An illegal reflective access operation has occurred
			WARNING: All illegal access operations will be denied in a future release

			If we know, we are on a Mac, we could do something like:

		if (device instanceof CGraphicsDevice) {	// apple.awt.CGraphicsDevice
			final CGraphicsDevice cgd = (CGraphicsDevice)device;

			// this is the missing correction factor, it's equal to 2 on HiDPI a.k.a. Retina displays
			final int scaleFactor = cgd.getScaleFactor();

			// now we can compute the real DPI of the screen
			final double realDPI = scaleFactor * (cgd.getXResolution() + cgd.getYResolution()) / 2;
			}*/
		else {
			GraphicsDevice sd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			sRetinaFactor = (float) sd.getDefaultConfiguration().getDefaultTransform().getScaleX();
		}
		return sRetinaFactor;
	}

	/**
	 * For Windows and Linux this method returns the user defined UI scaling factor.
	 * This is done by judging from the size of the UIManager's Label.font
	 * and comparing it to the unscaled default (13). Typically this factor is larger
	 * than 1.0 on HiDPI devices. For this method to work the Look&Feel must consider
	 * the OS provided setting and scale its fonts accordingly (Substance LaF does).<br>
	 * On the Macintosh this factor is usually 1.0, because HiDPI device support uses
	 * a different mechanism (see getRetinaScaleFactor()).
	 * @return typically 1.0 or 1.25, 1.5, ...
	 */
	public static float getUIScaleFactor() {
		if (sUIScaleFactor == -1) {
			if (getRetinaScaleFactor() != 1f)
				sUIScaleFactor = 1f;
			else {
				float f = 0;
				String dpiFactor = System.getProperty("dpifactor");
				if (dpiFactor != null)
					try { f = Float.parseFloat(dpiFactor); } catch (NumberFormatException nfe) {}
				if (f != 0)
					sUIScaleFactor = f;
				else
					sUIScaleFactor = Platform.isMacintosh() ? 1f : (float)UIManager.getFont("Label.font").getSize() / 12f;
				}
//System.out.println("HiDPIHelper.getUIScaleFactor() retina:"+sRetinaFactor+" UI:"+sUIScaleFactor);
			}

		return sUIScaleFactor;
		}

	/**
	 * This is a convenience method that scales the passed int value with getUIScaleFactor()
	 * and returns the rounded result.
	 * @param value
	 * @return
	 */
	public static int scale(float value) {
		return Math.round(getUIScaleFactor() * value);
		}

	/**
	 * This is a convenience method that scales the passed int value with getUIScaleFactor()
	 * and with getRetinaScaleFactor() and returns the rounded result.
	 * @param value
	 * @return
	 */
	public static int scaleRetinaAndUI(float value) {
		return Math.round(getUIScaleFactor() * getRetinaScaleFactor() * value);
		}

	public static void setIconSpotColors(int[] rgb) {
		sSpotColor = rgb;
		}

	public static int[] getThemeSpotRGBs() {
		int[] rgb = (sSpotColor != null) ? sSpotColor
				: LookAndFeelHelper.isDarkLookAndFeel() ? DARK_LAF_SPOT_COLOR
				: ICON_SPOT_COLOR;

		return rgb;
		}

	/**
	 * If the current look&feel is dark, then colors are adapted for optimal contrast.
	 * @param image
	 * @return
	 */
	public static void adaptForLookAndFeel(GenericImage image) {
		if (sSpotColor != null)
			replaceSpotColors(image, sSpotColor);
		else if (LookAndFeelHelper.isDarkLookAndFeel())
			replaceSpotColors(image, DARK_LAF_SPOT_COLOR);
	}

	public static void disableImage(GenericImage image) {
		Color gray = LookAndFeelHelper.isDarkLookAndFeel() ?
				ColorHelper.brighter(UIManager.getColor("Panel.background"), 0.8f)
				: ColorHelper.darker(UIManager.getColor("Panel.background"), 0.8f);
		int grayRGB = 0x00FFFFFF & gray.getRGB();

		for (int x=0; x<image.getWidth(); x++) {
			for (int y=0; y<image.getHeight(); y++) {
				int argb = image.getRGB(x, y);
				image.setRGB(x, y, (0xFF000000 & argb) + grayRGB);
			}
		}
	}

	private static void replaceSpotColors(GenericImage image, int[] altRGB) {
		for (int x=0; x<image.getWidth(); x++) {
			for (int y=0; y<image.getHeight(); y++) {
				int argb = image.getRGB(x, y);
				int rgb = argb & 0x00FFFFFF;
				for (int i=0; i<ICON_SPOT_COLOR.length; i++) {
					if (rgb == ICON_SPOT_COLOR[i]) {
						image.setRGB(x, y, (0xFF000000 & argb) + altRGB[i]);
						break;
					}
				}
			}
		}
	}
}
