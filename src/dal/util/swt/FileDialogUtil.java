package dal.util.swt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class FileDialogUtil {

	public static List<File> openReadDialogFiles(Shell shell, String[] filter_names, String[] fileter_exts, String initPath) {
		FileDialog dlg = new FileDialog(shell, SWT.MULTI);
		dlg.setFilterPath(initPath);
		dlg.setFilterNames(filter_names);
		dlg.setFilterExtensions(fileter_exts);
		String fn = dlg.open();
		List<File> files = new ArrayList<File>();
		if(fn != null) {
			String[] names = dlg.getFileNames();
			String temp;
			File f;
			for(int i = 0; i < names.length; i++) {
				temp = dlg.getFilterPath();
				f = new File(temp + ((temp.charAt(temp.length() - 1) != File.separatorChar) ? File.separatorChar : "") + names[i]);
				files.add(f);
			}
		}
		return files;
	}

	public static File openReadDialogFile(Shell shell, String[] filter_names, String[] fileter_exts, String initPath) {
		FileDialog dlg = new FileDialog(shell, SWT.SINGLE);
		if(initPath != null) {
			dlg.setFilterPath(initPath);
		}
		if(filter_names != null) {
			dlg.setFilterNames(filter_names);
		}
		if(fileter_exts != null) {
			dlg.setFilterExtensions(fileter_exts);
		}
		String result = dlg.open();
		if(result != null) {
			File f = new File(result);
			if(f.getName().indexOf('.') < 0 && fileter_exts.length == 1) {
				String suffix = fileter_exts[0].substring(fileter_exts[0].indexOf('.'));
				return new File(f.getParent(), f.getName()+suffix);
			} else {
				return f;
			}
		}
		return null;
	}

	public static File openReadDialogDirectory(Shell shell, String initPath) {
		DirectoryDialog dlg = new DirectoryDialog(shell);
		if(initPath != null) {
			dlg.setFilterPath(initPath);
		}
		String result = dlg.open();
		if(result != null) {
			return new File(result);
		}
		return null;
	}

	public static File openSaveDialogFile(Shell shell, String[] filter_names, String[] fileter_exts, String initPath) {
		FileDialog dlg = new FileDialog(shell, SWT.SAVE);
		if(initPath != null) {
			dlg.setFilterPath(initPath);
		}
		if(filter_names != null) {
			dlg.setFilterNames(filter_names);
		}
		if(fileter_exts != null) {
			dlg.setFilterExtensions(fileter_exts);
		}
		String result = dlg.open();
		if(result != null) {
			File f = new File(result);
			if(f.exists()) {
				if(confirmOverwrite(shell)) {
					return f;
				} else {
					return null;
				}
			} else {
				return f;
			}
		}
		return null;
	}

	private static boolean confirmOverwrite(Shell shell) {
		String msg = "The filename alreay exists.\n\nDo you want to overwrite?";
		return MessageUtil.showConfirmMessage(shell, msg);
	}

}
