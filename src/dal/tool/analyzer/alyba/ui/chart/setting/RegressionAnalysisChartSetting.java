package dal.tool.analyzer.alyba.ui.chart.setting;

import javax.persistence.EntityManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.chart.Chart;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.AggregationType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.RegressionType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.ResourceMergeType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableX;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableY;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;
import dal.tool.analyzer.alyba.ui.comp.ResultChart;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.db.ObjectDBUtil;

public class RegressionAnalysisChartSetting extends ChartSetting {

	private Group grp_setting;
	private Group grp_aggr;
	private Group grp_merge;
	private Label lb_aggr;
	private Label lb_var_x;	
	private Label lb_var_y;
	private Label lb_reg_type;
	private Button btn_aggr_byname;
	private Button btn_aggr_bygroup;
	private Button btn_aggr_allinone;
	private Button btn_merge_avg;
	private Button btn_merge_sum;
	private Combo cb_var_x;
	private Combo cb_var_y;
	private Combo cb_reg_type;
	private Button chk_reg_line;
	private Button chk_reg_equation;
	private Button chk_max_y_to_100;
	private Button btn_apply;

	public RegressionAnalysisChartSetting(Composite parent, ResultChart result_chart) {
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

		FormData fd_lb_aggr = new FormData();
		fd_lb_aggr.left = new FormAttachment(grp_setting, 0, SWT.LEFT);
		fd_lb_aggr.top = new FormAttachment(grp_setting, 0);
		lb_aggr = new Label(grp_setting, SWT.NONE);
		lb_aggr.setLayoutData(fd_lb_aggr);
		lb_aggr.setFont(Utility.getFont());
		lb_aggr.setText("Aggregation");
		
		FormData fd_grp_aggr = new FormData();
		fd_grp_aggr.right = new FormAttachment(80);
		fd_grp_aggr.top = new FormAttachment(lb_aggr, 0);
		fd_grp_aggr.left = new FormAttachment(lb_aggr, 20, SWT.LEFT);
		grp_aggr = new Group(grp_setting, SWT.NONE);
		grp_aggr.setLayoutData(fd_grp_aggr);
		GridLayout gl_aggr = new GridLayout();
		gl_aggr.verticalSpacing = 2;
		gl_aggr.marginHeight = 0;
		gl_aggr.marginBottom = 5;
		gl_aggr.numColumns = 1;
		grp_aggr.setLayout(gl_aggr);

		btn_aggr_byname = new Button(grp_aggr, SWT.RADIO);
		btn_aggr_byname.setText("by Name");
		btn_aggr_byname.setSelection(true);
		btn_aggr_bygroup = new Button(grp_aggr, SWT.RADIO);
		btn_aggr_bygroup.setText("by Group");
		btn_aggr_allinone = new Button(grp_aggr, SWT.RADIO);
		btn_aggr_allinone.setText("All-In-One");

		FormData fd_grp_merge = new FormData();
		fd_grp_merge.left = new FormAttachment(grp_aggr, 0, SWT.LEFT);
		fd_grp_merge.right = new FormAttachment(80);
		fd_grp_merge.top = new FormAttachment(grp_aggr, 0);
		grp_merge = new Group(grp_setting, SWT.NONE);
		grp_merge.setLayoutData(fd_grp_merge);
		FillLayout gl_merge = new FillLayout();
		gl_merge.spacing = 5;
		gl_merge.marginWidth = 5;
		gl_merge.marginHeight = 0;
		grp_merge.setLayout(gl_merge);
		grp_merge.setEnabled(false);

		btn_merge_avg = new Button(grp_merge, SWT.RADIO);
		btn_merge_avg.setText("Avg");
		btn_merge_avg.setSelection(true);
		btn_merge_avg.setEnabled(false);
		btn_merge_sum = new Button(grp_merge, SWT.RADIO);
		btn_merge_sum.setText("Sum");
		btn_merge_sum.setEnabled(false);

		FormData fd_lb_var_x = new FormData();
		fd_lb_var_x.top = new FormAttachment(grp_merge, 25);
		fd_lb_var_x.left = new FormAttachment(lb_aggr, 0, SWT.LEFT);
		lb_var_x = new Label(grp_setting, SWT.CENTER);
		lb_var_x.setLayoutData(fd_lb_var_x);
		lb_var_x.setFont(Utility.getFont());
		lb_var_x.setText("Regression Variable-X");
		
		FormData fd_cb_var_x = new FormData();
		fd_cb_var_x.top = new FormAttachment(lb_var_x, 5);
		fd_cb_var_x.left = new FormAttachment(lb_var_x, 20, SWT.LEFT);
		fd_cb_var_x.right = new FormAttachment(100, -5);
		cb_var_x = new Combo(grp_setting, SWT.DROP_DOWN | SWT.READ_ONLY);
		cb_var_x.setLayoutData(fd_cb_var_x);
		cb_var_x.setFont(Utility.getFont());

		FormData fd_lb_var_y = new FormData();
		fd_lb_var_y.top = new FormAttachment(cb_var_x, 15);
		fd_lb_var_y.left = new FormAttachment(lb_var_x, 0, SWT.LEFT);
		lb_var_y = new Label(grp_setting, SWT.CENTER);
		lb_var_y.setLayoutData(fd_lb_var_y);
		lb_var_y.setFont(Utility.getFont());
		lb_var_y.setText("Regression Variable-Y");

		FormData fd_cb_var_y = new FormData();
		fd_cb_var_y.top = new FormAttachment(lb_var_y, 5);
		fd_cb_var_y.left = new FormAttachment(lb_var_y, 20, SWT.LEFT);
		fd_cb_var_y.right = new FormAttachment(100, -5);
		cb_var_y = new Combo(grp_setting, SWT.DROP_DOWN | SWT.READ_ONLY);
		cb_var_y.setLayoutData(fd_cb_var_y);
		cb_var_y.setFont(Utility.getFont());

		FormData fd_lb_reg_type = new FormData();
		fd_lb_reg_type.top = new FormAttachment(cb_var_y, 15);
		fd_lb_reg_type.left = new FormAttachment(lb_var_y, 0, SWT.LEFT);
		lb_reg_type = new Label(grp_setting, SWT.CENTER);
		lb_reg_type.setLayoutData(fd_lb_reg_type);
		lb_reg_type.setFont(Utility.getFont());
		lb_reg_type.setText("Regression Type");

		FormData fd_cb_reg_type = new FormData();
		fd_cb_reg_type.top = new FormAttachment(lb_reg_type, 5);
		fd_cb_reg_type.left = new FormAttachment(lb_reg_type, 20, SWT.LEFT);
		fd_cb_reg_type.right = new FormAttachment(100, -5);
		cb_reg_type = new Combo(grp_setting, SWT.DROP_DOWN | SWT.READ_ONLY);
		cb_reg_type.setLayoutData(fd_cb_reg_type);
		cb_reg_type.setFont(Utility.getFont());
		
		FormData fd_chk_reg_line = new FormData();
		fd_chk_reg_line.top = new FormAttachment(cb_reg_type, 25);
		fd_chk_reg_line.left = new FormAttachment(lb_reg_type, 0, SWT.LEFT);
		chk_reg_line = new Button(grp_setting, SWT.CHECK);
		chk_reg_line.setLayoutData(fd_chk_reg_line);
		chk_reg_line.setFont(Utility.getFont());
		chk_reg_line.setText(" Regression Line");
		chk_reg_line.setSelection(true);

		FormData fd_chk_reg_equation = new FormData();
		fd_chk_reg_equation.top = new FormAttachment(chk_reg_line, 10);
		fd_chk_reg_equation.left = new FormAttachment(chk_reg_line, 20, SWT.LEFT);
		chk_reg_equation = new Button(grp_setting, SWT.CHECK);
		chk_reg_equation.setLayoutData(fd_chk_reg_equation);
		chk_reg_equation.setFont(Utility.getFont());
		chk_reg_equation.setText(" Regression Equation");
		chk_reg_equation.setSelection(true);
		
		FormData fd_chk_max_y_to_100 = new FormData();
		fd_chk_max_y_to_100.top = new FormAttachment(chk_reg_equation, 25);
		fd_chk_max_y_to_100.left = new FormAttachment(chk_reg_line, 0, SWT.LEFT);
		chk_max_y_to_100 = new Button(grp_setting, SWT.CHECK);
		chk_max_y_to_100.setLayoutData(fd_chk_max_y_to_100);
		chk_max_y_to_100.setFont(Utility.getFont());
		chk_max_y_to_100.setText(" Set Y-Axis max to 100");
		chk_max_y_to_100.setSelection(false);

		FormData fd_btn_apply = new FormData();
		fd_btn_apply.right = new FormAttachment(100);
		fd_btn_apply.bottom = new FormAttachment(100);
		fd_btn_apply.width = 80;
		btn_apply = new Button(grp_setting, SWT.NONE);
		btn_apply.setFont(Utility.getFont());
		btn_apply.setText("Apply");
		btn_apply.setLayoutData(fd_btn_apply);
		
		grp_setting.setTabList(new Control[]{ grp_aggr, grp_merge, cb_var_x, cb_var_y, cb_reg_type, chk_reg_line, chk_reg_equation, chk_max_y_to_100, btn_apply });

	}

