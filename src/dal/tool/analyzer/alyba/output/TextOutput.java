package dal.tool.analyzer.alyba.output;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.BadTransactionEntryVO;
import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.output.vo.TimeAggregationEntryVO;
import dal.tool.analyzer.alyba.output.vo.TransactionEntryVO;
import dal.tool.analyzer.alyba.setting.LogAnalyzerSetting;
import dal.tool.analyzer.alyba.util.Logger;
import dal.util.DateUtil;
import dal.util.console.Column;
import dal.util.console.Fields;
import dal.util.console.ResultTable;
import dal.util.db.ObjectDBUtil;

public class TextOutput extends ResultOutput {

	public TextOutput(LogAnalyzerSetting setting, ObjectDBUtil db, EntityManager em, String filename) {
		super(setting, db, em, filename);
	}

	public void generate() throws Exception {
		try {
			dal.tool.analyzer.alyba.ui.Logger.debug("Writing to the text file : " + filename);
			exportToText(filename);
		} catch(Exception e) {
			try {
				File f = new File(filename);
				if(f.exists()) {
					f.delete();
				}
			} catch(Exception e2) {
			}
			dal.tool.analyzer.alyba.ui.Logger.debug("Failed to create text file : " + filename);
			throw e;
		}
	}

	private void exportToText(String fname) throws Exception {
		Logger.openFile(fname, false);
		Logger.setTarget(Logger.FILE);

		Logger.logln("################################################");
		Logger.logln("          Result of Accesslog Analysis           ");
		Logger.logln("################################################");
		Logger.logln();
		Logger.logln();
		try {
			SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
			summaryVo.setCreatedTime(new Date());
			printSummary(summaryVo);
			printTPM(getEntryList(TPMEntryVO.class, null));
			printDay(getEntryList(TimeAggregationEntryVO.class, "DAY"));
			printHour(getEntryList(TimeAggregationEntryVO.class, "HOUR"));
			printCount(getEntryList(KeyEntryVO.class, "URI"), Type.URI);
			printCount(getEntryList(KeyEntryVO.class, "IP"), Type.IP);
			printCount(getEntryList(KeyEntryVO.class, "METHOD"), Type.METHOD);
			printCount(getEntryList(KeyEntryVO.class, "VERSION"), Type.VERSION);
			printCount(getEntryList(KeyEntryVO.class, "EXT"), Type.EXTENSION);
			printCount(getEntryList(KeyEntryVO.class, "CODE"), Type.CODE);

			int long_resp = summaryVo.getBadElapsedCount();
			if(long_resp > 1000) {
				dal.tool.analyzer.alyba.ui.Logger.debug("Too many long transaction : count=" + long_resp);
			} else {
				printResponse(getEntryList(BadTransactionEntryVO.class, "TIME"), Type.BAD_TIME);
			}

			int large_resp = summaryVo.getBadByteCount();
			if(large_resp > 1000) {
				dal.tool.analyzer.alyba.ui.Logger.debug("Too many large response : count=" + large_resp);
			} else {
				printResponse(getEntryList(BadTransactionEntryVO.class, "SIZE"), Type.BAD_BYTE);
			}

			int total_err = summaryVo.getBadCodeCount();
			if(total_err > 1000) {
				dal.tool.analyzer.alyba.ui.Logger.debug("Too many error response : count=" + total_err);
			} else {
				printResponse(getEntryList(BadTransactionEntryVO.class, "CODE"), Type.BAD_CODE);
			}
		} catch(Exception e) {
			dal.tool.analyzer.alyba.ui.Logger.debug(e);
			throw e;
		} finally {
			Logger.closeFile();
			Logger.setTarget(Logger.CONSOLE);
		}
	}
	
