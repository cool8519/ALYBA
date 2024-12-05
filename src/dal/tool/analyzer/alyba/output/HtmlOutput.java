package dal.tool.analyzer.alyba.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import dal.util.StringUtil;
import dal.util.db.ObjectDBUtil;

public class HtmlOutput extends ResultOutput {

	public HtmlOutput(LogAnalyzerSetting setting, ObjectDBUtil db, EntityManager em, String filename) {
		super(setting, db, em, filename);
	}

	public void generate() throws Exception {
		try {
			dal.tool.analyzer.alyba.ui.Logger.debug("Writing to the html file : " + filename);
			exportToHtml(filename);
		} catch(Exception e) {
			try {
				File f = new File(filename);
				if(f.exists()) {
					f.delete();
				}
			} catch(Exception e2) {
			}
			dal.tool.analyzer.alyba.ui.Logger.debug("Failed to create html file : " + filename);
			throw e;
		}
	}

	private void exportToHtml(String fname) throws Exception {
		Logger.openFile(fname, false);
		Logger.setTarget(Logger.FILE);

		try {
			String content = getHtmlContent(ClassLoader.getSystemResource(Constant.FILE_PATH_HTMLTEMPLATE).openStream());
			if(content != null) {
				Logger.logln(content);
			}
		} catch(Exception e) {
			dal.tool.analyzer.alyba.ui.Logger.debug(e);
			throw e;
		} finally {
			Logger.closeFile();
			Logger.setTarget(Logger.CONSOLE);
		}
	}

	private String getHtmlContent(InputStream is) throws Exception {
		StringBuffer htmlContent = new StringBuffer();
		BufferedReader in = null;
		try {
			SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
			summaryVo.setCreatedTime(new Date());

			in = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while((line = in.readLine()) != null) {
				try {
					line = replaceVariable(line, summaryVo);
				} catch(Exception e) {
					dal.tool.analyzer.alyba.ui.Logger.debug("Failed to replace data to html format");
					dal.tool.analyzer.alyba.ui.Logger.debug(e);
				} finally {
					htmlContent.append(line + "\n");
				}
			}
		} catch(Exception e) {
			dal.tool.analyzer.alyba.ui.Logger.debug("Failed to get html content from template file");
			dal.tool.analyzer.alyba.ui.Logger.debug(e);
			return null;
		} finally {
			if(in != null) {
				in.close();
			}
		}
		return htmlContent.toString();
	}
	
