package dal.tool.analyzer.alyba.ui.chart.scatter;

import java.awt.Color;
import java.util.List;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
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

	public <E extends EntryVO> void createDataset(List<E> dataList) {
		if(chartType == Type.ScatterPlot) {
			XYSeriesCollection xy_collection = new XYSeriesCollection();
	    	if(show_regression_line) {
	    		regression = new SimpleRegression();
	    	}
	    	XYSeries xy_success = new XYSeries("Success");
	    	XYSeries xy_error = new XYSeries("Error");
	    	for(EntryVO data : dataList) {
	    		BadTransactionEntryVO vo = (BadTransactionEntryVO) data;
	    		long x = new Second(vo.getDate()).getMiddleMillisecond();
	    		long y = vo.getResponseBytes() / 1024;
	    		if(vo.isResponseError()) {
	    			xy_error.add(new TransactionXYDataItem(x, y, vo));
	    		} else {
	    			xy_success.add(new TransactionXYDataItem(x, y, vo));
	    		}
		    	if(show_regression_line) {
		    		regression.addData(x, y);
		    	}
	    	}
	    	if(xy_success.getItemCount() > 0) {
	    		xy_collection.addSeries(xy_success);
	    	}
	    	if(xy_error.getItemCount() > 0) {
	    		xy_collection.addSeries(xy_error);
	    	}
		    dataset = xy_collection;
		} else {
			createDistributionDataset(dataList);
		}
	}
	
	public void afterCreateChart(JFreeChart jfreeChart) {
		if(chartType == Type.ScatterPlot) {
			XYPlot plot = (XYPlot)jfreeChart.getPlot();
			plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(0);
			renderer.setSeriesShape(1, renderer.getSeriesShape(0));
			renderer.setSeriesPaint(0, new Color(85, 85, 255));
			renderer.setSeriesPaint(1, new Color(255, 85, 85));
		} else if(chartType != Type.Pie) {
			CategoryPlot categoryPlot = (CategoryPlot)jfreeChart.getPlot();
			BarRenderer renderer = (BarRenderer)categoryPlot.getRenderer();
			if(categoryDataset.getRowCount() > 1) {
				renderer.setSeriesPaint(0, new Color(85, 85, 255));
				renderer.setSeriesPaint(1, new Color(255, 85, 85));
			}
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
