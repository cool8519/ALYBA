package dal.tool.analyzer.alyba.ui.chart.setting;

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

import dal.tool.analyzer.alyba.ui.chart.Chart;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.tool.analyzer.alyba.ui.comp.ResultChart;
import dal.tool.analyzer.alyba.util.Utility;

public class TimeSeriesChartSetting extends ChartSetting {

	private Group grp_setting;
	private Button chk_sma;
	private Button chk_merge;
	private Button chk_shape;
	private Button chk_secondary_data;
	private Button btn_apply;
	private Spinner sp_sma_items;
	private Spinner sp_merge_items;
	private Label lb_sma_avg;
	private Label lb_sma_unit;
	private Label lb_merge_every;
	private Label lb_merge_unit;

	public TimeSeriesChartSetting(Composite parent, ResultChart result_chart) {
		super(parent, result_chart);
		createContents();
		addEventListener();
	}

	protected void createContents() {
		setLayout(new FillLayout(SWT.HORIZONTAL));

	    FormLayout forml_grp_setting = new FormLayout();
	    forml_grp_setting.marginHeight = 15;
	    forml_grp_setting.marginWidth = 15;
		grp_setting = new Group(this, SWT.NONE);
		grp_setting.setLayout(forml_grp_setting);

		FormData fd_chk_sma = new FormData();
		fd_chk_sma.left = new FormAttachment(grp_setting, 0, SWT.LEFT);
		fd_chk_sma.top = new FormAttachment(grp_setting, 0);
		chk_sma = new Button(grp_setting, SWT.CHECK);
		chk_sma.setLayoutData(fd_chk_sma);
		chk_sma.setFont(Utility.getFont());
		chk_sma.setText(" Simple Moving Average");
		
		FormData fd_lb_sma_avg = new FormData();
		fd_lb_sma_avg.left = new FormAttachment(chk_sma, 20, SWT.LEFT);
		fd_lb_sma_avg.top = new FormAttachment(chk_sma, 10);
		lb_sma_avg = new Label(grp_setting, SWT.NONE);
		lb_sma_avg.setLayoutData(fd_lb_sma_avg);
		lb_sma_avg.setAlignment(SWT.RIGHT);
		lb_sma_avg.setFont(Utility.getFont());
		lb_sma_avg.setText("average");

		FormData fd_sp_sma_items = new FormData();
		fd_sp_sma_items.top = new FormAttachment(lb_sma_avg, -2, SWT.TOP);
		fd_sp_sma_items.left = new FormAttachment(lb_sma_avg, 6);
		fd_sp_sma_items.width = 20;
		fd_sp_sma_items.height = 16;
		sp_sma_items = new Spinner(grp_setting, SWT.BORDER);
		sp_sma_items.setLayoutData(fd_sp_sma_items);
		sp_sma_items.setFont(Utility.getFont());
		sp_sma_items.setTextLimit(3);
		sp_sma_items.setMaximum(999);
		sp_sma_items.setMinimum(1);
		sp_sma_items.setSelection(1);
		sp_sma_items.setEnabled(false);
		
		FormData fd_lb_sma_unit = new FormData();
		fd_lb_sma_unit.left = new FormAttachment(sp_sma_items, 6);
		fd_lb_sma_unit.top = new FormAttachment(chk_sma, 10);
		fd_lb_sma_unit.width = 60;
		lb_sma_unit = new Label(grp_setting, SWT.NONE);
		lb_sma_unit.setLayoutData(fd_lb_sma_unit);
		lb_sma_unit.setFont(Utility.getFont());
		lb_sma_unit.setText("unit(s)");		

		FormData fd_chk_merge = new FormData();
		fd_chk_merge.top = new FormAttachment(sp_sma_items, 30);
		fd_chk_merge.left = new FormAttachment(chk_sma, 0, SWT.LEFT);
		chk_merge = new Button(grp_setting, SWT.CHECK);
		chk_merge.setLayoutData(fd_chk_merge);
		chk_merge.setFont(Utility.getFont());
		chk_merge.setText(" Merge");
		
		FormData fd_lb_merge_every = new FormData();
		fd_lb_merge_every.left = new FormAttachment(chk_merge, 20, SWT.LEFT);
		fd_lb_merge_every.top = new FormAttachment(chk_merge, 10);
		lb_merge_every = new Label(grp_setting, SWT.NONE);
		lb_merge_every.setLayoutData(fd_lb_merge_every);
		lb_merge_every.setAlignment(SWT.RIGHT);
		lb_merge_every.setFont(Utility.getFont());
		lb_merge_every.setText("every");

		FormData fd_sp_merge_items = new FormData();
		fd_sp_merge_items.top = new FormAttachment(lb_merge_every, -2, SWT.TOP);
		fd_sp_merge_items.left = new FormAttachment(lb_merge_every, 6);
		fd_sp_merge_items.width = 20;
		fd_sp_merge_items.height = 16;
		sp_merge_items = new Spinner(grp_setting, SWT.BORDER);
		sp_merge_items.setLayoutData(fd_sp_merge_items);
		sp_merge_items.setFont(Utility.getFont());
		sp_merge_items.setTextLimit(3);
		sp_merge_items.setMaximum(999);
		sp_merge_items.setMinimum(1);		
		sp_merge_items.setSelection(1);
		sp_merge_items.setEnabled(false);
		
		FormData fd_lb_merge_unit = new FormData();
		fd_lb_merge_unit.left = new FormAttachment(sp_merge_items, 6);
		fd_lb_merge_unit.top = new FormAttachment(chk_merge, 10);
		fd_lb_merge_unit.width = 60;
		lb_merge_unit = new Label(grp_setting, SWT.NONE);
		lb_merge_unit.setLayoutData(fd_lb_merge_unit);
		lb_merge_unit.setFont(Utility.getFont());
		lb_merge_unit.setText("unit(s)");

		FormData fd_chk_shape = new FormData();
		fd_chk_shape.top = new FormAttachment(sp_merge_items, 30);
		fd_chk_shape.left = new FormAttachment(chk_sma, 0, SWT.LEFT);
		chk_shape = new Button(grp_setting, SWT.CHECK);
		chk_shape.setLayoutData(fd_chk_shape);
		chk_shape.setFont(Utility.getFont());
		chk_shape.setText(" Shape on point");

		FormData fd_chk_secondary_data = new FormData();
		fd_chk_secondary_data.top = new FormAttachment(chk_shape, 30);
		fd_chk_secondary_data.left = new FormAttachment(chk_shape, 0, SWT.LEFT);
		chk_secondary_data = new Button(grp_setting, SWT.CHECK);
		chk_secondary_data.setLayoutData(fd_chk_secondary_data);
		chk_secondary_data.setFont(Utility.getFont());
		chk_secondary_data.setText(" Secondary Axis Data");

		FormData fd_btn_apply = new FormData();
		fd_btn_apply.right = new FormAttachment(100);
		fd_btn_apply.bottom = new FormAttachment(100);
		fd_btn_apply.width = 80;
		btn_apply = new Button(grp_setting, SWT.NONE);
		btn_apply.setFont(Utility.getFont());
		btn_apply.setText("Apply");
		btn_apply.setLayoutData(fd_btn_apply);
		
		grp_setting.setTabList(new Control[]{ chk_sma, sp_sma_items, chk_merge, sp_merge_items, chk_shape, chk_secondary_data, btn_apply });

	}

