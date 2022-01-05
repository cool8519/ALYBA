package dal.tool.analyzer.alyba.output.vo;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import dal.tool.analyzer.alyba.setting.LogAnalyzerSetting;

@Entity
public abstract class DateEntryVO extends EntryVO {

	private static final long serialVersionUID = 1L;

	private static final DecimalFormat DF_RATIO = new DecimalFormat("##0.000");

	@Id
	private Date unit_date = null;

	private long req_total = 0;
	private long filter_req_total = 0;
	private int req_count = 0;
	private float req_ratio = 0F;
	private float filter_req_ratio = 0F;
	private double avg_response_time = 0D;
	@OneToOne(cascade=CascadeType.ALL)
	private TransactionEntryVO max_response_time = null;
	private double avg_response_byte = 0D;
	@OneToOne(cascade=CascadeType.ALL)
	private TransactionEntryVO max_response_byte = null;
	@OneToOne(cascade=CascadeType.ALL)
	private TransactionEntryVO last_error = null;
	private int err_count = 0;
	private float filter_err_ratio = 0F;
	private float entry_err_ratio = 0F;
	private int request_ip_count = 0; 
	private Set<String> request_ip_list = new HashSet<String>();

	public DateEntryVO(Date unit_date) {
		setUnitDate(unit_date);
	}

	public Date getUnitDate() {
		return unit_date;
	}

	public void setUnitDate(Date unit_date) {
		this.unit_date = unit_date;
	}

	public void setData(int count) {
		this.req_count = count;
	}
	
	public void addData() {
		req_count++;
	}

	public void addData(TransactionEntryVO vo, LogAnalyzerSetting setting) {
		req_count++;
		if(setting.fieldMapping.isMappedIP()) {
			if(vo.getRequestIP() != null) {
				if(request_ip_list.contains(vo.getRequestIP()) == false) {
					request_ip_list.add(vo.getRequestIP());
					request_ip_count = request_ip_list.size();
				}
			}
		}
		if(setting.fieldMapping.isMappedElapsed()) {
			avg_response_time = ((avg_response_time * (req_count - 1)) + vo.getResponseTime()) / req_count;
			if(max_response_time == null || vo.getResponseTime() > max_response_time.getResponseTime()) {
				max_response_time = vo;
			}
		} else {
			avg_response_time = -1D;
		}
		if(setting.fieldMapping.isMappedBytes()) {
			avg_response_byte = ((avg_response_byte * (req_count - 1)) + vo.getResponseBytes()) / req_count;
			if(max_response_byte == null || vo.getResponseBytes() > max_response_byte.getResponseBytes()) {
				max_response_byte = vo;
			}
		} else {
			avg_response_byte = -1D;
		}
		if(setting.fieldMapping.isMappedCode()) {
			if(vo.getResponseCode() != null && (vo.getResponseCode().startsWith("4") || vo.getResponseCode().startsWith("5"))) {
				if(last_error == null || vo.getDate().compareTo(last_error.getDate()) > 0) {
					last_error = vo;
				}
				err_count++;
			}
			setErrorRatio();
		}
	}
	
	public void setErrorRatio() {
		entry_err_ratio = ((float)err_count / req_count) * 100;
	}

	public void setTotal(long total) {
		req_total = total;
		req_ratio = ((float)req_count / total) * 100;
	}

	public void setFilteredTotal(long total) {
		filter_req_total = total;
		filter_req_ratio = ((float)req_count / total) * 100;
		filter_err_ratio = ((float)err_count / total) * 100;
	}

	public long getTotal() {
		return req_total;
	}

	public long getFilteredTotal() {
		return filter_req_total;
	}

	public int getRequestCount() {
		return req_count;
	}

	public String getTotalRequestRatio() {
		return DF_RATIO.format(req_ratio);
	}

	public String getFilterdRequestRatio() {
		return DF_RATIO.format(filter_req_ratio);
	}

	public double getAverageResponseTime() {
		return avg_response_time;
	}

	public TransactionEntryVO getMaxResponseTime() {
		return (max_response_time == null) ? new TransactionEntryVO() : max_response_time;
	}

	public double getAverageResponseBytes() {
		return avg_response_byte;
	}

	public TransactionEntryVO getMaxResponseBytes() {
		return (max_response_byte == null) ? new TransactionEntryVO() : max_response_byte;
	}

	public int getErrorCount() {
		return err_count;
	}

	public String getFilterErrorRatio() {
		return DF_RATIO.format(filter_err_ratio);
	}

