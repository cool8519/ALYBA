package dal.tool.analyzer.alyba.output.vo;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class DailyEntryVO extends DateEntryVO {

	private static final long serialVersionUID = 1L;

	public DailyEntryVO(Date unit_date) {
		super(unit_date);
	}

	@SuppressWarnings("unchecked")
	public <E extends DateEntryVO> E createEntryVO() {
		return (E)(new DailyEntryVO(getUnitDate()));
	}

}
