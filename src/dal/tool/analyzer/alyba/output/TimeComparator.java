package dal.tool.analyzer.alyba.output;

import java.util.Comparator;
import java.util.Date;

import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;

public class TimeComparator implements Comparator<EntryVO> {

	public boolean m_sortASC = true;

	public TimeComparator() {
	}

	public TimeComparator(boolean asc) {
		m_sortASC = asc;
	}

	public int compare(EntryVO o1, EntryVO o2) {
		if(o1 == null || o2 == null || !(o1 instanceof DateEntryVO) || !(o2 instanceof DateEntryVO)) {
			return 0;
		}
		DateEntryVO vo1 = (DateEntryVO)((m_sortASC) ? o2 : o1);
		DateEntryVO vo2 = (DateEntryVO)((m_sortASC) ? o1 : o2);
		Date dt1 = vo1.getUnitDate();
		Date dt2 = vo2.getUnitDate();
		if(dt1 == null || dt2 == null) {
			return 0;
		} else {
			return dt2.compareTo(dt1);
		}
	}

}