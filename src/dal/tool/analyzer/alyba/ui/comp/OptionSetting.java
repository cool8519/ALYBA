package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.MessageUtil;

public class OptionSetting extends Composite {

	private Button btn_loadSetting;
	private Button btn_saveSetting;
	private Button btn_defaultSetting;
	public Button chk_multiThread;
	public Button chk_fixedFields;
	public Button chk_strictCheck;
	public Button chk_allowErrors;
	public Button chk_includeParams;
	public Button chk_checkFileEncoding;
	public Button chk_collectTPM;
	public Button chk_collectElapsed;
	public Button chk_collectBytes;
	public Button chk_collectErrors;
	public Button chk_collectIP;
	public Button chk_collectTPS;
	public Spinner spn_allowErrors;
	public Spinner spn_tpmUnit;
	public Spinner spn_collectElapsed;
	public Spinner spn_collectBytes;

	public OptionSetting(Composite parent, int style) {
		super(parent, style);
		createContents();
		addEventListener();
	}

	public boolean checkMultiThread() {
		return chk_multiThread.getSelection();
	}

	public boolean checkFixedFields() {
		return chk_fixedFields.getSelection();
	}

	public boolean checkStrictCheck() {
		return chk_strictCheck.getSelection();
	}
	
	public boolean checkAllowErrors() {
		return chk_allowErrors.getSelection();
	}

	public String getAllowErrorCount() {
		return spn_allowErrors.getText();
	}

	public boolean checkIncludeParams() {
		return chk_includeParams.getSelection();
	}
	
	public boolean checkCheckFileEncoding() {
		return chk_checkFileEncoding.getSelection();
	}

	public boolean checkCollectTPM() {
		return chk_collectTPM.getSelection();
	}

	public String getTPMUnitMinutes() {
		return spn_tpmUnit.getText();
	}

	public String getCollectElapsedTimeMS() {
		return spn_collectElapsed.getText();
	}

	public String getCollectResponseBytesKB() {
		return spn_collectBytes.getText();
	}

	public boolean checkCollectElaspsedTime() {
		return chk_collectElapsed.getSelection();
	}

	public boolean checkCollectResponseBytes() {
		return chk_collectBytes.getSelection();
	}

	public boolean checkCollectErrors() {
		return chk_collectErrors.getSelection();
	}

	public boolean checkCollectIP() {
		return chk_collectIP.getSelection();
	}

	public boolean checkCollectTPS() {
		return chk_collectTPS.getSelection();
	}

