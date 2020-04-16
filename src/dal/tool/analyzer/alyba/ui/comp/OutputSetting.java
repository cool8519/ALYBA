package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.swt.FileDialogUtil;

public class OutputSetting extends Composite {

	public Button chk_excel;
	public Button chk_html;
	public Button chk_text;
	public Button btn_openDir;
	public Button btn_count;
	public Button btn_name;
	public Text txt_directory;

	public OutputSetting(Composite parent, int style) {
		super(parent, style);
		createContents();
		addEventListener();
	}

	public boolean checkExcelType() {
		return chk_excel.getSelection();
	}

	public boolean checkHtmlType() {
		return chk_html.getSelection();
	}

	public boolean checkTextType() {
		return chk_text.getSelection();
	}

	public String getOutputDirectory() {
		String dirname = txt_directory.getText();
		if(dirname.trim().length() > 0) {
			return dirname;
		} else {
			return ".";
		}
	}

	public boolean sortByCount() {
		return btn_count.getSelection();
	}

	public boolean sortByName() {
		return btn_name.getSelection();
	}

	protected void createContents() {

		Label lb_directory = new Label(this, SWT.NONE);
		lb_directory.setAlignment(SWT.RIGHT);
		lb_directory.setFont(Utility.getFont());
		lb_directory.setText("Directory");
		lb_directory.setBounds(24, 42, 82, 15);

		txt_directory = new Text(this, SWT.BORDER);
		txt_directory.setFont(Utility.getFont());
		txt_directory.setText(Constant.OUTPUT_DEFAULT_DIRECTORY);
		txt_directory.setBounds(125, 40, 390, 19);

		btn_openDir = new Button(this, SWT.NONE);
		btn_openDir.setFont(Utility.getFont());
		btn_openDir.setText("Open");
		btn_openDir.setBounds(521, 38, 100, 23);

		Label hline_output = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_output.setBounds(24, 80, 650, 10);

		Label lb_fileType = new Label(this, SWT.NONE);
		lb_fileType.setAlignment(SWT.RIGHT);
		lb_fileType.setFont(Utility.getFont());
		lb_fileType.setText("File Type");
		lb_fileType.setBounds(24, 116, 82, 15);

		chk_excel = new Button(this, SWT.CHECK);
		chk_excel.setFont(Utility.getFont());
		chk_excel.setText("EXCEL");
		chk_excel.setBounds(125, 115, 65, 16);

		chk_html = new Button(this, SWT.CHECK);
		chk_html.setFont(Utility.getFont());
		chk_html.setText("HTML");
		chk_html.setBounds(215, 115, 65, 16);

		chk_text = new Button(this, SWT.CHECK);
		chk_text.setFont(Utility.getFont());
		chk_text.setText("TEXT");
		chk_text.setBounds(305, 115, 65, 16);

		Label lb_sortBy = new Label(this, SWT.NONE);
		lb_sortBy.setAlignment(SWT.RIGHT);
		lb_sortBy.setFont(Utility.getFont());
		lb_sortBy.setText("Sort by");
		lb_sortBy.setBounds(24, 151, 82, 15);

		btn_count = new Button(this, SWT.RADIO);
		btn_count.setSelection(true);
		btn_count.setEnabled(false);
		btn_count.setFont(Utility.getFont());
		btn_count.setText("Count");
		btn_count.setBounds(125, 150, 65, 16);

		btn_name = new Button(this, SWT.RADIO);
		btn_name.setEnabled(false);
		btn_name.setFont(Utility.getFont());
		btn_name.setText("Name");
		btn_name.setBounds(215, 150, 65, 16);

		setTabList(new Control[] { txt_directory, btn_openDir, chk_excel, chk_html, chk_text, btn_count, btn_name });

	}

	protected void addEventListener() {

		chk_excel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkCheckBox();
			}
		});

		chk_html.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkCheckBox();
			}
		});

		chk_text.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				checkCheckBox();
			}
		});

		btn_openDir.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				File f = FileDialogUtil.openReadDialogDirectory(getShell(), null);
				if(f != null) {
					try {
						txt_directory.setText(f.getCanonicalPath());
					} catch(Exception ex) {
						AlybaGUI.getInstance().debug(ex);
					}
				}
			}
		});

	}

	public void checkCheckBox() {
		boolean checked = chk_excel.getSelection() || chk_html.getSelection() || chk_text.getSelection();
		btn_count.setEnabled(checked);
		btn_name.setEnabled(checked);
	}

}
