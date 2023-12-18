package dal.tool.analyzer.alyba.ui.chart.regression;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import dal.tool.analyzer.alyba.output.ResourceGroupAndNameComparator;
import dal.tool.analyzer.alyba.output.ResourceGroupAndNameComparator.SortBy;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.RegressionEntryVO;
import dal.tool.analyzer.alyba.ui.chart.DistributionChart;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.AggregationType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.RegressionType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.ResourceMergeType;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableX;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableY;

public class RegressionAnalysisChart extends DistributionChart {

	protected static final DecimalFormat DF_NUMBER = new DecimalFormat("#.##");
	protected static final Class<?> DATA_CLASS = RegressionEntryVO.class;
	protected static final ShapeSize DEFAULT_SHAPE_SIZE = ShapeSize.Smallest;
	protected static final ShapeSize BOLD_SHAPE_SIZE = ShapeSize.Large;
	protected static final float DEFAULT_LINE_WIDTH = 2.0F;
	protected static final float BOLD_LINE_WIDTH = 4.0F;
	
	protected RegressionChart parent_chart;
	protected AggregationType aggregation_type = AggregationType.NAME;
	protected ResourceMergeType resource_merge_type = ResourceMergeType.AVG;
	protected RegressionType regression_type = RegressionType.LINEAR;
	protected boolean resource_axis_to_100 = false;

	protected List<SimpleRegression> regressions = new ArrayList<SimpleRegression>();	
	protected ShapeSize[] shape_sizes;
	protected XYPointerAnnotation[] equation_annotations;

    private boolean isDragging = false;
    private double selectedMinX = -1d;
    private double selectedMaxX = -1d;
    private double selectedMinY = -1d;
    private double selectedMaxY = -1d;
    private Range orgDomainRange = null;
    private Range orgValueRange = null;
    private Range autoDomainRange = null;
    private Range autoValueRange = null;
    private Range resetDomainRange = null;
    private Range resetValueRange = null;
    
	public RegressionAnalysisChart(RegressionChart parent, String title, VariableX varX, VariableY varY, AggregationType aggregationType, ResourceMergeType resourceMergeType, RegressionType regressionType, boolean showRegressionLine, boolean showRegressionEquation, boolean axisYto100) {
		super(Type.ScatterPlot, title);
		this.parent_chart = parent;
    	setLabelX(varX.name());
    	setLabelY(varY.name());
	    setDateFormat("yyyy.MM.dd HH:mm");
	    this.aggregation_type = aggregationType;
	    this.resource_merge_type = resourceMergeType;
	    this.regression_type = regressionType;
	    this.show_regression_line = showRegressionLine;
	    this.show_regression_equation = showRegressionEquation;
    	this.resource_axis_to_100 = axisYto100;
	}

	public AggregationType getAggregationType() {
		return aggregation_type;
	}
	
	public void setAggregationType(AggregationType type) {
		this.aggregation_type = type;
	}
	
	public ResourceMergeType getResourceMergeType() {
		return resource_merge_type;
	}
	
	public void setResourceMergeType(ResourceMergeType type) {
		this.resource_merge_type = type;
	}
	
	public RegressionType getRegressionType() {
		return regression_type;
	}
	
	public void setRegressionType(RegressionType type) {
		this.regression_type = type;
	}
	
	public List<SimpleRegression> getRegressions() {
		return regressions;
	}
	
	public Double[] getDefaultBoundaryValues() {
		return null;
	}

	public double getDistributionValue(EntryVO vo) {
		return -1.0D;
	}

