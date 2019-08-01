package dal.tool.analyzer.alyba.output;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

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
import dal.util.DateUtil;
import dal.util.db.ObjectDBUtil;
import dal.util.excel.ExcelColumn;
import dal.util.excel.ExcelSheetVO;
import dal.util.excel.ExcelWriter;

public class ExcelOutput extends ResultOutput {

	private ExcelWriter writer = null;

	public ExcelOutput(AnalyzerSetting setting, ObjectDBUtil db, EntityManager em, String filename) {
		super(setting, db, em, filename);
	}

	public void generate() throws Exception {
		try {
			dal.tool.analyzer.alyba.ui.Logger.logln("Writing to the excel file : " + filename);
			exportToExcel(filename);
		} catch(Exception e) {
			try {
				File f = new File(filename);
				if(f.exists()) {
					f.delete();
				}
			} catch(Exception e2) {
			}
			dal.tool.analyzer.alyba.ui.Logger.logln("Failed to create excel file : " + filename);
			throw e;
		}
	}

	private void exportToExcel(String fname) throws Exception {
		writer = new ExcelWriter(fname);		
		writer.setDateFormat(SDF_DateSecond);

		try {
			SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
			summaryVo.setCreatedTime(new Date());
			createSummarySheet(summaryVo);
			createTPMSheet(getEntryList(TPMEntryVO.class, null));
			createDaySheet(getEntryList(DailyEntryVO.class, null));
			createHourSheet(getEntryList(HourlyEntryVO.class, null));
			createCountSheet(getEntryList(KeyEntryVO.class, "URI"), Type.URI);
			createCountSheet(getEntryList(KeyEntryVO.class, "IP"), Type.IP);
			createCountSheet(getEntryList(KeyEntryVO.class, "METHOD"), Type.METHOD);
			createCountSheet(getEntryList(KeyEntryVO.class, "VERSION"), Type.VERSION);
			createCountSheet(getEntryList(KeyEntryVO.class, "EXT"), Type.EXTENSION);
			createCountSheet(getEntryList(KeyEntryVO.class, "CODE"), Type.CODE);

			int long_resp = summaryVo.getBadElapsedCount();
			if(long_resp > 0) {
				if(long_resp > ExcelWriter.MAX_ROWS-5) {
					dal.tool.analyzer.alyba.ui.Logger.logln("Too many long transaction : count=" + long_resp);
				} else {
					createResponseSheet(getEntryList(BadResponseEntryVO.class, "TIME"), Type.BAD_TIME);
				}
			}

			int large_resp = summaryVo.getBadByteCount();
			if(large_resp > 0) {
				if(large_resp > ExcelWriter.MAX_ROWS-5) {
					dal.tool.analyzer.alyba.ui.Logger.logln("Too many large response : count=" + large_resp);
				} else {
					createResponseSheet(getEntryList(BadResponseEntryVO.class, "SIZE"), Type.BAD_BYTE);
				}
			}

			int total_err = summaryVo.getBadCodeCount();
			if(total_err > 0) {
				if(total_err > ExcelWriter.MAX_ROWS-5) {
					dal.tool.analyzer.alyba.ui.Logger.logln("Too many error response : count=" + total_err);
				} else {
					createResponseSheet(getEntryList(BadResponseEntryVO.class, "CODE"), Type.BAD_CODE);
				}
			}

			writer.write();
		} catch(Exception e) {
			dal.tool.analyzer.alyba.ui.Logger.debug(e);
			throw e;
		}
	}
	
