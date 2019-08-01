package dal.tool.analyzer.alyba.parser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.ResponseEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPSEntryVO;
import dal.tool.analyzer.alyba.setting.AnalyzerSetting;
import dal.util.DateUtil;

public class PostParser extends LogLineParser {

	private Date unit_second = null;
	private Date from = null;
	private Date to = null;
	
	public PostParser(AnalyzerSetting setting) throws Exception {
		super(setting);
	}
	
	public void initDatabase() {
		super.initDatabase();
		Date dt = getMostRequestedDay();
		this.from = DateUtil.getFirstOfDay(dt);
		this.to = DateUtil.getLastOfDay(dt);
	}
	
	protected void aggregate(ResponseEntryVO vo) throws Exception {
		addAggregationTPS(vo);
	}

	public void sortData() throws Exception {
		sortDataByTime(aggr_data.get("TPS"));
	}

	public void writeDataToDB() throws Exception {
		writeAggregationDataToDB();		
	}

	protected boolean allowedAggregation(Date dt, String ip, String url, String ext, String code, String method, String version) throws Exception {
		if(from.compareTo(dt) > 0 || to.compareTo(dt) < 0) {
			return false;
		}
		return super.allowedAggregation(dt, ip, url, ext, code, method, version);
	}

	private Date getMostRequestedDay() {
		return em.createQuery("SELECT vo.peak_daily_time FROM SummaryEntryVO AS vo", Date.class).getSingleResult();
	}
	
	private void addAggregationTPS(ResponseEntryVO vo) throws Exception {
		if(vo == null || !setting.collectTPS) {
			return;
		}
		
		if(unit_second == null) {
			unit_second = DateUtil.getFirstOfDay(vo.getResponseDate());
		}
		
		Date unit_dt = checkAggregationTPSUnit(vo.getResponseDate());
		List<EntryVO> aggr_tps = (List<EntryVO>)aggr_data.get("TPS");
		if(aggr_tps == null) {
			aggr_tps = new ArrayList<EntryVO>();
			aggr_data.put("TPS", aggr_tps);
		}
		DateEntryVO result_vo = getContainsKey(aggr_tps, unit_dt);
		if(result_vo == null) {
			result_vo = new TPSEntryVO(unit_dt);
			aggr_tps.add(result_vo);
		}
		result_vo.addData(vo, setting);
	}

	private Date checkAggregationTPSUnit(Date dt) throws Exception {
		Calendar unit = Calendar.getInstance();
		unit.setTime(unit_second);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		if(cal.compareTo(unit) >= 0) {
			while(cal.compareTo(unit) >= 0) {
				if(thread.isStopped() || isCanceled) {
					throw new InterruptedException();
				}
				unit = DateUtil.addCalendarUnit(unit, Calendar.SECOND, 1);
			}
			unit = DateUtil.addCalendarUnit(unit, Calendar.SECOND, -1);
		} else {
			while(cal.compareTo(unit) < 0) {
				if(thread.isStopped() || isCanceled) {
					throw new InterruptedException();
				}
				unit = DateUtil.addCalendarUnit(unit, Calendar.SECOND, -1);
			}
		}
		unit_second = unit.getTime();
		return unit_second;
	}

	private void writeAggregationDataToDB() throws Exception {
		Set<String> keys = aggr_data.keySet();
		List<EntryVO> list_vo;
		for(String key : keys) {
			list_vo = (List<EntryVO>)aggr_data.get(key);
			db.beginTransaction(em);
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
		if(db.count(em, TPSEntryVO.class) > 0) {
			peak = em.createQuery("SELECT vo FROM TPSEntryVO AS vo ORDER BY req_count DESC", TPSEntryVO.class).setMaxResults(1).getSingleResult();
			if(peak != null) {
				vo.setSecondlyPeakTime(peak.getUnitDate());
				vo.setSecondlyPeakCount(peak.getRequestCount());
			}
		}
		db.commitTransaction(em);
	}

}
