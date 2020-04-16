package dal.tool.analyzer.alyba.ui.chart.regression;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.RangeType;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
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
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableX;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.VariableY;

public class RegressionAnalysisChart extends DistributionChart {

	protected static final DecimalFormat DF_NUMBER = new DecimalFormat("#.##");
	protected static final Class<?> DATA_CLASS = RegressionEntryVO.class;
	protected static final ShapeSize DEFAULT_SHAPE_SIZE = ShapeSize.Smallest;
	protected static final ShapeSize BOLD_SHAPE_SIZE = ShapeSize.Large;
	protected static final float DEFAULT_LINE_WIDTH = 2.0F;
	protected static final float BOLD_LINE_WIDTH = 4.0F;
	
	protected AggregationType aggregation_type = AggregationType.NAME;
	protected RegressionType regression_type = RegressionType.LINEAR;
	protected boolean resource_axis_to_100 = false;

	protected List<SimpleRegression> regressions = new ArrayList<SimpleRegression>();	
	protected ShapeSize[] shape_sizes;
	protected XYPointerAnnotation[] equation_annotations;

	public RegressionAnalysisChart(String title, VariableX varX, VariableY varY, AggregationType aggregationType, RegressionType regressionType, boolean showRegressionLine, boolean showRegressionEquation, boolean axisYto100) {
		super(Type.ScatterPlot, title);
    	setLabelX(varX.name());
    	setLabelY(varY.name());
	    setDateFormat("yyyy.MM.dd HH:mm");
	    this.aggregation_type = aggregationType;
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
		SimpleRegression regression = null;
		if(VariableY.AVG_RESPONSE.name().equals(label_y) || VariableY.ERROR.name().equals(label_y)) {
			xy_series = new XYSeries(label_y);
    		regression = new SimpleRegression();
			for(Object data : dataList) {
				RegressionEntryVO vo = (RegressionEntryVO)data;
				x = RegressionChart.getVariableData(vo, label_x);
				y = RegressionChart.getVariableData(vo, label_y);
				xy_series.addOrUpdate(x, y);
	    		regression.addData(x.doubleValue(), y.doubleValue());
			}
    		xy_collection.addSeries(xy_series);
    		regressions.add(regression);
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
		    	    		xy_collection.addSeries(xy_series);	 
		    	    		regressions.add(regression);
		    			}
		    			xy_series = new XYSeries(str_name);
			    		regression = new SimpleRegression();
		    			str_name_prev = str_name;
		    		}
		    		x = RegressionChart.getVariableData(vo, label_x);
		    		y = RegressionChart.getVariableData(vo, label_y);
		    		if(x != null && y != null) {
			    		xy_series.add(x, y);
			    		regression.addData(x.doubleValue(), y.doubleValue());
		    		}
			    }
	    		xy_collection.addSeries(xy_series);	    
	    		regressions.add(regression);
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
			    	    		regression.addData(x.doubleValue(), y.doubleValue());
				    		}
				    		xy_collection.addSeries(xy_series);	    
				    		regressions.add(regression);
		    				dt_prev = null;
		    			}
		    			xy_series = new XYSeries(str_name);
			    		regression = new SimpleRegression();
		    			str_name_prev = str_name;
		    		}
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo);
		    		} else {
					    x = RegressionChart.getVariableData(mergedVO, label_x);
					    y = RegressionChart.getVariableData(mergedVO, label_y);
			    		if(x != null && y != null) {
				    		xy_series.add(x, y);
				    		regression.addData(x.doubleValue(), y.doubleValue());
			    		}
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
			    }
			    x = RegressionChart.getVariableData(mergedVO, label_x);
			    y = RegressionChart.getVariableData(mergedVO, label_y);
	    		if(x != null && y != null) {
		    		xy_series.add(x, y);
		    		regression.addData(x.doubleValue(), y.doubleValue());
	    		}
	    		xy_collection.addSeries(xy_series);	    
	    		regressions.add(regression);
			} else {
				Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.TIME));
				xy_series = new XYSeries(label_y);
	    		regression = new SimpleRegression();
				for(Object data : dataList) {
					RegressionEntryVO vo = (RegressionEntryVO)data;
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo);
		    		} else {
		    			x = RegressionChart.getVariableData(mergedVO, label_x);
		    			y = RegressionChart.getVariableData(mergedVO, label_y);
			    		if(x != null && y != null) {
				    		xy_series.add(x, y);
				    		regression.addData(x.doubleValue(), y.doubleValue());
			    		}
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
				}
    			x = RegressionChart.getVariableData(mergedVO, label_x);
    			y = RegressionChart.getVariableData(mergedVO, label_y);
	    		if(x != null && y != null) {
		    		xy_series.add(x, y);
		    		regression.addData(x.doubleValue(), y.doubleValue());
	    		}
	    		xy_collection.addSeries(xy_series);	    
	    		regressions.add(regression);
			}
		}
	    dataset = xy_collection;
	    shape_sizes = new ShapeSize[dataset.getSeriesCount()];
	    for(int i = 0; i < dataset.getSeriesCount(); i++) {
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
		numberAxis.setRange(0, numberAxis.getRange().getUpperBound());
		xyPlot.setDomainAxis(numberAxis);

		NumberAxis rangeAxis = (NumberAxis)xyPlot.getRangeAxis();
		NumberFormat formatter = NumberFormat.getIntegerInstance();
		rangeAxis.setNumberFormatOverride(formatter);			
		rangeAxis.setRangeType(RangeType.POSITIVE);
		rangeAxis.setAutoRangeMinimumSize(10.0D);
	    if(resource_axis_to_100 && RegressionChart.isVariableResource(label_y)) {
	    	rangeAxis.setRange(0.0D, 100.0D);
	    }
		
		XYItemRenderer renderer = xyPlot.getRenderer();
		int size = DEFAULT_SHAPE_SIZE.ordinal() + 1;
		Shape shape = new Ellipse2D.Double(-size, -size, size, size);
		for(int i = 0; i < xyPlot.getSeriesCount(); i++) {
			renderer.setSeriesShape(i, shape);
		}
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("[{0}] : ({1}, {2})", DF_NUMBER, DF_NUMBER));
		
		if(show_regression_line) {
			XYSeriesCollection regDataset = new XYSeriesCollection();
			XYLineAndShapeRenderer regRenderer = new XYLineAndShapeRenderer(true, false);
			DrawingSupplier drawingSupplier = new DefaultDrawingSupplier();
			if(regression_type == RegressionType.LINEAR) {
				for(int series = 0; series < regressions.size(); series++) {
					SimpleRegression regression = regressions.get(series);
					LineFunction2D lineFunction2D = new LineFunction2D(regression.getIntercept(), regression.getSlope());
					double start = 0;
					double end = numberAxis.getRange().getUpperBound();
					XYSeries regSeriesDataset = DatasetUtilities.sampleFunction2DToSeries(lineFunction2D, start, end, 2, dataset.getSeriesKey(series)+"(Regression)");
					regDataset.addSeries(regSeriesDataset);
					regRenderer.setSeriesPaint(series, drawingSupplier.getNextPaint());
					regRenderer.setSeriesStroke(series, new BasicStroke(DEFAULT_LINE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{4.0f}, 0.0f));
					if(show_regression_equation) {
						double eq_x = (start+end)/2 + (start+end)/2/regressions.size()*series;
						double eq_y = regression.predict(eq_x);
						StringBuffer eq = new StringBuffer();
						eq.append(" Y = ");
						eq.append(String.format("%.5f", regression.getSlope())).append("X");
						if(regression.hasIntercept()) {
							eq.append(" + ").append(String.format("%.3f", regression.getIntercept()));
						}
						eq.append(", R©÷ = ").append(String.format("%1.3f", regression.getRSquare()));
						XYPointerAnnotation annotation = new XYPointerAnnotation(eq.toString(), eq_x, eq_y-0.5, Math.toRadians(225));
					    annotation.setLabelOffset(1.0D);					    
					    annotation.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
					    Color color = (Color)regRenderer.getSeriesPaint(series);
					    annotation.setBackgroundPaint(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
					    annotation.setOutlineVisible(false);
					    annotation.setFont(new Font("Arial", Font.PLAIN, annotation.getFont().getSize()+1));
					    xyPlot.addAnnotation(annotation);
					    equation_annotations[series] = annotation;
					}
				}
				xyPlot.setDataset(1, regDataset);
				xyPlot.setRenderer(1, regRenderer);
			    xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			}
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
	}

	public void clickedLegendItem(int series_index, int renderer_index) {
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

}
