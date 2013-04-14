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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GRDM_U2 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
		//new ImageJ();
    	IJ.open("/Applications/ImageJ/Bilder/Bear.jpg");
    	//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");
		
		GRDM_U2 pw = new GRDM_U2();
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
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
        //Schieberegler anlegen 
        private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private JSlider jSliderSaturation;
		private JSlider jSliderHue;
		//Stadartwerte definiren damit if Abfrage funktioniert
		private double brightness = 0;
		private double saturation = 10;
		private double contrast = 10;
		private int hue = 0;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", 0, 256, 128); 
            jSliderContrast = makeTitledSilder("Kontrast", 0, 100, 10);
            jSliderSaturation = makeTitledSilder("SŠttigung", 0, 50, 10);
            jSliderHue = makeTitledSilder("Farbton", 0, 360, 0);
            panel.add(jSliderBrightness);
            panel.add(jSliderContrast);
            panel.add(jSliderSaturation);
            panel.add(jSliderHue);
            
            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue()-128;
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderContrast) {
				contrast = slider.getValue();
				String str = "Kontrast " + (contrast / 10); // werte auf 0.0 - 10.0 Umrechnen da Slider mit int arbeitet
				setSliderTitle(jSliderContrast, str); 
			}
			
			if (slider == jSliderSaturation) {
				saturation = slider.getValue();
				String str = "SŠttigung " + (saturation / 10); //siehe Kontrast
				setSliderTitle(jSliderSaturation, str); 
			}
			
			if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Farbton " + hue + "¡"; 
				setSliderTitle(jSliderHue, str); 
			}
			
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		private void changePixelValues(ImageProcessor ip) {
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			double sinHue;
			double cosHue;
			
			sinHue = Math.sin(Math.toRadians(hue));
			cosHue =  Math.cos(Math.toRadians(hue));
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					
					//Umrechnen in YCBCR
					double Y = 0.299 * r + 0.587 * g + 0.114 * b;
					double Cb = -0.168736 * r - 0.331264 * g + 0.5 * b;
					double Cr = 0.5 * r - 0.418688 * g - 0.081312 * b;

					//Helligkeit neu berechnen und 128 Werte auf Y werte anpassen
					if(brightness != 0){
						Y = Y + brightness * 2;
					}
					
					//Kontrast neu berechnen
					//Helligkeituntersciede werden verstŠrkt
					//Wenn Kontrast kleiner als 1.0 werden sie abgeschwŠcht
					if(contrast != 10){
						Y = (Y -128) * (contrast/10) + 128;
					}
					
					//Crominazwerte mit SŠttingungsfaktor multiplizieren
					if(saturation != 10){
						Cb = Cb* saturation/10;
						Cr = Cr* saturation/10;
					}
					
					//Crominazwerte nach Vectordrehformel veŠndern
					//bei 0 und 360 keine VerŠnderung
					if(hue%360 != 0){
						double oldCb = Cb;
						
						Cb = Cb * cosHue - Cr * sinHue;
						Cr = oldCb * sinHue + Cr * cosHue;
					}
					
					// zurŸckrechnen nach RGB
					int rn = (int) (Y + 1.402 * Cr);
					int gn = (int) (Y - 0.3441*Cb - 0.7141*Cr);
					int bn = (int) (Y + 1.772*Cb);
					
					//Wertebereich begrenzen 
					if(rn>255)rn = 255;
					if(gn>255)gn = 255;
					if(bn>255)bn = 255;
					
					if(rn<0)rn = 0;
					if(gn<0)gn = 0;
					if(bn<0)bn = 0;
					
					
					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}
		
    } // CustomWindow inner class
} 
