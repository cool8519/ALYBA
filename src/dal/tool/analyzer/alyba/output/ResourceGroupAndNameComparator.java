package dal.tool.analyzer.alyba.output;

import java.util.Comparator;
import java.util.Date;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;

public class ResourceGroupAndNameComparator implements Comparator<EntryVO> {

	public static enum SortBy { TIME, GROUP_TIME, GROUP_NAME_TIME }

	public SortBy m_sortField = SortBy.GROUP_NAME_TIME;
	public boolean m_sortASC = true;

	public ResourceGroupAndNameComparator() {
	}

	public ResourceGroupAndNameComparator(SortBy sort) {
		m_sortField = sort;
	}

	public ResourceGroupAndNameComparator(SortBy sort, boolean asc) {
		m_sortField = sort;
		m_sortASC = asc;
	}

	public int compare(EntryVO o1, EntryVO o2) {
		if(o1 == null || o2 == null || !(o1 instanceof ResourceUsageEntryVO) || !(o2 instanceof ResourceUsageEntryVO)) {
			return 0;
		}
		ResourceUsageEntryVO vo1 = (ResourceUsageEntryVO)((m_sortASC) ? o2 : o1);
		ResourceUsageEntryVO vo2 = (ResourceUsageEntryVO)((m_sortASC) ? o1 : o2);
		int ret = 0;
		if(m_sortField == SortBy.TIME) {
			Date dt1 = vo1.getUnitDate();
			Date dt2 = vo2.getUnitDate();
			ret = dt2.compareTo(dt1);
			return ret;
		} else if(m_sortField == SortBy.GROUP_TIME) {
			String g1 = vo1.getServerGroup();
			String g2 = vo2.getServerGroup();
			ret = g2.compareTo(g1);
			if(ret != 0) {
				return ret;
			}
			Date dt1 = vo1.getUnitDate();
			Date dt2 = vo2.getUnitDate();
			ret = dt2.compareTo(dt1);
			return ret;
		} else if(m_sortField == SortBy.GROUP_NAME_TIME) {
			String g1 = vo1.getServerGroup();
			String g2 = vo2.getServerGroup();
			ret = g2.compareTo(g1);
			if(ret != 0) {
				return ret;
			}
	        String n1 = vo1.getServerName();
	        String n2 = vo2.getServerName();
	        ret = n2.compareTo(n1);
			if(ret != 0) {
				return ret;
			}
			Date dt1 = vo1.getUnitDate();
			Date dt2 = vo2.getUnitDate();
			ret = dt2.compareTo(dt1);
			return ret;
        } else {
        	return 0;
        }
	}

}