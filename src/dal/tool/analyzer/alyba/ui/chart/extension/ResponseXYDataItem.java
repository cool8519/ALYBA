package dal.tool.analyzer.alyba.ui.chart.extension;

import org.jfree.data.xy.XYDataItem;

import dal.tool.analyzer.alyba.output.vo.ResponseEntryVO;

public class ResponseXYDataItem extends XYDataItem {

	private static final long serialVersionUID = -1L;

	private ResponseEntryVO response;
	
	public ResponseXYDataItem(double x, double y, ResponseEntryVO response) {
		super(x, y);
		this.response = response;
	}

	public ResponseEntryVO getResponse() {
		return response;
	}

	public void setResponse(ResponseEntryVO response) {
		this.response = response;
	}

}
