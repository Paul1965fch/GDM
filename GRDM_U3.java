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
		"Bin√§rbild",
		"Bin√§rbild mit Fehlerdiffusion",
		"Sepia",
		"Auf 6 Farben reduzieren"
	};


	public static void main(String args[]) {

		//IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");
		IJ.open("/Applications/ImageJ/Bilder/Bear.jpg");

		
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

			// Array zum Zur√ºckschreiben der Pixelwerte
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
						
						// der grauwert ist der durschnitt aller kan√§le
						int grey = (r + g + b) / 3;
						
						pixels[pos] = (0xFF<<24) | (grey << 16) | (grey << 8) | grey;
					}
				}
			} // end "Graustufen"
			else if (method.equals("Bin√§rbild")) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];
						
						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						// durchschnitt (grauwert) berechnen
						int avg = (r + g + b) / 3;
						
						// je nachdem ob n√§her an schwarz oder wei√ü
						// wird der pixel in einer der beiden farben gef√§rbt
						if (avg >= 128) {
							avg = 255;
						} else avg = 0;
						
						pixels[pos] = (0xFF<<24) | (avg << 16) | (avg << 8) | avg;
					}
				}
			} // end bin√§rbild
			else if (method.equals("Bin√§rbild mit Fehlerdiffusion")) {
				int lineError = 0;
				
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int pos = y * width + x;
						int argb = origPixels[pos];
						
						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = argb & 0xff;
						
						// der grauwert ist der mittelwert aus allen kan√§len
						int avg = (r + g + b) / 3;
						int color;
						
						// wenn grau +/- fehler n√§her an wei√ü ist als
						// an schwarz, soll der pixel wei√ü werden
						
						if (avg + lineError >= 128) {
							// der wert wei√ü ist zu hoch
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
			} // end "Bin√§rbild mit Fehlerkorrektur"
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
						//Erhšhen der Rot und Grun Wetre = mehr Gelb
						int rn = (int) (avg * 1.4);
						int gn = (int) (avg * 1.4);
						int bn = (int) (avg);
						
						//werte max 255
						rn = rn >255?255:rn;
						gn = gn >255?255:gn;
						bn = bn >255?255:bn;

					//zrŸckscreiben
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

		
						
						//Farben wurden manuell ausgewŠhlt mit Photoshop
						//es wird eine art Abstand zu einer Farbe ŸberprŸft 
						//und dann je nach dem die angegbene Farbe eingetragen
						
						
						//sehr dunkel
						if ( r <= 59 && g < 70) {
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
