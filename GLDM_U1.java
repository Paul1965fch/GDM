import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1 implements PlugIn {
	
	final static String[] choices = {
		"Schwarzes Bild",
		"Gelbes Bild",
		"Schwarz/Weiss Verlauf",
		"Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
		"Italienische Fahne",
		"Bahamische Fahne",
		"Japanische Fahne",
		"Japanische Fahne mit weichen Kanten",
		"Schwarze diagonale"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1 imageGeneration = new GLDM_U1();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		
		int width  = 566;  // Breite
		int height = 400;  // Hoehe
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		
		dialog();
		
		////////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen
		
		if ( choice.equals("Schwarzes Bild") ) {
			
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					int r = 0;
					int g = 0;
					int b = 0;
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		else if ( choice.equals( choices[1] ) ) {
			System.out.println("Gelbes bild!");

			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen

					int r = 255;
					int g = 255;
					int b = 0;

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		else if ( choice.equals( choices[2] )) { // schwarz-weiß-verlauf
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen

					int r, g, b;
					
					r = g = b = 255 * x / (width - 1);
								
					// System.out.println(255 * x / width);

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		else if ( choice.equals( choices[3] ) ) { // horiz. schwarz-blau, vert. schwar-rot
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen

					int r = 255 * x / (width - 1);
					int g = 0;
					int b = 255 * y / (height - 1);

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		else if ( choice.equals( choices[4] )) { // italienische flagge
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					int r, g, b;
					
					if (x <= width / 3) {
						r = 255;
						g = 0;
						b = 0;
					}
					else if (x <= width / 3 * 2) {
						r = 255;
						g = 255;
						b = 255;
					}
					else {
						r = 0;
						g = 255;
						b = 0;
					}
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		else if ( choice.equals(choices[5]) ) { // bahamische flagge
			// schwarzes dreieck auf der linken seite
			// blau-gelb-blau
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen

					int r, g, b;
					
					if (y < height / 3 || y > height / 3 * 2) {
						r = 0;
						g = 0;
						b = 255;
					}
					else {
						r = 255;
						g = 255;
						b = 0;
					}
					
					if ( y <= height / 2 ) {
						if (x < y) {
							r = g = b = 0;
						}
					}
					else {
						if (x < height - y) {
							r = g = b = 0;
						}
					}

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		else if ( choice.equals(choices[6]) ) { // japanische flagge
			int rad = height / 6 * 2;
			int centerX = width / 2;
			int centerY = height / 2;
			
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen

					int r, g, b;
					
					if (Math.sqrt(Math.pow((x - centerX), 2) + Math.pow((y - centerY), 2)) <= rad) {
						r = 255;
						g = b = 0;
					} else {
						r = g = b = 255;
					}

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		else if ( choice.equals(choices[7]) ) { // japanische flagge, antialiasing
			double rad = height / 6 * 2;
			int centerX = width / 2;
			int centerY = height / 2;
			
			int alias = 2;
			
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen

					int r, g, b;
					double dist = Math.sqrt(Math.pow((x - centerX), 2) + Math.pow((y - centerY), 2));
					
					if (dist <= rad) {
						r = 255;
						g = b = 0;
					} 
					else if (dist < rad + alias) {
						r = 255;
						g = b = (int) (255 * ((dist - rad)) / alias); // / 10);
						
						if ( r == g && r == b ) {
							System.out.print("dist: " + dist + ", rad: " + rad + ", rgb: " + r + '\n');
						}
					}
					else {
						r = g = b = 255;
					}

					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		else if ( choice.equals(choices[8]) ) { // diagonale Linie
			int thick = 2;
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					int r, g, b;
					// Zeile y * Steigungsfaktor (in diesem Fall die diagonale durch das Fenster)
					if(x > (height - y) * width/height - thick && x < (height - y) * width/height + thick){
						r = 0;
						g = 0;
						b = 0;
					}
					else{
						r = 255;
						g = 255;
						b = 255;
					}
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		////////////////////////////////////////////////////////////////////
		
		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}
	
	
	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		
		gd.addChoice("Bildtyp", choices, choices[0]);
		
		
		gd.showDialog();	// generiere Eingabefenster
		
		choice = gd.getNextChoice(); // Auswahl uebernehmen
		
		if (gd.wasCanceled())
			System.exit(0);
	}
}

