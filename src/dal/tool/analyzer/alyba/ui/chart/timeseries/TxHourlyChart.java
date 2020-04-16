package dal.tool.analyzer.alyba.ui.chart.timeseries;

import java.text.SimpleDateFormat;
import java.util.List;

import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.TimeAggregationEntryVO;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;

public class TxHourlyChart extends TimeSeriesChart {

	protected static final Class<?> DATA_CLASS = TimeAggregationEntryVO.class;
	
	public TxHourlyChart() {
		super("Hourly Transaction", "Time of Day", "Transactions");
	    setDateFormat("HH:mm");
	    setUnitString("hour(s)");
	    setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1, new SimpleDateFormat("HH:mm ~ ")));
	    setMovingAverageCount(3);
	    setShowShape(true);
	    if(ResultAnalyzer.hasMappingInfo("IP")) {
	    	setShowSecondaryAxis(true);
	    	setLabelYSecondary("Number of IP-Address");
	    }
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
	    TimeSeries ts = new TimeSeries("COUNT");
	    TimeSeries ts2 = new TimeSeries("Number of IP-Address");
	    if(merge_item) {
	    	int count = 0;
	    	TimeAggregationEntryVO mergedVO = null;
	    	for(Object data : dataList) {
	    		TimeAggregationEntryVO vo = (TimeAggregationEntryVO) data;
	    		if(merge_item_count > count++) {
	    			if(mergedVO == null) {
	    				mergedVO = vo;
	    			} else {
	    				mergedVO = mergedVO.merge(vo);
	    			}
	    		}
		    	if(merge_item_count == count) {
	    			ts.add(new Hour(mergedVO.getUnitDate()), mergedVO.getRequestCount());	    	
	    			ts2.add(new Hour(mergedVO.getUnitDate()), mergedVO.getRequestIPCount());	    	
	    			count = 0;
	    			mergedVO = null;
	    		}
	    	}
	    } else {	    
		    for(Object data : dataList) {
		    	TimeAggregationEntryVO vo = (TimeAggregationEntryVO) data;
		    	ts.add(new Hour(vo.getUnitDate()), vo.getRequestCount());	    	
	    		ts2.add(new Hour(vo.getUnitDate()), vo.getRequestIPCount());
		    }
	    }

	    TimeSeriesCollection ts_collection = new TimeSeriesCollection();
	    ts_collection.addSeries(ts);	    
	    dataset = ts_collection;
	    if(show_secondary_axis) {
		    TimeSeriesCollection ts2_collection = new TimeSeriesCollection();
		    ts2_collection.addSeries(ts2);
		    dataset2 = ts2_collection;
	    }	    
	}	

}
