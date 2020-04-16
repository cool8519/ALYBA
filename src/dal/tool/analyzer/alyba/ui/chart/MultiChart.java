package dal.tool.analyzer.alyba.ui.chart;

import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import dal.tool.analyzer.alyba.output.vo.EntryVO;

public abstract class MultiChart extends Chart {

	protected Chart[] charts;
	
	public MultiChart() {
		this(1);
	}

	public MultiChart(int count) {
		this.charts = new Chart[count];
	}

	public MultiChart(String title, int count) {
		this.title = title;
		this.charts = new Chart[count];
	}

	public int getChartCount() {
		return charts.length;
	}

	public void setChart(int i, Chart chart) {
		this.charts[i] = chart; 
	}
	
	public Chart getChart(int i) {
		return this.charts[i];
	}

	public ChartPanel getChartPanel(int i) {
		return charts[i].getChartPanel();
	}

	public <E extends EntryVO> List<E> beforeCreateDataset(List<E> dataList) {
		for(Chart chart : charts) {
			chart.beforeCreateDataset(dataList);
		}
		return dataList;
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
		for(Chart chart : charts) {
			chart.createDataset(dataList);
		}
	}

	public void afterCreateDataset() {
		for(Chart chart : charts) {
			chart.afterCreateDataset();
		}
	}

	public void createChart() {
		for(Chart chart : charts) {
			chart.createChart();
		}
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
		for(Chart chart : charts) {
			chart.afterCreateChart(chart.getJFreeChart());
		}
	}
	
	public void afterCreateChartPanel(ChartPanel chartPanel) {
		for(Chart chart : charts) {
			chart.afterCreateChartPanel(chart.getChartPanel());
		}
	}
	
	public Type[] getSupportChartTypes() {
		return null;
	}

	public Type getDefaultChartType() {
		return null;
	}

	public abstract void initCharts();	

}
