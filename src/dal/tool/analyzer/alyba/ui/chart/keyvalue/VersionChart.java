package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class VersionChart extends KeyValueChart {

	public VersionChart() {
		super(Type.Pie, "Transaction by HTTP Version", "HTTP Version", "Transactions");
		setCategoryName("VERSION");
		setMergeToOthers(false);
	}

	public Type getDefaultChartType() {
		return Type.Pie;
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		super.afterCreateChart(jfreeChart);
		if(chartType != Type.Pie) {
			jfreeChart.removeLegend();
		}
	}	
	
}
