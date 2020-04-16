package dal.tool.analyzer.alyba.ui.chart.scatter;

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.BadResponseEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.ui.chart.DistributionChart;
import dal.tool.analyzer.alyba.ui.chart.extension.ResponseXYDataItem;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;

public class OverTimeChart extends DistributionChart {

	protected static final Class<?> DATA_CLASS = BadResponseEntryVO.class;
	
	public OverTimeChart() {
		super(Type.ScatterPlot, "Over-Time Transaction", "Response Time (ms)", "Transactions", "Time", "Response Time (ms)");
		setUseDateAxis(true);
	    setDateFormat("yyyy.MM.dd HH:mm");
	    setBoundaryValues(getDefaultBoundaryValues());
	}

	public static boolean showChart() {
		return ResultAnalyzer.hasMappingInfo("ELAPSED");
	}

	public <E extends EntryVO> void createDataset(List<E> dataList) {
		if(chartType == Type.ScatterPlot) {
	    	XYSeries xy = new XYSeries("Transaction");
	    	if(show_regression_line) {
	    		regression = new SimpleRegression();
	    	}
	    	for(EntryVO data : dataList) {
	    		BadResponseEntryVO vo = (BadResponseEntryVO) data;
	    		long x = new Second(vo.getResponseDate()).getMiddleMillisecond();
	    		long y = vo.getResponseTime();
				xy.add(new ResponseXYDataItem(x, y, vo));
		    	if(show_regression_line) {
		    		regression.addData(x, y);
		    	}
	    	}
	    	XYSeriesCollection xy_collection = new XYSeriesCollection();
	        xy_collection.addSeries(xy);
		    dataset = xy_collection;
		} else {
			createDistributionDataset(dataList);
		}
	}
	
	public String getAnnotationText(XYDataItem item) {
		return ((ResponseXYDataItem)item).getResponse().toPrettyString();
	}

	public Double[] getDefaultBoundaryValues() {
		return new Double[] { 1000D, 3000D, 10000D, 30000D };
	}
	
	public double getDistributionValue(EntryVO vo) {
		return ((BadResponseEntryVO)vo).getResponseTime();
	}

}
