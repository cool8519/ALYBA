package dal.tool.analyzer.alyba.ui.comp;

import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.util.Utility;
import dal.util.swt.DateTimePicker;

public class FilterSetting extends Composite {

	private HashMap<String, String> includeFilterData;
	private HashMap<String, String> excludeFilterData;

	public Button chk_allRange;
	public DateTimePicker dtp_fromDate;
	public DateTimePicker dtp_toDate;
	public Group grp_inclueFilter;
	public Button chk_includeFilter;
	public Button chk_inc_ignoreCase;
	public Button btn_inc_chkAnd;
	public Button btn_inc_chkOr;
	public Text txt_inc_uri;
	public Text txt_inc_ext;
	public Text txt_inc_ip;
	public Text txt_inc_method;
	public Text txt_inc_version;
	public Text txt_inc_code;
	public Group grp_exclueFilter;
	public Button chk_excludeFilter;
	public Button chk_exc_ignoreCase;
	public Button btn_exc_chkAnd;
	public Button btn_exc_chkOr;
	public Text txt_exc_uri;
	public Text txt_exc_ext;
	public Text txt_exc_ip;
	public Text txt_exc_method;
	public Text txt_exc_version;
	public Text txt_exc_code;

	private boolean updatedFromDate = false;
	private boolean updatedToDate = false;

	public FilterSetting(Composite parent, int style) {
		super(parent, style);
		createContents();
		addEventListener();
		includeFilterData = new HashMap<String, String>();
		excludeFilterData = new HashMap<String, String>();
	}

	public HashMap<String, String> getIncludeFilterData() {
		includeFilterData.clear();
		if(txt_inc_uri.getText().length() > 0)
			includeFilterData.put("URI", txt_inc_uri.getText());
		if(txt_inc_ext.getText().length() > 0)
			includeFilterData.put("EXT", txt_inc_ext.getText());
		if(txt_inc_ip.getText().length() > 0)
			includeFilterData.put("IP", txt_inc_ip.getText());
		if(txt_inc_method.getText().length() > 0)
			includeFilterData.put("METHOD", txt_inc_method.getText());
		if(txt_inc_version.getText().length() > 0)
			includeFilterData.put("VERSION", txt_inc_version.getText());
		if(txt_inc_code.getText().length() > 0)
			includeFilterData.put("CODE", txt_inc_code.getText());
		return includeFilterData;
	}

	public HashMap<String, String> getExcludeFilterData() {
		excludeFilterData.clear();
		if(txt_exc_uri.getText().length() > 0)
			excludeFilterData.put("URI", txt_exc_uri.getText());
		if(txt_exc_ext.getText().length() > 0)
			excludeFilterData.put("EXT", txt_exc_ext.getText());
		if(txt_exc_ip.getText().length() > 0)
			excludeFilterData.put("IP", txt_exc_ip.getText());
		if(txt_exc_method.getText().length() > 0)
			excludeFilterData.put("METHOD", txt_exc_method.getText());
		if(txt_exc_version.getText().length() > 0)
			excludeFilterData.put("VERSION", txt_exc_version.getText());
		if(txt_exc_code.getText().length() > 0)
			excludeFilterData.put("CODE", txt_exc_code.getText());
		return excludeFilterData;
	}

	public boolean checkAllRangeEnable() {
		return chk_allRange.getSelection();
	}

	public Date getRangeFromDate() {
		return dtp_fromDate.getDate();
	}

	public Date getRangeToDate() {
		return dtp_toDate.getDate();
	}

	public boolean getIncludeFilterEnable() {
		return chk_includeFilter.getSelection();
	}

	public boolean getExcludeFilterEnable() {
		return chk_excludeFilter.getSelection();
	}

	public boolean getIncludeFilterIgnoreCase() {
		return chk_inc_ignoreCase.getSelection();
	}

	public boolean getExcludeFilterIgnoreCase() {
		return chk_exc_ignoreCase.getSelection();
	}

	public boolean checkIncludeFilterAndCheck() {
		return btn_inc_chkAnd.getSelection();
	}

	public boolean checkExcludeFilterAndCheck() {
		return btn_exc_chkAnd.getSelection();
	}

