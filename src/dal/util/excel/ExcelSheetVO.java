package dal.util.excel;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import jxl.write.WritableCellFormat;

public class ExcelSheetVO {

	private String name = null;
	private String title = null;
	private List<ExcelColumn> columns = new ArrayList<ExcelColumn>();
	private List<List<Object>> data = new ArrayList<List<Object>>();
	private boolean show_title = true;
	private boolean show_header = true;

	public boolean isShowTitle() {
		return show_title;
	}

	public void setShowTitle(boolean show_title) {
		this.show_title = show_title;
	}

	public boolean isShowHeader() {
		return show_header;
	}

	public void setShowHeader(boolean show_header) {
		this.show_header = show_header;
	}

	public ExcelSheetVO(String name) {
		setName(name);
	}

	public ExcelSheetVO(String name, List<List<Object>> data) {
		setName(name);
		setData(data);
	}

	public ExcelSheetVO(String name, String title, List<ExcelColumn> columns, List<List<Object>> data) {
		setName(name);
		setTitle(title);
		setColumns(columns);
		setData(data);
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public List<ExcelColumn> getColumns() {
		return columns;
	}

	public List<List<Object>> getData() {
		return data;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setColumns(List<ExcelColumn> columns) {
		this.columns = columns;
	}

	public void setData(List<List<Object>> data) {
		this.data = data;
	}

	public void addColumn(String columnName) {
		columns.add(new ExcelColumn(columnName));
	}

	public void addColumn(String columnName, int align) {
		ExcelColumn column = new ExcelColumn(columnName, align);
		columns.add(column);
	}

	public void addColumn(String columnName, int align, int width) {
		ExcelColumn column = new ExcelColumn(columnName, align, width);
		columns.add(column);
	}

	public void addColumn(String columnName, int align, int width, WritableCellFormat format) {
		ExcelColumn column = new ExcelColumn(columnName, align, width, format);
		columns.add(column);
	}

	public void addColumn(String columnName, int align, WritableCellFormat format) {
		ExcelColumn column = new ExcelColumn(columnName, align, -1, format);
		columns.add(column);
	}

	public void addColumn(String columnName, int align, Format format) {
		addColumn(columnName, align, -1, format);
	}

	public void addColumn(String columnName, int align, int width, Format format) {
		ExcelColumn column = new ExcelColumn(columnName, align, width, format);
		columns.add(column);
	}

	public void addColumn(ExcelColumn column) {
		columns.add(column);
	}

	public void addColumn(ExcelColumn column, int align) {
		column.setAlign(align);
		columns.add(column);
	}

	public void addData(List<Object> rowData) {
		data.add(rowData);
	}

	public int getColumnSize() {
		return columns.size();
	}

	public int getRowSize() {
		return data.size();
	}

}
