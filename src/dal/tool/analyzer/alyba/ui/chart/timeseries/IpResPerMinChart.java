package dal.tool.analyzer.alyba.ui.chart.timeseries;

import java.text.DecimalFormat;
import java.util.List;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;

public class IpResPerMinChart extends TimeSeriesChart {

	protected static final DecimalFormat DF_NUMBER = new DecimalFormat("0");

	protected static final Class<?> DATA_CLASS = TPMEntryVO.class;

	public IpResPerMinChart() {
		super("IP-Address & ResponseTime Per Minute(s)", "Time", "Number of IP-Address");
	    setDateFormat("yyyy.MM.dd HH:mm");
	    setUnitString("minute(s)");
	    setMovingAverageCount(30);
    	setShowSecondaryAxis(true);
    	setLabelYSecondary("Average Response Time (ms)");
	}

	public static boolean showChart() {
		return ResultAnalyzer.hasMappingInfo("ELAPSED");
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
	    TimeSeries ts = new TimeSeries("Number of IP-Address");
	    TimeSeries ts2 = new TimeSeries("Avg.ResponseTime");
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
	    			ts.add(new Minute(mergedVO.getUnitDate()), mergedVO.getRequestIPCount());	    	
			    	ts2.add(new Minute(mergedVO.getUnitDate()), mergedVO.getAverageResponseTime());
	    			count = 0;
	    			mergedVO = null;
	    		}
	    	}
	    } else {	    
	    	for(Object data : dataList) {
	    		TPMEntryVO vo = (TPMEntryVO) data;
	    		ts.add(new Minute(vo.getUnitDate()), vo.getRequestIPCount());
	    		ts2.add(new Minute(vo.getUnitDate()), vo.getAverageResponseTime());
	    	}
	    }
	    
	    TimeSeriesCollection ts_collection = new TimeSeriesCollection();
	    ts_collection.addSeries(ts);	    
	    dataset = ts_collection;
	    TimeSeriesCollection ts2_collection = new TimeSeriesCollection();
	    ts2_collection.addSeries(ts2);
	    dataset2 = ts2_collection;
	}

}
