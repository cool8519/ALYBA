package dal.tool.analyzer.alyba.ui.comp;

import java.awt.Frame;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
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
import dal.tool.analyzer.alyba.output.vo.RegressionEntryVO;
import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.chart.Chart;
import dal.tool.analyzer.alyba.ui.chart.Chart.Type;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.CodeChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.ExtChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.IpChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.MethodChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.UriChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.VersionChart;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionAnalysisChart;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart;
import dal.tool.analyzer.alyba.ui.chart.regression.RegressionSummary;
import dal.tool.analyzer.alyba.ui.chart.scatter.OverSizeChart;
import dal.tool.analyzer.alyba.ui.chart.scatter.OverTimeChart;
import dal.tool.analyzer.alyba.ui.chart.setting.ChartSetting;
import dal.tool.analyzer.alyba.ui.chart.setting.KeyValueChartSetting;
import dal.tool.analyzer.alyba.ui.chart.setting.RegressionAnalysisChartSetting;
import dal.tool.analyzer.alyba.ui.chart.setting.ResourceChartSetting;
import dal.tool.analyzer.alyba.ui.chart.setting.ScatterPlotChartSetting;
import dal.tool.analyzer.alyba.ui.chart.setting.TimeSeriesChartSetting;
import dal.tool.analyzer.alyba.ui.chart.timeseries.IpResPerMinChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.ResourceChart;
import dal.tool.analyzer.alyba.ui.chart.timeseries.ResourceChart.ResourceType;
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
import dal.util.swt.MessageUtil;

public class ResultChart extends Composite {

	private static LinkedHashMap<String,Class<?>> map_data = new LinkedHashMap<String,Class<?>>(15); 
	private SashForm sf_main;
	private SashForm sf_regression;
	private SashForm sf_regression2;
	private Composite comp_left;
	private Composite comp_right;
	private Composite comp_chart;
	private Composite comp_regression;
	private Composite comp_regression_top;
	private Composite comp_regression_left;
	private Group grp_left;
	private Group grp_right;
	private Combo cb_data;
	private Label lb_data;
	private Label lb_type;
	private ToolBar tb_charttype;
	private ToolBar tb_restype;
	private ToolItem ti_line;
	private ToolItem ti_hbar;
	private ToolItem ti_vbar;
	private ToolItem ti_pie;
	private ToolItem ti_scatter;
	private ToolItem ti_all;
	private ToolItem ti_cpu;
	private ToolItem ti_memory;
	private ToolItem ti_disk;
	private ToolItem ti_network;
	