	private void createSummarySheet(SummaryEntryVO vo) throws Exception {
		int total_req = vo.getTotalRequestCount();
		int filtered_req = vo.getFilteredRequestCount();
		int total_err = vo.getTotalErrorCount();
		int filtered_err = vo.getFilteredErrorCount();
		double filtered_req_pct = (double)filtered_req / total_req * 100;
		double filtered_err_pct = (double)filtered_err / total_err * 100;
		Date f_req_time = vo.getFirstRequestTime();
		Date l_req_time = vo.getLastRequestTime();
		Date f_filter_time = vo.getFilterFromTime();
		Date l_filter_time = vo.getFilterToTime();		
		
		ExcelSheetVO sheet;
		sheet = new ExcelSheetVO("Summary");
		sheet.setShowHeader(false);
		sheet.addColumn("", ExcelColumn.CENTER_ALIGN, 30);
		sheet.addColumn("", ExcelColumn.LEFT_ALIGN);
		sheet.setTitle("Summary");

		SimpleDateFormat local_tz_sdf = (SimpleDateFormat)SDF_DateSecondTZ.clone();
		local_tz_sdf.setTimeZone(Constant.TIMEZONE_DEFAULT);
		
		List<List<Object>> list = new ArrayList<List<Object>>();
		List<Object> rowData;
		rowData = new ArrayList<Object>();
		rowData.add("Title");
		rowData.add(setting.title);
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Parsed Date");
		rowData.add(DateUtil.dateToString(vo.getParsedTime(), local_tz_sdf));
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Created Date");
		rowData.add(DateUtil.dateToString(vo.getCreatedTime(), local_tz_sdf));
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Request Range");
		rowData.add(((f_req_time == null) ? "N/A" : (DateUtil.dateToString(f_req_time, SDF_DateSecond) + " ~ " + DateUtil.dateToString(l_req_time, SDF_DateSecond))));
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Total Requests");
		rowData.add(new Integer(total_req));
		list.add(rowData);
		if(setting.fieldMapping.isMappedCode()) {
			rowData = new ArrayList<Object>();
			rowData.add("Total Errors");
			rowData.add(new Integer(total_err));
			list.add(rowData);
		}
		rowData = new ArrayList<Object>();
		rowData.add("Time Range Filter");
		rowData.add(((setting.filterSetting.allRangeEnable) ? "N/A" : (DateUtil.dateToString(f_filter_time, SDF_DateSecond) + " ~ " + DateUtil.dateToString(l_filter_time, SDF_DateSecond))));
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Include Filter");
		rowData.add(vo.getFilterIncludeInfo());
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Exclude Filter");
		rowData.add(vo.getFilterExcludeInfo());
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Filtered Requests");
		rowData.add(filtered_req + " (" + (Double.isNaN(filtered_req_pct) ? 0 : DF_Percent.format(filtered_req_pct)) + "%)");
		list.add(rowData);
		if(setting.fieldMapping.isMappedCode()) {
			rowData = new ArrayList<Object>();
			rowData.add("Filtered Errors");
			rowData.add(filtered_err + " (" + (Double.isNaN(filtered_err_pct) ? 0 : DF_Percent.format(filtered_err_pct)) + "%)");
			list.add(rowData);
		}
		rowData = new ArrayList<Object>();
		rowData.add("Filtered URIs");
		rowData.add(vo.getFilteredUriCount());
		list.add(rowData);
		if(setting.fieldMapping.isMappedIP()) {
			rowData = new ArrayList<Object>();
			rowData.add("Filtered IPs");
			rowData.add(vo.getFilteredIpCount());
			list.add(rowData);
		}
		if(setting.fieldMapping.isMappedMethod()) {
			rowData = new ArrayList<Object>();
			rowData.add("Filtered METHODs");
			rowData.add(vo.getFilteredMethodCount());
			list.add(rowData);
		}
		rowData = new ArrayList<Object>();
		rowData.add("Filtered EXTs");
		rowData.add(vo.getFilteredExtCount());
		list.add(rowData);
		if(setting.fieldMapping.isMappedCode()) {
			rowData = new ArrayList<Object>();
			rowData.add("Filtered CODEs");
			rowData.add(vo.getFilteredCodeCount());
			list.add(rowData);
		}
		rowData = new ArrayList<Object>();
		rowData.add("Daily Peak Time");
		rowData.add(DateUtil.dateToString(vo.getDailyPeakTime(), SDF_DateOnly));
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Daily Peak Requests");
		rowData.add(vo.getDailyPeakCount());
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Hourly Peak Time");
		rowData.add(DateUtil.dateToString(vo.getHourlyPeakTime(), SDF_HourOnly));
		list.add(rowData);
		rowData = new ArrayList<Object>();
		rowData.add("Hourly Peak Requests");
		rowData.add(vo.getHourlyPeakCount());
		list.add(rowData);
		if(setting.isCollectTPM()) {
			rowData = new ArrayList<Object>();
			rowData.add("Minutely Peak Time");
			rowData.add(DateUtil.dateToString(vo.getMinutelyPeakTime(), SDF_DateMinute));
			list.add(rowData);
			rowData = new ArrayList<Object>();
			rowData.add("Minutely Peak Requests");
			rowData.add(vo.getMinutelyPeakCount());
			list.add(rowData);
		}
		if(setting.isCollectTPS()) {
			rowData = new ArrayList<Object>();
			rowData.add("Secondly Peak Time");
			rowData.add(DateUtil.dateToString(vo.getSecondlyPeakTime(), SDF_DateSecond));
			list.add(rowData);
			rowData = new ArrayList<Object>();
			rowData.add("Secondly Peak Requests");
			rowData.add(vo.getSecondlyPeakCount());
			list.add(rowData);
		}
		if(setting.fieldMapping.isMappedElapsed() && setting.isCollectElapsedTime()) {
			rowData = new ArrayList<Object>();
			rowData.add("Bad Response Time");
			rowData.add(vo.getBadElapsedCount() + " (>=" + setting.collectElapsedTimeMS + "ms)");
			list.add(rowData);
		}
		if(setting.fieldMapping.isMappedBytes() && setting.isCollectResponseBytes()) {
			rowData = new ArrayList<Object>();
			rowData.add("Bad Response Byte");
			rowData.add(vo.getBadByteCount() + " (>=" + setting.collectResponseBytesKB + "KB)");
			list.add(rowData);
		}
		if(setting.fieldMapping.isMappedCode() && setting.isCollectErrors()) {
			rowData = new ArrayList<Object>();
			rowData.add("Bad Response Code");
			rowData.add(vo.getBadCodeCount());
			list.add(rowData);
		}
		sheet.setData(list);
		writer.addSheet(sheet);
	}

