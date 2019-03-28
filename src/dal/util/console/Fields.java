package dal.util.console;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Fields {

	private final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	private final ArrayList<Field> fields;
	private final SimpleDateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

	public Fields() {
		this(0);
	}

	public Fields(int size) {
		fields = new ArrayList<Field>(size);
	}

	public void addField(Date data) {
		if(data == null) {
			fields.add(new Field(""));
		} else {
			fields.add(new Field(format.format(data)));
		}
	}

	public void addField(Date data, String formatStr) {
		if(formatStr == null || formatStr.equals(DEFAULT_DATE_FORMAT)) {
			addField(data);
		} else {
			fields.add(new Field((new SimpleDateFormat(formatStr)).format(data)));
		}
	}

	public void addField(String data) {
		if(data == null) {
			fields.add(new Field(""));
		} else {
			fields.add(new Field(data));
		}
	}

	public void addField(int data) {
		fields.add(new Field(data));
	}

	public void addField(boolean data) {
		fields.add(new Field(data));
	}

	public void addField(long data) {
		fields.add(new Field(data));
	}

	public void addField(double data) {
		fields.add(new Field(data));
	}

	public void addField(Field field) {
		fields.add(field);
	}

	public Field getField(int index) {
		return (Field)fields.get(index);
	}

	public int getLength() {
		return fields.size();
	}

}