	public <E extends EntryVO> void createDataset(List<E> dataList) {
		XYSeries xy_series = null;
		XYSeriesCollection xy_collection = new XYSeriesCollection();
		Number x = null;
		Number y = null;
		if(VariableY.AVG_RESPONSE.name().equals(label_y) || VariableY.ERROR.name().equals(label_y)) {
			xy_series = new XYSeries(label_y);
			for(Object data : dataList) {
				RegressionEntryVO vo = (RegressionEntryVO)data;
				x = RegressionChart.getVariableData(vo, label_x);
				y = RegressionChart.getVariableData(vo, label_y);
				xy_series.addOrUpdate(x, y);
			}
    		xy_collection.addSeries(xy_series);
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
		    			if(str_name_prev != null && xy_series.getItemCount() > 0) {
		    	    		xy_collection.addSeries(xy_series);	 
		    			}
		    			xy_series = new XYSeries(str_name);
		    			str_name_prev = str_name;
		    		}
		    		x = RegressionChart.getVariableData(vo, label_x);
		    		y = RegressionChart.getVariableData(vo, label_y);
		    		if(x != null && y != null) {
			    		xy_series.add(x, y);
		    		}
			    }
	    		xy_collection.addSeries(xy_series);	    
			} else if(aggregation_type == AggregationType.GROUP) {
				Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.GROUP_TIME));
			    for(EntryVO data : dataList) {
			    	RegressionEntryVO vo = (RegressionEntryVO)data;
		    		String str_name = vo.getServerGroup();
		    		if(str_name_prev == null || !str_name_prev.equals(str_name)) {
		    			if(str_name_prev != null) {
		    			    x = RegressionChart.getVariableData(mergedVO, label_x);
		    			    y = RegressionChart.getVariableData(mergedVO, label_y);
				    		if(x != null && y != null) {
			    	    		xy_series.add(x, y);
				    		}
				    		if(xy_series.getItemCount() > 0) {
					    		xy_collection.addSeries(xy_series);	    
					    		dt_prev = null;
				    		}
		    			}
		    			xy_series = new XYSeries(str_name);
		    			str_name_prev = str_name;
		    		}
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo, resource_merge_type);
		    		} else {
					    x = RegressionChart.getVariableData(mergedVO, label_x);
					    y = RegressionChart.getVariableData(mergedVO, label_y);
			    		if(x != null && y != null) {
				    		xy_series.add(x, y);
			    		}
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
			    }
			    x = RegressionChart.getVariableData(mergedVO, label_x);
			    y = RegressionChart.getVariableData(mergedVO, label_y);
	    		if(x != null && y != null) {
		    		xy_series.add(x, y);
	    		}
	    		xy_collection.addSeries(xy_series);	    
			} else {
				Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.TIME));
				xy_series = new XYSeries(label_y);
				for(Object data : dataList) {
					RegressionEntryVO vo = (RegressionEntryVO)data;
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo, resource_merge_type);
		    		} else {
		    			x = RegressionChart.getVariableData(mergedVO, label_x);
		    			y = RegressionChart.getVariableData(mergedVO, label_y);
			    		if(x != null && y != null) {
				    		xy_series.add(x, y);
			    		}
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
				}
    			x = RegressionChart.getVariableData(mergedVO, label_x);
    			y = RegressionChart.getVariableData(mergedVO, label_y);
	    		if(x != null && y != null) {
		    		xy_series.add(x, y);
	    		}
	    		xy_collection.addSeries(xy_series);	    
			}
		}
	    dataset = xy_collection;
	    shape_sizes = new ShapeSize[dataset.getSeriesCount()+1];
	    for(int i = 0; i < dataset.getSeriesCount()+1; i++) {
	    	shape_sizes[i] = DEFAULT_SHAPE_SIZE;
	    }
	    equation_annotations = new XYPointerAnnotation[dataset.getSeriesCount()];
	}

	public void createChart() {		
		jfreeChart = ChartFactory.createScatterPlot(title, label_x, label_y, dataset, PlotOrientation.VERTICAL, true, true, false);

		XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
		xyPlot.setDomainPannable(true);
		xyPlot.setRangePannable(true);
		xyPlot.setDomainCrosshairVisible(false);
		xyPlot.setRangeCrosshairVisible(false);

		NumberAxis numberAxis = (NumberAxis)xyPlot.getDomainAxis();
		numberAxis.setVerticalTickLabels(true);
		numberAxis.setLowerMargin(0.0D);
		numberAxis.setUpperMargin(0.0D);
		autoDomainRange = numberAxis.getRange();
    	resetDomainRange = new Range(-2.0D, autoDomainRange.getUpperBound()+2.0D);
    	numberAxis.setRange(resetDomainRange);

		NumberAxis rangeAxis = (NumberAxis)xyPlot.getRangeAxis();
		rangeAxis.setRangeType(RangeType.POSITIVE);
		rangeAxis.setAutoRangeMinimumSize(10.0D);
		rangeAxis.setLowerMargin(0.0D);
		rangeAxis.setUpperMargin(0.0D);
		autoValueRange = rangeAxis.getRange();
	    if(resource_axis_to_100 && RegressionChart.isVariableResource(label_y)) {
	    	resetValueRange = new Range(-2.0D, Math.max(100.0D, autoValueRange.getUpperBound())+2.0D);
	    } else {
	    	resetValueRange = new Range(-2.0D, autoValueRange.getUpperBound()+2.0D);
	    }
	    rangeAxis.setRange(resetValueRange);
		
		XYItemRenderer renderer = xyPlot.getRenderer();
		int size = DEFAULT_SHAPE_SIZE.ordinal() + 1;
		Shape shape = new Ellipse2D.Double(-size, -size, size, size);
		for(int i = 0; i < xyPlot.getSeriesCount(); i++) {
			renderer.setSeriesShape(i, shape);
		}
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("[{0}] : ({1}, {2})", DF_NUMBER, DF_NUMBER));

		if(show_regression_line) {
			updateRegressionLines(true);
		}

		TextTitle title = jfreeChart.getTitle();
		if(title != null) {
			RectangleInsets padding = title.getPadding();
			double bottomPadding = Math.max(padding.getBottom(), 4.0D);
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
					clickedLegendItem(series_index, xyDataset==dataset?0:1);
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
    			selectedMinX = xyPlot.getDomainAxis().java2DToValue(sPoint.getX(), dataArea, xyPlot.getDomainAxisEdge());
    			selectedMaxY = xyPlot.getRangeAxis().java2DToValue(sPoint.getY(), dataArea, xyPlot.getRangeAxisEdge());
    			orgDomainRange = xyPlot.getDomainAxis().getRange();
    			orgValueRange = xyPlot.getRangeAxis().getRange();
        	}
			public void mouseReleased(MouseEvent e) {
				if(e.isControlDown() || !isDragging || !parent_chart.isDeleteMode() || selectedMinX >= selectedMaxX || selectedMinY >= selectedMaxY) return;
				XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
				xyPlot.getDomainAxis().setRange(orgDomainRange);
		    	xyPlot.getRangeAxis().setRange(orgValueRange);
		    	processDelete();
		    	isDragging = false;
		    	selectedMinX = selectedMaxX = selectedMinY = selectedMaxY = -1D;
			}
		});
		chartPanel.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
        		if(e.isControlDown() || !parent_chart.isDeleteMode()) return;
    			isDragging = true;
    			Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
    			XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
    			Point2D ePoint = chartPanel.translateScreenToJava2D(e.getPoint());
    			selectedMaxX = xyPlot.getDomainAxis().java2DToValue(ePoint.getX(), dataArea, xyPlot.getDomainAxisEdge());
    			selectedMinY = xyPlot.getRangeAxis().java2DToValue(ePoint.getY(), dataArea, xyPlot.getRangeAxisEdge());
			}
		});
		NumberAxis xAxis = (NumberAxis)((XYPlot)jfreeChart.getPlot()).getDomainAxis();
		xAxis.addChangeListener(new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent e) {
            	Range r = ((NumberAxis)e.getSource()).getRange();
            	if(r.getLowerBound() == autoDomainRange.getLowerBound() && r.getUpperBound() == autoDomainRange.getUpperBound()) {
            		xAxis.setRange(resetDomainRange, true, false);
            	}
            }
        });
		NumberAxis yAxis = (NumberAxis)((XYPlot)jfreeChart.getPlot()).getRangeAxis();
		yAxis.addChangeListener(new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent e) {
            	Range r = ((NumberAxis)e.getSource()).getRange();
            	if(Math.floor(r.getLowerBound()) == Math.floor(autoValueRange.getLowerBound()) && r.getUpperBound() == autoValueRange.getUpperBound()) {
            		yAxis.setRange(resetValueRange, true, false);
            	}
            }
        });
	}

	private void clickedLegendItem(int series_index, int renderer_index) {
		XYPlot plot = (XYPlot)jfreeChart.getPlot();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(renderer_index);
		Boolean visible = (renderer_index==0) ? renderer.getSeriesShapesVisible(series_index) : renderer.getSeriesLinesVisible(series_index);
		if(renderer_index == 0) {
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
		} else {
			if(visible == null || visible == Boolean.TRUE) {
				float width = ((BasicStroke)renderer.getSeriesStroke(series_index)).getLineWidth();
				if(width == DEFAULT_LINE_WIDTH) {
					renderer.setSeriesStroke(series_index, new BasicStroke(BOLD_LINE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{6.0f}, 0.0f));
					plot.removeAnnotation(equation_annotations[series_index]);
					equation_annotations[series_index].setFont(new Font("Arial", Font.BOLD, equation_annotations[series_index].getFont().getSize()));
					Color color = (Color)equation_annotations[series_index].getBackgroundPaint();
					equation_annotations[series_index].setBackgroundPaint(new Color(color.getRed(), color.getGreen(), color.getBlue(), 200));
					plot.addAnnotation(equation_annotations[series_index]);
				} else {
					renderer.setSeriesLinesVisible(series_index, false);
					plot.removeAnnotation(equation_annotations[series_index]);
				}
			} else {
				renderer.setSeriesLinesVisible(series_index, true);
				renderer.setSeriesStroke(series_index, new BasicStroke(DEFAULT_LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{4.0f}, 0.0f));
				equation_annotations[series_index].setFont(new Font("Arial", Font.PLAIN, equation_annotations[series_index].getFont().getSize()));
				Color color = (Color)equation_annotations[series_index].getBackgroundPaint();
				equation_annotations[series_index].setBackgroundPaint(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
				plot.addAnnotation(equation_annotations[series_index]);
			}			
		}		
		renderer.setDrawSeriesLineAsPath(true);		
	}

	private void updateRegressionLines(boolean refresh) {
		XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();

		Boolean[] regVisible = null; 
		if(xyPlot.getDataset(1) != null) {
			XYSeriesCollection regSeriesCollection = (XYSeriesCollection)xyPlot.getDataset(1);
			//previous state
			if(!refresh) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)xyPlot.getRenderer(1);
				regVisible = new Boolean[regSeriesCollection.getSeriesCount()];
				for(int idx_series = 0; idx_series < regSeriesCollection.getSeriesCount(); idx_series++) {
					regVisible[idx_series] = renderer.getSeriesLinesVisible(idx_series);
				}
			}
			// clear regression
			regressions.clear();
			regSeriesCollection.removeAllSeries();
			xyPlot.clearAnnotations();
		}
		
		// make data for regression
		double maxX = Double.MIN_VALUE;
		XYSeriesCollection seriesCollection = (XYSeriesCollection)dataset;
		for(int idx_series = 0; idx_series < seriesCollection.getSeriesCount(); idx_series++) {
			XYSeries series = seriesCollection.getSeries(idx_series);
			if(series.getKey().equals("Removed")) continue;
			SimpleRegression regression = new SimpleRegression();
			for(int i = 0; i < series.getItemCount(); i++) {
				XYDataItem item = series.getDataItem(i);
				maxX = Math.max(maxX, item.getXValue());
				regression.addData(item.getXValue(), item.getYValue());
			}
			regressions.add(regression);
		}
		
		// draw regression lines by data
		XYSeriesCollection regDataset = new XYSeriesCollection();
		XYLineAndShapeRenderer regRenderer = new XYLineAndShapeRenderer(true, false);
		DrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
		if(regression_type == RegressionType.LINEAR) {
			for(int series = 0; series < regressions.size(); series++) {
				SimpleRegression regression = regressions.get(series);
				LineFunction2D lineFunction2D = new LineFunction2D(regression.getIntercept(), regression.getSlope());
				double start = 0;
				double end = maxX;
				XYSeries regSeries = DatasetUtilities.sampleFunction2DToSeries(lineFunction2D, start, end, 2, dataset.getSeriesKey(series)+"(Regression)");
				regDataset.addSeries(regSeries);
				regRenderer.setSeriesPaint(series, drawingSupplier.getNextPaint());
				regRenderer.setSeriesStroke(series, new BasicStroke(DEFAULT_LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{4.0f}, 0.0f));
				boolean visible = (regVisible == null || regVisible[series] == null || regVisible[series] == Boolean.TRUE);
				regRenderer.setSeriesLinesVisible(series, visible);
				if(show_regression_equation) {
					double eq_x = (start+end)/2 + (start+end)/2/regressions.size()*series;
					double eq_y = regression.predict(eq_x);
					Range screenRangeY = resetValueRange;
					double center = (screenRangeY.getUpperBound()+screenRangeY.getLowerBound()) / 2;
					int angle = eq_y < center ? 225 : 135;
					StringBuffer eq = new StringBuffer();
					eq.append(" Y = ");
					eq.append(String.format("%.5f", regression.getSlope())).append("X");
					if(regression.hasIntercept()) {
						eq.append(" + ").append(String.format("%.3f", regression.getIntercept()));
					}
					eq.append(", R² = ").append(String.format("%1.3f", regression.getRSquare()));
					XYPointerAnnotation annotation = new XYPointerAnnotation(eq.toString(), eq_x, eq_y, Math.toRadians(angle));
				    annotation.setLabelOffset(1.0D);					    
				    annotation.setTextAnchor(angle==225 ? TextAnchor.BOTTOM_RIGHT : TextAnchor.TOP_RIGHT);
				    Color color = (Color)regRenderer.getSeriesPaint(series);
				    annotation.setBackgroundPaint(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
				    annotation.setOutlineVisible(false);
				    annotation.setFont(new Font("Arial", Font.PLAIN, annotation.getFont().getSize()+1));
				    if(visible) {
				    	xyPlot.addAnnotation(annotation);
				    }
				    equation_annotations[series] = annotation;
				}
			}
			xyPlot.setDataset(1, regDataset);
			xyPlot.setRenderer(1, regRenderer);
		    xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		}
	}
	
	private void processDelete() {
		Map<String,List<XYDataItem>> selectedItems = getSelectedItems(selectedMinX, selectedMaxX, selectedMinY, selectedMaxY, dataset);
		if(selectedItems.size() > 0) {
			nominateItems(selectedItems, dataset);
			parent_chart.nominateItemsPropagationByAnalysis(selectedItems);
		}
	}

    private Map<String,List<XYDataItem>> getSelectedItems(double minX, double maxX, double minY, double maxY, XYDataset dataset) {
    	Map<String,List<XYDataItem>> result = new HashMap<String,List<XYDataItem>>();
    	for(int idx = 0; idx < dataset.getSeriesCount(); idx++) {
    		XYSeries series = ((XYSeriesCollection)dataset).getSeries(idx);
    		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(0);
    		Boolean visible = renderer.getSeriesShapesVisible(idx);
    		if(!series.getKey().equals("Removed") && (visible == null || visible == Boolean.TRUE)) {
    			List<XYDataItem> selectedList = new ArrayList<XYDataItem>();
	    		for(Object itemObj : series.getItems()) {
	    			XYDataItem item = (XYDataItem)itemObj;
	    			double xValue = item.getX().doubleValue();
	    			double yValue = item.getY().doubleValue();
	    			if(xValue >= minX && xValue <= maxX && yValue >= minY && yValue <= maxY) {
	    				selectedList.add(item);
	    			}
	    		}
	    		if(selectedList.size() > 0) {
	    			result.put((String)dataset.getSeriesKey(idx), selectedList);
	    		}
    		}
    	}
    	return result;
    }
    
    private void nominateItems(Map<String,List<XYDataItem>> mapToRemove, XYDataset dataset) {
    	XYSeriesCollection seriesCollection = (XYSeriesCollection)dataset;
    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(0);
    	for(String key : mapToRemove.keySet()) {
    		XYSeries series = seriesCollection.getSeries(key);
    		XYSeries toRemoveSeries = new XYSeries(key+"~toRemove");
    		series.setNotify(false);
    		toRemoveSeries.setNotify(false);
    		for(int idx_item = series.getItemCount()-1; idx_item >= 0; idx_item--) {
    			XYDataItem item = series.getDataItem(idx_item);
    			for(XYDataItem itemToRemove : mapToRemove.get(key)) {
    				if(item.equals(itemToRemove)) {
    	    			series.remove(idx_item);
    	    			toRemoveSeries.addOrUpdate(item);
    	    			break;
    				}
    			}
    		}
    		series.setNotify(true);
    		toRemoveSeries.setNotify(true);
    		if(toRemoveSeries.getItemCount() > 0) {
    			seriesCollection.addSeries(toRemoveSeries);
    			renderer.setSeriesPaint(dataset.getSeriesCount()-1, Color.BLACK);
    			renderer.setSeriesShape(dataset.getSeriesCount()-1, renderer.getSeriesShape(seriesCollection.indexOf(key)));
    			renderer.setSeriesVisibleInLegend(dataset.getSeriesCount()-1, false);
    		}
    	}
    }

    public void nominateItems(Map<String,List<CustomTimeSeriesDataItem>> mapToRemove) {
    	XYSeriesCollection seriesCollection = (XYSeriesCollection)dataset;
    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(0);
    	for(String key : mapToRemove.keySet()) {
    		XYSeries series = seriesCollection.getSeries(key);
    		XYSeries toRemoveSeries = new XYSeries(key+"~toRemove");
    		series.setNotify(false);
    		toRemoveSeries.setNotify(false);
    		for(CustomTimeSeriesDataItem itemToRemove : mapToRemove.get(key)) {
        		for(int idx_item = series.getItemCount()-1; idx_item >= 0; idx_item--) {
        			XYDataItem item = series.getDataItem(idx_item);
    				if(itemToRemove.getValue().equals(item.getY())) {
    					series.remove(idx_item);
    	    			toRemoveSeries.addOrUpdate(item);
    	    			break;
    				}
        		}
    		}
    		series.setNotify(true);
    		toRemoveSeries.setNotify(true);
    		if(toRemoveSeries.getItemCount() > 0) {
    			seriesCollection.addSeries(toRemoveSeries);
    			renderer.setSeriesPaint(dataset.getSeriesCount()-1, Color.BLACK);
    			renderer.setSeriesShape(dataset.getSeriesCount()-1, renderer.getSeriesShape(seriesCollection.indexOf(key)));
    			renderer.setSeriesVisibleInLegend(dataset.getSeriesCount()-1, false);
    		}
    	}
    }

	public void removeOrRestoreItems(boolean remove) {
    	XYSeriesCollection seriesCollection = (XYSeriesCollection)dataset;
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)((XYPlot)jfreeChart.getPlot()).getRenderer(0);
    	int removedSeriesIndex = seriesCollection.getSeriesIndex("Removed");
    	XYSeries removedSeries = removedSeriesIndex < 0 ? new XYSeries("Removed") : seriesCollection.getSeries(removedSeriesIndex);
    	removedSeries.setNotify(false);
		for(int idx_series = seriesCollection.getSeriesCount()-1; idx_series >= 0 ; idx_series--) {
			XYSeries series = seriesCollection.getSeries(idx_series);
			String key = (String)series.getKey();
			int idx = key.indexOf("~toRemove");
			if(idx < 0) break;
			XYSeries orgSeries = seriesCollection.getSeries(key.substring(0, idx));
    		series.setNotify(false);
    		orgSeries.setNotify(false);
			for(int i = series.getItemCount()-1; i >= 0; i--) {
				XYDataItem item = series.remove(i);
				if(remove) {
					removedSeries.addOrUpdate(item);
				} else {
					orgSeries.addOrUpdate(item);
				}
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
		if(remove) {
			updateRegressionLines(false);
		}
	}
    
}
