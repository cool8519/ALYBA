package dal.tool.analyzer.alyba.output;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.persistence.EntityManager;

import dal.tool.analyzer.alyba.output.vo.BadResponseEntryVO;
import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.setting.AnalyzerSetting;
import dal.util.db.ObjectDBUtil;

public abstract class ResultOutput {

	protected static enum Type { TPM, DAY, HOUR, URI, IP, METHOD, VERSION, EXTENSION, CODE, BAD_TIME, BAD_BYTE, BAD_CODE };

	protected static DecimalFormat DF_Percent = new DecimalFormat("##0.000");
	protected static DecimalFormat DF_FloatPoint = new DecimalFormat("0.0");
	protected static SimpleDateFormat SDF_DateSecondTZ = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
	protected static SimpleDateFormat SDF_DateSecond = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	protected static SimpleDateFormat SDF_DateOnly = new SimpleDateFormat("yyyy.MM.dd");
	protected static SimpleDateFormat SDF_HourOnly = new SimpleDateFormat("HH:mm");
	protected static SimpleDateFormat SDF_DateMinute = new SimpleDateFormat("yyyy.MM.dd HH:mm");
	protected static SimpleDateFormat SDF_NoDateMinute = new SimpleDateFormat("HH:mm");
	protected static SimpleDateFormat SDF_OuputFile = new SimpleDateFormat("yyyyMMdd.HHmm");

	protected AnalyzerSetting setting = null;
	protected ObjectDBUtil db = null;
	protected EntityManager em = null;
	protected String filename = null;

	public ResultOutput(AnalyzerSetting setting, ObjectDBUtil db, EntityManager em, String filename) {
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
		} else if(clazz == KeyEntryVO.class) {
			if(setting.outputSortBy == "COUNT") {
				sortColumn = "req_count";
				desc = true;
			} else if(setting.outputSortBy == "NAME") {
				sortColumn = "key";
			}
			condition = "WHERE vo.type = '" + type + "' ";
		} else if(clazz == BadResponseEntryVO.class) {
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
		return db.selectList(em, "SELECT vo FROM " + clazz.getSimpleName() + " AS vo " + condition + "ORDER BY " + sortColumn + (desc?" DESC":""), clazz, null);
	}
	
	public abstract void generate() throws Exception;
	
}
