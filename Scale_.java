import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Scale_ implements PlugInFilter {

	protected ImagePlus imp;
	private int[] origPixels;
	private int width;
	private int height;
	
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about"))
		{showAbout(); return DONE;}
		return DOES_RGB+NO_CHANGES;
		// kann RGB-Bilder und veraendert das Original nicht
	}
	
	public static void main(String args[]) {
		
		String path = "/home/arnold/IMI/WiSe 12-13/GDM/ImageJ/Bilder/";
		IJ.open(path + "component.jpg");
		
		Scale_ scale = new Scale_();
		scale.imp = IJ.getImage();
		ImageProcessor ip = scale.imp.getProcessor();
		scale.run(ip);
	}

	public void run(ImageProcessor ip) {

		String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear"};

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
		gd.addNumericField("Breite:",400,0);
		gd.addNumericField("Hoehe:",500,0);
		gd.showDialog();
		
		
		int choice = gd.getNextChoiceIndex();
		int width_n =  (int)gd.getNextNumber(); // _n fuer das neue skalierte Bild
		int height_n = (int)gd.getNextNumber();
		
		int width  = ip.getWidth();  // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen

		//height_n = height;
		//width_n  = width;
		
		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild (Original: " + width + "×" + height + ")",
		                   width_n, height_n, 1, NewImage.FILL_BLACK);
		
		ImageProcessor ip_n = neu.getProcessor();

		
		int[] pix = (int[])ip.getPixels();
		int[] pix_n = (int[])ip_n.getPixels();
		
		double[] ratio = {
			(double) width / width_n,
			(double) height / height_n	
		};
		
		System.out.println("width: " + width
				+ ", height: " + height);
		System.out.println("width_n: " + width_n
				+ ", height_n: " + height_n);
		System.out.println("ratio: " + ratio[0] + "/" + ratio[1]);
		System.out.println("pix.length: " + pix.length + ", pix_n.length: " + pix_n.length );

		// Schleife ueber das neue Bild
		switch(choice)  {
		case 0: // copy
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					
					int y = y_n;
					int x = x_n;
					
					if (y < height && x < width) {
						int pos_n = y_n * width_n + x_n;
						int pos  =  y  *width   + x;
					
						pix_n[pos_n] = pix[pos];
					}
				}
			}
			
			break;
			
		case 1: // nearest neighbour
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					
					// to decide which pixels we take,
					// we calculate where we would be
					// on the scaled image at the
					// corrseponding ratio and round it.
					
					int[] scaled = {
						(int) Math.floor( x_n * ratio[0] ),	// x
						(int) Math.floor( y_n * ratio[1] )	// y
					};
					
					int pos = y_n * width_n + x_n;
					int scaledPos = scaled[1] * width + scaled[0];
					
					pix_n[pos] = pix[scaledPos];
				}
			}
			
			break;
			
		case 2: // bilinear
			for (int y_n = 0; y_n < height_n; y_n++) {
				for (int x_n = 0; x_n < width_n; x_n++) {
					
					// now we have to see how much of each
					// neighboring pixel we have to take
					
					double[] scaled = {
						x_n * ratio[0],
						y_n * ratio[1]
					};
					
					double[] distance = {
						scaled[0] % 1, scaled[1] % 1
					};
					
					if (y_n % 100 == 0 && x_n % 100 == 0) {
						System.out.println("scaled[x]: " + scaled[0] + ", scaled[y]: " + scaled[1]);
						System.out.println("scaled[x] % 1: " + (scaled[0] % 1));
					}
					
				}
			}
			
			break;
		}


		// neues Bild anzeigen
		neu.show();
		neu.updateAndDraw();
	}

	void showAbout() {
		IJ.showMessage("");
	}
}

