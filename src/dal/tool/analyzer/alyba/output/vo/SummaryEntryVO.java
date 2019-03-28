package dal.tool.analyzer.alyba.output.vo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SummaryEntryVO extends EntryVO {

	private static final long serialVersionUID = 1L;

	@Id
	private String title = null;
	
	private String version = null;
	private Date parsed_time = null;
	private Date created_time = null;
	private Date first_request_time = null;
	private Date last_request_time = null;
	private int total_request_count = 0;
	private int total_error_count = 0;
	private int filtered_request_count = 0;
	private int filtered_error_count = 0;
	private int filtered_uri_count = 0;
	private int filtered_ip_count = 0;
	private int filtered_method_count = 0;
	private int filtered_ext_count = 0;
	private int filtered_code_count = 0;
	private Date peak_daily_time = null;
	private Date peak_hourly_time = null;
	private Date peak_minutely_time = null;	
	private Date peak_secondly_time = null;	
	private int peak_daily_count = 0;
	private int peak_hourly_count = 0;
	private int peak_minutely_count = 0;
	private int peak_secondly_count = 0;
	private int bad_elapsed_count = 0;
	private int bad_byte_count = 0;
	private int bad_code_count = 0;
	private Date filter_from_time = null;
	private Date filter_to_time = null;
	private String filter_include_info = null;
	private String filter_exclude_info = null;

	public SummaryEntryVO() {}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getParsedTime() {
		return parsed_time;
	}

	public void setParsedTime(Date parsed_time) {
		this.parsed_time = parsed_time;
	}

	public Date getCreatedTime() {
		return created_time;
	}

	public void setCreatedTime(Date created_time) {
		this.created_time = created_time;
	}

	public Date getFirstRequestTime() {
		return first_request_time;
	}

	public void setFirstRequestTime(Date first_request_time) {
		this.first_request_time = first_request_time;
	}

	public Date getLastRequestTime() {
		return last_request_time;
	}

	public void setLastRequestTime(Date last_request_time) {
		this.last_request_time = last_request_time;
	}

	public int getTotalRequestCount() {
		return total_request_count;
	}

	public void setTotalRequestCount(int total_request_count) {
		this.total_request_count = total_request_count;
	}

	public int getTotalErrorCount() {
		return total_error_count;
	}

	public void setTotalErrorCount(int total_error_count) {
		this.total_error_count = total_error_count;
	}

	public int getFilteredRequestCount() {
		return filtered_request_count;
	}

	public void setFilteredRequestCount(int filtered_request_count) {
		this.filtered_request_count = filtered_request_count;
	}

	public int getFilteredErrorCount() {
		return filtered_error_count;
	}

	public void setFilteredErrorCount(int filtered_error_count) {
		this.filtered_error_count = filtered_error_count;
	}

	public int getFilteredUriCount() {
		return filtered_uri_count;
	}

	public void setFilteredUriCount(int filtered_uri_count) {
		this.filtered_uri_count = filtered_uri_count;
	}

	public int getFilteredIpCount() {
		return filtered_ip_count;
	}

	public void setFilteredIpCount(int filtered_ip_count) {
		this.filtered_ip_count = filtered_ip_count;
	}

	public int getFilteredMethodCount() {
		return filtered_method_count;
	}

	public void setFilteredMethodCount(int filtered_method_count) {
		this.filtered_method_count = filtered_method_count;
	}

	public int getFilteredExtCount() {
		return filtered_ext_count;
	}

	public void setFilteredExtCount(int filtered_ext_count) {
		this.filtered_ext_count = filtered_ext_count;
	}

	public int getFilteredCodeCount() {
		return filtered_code_count;
	}

	public void setFilteredCodeCount(int filtered_code_count) {
		this.filtered_code_count = filtered_code_count;
	}

	public Date getDailyPeakTime() {
		return peak_daily_time;
	}
	
	public void setDailyPeakTime(Date dt) {
		this.peak_daily_time = dt;
	}

	public Date getHourlyPeakTime() {
		return peak_hourly_time;
	}
	
	public void setHourlyPeakTime(Date dt) {
		this.peak_hourly_time = dt;
	}

	public Date getMinutelyPeakTime() {
		return peak_minutely_time;
	}
	
	public void setMinutelyPeakTime(Date dt) {
		this.peak_minutely_time = dt;
	}

	public Date getSecondlyPeakTime() {
		return peak_secondly_time;
	}
	
	public void setSecondlyPeakTime(Date dt) {
		this.peak_secondly_time = dt;
	}

	public int getDailyPeakCount() {
		return peak_daily_count;
	}

	public void setDailyPeakCount(int count) {
		this.peak_daily_count = count;
	}

	public int getHourlyPeakCount() {
		return peak_hourly_count;
	}

	public void setHourlyPeakCount(int count) {
		this.peak_hourly_count = count;
	}

	public int getMinutelyPeakCount() {
		return peak_minutely_count;
	}

	public void setMinutelyPeakCount(int count) {
		this.peak_minutely_count = count;
	}

	public int getSecondlyPeakCount() {
		return peak_secondly_count;
	}

	public void setSecondlyPeakCount(int count) {
		this.peak_secondly_count = count;
	}

	public int getBadElapsedCount() {
		return bad_elapsed_count;
	}

	public void setBadElapsedCount(int bad_elapsed_count) {
		this.bad_elapsed_count = bad_elapsed_count;
	}

	public int getBadByteCount() {
		return bad_byte_count;
	}

	public void setBadByteCount(int bad_byte_count) {
		this.bad_byte_count = bad_byte_count;
	}

	public int getBadCodeCount() {
		return bad_code_count;
	}

	public void setBadCodeCount(int bad_code_count) {
		this.bad_code_count = bad_code_count;
	}

	public Date getFilterFromTime() {
		return filter_from_time;
	}

	public void setFilterFromTime(Date filter_from_time) {
		this.filter_from_time = filter_from_time;
	}

	public Date getFilterToTime() {
		return filter_to_time;
	}

	public void setFilterToTime(Date filter_to_time) {
		this.filter_to_time = filter_to_time;
	}

	public String getFilterIncludeInfo() {
		return filter_include_info;
	}

	public void setFilterIncludeInfo(String filter_include_info) {
		this.filter_include_info = filter_include_info;
	}

	public String getFilterExcludeInfo() {
		return filter_exclude_info;
	}

	public void setFilterExcludeInfo(String filter_exclude_info) {
		this.filter_exclude_info = filter_exclude_info;
	}

	public String toString() {
		return "SummaryEntryVO [title=" + title + ", parsed_time=" + parsed_time + ", created_time=" + created_time + ", first_request_time=" + first_request_time + ", last_request_time=" + last_request_time + ", total_request_count=" + total_request_count + ", total_error_count=" + total_error_count + ", filtered_request_count=" + filtered_request_count + ", filtered_error_count=" + filtered_error_count + ", filtered_uri_count=" + filtered_uri_count + ", filtered_ip_count=" + filtered_ip_count + ", filtered_method_count=" + filtered_method_count + ", filtered_ext_count=" + filtered_ext_count + ", filtered_code_count=" + filtered_code_count + ", bad_elapsed_count=" + bad_elapsed_count + ", bad_byte_count=" + bad_byte_count + ", bad_code_count=" + bad_code_count + ", filter_from_time=" + filter_from_time + ", filter_to_time=" + filter_to_time + ", filter_include_info=" + filter_include_info + ", filter_exclude_info=" + filter_exclude_info + "]";
	}

}
