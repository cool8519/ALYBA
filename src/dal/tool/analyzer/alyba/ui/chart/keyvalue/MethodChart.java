package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class MethodChart extends KeyValueChart {

	protected static Class<?> dataClass = KeyEntryVO.class;
	
	public MethodChart() {
		super("Transaction by HTTP Method", "HTTP Method", "Transactions");
		setCategoryName("METHOD");
		setMergeToOthers(false);
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		if(chartType != Type.Pie) {
			jfreeChart.removeLegend();
		}
	}	

}
