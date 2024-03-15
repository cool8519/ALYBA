package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class IpChart extends KeyValueChart {

	protected boolean show_by_location = true;

	public IpChart() {
		super(Type.VerticalBar, "Transaction by IP-Address", "IP-Address", "Transactions");
		setCategoryName("IP");
	}

	public Type getDefaultChartType() {
		return Type.HorizontalBar;
	}

	public boolean getShowByLocation() {
		return show_by_location;
	}
	
	public void setShowByLocation(boolean flag) {
		this.show_by_location = flag;
	}
	
	protected <E extends EntryVO> void createDataset(List<E> dataList) {
		if(!show_by_location) {
			super.createDataset(dataList);
		} else {
			createDatasetByLocation(dataList);
		}
	}
	
	private <E extends EntryVO> void createDatasetByLocation(List<E> dataList) {
		int total_item_count = dataList.size();
		long other_total = 0L;
		long other_error = 0L;
		int other_count = 0;
		int item_count = 0;
		long req_total = 0;
		String last_other_name = null;
		Map<String,Long> loc_total_map = new HashMap<String,Long>();
		Map<String,Long> loc_error_map = new HashMap<String,Long>();
		
		if(chartType == Type.Pie) {
			pieDataset = new DefaultPieDataset();
		} else {
			categoryDataset = new DefaultCategoryDataset();
		}		
		for(int i = 0; i < total_item_count; i++) {
			KeyEntryVO vo = (KeyEntryVO)dataList.get(i);
			req_total = vo.getTotal();
			Long acc_total_value = loc_total_map.get(vo.getDescription());
			Long acc_error_value = loc_error_map.get(vo.getDescription());
			loc_total_map.put(vo.getDescription(), (acc_total_value==null)?(long)vo.getRequestCount():acc_total_value+vo.getRequestCount());
			loc_error_map.put(vo.getDescription(), (acc_error_value==null)?(long)vo.getErrorCount():acc_error_value+vo.getErrorCount());
		}
		loc_total_map = sortByValue(loc_total_map);
		for(String key : loc_total_map.keySet()) {
			Long total = loc_total_map.get(key);
			Long error = loc_error_map.get(key);
			float ratio = (float)(total / Double.valueOf(req_total)) * 100;
			if(merge_to_others && (ratio < min_item_percent || item_count >= max_item_count)) {
				last_other_name = key;
				other_total += total;
				other_error += error;
				other_count++;
			} else {
				if(chartType == Type.Pie) {
					pieDataset.setValue(key, total);
				} else {
					long success = total - error;
					if(success > 0) {
						categoryDataset.addValue(success, "Success", key);
					}
					if(error > 0) {
						categoryDataset.addValue(error, "Error", key);
					}
				}
				item_count++;
			}
		}
		if(other_count > 0) {
			String other_key = (other_count==1) ? last_other_name : ("Others["+other_count+"]");   
			if(chartType == Type.Pie) {
				pieDataset.setValue(other_key, other_total);
			} else {
				if(other_total-other_error > 0) {
					categoryDataset.addValue(other_total-other_error, "Success", other_key);
				}
				if(other_error > 0) {
					categoryDataset.addValue(other_error, "Error", other_key);
				}
			}
		}
	}

    private Map<String,Long> sortByValue(Map<String,Long> oldMap) { 
        List<Map.Entry<String,Long>> list = new LinkedList<Map.Entry<String,Long>>(oldMap.entrySet()); 
        Collections.sort(list, new Comparator<Map.Entry<String,Long>>() { 
            public int compare(Map.Entry<String,Long> o1, Map.Entry<String,Long> o2) { 
                return (o2.getValue()).compareTo(o1.getValue()); 
            } 
        });
        Map<String,Long> newMap = new LinkedHashMap<String,Long>(); 
        for(Map.Entry<String,Long> ent : list) { 
        	newMap.put(ent.getKey(), ent.getValue()); 
        }
        return newMap; 
    } 

	public void afterCreateChart(JFreeChart jfreeChart) {
		super.afterCreateChart(jfreeChart);
		if(chartType != Type.Pie) {
			CategoryPlot categoryPlot = (CategoryPlot)jfreeChart.getPlot();
			BarRenderer renderer = (BarRenderer)categoryPlot.getRenderer();
			if(categoryDataset.getRowCount() > 1) {
				renderer.setSeriesPaint(0, new Color(85, 85, 255));
				renderer.setSeriesPaint(1, new Color(255, 85, 85));
			}
		}
	}
	
}