	private Frame frame_chart;
	private Frame frame_regression_top;
	private Frame frame_regression_bottom;
	private String current_data_name;
	private Chart current_chart;
	private ChartSetting current_setting;
	private ChartSetting chart_setting_ts;
	private ChartSetting chart_setting_kv;
	private ChartSetting chart_setting_sp;
	private ChartSetting chart_setting_rs;
	private ChartSetting chart_setting_ra;
	private RegressionSummary regression_summary;
	private boolean existsResourceData;
	
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
		map_data.put("System Resource", ResourceChart.class);
		map_data.put("Regression Analysis", RegressionChart.class);
	}
	
	public ResultChart(Composite parent, int style) {
		super(parent, style);
		createContents();
		addEventListener();
	}

	public void initDatabase() throws Exception {
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
		lb_data.setFont(Utility.getFont());
		lb_data.setText("Data");
		
		FormData fd_cb_data = new FormData();
		fd_cb_data.top = new FormAttachment(lb_data, -4, SWT.TOP);
		fd_cb_data.left = new FormAttachment(lb_data, 10);
		fd_cb_data.right = new FormAttachment(100, -5);
	    cb_data = new Combo(grp_left, SWT.DROP_DOWN | SWT.READ_ONLY);
	    cb_data.setLayoutData(fd_cb_data);
	    cb_data.setFont(Utility.getFont());

		FormData fd_lb_type = new FormData();
		fd_lb_type.top = new FormAttachment(lb_data, 30);
		fd_lb_type.left = new FormAttachment(0, 5);
		lb_type = new Label(grp_left, SWT.LEFT);
		lb_type.setLayoutData(fd_lb_type);
		lb_type.setFont(Utility.getFont());
		lb_type.setText("Type");
		
		FormData fd_toolBar_type = new FormData();
		fd_toolBar_type.top = new FormAttachment(lb_type, -4, SWT.TOP);
		fd_toolBar_type.left = new FormAttachment(lb_type, 10);
		fd_toolBar_type.right = new FormAttachment(100, -5);
		tb_charttype = new ToolBar(grp_left, SWT.RIGHT);
		tb_charttype.setLayoutData(fd_toolBar_type);
		tb_charttype.setEnabled(false);
		tb_charttype.setVisible(false);
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

		FormData fd_toolBar_res = new FormData();
		fd_toolBar_res.top = new FormAttachment(lb_type, -4, SWT.TOP);
		fd_toolBar_res.left = new FormAttachment(lb_type, 10);
		fd_toolBar_res.right = new FormAttachment(100, -5);
		tb_restype = new ToolBar(grp_left, SWT.RIGHT);
		tb_restype.setLayoutData(fd_toolBar_res);
		tb_restype.setEnabled(false);
		tb_restype.setVisible(false);
		ti_all = new ToolItem(tb_restype, SWT.CHECK);
		ti_all.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_ALL), 16, 16));
		ti_all.setToolTipText("All Charts");
		new ToolItem(tb_restype, SWT.SEPARATOR);
		ti_cpu = new ToolItem(tb_restype, SWT.CHECK);
		ti_cpu.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_CPU), 16, 16));		
		ti_cpu.setToolTipText("CPU Chart");
		new ToolItem(tb_restype, SWT.SEPARATOR);
		ti_memory = new ToolItem(tb_restype, SWT.CHECK);
		ti_memory.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_MEMORY), 16, 16));		
		ti_memory.setToolTipText("Memory Chart");
		new ToolItem(tb_restype, SWT.SEPARATOR);
		ti_disk = new ToolItem(tb_restype, SWT.CHECK);
		ti_disk.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_DISK), 16, 16));
		ti_disk.setToolTipText("Disk Chart");
		new ToolItem(tb_restype, SWT.SEPARATOR);
		ti_network = new ToolItem(tb_restype, SWT.CHECK);
		ti_network.setImage(ImageUtil.resize(ImageUtil.getImage(Constant.IMAGE_PATH_CHART_NETWORK), 16, 16));
		ti_network.setToolTipText("Network Chart");

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

		FormData fd_comp_chart_setting_rs = new FormData();
		fd_comp_chart_setting_rs.left = new FormAttachment(0, -5);
		fd_comp_chart_setting_rs.right = new FormAttachment(100, 5);
		fd_comp_chart_setting_rs.top = new FormAttachment(tb_charttype, 30);
		fd_comp_chart_setting_rs.bottom = new FormAttachment(100, -5);
		chart_setting_rs = new ResourceChartSetting(grp_left, this);
		chart_setting_rs.setLayoutData(fd_comp_chart_setting_rs);
		chart_setting_rs.setVisible(false);

		FormData fd_comp_chart_setting_ra = new FormData();
		fd_comp_chart_setting_ra.left = new FormAttachment(0, -5);
		fd_comp_chart_setting_ra.right = new FormAttachment(100, 5);
		fd_comp_chart_setting_ra.top = new FormAttachment(cb_data, 30);
		fd_comp_chart_setting_ra.bottom = new FormAttachment(100, -5);
		chart_setting_ra = new RegressionAnalysisChartSetting(grp_left, this);
		chart_setting_ra.setLayoutData(fd_comp_chart_setting_ra);
		chart_setting_ra.setVisible(false);

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
		comp_chart.setLayout(new FillLayout(SWT.HORIZONTAL));
		comp_chart.setLayoutData(fd_comp_chart);
		comp_chart.setVisible(false);
	    frame_chart = SWT_AWT.new_Frame(comp_chart);

		FormData fd_sf_regression = new FormData();
		fd_sf_regression.left = new FormAttachment(0);
		fd_sf_regression.right = new FormAttachment(100);
		fd_sf_regression.top = new FormAttachment(0);
		fd_sf_regression.bottom = new FormAttachment(100, -5);
		comp_regression = new Composite(grp_right, SWT.EMBEDDED);		
		comp_regression.setLayout(new FillLayout(SWT.HORIZONTAL));
		comp_regression.setLayoutData(fd_sf_regression);
		comp_regression.setVisible(false);
		
	    sf_regression = new SashForm(comp_regression, SWT.VERTICAL);
	    sf_regression.setLayout(new FillLayout());
	    sf_regression.setSashWidth(20);
		comp_regression_top = new Composite(sf_regression, SWT.EMBEDDED);
	    frame_regression_top = SWT_AWT.new_Frame(comp_regression_top);

	    sf_regression2 = new SashForm(sf_regression, SWT.HORIZONTAL);
	    sf_regression2.setSashWidth(10);
	    comp_regression_left = new Composite(sf_regression2, SWT.EMBEDDED);	
	    frame_regression_bottom = SWT_AWT.new_Frame(comp_regression_left);	    
	    regression_summary = new RegressionSummary(sf_regression2);

		resize(new Rectangle(-1, -1, getSize().x+21, getSize().y));
	    
	}

	protected void addEventListener() {

	    cb_data.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		dataSelected(cb_data.getText());
	    	}
	    });

		ti_line.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.TimeSeries, cb_data.getText());
			}
		});

		ti_vbar.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.VerticalBar, cb_data.getText());
			}
		});
		
		ti_hbar.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.HorizontalBar, cb_data.getText());
			}
		});

		ti_pie.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				drawChart(Type.Pie, cb_data.getText());
			}
		});

		ti_scatter.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawChart(Type.ScatterPlot, cb_data.getText());
			}
		});

		ti_all.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawResourceChart(ResourceType.ALL, cb_data.getText());
			}
		});

		ti_cpu.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawResourceChart(ResourceType.CPU, cb_data.getText());
			}
		});
		
		ti_memory.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawResourceChart(ResourceType.Memory, cb_data.getText());
			}
		});

		ti_disk.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				drawResourceChart(ResourceType.Disk, cb_data.getText());
			}
		});

		ti_network.addListener(SWT.Selection, new Listener() {			
			public void handleEvent(Event e) {
				drawResourceChart(ResourceType.Network, cb_data.getText());
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
	
	private void checkResourceChartTypeButtons(Widget item) {
		ti_all.setSelection(ti_all.equals(item));
		ti_cpu.setSelection(ti_cpu.equals(item));
		ti_memory.setSelection(ti_memory.equals(item));
		ti_disk.setSelection(ti_disk.equals(item));
		ti_network.setSelection(ti_network.equals(item));
		chart_setting_rs.setVisible(true);
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
			String table_name = "Regression Analysis".equals(name) ? "TPMEntryVO" : getDataClassOfChart(map_data.get(name)).getName();  
			String count_query = "SELECT COUNT(o) FROM " + table_name + " AS o " + condition;			
			long count = db.select(em, count_query, Long.class, null);
			if("System Resource".equals(name)) {
				existsResourceData = count > 0;
			}
			if(show && count > 0) {
				cb_data.add(name);
			}
		}
		chart_setting_ts.init();
		chart_setting_kv.init();
		chart_setting_sp.init();
		chart_setting_rs.init();
		chart_setting_ra.init();
	}
	
	private <E extends EntryVO> void dataSelected(String dataName) {
		if(!dataName.equals(current_data_name)) {
			current_data_name = dataName;			
			if(current_setting != null) {
				current_setting.setVisible(false);
			}
			resetChartSettings(dataName);
			if(dataName.equals("System Resource")) {
				drawResourceChart(null, dataName);
			} else if(dataName.equals("Regression Analysis")) {
				drawRegressionChart(dataName);
			} else {
				drawChart(null, dataName);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void resetChartSettings(String name) {
		try {
			if(name.equals("System Resource")) {
				lb_type.setVisible(true);
				tb_charttype.setVisible(false);
				tb_restype.setVisible(true);
				comp_chart.setVisible(true);
				comp_regression.setVisible(false);				
				Class<Chart> chartClass = (Class<Chart>)map_data.get(name);
				Chart chart = chartClass.newInstance();
				chart_setting_rs.reset(chart);
			} else if(name.equals("Regression Analysis")) {
				lb_type.setVisible(false);
				tb_charttype.setVisible(false);
				tb_restype.setVisible(false);
				comp_chart.setVisible(false);
				comp_regression.setVisible(true);
				Class<Chart> chartClass = (Class<Chart>)map_data.get(name);
				Chart chart = chartClass.newInstance();
				chart_setting_ra.reset(chart);
			} else {
				lb_type.setVisible(true);
				tb_restype.setVisible(false);
				tb_charttype.setVisible(true);
				comp_chart.setVisible(true);
				comp_regression.setVisible(false);				
				Class<Chart> chartClass = (Class<Chart>)map_data.get(name);
				Chart chart = chartClass.newInstance();
				boolean resetTS = false;
				boolean resetKV = false;
				boolean resetSP = false;
				for(Type type : chart.getSupportChartTypes()) {
					if(!resetTS && type == Type.TimeSeries) {
						chart_setting_ts.reset(chart);
						resetTS = true;
					} else if(!resetKV && (type == Type.VerticalBar || type == Type.HorizontalBar || type == Type.Pie)) {
						chart_setting_kv.reset(chart);
						resetKV = true;
					} else if(!resetSP && type == Type.ScatterPlot) {
						chart_setting_sp.reset(chart);
						resetSP = true;
					}
				}
			}
		} catch(Exception e) {
			Logger.error(e);
		}		
	}

	@SuppressWarnings("unchecked")
	private <E extends EntryVO> void drawChart(Type chartType, String name) {
		frame_chart.removeAll();
		try {
			Class<Chart> chartClass = (Class<Chart>)map_data.get(name);
			Class<E> dataClass = (Class<E>)getDataClassOfChart(chartClass);
			String dataType = getDataTypeByName(name);			
			Chart chart = chartClass.newInstance();
			chart.setType(chartType==null ? chart.getDefaultChartType() : chartType);
			ChartSetting chartSetting = getChartSetting(chart.getType());			
			current_chart = chart;
			current_setting = chartSetting;
			checkChartType(chart);
			if(chartSetting != null) {
				chartSetting.configure(chart);
			}			
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
			chart.draw(dataList);
			ChartPanel chartPanel = chart.getChartPanel();
			chartPanel.setPreferredSize(frame_chart.getSize());
			frame_chart.add(chartPanel);
			frame_chart.setVisible(true);
			tb_charttype.setEnabled(true);
			comp_chart.setFocus();
			current_data_name = name;
		} catch(Exception e) {
			Logger.error(e);
			frame_chart.removeAll();
			comp_chart.setVisible(false);
			tb_charttype.setEnabled(false);
			MessageUtil.showErrorMessage(getShell(), e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private <E extends EntryVO> void drawResourceChart(ResourceType chartType, String name) {
		frame_chart.removeAll();		
		try {
			Class<ResourceChart> chartClass = (Class<ResourceChart>)map_data.get(name);
			Class<E> dataClass = (Class<E>)getDataClassOfChart(chartClass);
			ResourceChart chart = chartClass.newInstance();
			chart.setResourceType(chartType==null ? ResourceType.ALL : chartType);
			chart.setType(Type.TimeSeries);
			ChartSetting chartSetting = chart_setting_rs;			
			current_chart = chart;
			current_setting = chartSetting;
			checkResourceChartType(chart);
			if(chartSetting != null) {
				chartSetting.setVisible(true);
				chartSetting.configure(chart);
			}			
			String query = "SELECT t FROM " + dataClass.getSimpleName() + " AS t ";
			List<E> dataList = db.selectList(em, query, dataClass, null);
			chart.draw(dataList);
			ChartPanel chartPanel = chart.getChartPanel();
			chartPanel.setPreferredSize(frame_chart.getSize());
			frame_chart.add(chartPanel);
			frame_chart.setVisible(true);
			tb_restype.setEnabled(true);
			comp_chart.setFocus();
			current_data_name = name;
		} catch(Exception e) {
			Logger.error(e);
			frame_chart.removeAll();
			comp_chart.setVisible(false);
			tb_restype.setEnabled(false);
			MessageUtil.showErrorMessage(getShell(), e.getMessage());
		}
	}

	private <E extends EntryVO> void drawRegressionChart(String name) {
	    sf_regression.setWeights(new int[] {40, 60});
	    sf_regression2.setWeights(new int[] {65, 35});
		frame_regression_top.removeAll();		
		frame_regression_bottom.removeAll();
		try {			
			RegressionChart chart = new RegressionChart();
			ChartSetting chartSetting = chart_setting_ra;		
			current_chart = chart;
			current_setting = chartSetting;
			if(chartSetting != null) {
				chartSetting.setVisible(true);
				chartSetting.configure(chart);
			}			
			List<RegressionEntryVO> dataList = null;
			StringBuffer query = new StringBuffer();
			boolean ignoreResourceData = false;
			if(existsResourceData) {
				query.append("SELECT t.unit_date, r.name, r.group, t.req_count, t.request_ip_count, t.avg_response_time, t.err_count, r.cpu, r.memory, r.disk, r.network ");
				query.append("FROM TPMEntryVO as t, ResourceUsageEntryVO as r "); 
				query.append("WHERE t.unit_date = r.unit_date"); 
				List<Object[]> tempList = db.selectList(em, query.toString(), Object[].class, null);
				dataList = new ArrayList<RegressionEntryVO>(tempList.size());
				for(Object[] row : tempList) {
					RegressionEntryVO vo = new RegressionEntryVO((Date)row[0], (String)row[1], (String)row[2]);
					vo.setRequestTxCount((Integer)row[3]);
					vo.setRequestIpCount((Integer)row[4]);
					vo.setAverageResponseTimeMS((Double)row[5]);
					vo.setErrorCount((Integer)row[6]);
					vo.setCpuUsage((Double)row[7]);
					vo.setMemoryUsage((Double)row[8]);
					vo.setDiskUsage((Double)row[9]);
					vo.setNetworkUsage((Double)row[10]);
					dataList.add(vo);
				}
				if(dataList.size() < 1) {
					ignoreResourceData = MessageUtil.showConfirmMessage(getShell(), "The time of the transaction and resource data do not match. Do you want analyze for regression without resource?");
					if(!ignoreResourceData) {
						return;
					}
				}
			}
			if(!existsResourceData || ignoreResourceData) {
				query.append("SELECT t FROM TPMEntryVO as t"); 
				List<TPMEntryVO> tempList = db.selectList(em, query.toString(), TPMEntryVO.class, null);
				dataList = new ArrayList<RegressionEntryVO>(tempList.size());
				for(TPMEntryVO row : tempList) {
					RegressionEntryVO vo = new RegressionEntryVO(row.getUnitDate());
					vo.setRequestTxCount(row.getRequestCount());
					vo.setRequestIpCount(row.getRequestIPCount());
					vo.setAverageResponseTimeMS(row.getAverageResponseTime());
					vo.setErrorCount(row.getErrorCount());
					dataList.add(vo);
				}
			}
			if(dataList.size() < 1) {
				throw new Exception("No data to analyze for regression.");
			}
			chart.initCharts();
			chart.draw(dataList);			
			ChartPanel chartPanel_top = chart.getChartPanel(0);
			chartPanel_top.setPreferredSize(frame_regression_top.getSize());
			frame_regression_top.add(chartPanel_top);
			frame_regression_top.setVisible(true);
			ChartPanel chartPanel_bottom = chart.getChartPanel(1);
			chartPanel_bottom.setPreferredSize(frame_regression_bottom.getSize());
			frame_regression_bottom.add(chartPanel_bottom);
			frame_regression_bottom.setVisible(true);
			regression_summary.apply((RegressionAnalysisChart)chart.getChart(1));
			comp_regression.setFocus();
			current_data_name = name;			
		} catch(Exception e) {
			Logger.error(e);
			frame_regression_top.removeAll();		
			frame_regression_bottom.removeAll();
			comp_regression.setVisible(false);
			MessageUtil.showErrorMessage(getShell(), e.getMessage());
		}
	}

	public void resize(Rectangle rect) {
		int width = rect.width - 24;
	    sf_main.setWeights(new int[] {280, width-280});
	}
	
	public void resetData() {
		cb_data.removeAll();
		lb_type.setVisible(false);
		tb_charttype.setVisible(false);
		tb_charttype.setEnabled(false);
		tb_restype.setVisible(false);
		tb_restype.setEnabled(false);
		frame_chart.removeAll();
		comp_chart.setVisible(false);
		comp_regression.setVisible(false);
		chart_setting_ts.reset(null);
		chart_setting_ts.setVisible(false);
		chart_setting_kv.reset(null);
		chart_setting_kv.setVisible(false);
		chart_setting_sp.reset(null);
		chart_setting_sp.setVisible(false);
		chart_setting_rs.reset(null);
		chart_setting_rs.setVisible(false);
		chart_setting_ra.reset(null);
		chart_setting_ra.setVisible(false);
		regression_summary.reset();
		current_data_name = null;
		existsResourceData = false;
	}
	
	private Class<?> getDataClassOfChart(Class<?> chart_class) {
		try {
			if(chart_class == RegressionChart.class) {
				return ResourceUsageEntryVO.class;
			}
			for(Field f : ReflectionUtil.getFields(chart_class)) {
				if("DATA_CLASS".equals(f.getName())) {
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
		for(Type t : Type.values()) {
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

	private long getResourceCount(ResourceType type) {
		try {
			String count_query = "SELECT COUNT(o) FROM ResourceUsageEntryVO AS o WHERE " + type.name().toLowerCase() + " > -1";			
			return db.select(em, count_query, Long.class, null);
		} catch(Exception e) {
			Logger.error(e);
			return -1L;
		}
	}
	
	private void checkResourceChartType(ResourceChart chart) {		
		boolean exist_cpu = getResourceCount(ResourceType.CPU) > 0;
		boolean exist_memory = getResourceCount(ResourceType.Memory) > 0;
		boolean exist_disk = getResourceCount(ResourceType.Disk) > 0;
		boolean exist_network = getResourceCount(ResourceType.Network) > 0;		
		ti_all.setEnabled(true);
		ti_cpu.setEnabled(exist_cpu);
		ti_memory.setEnabled(exist_memory);
		ti_disk.setEnabled(exist_disk);
		ti_network.setEnabled(exist_network);
		chart.setCpuAvailable(exist_cpu);
		chart.setMemoryAvailable(exist_memory);
		chart.setDiskAvailable(exist_disk);
		chart.setNetworkAvailable(exist_network);		
		ResourceType currentType = chart.getResourceType();
		if(currentType == ResourceType.ALL) {
			checkResourceChartTypeButtons(ti_all);
		} else if(currentType == ResourceType.CPU) {
			checkResourceChartTypeButtons(ti_cpu);
		} else if(currentType == ResourceType.Memory) {
			checkResourceChartTypeButtons(ti_memory);
		} else if(currentType == ResourceType.Disk) {
			checkResourceChartTypeButtons(ti_disk);
		} else if(currentType == ResourceType.Network) {
			checkResourceChartTypeButtons(ti_network);
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
		String dataName = cb_data.getText();
		if(dataName.equals("System Resource")) {
			drawResourceChart(((ResourceChart)current_chart).getResourceType(), dataName);
		} else if(dataName.equals("Regression Analysis")) {
			drawRegressionChart(dataName);
		} else {
			drawChart(current_chart.getType(), dataName);
		}
	}
	
	public Chart getCurrentChart() {
		return current_chart;
	}
	
	private String getDataTypeByName(String name) {
		if(name == null) {
			return null;
		} else if(name.equals("Tx: Daily")) {
			return "DAY";
		} else if(name.equals("Tx: Hourly")) {
			return "HOUR";
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
