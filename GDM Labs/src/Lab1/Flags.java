package Lab1;

import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)

public class Flags implements PlugIn {

	private int width = 566;
	private int height = 400;

	final static String[] choices = { "Schwarzes Bild", "Gelbes Bild", "Belgische Flagge", "USA Flagge",
			"Schwarz/Rot/Blau Gradient", "Czech Flagge" };

	private String choice;

	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen
		ij.exitWhenQuitting(true);

		Flags imageGeneration = new Flags();
		imageGeneration.run("");
	}

	public void run(String arg) {

		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();

		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[]) ip.getPixels();

//		dialog();
//
//		////////////////////////////////////////////////////////////////
//		// Hier bitte Ihre Aenderungen / Erweiterungen
//
//		if (choice.equals("Schwarzes Bild")) {
//			generateBlackImage(this.width, this.height, pixels);
//		} else  if (choice.equals("Gelbes Bild")) {
//			generateImage(255,255,0,this.width, this.height, pixels);
//		}else  if (choice.equals("Belgische Flagge")) {
//			generateBelgianFlag(pixels);
//		}else  if (choice.equals("USA Flagge")) {
//		generateUsaFlag(pixels);
//		}else if (choice.equals("Schwarz/Rot/Blau Gradient")) {
//		generateGradient1(pixels);
//		}else if (choice.equals("Czech Flagge")) {
//		generateCzechFlag(pixels);
//		}

		// generateBelgianFlag(pixels);
		// generateUsaFlag(pixels);
		// generateGradient1(pixels);
		generateCzechFlag(pixels);
		////////////////////////////////////////////////////////////////////

		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}

	private void generateBlackImage(int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) {
				int pos = y * width + x; // Arrayposition bestimmen

				int r = 0;
				int g = 0;
				int b = 0;

				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	private void generateImage(int red, int green, int blue, int width, int height, int[] pixels) {
		// Schleife ueber die y-Werte
		for (int y = 0; y < height; y++) {
			// Schleife ueber die x-Werte
			for (int x = 0; x < width; x++) {
				int pos = y * this.width + x; // Arrayposition bestimmen
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
			}
		}
	}

	private void generateBelgianFlag(int[] pixels) {
		generateImage(255, 0, 0, this.width, this.height, pixels);
		generateImage(255, 255, 0, (int) (this.width * 0.66), this.height, pixels);
		generateImage(0, 0, 0, (int) (this.width * 0.33), this.height, pixels);
	}

	private void generateUsaFlag(int[] pixels) {
		// generate Stripes
		Boolean isRed = true;
		int stripes = 13;
		for (int i = 0; i < stripes; i++) {
			if (isRed) {
				generateImage(255, 0, 0, this.width, this.height - i * this.height / stripes, pixels);

			} else {
				generateImage(255, 255, 255, this.width, this.height - i * this.height / stripes, pixels);
			}
			isRed = !isRed;
		}

		// generate blue part
		generateImage(0, 0, 255, (int) (this.width * 0.45), (int) (this.height * 0.5), pixels);
	}

	private void generateGradient1(int[] pixels) {

		for (int y = 0; y < this.height; y++) {
			// Schleife ueber die x-Werte
			int red = 0;
			int green = 0;
			int blue = 0;

			for (int x = 0; x < this.width; x++) {
				int pos = y * this.width + x; // Arrayposition bestimmen
				// Werte zurueckschreiben
				pixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
				if (red != 255) {
					red = (int) (((double) x / (double) this.width) * 255);
				}
				if (blue != 255) {
					blue = (int) (((double) y / (double) this.height) * 255);
				}
			}
		}
	}

	private void generateCzechFlag(int[] pixels) {
		// generate Background
		generateImage(255, 0, 0, this.width, (int) (this.height), pixels);
		generateImage(255, 255, 255, this.width, (int) (this.height * 0.5), pixels);

		// generate Triangle
		// andere H�lfte fehlt
		int triWidth = (int) (this.width * 0.4);
		for (int y = 0; y < this.height; y++) {


			int red = 0;
			int green = 0;
			int blue = 0;

			for (int x = 0; x < triWidth; x++) {
				// top half of triangle
				if (y < this.height/2 && x < triWidth-x) {

					blue = 255;
					int pos = y * this.width + x; // Arrayposition bestimmen
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;

				}else if(y>=this.height/2){
					
					if(x < triWidth-(y-this.height/2)) {						
						blue = 200;
						int pos = y * this.width + x; // Arrayposition bestimmen
						// Werte zurueckschreiben
						pixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
					}
				}

			}
		}

	}

	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");

		gd.addChoice("Bildtyp", choices, choices[0]);

		gd.showDialog(); // generiere Eingabefenster

		choice = gd.getNextChoice(); // Auswahl uebernehmen

		if (gd.wasCanceled())
			System.exit(0);
	}
}
