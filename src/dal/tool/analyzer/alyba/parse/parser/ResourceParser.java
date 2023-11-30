package dal.tool.analyzer.alyba.parse.parser;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.UnitDateComparator;
import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.parse.FieldIndex;
import dal.tool.analyzer.alyba.parse.FileInfo;
import dal.tool.analyzer.alyba.parse.ParserUtil;
import dal.tool.analyzer.alyba.setting.ResourceAnalyzerSetting;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.DateUtil;
import dal.util.NumberUtil;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.ProgressBarTask;

public class ResourceParser extends FileLineParser {

	protected static final String STR_DATE_SAR = "MM/dd/yyyy";

	protected ResourceAnalyzerSetting setting = null;
	protected ObjectDBUtil db = null;
	protected EntityManager em = null;
	protected HashMap<String, FieldIndex[]> fieldIndexCache = new HashMap<String, FieldIndex[]>();
	protected HashMap<String, List<ResourceUsageEntryVO>> aggr_data = new HashMap<String, List<ResourceUsageEntryVO>>();

	protected int total_count = 0;
	protected Date unit_minute = null;
	protected Date first_time = null;
	protected Date last_time = null;
	protected Calendar prev_cal = null;
	protected Calendar curr_cal = null;
	protected String prev_filepath = null;
	protected SimpleDateFormat sdf = null;
	protected SimpleDateFormat sdf_date_sar = null;
	protected SimpleDateFormat sdf_no_under_score = null;
	protected String under_second_format = null;
	protected DateTimeFormatter usf = null;
	protected String delimeter = null;
	protected String[] bracelets = null;

