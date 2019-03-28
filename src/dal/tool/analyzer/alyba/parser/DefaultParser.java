package dal.tool.analyzer.alyba.parser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.BadResponseEntryVO;
import dal.tool.analyzer.alyba.output.vo.DailyEntryVO;
import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.HourlyEntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.output.vo.ResponseEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.setting.AnalyzerSetting;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.DateUtil;

public class DefaultParser extends LogLineParser {

	private Date unit_minute = null;
	private Date unit_hour = null;

	public DefaultParser(AnalyzerSetting setting) throws Exception {
		super(setting);
	}
	
	protected void aggregate(ResponseEntryVO vo) throws Exception {		
		addAggregationTPM(vo);
		addAggregationDay(vo);
		addAggregationHour(vo);
		addAggregationCount("URI", vo);
		addAggregationCount("IP", vo);
		addAggregationCount("EXT", vo);
		addAggregationCount("CODE", vo);
		addAggregationCount("METHOD", vo);
		addAggregationCount("VERSION", vo);
		addAggregationResponse(vo);
	}

	public void sortData() throws Exception {
		sortDataByTime(aggr_data.get("TPM"));
		sortDataByTime(aggr_data.get("HOUR"));
		sortDataByTime(aggr_data.get("DAY"));
		sortDataByValue(aggr_data.get("URI"));
		sortDataByValue(aggr_data.get("IP"));
		sortDataByValue(aggr_data.get("METHOD"));
		sortDataByValue(aggr_data.get("VERSION"));
		sortDataByValue(aggr_data.get("EXT"));
		sortDataByValue(aggr_data.get("CODE"));
	}

	public void writeDataToDB() throws Exception {
		writeSummaryDataToDB();
		writeAggregationDataToDB();		
	}

	private Date checkAggregationTPMUnit(Date dt) throws Exception {
		Calendar unit = Calendar.getInstance();
		unit.setTime(unit_minute);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		if(cal.compareTo(unit) >= 0) {
			while(cal.compareTo(unit) >= 0) {
				if(thread.isStopped() || isCanceled) {
					throw new InterruptedException();
				}
				unit = DateUtil.addCalendarUnit(unit, Calendar.MINUTE, setting.tpmUnitMinutes);
			}
			unit = DateUtil.addCalendarUnit(unit, Calendar.MINUTE, -setting.tpmUnitMinutes);
		} else {
			while(cal.compareTo(unit) < 0) {
				if(thread.isStopped() || isCanceled) {
					throw new InterruptedException();
				}
				unit = DateUtil.addCalendarUnit(unit, Calendar.MINUTE, -setting.tpmUnitMinutes);
			}
		}
		unit_minute = unit.getTime();
		return unit_minute;
	}

