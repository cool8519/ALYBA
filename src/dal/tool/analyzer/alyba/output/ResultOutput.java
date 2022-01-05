package dal.tool.analyzer.alyba.output;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;

import dal.tool.analyzer.alyba.output.vo.BadTransactionEntryVO;
import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.setting.LogAnalyzerSetting;
import dal.util.db.ObjectDBUtil;

public abstract class ResultOutput {

	protected static enum Type { TPM, DAY, HOUR, URI, IP, METHOD, VERSION, EXTENSION, CODE, BAD_TIME, BAD_BYTE, BAD_CODE };

	protected static final DecimalFormat DF_Percent = new DecimalFormat("##0.000");
	protected static final DecimalFormat DF_FloatPoint = new DecimalFormat("0.0");
	protected static final SimpleDateFormat SDF_DateSecondTZ = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
	protected static final SimpleDateFormat SDF_DateSecond = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	protected static final SimpleDateFormat SDF_DateOnly = new SimpleDateFormat("yyyy.MM.dd");
	protected static final SimpleDateFormat SDF_HourOnly = new SimpleDateFormat("HH:mm");
	protected static final SimpleDateFormat SDF_DateMinute = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	protected static final SimpleDateFormat SDF_NoDateMinute = new SimpleDateFormat("HH:mm");
	protected static final SimpleDateFormat SDF_OuputFile = new SimpleDateFormat("yyyyMMdd.HHmm");

	protected LogAnalyzerSetting setting = null;
	protected ObjectDBUtil db = null;
	protected EntityManager em = null;
	protected String filename = null;

	public ResultOutput(LogAnalyzerSetting setting, ObjectDBUtil db, EntityManager em, String filename) {
		this.setting = setting;
		this.db = db;
		this.em = em;
		this.filename = filename;
	}

	protected <E extends EntryVO> List<E> getEntryList(Class<E> clazz, String type) throws Exception {
		String condition = "";
		String sortColumn = null;
		boolean desc = false;
		if(clazz.getSuperclass() == DateEntryVO.class) {
			sortColumn = "unit_date";
			if(type != null) {
				condition = "WHERE vo.type = '" + type + "' ";
			}
		} else if(clazz == KeyEntryVO.class) {
			if(setting.outputSortBy == "COUNT") {
				sortColumn = "req_count";
				desc = true;
			} else if(setting.outputSortBy == "NAME") {
				sortColumn = "key";
			}
			condition = "WHERE vo.type = '" + type + "' ";
		} else if(clazz == BadTransactionEntryVO.class) {
			if("TIME".equals(type)) {
				sortColumn = "response_time";
				desc = true;
			} else if("SIZE".equals(type)) {
				sortColumn = "response_byte";
				desc = true;
			} else if("CODE".equals(type)) {
				sortColumn = "response_date";
			}
			condition = "WHERE vo.type = '" + type + "' ";
		} else {
			throw new Exception("Not Supported Class type.");
		}
		return db.selectList(em, "SELECT vo FROM " + clazz.getSimpleName() + " AS vo " + condition + "ORDER BY vo." + sortColumn + (desc?" DESC":""), clazz, null);
	}
	
	public abstract void generate() throws Exception;
	
}
