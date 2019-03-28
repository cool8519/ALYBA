package dal.tool.analyzer.alyba.ui.chart.scatter;

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.BadResponseEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.ui.chart.ScatterPlotChart;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;

public class OverTimeChart extends ScatterPlotChart {

	protected static Class<?> dataClass = BadResponseEntryVO.class;

	public OverTimeChart() {
		super("Over-Time Transaction", "Time", "Response Time (ms)");
		setUseDateAxis(true);
	    setDateFormat("yyyy.MM.dd HH:mm");
	}

	public static boolean showChart() {
		return ResultAnalyzer.hasMappingInfo("ELAPSED");
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
    	XYSeries xy = new XYSeries("Transaction");
    	if(show_regression_linear) {
    		regression = new SimpleRegression();
    	}
    	for(Object data : dataList) {
    		BadResponseEntryVO vo = (BadResponseEntryVO) data;
    		long x = new Second(vo.getResponseDate()).getMiddleMillisecond();
    		long y = vo.getResponseTime();
			xy.add(x, y);
	    	if(show_regression_linear) {
	    		regression.addData(x, y);
	    	}
    	}
    	XYSeriesCollection xy_collection = new XYSeriesCollection();
        xy_collection.addSeries(xy);
	    dataset = xy_collection;	    
	}
	
	public void afterCreateChart(JFreeChart jfreeChart) {
		if(!show_regression_linear) {
			jfreeChart.removeLegend();
		}
	}

}