	private String replaceVariable(String line, SummaryEntryVO summaryVo) throws Exception {
		String temp = line;
		SimpleDateFormat local_tz_sdf = (SimpleDateFormat)SDF_DateSecondTZ.clone();
		local_tz_sdf.setTimeZone(Constant.TIMEZONE_DEFAULT);		

		while(temp.indexOf("${") > -1 && temp.indexOf("}", temp.indexOf("${")) > -1) {
			int init = temp.indexOf("${");
			int end = temp.indexOf("}", init);
			String variable = temp.substring(init + 2, end);
			String value = null;
			if(variable.equals("TITLE")) {
				value = setting.title;
			} else if(variable.equals("PARSED_DATE")) {
				value = DateUtil.dateToString(summaryVo.getParsedTime(), local_tz_sdf);
			} else if(variable.equals("CREATED_DATE")) {
				value = DateUtil.dateToString(summaryVo.getCreatedTime(), local_tz_sdf);
			} else if(variable.equals("REQUEST_RANGE")) {
				value = (summaryVo.getFirstRequestTime() == null) ? "N/A" : (DateUtil.dateToString(summaryVo.getFirstRequestTime(), SDF_DateSecond) + " ~ " + DateUtil.dateToString(summaryVo.getLastRequestTime(), SDF_DateSecond));
			} else if(variable.equals("TOTAL_REQ_COUNT")) {
				value = new Long(summaryVo.getTotalRequestCount()).toString();
			} else if(variable.equals("TOTAL_ERR_COUNT")) {
				value = new Integer(summaryVo.getTotalErrorCount()).toString();
			} else if(variable.equals("TIME_RANGE_FILTER")) {
				value = (setting.filterSetting.allRangeEnable) ? "N/A" : (DateUtil.dateToString(summaryVo.getFilterFromTime(), SDF_DateSecond) + " ~ " + DateUtil.dateToString(summaryVo.getFilterToTime(), SDF_DateSecond));
			} else if(variable.equals("INCLUDE_FILTER")) {
				value = summaryVo.getFilterExcludeInfo();
			} else if(variable.equals("EXCLUDE_FILTER")) {
				value = summaryVo.getFilterExcludeInfo();
			} else if(variable.equals("FILTERED_REQ_COUNT")) {
				long total_req = summaryVo.getTotalRequestCount();
				long filtered_req = summaryVo.getFilteredRequestCount();
				value = filtered_req + " (" + ((double)filtered_req / total_req * 100) + "%)";
			} else if(variable.equals("FILTERED_ERR_COUNT")) {
				int total_err = summaryVo.getTotalErrorCount();
				int filtered_err = summaryVo.getFilteredErrorCount();
				value = filtered_err + " (" + ((double)filtered_err / total_err * 100) + "%)";
			} else if(variable.equals("FILTERED_URI_COUNT")) {
				value = new Integer(summaryVo.getFilteredUriCount()).toString();
			} else if(variable.equals("FILTERED_IP_COUNT")) {
				value = new Integer(summaryVo.getFilteredIpCount()).toString();
			} else if(variable.equals("FILTERED_METHOD_COUNT")) {
				value = new Integer(summaryVo.getFilteredMethodCount()).toString();
			} else if(variable.equals("FILTERED_EXT_COUNT")) {
				value = new Integer(summaryVo.getFilteredExtCount()).toString();
			} else if(variable.equals("FILTERED_CODE_COUNT")) {
				value = new Integer(summaryVo.getFilteredCodeCount()).toString();
			} else if(variable.equals("PEAK_DAILY_DATE")) {
				value = DateUtil.dateToString(summaryVo.getDailyPeakTime(), SDF_DateOnly);
			} else if(variable.equals("PEAK_DAILY_COUNT")) {
				value = new Integer(summaryVo.getDailyPeakCount()).toString();
			} else if(variable.equals("PEAK_HOURLY_DATE")) {
				value = DateUtil.dateToString(summaryVo.getHourlyPeakTime(), SDF_HourOnly);
			} else if(variable.equals("PEAK_HOURLY_COUNT")) {
				value = new Integer(summaryVo.getHourlyPeakCount()).toString();
			} else if(variable.equals("PEAK_MINUTELY_DATE")) {
				value = DateUtil.dateToString(summaryVo.getMinutelyPeakTime(), SDF_DateMinute);
			} else if(variable.equals("PEAK_MINUTELY_COUNT")) {
				value = new Integer(summaryVo.getMinutelyPeakCount()).toString();				
			} else if(variable.equals("BAD_RESPONSE_TIME")) {
				value = new Integer(summaryVo.getBadElapsedCount()).toString() + " (>=" + setting.collectElapsedTimeMS + "ms)";
			} else if(variable.equals("BAD_RESPONSE_BYTE")) {
				value = new Integer(summaryVo.getBadByteCount()).toString() + " (>=" + setting.collectResponseBytesKB + "KB)";
			} else if(variable.equals("BAD_RESPONSE_CODE")) {
				value = new Integer(summaryVo.getBadCodeCount()).toString();
			} else if(variable.equals("TPM_AGGR_UNIT")) {
				value = setting.tpmUnitMinutes == 1 ? "" : (Integer.toString(setting.tpmUnitMinutes) + "-");
			} else if(variable.equals("BAD_RESPONSE_TIME_AGGR_MS")) {
				value = new Long(setting.collectElapsedTimeMS).toString();
			} else if(variable.equals("BAD_RESPONSE_BYTE_AGGR_KB")) {
				value = new Integer(setting.collectResponseBytesKB).toString();
			} else if(variable.equals("TPM_AGGR_DATA")) {
				value = createHtmlString("tpm_div", getEntryList(TPMEntryVO.class, null), Type.TPM);
			} else if(variable.equals("DAY_AGGR_DATA")) {
				value = createHtmlString("day_div", getEntryList(TimeAggregationEntryVO.class, "DAY"), Type.DAY);
			} else if(variable.equals("HOUR_AGGR_DATA")) {
				value = createHtmlString("hour_div", getEntryList(TimeAggregationEntryVO.class, "HOUR"), Type.HOUR);
			} else if(variable.equals("URI_AGGR_DATA")) {
				value = createHtmlString("uri_div", getEntryList(KeyEntryVO.class, "URI"), Type.URI);
			} else if(variable.equals("IP_AGGR_DATA")) {
				value = createHtmlString("ip_div", getEntryList(KeyEntryVO.class, "IP"), Type.IP);
			} else if(variable.equals("METHOD_AGGR_DATA")) {
				value = createHtmlString("method_div", getEntryList(KeyEntryVO.class, "METHOD"), Type.METHOD);
			} else if(variable.equals("VERSION_AGGR_DATA")) {
				value = createHtmlString("version_div", getEntryList(KeyEntryVO.class, "VERSION"), Type.VERSION);
			} else if(variable.equals("EXT_AGGR_DATA")) {
				value = createHtmlString("ext_div", getEntryList(KeyEntryVO.class, "EXT"), Type.EXTENSION);
			} else if(variable.equals("CODE_AGGR_DATA")) {
				value = createHtmlString("code_div", getEntryList(KeyEntryVO.class, "CODE"), Type.CODE);
			} else if(variable.equals("BAD_RESPONSE_TIME_AGGR_DATA")) {
				int long_resp = summaryVo.getBadElapsedCount();
				if(long_resp > 1000) {
					dal.tool.analyzer.alyba.ui.Logger.debug("Too many long transaction : count=" + long_resp);
					value = getHideDivHtml("restime_div");
				} else {
					value = createHtmlString("restime_div", getEntryList(BadTransactionEntryVO.class, "TIME"), Type.BAD_TIME);
				}
			} else if(variable.equals("BAD_RESPONSE_BYTE_AGGR_DATA")) {
				int large_resp = summaryVo.getBadByteCount();
				if(large_resp > 1000) {
					dal.tool.analyzer.alyba.ui.Logger.debug("Too many large response : count=" + large_resp);
					value = getHideDivHtml("resbyte_div");
				} else {
					value = createHtmlString("resbyte_div", getEntryList(BadTransactionEntryVO.class, "SIZE"), Type.BAD_BYTE);
				}
			} else if(variable.equals("BAD_RESPONSE_CODE_AGGR_DATA")) {
				int total_err = summaryVo.getBadCodeCount();
				if(total_err > 1000) {
					dal.tool.analyzer.alyba.ui.Logger.debug("Too many error response : count=" + total_err);
					value = getHideDivHtml("rescode_div");
				} else {
					value = createHtmlString("rescode_div", getEntryList(BadTransactionEntryVO.class, "CODE"), Type.BAD_CODE);
				}
			} else {
				dal.tool.analyzer.alyba.ui.Logger.debug("No such variable data (" + variable + ")");
				return line;
			}
			temp = temp.substring(0, init) + value + temp.substring(end + 1);
		}

		return temp;
	}

