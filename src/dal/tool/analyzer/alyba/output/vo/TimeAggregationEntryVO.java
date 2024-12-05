package dal.tool.analyzer.alyba.output.vo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TimeAggregationEntryVO extends DateEntryVO {

	private static final long serialVersionUID = 1L;

	public static enum Type { DAY, HOUR };

	@Id
	protected String type; 

	public TimeAggregationEntryVO(Type type, Date unit_date) {
		super(unit_date);
		this.type = type.toString();
	}

	public TimeAggregationEntryVO(Type type, Date unit_date, int count) {
		super(unit_date);
		this.type = type.toString();
		setData(count);
	}
	
	public void addData(int count) {
		int req_cnt_sum;
		if(getRequestCount() >= 0 && count >= 0) {
			req_cnt_sum = getRequestCount() + count;
		} else if(getRequestCount() >= 0) {
			req_cnt_sum = getRequestCount();
		} else {
			req_cnt_sum = count;
		}
		setData(req_cnt_sum);
	}

	@SuppressWarnings("unchecked")
	public <E extends DateEntryVO> E createEntryVO() {
		return (E)(new TimeAggregationEntryVO(Type.valueOf(type), getUnitDate()));
	}

}
