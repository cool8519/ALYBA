package dal.tool.analyzer.alyba.ui.chart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import dal.tool.analyzer.alyba.ui.comp.ResultChart;

public abstract class ChartSetting extends Composite {
	
	protected ResultChart result_chart;

	public ChartSetting(Composite parent, ResultChart result_chart) {
		super(parent, SWT.NONE);
		this.result_chart = result_chart;
	}

	public void setVisible(boolean visible) {
		if(!getVisible() && visible) {
			reset();
		}
		super.setVisible(visible);
	}

	protected abstract void reset();
	public abstract void configure(Chart chart);
	
}