	private void createTPMSheet(List<TPMEntryVO> timeData) throws Exception {
		if(timeData == null || timeData.size() < 1) {
			return;
		}
		String title = "Transactions Per " + (setting.tpmUnitMinutes == 1 ? "" : (Integer.toString(setting.tpmUnitMinutes) + "-")) + "Minute";
		ExcelSheetVO sheet = createExcelSheet("TPM", title, timeData, Type.TPM);
		writer.addSheet(sheet);
	}

	private void createDaySheet(List<DailyEntryVO> timeData) throws Exception {
		if(timeData == null || timeData.size() < 1) {
			return;
		}
		String title = "Day Aggregation";
		ExcelSheetVO sheet = createExcelSheet("DAY", title, timeData, Type.DAY);
		writer.addSheet(sheet);
	}

	private void createHourSheet(List<HourlyEntryVO> timeData) throws Exception {
		if(timeData == null || timeData.size() < 1) {
			return;
		}
		String title = "Hour Aggregation";
		ExcelSheetVO sheet = createExcelSheet("HOUR", title, timeData, Type.HOUR);
		writer.addSheet(sheet);
	}

	private <E extends KeyEntryVO> void createCountSheet(List<E> cntData, Type type) throws Exception {
		if(cntData == null || cntData.size() < 1) {
			return;
		}
		String title = type.name() + " Aggregation";
		ExcelSheetVO sheet = createExcelSheet(type.name(), title, cntData, type);
		writer.addSheet(sheet);
	}

	private <E extends ResponseEntryVO> void createResponseSheet(List<E> resData, Type type) throws Exception {
		if(resData == null || resData.size() < 1) {
			return;
		}
		String name = null;
		String title = "";
		if(type == Type.BAD_TIME) {
			name = "BAD_TIME";
			title = "Bad Response Time Aggregation (over " + setting.collectElapsedTimeMS + "ms)";
		} else if(type == Type.BAD_BYTE) {
			name = "BAD_BYTE";
			title = "Bad Response Byte Aggregation (over " + setting.collectResponseBytesKB + "KB)";
		} else if(type == Type.BAD_CODE) {
			name = "BAD_CODE";
			title = "Bad Response Code Aggregation";
		}
		ExcelSheetVO sheet = createExcelSheet(name, title, resData, type);
		writer.addSheet(sheet);
	}

