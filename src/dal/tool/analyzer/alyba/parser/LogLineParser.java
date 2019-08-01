package dal.tool.analyzer.alyba.parser;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.TimeComparator;
import dal.tool.analyzer.alyba.output.ValueComparator;
import dal.tool.analyzer.alyba.output.vo.DailyEntryVO;
import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.HourlyEntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.output.vo.ResponseEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPSEntryVO;
import dal.tool.analyzer.alyba.setting.AnalyzerSetting;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.DateUtil;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.ProgressBarTask;

public abstract class LogLineParser {

	protected static final String STR_DATE = "yyyy.MM.dd";
	protected static final String STR_DATETIME = "yyyy.MM.dd HH:mm:ss";

	protected LogReaderThread thread = null;
	protected AnalyzerSetting setting = null;
	protected ObjectDBUtil db = null;
	protected EntityManager em = null;
	protected HashMap<String, List<String>> include_filter_regex = new HashMap<String, List<String>>();
	protected HashMap<String, List<String>> exclude_filter_regex = new HashMap<String, List<String>>();
	protected boolean isFailed = false;
	protected boolean isCanceled = false;

	protected HashMap<String, FieldIndex[]> fieldIndexCache = new HashMap<String, FieldIndex[]>();
	protected double responseTimeUnitCache = -1D;
	protected HashMap<String, List<EntryVO>> aggr_data = new HashMap<String, List<EntryVO>>();

	protected int total_request_count = 0;
	protected int total_error_count = 0;
	protected int filtered_request_count = 0;
	protected int filtered_error_count = 0;
	protected Date first_time = null;
	protected Date last_time = null;
	protected Date parsed_time = null;
	protected SimpleDateFormat sdf = null;
	protected SimpleDateFormat sdf_date = null;
	protected SimpleDateFormat sdf_datetime = null;
	protected String under_second_format = null;
	protected DateTimeFormatter usf = null;
	protected String delimeter = null;
	protected String[] bracelets = null;

	public LogLineParser(AnalyzerSetting setting) throws Exception {
		this.setting = setting;
		this.parsed_time = new Date();
		this.delimeter = setting.fieldMapping.getFieldDelimeter();
		this.bracelets = setting.fieldMapping.getFieldBracelets();
		setFilterRegex();
		if(!setting.fieldMapping.timeFormat.equals(Constant.UNIX_TIME_STR)) {
			Matcher m = Pattern.compile("S+").matcher(setting.fieldMapping.timeFormat);
			if(m.find()) {
				under_second_format = setting.fieldMapping.timeFormat.substring(m.start(), m.end());			
			}
		}
	}
	
	protected abstract void aggregate(ResponseEntryVO vo) throws Exception;

	public abstract void sortData() throws Exception;

	public abstract void writeDataToDB() throws Exception;


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
	
	public LogReaderThread getThread() {
		return thread;
	}
	
	public void setThread(LogReaderThread thread) {
		this.thread = thread;
	}
	
	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed() {
		isFailed = true;
	}

	public boolean isCanceled() {
		return isCanceled;
	}
	
	public void cancel() {
		isCanceled = true;
	}
	
	public AnalyzerSetting getSetting() {
		return setting;
	}

