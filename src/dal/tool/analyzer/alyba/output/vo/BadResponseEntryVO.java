package dal.tool.analyzer.alyba.output.vo;

import javax.persistence.Entity;

@Entity
public class BadResponseEntryVO extends ResponseEntryVO {

	public static enum Type { TIME, SIZE, CODE };
	private static final long serialVersionUID = 1L;

	protected String type; 
	
	public BadResponseEntryVO() {
		super();
	}

	public BadResponseEntryVO(Type type, ResponseEntryVO vo) {
		this.type = type.toString();
		response_date = vo.response_date;
		request_date = vo.request_date;
		request_uri = vo.request_uri;
		request_ip = vo.request_ip;
		response_time = vo.response_time;
		response_byte = vo.response_byte;
		response_code = vo.response_code;
		request_method = vo.request_method;
		request_version = vo.request_version;
		request_ext = vo.request_ext;
	}

}