	public String getEntryErrorRatio() {
		return DF_RATIO.format(entry_err_ratio);
	}

	public TransactionEntryVO getLastError() {
		return (last_error == null) ? new TransactionEntryVO() : last_error;
	}

	public void setRequestCount(int count) {
		req_count = count;
	}

	public void setAverageResponseTime(double response) {
		avg_response_time = response;
	}

	public void setMaxResponseTime(TransactionEntryVO vo) {
		max_response_time = vo;
	}

	public void setAverageResponseBytes(double response) {
		avg_response_byte = response;
	}

	public void setMaxResponseBytes(TransactionEntryVO vo) {
		max_response_byte = vo;
	}

	public void setErrorCount(int count) {
		err_count = count;
		entry_err_ratio = (req_count < 1) ? 0F : (((float)err_count / req_count) * 100);
	}

	public void setLastError(TransactionEntryVO vo) {
		last_error = vo;
	}

	public Set<String> getRequestIPList() {
		return request_ip_list;
	}

	public void addRequestIPList(Set<String> l) {
		request_ip_list.addAll(l);
		request_ip_count = request_ip_list.size();
	}

	public int getRequestIPCount() {
		return request_ip_count;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" +hashCode() + ")" + " [unit_date=" + unit_date + ", req_total=" + req_total + ", filter_req_total=" + filter_req_total + ", req_count=" + req_count + ", req_ratio=" + req_ratio + ", filter_req_ratio=" + filter_req_ratio + ", avg_response_time=" + avg_response_time + ", max_response_time=" + max_response_time + ", avg_response_byte=" + avg_response_byte + ", max_response_byte=" + max_response_byte + ", err_count=" + err_count + ", filter_err_ratio=" + filter_err_ratio + ", entry_err_ratio=" + entry_err_ratio + ", last_error=" + last_error + ", request_ip_count=" + request_ip_count + ", request_ip_list=" + request_ip_list + "]";
	}

	public abstract <E extends DateEntryVO> E createEntryVO();
	
	public <E extends DateEntryVO> E merge(E subVO) {
		E vo = createEntryVO();
		vo.setRequestCount(getRequestCount() + subVO.getRequestCount());
		vo.setErrorCount(getErrorCount() + subVO.getErrorCount());
		if(getLastError().getDate() == null) {
			vo.setLastError(subVO.getLastError());
		} else if(subVO.getLastError().getDate() == null) {
			vo.setLastError(getLastError());
		} else {
			vo.setLastError((getLastError().getDate().compareTo(subVO.getLastError().getDate()) > 0) ? getLastError() : subVO.getLastError());
		}
		if(getMaxResponseTime().getDate() == null) {
			vo.setMaxResponseTime(subVO.getMaxResponseTime());
		} else if(subVO.getMaxResponseTime().getDate() == null) {
			vo.setMaxResponseTime(getMaxResponseTime());
		} else {
			vo.setMaxResponseTime((getMaxResponseTime().getResponseTime() > subVO.getMaxResponseTime().getResponseTime()) ? getMaxResponseTime() : subVO.getMaxResponseTime());
		}
		if(getMaxResponseBytes().getDate() == null) {
			vo.setMaxResponseBytes(subVO.getMaxResponseBytes());
		} else if(subVO.getMaxResponseBytes().getDate() == null) {
			vo.setMaxResponseBytes(getMaxResponseBytes());
		} else {
			vo.setMaxResponseBytes((getMaxResponseBytes().getResponseBytes() > subVO.getMaxResponseBytes().getResponseBytes()) ? getMaxResponseBytes() : subVO.getMaxResponseBytes());
		}
		if(vo.getRequestCount() > 0) {
			vo.setAverageResponseTime((getRequestCount() * getAverageResponseTime() + subVO.getRequestCount() * subVO.getAverageResponseTime()) / vo.getRequestCount());
			vo.setAverageResponseBytes((getRequestCount() * getAverageResponseBytes() + subVO.getRequestCount() * subVO.getAverageResponseBytes()) / vo.getRequestCount());
		} else {
			vo.setAverageResponseTime(0D);
			vo.setAverageResponseBytes(0D);
		}
		if(getRequestIPList() != null) {
			vo.addRequestIPList(getRequestIPList());
		}
		if(subVO.getRequestIPList() != null) {
			vo.addRequestIPList(subVO.getRequestIPList());
		}
		vo.setErrorRatio();
		vo.setTotal(getTotal());
		vo.setFilteredTotal(getFilteredTotal());
		return vo;
	}

}