	private <E extends EntryVO> ExcelSheetVO createExcelSheet(String name, String title, List<E> data, Type type) throws Exception {
		ExcelSheetVO vo = new ExcelSheetVO(name);
		vo.setTitle(title);
		if(type == Type.TPM || type == Type.DAY || type == Type.HOUR) {
			if(type == Type.TPM) {
				vo.addColumn("TIME", ExcelColumn.CENTER_ALIGN, 25, SDF_DateMinute);
			} else if(type == Type.DAY) {
				vo.addColumn("TIME", ExcelColumn.CENTER_ALIGN, 25, SDF_DateOnly);
			} else if(type == Type.HOUR) {
				vo.addColumn("TIME", ExcelColumn.CENTER_ALIGN, 25, SDF_NoDateMinute);
			}
			if(setting.fieldMapping.isMappedIP()) {
				vo.addColumn("REQ IP", ExcelColumn.RIGHT_ALIGN, 10);
			}
			vo.addColumn("COUNT", ExcelColumn.RIGHT_ALIGN, 15);
			vo.addColumn("RATIO", ExcelColumn.RIGHT_ALIGN, 15);
			if(setting.fieldMapping.isMappedElapsed()) {
				vo.addColumn("AVG RESP", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX RESP", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX RESP TIME", ExcelColumn.CENTER_ALIGN, 25);
			}
			if(setting.fieldMapping.isMappedBytes()) {
				vo.addColumn("AVG BYTE", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX BYTE", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX BYTE TIME", ExcelColumn.CENTER_ALIGN, 25);
			}
			if(setting.fieldMapping.isMappedCode()) {
				vo.addColumn("ERROR", ExcelColumn.RIGHT_ALIGN, 10);
				vo.addColumn("ERR RATIO", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("LAST ERROR", ExcelColumn.LEFT_ALIGN, 15);
				vo.addColumn("LAST ERROR TIME", ExcelColumn.CENTER_ALIGN, 25);
			}
		} else if(type == Type.URI || type == Type.IP || type == Type.METHOD || type == Type.VERSION || type == Type.EXTENSION || type == Type.CODE) {
			if(type == Type.URI) {
				vo.addColumn(type.name(), ExcelColumn.LEFT_ALIGN, 100);
			} else if(type == Type.IP) {
				vo.addColumn(type.name(), ExcelColumn.CENTER_ALIGN);
				vo.addColumn("COUNTRY", ExcelColumn.LEFT_ALIGN, 30);
			} else if(type == Type.CODE) {
				vo.addColumn(type.name(), ExcelColumn.CENTER_ALIGN);
				vo.addColumn("DESCRIPTION", ExcelColumn.LEFT_ALIGN, 40);
			} else {
				vo.addColumn(type.name(), ExcelColumn.LEFT_ALIGN, 25);
			}
			vo.addColumn("COUNT", ExcelColumn.RIGHT_ALIGN, 15);
			vo.addColumn("RATIO", ExcelColumn.RIGHT_ALIGN, 15);
			if(setting.fieldMapping.isMappedElapsed()) {
				vo.addColumn("AVG RESP", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX RESP", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX RESP TIME", ExcelColumn.CENTER_ALIGN, 25);
			}
			if(setting.fieldMapping.isMappedBytes()) {
				vo.addColumn("AVG BYTE", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX BYTE", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("MAX BYTE TIME", ExcelColumn.CENTER_ALIGN, 25);
			}
			if(setting.fieldMapping.isMappedCode()) {
				vo.addColumn("ERROR", ExcelColumn.RIGHT_ALIGN, 10);
				vo.addColumn("ERR RATIO", ExcelColumn.RIGHT_ALIGN, 15);
				vo.addColumn("LAST ERROR", ExcelColumn.LEFT_ALIGN, 15);
				vo.addColumn("LAST ERROR TIME", ExcelColumn.CENTER_ALIGN, 25);
			}
		} else if(type == Type.BAD_TIME || type == Type.BAD_BYTE || type == Type.BAD_CODE) {
			if(setting.fieldMapping.isMappedElapsed()) {
				vo.addColumn("REQ TIME", ExcelColumn.CENTER_ALIGN, 25);
			}
			vo.addColumn("TIME", ExcelColumn.CENTER_ALIGN, 25);
			if(setting.fieldMapping.isMappedIP()) {
				vo.addColumn("IP", ExcelColumn.LEFT_ALIGN, 25);
			}
			vo.addColumn("URI", ExcelColumn.LEFT_ALIGN, 100);
			if(setting.fieldMapping.isMappedElapsed()) {
				vo.addColumn("RESP TIME", ExcelColumn.RIGHT_ALIGN, 25);
			}
			if(setting.fieldMapping.isMappedBytes()) {
				vo.addColumn("RESP BYTE", ExcelColumn.RIGHT_ALIGN, 25);
			}
			if(setting.fieldMapping.isMappedCode()) {
				vo.addColumn("CODE", ExcelColumn.LEFT_ALIGN, 15);
			}
			if(setting.fieldMapping.isMappedMethod()) {
				vo.addColumn("METHOD", ExcelColumn.LEFT_ALIGN, 15);
			}
			if(setting.fieldMapping.isMappedVersion()) {
				vo.addColumn("VERSION", ExcelColumn.LEFT_ALIGN, 25);
			}
			vo.addColumn("EXT", ExcelColumn.LEFT_ALIGN, 15);
		}
		List<List<Object>> list = new ArrayList<List<Object>>();
		List<Object> row;
		int size = data.size();
		for(int i = 0; i < size; i++) {
			row = new ArrayList<Object>();
			if(type == Type.BAD_TIME || type == Type.BAD_BYTE || type == Type.BAD_CODE) {
				ResponseEntryVO entryVO = (ResponseEntryVO)data.get(i);
				entryVO = (ResponseEntryVO)data.get(i);
				if(setting.fieldMapping.isMappedElapsed()) {
					row.add(entryVO.getRequestDate());
				}
				row.add(entryVO.getResponseDate());
				if(setting.fieldMapping.isMappedIP()) {
					row.add(entryVO.getRequestIP());
				}
				row.add(entryVO.getRequestURI());
				if(setting.fieldMapping.isMappedElapsed()) {
					row.add(entryVO.getResponseTime());
				}
				if(setting.fieldMapping.isMappedBytes()) {
					row.add(entryVO.getResponseBytes());
				}
				if(setting.fieldMapping.isMappedCode()) {
					row.add(entryVO.getResponseCode());
				}
				if(setting.fieldMapping.isMappedMethod()) {
					row.add(entryVO.getRequestMethod());
				}
				if(setting.fieldMapping.isMappedVersion()) {
					row.add(entryVO.getRequestVersion());
				}
				row.add(entryVO.getRequestExt());
			} else if(type == Type.TPM || type == Type.DAY || type == Type.HOUR) {
				DateEntryVO entryVO = (DateEntryVO)data.get(i);
				row.add(entryVO.getUnitDate());
				if(setting.fieldMapping.isMappedIP()) {
					row.add(entryVO.getRequestIPCount());
				}
				row.add(entryVO.getRequestCount());
				row.add(entryVO.getFilterdRequestRatio());
				if(setting.fieldMapping.isMappedElapsed()) {
					row.add(entryVO.getAverageResponseTime());
					row.add(entryVO.getMaxResponseTime().getResponseTime());
					row.add(entryVO.getMaxResponseTime().getResponseDate());
				}
				if(setting.fieldMapping.isMappedBytes()) {
					row.add(entryVO.getAverageResponseBytes());
					row.add(entryVO.getMaxResponseBytes().getResponseBytes());
					row.add(entryVO.getMaxResponseBytes().getResponseDate());
				}
				if(setting.fieldMapping.isMappedCode()) {
					row.add(entryVO.getErrorCount());
					row.add(entryVO.getEntryErrorRatio());
					row.add(entryVO.getLastError().getResponseCode());
					row.add(entryVO.getLastError().getResponseDate());
				}
			} else {
				KeyEntryVO entryVO = (KeyEntryVO)data.get(i);
				row.add(entryVO.getKey());
				if(type == Type.IP || type == Type.CODE) {
					row.add(entryVO.getDescription());
				}
				row.add(entryVO.getRequestCount());
				row.add(entryVO.getFilterdRequestRatio());
				if(setting.fieldMapping.isMappedElapsed()) {
					row.add(entryVO.getAverageResponseTime());
					row.add(entryVO.getMaxResponseTime().getResponseTime());
					row.add(entryVO.getMaxResponseTime().getResponseDate());
				}
				if(setting.fieldMapping.isMappedBytes()) {
					row.add(entryVO.getAverageResponseBytes());
					row.add(entryVO.getMaxResponseBytes().getResponseBytes());
					row.add(entryVO.getMaxResponseBytes().getResponseDate());
				}
				if(setting.fieldMapping.isMappedCode()) {
					row.add(entryVO.getErrorCount());
					row.add(entryVO.getEntryErrorRatio());
					row.add(entryVO.getLastError().getResponseCode());
					row.add(entryVO.getLastError().getResponseDate());
				}
			}
			list.add(row);
		}
		vo.setData(list);
		return vo;
	}

}
