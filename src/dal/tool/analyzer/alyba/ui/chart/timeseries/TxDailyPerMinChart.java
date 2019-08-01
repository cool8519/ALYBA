package dal.tool.analyzer.alyba.ui.chart.timeseries;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.util.DateUtil;

public class TxDailyPerMinChart extends TimeSeriesChart {

	protected static final Class<?> DATA_CLASS = TPMEntryVO.class;
	
	public TxDailyPerMinChart() {
		super("Daily Transaction Per Minute(s)", "Time of Day", "Transactions");
	    setUnitString("minute(s)");
	    setDateFormat("HH:mm");
	    setMovingAverageCount(30);
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
		TimeSeries ts = null;
	    TimeSeriesCollection ts_collection = new TimeSeriesCollection();
	    String str_date_prev = null;
	    if(merge_item) {
	    	int count = 0;
	    	TPMEntryVO mergedVO = null;
	    	for(Object data : dataList) {
		    	TPMEntryVO vo = (TPMEntryVO) data;
		    	Date dt = vo.getUnitDate();
		    	String str_date = DateUtil.dateToString(dt, DateUtil.SDF_DATE);
		    	if(str_date_prev == null || !str_date_prev.equals(str_date)) {
		    		if(str_date_prev != null) {
		    			ts_collection.addSeries(ts);
		    		}
		    		ts = new TimeSeries(str_date);
		    		str_date_prev = str_date;
	    			count = 0;
	    			mergedVO = vo;
		    	} else {
		    		if(merge_item_count > count++) {
		    			if(mergedVO == null) {
		    				mergedVO = vo;
		    			} else {
		    				mergedVO = mergedVO.merge(vo);
		    			}
		    		}
			    	if(merge_item_count == count) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(mergedVO.getUnitDate());
						cal.set(Calendar.YEAR, 2000);
						cal.set(Calendar.MONTH, 0);
						cal.set(Calendar.DATE, 1);
				    	ts.add(new Minute(cal.getTime()), mergedVO.getRequestCount());
		    			count = 0;
		    			mergedVO = null;
		    		}
		    	}
	    	}
	    } else {	    
		    for(Object data : dataList) {
		    	TPMEntryVO vo = (TPMEntryVO) data;
		    	Date dt = vo.getUnitDate();
		    	String str_date = DateUtil.dateToString(dt, DateUtil.SDF_DATE);
		    	if(str_date_prev == null || !str_date_prev.equals(str_date)) {
		    		if(str_date_prev != null) {
		    			ts_collection.addSeries(ts);
		    		}
		    		ts = new TimeSeries(str_date);
		    		str_date_prev = str_date;
		    	}		    	
				Calendar cal = Calendar.getInstance();
				cal.setTime(dt);
				cal.set(Calendar.YEAR, 2000);
				cal.set(Calendar.MONTH, 0);
				cal.set(Calendar.DATE, 1);
		    	ts.add(new Minute(cal.getTime()), vo.getRequestCount());
		    }
	    }
	    
	    ts_collection.addSeries(ts);	    
	    dataset = ts_collection;
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		super.afterCreateChart(jfreeChart);
	    XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
	    xyPlot.getRenderer().setBaseToolTipGenerator(new StandardXYToolTipGenerator("\"{0} {1}\": {2}", new SimpleDateFormat("HH:mm:ss"), NumberFormat.getInstance()));
	}

}