	protected void createContents() {

		Label lb_timeRange = new Label(this, SWT.NONE);
		lb_timeRange.setFont(Utility.getFont());
		lb_timeRange.setText("Time Range");
		lb_timeRange.setAlignment(SWT.RIGHT);
		lb_timeRange.setBounds(10, 19, 100, 15);

		chk_allRange = new Button(this, SWT.CHECK);
		chk_allRange.setFont(Utility.getFont());
		chk_allRange.setText("All");
		chk_allRange.setBounds(133, 18, 45, 16);

		dtp_fromDate = new DateTimePicker(this, SWT.NONE, Utility.getFont());
		dtp_fromDate.setBounds(184, 13, 240, 26);

		Label lb_fromto = new Label(this, SWT.NONE);
		lb_fromto.setFont(Utility.getFont(SWT.BOLD));
		lb_fromto.setText("~");
		lb_fromto.setAlignment(SWT.CENTER);
		lb_fromto.setBounds(421, 19, 15, 15);

		dtp_toDate = new DateTimePicker(this, SWT.NONE, Utility.getFont());
		dtp_toDate.setBounds(446, 13, 240, 26);

		chk_includeFilter = new Button(this, SWT.CHECK);
		chk_includeFilter.setFont(Utility.getFont());
		chk_includeFilter.setText("Include Filter");
		chk_includeFilter.setBounds(133, 59, 94, 16);

		grp_inclueFilter = new Group(this, SWT.NONE);
		grp_inclueFilter.setBounds(10, 74, 340, 245);

		Label lb_inc_chkOperator = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_chkOperator.setFont(Utility.getFont());
		lb_inc_chkOperator.setText("Check Operator");
		lb_inc_chkOperator.setAlignment(SWT.RIGHT);
		lb_inc_chkOperator.setBounds(10, 25, 100, 15);

		btn_inc_chkAnd = new Button(grp_inclueFilter, SWT.RADIO);
		btn_inc_chkAnd.setFont(Utility.getFont());
		btn_inc_chkAnd.setText("AND");
		btn_inc_chkAnd.setBounds(122, 25, 60, 15);

		btn_inc_chkOr = new Button(grp_inclueFilter, SWT.RADIO);
		btn_inc_chkOr.setFont(Utility.getFont());
		btn_inc_chkOr.setText("OR");
		btn_inc_chkOr.setBounds(188, 25, 60, 15);

		Label lb_inc_ignoreCase = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_ignoreCase.setFont(Utility.getFont());
		lb_inc_ignoreCase.setText("Ignore Case");
		lb_inc_ignoreCase.setAlignment(SWT.RIGHT);
		lb_inc_ignoreCase.setBounds(10, 52, 100, 15);

		chk_inc_ignoreCase = new Button(grp_inclueFilter, SWT.CHECK);
		chk_inc_ignoreCase.setFont(Utility.getFont());
		chk_inc_ignoreCase.setBounds(122, 51, 94, 16);

		Label lb_inc_uri = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_uri.setFont(Utility.getFont());
		lb_inc_uri.setText("Request URI");
		lb_inc_uri.setAlignment(SWT.RIGHT);
		lb_inc_uri.setBounds(10, 79, 100, 15);

		txt_inc_uri = new Text(grp_inclueFilter, SWT.BORDER);
		txt_inc_uri.setFont(Utility.getFont());
		txt_inc_uri.setBounds(122, 76, 208, 21);
		txt_inc_uri.setEnabled(false);

		Label lb_inc_ext = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_ext.setFont(Utility.getFont());
		lb_inc_ext.setText("Request Ext");
		lb_inc_ext.setAlignment(SWT.RIGHT);
		lb_inc_ext.setBounds(10, 106, 100, 15);

		txt_inc_ext = new Text(grp_inclueFilter, SWT.BORDER);
		txt_inc_ext.setFont(Utility.getFont());
		txt_inc_ext.setBounds(122, 103, 208, 21);
		txt_inc_ext.setEnabled(false);

		Label lb_inc_ip = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_ip.setFont(Utility.getFont());
		lb_inc_ip.setText("Request IP");
		lb_inc_ip.setAlignment(SWT.RIGHT);
		lb_inc_ip.setBounds(10, 133, 100, 15);

		txt_inc_ip = new Text(grp_inclueFilter, SWT.BORDER);
		txt_inc_ip.setFont(Utility.getFont());
		txt_inc_ip.setBounds(122, 130, 208, 21);
		txt_inc_ip.setEnabled(false);

		Label lb_inc_method = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_method.setFont(Utility.getFont());
		lb_inc_method.setText("Request Method");
		lb_inc_method.setAlignment(SWT.RIGHT);
		lb_inc_method.setBounds(10, 160, 100, 15);

		txt_inc_method = new Text(grp_inclueFilter, SWT.BORDER);
		txt_inc_method.setFont(Utility.getFont());
		txt_inc_method.setBounds(122, 157, 208, 21);
		txt_inc_method.setEnabled(false);

		Label lb_inc_version = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_version.setFont(Utility.getFont());
		lb_inc_version.setText("Request Version");
		lb_inc_version.setAlignment(SWT.RIGHT);
		lb_inc_version.setBounds(10, 187, 100, 15);

		txt_inc_version = new Text(grp_inclueFilter, SWT.BORDER);
		txt_inc_version.setFont(Utility.getFont());
		txt_inc_version.setBounds(122, 184, 208, 21);
		txt_inc_version.setEnabled(false);

		Label lb_inc_code = new Label(grp_inclueFilter, SWT.NONE);
		lb_inc_code.setFont(Utility.getFont());
		lb_inc_code.setText("Response Code");
		lb_inc_code.setAlignment(SWT.RIGHT);
		lb_inc_code.setBounds(10, 214, 100, 15);

		txt_inc_code = new Text(grp_inclueFilter, SWT.BORDER);
		txt_inc_code.setFont(Utility.getFont());
		txt_inc_code.setBounds(122, 211, 208, 21);
		txt_inc_code.setEnabled(false);

		chk_excludeFilter = new Button(this, SWT.CHECK);
		chk_excludeFilter.setFont(Utility.getFont());
		chk_excludeFilter.setText("Exclude Filter");
		chk_excludeFilter.setBounds(479, 59, 94, 16);

		grp_exclueFilter = new Group(this, SWT.NONE);
		grp_exclueFilter.setBounds(356, 74, 340, 245);

		Label lb_exc_chkOperator = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_chkOperator.setFont(Utility.getFont());
		lb_exc_chkOperator.setText("Check Operator");
		lb_exc_chkOperator.setAlignment(SWT.RIGHT);
		lb_exc_chkOperator.setBounds(10, 25, 100, 15);

		btn_exc_chkAnd = new Button(grp_exclueFilter, SWT.RADIO);
		btn_exc_chkAnd.setSelection(true);
		btn_exc_chkAnd.setFont(Utility.getFont());
		btn_exc_chkAnd.setText("AND");
		btn_exc_chkAnd.setBounds(122, 25, 60, 15);

		btn_exc_chkOr = new Button(grp_exclueFilter, SWT.RADIO);
		btn_exc_chkOr.setFont(Utility.getFont());
		btn_exc_chkOr.setText("OR");
		btn_exc_chkOr.setBounds(188, 25, 60, 15);

		Label lb_exc_ignoreCase = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_ignoreCase.setFont(Utility.getFont());
		lb_exc_ignoreCase.setText("Ignore Case");
		lb_exc_ignoreCase.setAlignment(SWT.RIGHT);
		lb_exc_ignoreCase.setBounds(10, 52, 100, 15);

		chk_exc_ignoreCase = new Button(grp_exclueFilter, SWT.CHECK);
		chk_exc_ignoreCase.setFont(Utility.getFont());
		chk_exc_ignoreCase.setBounds(122, 52, 94, 15);

		Label lb_exc_uri = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_uri.setFont(Utility.getFont());
		lb_exc_uri.setText("Request URI");
		lb_exc_uri.setAlignment(SWT.RIGHT);
		lb_exc_uri.setBounds(10, 79, 100, 15);

		txt_exc_uri = new Text(grp_exclueFilter, SWT.BORDER);
		txt_exc_uri.setFont(Utility.getFont());
		txt_exc_uri.setBounds(122, 76, 208, 21);
		txt_exc_uri.setEnabled(false);

		Label lb_exc_ext = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_ext.setFont(Utility.getFont());
		lb_exc_ext.setText("Request Ext");
		lb_exc_ext.setAlignment(SWT.RIGHT);
		lb_exc_ext.setBounds(10, 106, 100, 15);

		txt_exc_ext = new Text(grp_exclueFilter, SWT.BORDER);
		txt_exc_ext.setFont(Utility.getFont());
		txt_exc_ext.setBounds(122, 103, 208, 21);
		txt_exc_ext.setEnabled(false);

		Label lb_exc_ip = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_ip.setFont(Utility.getFont());
		lb_exc_ip.setText("Request IP");
		lb_exc_ip.setAlignment(SWT.RIGHT);
		lb_exc_ip.setBounds(10, 133, 100, 15);

		txt_exc_ip = new Text(grp_exclueFilter, SWT.BORDER);
		txt_exc_ip.setFont(Utility.getFont());
		txt_exc_ip.setBounds(122, 130, 208, 21);
		txt_exc_ip.setEnabled(false);

		Label lb_exc_method = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_method.setFont(Utility.getFont());
		lb_exc_method.setText("Request Method");
		lb_exc_method.setAlignment(SWT.RIGHT);
		lb_exc_method.setBounds(10, 160, 100, 15);

		txt_exc_method = new Text(grp_exclueFilter, SWT.BORDER);
		txt_exc_method.setFont(Utility.getFont());
		txt_exc_method.setBounds(122, 157, 208, 21);
		txt_exc_method.setEnabled(false);

		Label lb_exc_version = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_version.setFont(Utility.getFont());
		lb_exc_version.setText("Request Version");
		lb_exc_version.setAlignment(SWT.RIGHT);
		lb_exc_version.setBounds(10, 187, 100, 15);

		txt_exc_version = new Text(grp_exclueFilter, SWT.BORDER);
		txt_exc_version.setFont(Utility.getFont());
		txt_exc_version.setBounds(122, 184, 208, 21);
		txt_exc_version.setEnabled(false);

		Label lb_exc_code = new Label(grp_exclueFilter, SWT.NONE);
		lb_exc_code.setFont(Utility.getFont());
		lb_exc_code.setText("Response Code");
		lb_exc_code.setAlignment(SWT.RIGHT);
		lb_exc_code.setBounds(10, 214, 100, 15);

		txt_exc_code = new Text(grp_exclueFilter, SWT.BORDER);
		txt_exc_code.setFont(Utility.getFont());
		txt_exc_code.setBounds(122, 211, 208, 21);
		txt_exc_code.setEnabled(false);

		grp_inclueFilter.setTabList(new Control[] { btn_inc_chkAnd, btn_inc_chkOr, chk_inc_ignoreCase, txt_inc_uri, txt_inc_ext, txt_inc_ip, txt_inc_method, txt_inc_version, txt_inc_code });
		grp_exclueFilter.setTabList(new Control[] { btn_exc_chkAnd, btn_exc_chkOr, chk_exc_ignoreCase, txt_exc_uri, txt_exc_ext, txt_exc_ip, txt_exc_method, txt_exc_version, txt_exc_code });

		setTabList(new Control[] { chk_includeFilter, chk_excludeFilter });

	}

