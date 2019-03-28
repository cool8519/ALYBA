package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class VersionChart extends KeyValueChart {

	protected static Class<?> dataClass = KeyEntryVO.class;
	
	public VersionChart() {
		super("Transaction by HTTP Version", "HTTP Version", "Transactions");
		setCategoryName("VERSION");
		setMergeToOthers(false);
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		if(chartType != Type.Pie) {
			jfreeChart.removeLegend();
		}
	}	
	
}
