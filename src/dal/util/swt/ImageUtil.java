package dal.util.swt;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class ImageUtil {

	private static ImageRegistry imageRegistry = new ImageRegistry();

	public static void registryImage(String id, String urlName) {
		imageRegistry.put(id, ImageDescriptor.createFromURL(getSystemResource(urlName)));
	}

	public static Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose();
		return scaled;
	}

	public static ImageDescriptor getImageDescriptorFromURL(String urlName) {
		return ImageDescriptor.createFromURL(getSystemResource(urlName));
	}

	public static Image getImage(String urlName) {
		return ImageDescriptor.createFromURL(getSystemResource(urlName)).createImage();
	}

	public static ImageData getImageData(String urlName) {
		return ImageDescriptor.createFromURL(getSystemResource(urlName)).getImageData(100);
	}

	public static Image getImageFromRegistry(String id) {
		return imageRegistry.get(id);
	}

	public static void removeImageFromRegistry(String id) {
		imageRegistry.remove(id);
	}

	private static URL getSystemResource(String url_name) {
		try {
			return ClassLoader.getSystemResource(url_name);
		} catch(Exception e) {
			return null;
		}
	}

}