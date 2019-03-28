package dal.util.csv;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVWriter {

    public static final char NEW_LINE = '\n';
    public static final char COMMA = ',';

	private String filename;
	private char rec_sep = NEW_LINE;
	private char col_sep = COMMA;
	private List<?> header = null;
	private List<List<?>> data = new ArrayList<List<?>>();

	public CSVWriter(String filename) {
		setFileName(filename);
	}

	public String getFileName() {
		return filename;
	}

	public char getRecordSeparator() {
		return rec_sep;
	}

	public char getColumnSeparator() {
		return col_sep;
	}

	public List<List<?>> getData() {
		return data;
	}

	public List<?> getHeader() {
		return header;
	}

	public void setFileName(String filename) {
		this.filename = filename;
	}

	public void setRecordSeparator(char sep) {
		this.rec_sep = sep;
	}

	public void setColumnSeparator(char sep) {
		this.col_sep = sep;
	}

	public void setData(List<List<?>> data) {
		this.data = data;
	}
	
	public void setHeader(List<?> header) {
		this.header = header;
	}

	public void write() throws Exception {
		CSVPrinter csvPrinter = null;
		CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator(rec_sep).withDelimiter(col_sep);

		try {
			csvPrinter = new CSVPrinter(new FileWriter(filename), csvFormat);
			
			if(header != null && header.size() > 0) {
				csvPrinter.printRecord(header);
			}

			for(List<?> record : data) {
				csvPrinter.printRecord(record);				
			}
			
			csvPrinter.flush();
		} catch(Exception e) {
			throw e;
		} finally {
			if(csvPrinter != null) {
				try {
					csvPrinter.close();
				} catch(Throwable t) {
					t.printStackTrace();
					try {
						File f = new File(filename);
						if(f.exists()) {
							f.delete();
						}
					} catch(Exception e2) {
					}
				}
			}
		}
	}

}
