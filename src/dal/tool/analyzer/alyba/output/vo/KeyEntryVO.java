package dal.tool.analyzer.alyba.output.vo;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class KeyEntryVO extends EntryVO {

	public static enum Type { URI, IP, METHOD, VERSION, EXT, CODE };
	private static final long serialVersionUID = 1L;

	private static DecimalFormat ratioFormat = new DecimalFormat("##0.000");

	@Id
	private String key;
	@Id
	private String type;

	private int req_total = 0;
	private int filter_req_total = 0;
	private int req_count = 0;
	private float req_ratio = 0F;
	private float filter_req_ratio = 0F;
	private double avg_response_time = 0D;
	@OneToOne(cascade=CascadeType.ALL)
	private ResponseEntryVO max_response_time = null;
	private double avg_response_byte = 0D;
	@OneToOne(cascade=CascadeType.ALL)
	private ResponseEntryVO max_response_byte = null;
	@OneToOne(cascade=CascadeType.ALL)
	private ResponseEntryVO last_error = null;
	private int err_count = 0;
	private float filter_err_ratio = 0F;
	private float entry_err_ratio = 0F;
	private Set<String> request_ip_list = new HashSet<String>();

	public KeyEntryVO(String type, String key) {
		this.type = type;
		this.key = key;
	}

	public KeyEntryVO(Type type, String key) {
		this.type = type.toString();
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void addData() {
		req_count++;
	}

	public void addData(ResponseEntryVO vo) {
		req_count++;
		if(vo.getRequestIP() != null) {
			if(request_ip_list.contains(vo.getRequestIP()) == false) {
				request_ip_list.add(vo.getRequestIP());
			}
		}
		avg_response_time = ((avg_response_time * (req_count - 1)) + vo.getResponseTime()) / req_count;
		if(max_response_time == null || vo.getResponseTime() > max_response_time.getResponseTime()) {
			max_response_time = vo;
		}
		avg_response_byte = ((avg_response_byte * (req_count - 1)) + vo.getResponseBytes()) / req_count;
		if(max_response_byte == null || vo.getResponseBytes() > max_response_byte.getResponseBytes()) {
			max_response_byte = vo;
		}
		if(vo.getResponseCode() != null && (vo.getResponseCode().startsWith("4") || vo.getResponseCode().startsWith("5"))) {
			if(last_error == null || vo.getResponseDate().compareTo(last_error.getResponseDate()) > 0) {
				last_error = vo;
			}
			err_count++;
		}
		entry_err_ratio = ((float)err_count / req_count) * 100;
	}

	public void setTotal(int total) {
		req_total = total;
		req_ratio = ((float)req_count / total) * 100;
	}

	public void setFilteredTotal(int total) {
		filter_req_total = total;
		filter_req_ratio = ((float)req_count / total) * 100;
		filter_err_ratio = ((float)err_count / total) * 100;
	}

	public int getTotal() {
		return req_total;
	}

	public int getFilteredTotal() {
		return filter_req_total;
	}

	public int getRequestCount() {
		return req_count;
	}

	public String getTotalRequestRatio() {
		return ratioFormat.format(req_ratio);
	}

	public String getFilterdRequestRatio() {
		return ratioFormat.format(filter_req_ratio);
	}

	public double getAverageResponseTime() {
		return avg_response_time;
	}

	public ResponseEntryVO getMaxResponseTime() {
		return (max_response_time == null) ? new ResponseEntryVO() : max_response_time;
	}

	public double getAverageResponseBytes() {
		return avg_response_byte;
	}

	public ResponseEntryVO getMaxResponseBytes() {
		return (max_response_byte == null) ? new ResponseEntryVO() : max_response_byte;
	}

	public int getErrorCount() {
		return err_count;
	}

	public String getFilterErrorRatio() {
		return ratioFormat.format(filter_err_ratio);
	}

	public String getEntryErrorRatio() {
		return ratioFormat.format(entry_err_ratio);
	}

	public ResponseEntryVO getLastError() {
		return (last_error == null) ? new ResponseEntryVO() : last_error;
	}

	public void setRequestCount(int count) {
		req_count = count;
	}

	public void setAverageResponseTime(double response) {
		avg_response_time = response;
	}

	public void setMaxResponseTime(ResponseEntryVO vo) {
		max_response_time = vo;
	}

	public void setAverageResponseBytes(double response) {
		avg_response_byte = response;
	}

	public void setMaxResponseBytes(ResponseEntryVO vo) {
		max_response_byte = vo;
	}

	public void setErrorCount(int count) {
		err_count = count;
		entry_err_ratio = (req_count < 1) ? 0F : (((float)err_count / req_count) * 100);
	}

	public void setLastError(ResponseEntryVO vo) {
		last_error = vo;
	}

	public Set<String> getRequestIPList() {
		return request_ip_list;
	}

	public void addRequestIPList(Set<String> l) {
		request_ip_list.addAll(l);
	}

	public int getRequestIPCount() {
		return request_ip_list.size();
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + hashCode() + ")" + " [key=" + key + ", req_total=" + req_total + ", filter_req_total=" + filter_req_total + ", req_count=" + req_count + ", req_ratio=" + req_ratio + ", filter_req_ratio=" + filter_req_ratio + ", avg_response_time=" + avg_response_time + ", max_response_time=" + max_response_time + ", avg_response_byte=" + avg_response_byte + ", max_response_byte=" + max_response_byte + ", err_count=" + err_count + ", filter_err_ratio=" + filter_err_ratio + ", entry_err_ratio=" + entry_err_ratio + ", last_error=" + last_error + ", request_ip_list=" + request_ip_list + "]";
	}

	public KeyEntryVO createEntryVO() {
		return new KeyEntryVO(type, key);
	}
	
	public KeyEntryVO merge(KeyEntryVO subVO) {
		KeyEntryVO vo = createEntryVO();
		vo.setRequestCount(getRequestCount() + subVO.getRequestCount());
		vo.setErrorCount(getErrorCount() + subVO.getErrorCount());
		if(getLastError().getResponseDate() == null) {
			vo.setLastError(subVO.getLastError());
		} else if(subVO.getLastError().getResponseDate() == null) {
			vo.setLastError(getLastError());
		} else {
			vo.setLastError((getLastError().getResponseDate().compareTo(subVO.getLastError().getResponseDate()) > 0) ? getLastError() : subVO.getLastError());
		}
		if(getMaxResponseTime().getResponseDate() == null) {
			vo.setMaxResponseTime(subVO.getMaxResponseTime());
		} else if(subVO.getMaxResponseTime().getResponseDate() == null) {
			vo.setMaxResponseTime(getMaxResponseTime());
		} else {
			vo.setMaxResponseTime((getMaxResponseTime().getResponseTime() > subVO.getMaxResponseTime().getResponseTime()) ? getMaxResponseTime() : subVO.getMaxResponseTime());
		}
		if(getMaxResponseBytes().getResponseDate() == null) {
			vo.setMaxResponseBytes(subVO.getMaxResponseBytes());
		} else if(subVO.getMaxResponseBytes().getResponseDate() == null) {
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
		return vo;
	}

}
