package dal.tool.analyzer.alyba.ui.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.ui.Logger;

public class HistoryManager {

	public static String dirname = Constant.OUTPUT_TEMPORARY_DIRECTORY+File.separator+"ALYBA"+File.separator+Constant.PROGRAM_VERSION;
	public static String propname = "result_history.properties";
	private File dirfile = null;
	private File propfile = null;
	private Properties props = new Properties();
	private boolean needFlush = false;

	public HistoryManager() throws Exception {
		dirfile = new File(dirname);
		propfile = new File(dirfile, propname);
		checkDirectoryAndFile();
		FileInputStream fis = new FileInputStream(propfile);
		props.load(fis);
		fis.close();
	}
	
	public List<HistoryVO> getAllHistories() throws Exception {
		Iterator<Object> it = props.keySet().iterator();
		List<HistoryVO> list = new ArrayList<HistoryVO>();
		while(it.hasNext()) {
			list.add(HistoryVO.fromValue(props.getProperty((String)(it.next()))));
		}
		return list;
	}
	
	public HistoryVO getHistory(String key) throws Exception {
		String value = props.getProperty(key);
		return HistoryVO.fromValue(value);
	}
	
	public void addHistory(HistoryVO vo) throws Exception {
		needFlush = true;
		props.setProperty(vo.getKey(), vo.toEncodedString());
		Logger.debug("Added the history in memory : " + vo.getKey());
	}
	
	public HistoryVO deleteHistory(String key) throws Exception {
		needFlush = true;
		String value = (String)props.remove(key);
		Logger.debug("Deleted the history in memory : " + key);
		return HistoryVO.fromValue(value);
	}
	
	public HistoryVO deleteHistory(HistoryVO vo) throws Exception {
		return deleteHistory(vo.getKey());
	}

	private void checkDirectoryAndFile() throws Exception {
		if(!dirfile.exists()) {
			dirfile.mkdirs();
			Logger.debug("Created the directory for histories : " + dirfile.getCanonicalPath());
		}
		if(!propfile.exists()) {
			propfile.createNewFile();
			Logger.debug("Created the file for histories : " + propfile.getCanonicalPath());
		}		
	}
	
	public void flush() throws Exception {
		FileOutputStream fos = new FileOutputStream(propfile);
		props.store(fos, null);
		Logger.debug("Saved all the histories in memory : " + propfile.getCanonicalPath());
		fos.close();
		needFlush = false;
	}
	
	public void close() throws Exception {
		if(needFlush) {
			flush();
		}
	}
	
}
