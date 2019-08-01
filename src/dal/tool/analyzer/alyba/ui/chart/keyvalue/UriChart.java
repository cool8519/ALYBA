package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class UriChart extends KeyValueChart {

	public UriChart() {
		super(Type.VerticalBar, "Transaction by URI", "URI", "Transactions");
		setCategoryName("URI");
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		super.afterCreateChart(jfreeChart);
		jfreeChart.removeLegend();
	}	
	
}
