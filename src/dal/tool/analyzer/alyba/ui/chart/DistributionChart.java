package dal.tool.analyzer.alyba.ui.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.eclipse.swt.SWT;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.DateRange;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.output.vo.TransactionEntryVO;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.chart.extension.Distribution.Boundary;
import dal.tool.analyzer.alyba.ui.chart.extension.Distribution.ValueBoundaray;
import dal.tool.analyzer.alyba.ui.chart.extension.Distribution.ValueRange;
import dal.tool.analyzer.alyba.ui.chart.extension.MultiLineXYPointerAnnotation;
import dal.tool.analyzer.alyba.ui.chart.extension.SuccessErrorCountToolTipGenerator;
import dal.tool.analyzer.alyba.ui.chart.extension.TotalCountItemLabelGenerator;
import dal.util.DateUtil;
import dal.util.NumberUtil;
import dal.util.db.ObjectDBUtil;

public abstract class DistributionChart extends KeyValueChart {

	protected static final Class<?> DATA_CLASS = TransactionEntryVO.class;
	protected static final ShapeSize DEFAULT_SHAPE_SIZE = ShapeSize.Large;

	protected String label_x = "X";
	protected String label_y = "Y";
	protected XYDataset dataset;
	protected boolean use_date_axis = false;
	protected String date_format = "yyyy.MM.dd HH:mm";
	protected DateTickUnit tick_unit = null;
	protected ShapeSize shape_size = DEFAULT_SHAPE_SIZE;
	protected boolean show_regression_line = false;
	protected boolean show_regression_equation = true;
	protected SimpleRegression regression = null;
	protected Double[] boundary_values = { 0D };

	public DistributionChart(Type chartType) {
		super(chartType);
	}

	public DistributionChart(Type chartType, String title) {
		super(chartType, title);
	}

	public DistributionChart(Type chartType, String title, String kv_label_name, String kv_label_value, String scatter_label_x, String scatter_label_y) {
		super(chartType, title, kv_label_name, kv_label_value);
		setScatterPlotLabel(scatter_label_x, scatter_label_y);
	}

	public String getLableX() {
		return label_x;
	}
	
	public String getLableY() {
		return label_y;
	}
	
	public boolean getUseDateAxis() {
		return use_date_axis;
	}

	public String getDateFormat() {
		return date_format;
	}
	
	public DateTickUnit getTickUnit() {
		return tick_unit;
	}
	
	public ShapeSize getShapeSize() {
		return shape_size;
	}
	
	public int getShapeSizeNumber() {
		return shape_size.ordinal()+1;
	}
	
	public boolean getShowRegressionLine() {
		return show_regression_line;
	}

	public boolean getShowRegressionEquation() {
		return show_regression_equation;
	}
	
	public Double[] getBoundaryValues() {
		return boundary_values;
	}

	public void setScatterPlotLabel(String label_x, String label_y) {
		setLabelX(label_x);
		setLabelY(label_y);
	}

	public void setLabelX(String label_x) {
		this.label_x = label_x;
	}

	public void setLabelY(String label_y) {
		this.label_y = label_y;
	}
	
	public void setUseDateAxis(boolean use_date_axis) {
		this.use_date_axis = use_date_axis;
	}
	
	public void setDateFormat(String date_format) {
		this.date_format = date_format;
	}
	
	public void setTickUnit(DateTickUnit tick_unit) {
		this.tick_unit = tick_unit;
	}

	public void setShapeSize(ShapeSize shape_size) {
		this.shape_size = shape_size;
	}
	
	public void setShapeSizeNumber(int shape_size) {
		this.shape_size = ShapeSize.values()[shape_size-1];
	}
	
	public void setShowRegressionLine(boolean show_regression_line) {
		this.show_regression_line = show_regression_line;
	}

	public void setShowRegressionEquation(boolean show_regression_equation) {
		this.show_regression_equation = show_regression_equation;
	}

	public void setBoundaryValues(Double[] boundary_values) {
		this.boundary_values = boundary_values;
	}
	
	public Type[] getSupportChartTypes() {
		return new Type[] { Type.VerticalBar, Type.HorizontalBar, Type.Pie, Type.ScatterPlot };
	}
	
	public Type getDefaultChartType() {
		return Type.ScatterPlot;
	}

