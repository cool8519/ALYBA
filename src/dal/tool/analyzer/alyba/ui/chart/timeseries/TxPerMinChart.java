package dal.tool.analyzer.alyba.ui.chart.timeseries;

import java.util.List;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;

public class TxPerMinChart extends TimeSeriesChart {

	protected static final Class<?> DATA_CLASS = TPMEntryVO.class;
	
	public TxPerMinChart() {
		super("Transaction Per Minute(s)", "Time", "Transactions");
	    setUnitString("minute(s)");
	    setDateFormat("yyyy.MM.dd HH:mm");
	    setMovingAverageCount(30);
	    if(ResultAnalyzer.hasMappingInfo("IP")) {
	    	setShowSecondaryAxis(true);
	    	setLabelYSecondary("Number of IP-Address");
	    }
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
	    TimeSeries ts = new TimeSeries("TPM");
	    TimeSeries ts2 = new TimeSeries("Number of IP-Address");
	    if(merge_item) {
	    	int count = 0;
	    	TPMEntryVO mergedVO = null;
	    	for(Object data : dataList) {
		    	TPMEntryVO vo = (TPMEntryVO) data;
	    		if(merge_item_count > count++) {
	    			if(mergedVO == null) {
	    				mergedVO = vo;
	    			} else {
	    				mergedVO = mergedVO.merge(vo);
	    			}
	    		}
		    	if(merge_item_count == count) {
	    			ts.add(new Minute(mergedVO.getUnitDate()), mergedVO.getRequestCount());	    	
	    			ts2.add(new Minute(mergedVO.getUnitDate()), mergedVO.getRequestIPCount());	    	
	    			count = 0;
	    			mergedVO = null;
	    		}
	    	}
	    } else {	    
		    for(Object data : dataList) {
		    	TPMEntryVO vo = (TPMEntryVO) data;
		    	ts.add(new Minute(vo.getUnitDate()), vo.getRequestCount());	    	
	    		ts2.add(new Minute(vo.getUnitDate()), vo.getRequestIPCount());
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
