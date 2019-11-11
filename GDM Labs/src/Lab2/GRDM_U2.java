package Lab2;

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
    	IJ.open("C:\\Users\\justi\\workspaceJava\\Eclipse\\Uni\\GDM-Labs\\GDM Labs\\orchid.jpg");
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
         
        private JSlider jSliderBrightness;
		private JSlider jSliderContrast;
		private JSlider jSliderSaturation;
		private JSlider jSliderHue;
		private double brightness = 0;
		private double contrast = 1;
		private double saturation = 1;
		private double hue = 0;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Brightness", 0, 256, 128);
            jSliderContrast = makeTitledSilder("Contrast", 0, 10, 5);
            jSliderSaturation = makeTitledSilder("Saturation", 0, 10, 4);
            jSliderHue = makeTitledSilder("Hue", 0, 360, 0);
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
				String str = "Brightness " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderContrast) {
				if(slider.getValue()>5) {
					contrast = (slider.getValue()-(double)5)*(double)2;
				}else {
					contrast = slider.getValue()/(double)5;	
				}
				
				String str = "Contrast " + contrast; 
				setSliderTitle(jSliderContrast, str); 
			}
			
			if (slider == jSliderSaturation) {
				if(slider.getValue()>4) {
					saturation = (slider.getValue()-(double)3);
				}else {
					saturation = slider.getValue()/(double)4;	
				}
				String str = "Saturation " + saturation; 
				setSliderTitle(jSliderSaturation, str); 
			}
			
			if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Hue " + hue; 
				setSliderTitle(jSliderHue, str); 
			}
			
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}
		
		private int cutOverflow(double value) {
			if(value> 255) {
				value = 255;
			}else if(value <0) {
				value = 0;
			}
			return (int)value;
		}
		
		private double[] convertToYUV(int r,int g, int b) {
			double y = 0.299 * r + 0.587 * g + 0.114 * b;
			double u = (b-y)*0.493;
			double v = (r-y)*0.877;
			
			return new double[]{y,u,v};
		}
		
		private int[] convertToRGB(double y,double u, double v) {
			int r = (int) (y + v/0.877);
			int b = (int) (y + u/0.493);
			int g = (int) (1/0.587 * y - 0.299/0.587*r - 0.114/0.587 * b);
			return new int[] {r,g,b};
		}

		
		private void changePixelValues(ImageProcessor ip) {
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			//für hue
			double rad = Math.toRadians(hue);
			double cos = Math.cos(rad);
			double sin = Math.sin(rad);
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					
					//rgb in YUV convertieren
					double[] yuv = convertToYUV(r, g, b);
					double Y = yuv[0];
					double U = yuv[1];
					double V = yuv[2];
					
					//brightness only
					//Y = Y+brightness;
					
					//hue			
					double Un = (cos*U)+(-sin*V);
					double Vn = (sin*U)+(cos*V);
					
					//contrast & saturation & brightness
					//f(x) = k*(x-128)+128+h
					double Yn = contrast*(Y -128) + 128 + brightness;
					Un = Un * (saturation);
					Vn = Vn * (saturation);
					
					
					int[] rgb = convertToRGB(Yn, Un, Vn);		
					pixels[pos] = (0xFF<<24) | (cutOverflow(rgb[0])<<16) | (cutOverflow(rgb[1])<<8) | cutOverflow(rgb[2]);

				}
			}
		}
		
    } // CustomWindow inner class
} 
