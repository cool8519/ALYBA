package dal.tool.analyzer.alyba.ui.comp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.DateUtil;
import dal.util.NumberUtil;
import dal.util.db.ObjectDBUtil;

public class ResultSummary extends Composite {

	private Label lb_title;
	private Label lb_time;
	private Label lb_filter;
	private Label lb_aggr;
	private Label lb_peak;
	private Label lb_bad;
	private Table tbl_time;
	private Table tbl_filter;
	private Table tbl_aggr;
	private Table tbl_peak;
	private Table tbl_bad;

	private ObjectDBUtil db = null;
	private EntityManager em = null;
	private SettingEntryVO settingVo = null;

	public ResultSummary(Composite parent, int style) {
		super(parent, style);
		createContents();
	}

	public void initDatabase() throws Exception {
		if(db != null) {
			db.close(em);
		}
		this.db = ObjectDBUtil.getInstance();
		this.em = db.createEntityManager();		
		this.settingVo = db.select(em, SettingEntryVO.class);
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
	
		lb_title = new Label(this, SWT.NONE);
		lb_title.setFont(Utility.getFont(18, SWT.NONE));
		lb_title.setBounds(478, 20, 300, 32);
		lb_title.setAlignment(SWT.CENTER);
		lb_title.setText("Summary of results");
				
		lb_time = new Label(this, SWT.NONE);
		lb_time.setText("¡ß Time");
		lb_time.setFont(Utility.getFont(SWT.BOLD));
		lb_time.setBounds(278, 65, 300, 18);
		tbl_time = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.NO_SCROLL);
		tbl_time.setLinesVisible(true);
		tbl_time.setHeaderVisible(false);
		tbl_time.setBounds(278, 85, 700, 40);
		TableColumn tblc_time_null = new TableColumn(tbl_time, SWT.NONE);
		tblc_time_null.setWidth(0);
		TableColumn tblc_time_index = new TableColumn(tbl_time, SWT.CENTER);
		tblc_time_index.setWidth(180);
		TableColumn tblc_time_data = new TableColumn(tbl_time, SWT.LEFT);
		tblc_time_data.setWidth(520);
		
