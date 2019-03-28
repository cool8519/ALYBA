package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class UriChart extends KeyValueChart {

	protected static Class<?> dataClass = KeyEntryVO.class;
	
	public UriChart() {
		super("Transaction by URI", "URI", "Transactions");
		setCategoryName("URI");
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		jfreeChart.removeLegend();
	}	
	
}
