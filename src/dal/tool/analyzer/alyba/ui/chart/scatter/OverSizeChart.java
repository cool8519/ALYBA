package dal.tool.analyzer.alyba.ui.chart.scatter;

import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.BadTransactionEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.ui.chart.DistributionChart;
import dal.tool.analyzer.alyba.ui.chart.extension.TransactionXYDataItem;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;

public class OverSizeChart extends DistributionChart {

	protected static final Class<?> DATA_CLASS = BadTransactionEntryVO.class;

	public OverSizeChart() {
		super(Type.ScatterPlot, "Over-Size Transaction", "Response Size (KB)", "Transactions", "Time", "Response Size (KB)");
		setUseDateAxis(true);
	    setDateFormat("yyyy.MM.dd HH:mm");
	    setBoundaryValues(getDefaultBoundaryValues());
	}

	public static boolean showChart() {
		return ResultAnalyzer.hasMappingInfo("BYTES");
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
		if(chartType == Type.ScatterPlot) {
	    	XYSeries xy = new XYSeries("Transaction");
	    	if(show_regression_line) {
	    		regression = new SimpleRegression();
	    	}    	
	    	for(EntryVO data : dataList) {
	    		BadTransactionEntryVO vo = (BadTransactionEntryVO) data;
	    		long x = new Second(vo.getDate()).getMiddleMillisecond();
	    		long y = vo.getResponseBytes() / 1024;
				xy.add(new TransactionXYDataItem(x, y, vo));
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
		return ((TransactionXYDataItem)item).getTransaction().toPrettyString();
	}

	public Double[] getDefaultBoundaryValues() {
		return new Double[] { 100000D, 500000D, 1000000D, 5000000D };
	}
	
	public double getDistributionValue(EntryVO vo) {
		return ((BadTransactionEntryVO)vo).getResponseBytes();
	}

}
