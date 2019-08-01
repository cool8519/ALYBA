package dal.tool.analyzer.alyba.ui.chart;

import java.awt.Dimension;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.TextAnchor;

import dal.tool.analyzer.alyba.output.vo.EntryVO;

public abstract class Chart {

	public static enum Type { TimeSeries, VerticalBar, HorizontalBar, Pie, ScatterPlot };

	protected static final Class<?> DATA_CLASS = null;

	protected Type chartType;
	protected ChartPanel chartPanel;
	protected JFreeChart jfreeChart;
	protected String title = "Untitled";

	public Chart(Type chartType) {
		this.chartType = chartType;
	}

	public Chart(Type chartType, String title) {
		this.chartType = chartType;
		setTitle(title);
	}
	
	public static boolean showChart() {
		return true;
	}
	
	public static Class<?> getDataClass() {
		return DATA_CLASS;
	}
	
	public ChartPanel getChartPanel() {
		if(chartPanel == null) {
			chartPanel = new ChartPanel(jfreeChart);
			chartPanel.setMaximumSize(new Dimension(2048, 1080));
			chartPanel.setMouseWheelEnabled(true);
			chartPanel.setMouseZoomable(true);
			afterCreateChartPanel(chartPanel);
		}
		return chartPanel;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public Type getType() {
		return chartType;
	}
	
	public void setType(Type chartType) throws Exception {
		if(!checkChartType(chartType)) {
			throw new Exception("Not allowed type of chart");
		}
		this.chartType = chartType;
	}

	public <E extends EntryVO> void setData(List<E> dataList) throws Exception {
		if(!checkDataset(dataList)) {
			throw new Exception("Invalid type of data");
		}
		createDataset(beforeCreateDataset(dataList));
		afterCreateDataset();
	}

	protected boolean checkDataset(List<?> dataList) {
		if(dataList == null || dataList.size() < 1) {
			return false;
		}
		if(getDataClass() == null || dataList.get(0).getClass() == getDataClass()) {
			return true;
		} else {
			return false;
		}
	}
	
	public final <E extends EntryVO> void draw(List<E> dataList) throws Exception {
		setData(dataList);
		createChart();	
		afterCreateChart(jfreeChart);
	}

	public <E extends EntryVO> List<E> beforeCreateDataset(List<E> dataList) {
		return dataList;
	}

	public void afterCreateDataset() {
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
	}
	
	public void afterCreateChartPanel(ChartPanel chartPanel) {
	}

	public boolean checkChartType(Type chartType) {
		if(getSupportChartTypes() == null) {
			return false;
		}
		for(Type t : getSupportChartTypes()) {
			if(t == chartType) {
				return true;
			}
		}
		return false;
	}
	
	public abstract Type[] getSupportChartTypes();
	public abstract Type getDefaultChartType();	
	protected abstract <E extends EntryVO> void createDataset(List<E> dataList);
	public abstract void createChart();
	
	protected TextAnchor getTextAnchorByAngle(int angle) {
		if(angle >= 0 && angle < 30) {
		    return TextAnchor.CENTER_LEFT;
		} else if(angle >= 30 && angle < 60) {
			return TextAnchor.TOP_LEFT;
		} else if(angle >= 60 && angle < 120) {
			return TextAnchor.TOP_CENTER;
		} else if(angle >= 120 && angle < 150) {
			return TextAnchor.TOP_RIGHT;
		} else if(angle >= 150 && angle < 210) {
			return TextAnchor.CENTER_RIGHT;
		} else if(angle >= 210 && angle < 240) {
			return TextAnchor.BOTTOM_RIGHT;
		} else if(angle >= 240 && angle < 300) {
			return TextAnchor.BOTTOM_CENTER;
		} else if(angle >= 300 && angle < 330) {
			return TextAnchor.BOTTOM_LEFT;
		} else if(angle >= 330 && angle < 360) {
			return TextAnchor.CENTER_LEFT;
		} else {
			return TextAnchor.TOP_LEFT;
		}
	}
	
}
