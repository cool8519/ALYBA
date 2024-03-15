package dal.tool.analyzer.alyba.output.vo;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;

import dal.tool.analyzer.alyba.util.Utility;
import dal.util.DateUtil;
import dal.util.NumberUtil;

@Entity
public class TransactionEntryVO extends EntryVO {

	private static final long serialVersionUID = 1L;

	protected Date request_date = null;
	protected Date response_date = null;
	protected String request_uri = null;
	protected String request_uri_pattern = null;
	protected String request_ip = null;
	protected String request_ip_country = null;
	protected long response_time = 0L;
	protected long response_byte = 0L;
	protected String response_code = null;
	protected String request_method = null;
	protected String request_version = null;
	protected String request_ext = null;
	protected boolean response_is_error = false;

	public TransactionEntryVO() {
	}

	public TransactionEntryVO(Date dt, String request_uri, String request_uri_pattern, String ip, long rtime, long rbyte, String code, String method, String version, String ext, boolean is_date_response, boolean is_error) {
		this.request_date = is_date_response ? null : dt;
		this.response_date = is_date_response ? dt : null;
		this.request_uri = request_uri;
		this.request_uri_pattern = request_uri_pattern;
		this.request_ip = ip;
		this.response_time = rtime;
		this.response_byte = rbyte;
		this.response_code = code;
		this.request_method = method;
		this.request_version = version;
		this.request_ext = ext;
		this.response_is_error = is_error;
		resolveRequestIPCountry();
		calculateRequestOrResponseDate(is_date_response);
	}
	
	public String getRequestURI() {
		return request_uri;
	}
	
	public void setRequestURI(String request_uri) {
		this.request_uri = request_uri;
	}

	public String getRequestURI_Pattern() {
		return request_uri_pattern;
	}

	public void setRequestURI_Pattern(String request_uri_pattern) {
		this.request_uri_pattern = request_uri_pattern;
	}

	public long getResponseTime() {
		return response_time;
	}

	public void setResponseTime(long response_time) {
		this.response_time = response_time;
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
	}

	public Date getRequestDate() {
		return this.request_date;
	}

	public void setRequestDate(Date request_date) {
		this.request_date = request_date;
	}

	public Date getDate() {
		return response_date != null ? response_date : request_date;
	}
	
	protected void resolveRequestIPCountry() {
		if(request_ip != null) {
			request_ip_country = Utility.getCountryFromIP(request_ip);
			request_ip_country = request_ip_country==null||"UNKNOWN".equals(request_ip_country) ? "#UNKNOWN#" : request_ip_country;
		}
	}
	
	protected void calculateRequestOrResponseDate(boolean is_response) {
		if(is_response) {
			if(response_time == -1L) {
				request_date = null;
			} else if(response_time == 0L) {
				request_date = response_date;
			} else {
				request_date = DateUtil.addDateUnit(response_date, Calendar.MILLISECOND, -(int)response_time); 
			}			
		} else {
			if(response_time == -1L) {
				response_date = null;
			} else if(response_time == 0L) {
				response_date = request_date;
			} else {
				response_date = DateUtil.addDateUnit(request_date, Calendar.MILLISECOND, (int)response_time); 
			}
		}
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
		resolveRequestIPCountry();
	}
	
