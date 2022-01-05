package dal.tool.analyzer.alyba.output.vo;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class TPMEntryVO extends DateEntryVO {

	private static final long serialVersionUID = 1L;

	public TPMEntryVO(Date unit_date) {
		super(unit_date);
	}

	public TPMEntryVO(Date unit_date, int count) {
		super(unit_date);		
		setData(count);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends DateEntryVO> E createEntryVO() {
		return (E)(new TPMEntryVO(getUnitDate()));
	}

}