	public void setFilterRegex() throws Exception {
		if(setting.filterSetting.includeFilterEnable) {
			HashMap<String, String> includeFilterMap = setting.filterSetting.includeFilterInfo;
			boolean includeFilterIgnoreCase = setting.filterSetting.includeFilterIgnoreCase;
			List<String> l;
			l = Utility.StringToList((String)includeFilterMap.get("EXT"), ",", includeFilterIgnoreCase);
			if(l != null)
				include_filter_regex.put("EXT", l);
			l = Utility.StringToList((String)includeFilterMap.get("URI"), ",", includeFilterIgnoreCase);
			if(l != null)
				include_filter_regex.put("URI", l);
			l = Utility.StringToList((String)includeFilterMap.get("CODE"), ",");
			if(l != null)
				include_filter_regex.put("CODE", l);
			l = Utility.StringToList((String)includeFilterMap.get("METHOD"), ",", includeFilterIgnoreCase);
			if(l != null)
				include_filter_regex.put("METHOD", l);
			l = Utility.StringToList((String)includeFilterMap.get("VERSION"), ",", includeFilterIgnoreCase);
			if(l != null)
				include_filter_regex.put("VERSION", l);
			l = Utility.StringToList((String)includeFilterMap.get("IP"), ",");
			if(l != null)
				include_filter_regex.put("IP", l);
		}
		if(setting.filterSetting.excludeFilterEnable) {
			HashMap<String, String> excludeFilterMap = setting.filterSetting.excludeFilterInfo;
			boolean excludeFilterIgnoreCase = setting.filterSetting.excludeFilterIgnoreCase;
			List<String> l;
			l = Utility.StringToList((String)excludeFilterMap.get("EXT"), ",", excludeFilterIgnoreCase);
			if(l != null)
				exclude_filter_regex.put("EXT", l);
			l = Utility.StringToList((String)excludeFilterMap.get("URI"), ",", excludeFilterIgnoreCase);
			if(l != null)
				exclude_filter_regex.put("URI", l);
			l = Utility.StringToList((String)excludeFilterMap.get("CODE"), ",");
			if(l != null)
				exclude_filter_regex.put("CODE", l);
			l = Utility.StringToList((String)excludeFilterMap.get("METHOD"), ",", excludeFilterIgnoreCase);
			if(l != null)
				exclude_filter_regex.put("METHOD", l);
			l = Utility.StringToList((String)excludeFilterMap.get("VERSION"), ",", excludeFilterIgnoreCase);
			if(l != null)
				exclude_filter_regex.put("VERSION", l);
			l = Utility.StringToList((String)excludeFilterMap.get("IP"), ",");
			if(l != null)
				exclude_filter_regex.put("IP", l);
		}
	}

	public HashMap<String, List<EntryVO>> getAggregationData() {
		return aggr_data;
	}

	public int getTotalRequestCount() {
		return total_request_count;
	}

	public Date getFirstRequestTime() {
		return first_time;
	}

	public Date getLastRequestTime() {
		return last_time;
	}

	public Date getParsedTime() {
		return parsed_time;
	}

	public int getFilteredRequestCount() {
		return filtered_request_count;
	}

	public int getTotalErrorCount() {
		return total_error_count;
	}

	public int getFilteredErrorCount() {
		return filtered_error_count;
	}

	public int getLongTransactionCount() {
		try {
			return (int)(long)db.select(em, "SELECT COUNT(o) FROM BadResponseEntryVO AS o WHERE o.type = 'TIME'", Long.class, null);
		} catch(Exception e) {			
			return -1;
		}		
	}

	public int getLargeResponseCount() {
		try {
			return (int)(long)db.select(em, "SELECT COUNT(o) FROM BadResponseEntryVO AS o WHERE o.type = 'SIZE'", Long.class, null);
		} catch(Exception e) {			
			return -1;
		}		
	}

	public int getErrorResponseCount() {
		try {
			return (int)(long)db.select(em, "SELECT COUNT(o) FROM BadResponseEntryVO AS o WHERE o.type = 'CODE'", Long.class, null);
		} catch(Exception e) {			
			return -1;
		}		
	}

	public int getFilteredURICount() {
		List<EntryVO> v = (List<EntryVO>)aggr_data.get("URI");
		return (v == null) ? 0 : v.size();
	}

	public int getFilteredExtensionCount() {
		List<EntryVO> v = (List<EntryVO>)aggr_data.get("EXT");
		return (v == null) ? 0 : v.size();
	}

	public int getFilteredIPAddressCount() {
		List<EntryVO> v = (List<EntryVO>)aggr_data.get("IP");
		return (v == null) ? 0 : v.size();
	}

	public int getFilteredMethodCount() {
		List<EntryVO> v = (List<EntryVO>)aggr_data.get("METHOD");
		return (v == null) ? 0 : v.size();
	}

	public int getFilteredCodeCount() {
		List<EntryVO> v = (List<EntryVO>)aggr_data.get("CODE");
		return (v == null) ? 0 : v.size();
	}