	protected void createContents() {

		chk_multiThread = new Button(this, SWT.CHECK);
		chk_multiThread.setFont(Utility.getFont());
		chk_multiThread.setText("Multi-thread Parsing");
		chk_multiThread.setToolTipText("Determines whether each log file is parsed by multiple threads.\nMulti-thread will speed up parsing but will be added merging time.");
		chk_multiThread.setBounds(24, 20, 145, 16);

		chk_fixedFields = new Button(this, SWT.CHECK);
		chk_fixedFields.setFont(Utility.getFont());
		chk_fixedFields.setText("Check if the number of fields are fixed");
		chk_fixedFields.setToolTipText("Determines whether it is considered an error if the total number of fields in the line is different from the number of mapped fields.");
		chk_fixedFields.setBounds(24, 50, 245, 16);		

		chk_strictCheck = new Button(this, SWT.CHECK);
		chk_strictCheck.setFont(Utility.getFont());
		chk_strictCheck.setText("Strict check of delimeter during parsing");
		chk_strictCheck.setToolTipText("Determines whether empty fields are ignored when parsing.\nNote that when this is enabled it will split multiple spaces into multiple blank fields in case of whitespace delimiter.");
		chk_strictCheck.setBounds(24, 80, 240, 16);		

		chk_allowErrors = new Button(this, SWT.CHECK);
		chk_allowErrors.setFont(Utility.getFont());
		chk_allowErrors.setText("Allow");
		chk_allowErrors.setToolTipText("Specify the number of errors to allow per parser thread.\nIf not checked, unlimited errors are allowed.");
		chk_allowErrors.setBounds(24, 110, 53, 16);

		spn_allowErrors = new Spinner(this, SWT.BORDER);
		spn_allowErrors.setTextLimit(4);
		spn_allowErrors.setMaximum(9999);
		spn_allowErrors.setMinimum(0);
		spn_allowErrors.setFont(Utility.getFont());
		spn_allowErrors.setToolTipText("If the specified number of times is exceeded, the parser thread will stop.");
		spn_allowErrors.setBounds(90, 108, 58, 21);

		Label lb_allowErrors = new Label(this, SWT.NONE);
		lb_allowErrors.setFont(Utility.getFont());
		lb_allowErrors.setText("error(s) per file");
		lb_allowErrors.setBounds(154, 110, 110, 15);

		chk_includeParams = new Button(this, SWT.CHECK);
		chk_includeParams.setFont(Utility.getFont());
		chk_includeParams.setText("URI includes parameters");
		chk_includeParams.setToolTipText("Check if the URI contains a query string.\nIt will be aggregated into a URI without the query string.");
		chk_includeParams.setBounds(24, 140, 226, 16);
		
		chk_checkFileEncoding = new Button(this, SWT.CHECK);
		chk_checkFileEncoding.setFont(Utility.getFont());
		chk_checkFileEncoding.setText("Check encoding of files");
		chk_checkFileEncoding.setToolTipText("Check what encoding the text in the files is in.\nThis may delay the start of log analysis, so it is recommended to check it only if the log contains special characters such as Korean.");
		chk_checkFileEncoding.setBounds(24, 170, 226, 16);

		Label lb_verticalSeparator = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
		lb_verticalSeparator.setBounds(280, 18, 2, 220);

		chk_collectTPM = new Button(this, SWT.CHECK);
		chk_collectTPM.setFont(Utility.getFont());
		chk_collectTPM.setText("Aggregate TPM every");
		chk_collectTPM.setToolTipText("Check to collect TPM data.");
		chk_collectTPM.setBounds(307, 20, 150, 15);

		spn_tpmUnit = new Spinner(this, SWT.BORDER);
		spn_tpmUnit.setTextLimit(4);
		spn_tpmUnit.setMaximum(1440);
		spn_tpmUnit.setMinimum(1);
		spn_tpmUnit.setFont(Utility.getFont());
		spn_tpmUnit.setToolTipText("TPM data will be collected at specified minute intervals.");
		spn_tpmUnit.setBounds(460, 18, 60, 21);

		Label lb_tpmUnit2 = new Label(this, SWT.NONE);
		lb_tpmUnit2.setFont(Utility.getFont());
		lb_tpmUnit2.setText("minute(s)");
		lb_tpmUnit2.setBounds(526, 20, 59, 15);

		chk_collectElapsed = new Button(this, SWT.CHECK);
		chk_collectElapsed.setFont(Utility.getFont());
		chk_collectElapsed.setText("Collect requests that elapsed time was over ");
		chk_collectElapsed.setToolTipText("Check to collect requests that exceed the specified response time.");
		chk_collectElapsed.setBounds(307, 50, 265, 16);

		spn_collectElapsed = new Spinner(this, SWT.BORDER);
		spn_collectElapsed.setPageIncrement(60000);
		spn_collectElapsed.setIncrement(1000);
		spn_collectElapsed.setTextLimit(9);
		spn_collectElapsed.setMaximum(999999999);
		spn_collectElapsed.setMinimum(100);
		spn_collectElapsed.setFont(Utility.getFont());
		spn_collectElapsed.setBounds(579, 47, 70, 21);

		Label lb_collectElapsed2 = new Label(this, SWT.NONE);
		lb_collectElapsed2.setFont(Utility.getFont());
		lb_collectElapsed2.setText("ms");
		lb_collectElapsed2.setBounds(655, 50, 30, 15);

		chk_collectBytes = new Button(this, SWT.CHECK);
		chk_collectBytes.setFont(Utility.getFont());
		chk_collectBytes.setText("Collect requests that response bytes was over ");
		chk_collectBytes.setToolTipText("Check to collect requests that exceed the specified response size.");
		chk_collectBytes.setBounds(307, 80, 278, 16);

		spn_collectBytes = new Spinner(this, SWT.BORDER);
		spn_collectBytes.setTextLimit(9);
		spn_collectBytes.setPageIncrement(1024);
		spn_collectBytes.setMaximum(999999999);
		spn_collectBytes.setMinimum(128);
		spn_collectBytes.setIncrement(128);
		spn_collectBytes.setFont(Utility.getFont());
		spn_collectBytes.setBounds(589, 78, 60, 21);

		Label lb_collectBytes2 = new Label(this, SWT.NONE);
		lb_collectBytes2.setFont(Utility.getFont());
		lb_collectBytes2.setText("KB");
		lb_collectBytes2.setBounds(656, 80, 30, 15);
		
		chk_collectErrors = new Button(this, SWT.CHECK);
		chk_collectErrors.setFont(Utility.getFont());
		chk_collectErrors.setText("Collect requests that response code was error");
		chk_collectErrors.setToolTipText("Check to collect error responses.");
		chk_collectErrors.setBounds(307, 110, 276, 16);

		chk_collectIP = new Button(this, SWT.CHECK);
		chk_collectIP.setFont(Utility.getFont());
		chk_collectIP.setText("Collect Client IP");
		chk_collectIP.setToolTipText("Check to collect the client IP address.\nIP addresses are usually very diverse, so too much data can be collected.");
		chk_collectIP.setBounds(307, 140, 245, 16);

		chk_collectTPS = new Button(this, SWT.CHECK);
		chk_collectTPS.setFont(Utility.getFont());
		chk_collectTPS.setText("Aggregate TPS on the most requested day(DB only)");
		chk_collectTPS.setToolTipText("Check to collect transactions per second for days with the highest total number of requests.\nCollecting TPS takes time, so collect only when necessary.");
		chk_collectTPS.setBounds(307, 170, 320, 16);

		btn_loadSetting = new Button(this, SWT.NONE);
		btn_loadSetting.setFont(Utility.getFont());
		btn_loadSetting.setText("Load setting");
		btn_loadSetting.setBounds(24, 275, 120, 23);

		btn_saveSetting = new Button(this, SWT.NONE);
		btn_saveSetting.setFont(Utility.getFont());
		btn_saveSetting.setText("Save setting");
		btn_saveSetting.setBounds(160, 275, 120, 23);

		btn_defaultSetting = new Button(this, SWT.NONE);
		btn_defaultSetting.setFont(Utility.getFont());
		btn_defaultSetting.setText("Default setting");
		btn_defaultSetting.setBounds(550, 275, 120, 23);
		
		setTabList(new Control[] { chk_multiThread,
								   chk_fixedFields,
								   chk_strictCheck,
								   chk_allowErrors, spn_allowErrors,
								   chk_includeParams,
								   chk_checkFileEncoding,
								   chk_collectTPM, spn_tpmUnit,
								   chk_collectElapsed, spn_collectElapsed,
								   chk_collectBytes, spn_collectBytes,
								   chk_collectErrors,
								   chk_collectIP,
								   chk_collectTPS,
								   btn_loadSetting, btn_saveSetting, btn_defaultSetting });

	}