	public ResourceParser(ResourceAnalyzerSetting setting) throws Exception {
		this.setting = setting;
		this.delimeter = setting.fieldMapping.getFieldDelimeter();
		this.bracelets = setting.fieldMapping.getFieldBracelets();
		if(!setting.fieldMapping.timeFormat.equals(Constant.UNIX_TIME_STR)) {
			Matcher m = Pattern.compile("S+").matcher(setting.fieldMapping.timeFormat);
			if(m.find()) {
				under_second_format = setting.fieldMapping.timeFormat.substring(m.start(), m.end());
				sdf_no_under_score = new SimpleDateFormat(setting.fieldMapping.timeFormat.replaceAll("S+", ""), setting.fieldMapping.timeLocale);
			}
		}
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
	
	public boolean isDatabaseReady() {
		return db != null && db.isReady(em); 
	}
	
	public String getTaskDetailMessage() {
		return null;
	}

	public ResourceAnalyzerSetting getSetting() {
		return setting;
	}

	public HashMap<String, List<ResourceUsageEntryVO>> getAggregationData() {
		return aggr_data;
	}

	public int getTotalCount() {
		return total_count;
	}

	public Date getFirstTime() {
		return first_time;
	}

	public Date getLastTime() {
		return last_time;
	}

	public void parseLine(String line, FileInfo fileInfo) throws Exception {
		if(line == null || line.trim().equals("") || line.startsWith("#")) {
			return;
		}
		if("sar".equals(setting.getFieldMapping().getFileType())) {
			if(line.indexOf("%idle") > -1 || line.startsWith("Average:") || line.indexOf("RESTART") > -1) {
				return;
			} else if(line.length() > 0 && line.charAt(0) != ' ' && Character.isDigit(line.charAt(0)) == false) {
				List<String> headerTokenList = ParserUtil.getTokenList(line, delimeter, bracelets, setting.getCheckStrict());
				String time_idx_str = setting.getFieldMapping().getMappingInfo().get("TIME");
				String[] time_idx = time_idx_str.split(",");
				for(String idx : time_idx) {
					if(idx.charAt(0) == 'H') {
						int header_idx = Integer.parseInt(idx.substring(1));
						String header_date = headerTokenList.get(header_idx-1);
						fileInfo.setFileMeta("date", header_date);
					}
				}
				return;
			}
		} else if("vmstat".equals(setting.getFieldMapping().getFileType())) {
			if(line.indexOf("-cpu-") > -1 || line.indexOf("free") > -1) {
				return;
			}
		}

		List<String> tokenList = ParserUtil.getTokenList(line, delimeter, bracelets, setting.getCheckStrict());
		if(tokenList.size() != setting.fieldMapping.fieldCount) {
			Logger.debug("Field count is different : " + tokenList.size() + " != " + setting.fieldMapping.fieldCount);
			throw new Exception("Invaid line : " + line);
		}

		String date_str = getStringOfField(tokenList, "TIME", fileInfo);
		Date dt = null;
		if(date_str != null) {
			if(setting.fieldMapping.timeFormat.equals(Constant.UNIX_TIME_STR)) {
				dt = new Date((long)(Double.parseDouble(date_str)*1000));
			} else {
				if(sdf == null) {
					sdf = new SimpleDateFormat(setting.fieldMapping.timeFormat, setting.fieldMapping.timeLocale);
					sdf.setLenient(false);
				}
				if(under_second_format == null || under_second_format.length() < 4) {
					dt = sdf.parse(date_str);
				} else {
					dt = sdf_no_under_score.parse(date_str);
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
		} else {
			Logger.debug("Time field cannot be null.");
			throw new Exception("Invaid line : " + line);
		}
		
		String s_cpu = getStringOfField(tokenList, "CPU");
		double d_cpu = -1;
		if(s_cpu != null && NumberUtil.isNumeric(s_cpu.trim(), true)) {
			d_cpu = ParserUtil.stringToDouble(s_cpu);
			if(setting.getFieldMapping().isCpuIdle()) {
				if(d_cpu > 100L) {
					Logger.debug("CPU Idle is greater than 100% : CPU(idle)=" + s_cpu);
					throw new Exception("Invaid line : " + line);
				} else {
					d_cpu = 100.0D - d_cpu;
				}
			}
		}

		String s_mem = getStringOfField(tokenList, "MEM");
		double d_mem = -1;
		if(s_mem != null && NumberUtil.isNumeric(s_mem.trim(), true)) {
			d_mem = ParserUtil.stringToDouble(s_mem);
			if(setting.getFieldMapping().isMemoryIdle()) {
				if(d_mem > 100L) {
					Logger.debug("Memory Idle is greater than 100% : MEM(idle)=" + s_mem);
					throw new Exception("Invaid line : " + line);
				} else {
					d_mem = 100.0D - d_mem;
				}
			}
		}

		String s_disk = getStringOfField(tokenList, "DISK");
		double d_disk = -1;
		if(s_disk != null && NumberUtil.isNumeric(s_disk.trim(), true)) {
			d_disk = ParserUtil.stringToDouble(s_disk);
			if(setting.getFieldMapping().isDiskIdle()) {
				if(d_disk > 100L) {
					Logger.debug("Disk Idle is greater than 100% : DISK(idle)=" + s_disk);
					throw new Exception("Invaid line : " + line);
				} else {
					d_disk = 100.0D - d_disk;
				}
			}
		}

		String s_network = getStringOfField(tokenList, "NETWORK");
		double d_network = -1;
		if(s_network != null && NumberUtil.isNumeric(s_network.trim(), true)) {
			d_network = ParserUtil.stringToDouble(s_network);
			if(setting.getFieldMapping().isNetworkIdle()) {
				if(d_network > 100L) {
					Logger.debug("Network Idle is greater than 100% : NETWORK(idle)=" + s_network);
					throw new Exception("Invaid line : " + line);
				} else {
					d_network = 100.0D - d_network;
				}
			}
		}

		if("sar".equals(setting.getFieldMapping().getFileType())) {
			if(sdf_date_sar == null) {
				sdf_date_sar = new SimpleDateFormat(STR_DATE_SAR, setting.fieldMapping.timeLocale);
			}
			if(prev_cal == null) {
				prev_cal = Calendar.getInstance();
			}
			if(curr_cal == null) {
				curr_cal = Calendar.getInstance();
			}
			curr_cal.setTime(dt);			
			if(curr_cal.get(Calendar.MONTH) == prev_cal.get(Calendar.MONTH) && curr_cal.get(Calendar.DATE) == prev_cal.get(Calendar.DATE) && 
			   prev_cal.get(Calendar.AM_PM) == Calendar.PM && curr_cal.get(Calendar.AM_PM) == Calendar.AM &&
			   prev_cal.get(Calendar.HOUR_OF_DAY) == 23 && curr_cal.get(Calendar.HOUR_OF_DAY) == 0 &&
			   fileInfo.getFilePath().equals(prev_filepath)) {					
				curr_cal = DateUtil.addCalendarUnit(curr_cal, Calendar.DATE, 1);
				dt = curr_cal.getTime();			
				fileInfo.setFileMeta("date", sdf_date_sar.format(dt));
			}
			prev_filepath = fileInfo.getFilePath();
			prev_cal = (Calendar)curr_cal.clone();
		}

		aggregate(dt, fileInfo.getFileMeta("name"), fileInfo.getFileMeta("group"), d_cpu, d_mem, d_disk, d_network);		
	}

	public void sortData() throws Exception {
		for(String name : aggr_data.keySet()) {
			List<ResourceUsageEntryVO>list = aggr_data.get(name);
			if(list != null) {
				Collections.sort(list, new UnitDateComparator());
			}
		}
	}

	public void writeDataToDB() throws Exception {
		SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
		summaryVo.setLastResourceInsertTime(new Date());
		Logger.debug("Updating Summary Data to DB.");
		db.insertWithTransaction(em, summaryVo, true);
		
		List<ResourceUsageEntryVO> list_vo;
		for(String key : aggr_data.keySet()) {
			list_vo = (List<ResourceUsageEntryVO>)aggr_data.get(key);
			db.beginTransaction(em);
			Logger.debug("Inserting Aggregation Data to DB : " + key);
			for(int i = 1; i <= list_vo.size(); i++) {				
				ResourceUsageEntryVO newVo = list_vo.get(i-1);
				HashMap<String,Object> paramMap = new HashMap<String,Object>();
				paramMap.put("group", newVo.getServerGroup());
				paramMap.put("name", newVo.getServerName());
				paramMap.put("unit_date", newVo.getUnitDate());
				ResourceUsageEntryVO oldVo = db.select(em, "select t from ResourceUsageEntryVO as t where t.group = :group and t.name = :name and t.unit_date = :unit_date", ResourceUsageEntryVO.class, paramMap);
				oldVo = (oldVo == null) ? newVo : mergeVo(oldVo, newVo);
				db.insert(em, oldVo);
				if((i%1000) == 0) {
					db.commitTransaction(em, true, true);
					db.beginTransaction(em);
				}
			}
			db.commitTransaction(em);
		}	
	}
	
	private ResourceUsageEntryVO mergeVo(ResourceUsageEntryVO oldVo, ResourceUsageEntryVO newVo) {
		if(oldVo.getCpuUsage() == -1D || newVo.getCpuUsage() != -1D) {
			oldVo.setCpuUsage(newVo.getCpuUsage());
		}
		if(oldVo.getMemoryUsage() == -1D || newVo.getMemoryUsage() != -1D) {
			oldVo.setMemoryUsage(newVo.getMemoryUsage());
		}
		if(oldVo.getDiskUsage() == -1D || newVo.getDiskUsage() != -1D) {
			oldVo.setDiskUsage(newVo.getDiskUsage());
		}
		if(oldVo.getNetworkUsage() == -1D || newVo.getNetworkUsage() != -1D) {
			oldVo.setNetworkUsage(newVo.getNetworkUsage());
		}
		return oldVo;
	}

	public void fillWithNullTime(ProgressBarTask progressbar) throws Exception {
		for(String name : aggr_data.keySet()) {
			List<ResourceUsageEntryVO> aggr_name = (List<ResourceUsageEntryVO>)aggr_data.get(name);
			if(aggr_name != null) {
				progressbar.setDetailSubMessage("filling null-data for '" + name + "'");
				Collections.sort(aggr_name, new UnitDateComparator());
				int size = aggr_name.size();
				if(size > 0) {
					ResourceUsageEntryVO vo_f = (ResourceUsageEntryVO)aggr_name.get(0);
					Date dt_f = DateUtil.getFirstOfDay(vo_f.getUnitDate());
					Calendar first = Calendar.getInstance();
					first.setTime(dt_f);
					ResourceUsageEntryVO vo_l = (ResourceUsageEntryVO)aggr_name.get(size - 1);
					Date dt_l = DateUtil.getLastOfDay(vo_l.getUnitDate());
					Calendar last = Calendar.getInstance();
					last.setTime(DateUtil.getLastOfDay(dt_l));
					int total_count = (int)((DateUtil.diff(dt_f, dt_l, TimeUnit.MINUTES)+1) / setting.getUnitMinutes());
					int curr_count = 1;
					while(first.compareTo(last) < 0) {
						if(thread.isStopped() || isCanceled) {
							throw new InterruptedException();
						}
						addNullMinute(first.getTime(), name);
						first = DateUtil.addCalendarUnit(first, Calendar.MINUTE, setting.getUnitMinutes());
						if(curr_count%10 == 0 || curr_count == total_count) {
							progressbar.setDetailSubMessage("filling null-data for '" + name + "' : " + curr_count + "/" + total_count, false);
						}
						curr_count++;
					}
				}
			}
		}
	}

	private String getStringOfField(List<String> fld_tokens, String fld_name) {
		return getStringOfField(fld_tokens, fld_name, null);
	}

	private String getStringOfField(List<String> fld_tokens, String fld_name, FileInfo file_info) {
		try {
			String fld_str = "";
			FieldIndex[] fld_idx = getIndexOfField(fld_name);
			if(fld_idx == null || fld_idx.length < 1) {
				return null;
			}
			if(fld_idx[0] == null) {
				fld_str = file_info.getFileMeta("date");
			} else {
				fld_str = fld_idx[0].getField(fld_tokens, delimeter, bracelets);
			}			
			fld_str = (fld_str == null) ? "UNKNOWN" : fld_str;
			if(fld_idx.length > 1 && fld_name.equals("TIME")) {
				if(fld_str.equals("UNKNOWN")) {
					return null;
				}
				for(int i = 1; i < fld_idx.length; i++) {
					String sub_fld_str = fld_idx[i].getField(fld_tokens, delimeter, bracelets);
					String joinChar = " ";
					fld_str += ((sub_fld_str == null) ? "" : (joinChar + sub_fld_str));
				}
			}
			return fld_str;
		} catch(Exception e) {
			Logger.debug(e);
			return null;
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
					if(s_arr[i].charAt(0) != 'H') {
						arr[i] = new FieldIndex(fld_name + "_" + i, s_arr[i]);
					}
				}
			} else {
				arr = new FieldIndex[] { (new FieldIndex(fld_name, idx_str)) };
			}
		}
		fieldIndexCache.put(fld_name, arr);
		return arr;
	}

	private void aggregate(Date dt, String name, String group, double cpu, double mem, double disk, double network) throws Exception {
		total_count++;
		if(first_time == null || first_time.compareTo(dt) > 0) {
			first_time = dt;
		}
		if(last_time == null || last_time.compareTo(dt) < 0) {
			last_time = dt;
		}
		ResourceUsageEntryVO vo = new ResourceUsageEntryVO(dt, name, group, cpu, mem, disk, network);
		aggregate(vo);		
	}

	private void aggregate(ResourceUsageEntryVO vo) throws Exception {
		addAggregationResource(vo);
	}

	private void addAggregationResource(ResourceUsageEntryVO vo) throws Exception {
		if(vo == null) {
			return;
		}
		if(unit_minute == null) {
			unit_minute = DateUtil.getFirstOfDay(vo.getUnitDate());
		}
		Date unit_dt = checkAggregationUnit(vo.getUnitDate());
		String name = vo.getServerName();
		List<ResourceUsageEntryVO> aggr_server = (List<ResourceUsageEntryVO>)aggr_data.get(name);
		if(aggr_server == null) {
			aggr_server = new ArrayList<ResourceUsageEntryVO>();
			aggr_data.put(name, aggr_server);
		}
		ResourceUsageEntryVO result_vo = getContainsKey(aggr_server, unit_dt);
		if(result_vo == null) {
			result_vo = new ResourceUsageEntryVO(unit_dt, vo.getServerName(), vo.getServerGroup());
			aggr_server.add(result_vo);
		}
		result_vo.addData(vo);
	}

	private Date checkAggregationUnit(Date dt) throws Exception {
		Calendar unit = Calendar.getInstance();
		unit.setTime(unit_minute);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		if(cal.compareTo(unit) >= 0) {
			while(cal.compareTo(unit) >= 0) {
				if(thread.isStopped() || isCanceled) {
					throw new InterruptedException();
				}
				unit = DateUtil.addCalendarUnit(unit, Calendar.MINUTE, setting.getUnitMinutes());
			}
			unit = DateUtil.addCalendarUnit(unit, Calendar.MINUTE, -setting.getUnitMinutes());
		} else {
			while(cal.compareTo(unit) < 0) {
				if(thread.isStopped() || isCanceled) {
					throw new InterruptedException();
				}
				unit = DateUtil.addCalendarUnit(unit, Calendar.MINUTE, -setting.getUnitMinutes());
			}
		}
		unit_minute = unit.getTime();
		return unit_minute;
	}

	private ResourceUsageEntryVO getContainsKey(List<ResourceUsageEntryVO> list, Date dt) {
		int size = list.size();
		ResourceUsageEntryVO vo;
		Date unitdt;
		for(int i = 0; i < size; i++) {
			vo = (ResourceUsageEntryVO)list.get(i);
			unitdt = vo.getUnitDate();
			if(unitdt != null && unitdt.equals(dt)) {
				return vo;
			}
		}
		return null;
	}

	private void addNullMinute(Date dt, String name) throws Exception {
		List<ResourceUsageEntryVO> aggr_time = (List<ResourceUsageEntryVO>)aggr_data.get(name);
		if(aggr_time == null) {
			aggr_time = new ArrayList<ResourceUsageEntryVO>();
			aggr_data.put(name, aggr_time);
		}
		ResourceUsageEntryVO vo;
		int size = aggr_time.size();
		boolean flag = false;
		for(int i = 0; i < size; i++) {
			vo = (ResourceUsageEntryVO)aggr_time.get(i);
			if(vo.getUnitDate().equals(dt)) {
				flag = true;
				break;
			}
		}
		if(!flag) {
			vo = new ResourceUsageEntryVO(dt, name, setting.getGroup(name));
			aggr_time.add(vo);
		}
	}

}
