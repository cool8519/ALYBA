package dal.tool.analyzer.alyba.ui.chart;

import java.awt.BasicStroke;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import dal.tool.analyzer.alyba.output.vo.DateEntryVO;

public abstract class TimeSeriesChart extends Chart {

	protected static final DecimalFormat DF_NUMBER = new DecimalFormat("0");
	
	protected static final Class<?> DATA_CLASS = DateEntryVO.class;

	protected String label_x = "Time";
	protected String label_y = "Value";
	protected String label_y2 = null;
	protected XYDataset dataset;
	protected XYDataset dataset2;
	protected String date_format = "yyyy.MM.dd HH:mm";
	protected DateTickUnit tick_unit = null;
	protected boolean show_secondary_axis = false;
	protected boolean show_shape = false;
	protected boolean show_moving_average = false;
	protected boolean merge_item = false;
	protected int moving_avg_count = 1;
	protected int merge_item_count = 1;
	protected String unit_str = "unit(s)";

	public TimeSeriesChart() {
		super(Type.TimeSeries);
	}

	public TimeSeriesChart(String title) {
		super(Type.TimeSeries, title);
	}

	public TimeSeriesChart(String title, String label_x, String label_y) {
		super(Type.TimeSeries, title);
		setLabel(label_x, label_y);
	}

	public String getLableX() {
		return label_x;
	}
	
	public String getLableY() {
		return label_y;
	}
	
	public String getLableYSecondary() {
		return label_y2;
	}

	public String getDateFormat() {
		return date_format;
	}
	
	public DateTickUnit getTickUnit() {
		return tick_unit;
	}

	public boolean getShowSecondaryAxis() {
		return show_secondary_axis;
	}

	public boolean getShowShape() {
		return show_shape;
	}
	
	public boolean getShowMovingAverage() {
		return show_moving_average;
	}
	
	public int getMovingAverageCount() {
		return moving_avg_count;
	}

	public boolean getMergeItem() {
		return merge_item;
	}
	
	public int getMergeItemCount() {
		return merge_item_count;
	}
	
	public String getUnitString() {
		return unit_str;
	}

	public void setLabel(String label_x, String label_y) {
		setLabelX(label_x);
		setLabelY(label_y);
	}

	public void setLabelX(String label_x) {
		this.label_x = label_x;
	}

	public void setLabelY(String label_y) {
		this.label_y = label_y;
	}

	public void setLabelYSecondary(String label_y_secondary) {
		this.label_y2 = label_y_secondary;
	}

	public void setDateFormat(String date_format) {
		this.date_format = date_format;
	}
	
	public void setTickUnit(DateTickUnit tick_unit) {
		this.tick_unit = tick_unit;
	}

	public void setShowSecondaryAxis(boolean show_secondary_axis) {
		this.show_secondary_axis = show_secondary_axis;
	}

	public void setShowShape(boolean show_shape) {
		this.show_shape = show_shape;
	}

	public void setShowMovingAverage(boolean show_moving_average) {
		this.show_moving_average = show_moving_average;
	}

	public void setMovingAverageCount(int moving_avg_count) {
		this.moving_avg_count = moving_avg_count;
	}

	public void setMergeItem(boolean merge_item) {
		this.merge_item = merge_item;
	}

	public void setMergeItemCount(int merge_item_count) {
		this.merge_item_count = merge_item_count;
	}

	public void setUnitString(String unit_str) {
		this.unit_str = unit_str;
	}

	public Type[] getSupportChartTypes() {
		return new Type[] { Type.TimeSeries };
	}
	
	public Type getDefaultChartType() {
		return Type.TimeSeries;
	}	
	
	public void createChart() {
		jfreeChart = ChartFactory.createTimeSeriesChart(title, label_x, label_y, dataset, true, true, false);
		XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
		xyPlot.setDomainPannable(true);
		xyPlot.setRangePannable(true);
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
	    DateAxis dateAxis = (DateAxis)xyPlot.getDomainAxis();
	    if(tick_unit == null) {
	    	dateAxis.setDateFormatOverride(new SimpleDateFormat(date_format));
		    dateAxis.setAutoTickUnitSelection(true);
	    } else {
		    dateAxis.setTickUnit(tick_unit);
	    }
	    dateAxis.setVerticalTickLabels(true);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)xyPlot.getRenderer();
		renderer.setBaseShapesVisible(show_shape);
		