		lb_filter = new Label(this, SWT.NONE);
		lb_filter.setText("¡ß Filter");
		lb_filter.setFont(Utility.getFont(SWT.BOLD));
		lb_filter.setBounds(278, 145, 300, 18);
		tbl_filter = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.NO_SCROLL);
		tbl_filter.setLinesVisible(true);
		tbl_filter.setHeaderVisible(false);
		tbl_filter.setBounds(278, 165, 700, 60);
		TableColumn tbl_filter_null = new TableColumn(tbl_filter, SWT.NONE);
		tbl_filter_null.setWidth(0);
		TableColumn tbl_filter_index = new TableColumn(tbl_filter, SWT.CENTER);
		tbl_filter_index.setWidth(180);
		TableColumn tbl_filter_data = new TableColumn(tbl_filter, SWT.LEFT);
		tbl_filter_data.setWidth(520);

		lb_aggr = new Label(this, SWT.NONE);
		lb_aggr.setText("¡ß Aggregation");
		lb_aggr.setFont(Utility.getFont(SWT.BOLD));
		lb_aggr.setBounds(278, 245, 300, 18);
		tbl_aggr = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.NO_SCROLL);
		tbl_aggr.setLinesVisible(true);
		tbl_aggr.setHeaderVisible(false);
		tbl_aggr.setBounds(278, 265, 700, 136);
		TableColumn tbl_aggr_null = new TableColumn(tbl_aggr, SWT.NONE);
		tbl_aggr_null.setWidth(0);
		TableColumn tbl_aggr_index = new TableColumn(tbl_aggr, SWT.CENTER);
		tbl_aggr_index.setWidth(180);
		TableColumn tbl_aggr_data = new TableColumn(tbl_aggr, SWT.LEFT);
		tbl_aggr_data.setWidth(520);

		lb_peak = new Label(this, SWT.NONE);
		lb_peak.setText("¡ß Peak");
		lb_peak.setFont(Utility.getFont(SWT.BOLD));
		lb_peak.setBounds(278, 415, 300, 18);
		tbl_peak = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.NO_SCROLL);
		tbl_peak.setLinesVisible(true);
		tbl_peak.setHeaderVisible(true);
		tbl_peak.setBounds(278, 435, 700, 103);
		tbl_peak.setFont(Utility.getFont(SWT.BOLD));
		TableColumn tbl_peak_null = new TableColumn(tbl_peak, SWT.NONE);
		tbl_peak_null.setWidth(0);
		TableColumn tbl_peak_index = new TableColumn(tbl_peak, SWT.CENTER);
		tbl_peak_index.setWidth(180);
		tbl_peak_index.setText("¡¬");
		TableColumn tbl_peak_time = new TableColumn(tbl_peak, SWT.LEFT);
		tbl_peak_time.setWidth(260);
		tbl_peak_time.setText("Peak Time");
		TableColumn tbl_peak_count = new TableColumn(tbl_peak, SWT.LEFT);
		tbl_peak_count.setWidth(260);
		tbl_peak_count.setText("Requests");

		lb_bad = new Label(this, SWT.NONE);
		lb_bad.setText("¡ß Bad Requests");
		lb_bad.setFont(Utility.getFont(SWT.BOLD));
		lb_bad.setBounds(278, 555, 300, 18);
		tbl_bad = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.NO_SCROLL);
		tbl_bad.setLinesVisible(true);
		tbl_bad.setHeaderVisible(true);
		tbl_bad.setBounds(278, 575, 700, 84);
		tbl_bad.setFont(Utility.getFont(SWT.BOLD));
		TableColumn tbl_bad_null = new TableColumn(tbl_bad, SWT.NONE);
		tbl_bad_null.setWidth(0);
		TableColumn tbl_bad_index = new TableColumn(tbl_bad, SWT.CENTER);
		tbl_bad_index.setWidth(180);
		tbl_bad_index.setText("¡¬");
		TableColumn tbl_bad_time = new TableColumn(tbl_bad, SWT.LEFT);
		tbl_bad_time.setWidth(260);
		tbl_bad_time.setText("Condition");
		TableColumn tbl_bad_count = new TableColumn(tbl_bad, SWT.LEFT);
		tbl_bad_count.setWidth(260);
		tbl_bad_count.setText("Requests");

	}

	public void load() throws Exception {
		initDatabase();
		
		SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
		TableItem item;
		DecimalFormat DF_Percent = new DecimalFormat("##0.000");
		SimpleDateFormat local_tz_sdf = (SimpleDateFormat)DateUtil.SDF_DATETIME_TZ.clone();
		local_tz_sdf.setTimeZone(Constant.TIMEZONE_DEFAULT);		

		item = new TableItem(tbl_time, SWT.NONE);
		item.setText(1, "Parsed Date");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, DateUtil.dateToString(summaryVo.getParsedTime(), local_tz_sdf));
		item = new TableItem(tbl_time, SWT.NONE);
		item.setText(1, "Created Date");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, DateUtil.dateToString(summaryVo.getCreatedTime(), local_tz_sdf));
		
		String range_filter = "N/A";
		if(settingVo.isDateFilterEnable()) {
			range_filter = DateUtil.dateToString(settingVo.getDateFilterFromRange(), DateUtil.SDF_DATETIME) + " ~ " + DateUtil.dateToString(settingVo.getDateFilterToRange(), DateUtil.SDF_DATETIME); 
		}
		item = new TableItem(tbl_filter, SWT.NONE);
		item.setText(1, "Time Range Filter");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, range_filter);
		item = new TableItem(tbl_filter, SWT.NONE);
		item.setText(1, "Include Filter");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, summaryVo.getFilterIncludeInfo());
		item = new TableItem(tbl_filter, SWT.NONE);
		item.setText(1, "Exclude Filter");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, summaryVo.getFilterExcludeInfo());

		double pct_req = (double)summaryVo.getFilteredRequestCount() / summaryVo.getTotalRequestCount() * 100;
		String pct_req_str = " (" + (Double.isNaN(pct_req) ? 0 : DF_Percent.format(pct_req)) + "%)";
		item = new TableItem(tbl_aggr, SWT.NONE);
		item.setText(1, "Request Range");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, DateUtil.dateToString(summaryVo.getFirstRequestTime(), DateUtil.SDF_DATETIME) + " ~ " + DateUtil.dateToString(summaryVo.getLastRequestTime(), DateUtil.SDF_DATETIME));
		item = new TableItem(tbl_aggr, SWT.NONE);
		item.setText(1, "Requests");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, NumberUtil.numberToString(summaryVo.getFilteredRequestCount()) + " / " + NumberUtil.numberToString(summaryVo.getTotalRequestCount()) + pct_req_str);
		item = new TableItem(tbl_aggr, SWT.NONE);
		item.setText(1, "URI");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, NumberUtil.numberToString(summaryVo.getFilteredUriCount()));
		item = new TableItem(tbl_aggr, SWT.NONE);
		item.setText(1, "IP");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, NumberUtil.numberToString(summaryVo.getFilteredIpCount()));
		item = new TableItem(tbl_aggr, SWT.NONE);
		item.setText(1, "METHOD");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, NumberUtil.numberToString(summaryVo.getFilteredMethodCount()));
		item = new TableItem(tbl_aggr, SWT.NONE);
		item.setText(1, "EXT");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, NumberUtil.numberToString(summaryVo.getFilteredExtCount()));
		item = new TableItem(tbl_aggr, SWT.NONE);
		item.setText(1, "CODE");
		item.setFont(1, Utility.getFont(SWT.BOLD));
		item.setText(2, NumberUtil.numberToString(summaryVo.getFilteredCodeCount()));

		item = new TableItem(tbl_peak, SWT.NONE);
		item.setText(1, "Daily Peak");
		item.setText(2, DateUtil.dateToString(summaryVo.getDailyPeakTime(), DateUtil.SDF_DATE));
		item.setFont(2, Utility.getFont());
		item.setText(3, NumberUtil.numberToString(summaryVo.getDailyPeakCount()));
		item.setFont(3, Utility.getFont());
		item = new TableItem(tbl_peak, SWT.NONE);
		item.setText(1, "Houly Peak");
		item.setText(2, DateUtil.dateToString(summaryVo.getHourlyPeakTime(), DateUtil.SDF_TIME_NOSEC));
		item.setFont(2, Utility.getFont());
		item.setText(3, NumberUtil.numberToString(summaryVo.getHourlyPeakCount()));
		item.setFont(3, Utility.getFont());
		item = new TableItem(tbl_peak, SWT.NONE);
		item.setText(1, "Milutely Peak");
		item.setText(2, DateUtil.dateToString(summaryVo.getMinutelyPeakTime(), DateUtil.SDF_DATETIME_NOSEC));
		item.setFont(2, Utility.getFont());
		item.setText(3, NumberUtil.numberToString(summaryVo.getMinutelyPeakCount()));
		item.setFont(3, Utility.getFont());
		item = new TableItem(tbl_peak, SWT.NONE);
		item.setText(1, "Secondly Peak");
		if(summaryVo.getSecondlyPeakTime() != null) {
			item.setText(2, DateUtil.dateToString(summaryVo.getSecondlyPeakTime(), DateUtil.SDF_DATETIME));
			item.setFont(2, Utility.getFont());
			item.setText(3, NumberUtil.numberToString(summaryVo.getSecondlyPeakCount()));
			item.setFont(3, Utility.getFont());
		} else {
			item.setText(2, "N/A");
			item.setFont(2, Utility.getFont());
			item.setText(3, "N/A");
			item.setFont(3, Utility.getFont());
		}

		double pct_time = (double)summaryVo.getBadElapsedCount() / summaryVo.getFilteredRequestCount() * 100;
		String pct_time_str = " (" + (Double.isNaN(pct_time) ? 0 : DF_Percent.format(pct_time)) + "%)";
		double pct_size = (double)summaryVo.getBadByteCount() / summaryVo.getFilteredRequestCount() * 100;
		String pct_size_str = " (" + (Double.isNaN(pct_size) ? 0 : DF_Percent.format(pct_size)) + "%)";
		double pct_error = (double)summaryVo.getBadCodeCount() / summaryVo.getFilteredRequestCount() * 100;
		String pct_error_str = " (" + (Double.isNaN(pct_error) ? 0 : DF_Percent.format(pct_error)) + "%)";
		item = new TableItem(tbl_bad, SWT.NONE);
		item.setText(1, "Over Time");
		item.setText(2, ">= " + NumberUtil.numberToString(settingVo.getCollectElapsedTimeMS()) + "ms");
		item.setFont(2, Utility.getFont());
		item.setText(3, NumberUtil.numberToString(summaryVo.getBadElapsedCount()) + pct_time_str);
		item.setFont(3, Utility.getFont());
		item = new TableItem(tbl_bad, SWT.NONE);
		item.setText(1, "Over Size");
		item.setText(2, ">= " + NumberUtil.numberToString(settingVo.getCollectResponseBytesKB()) + "KB");
		item.setFont(2, Utility.getFont());
		item.setText(3, NumberUtil.numberToString(summaryVo.getBadByteCount()) + pct_size_str);
		item.setFont(3, Utility.getFont());
		item = new TableItem(tbl_bad, SWT.NONE);
		item.setText(1, "Error Code");
		item.setText(2, "4xx, 5xx");
		item.setFont(2, Utility.getFont());
		item.setText(3, NumberUtil.numberToString(summaryVo.getBadCodeCount()) + pct_error_str);
		item.setFont(3, Utility.getFont());

		closeDatabase();
	}
	
	public void resize(Rectangle rect) {
		lb_title.setLocation((rect.width-40-lb_title.getBounds().width)/2, lb_title.getBounds().y);
		tbl_time.setLocation((rect.width-40-tbl_time.getBounds().width)/2, tbl_time.getBounds().y);
		lb_time.setLocation(tbl_time.getBounds().x, lb_time.getBounds().y);
		tbl_filter.setLocation((rect.width-40-tbl_filter.getBounds().width)/2, tbl_filter.getBounds().y);
		lb_filter.setLocation(tbl_filter.getBounds().x, lb_filter.getBounds().y);
		tbl_aggr.setLocation((rect.width-40-tbl_aggr.getBounds().width)/2, tbl_aggr.getBounds().y);
		lb_aggr.setLocation(tbl_aggr.getBounds().x, lb_aggr.getBounds().y);
		tbl_peak.setLocation((rect.width-40-tbl_peak.getBounds().width)/2, tbl_peak.getBounds().y);
		lb_peak.setLocation(tbl_peak.getBounds().x, lb_peak.getBounds().y);
		tbl_bad.setLocation((rect.width-40-tbl_bad.getBounds().width)/2, tbl_bad.getBounds().y);
		lb_bad.setLocation(tbl_bad.getBounds().x, lb_bad.getBounds().y);
	}

	public void resetData() {
		tbl_time.removeAll();
		tbl_filter.removeAll();
		tbl_aggr.removeAll();
		tbl_peak.removeAll();
		tbl_bad.removeAll();
	}

}