	protected void addEventListener() {

		btn_apply.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		result_chart.clickApplyButton();
	    	}
	    });

		chk_sma.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		sp_sma_items.setEnabled(chk_sma.getSelection());
	    	}
	    });

		chk_merge.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		sp_merge_items.setEnabled(chk_merge.getSelection());
	    	}
	    });

	}
	
	public void init() {
	}
	
	public void reset(Chart chart) {
		if(chart != null) {
			TimeSeriesChart tsChart = (TimeSeriesChart)chart;
			chk_sma.setSelection(tsChart.getShowMovingAverage());
			sp_sma_items.setSelection(tsChart.getMovingAverageCount());
			sp_sma_items.setEnabled(tsChart.getShowMovingAverage());
			chk_merge.setSelection(tsChart.getMergeItem());
			sp_merge_items.setSelection(tsChart.getMergeItemCount());
			sp_merge_items.setEnabled(tsChart.getMergeItem());
			chk_shape.setSelection(tsChart.getShowShape());
			lb_sma_unit.setText(tsChart.getUnitString());		
			lb_merge_unit.setText(tsChart.getUnitString());
			chk_secondary_data.setSelection(tsChart.getShowSecondaryAxis());
		} else {
			chk_sma.setSelection(false);
			sp_sma_items.setSelection(1);
			sp_sma_items.setEnabled(false);
			chk_merge.setSelection(false);
			sp_merge_items.setSelection(1);
			sp_merge_items.setEnabled(false);
			chk_shape.setSelection(false);
			lb_sma_unit.setText("unit(s)");		
			lb_merge_unit.setText("unit(s)");
			chk_secondary_data.setSelection(false);
		}
	}
	
	public void configure(Chart chart) {
		TimeSeriesChart ts_chart = (TimeSeriesChart)chart;
		ts_chart.setShowMovingAverage(chk_sma.getSelection());
		ts_chart.setMovingAverageCount(sp_sma_items.getSelection());
		ts_chart.setMergeItem(chk_merge.getSelection());
		ts_chart.setMergeItemCount(sp_merge_items.getSelection());
		ts_chart.setShowShape(chk_shape.getSelection());
		ts_chart.setShowSecondaryAxis(chk_secondary_data.getSelection());
	}

}
