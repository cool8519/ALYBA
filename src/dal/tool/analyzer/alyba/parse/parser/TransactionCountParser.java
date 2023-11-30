package dal.tool.analyzer.alyba.parse.parser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.UnitDateComparator;
import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.TransactionEntryVO;
import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPSEntryVO;
import dal.tool.analyzer.alyba.output.vo.TimeAggregationEntryVO;
import dal.tool.analyzer.alyba.parse.FieldIndex;
import dal.tool.analyzer.alyba.parse.FileInfo;
import dal.tool.analyzer.alyba.parse.ParserUtil;
import dal.tool.analyzer.alyba.setting.TPMAnalyzerSetting;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.DateUtil;
import dal.util.NumberUtil;
import dal.util.swt.ProgressBarTask;

public class TransactionCountParser extends LogLineParser {

	private TPMAnalyzerSetting tpmAnalyzerSetting;
	
	public TransactionCountParser(TPMAnalyzerSetting setting) throws Exception {
		super(setting);
		this.tpmAnalyzerSetting = setting;
	}
	
	public String getTaskDetailMessage() {
		return null;
	}

	protected void aggregate(TransactionEntryVO vo) throws Exception {
	}

	public void parseLine(String line, FileInfo fileInfo) throws Exception {
		if(line == null || line.trim().equals("") || line.startsWith("#")) {
			return;
		}

		List<String> tokenList = ParserUtil.getTokenList(line, delimeter, bracelets, setting.checkStrict);
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
		
		String s_count = getStringOfField(tokenList, "COUNT");
		long l_count = -1;
		if(s_count != null && NumberUtil.isNumeric(s_count, true)) {
			l_count = ParserUtil.stringToLong(s_count);
		}

		aggregate(dt, (int)l_count);		
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

	private void aggregate(Date dt, int count) throws Exception {
		total_request_count += count;
		if(count > 0 && (first_time == null || first_time.compareTo(dt) > 0)) {
			first_time = dt;
		}
		if(count > 0 && (last_time == null || last_time.compareTo(dt) < 0)) {
			last_time = dt;
		}
		if(tpmAnalyzerSetting.getFieldMapping().isTPS) {
			TPSEntryVO vo = new TPSEntryVO(dt, count);
			addAggregationTPS(vo);
		} else {
			Calendar unit = Calendar.getInstance();
			unit.setTime(dt);
			unit.set(Calendar.SECOND, 0);
			TPMEntryVO vo = new TPMEntryVO(unit.getTime(), count);
			addAggregationTPM(vo);
		}
	}

	private void addAggregationTPM(TPMEntryVO vo) throws Exception {
		if(vo == null) {
			return;
		}
		List<EntryVO> aggr_tpm = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_tpm == null) {
			aggr_tpm = new ArrayList<EntryVO>();
			aggr_data.put("TPM", aggr_tpm);
		}
		DateEntryVO result_vo = getContainsKey(aggr_tpm, vo.getUnitDate());
		if(result_vo == null) {
			aggr_tpm.add(vo);
		} else {
			Logger.debug("The insert has been ignored. Data from the same time exist. '" + DateUtil.dateToString(vo.getUnitDate(), DateUtil.SDF_DATETIME) + "'");
		}
	}

	private void addAggregationTPS(TPSEntryVO vo) throws Exception {
		if(vo == null) {
			return;
		}
		List<EntryVO> aggr_tps = (List<EntryVO>)aggr_data.get("TPS");
		if(aggr_tps == null) {
			aggr_tps = new ArrayList<EntryVO>();
			aggr_data.put("TPS", aggr_tps);
		}		
		DateEntryVO result_vo = getContainsKey(aggr_tps, vo.getUnitDate());
		if(result_vo == null) {
			aggr_tps.add(vo);
		} else {
			Logger.debug("The insert has been ignored. Data from the same time exist. '" + DateUtil.dateToString(vo.getUnitDate(), DateUtil.SDF_DATETIME) + "'");
		}
	}

	public void sortData() throws Exception {
		sortDataByTime(aggr_data.get("TPS"));
		sortDataByTime(aggr_data.get("TPM"));
	}

	public void generateOtherData() throws Exception {
		if(tpmAnalyzerSetting.getFieldMapping().isTPS) {
			aggregationTPM();
		}
		aggregationDay();		
		aggregationHour();
	}
	
