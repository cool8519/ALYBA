package dal.tool.analyzer.alyba.output;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.parse.task.OutputTask;
import dal.tool.analyzer.alyba.setting.LogAnalyzerSetting;
import dal.util.DateUtil;
import dal.util.db.ObjectDBUtil;

public class LogAnalyzeOutput {

	public static final SimpleDateFormat SDF_OutputFile = new SimpleDateFormat("yyyyMMdd.HHmmss");

	private LogAnalyzerSetting setting = null;
	private ObjectDBUtil db = null;
	private EntityManager em = null;

	public LogAnalyzeOutput(LogAnalyzerSetting setting) {
		this.setting = setting;
		SDF_OutputFile.setTimeZone(setting.getAnalyzerTimezone());
	}

	public LogAnalyzerSetting getAnalyzerSetting() {
		return setting;
	}
	
	public String getFileName() {
		return Constant.OUTPUT_FILENAME_PREFIX + "_" + setting.getTitle() + "_" + DateUtil.dateToString(setting.getAnalyzeDate(), SDF_OutputFile);
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

	private void checkAndMakeDir(String s_dir) throws Exception {
		File dir = new File(s_dir);
		if(!dir.isDirectory()) {
			if(!dir.mkdir()) {
				dal.tool.analyzer.alyba.ui.Logger.logln("Cannot create directory : " + dir.getCanonicalFile());
				dal.tool.analyzer.alyba.ui.Logger.logln("Create directory is changed : " + Constant.OUTPUT_TEMPORARY_DIRECTORY);
				setting.outputDirectory = Constant.OUTPUT_TEMPORARY_DIRECTORY;
			}
		}
	}

	private String getCreateFileName(String directory, String filename, String ext) {
		String filenameWithoutExt = directory + File.separatorChar + filename;
		String fname = filenameWithoutExt + "." + ext;
		File f = new File(fname);
		int idx = 1;
		while(f.exists()) {
			fname = filenameWithoutExt + "_" + idx + "." + ext;
			f = new File(fname);
			idx++;
		}
		return fname;
	}

	public List<String> out(OutputTask task) throws Exception {
		initDatabase();
		
		if(setting.outputDirectory == null || setting.outputDirectory.trim().equals("")) {
			setting.outputDirectory = Constant.OUTPUT_DEFAULT_DIRECTORY;
		}
		if(setting.title == null || setting.title.trim().equals("")) {
			setting.title = Constant.OUTPUT_DEFAULT_TITLE;
		}
		checkAndMakeDir(setting.outputDirectory);
		
		int idx = 0;
		try {
			task.setDetailStatus(idx, OutputTask.STATUS_PROCESSING);
			task.setDetailMessage("Inserting the setting to database");
			dal.tool.analyzer.alyba.ui.Logger.debug("Writing the setting to database : dbfile=" + db.getDBFilePath());
			db.insertWithTransaction(db.createEntityManager(), getSettingEntryVO(setting), true);
			task.addCurrent((int)((double)(idx+1) / (setting.getOutputCount()+1) * 100));
			task.setTasksPercent(idx, 100);
		} catch(Exception e) {
			task.setDetailStatus(idx, OutputTask.STATUS_ERROR);
			task.setDetailMessage("Stopped inserting the setting");
			e.printStackTrace();
			dal.tool.analyzer.alyba.ui.Logger.debug(e);
		}
		
		String filename = getFileName();
		String result_filename = null;
		List<String> outFileList = new ArrayList<String>();

		if(setting.outputExcelType) {
			idx++;
			try {
				task.setDetailStatus(idx, OutputTask.STATUS_PROCESSING);
				task.setDetailMessage("Generating excel output");
				result_filename = getCreateFileName(setting.outputDirectory, filename, "xls");
				dal.tool.analyzer.alyba.ui.Logger.debug("Generating excel output : file=" + result_filename);
				ExcelOutput output = new ExcelOutput(setting, db, em, result_filename);
				output.generate();
				outFileList.add(result_filename);
				task.addCurrent((int)((double)(idx+1) / (setting.getOutputCount()+1) * 100));
				task.setTasksPercent(idx, 100);
			} catch(Exception e) {
				task.setDetailStatus(idx, OutputTask.STATUS_ERROR);
				task.setDetailMessage("Stopped generating excel output");
				e.printStackTrace();
				dal.tool.analyzer.alyba.ui.Logger.debug(e);
			}
		}
		if(setting.outputHtmlType) {
			idx++;
			try {
				task.setDetailStatus(idx, OutputTask.STATUS_PROCESSING);
				task.setDetailMessage("Generating html output");
				result_filename = getCreateFileName(setting.outputDirectory, filename, "html");
				dal.tool.analyzer.alyba.ui.Logger.debug("Generating html output : file=" + result_filename);
				HtmlOutput output = new HtmlOutput(setting, db, em, result_filename);
				output.generate();
				outFileList.add(result_filename);
				task.addCurrent((int)((double)(idx+1) / (setting.getOutputCount()+1) * 100));
				task.setTasksPercent(idx, 100);
			} catch(Exception e) {
				task.setDetailStatus(idx, OutputTask.STATUS_ERROR);
				task.setDetailMessage("Stopped generating html output");
				e.printStackTrace();
				dal.tool.analyzer.alyba.ui.Logger.debug(e);
			}
		}
		if(setting.outputTextType) {
			idx++;
			try {
				task.setDetailStatus(idx, OutputTask.STATUS_PROCESSING);
				task.setDetailMessage("Generating text output");
				result_filename = getCreateFileName(setting.outputDirectory, filename, "txt");
				dal.tool.analyzer.alyba.ui.Logger.debug("Generating text output : file=" + result_filename);
				TextOutput output = new TextOutput(setting, db, em, result_filename);
				output.generate();
				outFileList.add(result_filename);
				task.addCurrent((int)((double)(idx+1) / (setting.getOutputCount()+1) * 100));
				task.setTasksPercent(idx, 100);
			} catch(Exception e) {
				task.setDetailStatus(idx, OutputTask.STATUS_ERROR);
				task.setDetailMessage("Stopped generating text output");
				e.printStackTrace();
				dal.tool.analyzer.alyba.ui.Logger.debug(e);
			}
		}
		closeDatabase();
		return outFileList;
	}

	private SettingEntryVO getSettingEntryVO(LogAnalyzerSetting setting) {
		SettingEntryVO vo = new SettingEntryVO();
		vo.title = setting.getTitle();
		vo.analyzeDate = setting.getAnalyzeDate();
		vo.logFileList = setting.getLogFileList();
		vo.outputExcelType = setting.isOutputExcelType();
		vo.outputHtmlType = setting.isOutputHtmlType();
		vo.outputTextType = setting.isOutputTextType();
		vo.outputDirectory = setting.getOutputDirectory();
		vo.outputSortBy = setting.getOutputSortBy();
		vo.multiThreadParsing = setting.isMultiThreadParsing();
		vo.checkFieldCount = setting.isCheckFieldCount();
		vo.allowErrors = setting.isAllowErrors();
		vo.allowErrorCount = setting.getAllowErrorCount();
		vo.uriIncludeParams = setting.isUriIncludeParams();
		vo.collectTPM = setting.isCollectTPM();
		vo.tpmUnitMinutes = setting.getTPMUnitMinutes();
		vo.collectElapsedTime = setting.isCollectElapsedTime();
		vo.collectElapsedTimeMS = setting.getCollectElapsedTimeMS();
		vo.collectResponseBytes = setting.isCollectResponseBytes();
		vo.collectResponseBytesKB = setting.getCollectResponseBytesKB();
		vo.collectErrors = setting.isCollectErrors();
		vo.collectIP = setting.isCollectIP();
		vo.collectTPS = setting.isCollectTPS();
		vo.mappingFieldDelimeter = setting.getFieldMapping().getFieldDelimeter();
		vo.mappingFieldBracelet = setting.getFieldMapping().getFieldBracelet();
		vo.mappingTimeFormat = setting.getFieldMapping().getTimeFormat();
		vo.mappingTimeLocale = setting.getFieldMapping().getTimeLocale().toString();
		vo.mappingElapsedUnit = setting.getFieldMapping().getElapsedUnit();
		vo.mappingOffsetHour = setting.getFieldMapping().getOffsetHour();
		vo.mappingFieldCount = setting.getFieldMapping().getFieldCount();
		vo.logMappingInfo = setting.getFieldMapping().getMappingInfo();
		vo.dateFilterEnable = !setting.getFilterSetting().isAllRangeEnable();
		vo.dateFilterFromRange = setting.getFilterSetting().getFromDateRange();
		vo.dateFilterToRange = setting.getFilterSetting().getToDateRange();
		vo.includeFilterEnable = setting.getFilterSetting().isIncludeFilterEnable();
		vo.includeFilterAndCheck = setting.getFilterSetting().isIncludeFilterAndCheck();
		vo.includeFilterIgnoreCase = setting.getFilterSetting().isIncludeFilterIgnoreCase();
		vo.includeFilterInfo = setting.getFilterSetting().getIncludeFilterInfo();
		vo.excludeFilterEnable = setting.getFilterSetting().isExcludeFilterEnable();
		vo.excludeFilterAndCheck = setting.getFilterSetting().isExcludeFilterAndCheck();
		vo.excludeFilterIgnoreCase = setting.getFilterSetting().isExcludeFilterIgnoreCase();
		vo.excludeFilterInfo = setting.getFilterSetting().getExcludeFilterInfo();
		return vo;
	}
	
}
