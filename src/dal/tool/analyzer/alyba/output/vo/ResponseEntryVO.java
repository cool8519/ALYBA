package dal.tool.analyzer.alyba.output.vo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;

import dal.util.DateUtil;

@Entity
public class ResponseEntryVO extends EntryVO {

	private static final long serialVersionUID = 1L;

	protected Date response_date = null;
	protected Date request_date = null;
	protected String request_uri = null;
	protected String request_ip = null;
	protected long response_time = 0L;
	protected long response_byte = 0L;
	protected String response_code = null;
	protected String request_method = null;
	protected String request_version = null;
	protected String request_ext = null;

	public ResponseEntryVO() {
	}

	public ResponseEntryVO(Date dt, String request_uri, String ip, long rtime, long rbyte, String code, String method, String version, String ext) {
		this.response_date = dt;
		this.request_uri = request_uri;
		this.request_ip = ip;
		this.response_time = rtime;
		this.response_byte = rbyte;
		this.response_code = code;
		this.request_method = method;
		this.request_version = version;
		this.request_ext = ext;
		setRequestDate();
	}
	
	public String getRequestURI() {
		return request_uri;
	}

	public void setRequestURI(String request_uri) {
		this.request_uri = request_uri;
	}

	public long getResponseTime() {
		return response_time;
	}

	public void setResponseTime(long response_time) {
		this.response_time = response_time;
		setRequestDate();
	}

	public long getResponseBytes() {
		return response_byte;
	}

	public void setResponseBytes(long response_byte) {
		this.response_byte = response_byte;
	}

	public Date getResponseDate() {
		return response_date;
	}

	public void setResponseDate(Date response_date) {
		this.response_date = response_date;
		setRequestDate();
	}

	public void setRequestDate() {
		if(response_time == -1L) {
			request_date = null;
		} else if(response_time == 0L) {
			request_date = response_date;
		} else {
			request_date = DateUtil.addDateUnit(response_date, Calendar.SECOND, -(int)(response_time/1000)); 
		}
	}
	
	public Date getRequestDate() {
		return this.request_date;
	}
	
	public String getResponseCode() {
		return response_code;
	}

	public void setResponseCode(String response_code) {
		this.response_code = response_code;
	}

	public String getRequestIP() {
		return request_ip;
	}

	public void setRequestIP(String request_ip) {
		this.request_ip = request_ip;
	}

	public String getRequestMethod() {
		return request_method;
	}

	public void setRequestMethod(String request_method) {
		this.request_method = request_method;
	}

	public String getRequestVersion() {
		return request_version;
	}

	public void setRequestVersion(String request_version) {
		this.request_version = request_version;
	}

	public String getRequestExt() {
		return request_ext;
	}

	public void setRequestExt(String request_ext) {
		this.request_ext = request_ext;
	}
	
	public String toResponseTimeString(SimpleDateFormat dateFormat, DecimalFormat numberFormat) {
		if(response_date == null) {
			return "";
		}
		String dt_str = (dateFormat != null) ? dateFormat.format(response_date) : Long.toString(response_date.getTime());
		String data = (numberFormat != null) ? numberFormat.format(response_time) : Long.toString(response_time);
		return dt_str + "(" + data + ")";
	}

	public String toResponseByteString(SimpleDateFormat dateFormat, DecimalFormat numberFormat) {
		if(response_date == null) {
			return "";
		}
		String dt_str = (dateFormat != null) ? dateFormat.format(response_date) : Long.toString(response_date.getTime());
		String data = (numberFormat != null) ? numberFormat.format(response_byte) : Long.toString(response_byte);
		return dt_str + "(" + data + ")";
	}

	public String toErrorString(SimpleDateFormat dateFormat) {
		if(response_date == null) {
			return "";
		}
		String dt_str = (dateFormat != null) ? dateFormat.format(response_date) : Long.toString(response_date.getTime());
		return dt_str + "(" + response_code + ")";
	}
	
	public ResponseEntryVO copy() {
		ResponseEntryVO vo = new ResponseEntryVO();
		vo.response_date = response_date == null ? null : new Date(response_date.getTime());
		vo.request_date = request_date == null ? null : new Date(request_date.getTime());
		vo.request_uri = request_uri == null ? null : new String(request_uri);
		vo.request_ip = request_ip == null ? null : new String(request_ip);
		vo.response_time = response_time;
		vo.response_byte = response_byte;
		vo.response_code = response_code == null ? null : new String(response_code);
		vo.request_method = request_method == null ? null : new String(request_method);
		vo.request_version = request_version == null ? null : new String(request_version);
		vo.request_ext = request_ext == null ? null : new String(request_ext);
		return vo;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName()).append("(").append(hashCode()).append(")");
		buffer.append("[");
		buffer.append("request_date=").append(request_date).append(", ");
		buffer.append("response_date=").append(response_date).append(", ");
		buffer.append("request_ip=").append(request_ip).append(", ");
		buffer.append("request_uri=").append(request_uri).append(", ");
		buffer.append("request_ext=").append(request_ext).append(", ");
		buffer.append("request_method=").append(request_method).append(", ");
		buffer.append("request_version=").append(request_version).append(", ");
		buffer.append("response_code=").append(response_code).append(", ");
		buffer.append("response_byte=").append(response_byte).append(", ");
		buffer.append("response_time=").append(response_time);
		buffer.append("]");
		return buffer.toString();
	}
	
	public String toPrettyString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{").append("\n");
		buffer.append("  request : {").append("\n");
		buffer.append("    date : ").append(request_date).append("\n");
		buffer.append("    ip : ").append(request_ip).append("\n");
		buffer.append("    uri : ").append(request_uri).append("\n");
		buffer.append("    ext : ").append(request_ext).append("\n");
		buffer.append("    method : ").append(request_method).append("\n");
		buffer.append("    version : ").append(request_version).append("\n");
		buffer.append("  },").append("\n");
		buffer.append("  response : {").append("\n");
		buffer.append("    code : ").append(response_code).append("\n");
		buffer.append("    byte : ").append(response_byte).append("\n");
		buffer.append("    time : ").append(response_time).append("\n");
		buffer.append("  }").append("\n");
		buffer.append("}");
		return buffer.toString();
	}

}
