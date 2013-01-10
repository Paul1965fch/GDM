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
		
		String path = "";
		IJ.open(path + "component.jpg");
		
		Scale_ scale = new Scale_();
		scale.imp = IJ.getImage();
		ImageProcessor ip = scale.imp.getProcessor();
		scale.run(ip);
	}

	public void run(ImageProcessor ip) {
		
		int width  = ip.getWidth();  // Breite bestimmen
		int height = ip.getHeight(); // Hoehe bestimmen

		String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear"};

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
		gd.addNumericField("Breite:", width, 0);
		gd.addNumericField("Hoehe:", height, 0);
		gd.showDialog();
		
		int choice = gd.getNextChoiceIndex();
		int width_n =  (int)gd.getNextNumber(); // _n fuer das neue skalierte Bild
		int height_n = (int)gd.getNextNumber();

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
						scaled[0] % 1,
						scaled[1] % 1
					};
					
					int pos = y_n * width_n + x_n;
					int scaledPos = (int) (Math.floor(scaled[1]) * width + Math.floor(scaled[0]));
					int argb[]; // the 4 pixels around our calculated pixel
					if(scaledPos < width*height -width-1){
						int[] temp = {
								pix[scaledPos],
								pix[scaledPos+1],
								pix[scaledPos+width],
								pix[scaledPos+width+1],
							};
						argb = temp;
					}else{
						int[] temp = {
								pix[scaledPos],
								pix[scaledPos-1],
								pix[scaledPos-width],
								pix[scaledPos-width-1],
							};
						argb = temp;
					}
				
					int[][] neighbors = new int[4][3];
					
					// split argb[] up into r - g - b
					for (int i = 0; i < neighbors.length; i++) {
						for (int j = 0; j < neighbors[i].length; j++) {
							neighbors[i][j] = ( argb[i] >> (2 - j) ) & 0xff;
						}
					}
					
					// now calculate the avg…
					
					//P =          A              * (1-h)         * (1-v)          + B              * h         *(1-v)           + C              *(1-h)          *v           + D              *h          *v
					int r = (int) (neighbors[0][0]*(1-distance[0])*(1-distance[1]) + neighbors[1][0]*distance[0]*(1-distance[1]) + neighbors[2][0]*(1-distance[0])*distance[1] + neighbors[3][0]*distance[0]*distance[1]);
					int g = (int) (neighbors[0][1]*(1-distance[0])*(1-distance[1]) + neighbors[1][1]*distance[0]*(1-distance[1]) + neighbors[2][1]*(1-distance[0])*distance[1] + neighbors[3][1]*distance[0]*distance[1]);
					int b = (int) (neighbors[0][2]*(1-distance[0])*(1-distance[1]) + neighbors[1][2]*distance[0]*(1-distance[1]) + neighbors[2][2]*(1-distance[0])*distance[1] + neighbors[3][2]*distance[0]*distance[1]);
					pix_n[pos] = (0xff << 24) | (r << 16) | (g << 8) | b;
					
//					if (y_n % 100 == 0 && x_n % 100 == 0) {
//						System.out.println("scaled[x]: " + scaled[0] + ", scaled[y]: " + scaled[1]);
//						System.out.println("scaled[x] % 1: " + (scaled[0] % 1));
//					}
					
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

