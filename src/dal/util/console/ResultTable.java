package dal.util.console;

import java.util.ArrayList;

import dal.tool.analyzer.alyba.util.Logger;

public class ResultTable {

	private static final int FREE_SPACE_SIZE = 1;
	private static final String SPACE = " ";
	private static final String COLUMN_SEPARATOR = "|";
	private static final String LINE_ELEMENT = "-";
	private static final String LAYOUT_LINE_ELEMENT = "=";

	private final ArrayList<Column> columns = new ArrayList<Column>();
	private final ArrayList<Fields> listOfFields = new ArrayList<Fields>();
	private String title;
	private String addendum;
	private int layoutLineLength;
	private boolean showLayoutLine = true;

	public ResultTable() {
	}

	public void addColumn(String columnName) {
		columns.add(new Column(columnName));
	}

	public void addColumn(String columnName, int align) {
		Column column = new Column(columnName);
		column.setAlign(align);
		columns.add(column);
	}

	public void addColumn(Column column) {
		columns.add(column);
	}

	public void addColumn(Column column, int align) {
		column.setAlign(align);
		columns.add(column);
	}

	public void addFields(Fields fields) {
		listOfFields.add(fields);
	}

	public Column getColumn(int index) {
		return (Column)columns.get(index);
	}

	public Fields getFields(int index) {
		return (Fields)listOfFields.get(index);
	}

	public int getSizeOfColumns() {
		return columns.size();
	}

	public int getSizeOfListOfFields() {
		return listOfFields.size();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddendum() {
		return addendum;
	}

	public void setAddendum(String addendum) {
		this.addendum = addendum;
	}

	public int getLayoutLineLength() {
		return layoutLineLength;
	}

	public void setLayoutLineLength(int layoutLineLength) {
		this.layoutLineLength = layoutLineLength;
	}

	public void setLayoutLine(boolean showLayoutLine) {
		this.showLayoutLine = showLayoutLine;
	}

	public void print() {
		StringBuffer lineForRowSeparation = new StringBuffer();
		lineForRowSeparation.append(LINE_ELEMENT);
		for(int i = 0; i < getSizeOfColumns(); i++) {
			int length = getColumn(i).getLength();
			for(int j = 0; j < getSizeOfListOfFields(); j++) {
				Fields fields = getFields(j);
				if(length < fields.getField(i).getLength())
					length = fields.getField(i).getLength();
			}

			getColumn(i).setMaxLengthOfField(length);
			addCharatersLikeLength(lineForRowSeparation, LINE_ELEMENT, 1);
			addCharatersLikeLength(lineForRowSeparation, LINE_ELEMENT, length);
			addCharatersLikeLength(lineForRowSeparation, LINE_ELEMENT, 1);
			lineForRowSeparation.append(LINE_ELEMENT);
		}

		StringBuffer columnLine = new StringBuffer();
		columnLine.append(COLUMN_SEPARATOR);
		for(int i = 0; i < getSizeOfColumns(); i++) {
			Column column = getColumn(i);
			int spaceLength = column.getMaxLengthOfField() - column.getLength();
			addFreeSpace(columnLine);
			if(column.getAlign() == Column.LEFT_ALIGN) {
				columnLine.append(column);
				addCharatersLikeLength(columnLine, SPACE, spaceLength);
			} else if(column.getAlign() == Column.RIGHT_ALIGN) {
				addCharatersLikeLength(columnLine, SPACE, spaceLength);
				columnLine.append(column);
			} else if(column.getAlign() == Column.CENTER_ALIGN) {
				int half = spaceLength / 2;
				addCharatersLikeLength(columnLine, SPACE, half);
				columnLine.append(column);
				addCharatersLikeLength(columnLine, SPACE, half + (spaceLength % 2));
			}
			addFreeSpace(columnLine);
			columnLine.append(COLUMN_SEPARATOR);
		}

		StringBuffer layoutLine = new StringBuffer(lineForRowSeparation.length());
		if(showLayoutLine) {
			addCharatersLikeLength(layoutLine, LAYOUT_LINE_ELEMENT, lineForRowSeparation.length());
			setLayoutLineLength(lineForRowSeparation.length());
			Logger.logln(layoutLine.toString());
		}
		if(getTitle() != null) {
			Logger.logln(getTitle());
		}
		if(getSizeOfColumns() > 0) {
			Logger.logln(lineForRowSeparation.toString());
			Logger.logln(columnLine.toString());
			Logger.logln(lineForRowSeparation.toString());
			for(int i = 0; i < getSizeOfListOfFields(); i++) {
				StringBuffer fieldsLine = new StringBuffer();
				fieldsLine.append(COLUMN_SEPARATOR);
				Fields fields = getFields(i);
				for(int j = 0; j < fields.getLength(); j++) {
					Column column = getColumn(j);
					int spaceLength = column.getMaxLengthOfField() - fields.getField(j).getLength();
					addFreeSpace(fieldsLine);
					if(column.getAlign() == Column.LEFT_ALIGN) {
						fieldsLine.append(fields.getField(j).getData());
						addCharatersLikeLength(fieldsLine, SPACE, spaceLength);
					} else if(column.getAlign() == Column.RIGHT_ALIGN) {
						addCharatersLikeLength(fieldsLine, SPACE, spaceLength);
						fieldsLine.append(fields.getField(j).getData());
					} else if(column.getAlign() == Column.CENTER_ALIGN) {
						int half = spaceLength / 2;
						addCharatersLikeLength(fieldsLine, SPACE, half);
						fieldsLine.append(fields.getField(j).getData());
						addCharatersLikeLength(fieldsLine, SPACE, half + (spaceLength % 2));
					}
					addFreeSpace(fieldsLine);
					fieldsLine.append(COLUMN_SEPARATOR);
				}

				Logger.logln(fieldsLine.toString());
			}
			Logger.logln(lineForRowSeparation.toString());
		}
		if(getAddendum() != null) {
			Logger.logln(getAddendum());
		}
		if(showLayoutLine) {
			Logger.logln(layoutLine.toString());
		}
	}

	private void addFreeSpace(StringBuffer buffer) {
		for(int i = 0; i < FREE_SPACE_SIZE; i++)
			buffer.append(SPACE);
	}

	private void addCharatersLikeLength(StringBuffer buffer, String character, int length) {
		for(int i = 0; i < length; i++)
			buffer.append(character);
	}

}
