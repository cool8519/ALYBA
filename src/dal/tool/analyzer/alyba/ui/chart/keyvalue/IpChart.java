package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class IpChart extends KeyValueChart {

	protected static Class<?> dataClass = KeyEntryVO.class;
	
	public IpChart() {
		super("Transaction by IP-Address", "IP-Address", "Transactions");
		setCategoryName("IP");
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
	    jfreeChart.removeLegend();
	}
	
}
