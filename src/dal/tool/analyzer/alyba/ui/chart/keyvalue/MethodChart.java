package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class MethodChart extends KeyValueChart {

	public MethodChart() {
		super(Type.Pie, "Transaction by HTTP Method", "HTTP Method", "Transactions");
		setCategoryName("METHOD");
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
