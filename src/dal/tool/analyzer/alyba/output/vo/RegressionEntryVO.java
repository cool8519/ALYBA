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
	protected ResourceMergeType mergeType = ResourceMergeType.AVG;
	protected boolean failed = false;
	

	public RegressionEntryVO(Date dt) {
		super(dt);
	}

	public RegressionEntryVO(Date dt, String name, String group) {
		super(dt, name, group);
	}

	public RegressionEntryVO(Date dt, String name, String group, ResourceMergeType mergeType) {
		super(dt, name, group);
		this.mergeType = mergeType;
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
	
	public ResourceMergeType getMergeType() {
		return mergeType;
	}
	
	public boolean getFailed() {
		return failed;
	}
	
	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public RegressionEntryVO merge(RegressionEntryVO subVO, ResourceMergeType resourceMergeType) {
		RegressionEntryVO vo = new RegressionEntryVO(getUnitDate(), (subVO.getServerName().equals(getServerName())?getServerName():null), (subVO.getServerGroup().equals(getServerGroup())?getServerGroup():null), resourceMergeType);
		vo.setRequestTxCount(request_tx_count);
		vo.setRequestIpCount(request_ip_count);
		vo.setAverageResponseTimeMS(avg_response_time);
		vo.setErrorCount(err_count);
		vo.setFailed(failed);
		double subvo_total = -1D;
		double myvo_total = -1D; 
		if(subVO.getCpuUsage() != -1D) {
			subvo_total = (subVO.getMergeType() == ResourceMergeType.AVG) ? subVO.getCpuUsage()*subVO.getDataCount() : subVO.getCpuUsage();
			if(cpu != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? cpu*item_count : cpu;
				vo.setCpuUsage((resourceMergeType == ResourceMergeType.AVG) ? ((myvo_total+subvo_total)/(item_count+subVO.getDataCount())) : (myvo_total+subvo_total));
			} else {
				vo.setCpuUsage((resourceMergeType == ResourceMergeType.AVG) ? (subvo_total/subVO.getDataCount()) : subvo_total);
				vo.setFailed(true);
			}
		} else {
			if(cpu != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? cpu*item_count : cpu;
				vo.setCpuUsage((resourceMergeType == ResourceMergeType.AVG) ? (myvo_total/item_count) : myvo_total);
				vo.setFailed(true);
			} else {
				vo.setCpuUsage(cpu);
			}
		}
		if(subVO.getMemoryUsage() != -1D) {
			subvo_total = (subVO.getMergeType() == ResourceMergeType.AVG) ? subVO.getMemoryUsage()*subVO.getDataCount() : subVO.getMemoryUsage();
			if(memory != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? memory*item_count : memory;
				vo.setMemoryUsage((resourceMergeType == ResourceMergeType.AVG) ? ((myvo_total+subvo_total)/(item_count+subVO.getDataCount())) : (myvo_total+subvo_total)); 
			} else {
				vo.setMemoryUsage((resourceMergeType == ResourceMergeType.AVG) ? (subvo_total/subVO.getDataCount()) : subvo_total);
				vo.setFailed(true);
			}
		} else {
			if(memory != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? memory*item_count : memory;
				vo.setMemoryUsage((resourceMergeType == ResourceMergeType.AVG) ? (myvo_total/item_count) : myvo_total);
				vo.setFailed(true);
			} else {
				vo.setMemoryUsage(memory);
			}
		}
		if(subVO.getDiskUsage() != -1D) {
			subvo_total = (subVO.getMergeType() == ResourceMergeType.AVG) ? subVO.getDiskUsage()*subVO.getDataCount() : subVO.getDiskUsage();
			if(disk != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? disk*item_count : disk;
				vo.setDiskUsage((resourceMergeType == ResourceMergeType.AVG) ? ((myvo_total+subvo_total)/(item_count+subVO.getDataCount())) : (myvo_total+subvo_total)); 
			} else {
				vo.setDiskUsage((resourceMergeType == ResourceMergeType.AVG) ? (subvo_total/subVO.getDataCount()) : subvo_total);
				vo.setFailed(true);
			}
		} else {
			if(disk != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? disk*item_count : disk;
				vo.setDiskUsage((resourceMergeType == ResourceMergeType.AVG) ? (myvo_total/item_count) : myvo_total);
				vo.setFailed(true);
			} else {
				vo.setDiskUsage(disk);
			}
		}
		if(subVO.getNetworkUsage() != -1D) {
			subvo_total = (subVO.getMergeType() == ResourceMergeType.AVG) ? subVO.getNetworkUsage()*subVO.getDataCount() : subVO.getNetworkUsage();
			if(network != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? network*item_count : network;
				vo.setNetworkUsage((resourceMergeType == ResourceMergeType.AVG) ? ((myvo_total+subvo_total)/(item_count+subVO.getDataCount())) : (myvo_total+subvo_total)); 
			} else {
				vo.setNetworkUsage((resourceMergeType == ResourceMergeType.AVG) ? (subvo_total/subVO.getDataCount()) : subvo_total);
				vo.setFailed(true);
			}
		} else {
			if(network != -1D) {
				myvo_total = (mergeType == ResourceMergeType.AVG) ? network*item_count : network;
				vo.setNetworkUsage((resourceMergeType == ResourceMergeType.AVG) ? (myvo_total/item_count) : myvo_total);
				vo.setFailed(true);
			} else {
				vo.setNetworkUsage(network);
			}
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
		buffer.append("merge=").append(mergeType).append(", ");		
		buffer.append("cpu=").append(cpu).append(", ");
		buffer.append("memory=").append(memory).append(", ");
		buffer.append("disk=").append(disk).append(", ");
		buffer.append("network=").append(network).append(", ");
		buffer.append("count=").append(item_count);
		buffer.append("]");
		return buffer.toString();
	}

}
