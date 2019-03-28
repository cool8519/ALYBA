package dal.util.swt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class MessageUtil {

	public static void showInfoMessage(Shell shell, String msg) {
		MessageBox msgBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
		msgBox.setText("Information");
		msgBox.setMessage(msg);
		msgBox.open();
	}

	public static void showWarningMessage(Shell shell, String msg) {
		MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
		msgBox.setText("Warning");
		msgBox.setMessage(msg);
		msgBox.open();
	}

	public static void showErrorMessage(Shell shell, String msg) {
		MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		msgBox.setText("Error");
		msgBox.setMessage(msg);
		msgBox.open();
	}

	public static boolean showYesNoMessage(Shell shell, String msg) {
		MessageBox msgBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		msgBox.setText("Question");
		msgBox.setMessage(msg);
		int result = msgBox.open();
		return (result == SWT.YES);
	}

	public static boolean showConfirmMessage(Shell shell, String msg) {
		MessageBox msgBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		msgBox.setText("Confirm");
		msgBox.setMessage(msg);
		int result = msgBox.open();
		return (result == SWT.OK);
	}

	public static int showSelectMessage(Shell shell, String title, String msg, String[] labels) {
		MessageDialog msgDialog = new MessageDialog(shell, title, null, msg, MessageDialog.QUESTION, labels, 0);
		return msgDialog.open();
	}

}
