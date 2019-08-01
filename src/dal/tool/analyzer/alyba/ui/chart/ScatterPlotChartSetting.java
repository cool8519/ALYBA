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
import org.eclipse.swt.widgets.Scale;

import dal.tool.analyzer.alyba.ui.comp.ResultChart;

public class ScatterPlotChartSetting extends ChartSetting {

	private Group grp_setting;
	private Scale scale_shape_size;
	private Label lb_shape_size;	
	private Button chk_reg_linear;
	private Button chk_reg_equation;
	private Button btn_apply;

	public ScatterPlotChartSetting(Composite parent, ResultChart result_chart) {
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

		lb_shape_size = new Label(grp_setting, SWT.CENTER);
		lb_shape_size.setLayoutData(new FormData());
		lb_shape_size.setBounds(20, 20, 170, 16);
		lb_shape_size.setText(" Size of shape");

		FormData fd_scale_shape_size = new FormData();
		fd_scale_shape_size.left = new FormAttachment(lb_shape_size, 0, SWT.LEFT);
		fd_scale_shape_size.top = new FormAttachment(lb_shape_size, 5);
		fd_scale_shape_size.width = 150;
		scale_shape_size = new Scale(grp_setting, SWT.NONE);
		scale_shape_size.setMinimum(1);
		scale_shape_size.setMaximum(5);
		scale_shape_size.setSelection(3);
		scale_shape_size.setIncrement(1);
		scale_shape_size.setPageIncrement(1);
		scale_shape_size.setLayoutData(fd_scale_shape_size);

		FormData fd_chk_reg_linear = new FormData();
		fd_chk_reg_linear.top = new FormAttachment(scale_shape_size, 30);
		fd_chk_reg_linear.left = new FormAttachment(lb_shape_size, 0, SWT.LEFT);
		chk_reg_linear = new Button(grp_setting, SWT.CHECK);
		chk_reg_linear.setLayoutData(fd_chk_reg_linear);
		chk_reg_linear.setText(" Linear Regression");

		FormData fd_chk_reg_equation = new FormData();
		fd_chk_reg_equation.top = new FormAttachment(chk_reg_linear, 10);
		fd_chk_reg_equation.left = new FormAttachment(chk_reg_linear, 20, SWT.LEFT);
		chk_reg_equation = new Button(grp_setting, SWT.CHECK);
		chk_reg_equation.setLayoutData(fd_chk_reg_equation);
		chk_reg_equation.setText(" Regression Equation");
		chk_reg_equation.setEnabled(false);

		FormData fd_btn_apply = new FormData();
		fd_btn_apply.right = new FormAttachment(100);
		fd_btn_apply.bottom = new FormAttachment(100);
		fd_btn_apply.width = 80;
		btn_apply = new Button(grp_setting, SWT.NONE);
		btn_apply.setText("Apply");
		btn_apply.setLayoutData(fd_btn_apply);
		
		grp_setting.setTabList(new Control[]{ scale_shape_size, chk_reg_linear, chk_reg_equation, btn_apply });

	}

	protected void addEventListener() {

		btn_apply.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		result_chart.clickApplyButton();
	    	}
	    });

		chk_reg_linear.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
				chk_reg_equation.setEnabled(chk_reg_linear.getSelection());
	    	}
	    });

	}
	
	public void reset(Chart chart) {
		if(chart != null) {
			if(chart instanceof ScatterPlotChart) {
				ScatterPlotChart spChart = (ScatterPlotChart)chart;
				scale_shape_size.setSelection(spChart.getShapeSize());
				chk_reg_linear.setSelection(spChart.getShowLinearRegression());
				chk_reg_equation.setSelection(spChart.getShowRegressionEquation());
				chk_reg_equation.setEnabled(chk_reg_linear.getSelection());
			} else if(chart instanceof DistributionChart) {
				DistributionChart distChart = (DistributionChart)chart;
				scale_shape_size.setSelection(distChart.getShapeSize());
				chk_reg_linear.setSelection(distChart.getShowLinearRegression());
				chk_reg_equation.setSelection(distChart.getShowRegressionEquation());
				chk_reg_equation.setEnabled(chk_reg_linear.getSelection());
			}
		} else {
			scale_shape_size.setSelection(3);
			chk_reg_linear.setSelection(false);
			chk_reg_equation.setSelection(false);
			chk_reg_equation.setEnabled(false);
		}
	}
	
	public void configure(Chart chart) {
		if(chart instanceof DistributionChart) {
			DistributionChart dist_chart = (DistributionChart)chart;
			dist_chart.setShapeSize(scale_shape_size.getSelection());
			dist_chart.setShowLinearRegression(chk_reg_linear.getSelection());
			dist_chart.setShowRegressionEquation(chk_reg_equation.getSelection());
		} else {
			ScatterPlotChart sp_chart = (ScatterPlotChart)chart;
			sp_chart.setShapeSize(scale_shape_size.getSelection());
			sp_chart.setShowLinearRegression(chk_reg_linear.getSelection());
			sp_chart.setShowRegressionEquation(chk_reg_equation.getSelection());
		}
	}
}
