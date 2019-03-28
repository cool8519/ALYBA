package dal.tool.analyzer.alyba.ui.comp;

import java.awt.Frame;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.jfree.chart.ChartPanel;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.chart.Chart;
import dal.tool.analyzer.alyba.ui.chart.ChartSetting;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChartSetting;
import dal.tool.analyzer.alyba.ui.chart.ScatterPlotChart;
import dal.tool.analyzer.alyba.ui.chart.ScatterPlotChartSetting;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChart;
import dal.tool.analyzer.alyba.ui.chart.TimeSeriesChartSetting;
import dal.tool.analyzer.alyba.ui.chart.Chart.Type;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.CodeChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.ExtChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.IpChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.MethodChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.UriChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.VersionChart;
import dal.tool.analyzer.alyba.ui.chart.scatter.OverSizeChart;
import dal.tool.analyzer.alyba.ui.chart.scatter.OverTimeChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.IpResPerMinChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.TxDailyChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.TxDailyPerMinChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.TxHourlyChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.TxPerMinChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.TxPerSecChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.TxResPerMinChart;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.ReflectionUtil;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.ImageUtil;

public class ResultChart extends Composite {

	private static LinkedHashMap<String,Class<?>> map_data = new LinkedHashMap<String,Class<?>>(15); 
	private SashForm sf_main;
	private Composite comp_left;
	private Composite comp_right;
	private Composite comp_chart;
	private Group grp_left;
	private Group grp_right;
	private Combo cb_data;
	private Label lb_data;
	private Label lb_type;
	private ToolBar tb_charttype;
	private ToolItem ti_line;
	private ToolItem ti_hbar;
	private ToolItem ti_vbar;
	private ToolItem ti_pie;
	private ToolItem ti_scatter;
	private Frame frame_chart;
	private String current_data;
	private Chart current_chart;
	private ChartSetting current_setting;
	private ChartSetting chart_setting_ts;
	private ChartSetting chart_setting_kv;
	private ChartSetting chart_setting_sp;
	
	private ObjectDBUtil db = null;
	private EntityManager em = null;

	static {
		map_data.put("Tx: per Sec(s)", TxPerSecChart.class);
		map_data.put("Tx: per Min(s)", TxPerMinChart.class);
		map_data.put("Tx: Daily per Min(s)", TxDailyPerMinChart.class);
		map_data.put("Tx: Daily", TxDailyChart.class);
		map_data.put("Tx: Hourly", TxHourlyChart.class);
		map_data.put("Tx/ResTime: per Min(s)", TxResPerMinChart.class);
		map_data.put("IP/ResTime: per Min(s)", IpResPerMinChart.class);
		map_data.put("URI", UriChart.class);
		map_data.put("EXT", ExtChart.class);
		map_data.put("IP", IpChart.class);
		map_data.put("METHOD", MethodChart.class);
		map_data.put("VERSION", VersionChart.class);
		map_data.put("CODE", CodeChart.class);
		map_data.put("Over-Time", OverTimeChart.class);
		map_data.put("Over-Size", OverSizeChart.class);
		/* TODO: customizable chart */
		//map_data.put("User-Defined", UserDefinedChart.class);
	}
	
	public ResultChart(Composite parent, int style) {
		super(parent, style);
		createContents();
		addEventListener();
	}

	public void initDatabase() {
		if(db != null) {
			db.close(em);
		}
		this.db = ObjectDBUtil.getInstance();
		this.em = db.createEntityManager();		
	}

	public void closeDatabase() {
		if(db != null) {
			db.close(em);
		}
		em = null;
		db = null;
	}