	private void aggregationTPM() throws Exception {
		List<EntryVO> aggr_tps = (List<EntryVO>)aggr_data.get("TPS");
		if(aggr_tps == null) {
			return;
		}
		List<EntryVO> aggr_tpm = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_tpm == null) {
			aggr_tpm = new ArrayList<EntryVO>();
			aggr_data.put("TPM", aggr_tpm);
		}		
		Calendar unit = null;
		Calendar cal = Calendar.getInstance();
		int sum = 0;
		for(EntryVO vo : aggr_tps) {
			TPSEntryVO tpsVo = (TPSEntryVO)vo;
			if(unit == null) {
				unit = Calendar.getInstance();
				unit.setTime(DateUtil.getFirstOfDay(tpsVo.getUnitDate()));
			}
			cal.setTime(tpsVo.getUnitDate());
			cal.set(Calendar.SECOND, 0);
			if(unit.before(cal)) {
				aggr_tpm.add(new TPMEntryVO(unit.getTime(), sum));
				unit.add(Calendar.MINUTE, 1);
				while(unit.before(cal)) {
					aggr_tpm.add(new TPMEntryVO(unit.getTime(), 0));
					unit.add(Calendar.MINUTE, 1);
				}
				sum = tpsVo.getRequestCount();
			} else {				
				sum += tpsVo.getRequestCount();
			}
		}
		aggr_tpm.add(new TPMEntryVO(unit.getTime(), sum));
		int last_day = unit.get(Calendar.DATE);
		unit.add(Calendar.MINUTE, 1);
		while(unit.get(Calendar.DATE) == last_day) {
			aggr_tpm.add(new TPMEntryVO(unit.getTime(), 0));
			unit.add(Calendar.MINUTE, 1);
		}
	}

	private void aggregationDay() throws Exception {
		List<EntryVO> aggr_tpm = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_tpm == null) {
			return;
		}
		List<EntryVO> aggr_day = (List<EntryVO>)aggr_data.get("DAY");
		if(aggr_day == null) {
			aggr_day = new ArrayList<EntryVO>();
			aggr_data.put("DAY", aggr_day);
		}		
		Calendar unit = null;
		Calendar cal = Calendar.getInstance();
		int sum = 0;
		for(EntryVO vo : aggr_tpm) {
			TPMEntryVO tpmVo = (TPMEntryVO)vo;
			if(unit == null) {
				unit = Calendar.getInstance();
				unit.setTime(tpmVo.getUnitDate());
				unit.set(Calendar.HOUR_OF_DAY, 0);
				unit.set(Calendar.MINUTE, 0);
				unit.set(Calendar.SECOND, 0);
			}
			cal.setTime(tpmVo.getUnitDate());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			if(unit.before(cal)) {
				aggr_day.add(new TimeAggregationEntryVO(TimeAggregationEntryVO.Type.DAY, unit.getTime(), sum));
				unit.add(Calendar.DATE, 1);
				while(unit.before(cal)) {
					aggr_day.add(new TimeAggregationEntryVO(TimeAggregationEntryVO.Type.DAY, unit.getTime(), 0));
					unit.add(Calendar.DATE, 1);
				}
				sum = tpmVo.getRequestCount();
			} else {				
				sum += tpmVo.getRequestCount();
			}
		}
		aggr_day.add(new TimeAggregationEntryVO(TimeAggregationEntryVO.Type.DAY, unit.getTime(), sum));
	}

	private void aggregationHour() throws Exception {
		List<EntryVO> aggr_tpm = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_tpm == null) {
			return;
		}
		List<EntryVO> aggr_hour = (List<EntryVO>)aggr_data.get("HOUR");
		if(aggr_hour == null) {
			aggr_hour = new ArrayList<EntryVO>();
			aggr_data.put("HOUR", aggr_hour);			
			Calendar unit = Calendar.getInstance();
			unit.set(Calendar.YEAR, 2000);
			unit.set(Calendar.MONTH, 0);
			unit.set(Calendar.DATE, 1);
			unit.set(Calendar.MINUTE, 0);
			unit.set(Calendar.SECOND, 0);
			for(int i = 0; i < 24; i++) {
				unit.set(Calendar.HOUR_OF_DAY, i);
				TimeAggregationEntryVO unit_vo = new TimeAggregationEntryVO(TimeAggregationEntryVO.Type.HOUR, unit.getTime());
				aggr_hour.add(unit_vo);
			}
		}
		for(EntryVO vo : aggr_tpm) {
			TPMEntryVO tpmVo = (TPMEntryVO)vo;
			Calendar cal = Calendar.getInstance();
			cal.setTime(tpmVo.getUnitDate());
			TimeAggregationEntryVO timeAggrVo = (TimeAggregationEntryVO)aggr_hour.get(cal.get(Calendar.HOUR_OF_DAY));
			timeAggrVo.addData(tpmVo.getRequestCount());
		}
	}

	public void fillWithNullTime(ProgressBarTask progressbar) throws Exception {
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_time != null) {
			progressbar.setDetailSubMessage("filling null-data for TPM");
			Collections.sort(aggr_time, new UnitDateComparator());
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
				int tpmUnitMinutes = tpmAnalyzerSetting.getFieldMapping().isTPS ? 1 : tpmAnalyzerSetting.getFieldMapping().countUnit;
				int total_count = (int)((DateUtil.diff(dt_f, dt_l, TimeUnit.MINUTES)+1) / tpmUnitMinutes);
				int curr_count = 1;
				while(first.compareTo(last) < 0) {
					if(thread.isStopped() || isCanceled) {
						throw new InterruptedException();
					}
					addNullMinute(first.getTime());
					first = DateUtil.addCalendarUnit(first, Calendar.MINUTE, tpmUnitMinutes);
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
			Collections.sort(aggr_time, new UnitDateComparator());
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
	}	
	
	public void writeDataToDB() throws Exception {
		writeSummaryDataToDB();
		writeAggregationDataToDB();		
	}	
	
	private void writeSummaryDataToDB() throws Exception {
		SummaryEntryVO vo = new SummaryEntryVO();
		vo.setVersion(Constant.PROGRAM_VERSION);
		vo.setTitle(setting.getTitle());
		vo.setParsedTime(parsed_time);
		vo.setCreatedTime(new Date());
		vo.setFirstRequestTime(first_time);
		vo.setLastRequestTime(last_time);
		vo.setTotalRequestCount(total_request_count);
		Logger.debug("Inserting Summary Data to DB.");
		db.insertWithTransaction(em, vo, true);
		
		SettingEntryVO settingVo = new SettingEntryVO();
		settingVo.title = setting.getTitle();
		settingVo.analyzeDate = setting.getAnalyzeDate();
		settingVo.logFileList = setting.getLogFileList();
		settingVo.collectTPM = true;
		settingVo.tpmUnitMinutes = tpmAnalyzerSetting.getFieldMapping().isTPS ? 1 : tpmAnalyzerSetting.getFieldMapping().countUnit;
		settingVo.collectTPS = tpmAnalyzerSetting.getFieldMapping().isTPS;
		settingVo.mappingFieldDelimeter = setting.getFieldMapping().getFieldDelimeter();
		settingVo.mappingFieldBracelet = setting.getFieldMapping().getFieldBracelet();
		settingVo.mappingTimeFormat = setting.getFieldMapping().getTimeFormat();
		settingVo.mappingTimeLocale = setting.getFieldMapping().getTimeLocale().toString();
		settingVo.mappingElapsedUnit = setting.getFieldMapping().getElapsedUnit();
		settingVo.mappingOffsetHour = setting.getFieldMapping().getOffsetHour();
		settingVo.mappingFieldCount = setting.getFieldMapping().getFieldCount();
		settingVo.logMappingInfo = setting.getFieldMapping().getMappingInfo();
		db.insertWithTransaction(em, settingVo, true);
	}

	private void writeAggregationDataToDB() throws Exception {
		Set<String> keys = aggr_data.keySet();
		List<EntryVO> list_vo;
		for(String key : keys) {
			list_vo = (List<EntryVO>)aggr_data.get(key);
			db.beginTransaction(em);
			Logger.debug("Inserting Aggregation Data to DB : " + key);
			for (int i = 1; i <= list_vo.size(); i++) {
				db.insert(em, list_vo.get(i-1));
				if((i%1000) == 0) {
					db.commitTransaction(em, true, true);
					db.beginTransaction(em);
				}
			}
			db.commitTransaction(em);
		}
		
		db.beginTransaction(em);
		SummaryEntryVO vo = db.select(em, SummaryEntryVO.class);
		DateEntryVO peak;
		if(db.count(em, TimeAggregationEntryVO.class) > 0) {
			peak = em.createQuery("SELECT vo FROM TimeAggregationEntryVO AS vo WHERE vo.type = 'DAY' ORDER BY req_count DESC", TimeAggregationEntryVO.class).setMaxResults(1).getSingleResult();
			if(peak != null) {
				vo.setDailyPeakTime(peak.getUnitDate());
				vo.setDailyPeakCount(peak.getRequestCount());
			}
			peak = em.createQuery("SELECT vo FROM TimeAggregationEntryVO AS vo WHERE vo.type = 'HOUR' ORDER BY req_count DESC", TimeAggregationEntryVO.class).setMaxResults(1).getSingleResult();
			if(peak != null) {
				vo.setHourlyPeakTime(peak.getUnitDate());
				vo.setHourlyPeakCount(peak.getRequestCount());
			}
		}
		if(db.count(em, TPMEntryVO.class) > 0) {
			peak = em.createQuery("SELECT vo FROM TPMEntryVO AS vo ORDER BY req_count DESC", TPMEntryVO.class).setMaxResults(1).getSingleResult();
			if(peak != null) {
				vo.setMinutelyPeakTime(peak.getUnitDate());
				vo.setMinutelyPeakCount(peak.getRequestCount());
			}
		}
		Logger.debug("Updating Summary Data to DB.");
		db.commitTransaction(em);
	}
	
}
