package dal.tool.analyzer.alyba.ui.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
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
	private Button btn_pauseResume;
	private boolean paused;

	public DebugConsole(Display display, int style) {
		super(display, style);
		createContents();
		open();
		layout();
	}

	public void addDebugMessage(String s) {
		if(!isDisposed() && !paused) {
			txt_debugConsole.append(s + "\n");
		}
	}

	protected void createContents() {

		GridLayout gl_main = new GridLayout();
		gl_main.verticalSpacing = 2;

		setSize(640, 250);
		setText("Debug Console");
		setLayout(new FormLayout());

		FormData fd_btn_clearConsole = new FormData();
		fd_btn_clearConsole.left = new FormAttachment(0, 5);
		fd_btn_clearConsole.top = new FormAttachment(0, 5);
		fd_btn_clearConsole.width = 150;
		btn_clearConsole = new Button(this, SWT.NONE);
		btn_clearConsole.setLayoutData(fd_btn_clearConsole);
		btn_clearConsole.setFont(Utility.getFont());
		btn_clearConsole.setText("Clear Text");

		FormData fd_btn_pauseResume = new FormData();
		fd_btn_pauseResume.right = new FormAttachment(100, -5);
		fd_btn_pauseResume.top = new FormAttachment(0, 5);
		fd_btn_pauseResume.width = 150;
		btn_pauseResume = new Button(this, SWT.NONE);
		btn_pauseResume.setLayoutData(fd_btn_pauseResume);
		btn_pauseResume.setFont(Utility.getFont());
		btn_pauseResume.setText("Pause");

		FormData fd_txt_debugConsole = new FormData();
		fd_txt_debugConsole.left = new FormAttachment(0, 5);
		fd_txt_debugConsole.right = new FormAttachment(100, -5);
		fd_txt_debugConsole.top = new FormAttachment(btn_clearConsole, 5);
		fd_txt_debugConsole.bottom = new FormAttachment(100, -5);
		txt_debugConsole = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txt_debugConsole.setLayoutData(fd_txt_debugConsole);
		txt_debugConsole.setFont(Utility.getFont());

		paused = false;
		
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

		btn_pauseResume.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(paused) {
					txt_debugConsole.append("!!! Debug Console is resumed. Debug logs will be output from now on.\n");
					btn_pauseResume.setText("Pause"); 
					paused = false;
				} else {
					paused = true;
					btn_pauseResume.setText("Resume"); 
					txt_debugConsole.append("!!! Debug Console is paused. Debug logs will not be output from now on.\n");
				}
			}
		});

	}

	protected void checkSubclass() {
	}

}
