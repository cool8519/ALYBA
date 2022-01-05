package dal.tool.analyzer.alyba.output.vo;

import java.util.Date;

import javax.persistence.Entity;

import dal.tool.analyzer.alyba.ui.chart.regression.RegressionChart.ResourceMergeType;

@Entity
public class RegressionEntryVO extends ResourceUsageEntryVO {

	private static final long serialVersionUID = 1L;

	protected int request_tx_count = 0;
	protected int request_ip_count = 0; 
	protected double avg_response_time = 0D;
	protected int err_count = 0;
	

	public RegressionEntryVO(Date dt) {
		super(dt);
		this.item_count = 1;
	}

	public RegressionEntryVO(Date dt, String name, String group) {
		super(dt, name, group);
		this.item_count = 1;
	}

	public int getRequestTxCount() {
		return request_tx_count;
	}

	public void setRequestTxCount(int request_tx_count) {
		this.request_tx_count = request_tx_count;
	}

	public int getRequestIpCount() {
		return request_ip_count;
	}

	public void setRequestIpCount(int request_ip_count) {
		this.request_ip_count = request_ip_count;
	}

	public double getAverageResponseTimeMS() {
		return avg_response_time;
	}

	public void setAverageResponseTimeMS(double avg_response_time) {
		this.avg_response_time = avg_response_time;
	}

	public int getErrorCount() {
		return err_count;
	}

	public void setErrorCount(int err_count) {
		this.err_count = err_count;
	}

	public RegressionEntryVO merge(RegressionEntryVO subVO, ResourceMergeType resourceMergeType) {
		RegressionEntryVO vo = new RegressionEntryVO(getUnitDate(), (subVO.getServerName().equals(getServerName())?getServerName():null), (subVO.getServerGroup().equals(getServerGroup())?getServerGroup():null));
		vo.setRequestTxCount(request_tx_count);
		vo.setRequestIpCount(request_ip_count);
		vo.setAverageResponseTimeMS(avg_response_time);
		vo.setErrorCount(err_count);
		if(subVO.getCpuUsage() != -1D) {
			if(cpu != -1D) {
				if(resourceMergeType == ResourceMergeType.AVG) {
					vo.setCpuUsage((cpu*item_count + subVO.getCpuUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
				} else {
					vo.setCpuUsage(cpu*item_count + subVO.getCpuUsage()*subVO.getDataCount());
				}
			} else {
				vo.setCpuUsage(subVO.getCpuUsage());
			}
		} else {
			vo.setCpuUsage(cpu);
		}
		if(subVO.getMemoryUsage() != -1D) {
			if(memory != -1D) {
				if(resourceMergeType == ResourceMergeType.AVG) {
					vo.setMemoryUsage((memory*item_count + subVO.getMemoryUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
				} else {
					vo.setMemoryUsage(memory*item_count + subVO.getMemoryUsage()*subVO.getDataCount());
				}
			} else {
				vo.setMemoryUsage(subVO.getMemoryUsage());
			}
		} else {
			vo.setMemoryUsage(memory);
		}
		if(subVO.getDiskUsage() != -1D) {
			if(disk != -1D) {
				if(resourceMergeType == ResourceMergeType.AVG) {
					vo.setDiskUsage((disk*item_count + subVO.getDiskUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
				} else {
					vo.setDiskUsage(disk*item_count + subVO.getDiskUsage()*subVO.getDataCount());
				}
			} else {
				vo.setDiskUsage(subVO.getDiskUsage());
			}
		} else {
			vo.setDiskUsage(disk);
		}
		if(subVO.getNetworkUsage() != -1D) {
			if(network != -1D) {
				if(resourceMergeType == ResourceMergeType.AVG) {
					vo.setNetworkUsage((network*item_count + subVO.getNetworkUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
				} else {
					vo.setNetworkUsage(network*item_count + subVO.getNetworkUsage()*subVO.getDataCount());
				}
			} else {
				vo.setNetworkUsage(subVO.getNetworkUsage());
			}
		} else {
			vo.setNetworkUsage(network);
		}
		vo.setDataCount(item_count+subVO.getDataCount());
		return vo;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName()).append("(").append(hashCode()).append(")");
		buffer.append("[");
		buffer.append("date=").append(unit_date).append(", ");
		buffer.append("name=").append(name).append(", ");
		buffer.append("group=").append(group).append(", ");
		buffer.append("tx=").append(request_tx_count).append(", ");
		buffer.append("ip=").append(request_ip_count).append(", ");
		buffer.append("resp=").append(avg_response_time).append(", ");
		buffer.append("error=").append(err_count).append(", ");		
		buffer.append("cpu=").append(cpu).append(", ");
		buffer.append("memory=").append(memory).append(", ");
		buffer.append("disk=").append(disk).append(", ");
		buffer.append("network=").append(network);
		buffer.append("]");
		return buffer.toString();
	}

}
