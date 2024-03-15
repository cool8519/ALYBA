package dal.tool.analyzer.alyba.ui.chart.timeseries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Minute;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import dal.tool.analyzer.alyba.output.ResourceGroupAndNameComparator;
import dal.tool.analyzer.alyba.output.ResourceGroupAndNameComparator.SortBy;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;

public class ResourceChart extends TimeSeriesChart {

	public static enum AggregationType { ALL, GROUP, NAME }
	public static enum ResourceType { ALL, CPU, Memory, Disk, Network };

	protected static final Class<?> DATA_CLASS = ResourceUsageEntryVO.class;
	protected static final float DEFAULT_LINE_WIDTH = 1.0F;
	protected static final float BOLD_LINE_WIDTH = 4.0F;
	
	protected ResourceType resourceType = ResourceType.ALL;
	protected AggregationType aggregationType = AggregationType.NAME;
	protected boolean cpuAvailable;
	protected boolean memoryAvailable;
	protected boolean diskAvailable;
	protected boolean networkAvailable;
	protected Map<ResourceType,XYDataset> datasetMap;
	protected XYLineAndShapeRenderer renderer;

	public ResourceChart() {
		super("Resource Usage Per Minute(s)", "Time", "Usage");
	    setUnitString("minute(s)");
	    setDateFormat("yyyy.MM.dd HH:mm");
	    setMovingAverageCount(30);
	    setMergeItemCount(10);
	    datasetMap = new HashMap<ResourceType,XYDataset>();
	}

	public ResourceType getResourceType() {
		return resourceType;
	}
	
	public void setResourceType(ResourceType resourceType) throws Exception {
		this.resourceType = resourceType;
		if(resourceType == ResourceType.CPU) {
			setTitle("CPU Usage Per Minute(s)");
			setLabelY("CPU");
		} else if(resourceType == ResourceType.Memory) {
			setTitle("Memory Usage Per Minute(s)");
			setLabelY("Memory");
		} else if(resourceType == ResourceType.Disk) {
			setTitle("Disk Usage Per Minute(s)");
			setLabelY("Disk");
		} else if(resourceType == ResourceType.Network) {
			setTitle("Network Usage Per Minute(s)");
			setLabelY("Network");
		}
	}

	public AggregationType getAggregationType() {
		return aggregationType;
	}
	
	public void setAggregationType(AggregationType aggregationType) {
		this.aggregationType = aggregationType;
	}

	public void setCpuAvailable(boolean flag) {
		cpuAvailable = flag;
	}

	public void setMemoryAvailable(boolean flag) {
		memoryAvailable = flag;
	}

	public void setDiskAvailable(boolean flag) {
		diskAvailable = flag;
	}

	public void setNetworkAvailable(boolean flag) {
		networkAvailable = flag;
	}

	public boolean getCpuAvailable() {
		return cpuAvailable;
	}
	
	public boolean getMemoryAvailable() {
		return memoryAvailable;
	}

	public boolean getDiskAvailable() {
		return diskAvailable;
	}

	public boolean getNetworkAvailable() {
		return networkAvailable;
	}

	
	protected <E extends EntryVO> void createDataset(List<E> dataList) {
		if(resourceType == ResourceType.ALL) {
			if(getCpuAvailable()) {
				datasetMap.put(ResourceType.CPU, getDataset(dataList, ResourceType.CPU));
			}
			if(getMemoryAvailable()) {
				datasetMap.put(ResourceType.Memory, getDataset(dataList, ResourceType.Memory));
			}
			if(getDiskAvailable()) {
				datasetMap.put(ResourceType.Disk, getDataset(dataList, ResourceType.Disk));
			}
			if(getNetworkAvailable()) {
				datasetMap.put(ResourceType.Network, getDataset(dataList, ResourceType.Network));
			}
		} else {
			dataset = getDataset(dataList, resourceType);
		}
	}