	private Date checkAggregationUnitHourly(Date dt) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		int hh = cal.get(Calendar.HOUR_OF_DAY);
		Calendar unit = Calendar.getInstance();
		unit.setTime(unit_hour);
		unit.set(Calendar.YEAR, 2000);
		unit.set(Calendar.MONTH, 0);
		unit.set(Calendar.DATE, 1);
		unit.set(Calendar.HOUR_OF_DAY, hh);
		unit_hour = unit.getTime();
		return unit_hour;
	}

	private void addAggregationTPM(ResponseEntryVO vo) throws Exception {
		if(vo == null || !setting.collectTPM) {
			return;
		}
		if(unit_minute == null) {
			unit_minute = DateUtil.getFirstOfDay(vo.getResponseDate());
		}
		Date unit_dt = checkAggregationTPMUnit(vo.getResponseDate());
		List<EntryVO> aggr_tpm = (List<EntryVO>)aggr_data.get("TPM");
		if(aggr_tpm == null) {
			aggr_tpm = new ArrayList<EntryVO>();
			aggr_data.put("TPM", aggr_tpm);
		}
		DateEntryVO result_vo = getContainsKey(aggr_tpm, unit_dt);
		if(result_vo == null) {
			result_vo = new TPMEntryVO(unit_dt);
			aggr_tpm.add(result_vo);
		}
		result_vo.addData(vo);
	}

	private void addAggregationDay(ResponseEntryVO vo) throws Exception {
		if(vo == null) {
			return;
		}
		if(sdf_date == null) {
			sdf_date = new SimpleDateFormat(sdf_str_date, setting.fieldMapping.timeLocale);
		}
		if(sdf_datetime == null) {
			sdf_datetime = new SimpleDateFormat(sdf_str_datetime, setting.fieldMapping.timeLocale);
		}
		String date_str = sdf_date.format(vo.getResponseDate());
		Date unit_dt = sdf_datetime.parse(date_str + " 00:00:00");
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("DAY");
		if(aggr_time == null) {
			aggr_time = new ArrayList<EntryVO>();
			aggr_data.put("DAY", aggr_time);
		}
		DateEntryVO result_vo = getContainsKey(aggr_time, unit_dt);
		if(result_vo == null) {
			result_vo = new DailyEntryVO(unit_dt);
			aggr_time.add(result_vo);
		}
		result_vo.addData(vo);
	}

	private void addAggregationHour(ResponseEntryVO vo) throws Exception {
		if(vo == null) {
			return;
		}
		if(unit_hour == null) {
			unit_hour = DateUtil.getFirstOfDay(vo.getResponseDate());
		}
		Date unit_dt = checkAggregationUnitHourly(vo.getResponseDate());
		List<EntryVO> aggr_time = (List<EntryVO>)aggr_data.get("HOUR");
		if(aggr_time == null) {
			aggr_time = new ArrayList<EntryVO>();
			aggr_data.put("HOUR", aggr_time);
		}
		DateEntryVO result_vo = getContainsKey(aggr_time, unit_dt);
		if(result_vo == null) {
			result_vo = new HourlyEntryVO(unit_dt);
			aggr_time.add(result_vo);
		}
		result_vo.addData(vo);
	}

	private void addAggregationCount(String dataKey, ResponseEntryVO vo) throws Exception {
		if(vo == null) {
			return;
		}
		String data = null;
		if(dataKey.equals("URI")) {
			data = vo.getRequestURI();
		} else if(dataKey.equals("IP")) {
			if(!setting.collectIP) {
				return;
			}
			data = vo.getRequestIP();
		} else if(dataKey.equals("EXT")) {
			data = vo.getRequestExt();
		} else if(dataKey.equals("CODE")) {
			data = vo.getResponseCode();
		} else if(dataKey.equals("METHOD")) {
			data = (vo.getRequestMethod() == null) ? null : vo.getRequestMethod().toUpperCase();
		} else if(dataKey.equals("VERSION")) {
			data = (vo.getRequestVersion() == null) ? null : vo.getRequestVersion().toUpperCase();
		}
		if(data == null) {
			return;
		}
		List<EntryVO> aggr_data2 = (List<EntryVO>)aggr_data.get(dataKey);
		if(aggr_data2 == null) {
			aggr_data2 = new ArrayList<EntryVO>();
			aggr_data.put(dataKey, aggr_data2);
		}
		KeyEntryVO result_vo = getContainsKey(aggr_data2, data);
		if(result_vo == null) {
			Logger.debug("Inserting new KeyEntry to memory : Type=" + dataKey + ", Key=" + data);
			if(dataKey.equals("URI")) result_vo = new KeyEntryVO(KeyEntryVO.Type.URI, data);
			else if(dataKey.equals("IP")) result_vo = new KeyEntryVO(KeyEntryVO.Type.IP, data);
			else if(dataKey.equals("EXT")) result_vo = new KeyEntryVO(KeyEntryVO.Type.EXT, data);
			else if(dataKey.equals("CODE")) result_vo = new KeyEntryVO(KeyEntryVO.Type.CODE, data);
			else if(dataKey.equals("METHOD"))result_vo = new KeyEntryVO(KeyEntryVO.Type.METHOD, data);
			else if(dataKey.equals("VERSION")) result_vo = new KeyEntryVO(KeyEntryVO.Type.VERSION, data);			
			aggr_data2.add(result_vo);
		}
		result_vo.addData(vo);
	}

	private void addAggregationResponse(ResponseEntryVO vo) throws Exception {
		long rtime = vo.getResponseTime();
		long rbyte = vo.getResponseBytes();
		String code = vo.getResponseCode();
		if(setting.collectElapsedTime && rtime > 0 && rtime >= setting.collectElapsedTimeMS) {
			Logger.debug("Inserting Response of time to DB : " + rtime);
			db.insertWithTransaction(em, new BadResponseEntryVO(BadResponseEntryVO.Type.TIME, vo.copy()), false);
		}
		if(setting.collectResponseBytes && rbyte > 0 && (int)(rbyte / 1024) >= setting.collectResponseBytesKB) {
			Logger.debug("Inserting Response of size to DB : " + rbyte);
			db.insertWithTransaction(em, new BadResponseEntryVO(BadResponseEntryVO.Type.SIZE, vo.copy()), false);
		}
		if(setting.collectErrors && code != null && (code.startsWith("4") || code.startsWith("5"))) {
			Logger.debug("Inserting Response of error to DB : " + code);
			db.insertWithTransaction(em, new BadResponseEntryVO(BadResponseEntryVO.Type.CODE, vo.copy()), false);
		}
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
		vo.setTotalErrorCount(total_error_count);
		vo.setFilteredRequestCount(filtered_request_count);
		vo.setFilteredErrorCount(filtered_error_count);
		vo.setFilteredUriCount(getFilteredURICount());
		vo.setFilteredIpCount(getFilteredIPAddressCount());
		vo.setFilteredMethodCount(getFilteredMethodCount());
		vo.setFilteredExtCount(getFilteredExtensionCount());
		vo.setFilteredCodeCount(getFilteredCodeCount());
		vo.setBadElapsedCount(getLongTransactionCount());
		vo.setBadByteCount(getLargeResponseCount());
		vo.setBadCodeCount(getErrorResponseCount());
		vo.setFilterFromTime(setting.getFilterSetting().getFromDateRange());
		vo.setFilterToTime(setting.getFilterSetting().getToDateRange());
		vo.setFilterIncludeInfo(setting.getFilterSetting().getIncludeFilterInfoString());
		vo.setFilterExcludeInfo(setting.getFilterSetting().getExcludeFilterInfoString());
		Logger.debug("Inserting Summary Data to DB.");
		db.insertWithTransaction(em, vo, true);
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
		if(db.count(em, DailyEntryVO.class) > 0) {
			peak = em.createQuery("SELECT vo FROM DailyEntryVO AS vo ORDER BY req_count DESC", DailyEntryVO.class).setMaxResults(1).getSingleResult();
			if(peak != null) {
				vo.setDailyPeakTime(peak.getUnitDate());
				vo.setDailyPeakCount(peak.getRequestCount());
			}
		}
		if(db.count(em, HourlyEntryVO.class) > 0) {
			peak = em.createQuery("SELECT vo FROM HourlyEntryVO AS vo ORDER BY req_count DESC", HourlyEntryVO.class).setMaxResults(1).getSingleResult();
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
