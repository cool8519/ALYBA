package dal.tool.analyzer.alyba.ui.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.util.Utility;

public class DebugConsole extends Shell {

	private Text txt_debugConsole;
	private Button btn_clearConsole;

	public DebugConsole(Display display, int style) {
		super(display, style);
		createContents();
		open();
		layout();
	}

	public void addDebugMessage(String s) {
		if(!isDisposed()) {
			txt_debugConsole.append(s + "\n");
		}
	}

	protected void createContents() {

		GridLayout gl_main = new GridLayout();
		gl_main.verticalSpacing = 2;

		setSize(640, 250);
		setText("Debug Console");
		setLayout(new GridLayout());

		btn_clearConsole = new Button(this, SWT.NONE);
		GridData gd_btn_clearConsole = new GridData(150, SWT.DEFAULT);
		gd_btn_clearConsole.grabExcessHorizontalSpace = true;
		gd_btn_clearConsole.verticalAlignment = SWT.CENTER;
		btn_clearConsole.setLayoutData(gd_btn_clearConsole);
		btn_clearConsole.setFont(Utility.getFont());
		btn_clearConsole.setText("Clear Text");

		txt_debugConsole = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txt_debugConsole.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true));
		txt_debugConsole.setFont(Utility.getFont());

		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				AlybaGUI.getInstance().toggleDebugConsole();
				e.doit = false;
			}
		});

		btn_clearConsole.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				txt_debugConsole.setText("");
			}
		});

	}

	protected void checkSubclass() {
	}

}