	protected void createContents() {

		setSize(1244, 690);		
	    setLayout(new FillLayout());
	    
	    sf_main = new SashForm(this, SWT.HORIZONTAL);

	    FormLayout forml_comp_left = new FormLayout();
	    forml_comp_left.marginHeight = 10;
	    forml_comp_left.marginWidth = 20;
	    comp_left = new Composite(sf_main, SWT.NONE);
		comp_left.setLayout(forml_comp_left);

		FillLayout fl_comp_right = new FillLayout(SWT.VERTICAL);
		fl_comp_right.marginWidth = 15;
		fl_comp_right.marginHeight = 15;
	    comp_right = new Composite(sf_main, SWT.NONE);
	    comp_right.setLayout(fl_comp_right);

	    FormData fd_grp_left = new FormData();
	    fd_grp_left.left = new FormAttachment(0);
	    fd_grp_left.right = new FormAttachment(100, 0);
	    fd_grp_left.top = new FormAttachment(0, 5);
		fd_grp_left.bottom = new FormAttachment(100, -5);
	    FormLayout forml_grp_left = new FormLayout();
	    forml_grp_left.marginHeight = 10;
	    forml_grp_left.marginWidth = 15;
	    grp_left = new Group(comp_left, SWT.NONE);
	    grp_left.setLayoutData(fd_grp_left);
	    grp_left.setLayout(forml_grp_left);
	    grp_left.setFont(Utility.getFont(SWT.BOLD));
	    grp_left.setText(" SETTING ");

		FormData fd_lb_data = new FormData();
		fd_lb_data.top = new FormAttachment(0, 10);
		fd_lb_data.left = new FormAttachment(0, 5);
		lb_data = new Label(grp_left, SWT.LEFT);
		lb_data.setLayoutData(fd_lb_data);
		lb_data.setText("Data");
		
		FormData fd_cb_data = new FormData();
		fd_cb_data.top = new FormAttachment(lb_data, -4, SWT.TOP);
		fd_cb_data.left = new FormAttachment(lb_data, 10);
		fd_cb_data.right = new FormAttachment(100, -5);
	    cb_data = new Combo(grp_left, SWT.DROP_DOWN | SWT.READ_ONLY);
	    cb_data.setLayoutData(fd_cb_data);

		FormData fd_lb_type = new FormData();
		fd_lb_type.top = new FormAttachment(lb_data, 30);
		fd_lb_type.left = new FormAttachment(0, 5);
		lb_type = new Label(grp_left, SWT.LEFT);
		lb_type.setLayoutData(fd_lb_type);
		lb_type.setText("Type");
		
		FormData fd_toolBar = new FormData();
		fd_toolBar.top = new FormAttachment(lb_type, -4, SWT.TOP);
		fd_toolBar.left = new FormAttachment(lb_type, 10);
		fd_toolBar.right = new FormAttachment(100, -5);
		tb_charttype = new ToolBar(grp_left, SWT.RIGHT);
		tb_charttype.setLayoutData(fd_toolBar);
		tb_charttype.setEnabled(false);
		
		ti_line = new ToolItem(tb_charttype, SWT.CHECK);
		ti_line.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_LINE), 16, 16));
		ti_line.setToolTipText("Line Chart");
		new ToolItem(tb_charttype, SWT.SEPARATOR);
		ti_vbar = new ToolItem(tb_charttype, SWT.CHECK);
		ti_vbar.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_VBAR), 16, 16));		
		ti_vbar.setToolTipText("Vertical Bar Chart");
		new ToolItem(tb_charttype, SWT.SEPARATOR);
		ti_hbar = new ToolItem(tb_charttype, SWT.CHECK);
		ti_hbar.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_HBAR), 16, 16));		
		ti_hbar.setToolTipText("Horizontal Bar Chart");
		new ToolItem(tb_charttype, SWT.SEPARATOR);
		ti_pie = new ToolItem(tb_charttype, SWT.CHECK);
		ti_pie.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_PIE), 16, 16));
		ti_pie.setToolTipText("Pie Chart");
		new ToolItem(tb_charttype, SWT.SEPARATOR);
		ti_scatter = new ToolItem(tb_charttype, SWT.CHECK);
		ti_scatter.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_SCATTER), 16, 16));
		ti_scatter.setToolTipText("Scatter Plot Chart");
		
		FormData fd_comp_chart_setting_ts = new FormData();
		fd_comp_chart_setting_ts.left = new FormAttachment(0, -5);
		fd_comp_chart_setting_ts.right = new FormAttachment(100, 5);
		fd_comp_chart_setting_ts.top = new FormAttachment(tb_charttype, 30);
		fd_comp_chart_setting_ts.bottom = new FormAttachment(100, -5);
		chart_setting_ts = new TimeSeriesChartSetting(grp_left, this);
		chart_setting_ts.setLayoutData(fd_comp_chart_setting_ts);
		chart_setting_ts.setVisible(false);

		FormData fd_comp_chart_setting_kv = new FormData();
		fd_comp_chart_setting_kv.left = new FormAttachment(0, -5);
		fd_comp_chart_setting_kv.right = new FormAttachment(100, 5);
		fd_comp_chart_setting_kv.top = new FormAttachment(tb_charttype, 30);
		fd_comp_chart_setting_kv.bottom = new FormAttachment(100, -5);
		chart_setting_kv = new KeyValueChartSetting(grp_left, this);
		chart_setting_kv.setLayoutData(fd_comp_chart_setting_kv);
		chart_setting_kv.setVisible(false);

		FormData fd_comp_chart_setting_sp = new FormData();
		fd_comp_chart_setting_sp.left = new FormAttachment(0, -5);
		fd_comp_chart_setting_sp.right = new FormAttachment(100, 5);
		fd_comp_chart_setting_sp.top = new FormAttachment(tb_charttype, 30);
		fd_comp_chart_setting_sp.bottom = new FormAttachment(100, -5);
		chart_setting_sp = new ScatterPlotChartSetting(grp_left, this);
		chart_setting_sp.setLayoutData(fd_comp_chart_setting_sp);
		chart_setting_sp.setVisible(false);

		/* TODO: CustomizeChartSetting */

		FormLayout forml_grp_right = new FormLayout();
		forml_grp_right.marginHeight = 10;
		forml_grp_right.marginWidth = 20;
		grp_right = new Group(comp_right, SWT.NONE);
		grp_right.setLayout(forml_grp_right);
		grp_right.setFont(Utility.getFont(SWT.BOLD));
		grp_right.setText(" CHART ");
		
		FormData fd_comp_chart = new FormData();
		fd_comp_chart.left = new FormAttachment(0);
		fd_comp_chart.right = new FormAttachment(100);
		fd_comp_chart.top = new FormAttachment(0);
		fd_comp_chart.bottom = new FormAttachment(100, -5);
		comp_chart = new Composite(grp_right, SWT.EMBEDDED);
		comp_chart.setLayoutData(fd_comp_chart);
	    frame_chart = SWT_AWT.new_Frame(comp_chart);
	    frame_chart.setVisible(false);
		
	    sf_main.setWeights(new int[] {280, 961});
	    
	}

	protected void addEventListener() {

	    cb_data.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		dataSelected(cb_data.getText());
	    	}
	    });

		ti_line.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.TimeSeries, cb_data.getText(), current_setting);
			}
		});

		ti_vbar.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.VerticalBar, cb_data.getText(), current_setting);
			}
		});
		
		ti_hbar.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.HorizontalBar, cb_data.getText(), current_setting);
			}
		});

		ti_pie.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				drawChart(Type.Pie, cb_data.getText(), current_setting);
			}
		});

		ti_scatter.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.ScatterPlot, cb_data.getText(), current_setting);
			}
		});

	}

	private void checkChartTypeButtons(Widget item) {
		ti_line.setSelection(ti_line.equals(item));
		ti_vbar.setSelection(ti_vbar.equals(item));
		ti_hbar.setSelection(ti_hbar.equals(item));
		ti_pie.setSelection(ti_pie.equals(item));
		ti_scatter.setSelection(ti_scatter.equals(item));
		
		chart_setting_ts.setVisible(ti_line.equals(item));
		chart_setting_kv.setVisible((ti_vbar.equals(item) || ti_hbar.equals(item) || ti_pie.equals(item)));
		chart_setting_sp.setVisible(ti_scatter.equals(item));
	}
	
	public void load() throws Exception {
		initDatabase();
		loadData();
	}
	
	@SuppressWarnings("unchecked")
	private void loadData() throws Exception {
		for(String name : map_data.keySet()) {
			Class<Chart> c = (Class<Chart>)map_data.get(name);
			Method mtd = c.getMethod("showChart");
			Boolean show = (Boolean)mtd.invoke(null);
			String type = getDataTypeByName(name);
			String condition = "";
			if(type != null) {
				condition = "WHERE o.type = '" + type + "'";
			}
			String count_query = "SELECT COUNT(o) FROM " + getDataClassOfChart(map_data.get(name)).getName() + " AS o " + condition;			
			long count = db.select(em, count_query, Long.class, null);
			if(show && ("User-Defined".equals(name) || count > 0)) {
				cb_data.add(name);
			}
		}
	}
	
	private <E extends EntryVO> void dataSelected(String name) {
		if(!name.equals(current_data)) {
			current_data = name;
			if(current_setting != null) {
				current_setting.setVisible(false);
			}
			current_setting = getChartSetting(getChartTypeByName(name));
			drawChart(null, name, current_setting);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <E extends EntryVO> void drawChart(Type chartType, String name, ChartSetting chartSetting) {
		frame_chart.removeAll();		
		try {
			Class<Chart> chartClass = (Class<Chart>)map_data.get(name);
			Class<E> dataClass = (Class<E>)getDataClassOfChart(chartClass);
			String dataType = getDataTypeByName(name);
			List<E> dataList;
			String query;
			String condition = (dataType!=null) ? ("WHERE t.type = '"+dataType+"' ") : "";
			if(KeyEntryVO.class.isAssignableFrom(dataClass)) {
				query = "SELECT t FROM " + dataClass.getSimpleName() + " AS t " + condition + "ORDER BY req_count DESC";
				dataList = db.selectList(em, query, dataClass, null);
			} else {
				query = "SELECT t FROM " + dataClass.getSimpleName() + " AS t " + condition;
				dataList = db.selectList(em, query, dataClass, null);
			}
			Chart chart = chartClass.newInstance();
			current_chart = chart;
			if(chartType != null) {
				chart.setType(chartType);
			}
			checkChartType(chart);
			if(chartSetting != null) {
				chartSetting.configure(chart);
			}
			chart.draw(dataList);
			ChartPanel chartPanel = chart.getChartPanel();
			chartPanel.setPreferredSize(frame_chart.getSize());
			frame_chart.add(chartPanel);
			frame_chart.setVisible(true);
			tb_charttype.setEnabled(true);
			comp_chart.setFocus();
			current_data = name;
		} catch(Exception e) {
			Logger.error(e);
			frame_chart.removeAll();
			frame_chart.setVisible(false);
			tb_charttype.setEnabled(false);
		}
	}

	public void resize(Rectangle rect) {
		int width = rect.width - 24;
	    sf_main.setWeights(new int[] {280, width-280});
	}
	
	public void resetData() {
		cb_data.removeAll();
		tb_charttype.setEnabled(false);
		frame_chart.removeAll();
		frame_chart.setVisible(false);
		chart_setting_ts.setVisible(false);
		chart_setting_kv.setVisible(false);
		chart_setting_sp.setVisible(false);
		current_data = null;
	}
	
	private Class<?> getDataClassOfChart(Class<?> chart_class) {
		try { 
			for(Field f : ReflectionUtil.getFields(chart_class)) {
				if("dataClass".equals(f.getName())) {
					f.setAccessible(true);
					return (Class<?>)f.get(null);
				}
			}
		} catch(Exception e) {
			Logger.error(e);
		}
		return null;
	}
	
	private void checkChartType(Chart chart) {
		for(Type t : Chart.Type.values()) {
			boolean flag = chart.checkChartType(t);
			if(t == Type.TimeSeries) {
				ti_line.setEnabled(flag);
			} else if(t == Type.VerticalBar) {
				ti_vbar.setEnabled(flag);
			} else if(t == Type.HorizontalBar) {
				ti_hbar.setEnabled(flag);
			} else if(t == Type.Pie) {
				ti_pie.setEnabled(flag);
			} else if(t == Type.ScatterPlot) {
				ti_scatter.setEnabled(flag);
			}
		}
		Type currentType = chart.getType();
		if(currentType == Type.TimeSeries) {
			checkChartTypeButtons(ti_line);
		} else if(currentType == Type.VerticalBar) {
			checkChartTypeButtons(ti_vbar);
		} else if(currentType == Type.HorizontalBar) {
			checkChartTypeButtons(ti_hbar);
		} else if(currentType == Type.Pie) {
			checkChartTypeButtons(ti_pie);
		} else if(currentType == Type.ScatterPlot) {
			checkChartTypeButtons(ti_scatter);
		}
	}
	
	private ChartSetting getChartSetting(Type charType) {
		if(charType == Type.TimeSeries) {
			return chart_setting_ts;
		} else if(charType == Type.VerticalBar || charType == Type.HorizontalBar | charType == Type.Pie) {
			return chart_setting_kv;
		} else if(charType == Type.ScatterPlot) {
			return chart_setting_sp;
		}
		return null;
	}
	
	public void clickApplyButton() {
		drawChart(current_chart.getType(), cb_data.getText(), current_setting);
	}
	
	public Chart getCurrentChart() {
		return current_chart;
	}
	
	@SuppressWarnings("unchecked")
	public Type getChartTypeByName(String name) {
		Class<Chart> chartClass = (Class<Chart>)map_data.get(name);
		if(TimeSeriesChart.class.isAssignableFrom(chartClass)) {
			return Type.TimeSeries;
		} else if(KeyValueChart.class.isAssignableFrom(chartClass)) {
			return Type.VerticalBar;
		} else if(ScatterPlotChart.class.isAssignableFrom(chartClass)) {
			return Type.ScatterPlot;
		}
		return null;
	}
	
	private String getDataTypeByName(String name) {
		if(name == null) {
			return null;
		} else if(name.equals("URI")) {
			return "URI";
		} else if(name.equals("EXT")) {
			return "EXT";
		} else if(name.equals("IP")) {
			return "IP";
		} else if(name.equals("METHOD")) {
			return "METHOD";
		} else if(name.equals("VERSION")) {
			return "VERSION";
		} else if(name.equals("CODE")) {
			return "CODE";
		} else if(name.equals("Over-Time")) {
			return "TIME";
		} else if(name.equals("Over-Size")) {
			return "SIZE";
		}
		return null;
	}
	
}
