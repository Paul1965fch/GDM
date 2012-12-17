import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U3 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {
		"Original", 
		"Rot-Kanal", 
		"Negativ", 
		"Graustufen",
		"10 Graustufen",
		"Binärbild",
		"Binärbild mit Fehlerdiffusion",
		"Sepia",
		"Auf 6 Farben reduzieren"
	};


	public static void main(String args[]) {

		//IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");
		IJ.open("/Users/Ernesto/Desktop/bear.jpg");

		
		GRDM_U3 pw = new GRDM_U3();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
		CustomCanvas cc = new CustomCanvas(imp);

		storePixelValues(imp.getProcessor());

		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
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
			//JPanel panel = new JPanel();
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


		private void changePixelValues(ImageProcessor ip) {

			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						
						pixels[pos] = origPixels[pos];
					}
				}
			}
			else if (method.equals("Rot-Kanal")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						//int g = (argb >>  8) & 0xff;
						//int b =  argb        & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			else if (method.equals("Negativ")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >>  8) & 0xff;
						int b =  argb        & 0xff;

						r = 255 - r;
						g = 255 - g;
						b = 255 - b;

						pixels[pos] = (0xFF<<24) | (r << 16) | (g << 8) | b;
					}
				}
			} // end "Negativ"
			else if (method.equals("Graustufen")) {
				for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pos = y * width + x;
					int argb = origPixels[pos]; 

					int r = (argb >> 16) & 0xff;
					int g = (argb >> 8) & 0xff;
					int b = argb & 0xff;

					//Abstand zwischen den grauwerten
					int border = 255*3 / 5;
					int greyNumber = 255 / 5;
			

					// Die Berechnung ist dann der Wert aller Pixel durch
					// den Schwellenwert mal die Anzahl der Graustufen
					int value = (r + g + b) / border* 5;

					// Wert wird Pixeln zugeordnet
					int rn = value;
					int gn = value;
					int bn = value;

					pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
				}
			}
			} // end "Graustufen"
			else if (method.equals("10 Graustufen")) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						//genau wie 5 nur mit 10 werten
						int pos = y * width + x;
						int argb = origPixels[pos]; 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						//Abstand zwischen den grauwerten
						int border = 255*3 / 10;
						int greyNumber = 255 / 10;
				

						// Die Berechnung ist dann der Wert aller Pixel durch
						// den Schwellenwert mal die Anzahl der Graustufen
						int value = (r + g + b) / border* 10;

						// Wert wird Pixeln zugeordnet
						int rn = value;
						int gn = value;
						int bn = value;

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8) | bn;
					}
				}
				
			}//end 10 Graustufen
			else if (method.equals("Binärbild")) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];
						
						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						// durchschnitt (grauwert) berechnen
						int avg = (r + g + b) / 3;
						
						// je nachdem ob näher an schwarz oder weiß
						// wird der pixel in einer der beiden farben gefärbt
						if (avg >= 128) {
							avg = 255;
						} else avg = 0;
						
						pixels[pos] = (0xFF<<24) | (avg << 16) | (avg << 8) | avg;
					}
				}
			} // end binärbild
			else if (method.equals("Binärbild mit Fehlerdiffusion")) {
				int lineError = 0;
				
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];
						
						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						// der grauwert ist der mittelwert aus allen kanälen
						int avg = (r + g + b) / 3;
						int color;
						
						// wenn grau +/- fehler näher an weiß ist als
						// an schwarz, soll der pixel weiß werden
						
						if (avg + lineError >= 128) {
							// der wert weiß ist zu hoch
							// das korrigieren wir mit einem negativen fehler
							lineError += avg - 255;
							color = 255;
						} else {
							lineError += avg;
							color = 0;
						}
						
						pixels[pos] = (0xFF<<24) | (color << 16) | (color << 8) | color;
					}
					
					// der fehler wird nach durchgang einer zeile resettet
					lineError = 0;
				}
			} // end "Binärbild mit Fehlerkorrektur"
			
			//Sepia
			else if (method.equals("Sepia")) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						// Grauwert
						int avg = (r + g + b) / 3;
						//Erh�hen der Rot und Grun Wetre = mehr Gelb
						int rn = (int) (avg * 1.4);
						int gn = (int) (avg * 1.4);
						int bn = (int) (avg);
						
						//werte max 255
						rn = rn >255?255:rn;
						gn = gn >255?255:gn;
						bn = bn >255?255:bn;

					//zr�ckscreiben
						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}
				
			}//end Sephia
			else if (method.equals("Auf 6 Farben reduzieren")) {
				for (int y = 0; y < height; y++) {

					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos]; 
						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;

						// Variablen Initalisieren
						int rn = 0;
						int gn = 0;
						int bn = 0;

		
						
						//Farben wurden manuell ausgew�hlt mit Photoshop
						//es wird eine art Abstand zu einer Farbe �berpr�ft 
						//und dann je nach dem die angegbene Farbe eingetragen
						
						
						//sehr dunkel
						if (r <= 38 || r > 38 && r <= 59 && g < 70) {
							rn = 2;
							gn = 8;
							bn = 10;
						}
						// dunkles Grau
						else if (r > 59 && r <= 100) {
							rn = 30;
							gn = 30;
							bn = 30;
						}
						// blau
						else if (((r > 38 && r <= 59) && g > 70)
								|| ((r > 59 && r <= 90) && b > 90)) {
							rn = 53;
							gn = 115;
							bn = 208;
						}
						
						// braun
						else if (r > 100 && r <= 120) {
							rn = 134;
							gn = 83;
							bn = 5;
						}
						// grau
						else if (r > 120 && r <= 150) {
							rn = 160;
							gn = 160;
							bn = 160;
						}
						// hellgrau
						else if (r > 150 && r <= 256) {
							rn = 200;
							gn = 200;
							bn = 200;
						}

						// Hier muessen die neuen RGB-Werte wieder auf den
						// Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF << 24) | (rn << 16) | (gn << 8)
								| bn;
					}
				}//end 6Farben
				
			}
		}


	} // CustomWindow inner class
} 