	private <E extends EntryVO> XYDataset getDataset(List<E> dataList, ResourceType type) {
		TimeSeries ts = null;
	    TimeSeriesCollection ts_collection = new TimeSeriesCollection();
	    String str_name_prev = null;
	    Date dt_prev = null;
    	ResourceUsageEntryVO mergedVO = null;
		if(aggregationType == AggregationType.NAME) {
			Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.GROUP_NAME_TIME));
		} else if(aggregationType == AggregationType.GROUP) {
			Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.GROUP_TIME));
		} else {
			Collections.sort(dataList, new ResourceGroupAndNameComparator(SortBy.TIME));
		}
	    if(merge_item) {
	    	int count = 0;
	    	for(Object data : dataList) {
	    		ResourceUsageEntryVO vo = (ResourceUsageEntryVO) data;
		    	String str_name = vo.getServerGroup() + ":" + vo.getServerName();
		    	if(str_name_prev == null || !str_name_prev.equals(str_name)) {
		    		if(str_name_prev != null) {
		    			ts_collection.addSeries(ts);
		    		}
		    		ts = new TimeSeries(str_name);
		    		str_name_prev = str_name;
	    			count = 0;
	    			mergedVO = vo;
		    	} else {
		    		if(merge_item_count > count++) {
		    			if(mergedVO == null) {
		    				mergedVO = vo;
		    			} else {
		    				mergedVO = mergedVO.merge(vo);
		    			}
		    		}
			    	if(merge_item_count == count) {
						double value = getValue(mergedVO, type);
						if(value > -1D) {
							ts.add(new Minute(mergedVO.getUnitDate()), value);
						}
		    			count = 0;
		    			mergedVO = null;
		    		}
		    	}
	    	}
		    ts_collection.addSeries(ts);
	    } else {
    		if(aggregationType == AggregationType.NAME) {
    	    	for(Object data : dataList) {
    	    		ResourceUsageEntryVO vo = (ResourceUsageEntryVO) data;
		    		String str_name = vo.getServerGroup() + ":" + vo.getServerName();
		    		if(str_name_prev == null || !str_name_prev.equals(str_name)) {
		    			if(str_name_prev != null) {
		    				ts_collection.addSeries(ts);
		    			}
		    			ts = new TimeSeries(str_name);
		    			str_name_prev = str_name;
		    		}		    	
		    		double value = getValue(vo, type);
					if(value > -1D) {
						ts.add(new Minute(vo.getUnitDate()), value);
					}
    	    	}
    	    	ts_collection.addSeries(ts);
    		} else if(aggregationType == AggregationType.GROUP) {
    	    	for(Object data : dataList) {
    	    		ResourceUsageEntryVO vo = (ResourceUsageEntryVO) data;
		    		String str_name = vo.getServerGroup();
		    		if(str_name_prev == null || !str_name_prev.equals(str_name)) {
		    			if(str_name_prev != null) {
		    	    		double value = getValue(mergedVO, type);
							if(value > -1D) {
								ts.add(new Minute(mergedVO.getUnitDate()), value);
							}
		    				ts_collection.addSeries(ts);
		    				dt_prev = null;
		    			}
		    			ts = new TimeSeries(str_name);
		    			str_name_prev = str_name;
		    		}		    	
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo);
		    		} else {
			    		double value = getValue(mergedVO, type);
						if(value > -1D) {
							ts.add(new Minute(mergedVO.getUnitDate()), value);
						}
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
    	    	}
	    		double value = getValue(mergedVO, type);
				if(value > -1D) {
					ts.add(new Minute(mergedVO.getUnitDate()), value);
				}
    	    	ts_collection.addSeries(ts);    			
    		} else {
    			ts = new TimeSeries(resourceType.name());
    			for(Object data : dataList) {
    	    		ResourceUsageEntryVO vo = (ResourceUsageEntryVO) data;
		    		Date dt = vo.getUnitDate();
		    		if(dt_prev == null) {
		    			mergedVO = vo;
		    		} else if(dt_prev.equals(dt)) {
	    				mergedVO = mergedVO.merge(vo);
		    		} else {
			    		double value = getValue(mergedVO, type);
						if(value > -1D) {
							ts.add(new Minute(mergedVO.getUnitDate()), value);
						}			    		
			    		mergedVO = vo;
		    		}
		    		dt_prev = dt;
    			}
	    		double value = getValue(mergedVO, type);
				if(value > -1D) {
					ts.add(new Minute(mergedVO.getUnitDate()), value);
				}
				ts_collection.addSeries(ts);    			
    		}
	    }	    
	    return ts_collection;
	}	
	
	private double getValue(ResourceUsageEntryVO vo, ResourceType type) {
		double value = -1D;
		if(type == ResourceType.CPU) {
			return vo.getCpuUsage(); 
		} else if(type == ResourceType.Memory) {
			return vo.getMemoryUsage(); 
		} else if(type == ResourceType.Disk) {
			return vo.getDiskUsage();
		} else if(type == ResourceType.Network) {
			return vo.getNetworkUsage(); 
		}
		return value;
	}

	
	public void createChart() {
		if(resourceType == ResourceType.ALL) {
	        DateAxis dateAxis = new DateAxis(label_x);
	        dateAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 13));
	        dateAxis.setLabelPaint(Color.darkGray);
	        dateAxis.setLowerMargin(0.02D);
	        dateAxis.setUpperMargin(0.02D);
		    if(tick_unit == null) {
		    	dateAxis.setDateFormatOverride(new SimpleDateFormat(date_format));
			    dateAxis.setAutoTickUnitSelection(true);
		    } else {
			    dateAxis.setTickUnit(tick_unit);
		    }
		    dateAxis.setVerticalTickLabels(true);
		    CombinedDomainXYPlot combinedXYPlot = new CombinedDomainXYPlot(dateAxis);
		    combinedXYPlot.setGap(10.0D);
			if(getCpuAvailable()) {
			    combinedXYPlot.add(createSubChart(ResourceType.CPU));
			}
			if(getMemoryAvailable()) {
			    combinedXYPlot.add(createSubChart(ResourceType.Memory));
			}
			if(getDiskAvailable()) {
			    combinedXYPlot.add(createSubChart(ResourceType.Disk));
			}
			if(getNetworkAvailable()) {
			    combinedXYPlot.add(createSubChart(ResourceType.Network));
			}
			combinedXYPlot.setDomainPannable(true);
			combinedXYPlot.setRangePannable(true);
	        jfreeChart = new JFreeChart(title, new Font("Tahoma", Font.BOLD, 20), combinedXYPlot, false);
			jfreeChart.setBackgroundPaint(Color.white);
			LegendTitle legend = new LegendTitle((XYPlot)combinedXYPlot.getSubplots().get(0));
			legend.setMargin(new RectangleInsets(1.0D, 1.0D, 1.0D, 1.0D));
			legend.setFrame(new LineBorder());
			legend.setBackgroundPaint(Color.white);
			legend.setPosition(RectangleEdge.BOTTOM);			
			TextTitle title = jfreeChart.getTitle();
			RectangleInsets padding = title.getPadding();
			double bottomPadding = Math.max(padding.getBottom(), 6.0D);
			title.setPadding(padding.getTop(), padding.getLeft(), bottomPadding, padding.getRight());			
			jfreeChart.addLegend(legend);
		} else {
			super.createChart();
			XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
			ValueAxis valueAxis = xyPlot.getRangeAxis();
			if(valueAxis.getRange().getUpperBound() < 100.0D) {
				valueAxis.setRange(0.0D, 100.0D);
			} else {
				valueAxis.setRange(0.0D, valueAxis.getRange().getUpperBound());
			}
		}
	}
	
	private XYPlot createSubChart(ResourceType type) {
		XYDataset dataset = datasetMap.get(type);
        NumberAxis numberAxis = new NumberAxis(type.name());
        numberAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 13));
        numberAxis.setLabelPaint(Color.darkGray);
        TimeSeriesCollection ts_collection = (TimeSeriesCollection)dataset;
        int series_count = ts_collection.getSeriesCount();
        if(renderer == null) {
	        renderer = new XYLineAndShapeRenderer(true, false);
		    renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("[{0}] : (\"{1}\", {2})", new SimpleDateFormat(date_format), NumberFormat.getInstance()));
		    renderer.setBaseShapesVisible(show_shape);
			for(int i = 0; i < series_count; i++) {
				renderer.setSeriesShape(i, new Ellipse2D.Double(-2, -2, 4, 4));
			}
        }
		if(show_moving_average) {
			for(int i = 0; i < series_count; i++) {
				TimeSeries ts = ts_collection.getSeries(i);
			    TimeSeries ts_mov = MovingAverage.createMovingAverage(ts, ts_collection.getSeriesKey(i)+"("+moving_avg_count+" Moving Avg.)", moving_avg_count, 0);
			    ts_collection.addSeries(ts_mov);
				renderer.setSeriesShapesVisible(series_count+i, false);
			}
		}
        XYPlot xyPlot = new XYPlot(dataset, null, numberAxis, renderer);
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
		xyPlot.setBackgroundPaint(Color.lightGray);
		xyPlot.setDomainGridlinePaint(Color.white);
		xyPlot.setRangeGridlinePaint(Color.white);
		if(numberAxis.getRange().getUpperBound() < 100.0D) {
			numberAxis.setRange(0.0D, 100.0D);
		} else {
			numberAxis.setRange(0.0D, numberAxis.getRange().getUpperBound());
		}
        return xyPlot;
	}

	
	public void afterCreateChart(JFreeChart jfreeChart) {
		if(resourceType != ResourceType.ALL) {
			super.afterCreateChart(jfreeChart);
		    XYPlot xyPlot = (XYPlot)jfreeChart.getPlot();
		    xyPlot.getRenderer().setBaseToolTipGenerator(new StandardXYToolTipGenerator("[{0}] : (\"{1}\", {2})", new SimpleDateFormat(date_format), NumberFormat.getInstance()));
		}
	}

	public void afterCreateChartPanel(ChartPanel chartPanel) {
		if(resourceType == ResourceType.ALL) {
			chartPanel.addChartMouseListener(new ChartMouseListener() {
				@SuppressWarnings("unchecked")
				public void chartMouseClicked(ChartMouseEvent event) {
					ChartEntity entity = event.getEntity();
					if(entity instanceof LegendItemEntity) {
						LegendItemEntity itemEntity = (LegendItemEntity)entity;
						XYDataset xyDataset = (XYDataset)itemEntity.getDataset();
						CombinedDomainXYPlot plots = (CombinedDomainXYPlot)event.getChart().getPlot();
						int index = xyDataset.indexOf(itemEntity.getSeriesKey());
						boolean first = true;
						float width = DEFAULT_LINE_WIDTH;
						Boolean visible = null;
						for(XYPlot plot : (List<XYPlot>)plots.getSubplots()) {
							XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(0);
							if(first) {
								first = false;
								width = ((BasicStroke)renderer.getSeriesStroke(index)).getLineWidth();
								visible = renderer.getSeriesLinesVisible(index);
							}
							if(visible == null || visible == Boolean.TRUE) {
								if(width == DEFAULT_LINE_WIDTH) {
									renderer.setSeriesStroke(index, new BasicStroke(BOLD_LINE_WIDTH, 2, 2));
								} else {
									renderer.setSeriesLinesVisible(index, false);
									renderer.setSeriesShapesVisible(index, false);
								}
							} else {
								renderer.setSeriesLinesVisible(index, true);
								renderer.setSeriesStroke(index, new BasicStroke(DEFAULT_LINE_WIDTH, 2, 2));
								renderer.setSeriesShapesVisible(index, show_shape);
							}
							renderer.setDrawSeriesLineAsPath(true);								
						}
					}
				}
				public void chartMouseMoved(ChartMouseEvent event) {
				}
			});
		} else {
			super.afterCreateChartPanel(chartPanel);
		}
	}

}
