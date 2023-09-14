package dal.tool.analyzer.alyba.ui.history;

import java.io.File;
import java.util.Base64;
import java.util.Date;

import dal.util.DateUtil;

public class HistoryVO {

	private String title;
	private long created;
	private long fileSize;
	private boolean fileExists;
	private String fileName;
	private String filePath;
	
	public HistoryVO(String fileName, String filePath) {
		this.fileName = fileName;
		this.filePath = filePath;
		title = "";
		created = -1L;
		File file = new File(filePath, fileName);
		fileExists = file.exists();
		fileSize = fileExists ? file.length() : -1L;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public boolean getFileExists() {
		return fileExists;
	}
	
	public void setFileExists(boolean fileExists) {
		this.fileExists = fileExists;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public String[] getFieldArray() {
		String[] arr = new String[6];
		arr[0] = title;
		arr[1] = DateUtil.dateToString(new Date(created), "yyyy.MM.dd/HH:mm:ss");
		arr[2] = String.valueOf(fileSize);
		arr[3] = fileExists?"O":"X";
		arr[4] = fileName;
		arr[5] = filePath;
		return arr;
	}

	public String toEncodedString() {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append("|");
		sb.append(created).append("|");
		sb.append(fileSize).append("|");
		sb.append(fileName).append("|");
		sb.append(filePath).append(";");
		return Base64.getEncoder().encodeToString(sb.toString().getBytes());
	}
	
	public String getKey() {
		String keyStr = created + fileName + filePath;
		return Integer.toString(keyStr.hashCode());
	}

	public static HistoryVO fromValue(String value_encoded) throws Exception {
		if(value_encoded == null) {
			return null;
		}
		String value = new String(Base64.getDecoder().decode(value_encoded));
		if(!value.endsWith(";")) {
			throw new Exception("value must ends with a semi-colon.");
		}
		String[] token = value.substring(0, value.length()-1).split("\\|");
		if(token.length != 5) {
			throw new Exception("value must have 5 tokens.");
		}
		try {
			HistoryVO vo = new HistoryVO(token[3], token[4]);
			vo.setTitle(token[0]);
			vo.setCreated(Long.parseLong(token[1]));
			vo.setFileSize(vo.getFileExists() ? vo.getFileSize() : Long.parseLong(token[2]));
			return vo;
		} catch(Exception e) {
			throw e;
		}
	}

}
