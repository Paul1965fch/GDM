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
public class GRDM_U5 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Weichzeichner","Hochpass","Kanten"};


	public static void main(String args[]) {

		IJ.open("/Applications/ImageJ/Bilder/sail.jpg");
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

		GRDM_U5 pw = new GRDM_U5();
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
	
	private int[] getNeighbours (int x, int y, int[] orgPixels) {
		int[] neighbours = new int[9];
		
		// reihenfolge:
		// (x - 1) + width * (y - 1); x + width * (y - 1); (x + 1) + width * (y - 1)
		// (x - 1) + width * y; x + width * y; (x + 1) + width * y
		// (x - 1) + width * (y + 1); x + width * (y + 1); (x + 1) + width * (y + 1)
		
		int i = 0;
		for (int yOff = -1; yOff <= 1; yOff++) {
			for (int xOff = -1; xOff <= 1; xOff++) {
				if (x == (width - 1) || x == 0 || y == (height - 1) || y == 0) {
					// randbehandlung
					// TODO: nicht immer gleichen pixel in nachbarpixel schreiben
					neighbours[i] = orgPixels[x + width * y];
				} else {
					neighbours[i] = orgPixels[x + xOff + width * (y + yOff)];
				}
				
				i++;
			}
		}
		
		return neighbours;
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

			// Array zum ZurÃ¼ckschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			int[] filterMatrix = null;
			int divisor = 1;
			int offset = 0;
			
			if (method.equals("Original")) {
				// vom kern wird nur der pixel selber in der berechnung beachtet (= identitŠt)
				filterMatrix = new int[] {
						0, 0, 0,
						0, 1, 0,
						0, 0, 0
				};
				divisor = 1;
			}
			else if (method.equals("Weichzeichner")) {
				// alle Nachbarn gehen gleichmŠ§ig in die Rechnung ein
				filterMatrix = new int[] {
						1, 1, 1,
						1, 1, 1,
						1, 1, 1
				};
				divisor = 9;
			}
			else if (method.equals("Hochpass")) {
				// Umkehrung von Weichzeichner, also IdentitŠts-Matrix - Weichzeichnermatrix
				filterMatrix = new int[] {
						-1, -1, -1,
						-1,  8, -1,
						-1, -1, -1
				};
				divisor = 9;
				offset = 128;
			}
			else if (method.equals("Kanten")) {
				// identitŠt + hochpass
				filterMatrix = new int[] {
						-1, -1, -1,
						-1, 17, -1,
						-1, -1, -1
				};
				divisor = 9;
			}
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int[] neighbours = getNeighbours(x, y, origPixels);
					int pos = y * width + x;
					
					int newR = 0, 
						newG = 0, 
						newB = 0;
					
					for (int i = 0; i < neighbours.length; i++) {
						int r = (neighbours[i] >> 16) & 0xff;
						int g = (neighbours[i] >>  8) & 0xff;
						int b =  neighbours[i]        & 0xff;
						
						newR += r * filterMatrix[i];
						newG += g * filterMatrix[i];
						newB += b * filterMatrix[i];
					}
					
					newR /= divisor;
					newG /= divisor;
					newB /= divisor;
					
					newR += offset;
					newG += offset;
					newB += offset;
					if (newR < -0 || newG < -0 || newB < -0) {
						System.out.println("newR: " + newR + ", newG: " + newG + ", newB: " + newB);
					}
					// begrenzung wegen overflows bei "unscharf maskieren"
					if (newR > 255) newR = 255;
					if (newG > 255) newG = 255;
					if (newB > 255) newB = 255;
					if (newR < 0) newR = 0;
					if (newG < 0) newG = 0;
					if (newB < 0) newB = 0;
					
				
					
					pixels[pos] = (0xff << 24) | (newR << 16) | (newG << 8) | newB;
					
				}
			}
			
			
			
		}


	} // CustomWindow inner class
} 
