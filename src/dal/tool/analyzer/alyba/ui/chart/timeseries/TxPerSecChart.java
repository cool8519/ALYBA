package dal.tool.analyzer.alyba.ui.chart.timeseries;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.TPSEntryVO;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;

public class TxPerSecChart extends TimeSeriesChart {

	protected static final Class<?> DATA_CLASS = TPSEntryVO.class;
	
	public TxPerSecChart() {
		super("Transaction Per Second(s)", "Time", "Transactions");
	    setUnitString("second(s)");
	    setDateFormat("yyyy.MM.dd HH:mm:ss");
	    setMovingAverageCount(60);
	    if(ResultAnalyzer.hasMappingInfo("IP")) {
	    	setShowSecondaryAxis(true);
	    	setLabelYSecondary("Number of IP-Address");
	    }
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
	    TimeSeries ts_total = new TimeSeries("Total");
	    TimeSeries ts_success = new TimeSeries("Success");
	    TimeSeries ts_error = new TimeSeries("Error");
	    TimeSeries ts2 = new TimeSeries("Number of IP-Address");
	    boolean exist_error = false;
	    if(merge_item) {
	    	int count = 0;
	    	TPSEntryVO mergedVO = null;
	    	for(Object data : dataList) {
	    		TPSEntryVO vo = (TPSEntryVO) data;
	    		if(merge_item_count > count++) {
	    			if(mergedVO == null) {
	    				mergedVO = vo;
	    			} else {
	    				mergedVO = mergedVO.merge(vo);
	    			}
	    		}
		    	if(merge_item_count == count) {
			    	exist_error = exist_error==true ? true : mergedVO.getErrorCount()>0;
			    	ts_total.add(new Second(mergedVO.getUnitDate()), mergedVO.getRequestCount());
			    	ts_success.add(new Second(mergedVO.getUnitDate()), mergedVO.getRequestCount()-mergedVO.getErrorCount());
			    	ts_error.add(new Second(mergedVO.getUnitDate()), mergedVO.getErrorCount());
	    			ts2.add(new Second(mergedVO.getUnitDate()), mergedVO.getRequestIPCount());	    	
	    			count = 0;
	    			mergedVO = null;
	    		}
	    	}
	    } else {
	    	for(Object data : dataList) {
	    		TPSEntryVO vo = (TPSEntryVO) data;
		    	exist_error = exist_error==true ? true : vo.getErrorCount()>0;
		    	ts_total.add(new Second(vo.getUnitDate()), vo.getRequestCount());
		    	ts_success.add(new Second(vo.getUnitDate()), vo.getRequestCount()-vo.getErrorCount());
		    	ts_error.add(new Second(vo.getUnitDate()), vo.getErrorCount());
	    		ts2.add(new Second(vo.getUnitDate()), vo.getRequestIPCount());
	    	}
	    }
	    
	    TimeSeriesCollection ts_collection = new TimeSeriesCollection();
	    ts_collection.addSeries(ts_total);
	    if(exist_error) {
	    	ts_collection.addSeries(ts_success);
	    	ts_collection.addSeries(ts_error);
	    }
	    dataset = ts_collection;
	    if(show_secondary_axis) {
		    TimeSeriesCollection ts2_collection = new TimeSeriesCollection();
		    ts2_collection.addSeries(ts2);
		    dataset2 = ts2_collection;
	    }	    
	}
	
	public void afterCreateChart(JFreeChart jfreeChart) {
		super.afterCreateChart(jfreeChart);
		XYPlot plot = (XYPlot)jfreeChart.getPlot();
		plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(0);
		renderer.setSeriesPaint(0, new Color(85, 255, 85));
		if(dataset.getSeriesCount() > 1) {
			renderer.setSeriesPaint(1, new Color(85, 85, 255));
			renderer.setSeriesPaint(2, new Color(255, 85, 85));
		}
		if(show_secondary_axis) {
			renderer = (XYLineAndShapeRenderer)plot.getRenderer(1);
			renderer.setSeriesPaint(0, Color.DARK_GRAY);
		}
	}

}