	private <E extends EntryVO> String createHtmlString(String div, List<E> data, Type type) throws Exception {
		StringBuffer sb = new StringBuffer();
		if(data == null || data.size() < 1) {
			sb.append(getHideDivHtml(div));
		} else {
			String td_front_center = "<td class=\"TableCellCenter\">";
			String td_front_left = "<td class=\"TableCellLeft\">";
			String td_front_right = "<td class=\"TableCellRight\">";
			String td_rear = "</td>";
			int size = data.size();
			int cnt = 1;
			for(int i = 0; i < size; i++) {
				if(type == Type.BAD_TIME || type == Type.BAD_BYTE || type == Type.BAD_CODE) {
					TransactionEntryVO vo = (TransactionEntryVO)data.get(i);
					sb.append("<tr class=\"TableRow" + cnt + "\">");
					sb.append(td_front_right + (i + 1) + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getRequestDate(), SDF_DateSecond)) + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getResponseDate(), SDF_DateSecond)) + td_rear);
					sb.append(td_front_left + NVL(vo.getRequestIP()) + td_rear);
					sb.append(td_front_left + NVL(vo.getRequestURI()) + td_rear);
					sb.append(td_front_right + NVL(vo.getResponseTime()) + td_rear);
					sb.append(td_front_right + NVL(vo.getResponseBytes()) + td_rear);
					sb.append(td_front_left + NVL(vo.getResponseCode()) + td_rear);
					sb.append(td_front_left + NVL(vo.getRequestMethod()) + td_rear);
					sb.append(td_front_left + NVL(vo.getRequestVersion()) + td_rear);
					sb.append(td_front_left + NVL(vo.getRequestExt()) + td_rear);
					sb.append("</tr>");
				} else if(type == Type.TPM || type == Type.DAY || type == Type.HOUR) {
					DateEntryVO vo = (DateEntryVO)data.get(i);
					if(vo.getRequestCount() < 0) {
						continue;
					}
					sb.append("<tr class=\"TableRow" + cnt + "\">");
					if(type == Type.TPM) {
						sb.append(td_front_center + DateUtil.dateToString(vo.getUnitDate(), SDF_DateMinute) + "&nbsp;&nbsp;~&nbsp;&nbsp;" + DateUtil.dateToString(DateUtil.addDateUnit(vo.getUnitDate(), Calendar.MINUTE, setting.tpmUnitMinutes), SDF_NoDateMinute) + td_rear);
					} else if(type == Type.DAY) {
						sb.append(td_front_center + DateUtil.dateToString(vo.getUnitDate(), SDF_DateOnly) + td_rear);
					} else if(type == Type.HOUR) {
						sb.append(td_front_center + DateUtil.dateToString(vo.getUnitDate(), SDF_NoDateMinute) + "&nbsp;&nbsp;~&nbsp;&nbsp;" + DateUtil.dateToString(DateUtil.addDateUnit(vo.getUnitDate(), Calendar.MINUTE, 60), SDF_NoDateMinute) + td_rear);
					}
					sb.append(td_front_right + vo.getRequestIPCount() + td_rear);
					sb.append(td_front_right + vo.getRequestCount() + td_rear);
					sb.append(td_front_right + vo.getFilterdRequestRatio() + td_rear);
					sb.append(td_front_right + DF_FloatPoint.format(vo.getAverageResponseTime()) + td_rear);
					sb.append(td_front_right + vo.getMaxResponseTime().getResponseTime() + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getMaxResponseTime().getDate(), SDF_DateSecond)) + td_rear);
					sb.append(td_front_right + DF_FloatPoint.format(vo.getAverageResponseBytes()) + td_rear);
					sb.append(td_front_right + vo.getMaxResponseBytes().getResponseBytes() + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getMaxResponseBytes().getDate(), SDF_DateSecond)) + td_rear);
					sb.append(td_front_right + vo.getErrorCount() + td_rear);
					sb.append(td_front_right + vo.getEntryErrorRatio() + td_rear);
					sb.append(td_front_left + NVL(vo.getLastError().getResponseCode()) + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getLastError().getDate(), SDF_DateSecond)) + td_rear);					
					sb.append("</tr>");
				} else {
					KeyEntryVO vo = (KeyEntryVO)data.get(i);
					sb.append("<tr class=\"TableRow" + cnt + "\">");
					sb.append(td_front_right + (i + 1) + td_rear);
					sb.append(td_front_center + vo.getKey() + td_rear);
					if(type == Type.IP || type == Type.CODE) {
						sb.append(td_front_left + StringUtil.NVL(vo.getDescription(),"UNKNOWN") + td_rear);
					}
					sb.append(td_front_right + vo.getRequestCount() + td_rear);
					sb.append(td_front_right + vo.getFilterdRequestRatio() + td_rear);
					sb.append(td_front_right + DF_FloatPoint.format(vo.getAverageResponseTime()) + td_rear);
					sb.append(td_front_right + vo.getMaxResponseTime().getResponseTime() + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getMaxResponseTime().getDate(), SDF_DateSecond)) + td_rear);
					sb.append(td_front_right + DF_FloatPoint.format(vo.getAverageResponseBytes()) + td_rear);
					sb.append(td_front_right + vo.getMaxResponseBytes().getResponseBytes() + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getMaxResponseBytes().getDate(), SDF_DateSecond)) + td_rear);
					sb.append(td_front_right + vo.getErrorCount() + td_rear);
					sb.append(td_front_right + vo.getEntryErrorRatio() + td_rear);
					sb.append(td_front_left + NVL(vo.getLastError().getResponseCode()) + td_rear);
					sb.append(td_front_center + NVL(DateUtil.dateToString(vo.getLastError().getDate(), SDF_DateSecond)) + td_rear);
					sb.append("</tr>");
				}
				cnt = (cnt == 1) ? 2 : 1;
			}
		}
		return sb.toString();
	}
	
	private String getHideDivHtml(String div) {
		return "<script language='javascript'>document.getElementById('" + div + "').style.display = 'none';</script>";
	}
	
	private String NVL(long value) {
		return (value < 0) ? "" : String.valueOf(value);
	}

	private String NVL(String value) {
		return StringUtil.NVL(value);
	}

}
