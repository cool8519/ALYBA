package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class UriChart extends KeyValueChart {

	protected boolean merge_methods = true;

	public UriChart() {
		super(Type.VerticalBar, "Transaction by URI", "URI", "Transactions");
		setCategoryName("URI");
	}

	public Type getDefaultChartType() {
		return Type.HorizontalBar;
	}

	public boolean getMergeMethods() {
		return merge_methods;
	}
	
	public void setMergeMethods(boolean flag) {
		this.merge_methods = flag;
	}
	
	protected <E extends EntryVO> void createDataset(List<E> dataList) {
		super.createDataset(merge_methods ? mergeMethods(dataList) : dataList);
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
	
	@SuppressWarnings("unchecked")
	protected <E extends EntryVO> List<E> mergeMethods(List<E> dataList) {
		Map<String,E> result = new HashMap<String,E>();
		for(int i = 0; i < dataList.size(); i++) {
			KeyEntryVO vo = (KeyEntryVO)dataList.get(i);
			String mergeKey = vo.getKey().split("<")[0];
			KeyEntryVO existVo = (KeyEntryVO)result.get(mergeKey);
			if(existVo == null) {
				existVo = vo.copy();
				existVo.setKey(mergeKey);
				result.put(mergeKey, (E)existVo);
			} else {
				existVo = existVo.merge(vo);
			}
		}
		List<E> mergedlist = new ArrayList<E>(result.values());
		mergedlist.sort((Comparator<? super E>) new Comparator<KeyEntryVO>() {
            @Override
            public int compare(KeyEntryVO o1, KeyEntryVO o2) {
                return Integer.compare(o2.getRequestCount(), o1.getRequestCount());
            }
        });
        return mergedlist;
	}

}
