package dal.tool.analyzer.alyba.output;

import java.util.Comparator;
import java.util.Date;

import dal.tool.analyzer.alyba.output.vo.DateEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.output.vo.TransactionEntryVO;

public class ValueComparator implements Comparator<EntryVO> {

	public String m_sortField = null;
	public boolean m_sortASC = true;

	public ValueComparator(String field, boolean asc) {
		m_sortField = field;
		m_sortASC = asc;
	}

	public ValueComparator(String field) {
		m_sortField = field;
	}

	public String getSortField() {
		return m_sortField;
	}

	public int compare(EntryVO o1, EntryVO o2) {
		if(o1 == null || o2 == null || m_sortField == null) {
			return 0;
		}
		if(o1 instanceof DateEntryVO && o2 instanceof DateEntryVO) {
			DateEntryVO vo1 = (DateEntryVO)((m_sortASC) ? o2 : o1);
			DateEntryVO vo2 = (DateEntryVO)((m_sortASC) ? o1 : o2);
			if(m_sortField.equals("COUNT")) {
				int cnt1 = vo1.getRequestCount();
				int cnt2 = vo2.getRequestCount();
				if(cnt1 > cnt2) {
					return 1;
				} else if(cnt1 < cnt2) {
					return -1;
				} else {
					return 0;
				}
			}
			return 0;
		} else if(o1 instanceof KeyEntryVO && o2 instanceof KeyEntryVO) {
			KeyEntryVO vo1 = (KeyEntryVO)((m_sortASC) ? o2 : o1);
			KeyEntryVO vo2 = (KeyEntryVO)((m_sortASC) ? o1 : o2);
			if(m_sortField.equals("NAME")) {
				return vo2.getKey().compareTo(vo1.getKey());
			} else if(m_sortField.equals("COUNT")) {
				int cnt1 = vo1.getRequestCount();
				int cnt2 = vo2.getRequestCount();
				if(cnt1 > cnt2) {
					return 1;
				} else if(cnt1 < cnt2) {
					return -1;
				} else {
					return 0;
				}
			}
			return 0;
		} else if(o1 instanceof TransactionEntryVO && o2 instanceof TransactionEntryVO) {
			TransactionEntryVO vo1 = (TransactionEntryVO)((m_sortASC) ? o2 : o1);
			TransactionEntryVO vo2 = (TransactionEntryVO)((m_sortASC) ? o1 : o2);
			if(m_sortField.equals("RES_TIME")) {
				double v1 = vo1.getResponseTime();
				double v2 = vo2.getResponseTime();
				if(v1 > v2) {
					return 1;
				} else if(v1 < v2) {
					return -1;
				} else {
					return 0;
				}
			} else if(m_sortField.equals("RES_BYTE")) {
				long v1 = vo1.getResponseBytes();
				long v2 = vo2.getResponseBytes();
				if(v1 > v2) {
					return 1;
				} else if(v1 < v2) {
					return -1;
				} else {
					return 0;
				}
			} else if(m_sortField.equals("DATE")) {
				Date dt1 = vo1.getDate();
				Date dt2 = vo2.getDate();
				if(dt1 == null || dt2 == null) {
					return 0;
				} else {
					return dt2.compareTo(dt1);
				}
			}
			return 0;
		} else {
			return 0;
		}
	}

}