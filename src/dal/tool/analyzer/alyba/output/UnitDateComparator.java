package dal.tool.analyzer.alyba.output;

import java.util.Comparator;
import java.util.Date;

import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;

public class UnitDateComparator implements Comparator<EntryVO> {

	public boolean m_sortASC = true;

	public UnitDateComparator() {
	}

	public UnitDateComparator(boolean asc) {
		m_sortASC = asc;
	}

	public int compare(EntryVO o1, EntryVO o2) {
		if(o1 == null || o2 == null) {
			return 0;
		}
		Date dt1;
		Date dt2;
		if((o1 instanceof DateEntryVO) && (o2 instanceof DateEntryVO)) {
			DateEntryVO vo1 = (DateEntryVO)((m_sortASC) ? o2 : o1);
			DateEntryVO vo2 = (DateEntryVO)((m_sortASC) ? o1 : o2);
			dt1 = vo1.getUnitDate();
			dt2 = vo2.getUnitDate();
		} else if((o1 instanceof ResourceUsageEntryVO) && (o2 instanceof ResourceUsageEntryVO)) {
			ResourceUsageEntryVO vo1 = (ResourceUsageEntryVO)((m_sortASC) ? o2 : o1);
			ResourceUsageEntryVO vo2 = (ResourceUsageEntryVO)((m_sortASC) ? o1 : o2);
			dt1 = vo1.getUnitDate();
			dt2 = vo2.getUnitDate();
		} else {
			return 0;
		}
		if(dt1 == null || dt2 == null) {
			return 0;
		} else {
			return dt2.compareTo(dt1);
		}
	}

}