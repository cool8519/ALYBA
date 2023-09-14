package dal.tool.analyzer.alyba.parse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileInfo {

	private File file;
	private Map<String,String> meta = new HashMap<String,String>();

	public FileInfo() {}
	
	public FileInfo(File file) {
		this.file = file;
	}
	
	public FileInfo(File file, Map<String,String> meta) {
		this.file = file;
		if(meta != null) {
			this.meta = meta;
		}
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getFilePath() {
		try {
			return file.getCanonicalPath();
		} catch(IOException ioe) {
			return file.getAbsolutePath();
		}
	}
	
	public void setFileMeta(String key, String value) {
		meta.put(key, value);
	}
	
	public String getFileMeta(String key) {
		return meta.get(key);
	}

	@Override
	public String toString() {
		return "FileInfo(filePath=" + getFilePath() + ", meta=" + meta + ")";
	}

}