		TimeSeriesCollection ts_collection = (TimeSeriesCollection)dataset;
		if(show_moving_average) {
			int series_count = ts_collection.getSeriesCount();
			for(int i = 0; i < series_count; i++) {
				TimeSeries ts = ts_collection.getSeries(i);
			    TimeSeries ts_mov = MovingAverage.createMovingAverage(ts, ts_collection.getSeriesKey(i)+"("+moving_avg_count+" Moving Avg.)", moving_avg_count, 0);
			    ts_collection.addSeries(ts_mov);
				renderer.setSeriesShapesVisible(series_count+i, false);
			}
		}		
		TextTitle title = jfreeChart.getTitle();
		RectangleInsets padding = title.getPadding();
		double bottomPadding = Math.max(padding.getBottom(), 4.0D);
		title.setPadding(padding.getTop(), padding.getLeft(), bottomPadding, padding.getRight());
	}

	public void afterCreateChartPanel(ChartPanel chartPanel) {
		chartPanel.addChartMouseListener(new ChartMouseListener() {
			public void chartMouseClicked(ChartMouseEvent event) {
				ChartEntity entity = event.getEntity();
				if(entity instanceof LegendItemEntity) {
					LegendItemEntity itemEntity = (LegendItemEntity)entity;
					XYDataset xyDataset = (XYDataset)itemEntity.getDataset();
					if(dataset2 == null && xyDataset.getSeriesCount() < 2) {
						return;
					}
					int index = xyDataset.indexOf(itemEntity.getSeriesKey());
					XYPlot plot = (XYPlot)event.getChart().getPlot();
					int index_renderer = (xyDataset == dataset) ? 0 : 1;
					XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(index_renderer);
					float width = ((BasicStroke)renderer.getSeriesStroke(index)).getLineWidth();
					Boolean visible = renderer.getSeriesLinesVisible(index);
					if(visible == null || visible == Boolean.TRUE) {
						if(width == 1.0F) {
							renderer.setSeriesStroke(index, new BasicStroke(4.0F, 2, 2));
						} else {
							renderer.setSeriesLinesVisible(index, false);
							renderer.setSeriesShapesVisible(index, false);
						}
					} else {
						renderer.setSeriesLinesVisible(index, true);
						renderer.setSeriesStroke(index, new BasicStroke(1.0F, 2, 2));
						renderer.setSeriesShapesVisible(index, show_shape);
					}
					renderer.setDrawSeriesLineAsPath(true);
				}
			}
			public void chartMouseMoved(ChartMouseEvent event) {
			}
		});
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
	    XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
	    xyPlot.getRenderer().setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: (\"{1}\", {2})", new SimpleDateFormat(date_format), DF_NUMBER));
	    if(label_y2 != null && show_secondary_axis) {
		    NumberAxis secondaryAxis = new NumberAxis(label_y2);
		    secondaryAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		    secondaryAxis.setLabelFont(xyPlot.getRangeAxis(0).getLabelFont());
		    xyPlot.setRangeAxis(1, secondaryAxis);
		    xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		    xyPlot.setDataset(1, dataset2);
		    xyPlot.mapDatasetToRangeAxis(1, 1);
		    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, show_shape);
		    renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: (\"{1}\", {2})", new SimpleDateFormat(date_format), DF_NUMBER));
		    xyPlot.setRenderer(1, renderer);
	
			TimeSeriesCollection ts_collection = (TimeSeriesCollection)dataset2;
			if(show_moving_average) {
				int series_count = ts_collection.getSeriesCount();
				for(int i = 0; i < series_count; i++) {
					TimeSeries ts = ts_collection.getSeries(i);
				    TimeSeries ts_mov = MovingAverage.createMovingAverage(ts, ts_collection.getSeriesKey(i)+"("+moving_avg_count+" Moving Avg.)", moving_avg_count, 0);
				    ts_collection.addSeries(ts_mov);
					renderer.setSeriesShapesVisible(series_count+i, false);
				}
			}
		}
	}

}