	protected void addEventListener() {

		cb_var_y.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				boolean usage_selected = cb_var_y.getText().endsWith(" Usage");
				grp_aggr.setEnabled(usage_selected);
				btn_aggr_byname.setEnabled(usage_selected);
				btn_aggr_bygroup.setEnabled(usage_selected);
				btn_aggr_allinone.setEnabled(usage_selected);
				chk_max_y_to_100.setEnabled(usage_selected);
			}
		});
		
		btn_aggr_byname.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				grp_merge.setEnabled(false);
				btn_merge_avg.setEnabled(false);
				btn_merge_sum.setEnabled(false);
			}
		});

		btn_aggr_bygroup.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				grp_merge.setEnabled(true);
				btn_merge_avg.setEnabled(true);
				btn_merge_sum.setEnabled(true);
			}
		});
		
		btn_aggr_allinone.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				grp_merge.setEnabled(true);
				btn_merge_avg.setEnabled(true);
				btn_merge_sum.setEnabled(true);
			}
		});
		
		chk_reg_line.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
				chk_reg_equation.setEnabled(chk_reg_line.getSelection());
	    	}
	    });

		btn_apply.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		result_chart.clickApplyButton();
	    	}
	    });

	}

	public void init() {
		boolean resource_flag = false;
		boolean cpu_flag = false;
		boolean mem_flag = false;
		boolean dsk_flag = false;
		boolean net_flag = false;
		boolean ip_flag = false;
		boolean elapsed_flag = false;		

		ObjectDBUtil db = ObjectDBUtil.getInstance();
		EntityManager em = db.createEntityManager();
		try {			
			resource_flag = db.count(em, ResourceUsageEntryVO.class) > 1;
			cpu_flag = db.count(em, "SELECT COUNT(o) FROM ResourceUsageEntryVO AS o WHERE o.cpu <> -1") > 1;
			mem_flag = db.count(em, "SELECT COUNT(o) FROM ResourceUsageEntryVO AS o WHERE o.memory <> -1") > 1;
			dsk_flag = db.count(em, "SELECT COUNT(o) FROM ResourceUsageEntryVO AS o WHERE o.disk <> -1") > 1;
			net_flag = db.count(em, "SELECT COUNT(o) FROM ResourceUsageEntryVO AS o WHERE o.network <> -1") > 1;
			ip_flag = ResultAnalyzer.hasMappingInfo("IP");
			elapsed_flag = ResultAnalyzer.hasMappingInfo("ELAPSED");
		} catch(Exception e) {
			Logger.debug("Failed to select count from ResourceUsageEntryVO.");
			Logger.error(e);
		} finally {
			if(db != null) {
				db.close(em);
			}
		}
			
		grp_aggr.setEnabled(resource_flag);
		btn_aggr_byname.setEnabled(resource_flag);
		btn_aggr_bygroup.setEnabled(resource_flag);
		btn_aggr_allinone.setEnabled(resource_flag);
		
		grp_merge.setEnabled(false);
		btn_merge_avg.setEnabled(false);
		btn_merge_sum.setEnabled(false);

		cb_var_x.removeAll();
		cb_var_y.removeAll();
		cb_reg_type.removeAll();
		
		cb_var_x.add("Transactions");
		if(ip_flag) {
			cb_var_x.add("Request IPs");
		}
		cb_var_x.select(0);

		if(resource_flag) {
			if(cpu_flag) cb_var_y.add("CPU Usage");
			if(mem_flag) cb_var_y.add("Memory Usage");
			if(dsk_flag) cb_var_y.add("Disk Usage");
			if(net_flag) cb_var_y.add("Network Usage");
		}
		if(elapsed_flag) {
			cb_var_y.add("Average Response-Time");
		}
		cb_var_y.add("Errors");
		cb_var_y.select(0);

		for(RegressionType type : RegressionType.values()) {
			cb_reg_type.add(type.name());			
		}
		cb_reg_type.select(0);
		
		chk_max_y_to_100.setEnabled(cb_var_y.getText().endsWith(" Usage"));
	}
	
	public void reset(Chart chart) {
		cb_var_x.select(0);
		cb_var_y.select(0);
		cb_reg_type.select(0);			
		btn_aggr_byname.setSelection(true);
		btn_merge_avg.setSelection(true);
		chk_reg_line.setSelection(true);
		chk_reg_equation.setSelection(true);
		chk_reg_equation.setEnabled(true);
		chk_max_y_to_100.setEnabled(cb_var_y.getText().endsWith(" Usage"));
	}
	
	public void configure(Chart chart) {
		RegressionChart regression_chart = (RegressionChart)chart;
		if(btn_aggr_byname.getSelection()) {
			regression_chart.setAggregationType(AggregationType.NAME);
		} else if(btn_aggr_bygroup.getSelection()) {
			regression_chart.setAggregationType(AggregationType.GROUP);
		} else if(btn_aggr_allinone.getSelection()) {
			regression_chart.setAggregationType(AggregationType.ALL);
		}
		if(btn_merge_avg.getSelection()) {
			regression_chart.setResourceMergeType(ResourceMergeType.AVG);
		} else if(btn_merge_sum.getSelection()) {
			regression_chart.setResourceMergeType(ResourceMergeType.SUM);
		}
		regression_chart.setVariableX(getVariableX(cb_var_x.getText()));
		regression_chart.setVariableY(getVariableY(cb_var_y.getText()));
		regression_chart.setRegressionType(RegressionType.valueOf(cb_reg_type.getText()));
		regression_chart.setShowRegressionLine(chk_reg_line.getSelection());
		regression_chart.setShowRegressionEquation(chk_reg_equation.getSelection());
		regression_chart.setResourceAxisTo100(chk_max_y_to_100.getSelection());
	}
	
	private VariableX getVariableX(String text) {
		if("Transactions".equals(text)) {
			return VariableX.TX;
		} else if("Request IPs".equals(text)) {
			return VariableX.IP;
		}
		return null;
	}

	private VariableY getVariableY(String text) {
		if("CPU Usage".equals(text)) {
			return VariableY.CPU;
		} else if("Memory Usage".equals(text)) {
			return VariableY.MEMORY;
		} else if("Disk Usage".equals(text)) {
			return VariableY.DISK;
		} else if("Network Usage".equals(text)) {
			return VariableY.NETWORK;
		} else if("Average Response-Time".equals(text)) {
			return VariableY.AVG_RESPONSE;
		} else if("Errors".equals(text)) {
			return VariableY.ERROR;
		}
		return null;
	}
	
}
