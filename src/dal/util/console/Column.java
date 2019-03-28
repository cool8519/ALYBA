package dal.util.console;

public class Column {

	public static final int LEFT_ALIGN = -1;
	public static final int RIGHT_ALIGN = 1;
	public static final int CENTER_ALIGN = 0;

	private String name;
	private String unit;
	private String description;
	private int maxLengthOfField;
	private int align = LEFT_ALIGN;

	public Column(String name) {
		this(name, null);
	}

	public Column(String name, String unit) {
		this(name, unit, null);
	}

	public Column(String name, String unit, String description) {
		this.name = name;
		this.unit = unit;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLength() {
		int length = name.length();
		if(unit != null)
			length += unit.length() + 2;
		return length;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMaxLengthOfField() {
		return maxLengthOfField;
	}

	public void setMaxLengthOfField(int maxLengthOfField) {
		this.maxLengthOfField = maxLengthOfField;
	}

	public String toString() {
		if(unit != null)
			return (name + "(" + unit + ")");
		else
			return name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public int getAlign() {
		return align;
	}

	public void setAlign(int align) {
		this.align = align;
	}

}