	private void printSummary(SummaryEntryVO vo) throws Exception {
		long total_req = vo.getTotalRequestCount();
		long filtered_req = vo.getFilteredRequestCount();
		int total_err = vo.getTotalErrorCount();
		int filtered_err = vo.getFilteredErrorCount();
		double filtered_req_pct = (double)filtered_req / total_req * 100;
		double filtered_err_pct = (double)filtered_err / total_err * 100;
		Date f_req_time = vo.getFirstRequestTime();
		Date l_req_time = vo.getLastRequestTime();
		Date f_filter_time = vo.getFilterFromTime();
		Date l_filter_time = vo.getFilterToTime();
		SimpleDateFormat local_tz_sdf = (SimpleDateFormat)SDF_DateSecondTZ.clone();
		local_tz_sdf.setTimeZone(Constant.TIMEZONE_DEFAULT);		
		Logger.logln();
		Logger.logln();
		Logger.logln("Summary");
		Logger.logln("----------------------------------------------------------------------");
		Logger.logln(" - Title                  : " + setting.title);
		Logger.logln(" - Parsed Date            : " + DateUtil.dateToString(vo.getParsedTime(), local_tz_sdf));
		Logger.logln(" - Created Date           : " + DateUtil.dateToString(vo.getCreatedTime(), local_tz_sdf));
		Logger.logln(" - Request Range          : " + ((f_req_time == null) ? "N/A" : (DateUtil.dateToString(f_req_time, SDF_DateSecond) + " ~ " + DateUtil.dateToString(l_req_time, SDF_DateSecond))));
		Logger.logln(" - Total Requests         : " + total_req);
		if(setting.fieldMapping.isMappedCode()) {
			Logger.logln(" - Total Errors           : " + total_err);
		}
		Logger.logln(" - Time Range Filter      : " + ((setting.filterSetting.allRangeEnable) ? "N/A" : (DateUtil.dateToString(f_filter_time, SDF_DateSecond) + " ~ " + DateUtil.dateToString(l_filter_time, SDF_DateSecond))));
		Logger.logln(" - Include Filter         : " + vo.getFilterIncludeInfo());
		Logger.logln(" - Exclude Filter         : " + vo.getFilterExcludeInfo());
		Logger.logln(" - Filtered Requests      : " + filtered_req + " (" + (Double.isNaN(filtered_req_pct) ? 0 : DF_Percent.format(filtered_req_pct)) + "%)");
		if(setting.fieldMapping.isMappedCode()) {
			Logger.logln(" - Filtered Errors        : " + filtered_err + " (" + (Double.isNaN(filtered_err_pct) ? 0 : DF_Percent.format(filtered_err_pct)) + "%)");
		}
		Logger.logln(" - Filtered URIs          : " + vo.getFilteredUriCount());
		if(setting.fieldMapping.isMappedIP()) {
			Logger.logln(" - Filtered IPs           : " + vo.getFilteredIpCount());
		}
		if(setting.fieldMapping.isMappedMethod()) {
			Logger.logln(" - Filtered METHODs       : " + vo.getFilteredMethodCount());
		}
		Logger.logln(" - Filtered EXTs          : " + vo.getFilteredExtCount());
		if(setting.fieldMapping.isMappedCode()) {
			Logger.logln(" - Filtered CODEs         : " + vo.getFilteredCodeCount());
		}
		Logger.logln(" - Daily Peak Time        : " + DateUtil.dateToString(vo.getDailyPeakTime(), SDF_DateOnly));
		Logger.logln(" - Daily Peak Requests    : " + vo.getDailyPeakCount());
		Logger.logln(" - Hourly Peak Time       : " + DateUtil.dateToString(vo.getHourlyPeakTime(), SDF_HourOnly));
		Logger.logln(" - Hourly Peak Requests   : " + vo.getHourlyPeakCount());
		if(setting.isCollectTPM()) {
			Logger.logln(" - Minutely Peak Time     : " + DateUtil.dateToString(vo.getMinutelyPeakTime(), SDF_DateMinute));
			Logger.logln(" - Minutely Peak Requests : " + vo.getMinutelyPeakCount());
		}
		if(setting.isCollectTPS()) {
			Logger.logln(" - Secondly Peak Time     : " + DateUtil.dateToString(vo.getSecondlyPeakTime(), SDF_DateSecond));
			Logger.logln(" - Secondly Peak Requests : " + vo.getSecondlyPeakCount());
		}
		if(setting.fieldMapping.isMappedElapsed() && setting.isCollectElapsedTime()) {
			Logger.logln(" - Bad Response Time      : " + vo.getBadElapsedCount() + " (>=" + setting.collectElapsedTimeMS + "ms)");
		}
		if(setting.fieldMapping.isMappedBytes() && setting.isCollectResponseBytes()) {
			Logger.logln(" - Bad Response Byte      : " + vo.getBadByteCount() + " (>=" + setting.collectResponseBytesKB + "KB)");
		}
		if(setting.fieldMapping.isMappedCode() && setting.isCollectErrors()) {
			Logger.logln(" - Bad Response Code      : " + vo.getBadCodeCount());
		}
		Logger.logln("----------------------------------------------------------------------");
		Logger.logln();
		Logger.logln();
	}

