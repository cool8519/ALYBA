package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
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
		long sum_value = 0L;
		int sum_count = 0;
		int item_count = 0;
		int req_total = 0;
		String last_other_name = null;
		Map<String,Long> loc_map = new HashMap<String,Long>();
		
		if(chartType == Type.Pie) {
			pieDataset = new DefaultPieDataset();
		} else {
			categoryDataset = new DefaultCategoryDataset();
		}		
		for(int i = 0; i < total_item_count; i++) {
			KeyEntryVO vo = (KeyEntryVO)dataList.get(i);
			req_total = vo.getTotal();
			Long acc_value = loc_map.get(vo.getDescription());
			loc_map.put(vo.getDescription(), (acc_value==null)?(long)vo.getRequestCount():acc_value+vo.getRequestCount());
		}
		loc_map = sortByValue(loc_map);
		for(String key : loc_map.keySet()) {
			Long value = loc_map.get(key);
			float ratio = (float)(value / Double.valueOf(req_total)) * 100;
			if(merge_to_others && (ratio < min_item_percent || item_count >= max_item_count)) {
				last_other_name = key;
				sum_value += value;
				sum_count++;
			} else {
				if(chartType == Type.Pie) {
					pieDataset.setValue(key, value);
				} else {
					categoryDataset.addValue(value, category_name, key);
				}
				item_count++;
			}
		}
		if(sum_count > 0) {
			String other_key = (sum_count==1) ? last_other_name : ("Others["+sum_count+"]");   
			if(chartType == Type.Pie) {
				pieDataset.setValue(other_key, sum_value);
			} else {
				categoryDataset.addValue(sum_value, category_name, other_key);
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
			jfreeChart.removeLegend();
		}
	}
	
}
