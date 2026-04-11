package dal.util.swt;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
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

	public static void centralizeOnMonitorOf(Shell shell, Shell referenceShell) {
		if(shell == null || shell.isDisposed()) {
			return;
		}
		Rectangle b = shell.getBounds();
		Point loc = computeCenteredLocationOnMonitorOf(referenceShell, b.width, b.height);
		shell.setLocation(loc);
	}

	public static Point computeCenteredLocationOnMonitorOf(Shell referenceShell, int width, int height) {
		Rectangle ca = clientAreaForShell(referenceShell);
		int x = ca.x + (ca.width - width) / 2;
		int y = ca.y + (ca.height - height) / 2;
		if(x < ca.x) {
			x = ca.x;
		}
		if(y < ca.y) {
			y = ca.y;
		}
		if(x + width > ca.x + ca.width) {
			x = ca.x + ca.width - width;
		}
		if(y + height > ca.y + ca.height) {
			y = ca.y + ca.height - height;
		}
		if(x < ca.x) {
			x = ca.x;
		}
		if(y < ca.y) {
			y = ca.y;
		}
		return new Point(x, y);
	}

	private static Rectangle clientAreaForShell(Shell referenceShell) {
		if(referenceShell == null || referenceShell.isDisposed()) {
			Display d = Display.getCurrent();
			if(d != null) {
				return d.getPrimaryMonitor().getClientArea();
			}
			return new Rectangle(0, 0, 800, 600);
		}
		Monitor mon = referenceShell.getMonitor();
		if(mon == null) {
			mon = referenceShell.getDisplay().getPrimaryMonitor();
		}
		return mon.getClientArea();
	}

}
