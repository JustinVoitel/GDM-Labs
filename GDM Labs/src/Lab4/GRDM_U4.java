package Lab4;

import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class GRDM_U4 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = { "Wischen", "Weiche Blende", "Overlay", "Schieb-Blende", "Chroma Key",
			"Geometrisch" };
	boolean skipDialog = true;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB + STACK_REQUIRED;
	}

	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
		ij.exitWhenQuitting(true);

		IJ.open("F:\\workspaceJava\\Eclipse\\Uni\\GDM-Labs\\GDM Labs\\ImageJ\\Bilder\\StackB.zip");

		GRDM_U4 sd = new GRDM_U4();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();

		int length = stack_B.getSize();
		int width = B_ip.getWidth();
		int height = B_ip.getHeight();

		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		// OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...", "");

		// Film A wird dazugeladen
//		String dateiA = od_A.getFileName();
//		if (dateiA == null) return; // Abbruch
//		String pfadA = od_A.getDirectory();
//		ImagePlus A = o.openImage(pfadA,dateiA);

		ImagePlus A = o.openImage("F:/workspaceJava/Eclipse/Uni/GDM-Labs/GDM Labs/ImageJ/Bilder/", "StackA.zip");

		if (A == null)
			return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height) {
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}

		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length, stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg = Erg.getStack();

		int methode = 5;
		// Dialog fuer Auswahl des Ueberlagerungsmodus
		if (!skipDialog) {

			GenericDialog gd = new GenericDialog("Überlagerung");
			gd.addChoice("Methode", choices, "");
			gd.showDialog();
			String s = gd.getNextChoice();

			if (s.equals("Wischen"))
				methode = 1;
			if (s.equals("Weiche Blende"))
				methode = 2;
			if (s.equals("Overlay"))
				methode = 3;
			if (s.equals("Schieb-Blende"))
				methode = 4;
			if (s.equals("Chroma Key"))
				methode = 5;
			if (s.equals("Geometrisch"))
				methode = 6;
		}

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z = 1; z <= length; z++) {
			pixels_B = (int[]) stack_B.getPixels(z);
			pixels_A = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);
			// System.out.println(z);
			int pos = 0;
			for (int y = 0; y < height; y++)
				for (int x = 0; x < width; x++, pos++) {
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);

					if (methode == 1) {
						if (y + 1 > (z - 1) * (double) width / (length - 1))
							pixels_Erg[pos] = pixels_B[pos];
						else
							pixels_Erg[pos] = pixels_A[pos];
					}

					if (methode == 2) {
						int r = (int) ((rA * ((double) z / 100)) + (rB * (100 - (double) z) / 100));
						int g = (int) ((gA * ((double) z / 100)) + (gB * (100 - (double) z) / 100));
						int b = (int) ((bA * ((double) z / 100)) + (bB * (100 - (double) z) / 100));

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}

					if (methode == 3) {
						int r = overlay(rB, rA);
						int g = overlay(gB, gA);
						int b = overlay(bB, bA);

						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}

					if (methode == 4) {

						if (x + 1 > (z - 1) * (double) width / (length - 1))
							try {
								pixels_Erg[pos] = pixels_B[(int) (pos - (z - 1) * (double) width / (length - 1))];

							} catch (Exception e) {
								// TODO: handle exception
							}
						else
							try {

								pixels_Erg[pos] = pixels_A[(int) (pos - (z - 1) * (double) width / (length - 1))];
							} catch (Exception e) {
								// TODO: handle exception
							}

					}

					if (methode == 5) {
						int innerLimit = 50;
						int outerlimit = 65;
						double yuvA[] = convertToYUV(rA, gA, bA);
						double yuvK[] = convertToYUV(240, 176, 64);

						double uK = yuvK[1], vK = yuvK[2];
						double uA = yuvA[1], vA = yuvA[2];

						double keyToADist = Math.sqrt(
								Math.pow(((double) uA - (double) uK), 2) + Math.pow(((double) vA - (double) vK), 2));

						// System.out.println("dist: "+keyToADist);

						// im ring -> ausblenden
						if (keyToADist < outerlimit && keyToADist > innerLimit) {
							double proportion = 1-((keyToADist-innerLimit)/innerLimit-outerlimit);
							//System.out.println(1-((keyToADist-innerLimit)/20));

							int r = (int) ((rB * ((double) proportion / 100)) + (rA * (100 - (double) proportion) / 100));
							int g = (int) ((gB * ((double) proportion / 100)) + (gA * (100 - (double) proportion) / 100));
							int b = (int) ((bB * ((double) proportion / 100)) + (bA * (100 - (double) proportion) / 100));

							pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
						} else if (keyToADist < innerLimit) {
							pixels_Erg[pos] = pixels_B[pos];
						} else {
							pixels_Erg[pos] = pixels_A[pos];
						}
					}

					if (methode == 6) {
						//TODO:
					}

				}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();

	}

	public int overlay(int v, int h) {
		if (h < 128) {
			return (int) ((double) v * (double) h / 128);
		} else {
			return (int) (255 - ((255 - (double) v) * (255 - (double) h) / 128));
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

}
