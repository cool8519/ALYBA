package dal.tool.analyzer.alyba.ui.chart.regression;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.RangeType;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import dal.tool.analyzer.alyba.output.ResourceGroupAndNameComparator;
import dal.tool.analyzer.alyba.output.ResourceGroupAndNameComparator.SortBy;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.RegressionEntryVO;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.AggregationType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.ResourceMergeType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableX;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableY;

public class RegressionVariablesChart extends TimeSeriesChart {

	protected static final DecimalFormat DF_NUMBER = new DecimalFormat("#.##");
	protected static final Class<?> DATA_CLASS = RegressionEntryVO.class;
	protected static final ShapeSize DEFAULT_SHAPE_SIZE = ShapeSize.Smallest;
	protected static final ShapeSize BOLD_SHAPE_SIZE = ShapeSize.Large;
	
	protected AggregationType aggregation_type = AggregationType.NAME;
	protected ResourceMergeType resource_merge_type = ResourceMergeType.AVG;
	protected boolean resource_axis_to_100 = false;

	protected ShapeSize[] shape_sizes;

	public RegressionVariablesChart(String title, VariableX varX, VariableY varY, AggregationType aggregationType, ResourceMergeType resourceMergeType, boolean axisYto100) {
		super(title);
	    setUnitString("minute(s)");
	    setDateFormat("yyyy.MM.dd HH:mm");
    	setShowSecondaryAxis(true);
    	setLabelX("Time");
    	setLabelY(varX.name());
    	setLabelYSecondary(varY.name());
    	this.aggregation_type = aggregationType;
    	this.resource_merge_type = resourceMergeType;
    	this.resource_axis_to_100 = axisYto100;
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
	    TimeSeries ts_var1 = new TimeSeries(label_y);
	    TimeSeriesCollection tsc_var1 = new TimeSeriesCollection();
		TimeSeries ts_var2 = null;
	    TimeSeriesCollection tsc_var2 = new TimeSeriesCollection();
		if(VariableY.AVG_RESPONSE.name().equals(label_y2) || VariableY.ERROR.name().equals(label_y2)) {
			ts_var2 = new TimeSeries(label_y2);
			for(Object data : dataList) {
				RegressionEntryVO vo = (RegressionEntryVO)data;
	    		ts_var1.addOrUpdate(new Minute(vo.getUnitDate()), RegressionChart.getVariableData(vo, label_y));
	    		ts_var2.addOrUpdate(new Minute(vo.getUnitDate()), RegressionChart.getVariableData(vo, label_y2));
			}
		    tsc_var2.addSeries(ts_var2);	    
		    tsc_var1.addSeries(ts_var1);			
		} else {
		    String str_name_prev = null;
		    Date dt_prev = null;
	    	RegressionEntryVO mergedVO = null;
			if(aggregation_type == AggregationType.NAME) {
				Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.GROUP_NAME_TIME));
			    for(EntryVO data : dataList) {
			    	RegressionEntryVO vo = (RegressionEntryVO)data;
		    		String str_name = vo.getServerGroup() + ":" + vo.getServerName();
		    		if(str_name_prev == null || !str_name_prev.equals(str_name)) {
		    			if(str_name_prev != null) {
		    				tsc_var2.addSeries(ts_var2);
		    			}
		    			ts_var2 = new TimeSeries(str_name);
		    			str_name_prev = str_name;
		    		}		    	
		    		ts_var1.addOrUpdate(new Minute(vo.getUnitDate()), RegressionChart.getVariableData(vo, label_y));
		    		ts_var2.add(new Minute(vo.getUnitDate()), RegressionChart.getVariableData(vo, label_y2));
			    }
			    tsc_var2.addSeries(ts_var2);	    
			    tsc_var1.addSeries(ts_var1);
			} else if(aggregation_type == AggregationType.GROUP) {
				Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.GROUP_TIME));
			    for(EntryVO data : dataList) {
			    	RegressionEntryVO vo = (RegressionEntryVO)data;
		    		String str_name = vo.getServerGroup();
		    		if(str_name_prev == null || !str_name_prev.equals(str_name)) {
		    			if(str_name_prev != null) {
				    		ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y));
				    		ts_var2.add(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y2));
		    				tsc_var2.addSeries(ts_var2);
		    				dt_prev = null;
		    			}
		    			ts_var2 = new TimeSeries(str_name);
		    			str_name_prev = str_name;
		    		}
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo, resource_merge_type);
		    		} else {
			    		ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y));
			    		ts_var2.add(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y2));
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
			    }
	    		ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y));
	    		ts_var2.add(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y2));
			    tsc_var2.addSeries(ts_var2);	    
			    tsc_var1.addSeries(ts_var1);
			} else {
				Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.TIME));
				ts_var2 = new TimeSeries(label_y2);
				for(Object data : dataList) {
					RegressionEntryVO vo = (RegressionEntryVO)data;
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo, resource_merge_type);
		    		} else {
			    		ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y));
			    		ts_var2.add(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y2));
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
				}
	    		ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y));
	    		ts_var2.add(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y2));
			    tsc_var2.addSeries(ts_var2);	    
			    tsc_var1.addSeries(ts_var1);
			}
		}
	    dataset = tsc_var1;
	    dataset2 = tsc_var2;
	    shape_sizes = new ShapeSize[dataset2.getSeriesCount()];
	    for(int i = 0; i < dataset2.getSeriesCount(); i++) {
	    	shape_sizes[i] = DEFAULT_SHAPE_SIZE;
	    }
	}

	public void createChart() {		
		jfreeChart = ChartFactory.createTimeSeriesChart(title, null, label_y, dataset, true, true, false);
		XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
		xyPlot.setDomainPannable(true);
		xyPlot.setRangePannable(true);
		xyPlot.setDomainCrosshairVisible(false);
		xyPlot.setRangeCrosshairVisible(false);

		DateAxis dateAxis = (DateAxis)xyPlot.getDomainAxis();
	    if(tick_unit == null) {
	    	dateAxis.setDateFormatOverride(new SimpleDateFormat(date_format));
		    dateAxis.setAutoTickUnitSelection(true);
	    } else {
		    dateAxis.setTickUnit(tick_unit);
	    }
	    dateAxis.setLabelFont(xyPlot.getRangeAxis(0).getLabelFont());
	    dateAxis.setTickLabelFont(xyPlot.getRangeAxis(0).getTickLabelFont());
		dateAxis.setRange(dataset.getXValue(0, 0), dataset.getXValue(0, dataset.getItemCount(0)-1));

		NumberAxis primaryAxis = (NumberAxis)xyPlot.getRangeAxis();
		primaryAxis.setRangeType(RangeType.POSITIVE);
		primaryAxis.setAutoRangeMinimumSize(10.0D);
		
	    NumberAxis secondaryAxis = new NumberAxis(label_y2);
	    secondaryAxis.setLabelFont(xyPlot.getRangeAxis(0).getLabelFont());
	    secondaryAxis.setTickLabelFont(xyPlot.getRangeAxis(0).getTickLabelFont());
	    secondaryAxis.setRangeType(RangeType.POSITIVE);
	    secondaryAxis.setAutoRangeMinimumSize(10.0D);
	    if(resource_axis_to_100 && RegressionChart.isVariableResource(label_y2)) {
	    	secondaryAxis.setRange(0.0D, 100.0D);
	    }
	    xyPlot.setRangeAxis(1, secondaryAxis);
	    xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
	    xyPlot.setDataset(1, dataset2);
	    xyPlot.mapDatasetToRangeAxis(1, 1);
	    xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

	    XYLineAndShapeRenderer primaryRenderer = (XYLineAndShapeRenderer)xyPlot.getRenderer();
		primaryRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: (\"{1}\", {2})", new SimpleDateFormat(date_format), DF_NUMBER));
		primaryRenderer.setSeriesPaint(0, Color.DARK_GRAY);

	    XYLineAndShapeRenderer secondaryRenderer = new XYLineAndShapeRenderer(false, true);
		secondaryRenderer.setBaseShapesFilled(true);
		secondaryRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: (\"{1}\", {2})", new SimpleDateFormat(date_format), DF_NUMBER));
		int size = DEFAULT_SHAPE_SIZE.ordinal() + 1;
		Shape shape = new Ellipse2D.Double(-size, -size, size, size);
		for(int i = 0; i < dataset2.getSeriesCount(); i++) {
			secondaryRenderer.setSeriesShape(i, shape);
		}
		xyPlot.setRenderer(1, secondaryRenderer);
		TextTitle title = jfreeChart.getTitle();
		if(title != null) {
			RectangleInsets padding = title.getPadding();
			double bottomPadding = Math.max(padding.getBottom(), 10.0D);
			title.setPadding(padding.getTop(), padding.getLeft(), bottomPadding, padding.getRight());
		}
	}

	public void afterCreateChart(JFreeChart jfreeChart) {
	}

	public void afterCreateChartPanel(ChartPanel chartPanel) {
		chartPanel.addChartMouseListener(new ChartMouseListener() {
			public void chartMouseClicked(ChartMouseEvent event) {
				ChartEntity entity = event.getEntity();
				if(entity instanceof LegendItemEntity) {
					LegendItemEntity itemEntity = (LegendItemEntity)entity;
					XYDataset xyDataset = (XYDataset)itemEntity.getDataset();
					int series_index = xyDataset.indexOf(itemEntity.getSeriesKey());
					int renderer_index = label_y.equals(itemEntity.getSeriesKey()) ? 0 : 1;  
					clickedLegendItem(series_index, renderer_index);
				}
			}
			public void chartMouseMoved(ChartMouseEvent event) {
			}
		});
	}

	public void clickedLegendItem(int series_index, int renderer_index) {
		XYPlot plot = (XYPlot)jfreeChart.getPlot();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(renderer_index);
		Boolean visible = (renderer_index==0) ? renderer.getSeriesLinesVisible(series_index) : renderer.getSeriesShapesVisible(series_index);
		if(renderer_index == 0) {
			if(visible == null || visible == Boolean.TRUE) {
				float width = ((BasicStroke)renderer.getSeriesStroke(series_index)).getLineWidth();
				if(width == DEFAULT_SHAPE_SIZE.ordinal()+1) {
					float size = BOLD_SHAPE_SIZE.ordinal() + 1;
					renderer.setSeriesStroke(series_index, new BasicStroke(size, 2, 2));
				} else {
					renderer.setSeriesLinesVisible(series_index, false);
				}
			} else {
				renderer.setSeriesLinesVisible(series_index, true);
				float size = DEFAULT_SHAPE_SIZE.ordinal() + 1;
				renderer.setSeriesStroke(series_index, new BasicStroke(size, 2, 2));
			}
		} else {
			if(visible == null || visible == Boolean.TRUE) {
				if(shape_sizes[series_index] == DEFAULT_SHAPE_SIZE) {
					shape_sizes[series_index] = BOLD_SHAPE_SIZE;
					int size = shape_sizes[series_index].ordinal() + 1;
					Shape shape = new Ellipse2D.Double(-size, -size, size, size);
					renderer.setSeriesShape(series_index, shape);
				} else {
					renderer.setSeriesShapesVisible(series_index, false);
				}
			} else {
				shape_sizes[series_index] = DEFAULT_SHAPE_SIZE;
				int size = shape_sizes[series_index].ordinal() + 1;
				Shape shape = new Ellipse2D.Double(-size, -size, size, size);
				renderer.setSeriesShape(series_index, shape);
				renderer.setSeriesShapesVisible(series_index, true);
			}
		}		
		renderer.setDrawSeriesLineAsPath(true);
	}
	
}