	public String getRequestIPCountry() {
		return request_ip_country;
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
	
	public boolean isResponseError() {
		return response_is_error;
	}
	
	public void setResponseError(boolean is_error) {
		this.response_is_error = is_error;
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
	
	public TransactionEntryVO copy() {
		TransactionEntryVO vo = new TransactionEntryVO();
		vo.request_date = request_date == null ? null : new Date(request_date.getTime());
		vo.response_date = response_date == null ? null : new Date(response_date.getTime());
		vo.request_uri = request_uri == null ? null : new String(request_uri);
		vo.request_uri_pattern = request_uri_pattern == null ? null : new String(request_uri_pattern);
		vo.request_ip = request_ip == null ? null : new String(request_ip);
		vo.request_ip_country = request_ip_country == null ? null : new String(request_ip_country);
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
		buffer.append("request_ip_country=").append(request_ip_country).append(", ");
		buffer.append("request_uri=").append(request_uri).append(", ");
		buffer.append("request_uri_pattern=").append(request_uri_pattern).append(", ");
		buffer.append("request_ext=").append(request_ext).append(", ");
		buffer.append("request_method=").append(request_method).append(", ");
		buffer.append("request_version=").append(request_version).append(", ");
		buffer.append("response_code=").append(response_code).append(", ");
		buffer.append("response_byte=").append(response_byte).append(", ");
		buffer.append("response_time=").append(response_time);
		buffer.append("]");
		return buffer.toString();
	}

	public String toJSONString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{").append("\n");
		buffer.append("  request : {").append("\n");
		if(request_date != null) {
			buffer.append("    date : ").append(DateUtil.dateToString(request_date, DateUtil.SDF_DATETIME)).append("\n");
		}
		if(request_ip != null) {
			buffer.append("    ip : ").append(request_ip);
			if(request_ip_country != null && !"UNKNOWN".equals(request_ip_country)) {
				buffer.append("(").append(request_ip_country).append(")");
			}
			buffer.append("\n");
		}
		if(request_uri != null) {
			buffer.append("    uri : ").append(request_uri).append("\n");
		}
		if(request_uri_pattern != null) {
			buffer.append("    uri_pattern : ").append(request_uri_pattern).append("\n");
		}
		if(request_ext != null) {
			buffer.append("    ext : ").append(request_ext).append("\n");
		}
		if(request_method != null) {
			buffer.append("    method : ").append(request_method).append("\n");
		}
		if(request_version != null) {
			buffer.append("    version : ").append(request_version).append("\n");
		}
		buffer.append("  },").append("\n");
		buffer.append("  response : {").append("\n");
		if(response_date != null) {
			buffer.append("    date : ").append(DateUtil.dateToString(response_date, DateUtil.SDF_DATETIME)).append("\n");
		}
		if(response_code != null) {
			buffer.append("    code : ").append(response_code).append("\n");
		}
		if(response_byte != -1L) {
			buffer.append("    byte : ").append(NumberUtil.numberToString(response_byte, NumberUtil.DF_NO_DOT_THOUSAND)).append("\n");
		}
		if(response_time != -1L) {
			buffer.append("    time : ").append(NumberUtil.numberToString(response_time, NumberUtil.DF_NO_DOT_THOUSAND)).append(" ms\n");
		}
		buffer.append("  }").append("\n");
		buffer.append("}");
		return buffer.toString();
	}

	public String toPrettyString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("# Request").append("\n");
		if(request_date != null) {
			buffer.append("    date : ").append(DateUtil.dateToString(request_date, DateUtil.SDF_DATETIME)).append("\n");
		}
		if(request_ip != null) {
			buffer.append("    ip : ").append(request_ip);
			if(request_ip_country != null && !"UNKNOWN".equals(request_ip_country)) {
				buffer.append(" (").append(request_ip_country).append(")");
			}
			buffer.append("\n");
		}
		if(request_uri != null) {
			buffer.append("    uri : ").append(request_uri).append("\n");
		}
		if(request_uri_pattern != null) {
			buffer.append("    uri pattern : ").append(request_uri_pattern).append("\n");
		}
		if(request_ext != null) {
			buffer.append("    ext : ").append(request_ext).append("\n");
		}
		if(request_method != null) {
			buffer.append("    method : ").append(request_method).append("\n");
		}
		if(request_version != null) {
			buffer.append("    version : ").append(request_version).append("\n");
		}
		buffer.append("# Response").append("\n");
		if(response_date != null) {
			buffer.append("    date : ").append(DateUtil.dateToString(response_date, DateUtil.SDF_DATETIME)).append("\n");
		}
		if(response_code != null) {
			buffer.append("    code : ").append(response_code).append("\n");
		}
		if(response_byte != -1L) {
			buffer.append("    byte : ").append(NumberUtil.numberToString(response_byte, NumberUtil.DF_NO_DOT_THOUSAND)).append("\n");
		}
		if(response_time != -1L) {
			buffer.append("    time : ").append(NumberUtil.numberToString(response_time, NumberUtil.DF_NO_DOT_THOUSAND)).append(" ms\n");
		}
		return buffer.toString();
	}

}
