package dal.util.console;

public class Field {

	private String data;

	public Field(int data) {
		this(Integer.toString(data));
	}

	public Field(boolean waiting) {
		this(Boolean.toString(waiting));
	}

	public Field(long data) {
		this(Long.toString(data));
	}

	public Field(double data) {
		this(Double.toString(data));
	}

	public Field(String data) {
		if(data == null)
			data = new String("");
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getLength() {
		return data.length();
	}

	public String toString() {
		return data;
	}

}