	private void aggregate(Date dt, String ip, String uri, String ext, String code, String method, String version, long rtime, long rbyte) throws Exception {
		if(ip != null)
			ip = ip.trim();
		if(ext != null)
			ext = ext.trim();
		if(code != null)
			code = code.trim();
		if(method != null)
			method = method.trim();
		if(version != null)
			version = version.trim();
		if(uri != null)
			uri = uri.trim();
		String uri_result = uri;
		if(uri_result != null && setting.uriIncludeParams) {
			uri_result = (uri_result.indexOf("?") > 0) ? uri_result.substring(0, uri_result.indexOf("?")) : uri_result;
		}
		boolean isError = false;
		if(code != null && (code.startsWith("4") || code.startsWith("5"))) {
			isError = true;
		}
		
		total_request_count++;
		if(isError) {
			total_error_count++;
		}

		if(!allowedAggregation(dt, ip, uri_result, ext, code, method, version)) {
			return;
		}

		filtered_request_count++;
		if(isError) {
			filtered_error_count++;
		}

		if(first_time == null || first_time.compareTo(dt) > 0) {
			first_time = dt;
		}
		if(last_time == null || last_time.compareTo(dt) < 0) {
			last_time = dt;
		}

		ResponseEntryVO vo = new ResponseEntryVO(dt, uri_result, ip, rtime, rbyte, code, method, version, ext);
		aggregate(vo);

	}
	
	protected boolean allowedAggregation(Date dt, String ip, String uri, String ext, String code, String method, String version) throws Exception {
		if(!setting.filterSetting.isAllRangeEnable()) {
			if(setting.filterSetting.getFromDateRange().compareTo(dt) > 0 || setting.filterSetting.getToDateRange().compareTo(dt) < 0) {
				return false;
			}
		}
		if(allowedByAllIncludeFilter(ip, uri, ext, code, method, version)) {
			return allowedByAllExcludeFilter(ip, uri, ext, code, method, version);
		} else {
			return false;
		}
	}

