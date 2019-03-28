package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class CodeChart extends KeyValueChart {

	protected static Class<?> dataClass = KeyEntryVO.class;
	
	public CodeChart() {
		super("Transaction by Response Code", "Response Code", "Transactions");
		setCategoryName("CODE");
		setMergeToOthers(false);
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		if(chartType != Type.Pie) {
			jfreeChart.removeLegend();
		}
	}	

}
