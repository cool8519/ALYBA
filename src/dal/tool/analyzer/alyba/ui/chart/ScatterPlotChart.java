package dal.tool.analyzer.alyba.ui.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.eclipse.swt.SWT;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.DateRange;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.output.vo.TransactionEntryVO;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.chart.extension.MultiLineXYPointerAnnotation;
import dal.util.DateUtil;
import dal.util.NumberUtil;
import dal.util.db.ObjectDBUtil;

public abstract class ScatterPlotChart extends Chart {

	protected static final Class<?> DATA_CLASS = TransactionEntryVO.class;
	protected static final ShapeSize DEFAULT_SHAPE_SIZE = ShapeSize.Large;

	protected String label_x = "X";
	protected String label_y = "Y";
	protected XYDataset dataset;
	protected boolean use_date_axis = false;
	protected String date_format = "yyyy.MM.dd HH:mm";
	protected DateTickUnit tick_unit = null;
	protected ShapeSize shape_size = DEFAULT_SHAPE_SIZE;
	protected boolean show_regression_linear = false;
	protected boolean show_regression_equation = true;
	protected SimpleRegression regression = null;

	public ScatterPlotChart() {
		super(Type.ScatterPlot);
	}

	public ScatterPlotChart(String title) {
		super(Type.ScatterPlot, title);
	}

	public ScatterPlotChart(String title, String label_x, String label_y) {
		super(Type.ScatterPlot, title);
		setLabel(label_x, label_y);
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
	
	public boolean getShowLinearRegression() {
		return show_regression_linear;
	}

	public boolean getShowRegressionEquation() {
		return show_regression_equation;
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
	
	public void setShowLinearRegression(boolean show_regression_linear) {
		this.show_regression_linear = show_regression_linear;
	}

	public void setShowRegressionEquation(boolean show_regression_equation) {
		this.show_regression_equation = show_regression_equation;
	}

	public Type[] getSupportChartTypes() {
		return new Type[] { Type.ScatterPlot };
	}
	
	public Type getDefaultChartType() {
		return Type.ScatterPlot;
	}

	public void createChart() {
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
		if(show_regression_linear) {
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
		TextTitle title = jfreeChart.getTitle();
		RectangleInsets padding = title.getPadding();
		double bottomPadding = Math.max(padding.getBottom(), 4.0D);
		title.setPadding(padding.getTop(), padding.getLeft(), bottomPadding, padding.getRight());
	}

	public void afterCreateChartPanel(final ChartPanel chartPanel) {
		chartPanel.addChartMouseListener(new ChartMouseListener() {
			public void chartMouseMoved(ChartMouseEvent event) {}
			public void chartMouseClicked(ChartMouseEvent event) {
				if(event.getEntity() instanceof XYItemEntity) {
					showAnnotation((XYItemEntity)event.getEntity());
				}
			}
		});
	}
	
	protected void setTimeRangeFromSetting(DateAxis dateAxis) {
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
	
	protected int getAngleForAnnotation(XYPlot xyPlot, double x, double y) {
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
	
	protected String getTooltipText(XYDataset dataset, int series, int item) {
		Date dt = new Date((long)dataset.getXValue(series, item));
		long val = (long)dataset.getYValue(series, item);
		String str_dt = DateUtil.dateToString(dt, DateUtil.SDF_DATETIME);
		String str_val = NumberUtil.numberToString(val, NumberUtil.DF_NO_DOT_THOUSAND);
		return "\"" + str_dt + "\": " + str_val;
	}
	
	protected void showAnnotation(XYItemEntity entity) {		
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

	protected String getAnnotationText(XYDataItem item) {
		Date dt = new Date((long)item.getXValue());
		long val = (long)item.getYValue();
		return "\"" + DateUtil.dateToString(dt, DateUtil.SDF_DATETIME) + "\": " + NumberUtil.numberToString(val, NumberUtil.DF_NO_DOT_THOUSAND);
	}

}