	private boolean allowedByAllIncludeFilter(String ip, String uri, String ext, String code, String method, String version) throws Exception {
		if(setting.filterSetting.includeFilterEnable) {
			if(setting.filterSetting.includeFilterAndCheck) {
				if(matchedByIncludeFilter("IP", ip) && matchedByIncludeFilter("URI", uri) && matchedByIncludeFilter("EXT", ext) && matchedByIncludeFilter("CODE", code) && matchedByIncludeFilter("METHOD", method) && matchedByIncludeFilter("VERSION", version)) {
					return true;
				} else {
					return false;
				}
			} else {
				if(matchedByIncludeFilter("IP", ip) || matchedByIncludeFilter("URI", uri) || matchedByIncludeFilter("EXT", ext) || matchedByIncludeFilter("CODE", code) || matchedByIncludeFilter("METHOD", method) || matchedByIncludeFilter("VERSION", version)) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			return true;
		}
	}

	private boolean allowedByAllExcludeFilter(String ip, String uri, String ext, String code, String method, String version) throws Exception {
		if(setting.filterSetting.excludeFilterEnable) {
			if(setting.filterSetting.excludeFilterAndCheck) {
				if(matchedByExcludeFilter("IP", ip) && matchedByExcludeFilter("URI", uri) && matchedByExcludeFilter("EXT", ext) && matchedByExcludeFilter("CODE", code) && matchedByExcludeFilter("METHOD", method) && matchedByExcludeFilter("VERSION", version)) {
					return false;
				} else {
					return true;
				}
			} else {
				if(matchedByExcludeFilter("IP", ip) || matchedByExcludeFilter("URI", uri) || matchedByExcludeFilter("EXT", ext) || matchedByExcludeFilter("CODE", code) || matchedByExcludeFilter("METHOD", method) || matchedByExcludeFilter("VERSION", version)) {
					return false;
				} else {
					return true;
				}
			}
		} else {
			return true;
		}
	}
	
	private boolean matchedByIncludeFilter(String field, String str) throws Exception {
		List<String> l = (List<String>)include_filter_regex.get(field);
		if(l == null) {
			return (setting.filterSetting.includeFilterAndCheck) ? true : false;
		} else {
			if(str == null) {
				return true;
			}
			return Utility.containsByRegex(l, str);
		}
	}

	private boolean matchedByExcludeFilter(String field, String str) throws Exception {
		List<String> l = (List<String>)exclude_filter_regex.get(field);
		if(l == null) {
			return (setting.filterSetting.excludeFilterAndCheck) ? true : false;
		} else {
			if(str == null) {
				return true;
			}
			return Utility.containsByRegex(l, str);
		}
	}

	public void fillWithNullTime(ProgressBarTask progressbar) throws Exception {
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_time != null) {
			progressbar.setDetailSubMessage("filling null-data for TPM");
			Collections.sort(aggr_time, new TimeComparator());
			int size = aggr_time.size();
			if(size > 0) {
				DateEntryVO vo_f = (DateEntryVO)aggr_time.get(0);
				Date dt_f = DateUtil.getFirstOfDay(vo_f.getUnitDate());
				Calendar first = Calendar.getInstance();
				first.setTime(dt_f);
				DateEntryVO vo_l = (DateEntryVO)aggr_time.get(size - 1);
				Date dt_l = DateUtil.getLastOfDay(vo_l.getUnitDate());
				Calendar last = Calendar.getInstance();
				last.setTime(DateUtil.getLastOfDay(dt_l));
				int total_count = (int)((DateUtil.diff(dt_f, dt_l, TimeUnit.MINUTES)+1) / setting.tpmUnitMinutes);
				int curr_count = 1;
				while(first.compareTo(last) < 0) {
					if(thread.isStopped() || isCanceled) {
						throw new InterruptedException();
					}
					addNullMinute(first.getTime());
					first = DateUtil.addCalendarUnit(first, Calendar.MINUTE, setting.tpmUnitMinutes);
					if(curr_count%10 == 0 || curr_count == total_count) {
						progressbar.setDetailSubMessage("filling null-data for TPM : " + curr_count + "/" + total_count, false);
					}
					curr_count++;
				}
			}
		}

		aggr_time = (List<EntryVO>)aggr_data.get("DAY");
		if(aggr_time != null) {
			progressbar.setDetailSubMessage("filling null-data for DAY");
			Collections.sort(aggr_time, new TimeComparator());
			int size = aggr_time.size();
			if(size > 0) {
				DateEntryVO vo_f = (DateEntryVO)aggr_time.get(0);
				Date dt_f = vo_f.getUnitDate();
				Calendar first = Calendar.getInstance();
				first.setTime(dt_f);
				DateEntryVO vo_l = (DateEntryVO)aggr_time.get(size - 1);
				Date dt_l = vo_l.getUnitDate();
				Calendar last = Calendar.getInstance();
				last.setTime(dt_l);
				first = DateUtil.addCalendarUnit(first, Calendar.DATE, 1);
				int total_count = (int)(DateUtil.diff(dt_f, dt_l, TimeUnit.DAYS)+1);
				int curr_count = 1;
				while(last.compareTo(first) > 0) {
					if(thread.isStopped() || isCanceled) {
						throw new InterruptedException();
					}
					addNullDay(first.getTime());
					first = DateUtil.addCalendarUnit(first, Calendar.DATE, 1);
					progressbar.setDetailSubMessage("filling null-data for DAY : " + curr_count + "/" + total_count);
					curr_count++;
				}
			}
		}

		aggr_time = (List<EntryVO>)aggr_data.get("HOUR");
		if(aggr_time != null) {
			progressbar.setDetailSubMessage("filling null-data for HOUR");
			Collections.sort(aggr_time, new TimeComparator());
			int size = aggr_time.size();
			if(size > 0) {
				DateEntryVO vo = (DateEntryVO)aggr_time.get(0);
				Date d = DateUtil.getFirstOfDay(vo.getUnitDate());
				Calendar first = Calendar.getInstance();
				first.setTime(d);
				Calendar last = DateUtil.addCalendarUnit(first, Calendar.DATE, 1);
				int curr_count = 1;
				while(first.get(Calendar.DATE) < last.get(Calendar.DATE)) {
					if(thread.isStopped() || isCanceled) {
						throw new InterruptedException();
					}
					addNullHour(first.getTime());
					first = DateUtil.addCalendarUnit(first, Calendar.HOUR_OF_DAY, 1);
					progressbar.setDetailSubMessage("filling null-data for DAY : " + curr_count + "/24");
					curr_count++;
				}
			}
		}

		aggr_time = (List<EntryVO>)aggr_data.get("TPS");
		if(aggr_time != null) {
			progressbar.setDetailSubMessage("filling null-data for TPS");
			Collections.sort(aggr_time, new TimeComparator());
			int size = aggr_time.size();
			if(size > 0) {
				DateEntryVO vo_f = (DateEntryVO)aggr_time.get(0);
				Date dt_f = DateUtil.getFirstOfDay(vo_f.getUnitDate());
				Calendar first = Calendar.getInstance();
				first.setTime(dt_f);
				DateEntryVO vo_l = (DateEntryVO)aggr_time.get(size - 1);
				Date dt_l = DateUtil.getLastOfDay(vo_l.getUnitDate());
				Calendar last = Calendar.getInstance();
				last.setTime(DateUtil.getLastOfDay(dt_l));
				int total_count = (int)(DateUtil.diff(dt_f, dt_l, TimeUnit.SECONDS)+1);
				int curr_count = 1;
				while(first.compareTo(last) <= 0) {
					if(thread.isStopped() || isCanceled) {
						throw new InterruptedException();
					}
					addNullSecond(first.getTime());
					first = DateUtil.addCalendarUnit(first, Calendar.SECOND, 1);
					if(curr_count%100 == 0 || curr_count == total_count) {
						progressbar.setDetailSubMessage("filling null-data for TPS : " + curr_count + "/" + total_count, false);
					}
					curr_count++;
				}
			}
		}
	}

