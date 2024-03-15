package dal.tool.analyzer.alyba.output.vo;

import javax.persistence.Entity;

@Entity
public class BadTransactionEntryVO extends TransactionEntryVO {

	private static final long serialVersionUID = 1L;

	public static enum Type { TIME, SIZE, CODE };

	protected String type; 
	
	public BadTransactionEntryVO() {
		super();
	}

	public BadTransactionEntryVO(Type type, TransactionEntryVO vo) {
		this.type = type.toString();
		request_date = vo.request_date;
		response_date = vo.response_date;
		request_uri = vo.request_uri;
		request_uri_pattern = vo.request_uri_pattern;
		request_ip = vo.request_ip;
		request_ip_country = vo.request_ip_country;
		response_time = vo.response_time;
		response_byte = vo.response_byte;
		response_code = vo.response_code;
		request_method = vo.request_method;
		request_version = vo.request_version;
		request_ext = vo.request_ext;
	}

}
