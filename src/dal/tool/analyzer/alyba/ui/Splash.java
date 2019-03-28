package dal.tool.analyzer.alyba.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import dal.tool.analyzer.alyba.Constant;
import dal.util.swt.ImageUtil;

public class Splash {

	private boolean endFlag = false;

	public Splash(Display display) {
		
		final Image image = ImageUtil.getImage(Constant.IMAGE_PATH_LOGO);

		final Shell splash = new Shell(SWT.ON_TOP);

		Label label = new Label(splash, SWT.NONE);
		label.setImage(image);

		FormLayout layout = new FormLayout();
		splash.setLayout(layout);
		splash.pack();

		Rectangle dispRect = display.getMonitors()[0].getBounds();
		Rectangle shellRect = splash.getBounds();
		splash.setLocation((dispRect.width - shellRect.width) / 2, (dispRect.height - shellRect.height) / 2);

		splash.open();

		display.timerExec(Constant.SPLASH_TIME, new Runnable() {
			public void run() {
				AlybaGUI.getInstance().init();
				splash.close();
				image.dispose();
				endFlag = true;
			}
		});
		
		while(!endFlag) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}
}