	protected void addEventListener() {

		chk_allRange.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dtp_fromDate.setEnabled(!chk_allRange.getSelection());
				dtp_toDate.setEnabled(!chk_allRange.getSelection());
			}
		});

		chk_includeFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				grp_inclueFilter.setEnabled(chk_includeFilter.getSelection());
			}
		});

		chk_excludeFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				grp_exclueFilter.setEnabled(chk_excludeFilter.getSelection());
			}
		});

	}

	public void updateFromDate(Date dt) {
		if(chk_allRange.getSelection() && (!updatedFromDate || dt.compareTo(dtp_fromDate.getDate()) < 0)) {
			dtp_fromDate.setDate(dt);
			updatedFromDate = true;
		}
	}

	public void updateToDate(Date dt) {
		if(chk_allRange.getSelection() && (!updatedToDate || dt.compareTo(dtp_toDate.getDate()) > 0)) {
			dtp_toDate.setDate(dt);
			updatedToDate = true;
		}
	}

	public void resetDateUpdateCheck() {
		updatedFromDate = false;
		updatedToDate = false;
	}

	public void setURIEnabled(boolean flag) {
		txt_inc_uri.setEnabled(flag);
		txt_exc_uri.setEnabled(flag);
		txt_inc_ext.setEnabled(flag);
		txt_exc_ext.setEnabled(flag);
	}

	public void setIPEnabled(boolean flag) {
		txt_inc_ip.setEnabled(flag);
		txt_exc_ip.setEnabled(flag);
	}

	public void setMethodEnabled(boolean flag) {
		txt_inc_method.setEnabled(flag);
		txt_exc_method.setEnabled(flag);
	}

	public void setVersionEnabled(boolean flag) {
		txt_inc_version.setEnabled(flag);
		txt_exc_version.setEnabled(flag);
	}

	public void setCodeEnabled(boolean flag) {
		txt_inc_code.setEnabled(flag);
		txt_exc_code.setEnabled(flag);
	}
}
