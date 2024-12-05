package dal.tool.analyzer.alyba.ui.chart.regression;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataItem;
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
	
	protected RegressionChart parent_chart;
	protected AggregationType aggregation_type = AggregationType.NAME;
	protected ResourceMergeType resource_merge_type = ResourceMergeType.AVG;
	protected boolean resource_axis_to_100 = false;

	protected ShapeSize[] shape_sizes;
    private boolean isDragging = false;
    private Date selectedMinX = null;
    private Date selectedMaxX = null;
    private double selectedMinY = -1d;
    private double selectedMaxY = -1d;
    private Range orgDomainRange = null;
    private Range orgValueRangeP = null;
    private Range orgValueRangeS = null;
    private Range autoValueRange = null;
    private Range resetValueRangeP = null;
    private Range resetValueRangeS = null;
    private Date firstDate = null;
    private Date lastDate = null;
    
	public RegressionVariablesChart(RegressionChart parent, String title, VariableX varX, VariableY varY, AggregationType aggregationType, ResourceMergeType resourceMergeType, boolean axisYto100) {
		super(title);
		this.parent_chart = parent;
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
	    Number value = null;
		if(VariableY.AVG_RESPONSE.name().equals(label_y2) || VariableY.ERROR.name().equals(label_y2)) {
			ts_var2 = new TimeSeries(label_y2);
			for(Object data : dataList) {
				RegressionEntryVO vo = (RegressionEntryVO)data;
				value = RegressionChart.getVariableData(vo, label_y);
				if(value != null) {
		    		ts_var1.addOrUpdate(new Minute(vo.getUnitDate()), value);
				}
				value = RegressionChart.getVariableData(vo, label_y2);
				if(value != null) {
		    		ts_var2.addOrUpdate(new Minute(vo.getUnitDate()), value);
				}
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
		    			if(str_name_prev != null && ts_var2.getItemCount() > 0) {
		    				tsc_var2.addSeries(ts_var2);
		    			}
		    			ts_var2 = new TimeSeries(str_name);
		    			str_name_prev = str_name;
		    		}
		    		value = RegressionChart.getVariableData(vo, label_y);
		    		setFirstAndLastDateByValue(value, vo.getUnitDate());
	    			ts_var1.addOrUpdate(new Minute(vo.getUnitDate()), value);
		    		value = RegressionChart.getVariableData(vo, label_y2);
		    		if(value != null) {
		    			ts_var2.add(new Minute(vo.getUnitDate()), value);
		    		}
			    }
			    tsc_var2.addSeries(ts_var2);	    
			    tsc_var1.addSeries(ts_var1);
			} else if(aggregation_type == AggregationType.GROUP) {
				Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.GROUP_TIME));
			    for(EntryVO data : dataList) {
			    	RegressionEntryVO vo = (RegressionEntryVO)data;
		    		String str_name = vo.getServerGroup();
		    		if(str_name_prev == null || !str_name_prev.equals(str_name)) {
		    			if(str_name_prev != null && ts_var2.getItemCount() > 0) {
				    		ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), RegressionChart.getVariableData(mergedVO, label_y));
				    		value = RegressionChart.getVariableData(mergedVO, label_y2);
				    		if(value != null) {		
				    			ts_var2.add(new Minute(mergedVO.getUnitDate()), value);
				    		}
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
		    			value = RegressionChart.getVariableData(mergedVO, label_y);
			    		setFirstAndLastDateByValue(value, mergedVO.getUnitDate());
	    				ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), value);
			    		value = RegressionChart.getVariableData(mergedVO, label_y2);
			    		if(value != null && !mergedVO.getFailed()) {
			    			ts_var2.add(new Minute(mergedVO.getUnitDate()), value);
			    		}
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
			    }
			    value = RegressionChart.getVariableData(mergedVO, label_y);
			    setFirstAndLastDateByValue(value, mergedVO.getUnitDate());
				ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), value);
	    		value = RegressionChart.getVariableData(mergedVO, label_y2);
	    		if(value != null && !mergedVO.getFailed()) {
	    			ts_var2.add(new Minute(mergedVO.getUnitDate()), value);
	    		}
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
		    			value = RegressionChart.getVariableData(mergedVO, label_y);
					    setFirstAndLastDateByValue(value, mergedVO.getUnitDate());
	    				ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), value);
			    		value = RegressionChart.getVariableData(mergedVO, label_y2);
			    		if(value != null && !mergedVO.getFailed()) {
			    			ts_var2.add(new Minute(mergedVO.getUnitDate()), value);
			    		}
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
				}
				value = RegressionChart.getVariableData(mergedVO, label_y);
			    setFirstAndLastDateByValue(value, mergedVO.getUnitDate());
				ts_var1.addOrUpdate(new Minute(mergedVO.getUnitDate()), value);
	    		value = RegressionChart.getVariableData(mergedVO, label_y2);
	    		if(value != null) {
	    			ts_var2.add(new Minute(mergedVO.getUnitDate()), value);
	    		}
			    tsc_var2.addSeries(ts_var2);	    
			    tsc_var1.addSeries(ts_var1);
			}
		}
	    dataset = tsc_var1;
	    dataset2 = tsc_var2;
	    shape_sizes = new ShapeSize[dataset2.getSeriesCount()+1];
	    for(int i = 0; i < dataset2.getSeriesCount()+1; i++) {
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
		xyPlot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
	    xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		DateAxis dateAxis = (DateAxis)xyPlot.getDomainAxis();
	    if(tick_unit == null) {
	    	dateAxis.setDateFormatOverride(new SimpleDateFormat(date_format));
		    dateAxis.setAutoTickUnitSelection(true);
	    } else {
		    dateAxis.setTickUnit(tick_unit);
	    }
	    dateAxis.setLabelFont(xyPlot.getRangeAxis(0).getLabelFont());
	    dateAxis.setTickLabelFont(xyPlot.getRangeAxis(0).getTickLabelFont());
	    dateAxis.setLowerMargin(0.0D);
	    dateAxis.setUpperMargin(0.0D);
	    if(firstDate != null && lastDate != null) {
	    	dateAxis.setRange(firstDate.getTime()-60000, lastDate.getTime()+60000);
	    }

		NumberAxis primaryAxis = (NumberAxis)xyPlot.getRangeAxis();
		primaryAxis.setRangeType(RangeType.POSITIVE);
		primaryAxis.setAutoRangeMinimumSize(10.0D);
		primaryAxis.setLowerMargin(0.0D);
		primaryAxis.setUpperMargin(0.0D);
		resetValueRangeP = primaryAxis.getRange();
		
	    NumberAxis secondaryAxis = new NumberAxis(label_y2);
	    secondaryAxis.setLabelFont(xyPlot.getRangeAxis(0).getLabelFont());
	    secondaryAxis.setTickLabelFont(xyPlot.getRangeAxis(0).getTickLabelFont());
	    secondaryAxis.setRangeType(RangeType.POSITIVE);
	    secondaryAxis.setAutoRangeMinimumSize(10.0D);
	    secondaryAxis.setLowerMargin(0.0D);
	    secondaryAxis.setUpperMargin(0.0D);
	    xyPlot.setRangeAxis(1, secondaryAxis);
	    xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
	    xyPlot.setDataset(1, dataset2);
	    xyPlot.mapDatasetToRangeAxis(1, 1);
	    autoValueRange = secondaryAxis.getRange();
	    if(resource_axis_to_100 && RegressionChart.isVariableResource(label_y2)) {
	    	resetValueRangeS = new Range(-2.0D, Math.max(100.0D, autoValueRange.getUpperBound())+2.0D);
	    } else {
	    	resetValueRangeS = new Range(-2.0D, autoValueRange.getUpperBound()+2.0D);
	    }
	    secondaryAxis.setRange(resetValueRangeS);

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
        chartPanel.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		if(e.isControlDown() || !parent_chart.isDeleteMode()) return;
    			Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
    			XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
    			Point2D sPoint = chartPanel.translateScreenToJava2D(e.getPoint());
    			double xVal = xyPlot.getDomainAxis().java2DToValue(sPoint.getX(), dataArea, xyPlot.getDomainAxisEdge());
    			selectedMinX = new Date((long)xVal);
    			selectedMaxY = xyPlot.getRangeAxis(1).java2DToValue(sPoint.getY(), dataArea, xyPlot.getRangeAxisEdge(1));
    			orgDomainRange = xyPlot.getDomainAxis().getRange();
    			orgValueRangeP = xyPlot.getRangeAxis(0).getRange();
    			orgValueRangeS = xyPlot.getRangeAxis(1).getRange();
        	}
			public void mouseReleased(MouseEvent e) {
				if(e.isControlDown() || !isDragging || !parent_chart.isDeleteMode() || !selectedMaxX.after(selectedMinX) || selectedMinY >= selectedMaxY) return;
				XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
				xyPlot.getDomainAxis().setRange(orgDomainRange);
				xyPlot.getRangeAxis(0).setRange(orgValueRangeP);
		    	xyPlot.getRangeAxis(1).setRange(orgValueRangeS);
		    	processDelete();
		    	isDragging = false;
		    	selectedMinX = selectedMaxX = null;
		    	selectedMinY = selectedMaxY = -1D;
			}
		});
		chartPanel.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
        		if(e.isControlDown() || !parent_chart.isDeleteMode()) return;
    			isDragging = true;
    			Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
    			XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
    			Point2D ePoint = chartPanel.translateScreenToJava2D(e.getPoint());
    			double xVal = xyPlot.getDomainAxis().java2DToValue(ePoint.getX(), dataArea, xyPlot.getDomainAxisEdge());
    			selectedMaxX = new Date((long)xVal);
    			selectedMinY = xyPlot.getRangeAxis(1).java2DToValue(ePoint.getY(), dataArea, xyPlot.getRangeAxisEdge(1));
			}
		});
		NumberAxis yAxis = (NumberAxis)((XYPlot)jfreeChart.getPlot()).getRangeAxis(1);
		yAxis.addChangeListener(new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent e) {
            	if(((NumberAxis)e.getSource()).getRange().equals(autoValueRange)) {
            		((XYPlot)jfreeChart.getPlot()).getRangeAxis(0).setRange(resetValueRangeP);
            		yAxis.setRange(resetValueRangeS, true, false);
            	}
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
					float size = DEFAULT_SHAPE_SIZE.ordinal() + 1;
					renderer.setSeriesStroke(series_index, new BasicStroke(size, 2, 2));
				}
			} else {
				renderer.setSeriesLinesVisible(series_index, true);
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
					shape_sizes[series_index] = DEFAULT_SHAPE_SIZE;
					int size = shape_sizes[series_index].ordinal() + 1;
					Shape shape = new Ellipse2D.Double(-size, -size, size, size);
					renderer.setSeriesShape(series_index, shape);
				}
			} else {
				renderer.setSeriesShapesVisible(series_index, true);
			}
		}		
		renderer.setDrawSeriesLineAsPath(true);
	}
	
	private void processDelete() {
		Map<String,List<CustomTimeSeriesDataItem>> selectedItems = getSelectedItems(selectedMinX, selectedMaxX, selectedMinY, selectedMaxY, dataset2, dataset);
		if(selectedItems.size() > 0) {
			nominateItems(selectedItems, dataset2);
			parent_chart.nominateItemsPropagationByVariables(selectedItems);
		}
	}

	private Map<String,List<CustomTimeSeriesDataItem>> getSelectedItems(Date minX, Date maxX, double minY, double maxY, XYDataset dataset, XYDataset dataset_tps) {
    	Map<String,List<CustomTimeSeriesDataItem>> result = new HashMap<String,List<CustomTimeSeriesDataItem>>();
    	TimeSeries tpsSeries = ((TimeSeriesCollection)dataset_tps).getSeries(0);
    	for(int idx = 0; idx < dataset.getSeriesCount(); idx++) {
    		TimeSeries series = ((TimeSeriesCollection)dataset).getSeries(idx);
    		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(1);
    		Boolean visible = renderer.getSeriesShapesVisible(idx);
    		if(!series.getKey().equals("Removed") && (visible == null || visible == Boolean.TRUE)) {
    			List<CustomTimeSeriesDataItem> selectedList = new ArrayList<CustomTimeSeriesDataItem>();
	    		for(Object itemObj : series.getItems()) {
	    			TimeSeriesDataItem item = (TimeSeriesDataItem)itemObj;
	    			long xValue = item.getPeriod().getFirstMillisecond();
	    			double yValue = item.getValue().doubleValue();
	    			if(xValue >= minX.getTime() && xValue <= maxX.getTime() && yValue >= minY && yValue <= maxY) {
	    				Number tpsValue = tpsSeries.getDataItem(item.getPeriod()).getValue();
	    				selectedList.add(new CustomTimeSeriesDataItem(item, tpsValue));
	    			}
	    		}
	    		if(selectedList.size() > 0) {
	    			result.put((String)dataset.getSeriesKey(idx), selectedList);
	    		}
    		}
    	}
    	return result;
    }

    private void nominateItems(Map<String,List<CustomTimeSeriesDataItem>> mapToRemove, XYDataset dataset) {
    	TimeSeriesCollection seriesCollection = (TimeSeriesCollection)dataset;
    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(1);
    	for(String key : mapToRemove.keySet()) {
    		TimeSeries series = seriesCollection.getSeries(key);
    		TimeSeries toRemoveSeries = new TimeSeries(key+"~toRemove");
    		series.setNotify(false);
    		toRemoveSeries.setNotify(false);
    		for(int idx_item = series.getItemCount()-1; idx_item >= 0; idx_item--) {
    			TimeSeriesDataItem item = series.getDataItem(idx_item);
    			for(CustomTimeSeriesDataItem itemToRemove : mapToRemove.get(key)) {
    				if(itemToRemove.equals(item)) {
    					series.delete(item.getPeriod());
    	    			toRemoveSeries.addOrUpdate(item);
    	    			break;
    				}
    			}
    		}
    		series.setNotify(true);
    		toRemoveSeries.setNotify(true);
    		if(toRemoveSeries.getItemCount() > 0) {
    			seriesCollection.addSeries(toRemoveSeries);
    			int orgIdx = seriesCollection.indexOf(key);
    			int newIdx = dataset.getSeriesCount() - 1;
    			renderer.setSeriesShape(newIdx, renderer.getSeriesShape(orgIdx));
    			renderer.setSeriesPaint(newIdx, ((Color)renderer.getSeriesPaint(orgIdx)).darker().darker());
    			renderer.setSeriesVisibleInLegend(newIdx, false);
    		}
    	}
    }

    public void nominateItems(Map<String,List<XYDataItem>> mapToRemove) {
    	TimeSeriesCollection seriesCollection = (TimeSeriesCollection)dataset2;
    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(1);
    	for(String key : mapToRemove.keySet()) {
    		TimeSeries series = seriesCollection.getSeries(key);
    		TimeSeries toRemoveSeries = new TimeSeries(key+"~toRemove");
    		series.setNotify(false);
    		toRemoveSeries.setNotify(false);
    		for(XYDataItem itemToRemove : mapToRemove.get(key)) {
        		for(int idx_item = series.getItemCount()-1; idx_item >= 0; idx_item--) {
        			TimeSeriesDataItem item = series.getDataItem(idx_item);
    				if(itemToRemove.getY().equals(item.getValue())) {
    					series.delete(item.getPeriod());
    	    			toRemoveSeries.addOrUpdate(item);
    	    			break;
    				}
        		}
    		}
    		series.setNotify(true);
    		toRemoveSeries.setNotify(true);
    		if(toRemoveSeries.getItemCount() > 0) {
    			seriesCollection.addSeries(toRemoveSeries);
    			int orgIdx = seriesCollection.indexOf(key);
    			int newIdx = dataset2.getSeriesCount() - 1;
    			renderer.setSeriesShape(newIdx, renderer.getSeriesShape(orgIdx));
    			renderer.setSeriesPaint(newIdx, ((Color)renderer.getSeriesPaint(orgIdx)).darker().darker());
    			renderer.setSeriesVisibleInLegend(newIdx, false);
    		}
    	}
    }

	public void removeOrRestoreItems(boolean remove) {
		TimeSeriesCollection seriesCollection = (TimeSeriesCollection)dataset2;
    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(1);
    	int removedSeriesIndex = seriesCollection.getSeriesIndex("Removed");
    	TimeSeries removedSeries = removedSeriesIndex < 0 ? new TimeSeries("Removed") : seriesCollection.getSeries(removedSeriesIndex);
    	removedSeries.setNotify(false);
		for(int idx_series = seriesCollection.getSeriesCount()-1; idx_series >= 0 ; idx_series--) {
			TimeSeries series = seriesCollection.getSeries(idx_series);
			String key = (String)series.getKey();
			int idx = key.indexOf("~toRemove");
			if(idx < 0) break;
			TimeSeries orgSeries = seriesCollection.getSeries(key.substring(0, idx));
    		series.setNotify(false);
    		orgSeries.setNotify(false);
			for(int i = series.getItemCount()-1; i >= 0; i--) {
				TimeSeriesDataItem item = series.getDataItem(i);
				if(remove) {
					removedSeries.addOrUpdate(item);
				} else {
					orgSeries.addOrUpdate(item);
				}
				series.delete(item.getPeriod());
			}
    		series.setNotify(true);
    		orgSeries.setNotify(true);
			seriesCollection.removeSeries(idx_series);
			renderer.setSeriesPaint(idx_series, null);
			renderer.setSeriesShape(idx_series, null);
			renderer.setSeriesVisibleInLegend(idx_series, null);
		}
		removedSeries.setNotify(true);
		if(remove && removedSeriesIndex < 0) {
			seriesCollection.addSeries(removedSeries);
			removedSeriesIndex = seriesCollection.getSeriesCount()-1;
			int size = shape_sizes[removedSeriesIndex].ordinal() + 1;
			renderer.setSeriesShape(removedSeriesIndex, new Ellipse2D.Double(-size, -size, size, size));
			renderer.setSeriesPaint(removedSeriesIndex, new Color(160, 160, 160));
			renderer.setSeriesVisibleInLegend(removedSeriesIndex, true);
		}
	}

	private void setFirstAndLastDateByValue(Number val, Date dt) {
		if(val != null) {
			if(firstDate == null) firstDate = dt;
			lastDate = dt;
		}
	}
	
}
