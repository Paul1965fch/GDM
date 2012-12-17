import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;


public class GRDM_U4 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wischen", "Weiche Blende","Ineinander Kopieren","Schiebblende", "Chroma Key", "Extra"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+STACK_REQUIRED;
	}
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		IJ.open("/Applications/ImageJ/Bilder/StackB.zip");
		
		GRDM_U4 sd = new GRDM_U4();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();
		
		int length = stack_B.getSize();
		int width  = B_ip.getWidth();
		int height = B_ip.getHeight();
		
		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		OpenDialog od_A = new OpenDialog("Auswählen des 2. Filmes ...",  "");
				
		// Film A wird dazugeladen
		String dateiA = od_A.getFileName();
		if (dateiA == null) return; // Abbruch
		String pfadA = od_A.getDirectory();
		ImagePlus A = o.openImage(pfadA,dateiA);
		if (A == null) return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A  = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height)
		{
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}
		
		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length,stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg  = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode",choices,"");
		gd.showDialog();

		int methode = 0;		
		String s = gd.getNextChoice();
		if (s.equals("Wischen")) methode = 1;
		if (s.equals("Weiche Blende")) methode = 2;
		if (s.equals("Ineinander Kopieren")) methode = 3;
		if (s.equals("Schiebblende")) methode = 4;
		if (s.equals("Chroma Key")) methode = 5;
		if (s.equals("Extra")) methode = 6;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z=1; z<=length; z++)
		{
			pixels_B   = (int[]) stack_B.getPixels(z);
			pixels_A   = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos = 0;
			for (int y=0; y<height; y++)
				for (int x=0; x<width; x++, pos++)
				{
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);

					if (methode == 1)
					{
						if (y+1 > (z-1)*(double)height/(length-1))
							pixels_Erg[pos] = pixels_B[pos];
						else
							pixels_Erg[pos] = pixels_A[pos];
					}
					// weiche blende
					else if (methode == 2)
					{
						// rA = rot von A
						// rB = rot von B
						// r = ergebnis, z = momentane zeit, length = dauer insgesamt
						
						int r = (rA * z + rB * (length - z)) / length;
						int g = (gA * z + gB * (length - z)) / length;
						int b = (bA * z + bB * (length - z)) / length;
						
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
					//Ineinanderkopieren
					else if (methode == 3)
					{
						int r, g, b;
						//formel von Foliensatz Bildmanipulation 1
						//wenn dunler wert verwende mittelwert
						if(rA <= 128){
							r = rB * rA/128;
						}
						//wenn heller wert invertire beide werte - verwende mittelwert - und invertiere erneut
						else{
							r = 255- ((255-rA)*(255-rB))/128;
						}
						
						if(gA <= 128){
							g = gB * gA/128;
						}
						else{
							g = 255- ((255-gA)*(255-gB))/128;
						}
						
						if(bA <= 128){
							b = rB * bA/128;
						}
						else{
							b = 255- ((255-bA)*(255-bB))/128;
						}
						
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
					//Schiebblende
					else if (methode == 4)
					{
						int r ,g, b, offset;
						
						// abstand vom linken Rand
						offset = width * z / length;
						
						if (x < offset) {
							//nehme von Stack A von der rechten Seite ausgehend den wert plus x (aus der aktuellen Zeile)
							//und schreibe ihn an die aktuelle Position
							pixels_Erg[pos] = pixels_A[width - (offset - x) + width * y];
						} else {
							//wie Stack A, von der linken Seite ausgehend (ohne width -)
							pixels_Erg[pos] = pixels_B[(x - offset) + width * y];
						}
					}
					
					// chroma key
					else if (methode == 5) 
					{
						//mit photoshop
						//dunkles gelb: r: 194 g:120 b:48
						// helles gelb  r: 236 g:182 b:70
						int r, g, b;
						
						// wenn farben im bereich + toleranz, verwende stackB
						if(rA >= 150 && rA <= 250 && gA >= 100 && gA <= 200 && bA >= 25 && bA <= 100) {
							r = rB;
							g = gB;
							b = bB;
						} else {
							r = rA;
							g = gA;
							b = bA;
						}
						
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
					}
					//weicher kreis
					else if (methode == 6)
					{
						double rad = (double) z / length * width / 2;
						double alias = 2 * z;
						int r,g,b;
						int centerX = width / 2;
						int centerY = height / 2;
						
						
						// pythagoras
						double dist = Math.sqrt(Math.pow((x - centerX), 2) + Math.pow((y - centerY), 2));
						
						if (dist <= rad) {
							r = rA;
							g = gA;
							b = bA;
							
						} 
						else if (dist < rad + alias) {
							double factor = (dist - rad) / alias;

							r = (int) (rA * (1 - factor) + rB * factor);
							g = (int) (gA * (1 - factor) + gB * factor);
							b = (int) (bA * (1 - factor) + bB * factor);
							
							// je weiter außen man sich befindet, desto weißer sollen g und b werden
							// d.h., wir rechnen tatsächlichen abstand (dist) - radius des kreises und
							// bringen es in abhängigkeit vom bereich, in dem wir glätten wollen,
							// damit sich die werte von nahe 0 bis 1 bewegen
						}
						else {
							r = rB;
							g = gB;
							b = bB;
						}
						
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + ( b & 0xff);
						
					}
					
				}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();

	}

}