	private void printTPM(List<TPMEntryVO> timeData) throws Exception {
		if(timeData == null || timeData.size() < 1) {
			return;
		}
		String title = "Transactions Per " + (setting.tpmUnitMinutes == 1 ? "" : (Integer.toString(setting.tpmUnitMinutes) + "-")) + "Minute";
		ResultTable resultTable = createResultTable(title, timeData, Type.TPM);
		Logger.logln();
		Logger.logln();
		resultTable.print();
		Logger.logln();
		Logger.logln();
	}

	private void printDay(List<TimeAggregationEntryVO> timeData) throws Exception {
		if(timeData == null || timeData.size() < 1) {
			return;
		}
		String title = "Day Aggregation";
		ResultTable resultTable = createResultTable(title, timeData, Type.DAY);
		Logger.logln();
		Logger.logln();
		resultTable.print();
		Logger.logln();
		Logger.logln();
	}

	private void printHour(List<TimeAggregationEntryVO> timeData) throws Exception {
		if(timeData == null || timeData.size() < 1) {
			return;
		}
		String title = "Hour Aggregation";
		ResultTable resultTable = createResultTable(title, timeData, Type.HOUR);
		Logger.logln();
		Logger.logln();
		resultTable.print();
		Logger.logln();
		Logger.logln();
	}

	private <E extends KeyEntryVO> void printCount(List<E> cntData, Type type) throws Exception {
		if(cntData == null || cntData.size() < 1) {
			return;
		}
		String title = type.name() + " Aggregation";
		ResultTable resultTable = createResultTable(title, cntData, type);
		Logger.logln();
		Logger.logln();
		resultTable.print();
		Logger.logln();
		Logger.logln();
	}

	private <E extends TransactionEntryVO> void printResponse(List<E> resData, Type type) throws Exception {
		if(resData == null || resData.size() < 1) {
			return;
		}
		String title = "";
		if(type == Type.BAD_TIME) {
			title = "Bad Response Time Aggregation (over " + setting.collectElapsedTimeMS + "ms)";
		} else if(type == Type.BAD_BYTE) {
			title = "Bad Response Byte Aggregation (over " + setting.collectResponseBytesKB + "KB)";
		} else if(type == Type.BAD_CODE) {
			title = "Bad Response Code Aggregation";
		}
		ResultTable resultTable = createResultTable(title, resData, type);
		Logger.logln();
		Logger.logln();
		resultTable.print();
		Logger.logln();
		Logger.logln();
	}

