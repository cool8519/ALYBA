package dal.tool.analyzer.alyba.ui.chart.keyvalue;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;

public class CodeChart extends KeyValueChart {

	public CodeChart() {
		super(Type.Pie, "Transaction by Response Code", "Response Code", "Transactions");
		setCategoryName("CODE");
		setMergeToOthers(false);
	}

	public Type getDefaultChartType() {
		return Type.Pie;
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
