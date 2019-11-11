package Lab3;

import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/**
 * Opens an image window and adds a panel below the image
 */

public class GRDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = { Mode.ORIGINAL, Mode.ROT_KANAL, Mode.NEGATIV, Mode.GRAUSTUFEN, Mode.BITONAR_SW, Mode.BITONAR_SW_5,
			Mode.BITONAR_SW_10, Mode.BITONAR_HZ, Mode.SEPIA, Mode.SIX_COLORS };

	public static void main(String args[]) {

		IJ.open("C:\\Users\\justi\\workspaceJava\\Eclipse\\Uni\\GDM-Labs\\GDM Labs\\Bear.jpg");
		// IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U3 pw = new GRDM_U3();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp == null)
			imp = WindowManager.getCurrentImage();
		if (imp == null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}

	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int[]) ip.getPixels()).clone();
	}

	class CustomCanvas extends ImageCanvas {

		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class

	class CustomWindow extends ImageWindow implements ItemListener {

		private String method;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			// JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {

			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			}

		}

		private int cutOverflow(double value) {
			if (value > 255) {
				value = 255;
			} else if (value < 0) {
				value = 0;
			}
			return (int) value;
		}

		private double[] convertToYUV(int r, int g, int b) {
			double y = 0.299 * r + 0.587 * g + 0.114 * b;
			double u = (b - y) * 0.493;
			double v = (r - y) * 0.877;

			return new double[] { y, u, v };
		}

		private int[] convertToRGB(double y, double u, double v) {
			int r = (int) (y + v / 0.877);
			int b = (int) (y + u / 0.493);
			int g = (int) (1 / 0.587 * y - 0.299 / 0.587 * r - 0.114 / 0.587 * b);
			return new int[] { r, g, b };
		}

		public int[][] getGreyColors(int count) {
			int[][] grey = new int[count][3];

			for (int i = 1; i < count; i++) {

				double u = (double) 1 / i;
				grey[i][0] = (int) (255 * u);
				grey[i][1] = (int) (255 * u);
				grey[i][2] = (int) (255 * u);
			}
			
			return grey;
		}

		public void greyImage(int colors[][], int[] pixels) {

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pos = y * width + x;
					int argb = origPixels[pos]; // Lesen der Originalwerte

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;

					double yuv[] = convertToYUV(r, g, b);

					double Y = yuv[0];
					double U = yuv[1];
					double V = yuv[2];

					int rgb[] = convertToRGB(Y, U, V);
					
//					for(int k = 0;k<3;k++) {
//						double avg = (r + g + b) / 3;
//						double distance = Math.abs(colors[0][1] - avg);
//						int idx = 0;
//						for (int i = 0; i < colors.length; i++) {
//							int cdistance = (int) Math.abs(grey[i] - avg);
//							if (cdistance < distance) {
//								idx = i;
//								distance = cdistance;
//							}
//						}
//						rgb[k] = (int) colors[idx];
//						
//					}


//					rgb[1] = (int) grey[idx];
//					rgb[2] = (int) grey[idx];

					pixels[pos] = (0xFF << 24) | (cutOverflow(rgb[0]) << 16) | (cutOverflow(rgb[1]) << 8)
							| cutOverflow(rgb[2]);
				}
			}
		}

		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[]) ip.getPixels();

			if (method.equals(Mode.ORIGINAL)) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;

						pixels[pos] = origPixels[pos];
					}
				}
			}

			if (method.equals(Mode.ROT_KANAL)) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						// int g = (argb >> 8) & 0xff;
						// int b = argb & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255
						// begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
			}

			if (method.equals(Mode.GRAUSTUFEN)) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						double yuv[] = convertToYUV(r, g, b);

						double Y = yuv[0];
						double U = yuv[1];
						double V = yuv[2];

						U = 0;
						V = 0;

						int rgb[] = convertToRGB(Y, U, V);
						pixels[pos] = (0xFF << 24) | (cutOverflow(rgb[0]) << 16) | (cutOverflow(rgb[1]) << 8)
								| cutOverflow(rgb[2]);
					}
				}
			}

			if (method.equals(Mode.NEGATIV)) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						double yuv[] = convertToYUV(r, g, b);

						double Y = yuv[0];
						double U = yuv[1];
						double V = yuv[2];

						int rgb[] = convertToRGB(Y, U, V);

						rgb[0] = Math.abs(rgb[0] - 255);
						rgb[1] = Math.abs(rgb[1] - 255);
						rgb[2] = Math.abs(rgb[2] - 255);

						pixels[pos] = (0xFF << 24) | (cutOverflow(rgb[0]) << 16) | (cutOverflow(rgb[1]) << 8)
								| cutOverflow(rgb[2]);
					}
				}
			}

			if (method.equals(Mode.BITONAR_SW)) {
				greyImage(getGreyColors(2), pixels);
			}

			if (method.equals(Mode.BITONAR_SW_5)) {
				greyImage(getGreyColors(5), pixels);
			}

			if (method.equals(Mode.BITONAR_SW_10)) {
				greyImage(getGreyColors(10), pixels);
			}

			if (method.equals(Mode.BITONAR_HZ)) {
				
				int quant_error = 0;

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						double yuv[] = convertToYUV(r, g, b);

						double Y = yuv[0];
						double U = yuv[1];
						double V = yuv[2];

						int rgb[] = convertToRGB(Y, U, V);

						double echtWert = (rgb[0] + rgb[1] + rgb[2]) / 3;
						int value = (int) echtWert+quant_error > 128 ? 255 : 0;

						quant_error = (int) (value - echtWert);
						

						pixels[pos] = (0xFF << 24) | (cutOverflow(value) << 16) | (cutOverflow(value) << 8)
								| cutOverflow(value);
					}
				}
			}

			if (method.equals(Mode.SEPIA)) {
				double sepiaIntensity = 0.7;
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						double yuv[] = convertToYUV(r, g, b);

						double Y = yuv[0];
						double U = yuv[1];
						double V = yuv[2];

						int rgb[] = convertToRGB(Y, U, V);

						rgb[0] = (int) ((int) (0.393 * rgb[0] + 0.769 * rgb[1] + 0.189 * rgb[2]) * sepiaIntensity);
						rgb[1] = (int) ((int) (0.349 * rgb[0] + 0.686 * rgb[1] + 0.168 * rgb[2]) * sepiaIntensity);
						rgb[2] = (int) ((int) (0.272 * rgb[0] + 0.534 * rgb[1] + 0.131 * rgb[2]) * sepiaIntensity);

						pixels[pos] = (0xFF << 24) | (cutOverflow(rgb[0]) << 16) | (cutOverflow(rgb[1]) << 8)
								| cutOverflow(rgb[2]);
					}
				}
			}

			if (method.equals(Mode.SIX_COLORS)) {

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; // Lesen der Originalwerte

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						double yuv[] = convertToYUV(r, g, b);

						double Y = yuv[0];
						double U = yuv[1];
						double V = yuv[2];

						int rgb[] = convertToRGB(Y, U, V);

//						rgb[0] = Math.abs(rgb[0]-255);
//						rgb[1] = Math.abs(rgb[1]-255);
//						rgb[2] = Math.abs(rgb[2]-255);

						pixels[pos] = (0xFF << 24) | (cutOverflow(rgb[0]) << 16) | (cutOverflow(rgb[1]) << 8)
								| cutOverflow(rgb[2]);
					}
				}
			}

		}

	} // CustomWindow inner class
}