	public void createChart() {		
		if(chartType == Type.ScatterPlot) {
			jfreeChart = ChartFactory.createScatterPlot(title, label_x, label_y, dataset, PlotOrientation.VERTICAL, true, true, false);
			XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
			xyPlot.setDomainPannable(true);
			xyPlot.setRangePannable(true);
			xyPlot.setDomainCrosshairVisible(true);
			xyPlot.setRangeCrosshairVisible(true);
			if(use_date_axis) {
				DateAxis dateAxis = new DateAxis("Time");
			    if(tick_unit == null) {
			    	dateAxis.setDateFormatOverride(new SimpleDateFormat(date_format));
				    dateAxis.setAutoTickUnitSelection(true);
			    } else {
				    dateAxis.setTickUnit(tick_unit);
			    }
			    dateAxis.setVerticalTickLabels(true);
			    dateAxis.setLabelFont(xyPlot.getRangeAxis(0).getLabelFont());
			    dateAxis.setTickLabelFont(xyPlot.getRangeAxis(0).getTickLabelFont());
			    setTimeRangeFromSetting(dateAxis);
			    xyPlot.setDomainAxis(dateAxis);
			} else {		
				NumberAxis numberAxis = (NumberAxis)xyPlot.getDomainAxis();
				numberAxis.setVerticalTickLabels(true);
				xyPlot.setDomainAxis(numberAxis);
			}
			XYItemRenderer renderer = xyPlot.getRenderer();
			int size = shape_size.ordinal() + 1;
			Shape shape = new Ellipse2D.Double(-size, -size, size, size);
			renderer.setSeriesShape(0, shape);
			StandardXYToolTipGenerator tooltipGenerator = new StandardXYToolTipGenerator() {
				private static final long serialVersionUID = 1L;
				public String generateToolTip(XYDataset dataset, int series, int item) {
					return getTooltipText(dataset, series, item);
				}
			};
			renderer.setBaseToolTipGenerator(tooltipGenerator);
			if(show_regression_line) {
				LineFunction2D lineFunction2D = new LineFunction2D(regression.getIntercept(), regression.getSlope());
				double start = dataset.getXValue(0, 0);
				double end = dataset.getXValue(0, dataset.getItemCount(0)-1);
				XYDataset regDataset = DatasetUtilities.sampleFunction2D(lineFunction2D, start, end, 2, "Linear Regression Line");
				xyPlot.setDataset(1, regDataset);
				XYLineAndShapeRenderer regRender = new XYLineAndShapeRenderer(true, false);
				regRender.setSeriesPaint(0, Color.BLUE);
				xyPlot.setRenderer(1, regRender);
				if(show_regression_equation) {
					double eq_x = (start+end) / 2;
					double eq_y = regression.predict(eq_x);
					Range screenRangeY = xyPlot.getRangeAxis().getRange();
					double center = (screenRangeY.getUpperBound()+screenRangeY.getLowerBound()) / 2;
					int angle = eq_y < center ? 270 : 90;
					StringBuffer eq = new StringBuffer();				
					eq.append(" Y = αX + β  (");
					eq.append("α = ").append(String.format("%.5e", regression.getSlope()));
					if(regression.hasIntercept()) {
						eq.append(", β = ").append(String.format("%.5e", regression.getIntercept()));
					}
					eq.append(", R² = ").append(String.format("%1.5e", regression.getRSquare())).append(") ");
				    XYPointerAnnotation annotation = new XYPointerAnnotation(eq.toString(), eq_x, eq_y, Math.toRadians(angle));
				    annotation.setLabelOffset(4.0D);
				    annotation.setTextAnchor(angle==270 ? TextAnchor.BOTTOM_CENTER : TextAnchor.TOP_CENTER);
				    annotation.setBackgroundPaint(new Color(0, 0, 255, 63));
				    annotation.setOutlineVisible(false);
				    annotation.setFont(new Font("Arial", Font.ITALIC, annotation.getFont().getSize()+3));
				    xyPlot.addAnnotation(annotation);
				}
			}
			xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		} else if(chartType == Type.Pie) {
		    jfreeChart = ChartFactory.createPieChart(title, pieDataset, true, true, false);
		    jfreeChart.setPadding(new RectangleInsets(0, 20, 20, 20));
		    PiePlot piePlot = (PiePlot)jfreeChart.getPlot();
		    piePlot.setToolTipGenerator(new StandardPieToolTipGenerator("{0}: ({1}, {2})", DF_NUMBER, DF_PERCENT));
		    if(show_item_label) {
		    	piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1}, {2})", DF_NUMBER, DF_PERCENT));
		    } else {
		    	piePlot.setLabelGenerator(null);
		    }
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
		}
		TextTitle title = jfreeChart.getTitle();
		RectangleInsets padding = title.getPadding();
		double bottomPadding = Math.max(padding.getBottom(), 4.0D);
		title.setPadding(padding.getTop(), padding.getLeft(), bottomPadding, padding.getRight());
	}
	
	public void afterCreateChartPanel(final ChartPanel chartPanel) {
		if(chartType == Type.ScatterPlot) {
			chartPanel.addChartMouseListener(new ChartMouseListener() {
				public void chartMouseMoved(ChartMouseEvent event) {}
				public void chartMouseClicked(ChartMouseEvent event) {
					ChartEntity entity = event.getEntity();
					if(entity instanceof XYItemEntity) {
						XYItemEntity itemEntity = (XYItemEntity)entity;
						showAnnotation(itemEntity);
					} else if(entity instanceof LegendItemEntity) {
						LegendItemEntity itemEntity = (LegendItemEntity)entity;
						XYDataset xyDataset = (XYDataset)itemEntity.getDataset();
						int series_index = xyDataset.indexOf(itemEntity.getSeriesKey());
						XYPlot plot = (XYPlot)jfreeChart.getPlot();
						XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(0);
						Boolean visible = renderer.getSeriesShapesVisible(series_index);
						renderer.setSeriesShapesVisible(series_index, !(visible==null || visible==Boolean.TRUE));
					}
				}
			});
		}
	}
	
	public void setTimeRangeFromSetting(DateAxis dateAxis) {
		Date from, to;		
		ObjectDBUtil db = null;
		EntityManager em = null;
		try {
			db = ObjectDBUtil.getInstance();
			em = db.createEntityManager();
			SummaryEntryVO summary = db.select(em, SummaryEntryVO.class);
			SettingEntryVO setting = db.select(em, SettingEntryVO.class);
			if(setting.isDateFilterEnable()) {
				from = summary.getFilterFromTime();
				to = summary.getFilterToTime();
			} else {
				from = summary.getFirstRequestTime();
				to = summary.getLastRequestTime();
			}
		    dateAxis.setRange(from.getTime(), to.getTime());
		} catch(Exception e) {
			Logger.debug("Failed to set time range from DB.");
			Logger.error(e);
		} finally {
			if(db != null && em != null) {
				db.close(em);
			}
		}
	}
	
	public int getAngleForAnnotation(XYPlot xyPlot, double x, double y) {
		DateRange range_x = (DateRange)((DateAxis)xyPlot.getDomainAxis()).getRange();
		Range range_y = ((NumberAxis)xyPlot.getRangeAxis()).getRange();
		double center_x = (range_x.getLowerMillis() + range_x.getUpperMillis()) / 2;
		double center_y = (range_y.getLowerBound() + range_y.getUpperBound()) / 2;
		if(x <= center_x && y >= center_y) {
			return 45;
		} else if(x >= center_x && y >= center_y) {
			return 135;
		} else if(x >= center_x && y <= center_y) {
			return 225;
		} else if(x <= center_x && y <= center_y) {
			return 315;
		} else {
			return 0;
		}
	}
	
	public String getTooltipText(XYDataset dataset, int series, int item) {
		Date dt = new Date((long)dataset.getXValue(series, item));
		long val = (long)dataset.getYValue(series, item);
		String str_dt = DateUtil.dateToString(dt, DateUtil.SDF_DATETIME);
		String str_val = NumberUtil.numberToString(val, NumberUtil.DF_NO_DOT_THOUSAND);
		return "\"" + str_dt + "\": " + str_val;
	}
	
	public <E extends EntryVO> void createDistributionDataset(List<E> dataList) {
		List<ValueRange> distRange = getDistributionRanges();
		if(chartType == Type.Pie) {
			pieDataset = new DefaultPieDataset();
		} else {
			categoryDataset = new DefaultCategoryDataset();
		}		
    	for(EntryVO data : dataList) {
    		for(ValueRange rangeItem : distRange) {
    			if(rangeItem.isValueInRange(getDistributionValue(data))) {
    				rangeItem.count++;
    				if(data instanceof TransactionEntryVO && ((TransactionEntryVO)data).isResponseError()) {
    					rangeItem.error++;
    				}
    			}
    		}    		
    	}
		for(ValueRange rangeItem : distRange) {
			if(chartType == Type.Pie) {
				if(rangeItem.count > 0) {
					pieDataset.setValue(rangeItem.getRangeString(), rangeItem.count);
				}
			} else {
				if(rangeItem.count-rangeItem.error > 0) {
					categoryDataset.addValue(rangeItem.count-rangeItem.error, "Success", rangeItem.getRangeString());
				}
				if(rangeItem.error > 0) {
					categoryDataset.addValue(rangeItem.error, "Error", rangeItem.getRangeString());
				}
			}
		}    		
	}

	public List<ValueRange> getDistributionRanges() {
		return DistributionChart.getDistributionRanges(getBoundaryValues());
	}
	
	public static List<ValueRange> getDistributionRanges(Double[] boundary_values) {
		if(boundary_values == null || boundary_values.length < 1) {
			return new ArrayList<ValueRange>();
		}
	    Set<Double> sortedSet = new TreeSet<Double>(Arrays.asList(boundary_values));
	    Double[] sortedBoundaryValues = new Double[sortedSet.size()];
	    sortedSet.toArray(sortedBoundaryValues);
		List<ValueRange> ranges = new ArrayList<ValueRange>(sortedBoundaryValues.length + 1);
		for(int i = 0; i < sortedBoundaryValues.length; i++) {
			if(i == 0) {
				ranges.add(new ValueRange(new ValueBoundaray(Boundary.Less, sortedBoundaryValues[i])));
			} else {
				ranges.add(new ValueRange(new ValueBoundaray(Boundary.GreaterOrEqual, sortedBoundaryValues[i-1]), new ValueBoundaray(Boundary.Less, sortedBoundaryValues[i])));
			}
		}
		ranges.add(new ValueRange(new ValueBoundaray(Boundary.GreaterOrEqual, sortedBoundaryValues[sortedBoundaryValues.length-1])));
		return ranges;
	}

	public void showAnnotation(XYItemEntity entity) {		
		XYSeries series = ((XYSeriesCollection)dataset).getSeries(entity.getSeriesIndex());
		XYDataItem item = series.getDataItem(entity.getItem());
		Date dt = new Date((long)item.getXValue());
		long val = (long)item.getYValue();
		String annotation_text = getAnnotationText(item);
		XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)xyPlot.getRenderer(1);
		MultiLineXYPointerAnnotation annotation = null;
		if(renderer != null) {
			List<?> anno = (List<?>)renderer.getAnnotations();
			annotation = (anno != null && anno.size() > 0) ? (MultiLineXYPointerAnnotation)anno.get(0) : null;
			if(annotation != null && annotation.getX() == dt.getTime() && annotation.getY() == val) {
				renderer.removeAnnotation(annotation);
				return;
			}
		}
		renderer = new XYLineAndShapeRenderer(true, true);
		renderer.setSeriesPaint(0, Color.BLACK);
		int angle = getAngleForAnnotation(xyPlot, dt.getTime(), val);
		annotation = new MultiLineXYPointerAnnotation(annotation_text, dt.getTime(), val, Math.toRadians(angle));
	    annotation.setFont(new Font("Arial", SWT.NONE, annotation.getFont().getSize()+1));
	    annotation.setTextAnchor(getTextAnchorByAngle(angle));
	    annotation.setBackgroundPaint(Color.WHITE);
	    renderer.addAnnotation(annotation);
	    xyPlot.setRenderer(1, renderer);
	}
	
	public String getAnnotationText(XYDataItem item) {
		Date dt = new Date((long)item.getXValue());
		long val = (long)item.getYValue();
		return "\"" + DateUtil.dateToString(dt, DateUtil.SDF_DATETIME) + "\": " + NumberUtil.numberToString(val, NumberUtil.DF_NO_DOT_THOUSAND);
	}

	public abstract Double[] getDefaultBoundaryValues();	
	public abstract double getDistributionValue(EntryVO vo);	
	
}
