package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class ExtChart extends KeyValueChart {

	protected static Class<?> dataClass = KeyEntryVO.class;
	
	public ExtChart() {
		super("Transaction by Extentsion", "Extension", "Transactions");
		setCategoryName("EXT");
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		jfreeChart.removeLegend();
	}	
	
}
