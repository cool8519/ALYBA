package dal.util.swt;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

public class Utility {

	public static void centralize(Shell shell, Rectangle bounds) {
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}

	public static void centralize(Shell shell) {
		centralize(shell, shell.getDisplay().getPrimaryMonitor().getBounds());
	}

	public static void centralize(Shell shell, Shell bounds_shell) {
		centralize(shell, bounds_shell.getBounds());
	}

}
