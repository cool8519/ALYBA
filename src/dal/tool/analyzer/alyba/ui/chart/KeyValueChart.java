package dal.tool.analyzer.alyba.ui.chart;

import java.text.DecimalFormat;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.chart.extension.SuccessErrorCountToolTipGenerator;
import dal.tool.analyzer.alyba.ui.chart.extension.TotalCountItemLabelGenerator;

public abstract class KeyValueChart extends Chart {

	protected static final DecimalFormat DF_NUMBER = new DecimalFormat("0");
	protected static final DecimalFormat DF_PERCENT = new DecimalFormat("0.00%");

	protected static final Class<?> DATA_CLASS = KeyEntryVO.class;

	protected String category_name = "";
	protected String label_name = "Name";
	protected String label_value = "Value";
	protected DefaultCategoryDataset categoryDataset;
	protected DefaultPieDataset pieDataset;
	protected boolean show_item_label = true;
	protected boolean merge_to_others = true;
	protected int max_item_count = 20;	
	protected float min_item_percent = 1.0F;
	
	public KeyValueChart(Type chartType) {
		super(chartType);
	}

	public KeyValueChart(Type chartType, String title) {
		super(chartType, title);
	}

	public KeyValueChart(Type chartType, String title, String label_name, String label_value) {
		super(chartType, title);
		setLabel(label_name, label_value);
	}

	public String getCategoryName() {
		return category_name;
	}
	
	public String getNameLable() {
		return label_name;
	}
	
	public String getValueLable() {
		return label_value;
	}
	
	public boolean getShowItemLabel() {
		return show_item_label;
	}
	
	public boolean getMergeToOthers() {
		return merge_to_others;
	}
	
	public int getMaxItemCount() {
		return max_item_count;
	}
	
	public float getMinItemPercent() {
		return min_item_percent;
	}

	public void setCategoryName(String category_name) {
		this.category_name = category_name;
	}
	
	public void setLabel(String label_name, String label_value) {
		setNameLabel(label_name);
		setValueLabel(label_value);
	}

	public void setNameLabel(String label_name) {
		this.label_name = label_name;
	}

	public void setValueLabel(String label_value) {
		this.label_value = label_value;
	}
	
	public void setShowItemLabel(boolean show_item_label) {
		this.show_item_label = show_item_label;
	}
	
	public void setMergeToOthers(boolean merge_to_others) {
		this.merge_to_others = merge_to_others;
	}
	
	public void setMaxItemCount(int max_item_count) {
		this.max_item_count = max_item_count;
	}
	
	public void setMinItemPercent(float min_item_percent) {
		this.min_item_percent = min_item_percent;
	}

	public Type[] getSupportChartTypes() {
		return new Type[] { Type.VerticalBar, Type.HorizontalBar, Type.Pie };
	}
	
	public Type getDefaultChartType() {
		return Type.VerticalBar;
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
		int total_item_count = dataList.size();
		long other_total = 0L;
		long other_error = 0L;
		int other_count = 0;
		int item_count = 0;
		String last_other_name = null;

		if(chartType == Type.Pie) {
			pieDataset = new DefaultPieDataset();
		} else {
			categoryDataset = new DefaultCategoryDataset();
		}		
		for(int i = 0; i < total_item_count; i++) {
			KeyEntryVO vo = (KeyEntryVO)dataList.get(i);
			if(merge_to_others && (Float.valueOf(vo.getFilterdRequestRatio()) < min_item_percent || item_count >= max_item_count)) {
				last_other_name = vo.getKey();
				other_total += vo.getRequestCount();
				other_error += vo.getErrorCount();
				other_count++;
			} else {
				if(chartType == Type.Pie) {
					pieDataset.setValue(vo.getKey(), vo.getRequestCount());
				} else {
					int error = vo.getErrorCount();
					int success = vo.getRequestCount() - error;
					categoryDataset.addValue(success, "Success", vo.getKey());
					if(error > 0) {
						categoryDataset.addValue(error, "Error", vo.getKey());
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
	
	public void createChart() {
		if(chartType == Type.Pie) {
		    jfreeChart = ChartFactory.createPieChart(title, pieDataset, true, true, false);
		    jfreeChart.setPadding(new RectangleInsets(0, 20, 0, 20));
		    PiePlot piePlot = (PiePlot)jfreeChart.getPlot();
		    piePlot.setToolTipGenerator(new StandardPieToolTipGenerator("{0}: ({1}, {2})", DF_NUMBER, DF_PERCENT));
		    if(show_item_label) {
		    	piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1}, {2})", DF_NUMBER, DF_PERCENT));
		    } else {
		    	piePlot.setLabelGenerator(null);
		    }
		    TextTitle title = jfreeChart.getTitle();
		    RectangleInsets padding = title.getPadding();
		    double bottomPadding = Math.max(padding.getBottom(), 10.0D);
		    title.setPadding(padding.getTop(), padding.getLeft(), bottomPadding, padding.getRight());			
		} else {
			PlotOrientation plotOrientation = (chartType == Type.VerticalBar) ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL; 
			jfreeChart = ChartFactory.createStackedBarChart(title, label_name, label_value, categoryDataset, plotOrientation, true, true, false);
			CategoryPlot categoryPlot = (CategoryPlot)jfreeChart.getPlot();
			categoryPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			categoryPlot.setRangePannable(true);
			categoryPlot.setRangeCrosshairVisible(true);
			CategoryAxis categoryAxis = categoryPlot.getDomainAxis();
		    categoryAxis.setCategoryMargin(0.25D);
		    categoryAxis.setUpperMargin(0.02D);
		    categoryAxis.setLowerMargin(0.02D);
		    if(chartType == Type.HorizontalBar) {
		    	categoryAxis.setMaximumCategoryLabelWidthRatio(0.20F);
		    }
			NumberAxis numberAxis = (NumberAxis)categoryPlot.getRangeAxis();
			numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			numberAxis.setUpperMargin(0.1D);
		    BarRenderer renderer = (BarRenderer)categoryPlot.getRenderer();
		    renderer.setBaseItemLabelGenerator(new TotalCountItemLabelGenerator());
		    renderer.setItemLabelAnchorOffset(9.0D);
		    renderer.setBaseItemLabelsVisible(true);
		    renderer.setBaseItemLabelsVisible(show_item_label);
		    if(chartType == Type.HorizontalBar) {
		    	renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT));
		    } else {
		    	renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER));
		    }
		    renderer.setBaseToolTipGenerator(new SuccessErrorCountToolTipGenerator());
		    TextTitle title = jfreeChart.getTitle();
		    RectangleInsets padding = title.getPadding();
		    double bottomPadding = Math.max(padding.getBottom(), 4.0D);
		    title.setPadding(padding.getTop(), padding.getLeft(), bottomPadding, padding.getRight());
		}
	}

}