	private <E extends EntryVO> ResultTable createResultTable(String title, List<E> data, Type type) throws Exception {
		ResultTable resultTable = new ResultTable();
		resultTable.setTitle(title);
		if(type == Type.TPM || type == Type.DAY || type == Type.HOUR) {
			resultTable.addColumn("TIME", Column.CENTER_ALIGN);
			if(setting.fieldMapping.isMappedIP()) {
				resultTable.addColumn("REQ IP", Column.RIGHT_ALIGN);
			}
			resultTable.addColumn("COUNT", Column.RIGHT_ALIGN);
			resultTable.addColumn("RATIO", Column.RIGHT_ALIGN);
			if(setting.fieldMapping.isMappedElapsed()) {
				resultTable.addColumn("AVG RESP", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX RESP", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX RESP TIME", Column.CENTER_ALIGN);
			}
			if(setting.fieldMapping.isMappedBytes()) {
				resultTable.addColumn("AVG BYTE", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX BYTE", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX BYTE TIME", Column.CENTER_ALIGN);
			}
			if(setting.fieldMapping.isMappedCode()) {
				resultTable.addColumn("ERROR", Column.RIGHT_ALIGN);
				resultTable.addColumn("ERR RATIO", Column.RIGHT_ALIGN);
				resultTable.addColumn("LAST ERROR", Column.LEFT_ALIGN);
				resultTable.addColumn("LAST ERROR TIME", Column.RIGHT_ALIGN);
			}
		} else if(type == Type.URI || type == Type.IP || type == Type.METHOD || type == Type.VERSION || type == Type.EXTENSION || type == Type.CODE) {
			resultTable.addColumn("No.", Column.RIGHT_ALIGN);
			resultTable.addColumn(type.name(), Column.LEFT_ALIGN);
			if(type == Type.IP) {
				resultTable.addColumn("COUNTRY", Column.LEFT_ALIGN);
			} else if(type == Type.CODE) {
				resultTable.addColumn("DESCRIPTION", Column.LEFT_ALIGN);
			}
			resultTable.addColumn("COUNT", Column.RIGHT_ALIGN);
			resultTable.addColumn("RATIO", Column.RIGHT_ALIGN);
			if(setting.fieldMapping.isMappedElapsed()) {
				resultTable.addColumn("AVG RESP", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX RESP", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX RESP TIME", Column.CENTER_ALIGN);
			}
			if(setting.fieldMapping.isMappedBytes()) {
				resultTable.addColumn("AVG BYTE", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX BYTE", Column.RIGHT_ALIGN);
				resultTable.addColumn("MAX BYTE TIME", Column.CENTER_ALIGN);
			}
			if(setting.fieldMapping.isMappedCode()) {
				resultTable.addColumn("ERROR", Column.RIGHT_ALIGN);
				resultTable.addColumn("ERR RATIO", Column.RIGHT_ALIGN);
				resultTable.addColumn("LAST ERROR", Column.LEFT_ALIGN);
				resultTable.addColumn("LAST ERROR TIME", Column.CENTER_ALIGN);
			}
		} else if(type == Type.BAD_TIME || type == Type.BAD_BYTE || type == Type.BAD_CODE) {
			resultTable.addColumn("No.", Column.RIGHT_ALIGN);
			resultTable.addColumn("REQ TIME", Column.LEFT_ALIGN);
			resultTable.addColumn("RES TIME", Column.LEFT_ALIGN);
			if(setting.fieldMapping.isMappedIP()) {
				resultTable.addColumn("IP", Column.LEFT_ALIGN);
			}
			resultTable.addColumn("URI", Column.LEFT_ALIGN);
			if(setting.fieldMapping.isMappedElapsed()) {
				resultTable.addColumn("ELAPSED TIME(ms)", Column.RIGHT_ALIGN);
			}
			if(setting.fieldMapping.isMappedBytes()) {
				resultTable.addColumn("RESP BYTE", Column.RIGHT_ALIGN);
			}
			if(setting.fieldMapping.isMappedCode()) {
				resultTable.addColumn("CODE", Column.LEFT_ALIGN);
			}
			if(setting.fieldMapping.isMappedMethod()) {
				resultTable.addColumn("METHOD", Column.LEFT_ALIGN);
			}
			if(setting.fieldMapping.isMappedVersion()) {
				resultTable.addColumn("VERSION", Column.LEFT_ALIGN);
			}
			resultTable.addColumn("EXT", Column.LEFT_ALIGN);
		}
		int size = data.size();
		for(int i = 0; i < size; i++) {
			Fields fields = new Fields(resultTable.getSizeOfColumns());
			if(type == Type.BAD_TIME || type == Type.BAD_BYTE || type == Type.BAD_CODE) {
				TransactionEntryVO vo = (TransactionEntryVO)data.get(i);
				fields.addField(i + 1);
				fields.addField(DateUtil.dateToString(vo.getRequestDate(), SDF_DateSecond));
				fields.addField(DateUtil.dateToString(vo.getResponseDate(), SDF_DateSecond));
				if(setting.fieldMapping.isMappedIP()) {
					fields.addField(vo.getRequestIP());
				}
				fields.addField(vo.getRequestURI());
				if(setting.fieldMapping.isMappedElapsed()) {
					fields.addField(vo.getResponseTime());
				}
				if(setting.fieldMapping.isMappedBytes()) {
					fields.addField(vo.getResponseBytes());
				}
				if(setting.fieldMapping.isMappedCode()) {
					fields.addField(vo.getResponseCode());
				}
				if(setting.fieldMapping.isMappedMethod()) {
					fields.addField(vo.getRequestMethod());
				}
				if(setting.fieldMapping.isMappedVersion()) {
					fields.addField(vo.getRequestVersion());
				}
				fields.addField(vo.getRequestExt());
			} else if(type == Type.TPM || type == Type.DAY || type == Type.HOUR) {
				DateEntryVO vo = (DateEntryVO)data.get(i);
				if(type == Type.TPM) {
					fields.addField(DateUtil.dateToString(vo.getUnitDate(), SDF_DateMinute) + " ~ " + DateUtil.dateToString(DateUtil.addDateUnit(vo.getUnitDate(), Calendar.MINUTE, setting.tpmUnitMinutes), SDF_NoDateMinute));
				} else if(type == Type.DAY) {
					fields.addField(DateUtil.dateToString(vo.getUnitDate(), SDF_DateOnly));
				} else if(type == Type.HOUR) {
					fields.addField(DateUtil.dateToString(vo.getUnitDate(), SDF_NoDateMinute) + " ~ " + DateUtil.dateToString(DateUtil.addDateUnit(vo.getUnitDate(), Calendar.MINUTE, 60), SDF_NoDateMinute));
				}
				if(setting.fieldMapping.isMappedIP()) {
					fields.addField(vo.getRequestIPCount());
				}
				fields.addField(vo.getRequestCount());
				fields.addField(vo.getFilterdRequestRatio());
				if(setting.fieldMapping.isMappedElapsed()) {
					fields.addField(DF_FloatPoint.format(vo.getAverageResponseTime()));
					fields.addField(vo.getMaxResponseTime().getResponseTime());
					fields.addField(DateUtil.dateToString(vo.getMaxResponseTime().getDate(), SDF_DateSecond));
				}
				if(setting.fieldMapping.isMappedBytes()) {
					fields.addField(DF_FloatPoint.format(vo.getAverageResponseBytes()));
					fields.addField(vo.getMaxResponseBytes().getResponseBytes());
					fields.addField(DateUtil.dateToString(vo.getMaxResponseBytes().getDate(), SDF_DateSecond));
				}
				if(setting.fieldMapping.isMappedCode()) {
					fields.addField(vo.getErrorCount());
					fields.addField(vo.getEntryErrorRatio());
					fields.addField(vo.getLastError().getResponseCode());
					fields.addField(DateUtil.dateToString(vo.getLastError().getDate(), SDF_DateSecond));
				}
			} else {
				KeyEntryVO vo = (KeyEntryVO)data.get(i);
				fields.addField(i + 1);
				fields.addField(vo.getKey());
				if(type == Type.IP || type == Type.CODE) {
					fields.addField(vo.getDescription());
				}
				fields.addField(vo.getRequestCount());
				fields.addField(vo.getFilterdRequestRatio());
				if(setting.fieldMapping.isMappedElapsed()) {
					fields.addField(DF_FloatPoint.format(vo.getAverageResponseTime()));
					fields.addField(vo.getMaxResponseTime().getResponseTime());
					fields.addField(DateUtil.dateToString(vo.getMaxResponseTime().getDate(), SDF_DateSecond));
				}
				if(setting.fieldMapping.isMappedBytes()) {
					fields.addField(DF_FloatPoint.format(vo.getAverageResponseBytes()));
					fields.addField(vo.getMaxResponseBytes().getResponseBytes());
					fields.addField(DateUtil.dateToString(vo.getMaxResponseBytes().getDate(), SDF_DateSecond));
				}
				if(setting.fieldMapping.isMappedCode()) {
					fields.addField(vo.getErrorCount());
					fields.addField(vo.getEntryErrorRatio());
					fields.addField(vo.getLastError().getResponseCode());
					fields.addField(DateUtil.dateToString(vo.getLastError().getDate(), SDF_DateSecond));
				}
			}
			resultTable.addFields(fields);
		}
		resultTable.setLayoutLine(false);
		return resultTable;
	}

}
