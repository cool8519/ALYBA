package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class CodeChart extends KeyValueChart {

	public CodeChart() {
		super(Type.Pie, "Transaction by Response Code", "Response Code", "Transactions");
		setCategoryName("CODE");
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
