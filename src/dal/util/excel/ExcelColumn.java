package dal.util.excel;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import jxl.write.WritableCellFormat;

public class ExcelColumn {

	public static final int LEFT_ALIGN = -1;
	public static final int RIGHT_ALIGN = 1;
	public static final int CENTER_ALIGN = 0;

	private String name;
	private int width = -1;
	private int align = RIGHT_ALIGN;
	private WritableCellFormat format = null;

	public ExcelColumn(String name) {
		this.name = name;
	}

	public ExcelColumn(String name, int align) {
		this.name = name;
		this.align = align;
	}

	public ExcelColumn(String name, int align, int width) {
		this.name = name;
		this.align = align;
		this.width = width;
	}

	public ExcelColumn(String name, int align, int width, WritableCellFormat format) {
		this.name = name;
		this.align = align;
		this.width = width;
		setFormat(format);
	}

	public ExcelColumn(String name, int align, int width, Format format) {
		this.name = name;
		this.align = align;
		this.width = width;
		WritableCellFormat cellFormat = null;
		if(format instanceof DecimalFormat) {
			jxl.write.NumberFormat customNumber = new jxl.write.NumberFormat(((DecimalFormat)format).toPattern());
			cellFormat = new WritableCellFormat(customNumber);
		} else if(format instanceof SimpleDateFormat) {
			jxl.write.DateFormat customDate = new jxl.write.DateFormat(((SimpleDateFormat)format).toPattern());
			cellFormat = new WritableCellFormat(customDate);
		}
		setFormat(cellFormat);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getAlign() {
		return align;
	}

	public void setAlign(int align) {
		this.align = align;
		setFormat(format);
	}

	public WritableCellFormat getFormat() {
		return format;
	}

	public void setFormat(WritableCellFormat format) {
		this.format = format;
		try {
			format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			if(align == LEFT_ALIGN) {
				format.setAlignment(jxl.format.Alignment.LEFT);
			} else if(align == RIGHT_ALIGN) {
				format.setAlignment(jxl.format.Alignment.RIGHT);
			} else {
				format.setAlignment(jxl.format.Alignment.CENTRE);
			}
		} catch(Exception e) {
		}
	}

}
