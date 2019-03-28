package dal.util.excel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import dal.tool.analyzer.alyba.util.Logger;
import jxl.CellView;
import jxl.Workbook;
import jxl.write.NumberFormats;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExcelWriter {

	public static int MAX_ROWS = 65536;
	public static int MAX_COLS = 256;
	public static int TITLE_ROWS = 4;

	private String dateFormatStr = "yyyy/MM/dd HH:mm:ss";
	private List<ExcelSheetVO> data = new ArrayList<ExcelSheetVO>();
	private String filename;
	WritableWorkbook workbook;

	public ExcelWriter(String filename) {
		setFileName(filename);
	}

	public String getFileName() {
		return filename;
	}

	public List<ExcelSheetVO> getData() {
		return data;
	}

	public String getDateFormat() {
		return dateFormatStr;
	}

	public void setFileName(String filename) {
		this.filename = filename;
	}

	public void setData(List<ExcelSheetVO> data) {
		this.data = data;
	}

	public void setDateFormat(String formatStr) {
		if(formatStr != null && !formatStr.trim().equals("")) {
			this.dateFormatStr = formatStr;
		}
	}

	public void setDateFormat(SimpleDateFormat format) {
		if(format != null) {
			this.dateFormatStr = format.toPattern();
		}
	}

	public void addSheet(String name, String title, List<ExcelColumn> columns, List<List<Object>> sheetData) {
		ExcelSheetVO sheetVO = new ExcelSheetVO(name, title, columns, sheetData);
		addSheet(sheetVO);
	}

	public void addSheet(ExcelSheetVO sheetVO) {
		data.add(sheetVO);
	}

	public void write() throws Exception {
		try {
			workbook = Workbook.createWorkbook(new File(filename));

			WritableFont tFont = new WritableFont(WritableFont.ARIAL, 20, WritableFont.BOLD);
			WritableCellFormat tFormat = new WritableCellFormat(tFont);
			tFormat.setAlignment(jxl.format.Alignment.LEFT);

			WritableCellFormat hFormat = new WritableCellFormat();
			hFormat.setBackground(jxl.format.Colour.GRAY_25);
			hFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			hFormat.setAlignment(jxl.format.Alignment.CENTRE);

			WritableCellFormat defFormat = new WritableCellFormat();
			defFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);

			List<ExcelColumn> columns = null;
			List<List<Object>> sheetData = null;
			List<Object> sheetRowData = null;
			Object tempData = null;
			ExcelSheetVO vo = null;
			WritableSheet sheet = null;
			WritableCellFormat format = null;
			WritableCell cell = null;
			ExcelColumn col = null;

			for(int sheetIdx = 0; sheetIdx < data.size(); sheetIdx++) {
				List<Object> cellFormatList = new ArrayList<Object>();
				vo = (ExcelSheetVO)data.get(sheetIdx);
				sheet = workbook.createSheet(vo.getName(), workbook.getNumberOfSheets());
				jxl.write.Label label;
				sheetData = vo.getData();
				int dataRows = sheetData.size();
				int overCnt = 0;
				int sheetRow = 0;
				int from_row = 0;
				for(int dRow = 0; dRow < dataRows; dRow++) {
					sheetRowData = (List<Object>)sheetData.get(dRow);
					if(sheetRow == 0) {
						from_row = 0;
						if(vo.isShowTitle()) {
							from_row++;
							label = new jxl.write.Label(0, from_row, vo.getTitle(), tFormat);
							sheet.addCell(label);
							from_row = from_row + (TITLE_ROWS - 1);
						}
						columns = vo.getColumns();
						for(int tCol = 0; tCol < columns.size(); tCol++) {
							col = (ExcelColumn)columns.get(tCol);
							if(vo.isShowHeader()) {
								label = new jxl.write.Label(tCol, from_row, col.getName(), hFormat);
								sheet.addCell(label);
							}
							CellView cellView = new CellView();
							if(col.getWidth() > 0) {
								cellView.setSize(col.getWidth() * 256);
							} else {
								cellView.setAutosize(true);
							}
							sheet.setColumnView(tCol, cellView);
						}
						if(vo.isShowHeader()) {
							from_row++;
						}
					}
					for(int dCol = 0; dCol < sheetRowData.size(); dCol++) {
						tempData = sheetRowData.get(dCol);
						if((dRow == 0 || cellFormatList.size() < columns.size()) && dCol <= columns.size() - 1) {
							col = (ExcelColumn)columns.get(dCol);
							WritableCellFormat customFormat = createCellFormat(col, tempData);
							if(tempData == null) {
								Object tempData2;
								int tempRow = dRow + 1;
								if(dataRows > 1) {
									do {
										tempData2 = ((List<Object>)sheetData.get(tempRow++)).get(dCol);
									} while(tempRow < dataRows - 1 && tempData2 == null);
									customFormat = createCellFormat(col, tempData2);
								}
							}
							cellFormatList.add(customFormat);
						}
						if(dCol > columns.size() - 1) {
							cellFormatList.add(defFormat);
						}
						try {
							format = (WritableCellFormat)cellFormatList.get(dCol);
							if(tempData instanceof java.util.Date) {
								cell = new jxl.write.DateTime(dCol, from_row + sheetRow, (java.util.Date)sheetRowData.get(dCol), format);
							} else if(tempData instanceof java.lang.Number) {
								cell = new jxl.write.Number(dCol, from_row + sheetRow, ((java.lang.Number)sheetRowData.get(dCol)).doubleValue(), format);
							} else {
								cell = new jxl.write.Label(dCol, from_row + sheetRow, (String)sheetRowData.get(dCol), format);
							}
						} catch(Exception e) {
							Logger.loglnStackTrace(e, Logger.DEBUG);
							cell = new jxl.write.Label(dCol, from_row + sheetRow, "?", defFormat);
						} finally {
							try {
								sheet.addCell(cell);
							} catch(Throwable t) {
								Logger.loglnStackTrace(t, Logger.DEBUG);
							}
						}
					}
					sheetRow++;
					WritableSheet tempSheet = checkMaxRows(vo, sheet, overCnt);
					if(tempSheet != null) {
						overCnt++;
						sheet = tempSheet;
						sheetRow = 0;
					}
				}
			}

			workbook.write();

		} catch(Exception e) {
			throw e;
		} finally {
			if(workbook != null) {
				try {
					workbook.close();
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

	private WritableCellFormat createCellFormat(ExcelColumn col, Object colData) throws Exception {
		if(col.getFormat() != null) {
			return col.getFormat();
		} else {
			WritableCellFormat cellFormat;
			if(colData instanceof java.util.Date) {
				jxl.write.DateFormat customDate = new jxl.write.DateFormat(dateFormatStr);
				cellFormat = new WritableCellFormat(customDate);
			} else if(colData instanceof java.lang.Number) {
				cellFormat = new WritableCellFormat(NumberFormats.DEFAULT);
			} else {
				cellFormat = new WritableCellFormat();
			}
			cellFormat.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			if(col.getAlign() == ExcelColumn.LEFT_ALIGN) {
				cellFormat.setAlignment(jxl.format.Alignment.LEFT);
			} else if(col.getAlign() == ExcelColumn.RIGHT_ALIGN) {
				cellFormat.setAlignment(jxl.format.Alignment.RIGHT);
			} else {
				cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			}
			return cellFormat;
		}
	}

	private WritableSheet checkMaxRows(ExcelSheetVO vo, WritableSheet sheet, int over_cnt) {
		if(sheet.getRows() >= MAX_ROWS) {
			sheet.setName(vo.getName() + "_" + (over_cnt + 1));
			WritableSheet new_sheet = workbook.createSheet((vo.getName() + "_" + (over_cnt + 2)), workbook.getNumberOfSheets());
			if(vo.isShowTitle()) {
				try {
					sheet.addCell(new jxl.write.Label(0, 0, "Page_" + (over_cnt + 1), new WritableCellFormat()));
					new_sheet.addCell(new jxl.write.Label(0, 0, "Page_" + (over_cnt + 2), new WritableCellFormat()));
				} catch(Exception e) {
				}
			}
			return new_sheet;
		} else {
			return null;
		}
	}

}
