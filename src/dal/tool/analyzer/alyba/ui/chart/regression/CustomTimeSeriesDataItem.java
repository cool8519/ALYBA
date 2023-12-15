package dal.tool.analyzer.alyba.ui.chart.regression;

import org.jfree.data.time.TimeSeriesDataItem;

public class CustomTimeSeriesDataItem extends TimeSeriesDataItem {

	private static final long serialVersionUID = 1L;
	
	protected Number extraValue;

	public CustomTimeSeriesDataItem(TimeSeriesDataItem tsDataItem) {
		super(tsDataItem.getPeriod(), tsDataItem.getValue());
	}
	
	public CustomTimeSeriesDataItem(TimeSeriesDataItem tsDataItem, Number extraValue) {
		super(tsDataItem.getPeriod(), tsDataItem.getValue());
		this.extraValue = extraValue;
	}

	public CustomTimeSeriesDataItem(TimeSeriesDataItem tsDataItem, double extraValue) {
		super(tsDataItem.getPeriod(), tsDataItem.getValue());
		this.extraValue = new Double(extraValue);
	}
	
	public Number getExtraValue() {
		return extraValue;
	}
	
	public void setExtraValue(Number extraValue) {
		this.extraValue = extraValue;
	}
	
	public boolean equals(TimeSeriesDataItem tsDataItem) {
		return super.equals(tsDataItem);
	}
	
	public boolean equalsExtraValue(Number extraValue) {
		if(this.extraValue == extraValue)
			return true;
		if(this.extraValue != null && extraValue !=null) {
			return this.extraValue.equals(extraValue);
		}
		return false; 
	}

}
