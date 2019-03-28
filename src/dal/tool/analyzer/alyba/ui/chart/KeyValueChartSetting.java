package dal.tool.analyzer.alyba.ui.chart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import dal.tool.analyzer.alyba.ui.comp.ResultChart;

public class KeyValueChartSetting extends ChartSetting {

	private Group grp_setting;
	private Button chk_merge;
	private Button chk_item_label;
	private Button btn_apply;
	private Spinner sp_merge_max;
	private Spinner sp_merge_pct;
	private Label lb_merge_max;
	private Label lb_merge_item;
	private Label lb_merge_less;
	private Label lb_merge_pct;

	public KeyValueChartSetting(Composite parent, ResultChart result_chart) {
		super(parent, result_chart);
		createContents();
		addEventListener();
	}

	protected void createContents() {
		setLayout(new FillLayout(SWT.HORIZONTAL));

	    FormLayout forml_grp_left = new FormLayout();
	    forml_grp_left.marginHeight = 15;
	    forml_grp_left.marginWidth = 15;

		grp_setting = new Group(this, SWT.NONE);
		grp_setting.setLayout(forml_grp_left);

		chk_merge = new Button(grp_setting, SWT.CHECK);
		chk_merge.setLayoutData(new FormData());
		chk_merge.setText(" Merge as \"Others\"");
		chk_merge.setBounds(20, 20, 170, 16);
		chk_merge.setSelection(true);
		
		FormData fd_lb_merge_max = new FormData();
		fd_lb_merge_max.left = new FormAttachment(chk_merge, 20, SWT.LEFT);
		fd_lb_merge_max.top = new FormAttachment(chk_merge, 10);
		lb_merge_max = new Label(grp_setting, SWT.NONE);
		lb_merge_max.setLayoutData(fd_lb_merge_max);
		lb_merge_max.setAlignment(SWT.RIGHT);
		lb_merge_max.setText("max");

		FormData fd_sp_merge_max = new FormData();
		fd_sp_merge_max.top = new FormAttachment(lb_merge_max, -2, SWT.TOP);
		fd_sp_merge_max.left = new FormAttachment(lb_merge_max, 6);
		fd_sp_merge_max.width = 25;
		sp_merge_max = new Spinner(grp_setting, SWT.BORDER);
		sp_merge_max.setLayoutData(fd_sp_merge_max);
		sp_merge_max.setTextLimit(3);
		sp_merge_max.setMaximum(999);
		sp_merge_max.setMinimum(1);
		sp_merge_max.setSelection(20);
		
		FormData fd_lb_merge_item = new FormData();
		fd_lb_merge_item.left = new FormAttachment(sp_merge_max, 6);
		fd_lb_merge_item.top = new FormAttachment(chk_merge, 10);
		lb_merge_item = new Label(grp_setting, SWT.NONE);
		lb_merge_item.setLayoutData(fd_lb_merge_item);
		lb_merge_item.setText("items");	
		
		FormData fd_lb_merge_less = new FormData();
		fd_lb_merge_less.top = new FormAttachment(lb_merge_max, 12);
		fd_lb_merge_less.left = new FormAttachment(chk_merge, 20, SWT.LEFT);
		lb_merge_less = new Label(grp_setting, SWT.NONE);
		lb_merge_less.setLayoutData(fd_lb_merge_less);
		lb_merge_less.setAlignment(SWT.RIGHT);
		lb_merge_less.setText("less than");

		FormData fd_sp_merge_pct = new FormData();
		fd_sp_merge_pct.top = new FormAttachment(lb_merge_less, -2, SWT.TOP);
		fd_sp_merge_pct.left = new FormAttachment(lb_merge_less, 6);
		fd_sp_merge_pct.width = 25;
		sp_merge_pct = new Spinner(grp_setting, SWT.BORDER);
		sp_merge_pct.setLayoutData(fd_sp_merge_pct);
		sp_merge_pct.setTextLimit(4);
		sp_merge_pct.setDigits(1);
		sp_merge_pct.setMinimum(0);
		sp_merge_pct.setMaximum(999);
		sp_merge_pct.setIncrement(10);
		sp_merge_pct.setSelection(10);
		
		FormData fd_lb_merge_pct = new FormData();
		fd_lb_merge_pct.top = new FormAttachment(lb_merge_max, 12);
		fd_lb_merge_pct.left = new FormAttachment(sp_merge_pct, 6);
		lb_merge_pct = new Label(grp_setting, SWT.NONE);
		lb_merge_pct.setLayoutData(fd_lb_merge_pct);
		lb_merge_pct.setText("%");

		FormData fd_chk_item_label = new FormData();
		fd_chk_item_label.top = new FormAttachment(lb_merge_less, 30);
		fd_chk_item_label.left = new FormAttachment(chk_merge, 0, SWT.LEFT);
		chk_item_label = new Button(grp_setting, SWT.CHECK);
		chk_item_label.setLayoutData(fd_chk_item_label);
		chk_item_label.setText(" Item label");

		FormData fd_btn_apply = new FormData();
		fd_btn_apply.right = new FormAttachment(100);
		fd_btn_apply.bottom = new FormAttachment(100);
		fd_btn_apply.width = 80;
		btn_apply = new Button(grp_setting, SWT.NONE);
		btn_apply.setText("Apply");
		btn_apply.setLayoutData(fd_btn_apply);
		
		grp_setting.setTabList(new Control[]{ chk_merge, sp_merge_max, sp_merge_pct, chk_item_label, btn_apply });

	}

	protected void addEventListener() {

		btn_apply.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		result_chart.clickApplyButton();
	    	}
	    });

		chk_merge.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		sp_merge_max.setEnabled(chk_merge.getSelection());
	    		sp_merge_pct.setEnabled(chk_merge.getSelection());
	    	}
	    });

	}
	
	protected void reset() {
		if(result_chart.getCurrentChart() != null) {
			KeyValueChart chart = (KeyValueChart)result_chart.getCurrentChart();
			chk_merge.setSelection(chart.getMergeToOthers());
			sp_merge_max.setSelection(chart.getMaxItemCount());
			sp_merge_max.setEnabled(chart.getMergeToOthers());
			sp_merge_pct.setSelection((int)(chart.getMinItemPercent()*Math.pow(10, sp_merge_pct.getDigits())));
			sp_merge_pct.setEnabled(chart.getMergeToOthers());
			chk_item_label.setSelection(chart.getShowItemLabel());
		} else {
			chk_merge.setSelection(true);
			sp_merge_max.setSelection(20);
			sp_merge_max.setEnabled(true);
			sp_merge_pct.setSelection(10);
			sp_merge_pct.setEnabled(true);
			chk_item_label.setSelection(true);
		}		
	}

	public void configure(Chart chart) {
		KeyValueChart kv_chart = (KeyValueChart)chart;
		kv_chart.setMergeToOthers(chk_merge.getSelection());
		kv_chart.setMaxItemCount(sp_merge_max.getSelection());
		kv_chart.setMinItemPercent((float)(sp_merge_pct.getSelection()/Math.pow(10, sp_merge_pct.getDigits())));
		kv_chart.setShowItemLabel(chk_item_label.getSelection());
	}
	
}