	protected void addNullMinute(Date dt) throws Exception {
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_time == null) {
			aggr_time = new ArrayList<EntryVO>();
			aggr_data.put("TPM", aggr_time);
		}
		TPMEntryVO vo;
		int size = aggr_time.size();
		boolean flag = false;
		for(int i = 0; i < size; i++) {
			vo = (TPMEntryVO)aggr_time.get(i);
			if(vo.getUnitDate().equals(dt)) {
				flag = true;
				break;
			}
		}
		if(!flag) {
			vo = new TPMEntryVO(dt);
			if(!setting.fieldMapping.isMappedElapsed()) {
				vo.setAverageResponseTime(-1D);
			}
			if(!setting.fieldMapping.isMappedBytes()) {
				vo.setAverageResponseBytes(-1D);
			}
			aggr_time.add(vo);
		}
	}

	protected void addNullSecond(Date dt) throws Exception {
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("TPS");
		if(aggr_time == null) {
			aggr_time = new ArrayList<EntryVO>();
			aggr_data.put("TPS", aggr_time);
		}
		TPSEntryVO vo;
		int size = aggr_time.size();
		boolean flag = false;
		for(int i = 0; i < size; i++) {
			vo = (TPSEntryVO)aggr_time.get(i);
			if(vo.getUnitDate().equals(dt)) {
				flag = true;
				break;
			}
		}
		if(!flag) {
			vo = new TPSEntryVO(dt);
			if(!setting.fieldMapping.isMappedElapsed()) {
				vo.setAverageResponseTime(-1D);
			}
			if(!setting.fieldMapping.isMappedBytes()) {
				vo.setAverageResponseBytes(-1D);
			}
			aggr_time.add(vo);
		}
	}

	protected void addNullDay(Date dt) throws Exception {
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("DAY");
		if(aggr_time == null) {
			aggr_time = new ArrayList<EntryVO>();
			aggr_data.put("DAY", aggr_time);
		}
		DailyEntryVO vo;
		int size = aggr_time.size();
		boolean flag = false;
		for(int i = 0; i < size; i++) {
			vo = (DailyEntryVO)aggr_time.get(i);
			if(vo.getUnitDate().equals(dt)) {
				flag = true;
				break;
			}
		}
		if(!flag) {
			vo = new DailyEntryVO(dt);
			if(!setting.fieldMapping.isMappedElapsed()) {
				vo.setAverageResponseTime(-1D);
			}
			if(!setting.fieldMapping.isMappedBytes()) {
				vo.setAverageResponseBytes(-1D);
			}
			aggr_time.add(vo);
		}
	}

	protected void addNullHour(Date dt) throws Exception {
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("HOUR");
		if(aggr_time == null) {
			aggr_time = new ArrayList<EntryVO>();
			aggr_data.put("HOUR", aggr_time);
		}
		HourlyEntryVO vo;
		int size = aggr_time.size();
		boolean flag = false;
		for(int i = 0; i < size; i++) {
			vo = (HourlyEntryVO)aggr_time.get(i);
			if(vo.getUnitDate().equals(dt)) {
				flag = true;
				break;
			}
		}
		if(!flag) {
			vo = new HourlyEntryVO(dt);
			if(!setting.fieldMapping.isMappedElapsed()) {
				vo.setAverageResponseTime(-1D);
			}
			if(!setting.fieldMapping.isMappedBytes()) {
				vo.setAverageResponseBytes(-1D);
			}
			aggr_time.add(vo);
		}
	}


	protected KeyEntryVO getContainsKey(List<EntryVO> list, String key) {
		int size = list.size();
		KeyEntryVO vo;
		String vokey;
		for(int i = 0; i < size; i++) {
			vo = (KeyEntryVO)list.get(i);
			vokey = vo.getKey();
			if(vokey != null && vokey.equals(key)) {
				return vo;
			}
		}
		return null;
	}

	protected DateEntryVO getContainsKey(List<EntryVO> list, Date dt) {
		int size = list.size();
		DateEntryVO vo;
		Date unitdt;
		for(int i = 0; i < size; i++) {
			vo = (DateEntryVO)list.get(i);
			unitdt = vo.getUnitDate();
			if(unitdt != null && unitdt.equals(dt)) {
				return vo;
			}
		}
		return null;
	}

	public void setTotalToResult() {
		Iterator<String> it = aggr_data.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			List<EntryVO> list = (List<EntryVO>)aggr_data.get(key);
			int size = list.size();
			for(int i = 0; i < size; i++) {
				EntryVO tempVO = list.get(i);
				if(tempVO instanceof DateEntryVO) {
					DateEntryVO vo = (DateEntryVO)tempVO;
					vo.setTotal(total_request_count);
					vo.setFilteredTotal(filtered_request_count);					
				} else {
					KeyEntryVO vo = (KeyEntryVO)tempVO;
					vo.setTotal(total_request_count);
					vo.setFilteredTotal(filtered_request_count);
				}
			}
		}
	}

	public <K extends KeyEntryVO,D extends DateEntryVO> void mergeData(LogLineParser sub) throws Exception {
		HashMap<String, List<EntryVO>> aggr_data_sub = sub.getAggregationData();
		Set<String> all_set = new HashSet<String>();
		all_set.addAll(aggr_data.keySet());
		all_set.addAll(aggr_data_sub.keySet());
		Iterator<String> it = all_set.iterator();
		while(it.hasNext()) {
			if(thread.isStopped() || isCanceled) {
				throw new InterruptedException();
			}
			String key = (String)it.next();
			List<EntryVO> list_main = (List<EntryVO>)aggr_data.get(key);
			List<EntryVO> list_sub = (List<EntryVO>)aggr_data_sub.get(key);
			if(list_main != null) {
				if(list_sub != null) {
					int size = list_sub.size();
					EntryVO tempVO;
					for(int i = 0; i < size; i++) {
						tempVO = list_sub.get(i);
						if(tempVO instanceof DateEntryVO) {
							DateEntryVO subVO = (DateEntryVO)tempVO;
							DateEntryVO mainVO = getContainsKey(list_main, ((DateEntryVO)subVO).getUnitDate());
							if(mainVO == null) {
								list_main.add(subVO);
							} else {
								DateEntryVO mergedVO = mainVO.merge(subVO);
								list_main.set(list_main.indexOf(mainVO), mergedVO);
							}
						} else {
							KeyEntryVO subVO = (KeyEntryVO)tempVO;
							KeyEntryVO mainVO = getContainsKey(list_main, ((KeyEntryVO)subVO).getKey());
							if(mainVO == null) {
								list_main.add(subVO);
							} else {
								KeyEntryVO mergedVO = mainVO.merge(subVO);
								list_main.set(list_main.indexOf(mainVO), mergedVO);
							}
						}
					}
				}
			} else {
				if(list_sub != null) {
					list_main = new ArrayList<EntryVO>();
					list_main.addAll(list_sub);
					aggr_data.put(key, list_main);
				}
			}
		}
		aggr_data_sub.clear();
		total_request_count += sub.getTotalRequestCount();
		filtered_request_count += sub.getFilteredRequestCount();
		total_error_count += sub.getTotalErrorCount();
		filtered_error_count += sub.getFilteredErrorCount();
		if(first_time == null || (sub.getFirstRequestTime() != null && first_time.compareTo(sub.getFirstRequestTime()) > 0)) {
			first_time = sub.getFirstRequestTime();
		}
		if(last_time == null || (sub.getLastRequestTime() != null && last_time.compareTo(sub.getLastRequestTime()) < 0)) {
			last_time = sub.getLastRequestTime();
		}
	}

	protected void sortDataByTime(List<EntryVO> list) {
		sortDataByTime(list, new TimeComparator());
	}

	protected void sortDataByValue(List<EntryVO> list) {
		sortDataByValue(list, new ValueComparator(setting.outputSortBy));
	}

	protected void sortDataByTime(List<EntryVO> list, TimeComparator comp) {
		if(list != null) {
			Collections.sort(list, comp);
		}
	}

	protected void sortDataByValue(List<EntryVO> list, ValueComparator comp) {
		if(list != null) {
			Collections.sort(list, comp);
			if(comp.getSortField().toUpperCase().equals("KEY")) {
				int size = list.size();
				KeyEntryVO vo;
				for(int i = 0; i < size; i++) {
					vo = (KeyEntryVO)list.get(i);
					if(vo.getKey().equals("UNKNOWN") || vo.getKey().equals("NO_EXT")) {
						list.remove(i);
						list.add(vo);
					}
				}
			}
		}
	}

	private FieldIndex[] getIndexOfField(String fld_name) throws Exception {
		FieldIndex[] arr = fieldIndexCache.get(fld_name);
		if(arr != null) {
			return arr;
		}
		String idx_str = setting.fieldMapping.mappingInfo.get(fld_name);
		if(idx_str == null) {
			arr = null;
		} else {
			if(idx_str.indexOf(',') > 0) {
				String[] s_arr = idx_str.split(",");
				arr = new FieldIndex[s_arr.length];
				for(int i = 0; i < s_arr.length; i++) {
					arr[i] = new FieldIndex(fld_name + "_" + i, s_arr[i]);
				}
			} else {
				arr = new FieldIndex[] { (new FieldIndex(fld_name, idx_str)) };
			}
		}
		fieldIndexCache.put(fld_name, arr);
		return arr;
	}

	private String getStringOfField(List<String> fld_tokens, String fld_name) {
		try {
			String fld_str = "";
			FieldIndex[] fld_idx = getIndexOfField(fld_name);
			if(fld_idx == null || fld_idx.length < 1) {
				return null;
			}
			fld_str = fld_idx[0].getField(fld_tokens, delimeter, bracelets);
			fld_str = (fld_str == null) ? "UNKNOWN" : fld_str;
			if(fld_idx.length > 1 && (fld_name.equals("URI") || fld_name.equals("TIME"))) {
				if(fld_str.equals("UNKNOWN")) {
					return null;
				}
				String sub_fld_str = fld_idx[1].getField(fld_tokens, delimeter, bracelets);
				String joinChar = fld_name.equals("URI") ? "?" : " ";
				fld_str += ((sub_fld_str == null) ? "" : (joinChar + sub_fld_str));
			}
			return fld_str;
		} catch(Exception e) {
			Logger.debug(e);
			return null;
		}
	}

	private double getResponseTimeUnit() {
		if(responseTimeUnitCache > 0) {
			return responseTimeUnitCache;
		}
		String unit = setting.fieldMapping.elapsedUnit;
		if(unit.equals(Constant.ELAPSED_TIME_UNITS[1])) {
			responseTimeUnitCache = 1000D;
		} else if(unit.equals(Constant.ELAPSED_TIME_UNITS[2])) {
			responseTimeUnitCache = 0.001D;
		} else {
			responseTimeUnitCache = 1D;
		}
		return responseTimeUnitCache;
	}

	public void parseLine(String line) throws Exception {
		if(line == null || line.trim().equals("") || line.startsWith("#") || line.startsWith("format=")) {
			return;
		}

		List<String> tokenList = ParserUtil.getTokenList(line, delimeter, bracelets);
		if(setting.checkFieldCount && tokenList.size() != setting.fieldMapping.logFieldCount) {
			Logger.debug("Field count is different : " + tokenList.size() + " != " + setting.fieldMapping.logFieldCount);
			throw new Exception("Invaid line : " + line);
		}

		String date_str = getStringOfField(tokenList, "TIME");
		Date dt = null;
		if(date_str != null) {
			if(setting.fieldMapping.timeFormat.equals(Constant.UNIX_TIME_STR)) {
				dt = new Date((long)(Double.parseDouble(date_str)*1000));
			} else {
				if(sdf == null) {
					sdf = new SimpleDateFormat(setting.fieldMapping.timeFormat, setting.fieldMapping.timeLocale);
				}
				if(under_second_format == null) {
					dt = sdf.parse(date_str);
				} else {
					if(usf == null) {
						usf = DateTimeFormatter.ofPattern(setting.fieldMapping.timeFormat, setting.fieldMapping.timeLocale);
					}
					LocalTime localTime = LocalTime.parse(date_str, usf);
					LocalDateTime localDateTime = localTime.atDate(LocalDate.ofEpochDay(0));
					dt = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());		
				}				
			}
			Calendar c = Calendar.getInstance();
			c.setTime(dt);
			if(c.get(Calendar.YEAR) < 1900) {
				throw new Exception("Invaid year : " + date_str);
			}
			if(setting.fieldMapping.offsetHour != 0.0F) {
				int hour = (int)setting.fieldMapping.offsetHour;
				int minute = (int)(60 * (setting.fieldMapping.offsetHour - hour));
				if(hour != 0) {
					c = DateUtil.addCalendarUnit(c, Calendar.HOUR_OF_DAY, hour);
				}
				if(minute != 0) {
					c = DateUtil.addCalendarUnit(c, Calendar.MINUTE, minute);
				}
				dt = c.getTime();
			}
		}

		String ip = getStringOfField(tokenList, "IP");

		String uri = getStringOfField(tokenList, "URI");
		String ext = null;
		if(uri != null) {
			uri = (uri.indexOf(";") > 0) ? uri.substring(0, uri.indexOf(";")) : uri;
			String noparam_uri = (uri.indexOf("?") > 0) ? uri.substring(0, uri.indexOf("?")) : uri;
			String temp = (noparam_uri.lastIndexOf("/") > 0) ? noparam_uri.substring(noparam_uri.lastIndexOf("/")) : noparam_uri;
			ext = (temp.lastIndexOf(".") > 0) ? temp.substring(temp.lastIndexOf(".") + 1) : "NO_EXT";
		}

		String code = getStringOfField(tokenList, "CODE");
		if(code != null) {
			code = code.equals("-") ? "UNKNOWN" : code;
			int idx = ParserUtil.getStartIndexOfDigit(code);
			code = code.substring(idx);
			idx = ParserUtil.getEndIndexOfDigit(code);
			code = code.substring(0, idx);
		}

		String method = getStringOfField(tokenList, "METHOD");

		String version = getStringOfField(tokenList, "VERSION");

		String s_rtime = getStringOfField(tokenList, "ELAPSED");
		long l_rtime = -1L;
		if(s_rtime != null) {
			s_rtime = (s_rtime.equals("-") ? "0" : s_rtime);
			int idx = ParserUtil.getStartIndexOfDigit(s_rtime);
			s_rtime = s_rtime.substring(idx);
			idx = ParserUtil.getEndIndexOfDigit(s_rtime);
			double d_rtime = ParserUtil.stringToDouble(s_rtime.substring(0, idx));
			l_rtime = (long)(d_rtime * getResponseTimeUnit());
		}

		String s_rbyte = getStringOfField(tokenList, "BYTES");
		long l_rbyte = -1L;
		if(s_rbyte != null) {
			s_rbyte = (s_rbyte.equals("-") ? "0" : s_rbyte);
			l_rbyte = ParserUtil.stringToLong(s_rbyte);
		}

		aggregate(dt, ip, uri, ext, code, method, version, l_rtime, l_rbyte);
	}

	public Date getParsedDate(String line) throws Exception {
		if(line == null || line.trim().equals("") || line.startsWith("#") || line.startsWith("format=")) {
			return null;
		}
		List<String> tokenList = ParserUtil.getTokenList(line, delimeter, bracelets);
		String date_str = getStringOfField(tokenList, "TIME");
		Date dt = null;
		if(date_str != null) {
			if(setting.fieldMapping.timeFormat.equals(Constant.UNIX_TIME_STR)) {
				dt = new Date((long)(Double.parseDouble(date_str)*1000));
			} else {
				if(sdf == null) {
					sdf = new SimpleDateFormat(setting.fieldMapping.timeFormat, setting.fieldMapping.timeLocale);
				}
				dt = sdf.parse(date_str);
				Calendar c = Calendar.getInstance();
				c.setTime(dt);
				if(c.get(Calendar.YEAR) < 1900) {
					throw new Exception("Invaid year : " + date_str);
				}
				if(setting.fieldMapping.offsetHour != 0.0F) {
					int hour = (int)setting.fieldMapping.offsetHour;
					int minute = (int)(60 * (setting.fieldMapping.offsetHour - hour));
					if(hour != 0) {
						c = DateUtil.addCalendarUnit(c, Calendar.HOUR_OF_DAY, hour);
					}
					if(minute != 0) {
						c = DateUtil.addCalendarUnit(c, Calendar.MINUTE, minute);
					}
					dt = c.getTime();
				}
			}
		}
		return dt;
	}

}
