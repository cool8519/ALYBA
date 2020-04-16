package dal.tool.analyzer.alyba.ui.chart.setting;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import dal.tool.analyzer.alyba.ui.chart.Chart;
import dal.tool.analyzer.alyba.ui.comp.ResultChart;

public abstract class ChartSetting extends Composite {
	
	protected ResultChart result_chart;

	public ChartSetting(Composite parent, ResultChart result_chart) {
		super(parent, SWT.NONE);
		this.result_chart = result_chart;
	}

	public abstract void init();
	public abstract void reset(Chart chart);
	public abstract void configure(Chart chart);
	
}
