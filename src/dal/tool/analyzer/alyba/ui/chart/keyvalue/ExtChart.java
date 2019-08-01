package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class ExtChart extends KeyValueChart {

	public ExtChart() {
		super(Type.VerticalBar, "Transaction by Extentsion", "Extension", "Transactions");
		setCategoryName("EXT");
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		super.afterCreateChart(jfreeChart);
		jfreeChart.removeLegend();
	}	
	
}