	protected void addEventListener() {

		chk_allowErrors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				spn_allowErrors.setEnabled(chk_allowErrors.getSelection());
			}
		});

		chk_collectTPM.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				spn_tpmUnit.setEnabled(chk_collectTPM.getSelection());
				chk_collectTPS.setEnabled(chk_collectTPM.getSelection());
			}
		});

		chk_collectElapsed.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				spn_collectElapsed.setEnabled(chk_collectElapsed.getSelection());
			}
		});

		chk_collectBytes.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				spn_collectBytes.setEnabled(chk_collectBytes.getSelection());
			}
		});

		chk_collectIP.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(chk_collectIP.getSelection()) {
					String msg = "This can take a long time to collect too much data.\n\nDo you really want to enable this setting?";
					if(!MessageUtil.showConfirmMessage(getShell(), msg)) {
						chk_collectIP.setSelection(false);
					}
				}
			}
		});

		chk_collectTPS.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(chk_collectTPS.getSelection()) {
					String msg = "This can take a long time to collect too much data.\n\nDo you really want to enable this setting?";
					if(!MessageUtil.showConfirmMessage(getShell(), msg)) {
						chk_collectTPS.setSelection(false);
					}
				}
			}
		});

		btn_loadSetting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				File f = FileDialogUtil.openReadDialogFile(getShell(), Constant.SETTING_FILTER_NAMES, Constant.SETTING_FILTER_EXTS, Constant.DIALOG_INIT_PATH);
				if(f != null) {
					AlybaGUI.getInstance().loadSetting(f);
				}
			}
		});

		btn_saveSetting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				File f = FileDialogUtil.openSaveDialogFile(getShell(), Constant.SETTING_FILTER_NAMES, Constant.SETTING_FILTER_EXTS, Constant.DIALOG_INIT_PATH);
				if(f != null) {
					AlybaGUI.getInstance().saveSetting(f);
				}
			}
		});

		btn_defaultSetting.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String msg = "The current setting will be replaced with default setting.\n\nDo you really want to load default setting?";
				if(MessageUtil.showConfirmMessage(getShell(), msg)) {
					AlybaGUI.getInstance().resetSetting();
				}
			}
		});

	}
}
