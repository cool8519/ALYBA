package dal.tool.analyzer.alyba.output.vo;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import dal.util.DateUtil;

@Entity
public class ResourceUsageEntryVO extends EntryVO {

	private static final long serialVersionUID = 1L;

	@Id
	protected Date unit_date = null;
	@Id
	protected String name;
	@Id
	protected String group;
	
	protected double cpu = -1D;
	protected double memory = -1D;
	protected double disk = -1D;
	protected double network = -1D;
	protected int item_count = 0;

	public ResourceUsageEntryVO(Date dt) {
		this.unit_date = dt;
	}
	
	public ResourceUsageEntryVO(Date dt, String name, String group) {
		this.unit_date = dt;
		this.name = name;
		this.group = group;
	}

	public ResourceUsageEntryVO(Date dt, String name, String group, double cpu, double memory, double disk, double network) {
		this.unit_date = dt;
		this.name = name;
		this.group = group;
		this.cpu = cpu;
		this.memory = memory;
		this.disk = disk;
		this.network = network;
		this.item_count = 1;
	}
	
	public Date getUnitDate() {
		return unit_date;
	}

	public void setUnitDate(Date unit_date) {
		this.unit_date = unit_date;
	}
	
	public String getServerName() {
		return name;
	}

	public void setServerName(String name) {
		this.name = name;
	}

	public String getServerGroup() {
		return group;
	}

	public void setServerGroup(String group) {
		this.group = group;
	}

	public String getNameTag() {
		return group + ":" + name;
	}

	public double getCpuUsage() {
		return cpu;
	}

	public void setCpuUsage(double cpu) {
		this.cpu = cpu;
	}

	public double getMemoryUsage() {
		return memory;
	}

	public void setMemoryUsage(double memory) {
		this.memory = memory;
	}

	public double getDiskUsage() {
		return disk;
	}

	public void setDiskUsage(double disk) {
		this.disk = disk;
	}

	public double getNetworkUsage() {
		return network;
	}

	public void setNetworkUsage(double network) {
		this.network = network;
	}
	
	public int getDataCount() {
		return item_count;
	}
	
	public void setDataCount(int count) {
		this.item_count = count;
	}

	public void addData(ResourceUsageEntryVO vo) {
		double temp_cpu = -1D;
		double temp_memory = -1D;
		double temp_disk = -1D;
		double temp_network = -1D;
		if(vo.getCpuUsage() != -1D) {
			temp_cpu = (cpu == -1D) ? vo.getCpuUsage() : ((cpu*item_count + vo.getCpuUsage()) / (item_count+1));
		}
		if(vo.getMemoryUsage() != -1D) {
			temp_memory = (memory == -1D) ? vo.getMemoryUsage() : ((memory*item_count + vo.getMemoryUsage()) / (item_count+1));
		}
		if(vo.getDiskUsage() != -1D) {
			temp_disk = (disk == -1D) ? vo.getDiskUsage() : ((disk*item_count + vo.getDiskUsage()) / (item_count+1));
		}
		if(vo.getNetworkUsage() != -1D) {
			temp_network = (network == -1D) ? vo.getNetworkUsage() : ((network*item_count + vo.getNetworkUsage()) / (item_count+1));
		}
		if((cpu != -1D && vo.getCpuUsage() != -1D) || (memory != -1D && vo.getMemoryUsage() != -1D) || (disk != -1D && vo.getDiskUsage() != -1D) || (network != -1D && vo.getNetworkUsage() != -1D)) {
			item_count++;
		} else {
			item_count = item_count < vo.getDataCount() ? vo.getDataCount() : item_count;
		}
		cpu = (temp_cpu!=-1D) ? temp_cpu : cpu;
		memory = (temp_memory!=-1D) ? temp_memory : memory;
		disk = (temp_disk!=-1D) ? temp_disk : disk;
		network = (temp_network!=-1D) ? temp_network : network;
	}

	public ResourceUsageEntryVO merge(ResourceUsageEntryVO subVO) {
		ResourceUsageEntryVO vo = new ResourceUsageEntryVO(getUnitDate(), getServerName(), getServerGroup());
		if(subVO.getCpuUsage() != -1D) {
			if(cpu != -1D) {
				vo.setCpuUsage((cpu*item_count + subVO.getCpuUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
			} else {
				vo.setCpuUsage(subVO.getCpuUsage());
			}
		} else {
			vo.setCpuUsage(cpu);
		}
		if(subVO.getMemoryUsage() != -1D) {
			if(memory != -1D) {
				vo.setMemoryUsage((memory*item_count + subVO.getMemoryUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
			} else {
				vo.setMemoryUsage(subVO.getMemoryUsage());
			}
		} else {
			vo.setMemoryUsage(memory);
		}
		if(subVO.getDiskUsage() != -1D) {
			if(disk != -1D) {
				vo.setDiskUsage((disk*item_count + subVO.getDiskUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
			} else {
				vo.setDiskUsage(subVO.getDiskUsage());
			}
		} else {
			vo.setDiskUsage(disk);
		}
		if(subVO.getNetworkUsage() != -1D) {
			if(network != -1D) {
				vo.setNetworkUsage((network*item_count + subVO.getNetworkUsage()*subVO.getDataCount()) / (item_count+subVO.getDataCount()));
			} else {
				vo.setNetworkUsage(subVO.getNetworkUsage());
			}
		} else {
			vo.setNetworkUsage(network);
		}
		vo.setDataCount(item_count+subVO.getDataCount());
		return vo;
	}
	
	public ResourceUsageEntryVO copy() {
		ResourceUsageEntryVO vo = new ResourceUsageEntryVO(new Date(unit_date.getTime()));
		vo.name = name == null ? null : new String(name);
		vo.group = group == null ? null : new String(group);
		vo.cpu = cpu;
		vo.memory = memory;
		vo.disk = disk;
		vo.network = network;
		vo.item_count = item_count;
		return vo;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getSimpleName()).append("(").append(hashCode()).append(")");
		buffer.append("[");
		buffer.append("date=").append(unit_date).append(", ");
		buffer.append("name=").append(name).append(", ");
		buffer.append("group=").append(group).append(", ");
		buffer.append("cpu=").append(cpu).append(", ");
		buffer.append("memory=").append(memory).append(", ");
		buffer.append("disk=").append(disk).append(", ");
		buffer.append("network=").append(network).append(", ");
		buffer.append("count=").append(item_count);
		buffer.append("]");
		return buffer.toString();
	}

	public String toJSONString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("{").append("\n");
		if(unit_date != null) {
			buffer.append("  date : ").append(DateUtil.dateToString(unit_date, DateUtil.SDF_DATETIME)).append("\n");
		}
		if(item_count > 0) {
			buffer.append("  count : ").append(item_count).append("\n");
		}
		buffer.append("  server : {").append("\n");
		if(name != null) {
			buffer.append("    name : ").append(name).append("\n");
		}
		if(group != null) {
			buffer.append("    group : ").append(group).append("\n");
		}
		buffer.append("  },").append("\n");
		buffer.append("  usage : {").append("\n");
		if(cpu != -1D) {
			buffer.append("    cpu : ").append(cpu).append("\n");
		}
		if(memory != -1D) {
			buffer.append("    memory : ").append(memory).append("\n");
		}
		if(disk != -1D) {
			buffer.append("    disk : ").append(disk).append("\n");
		}
		if(network != -1D) {
			buffer.append("    network : ").append(network).append("\n");
		}
		buffer.append("  }").append("\n");
		buffer.append("}");
		return buffer.toString();
	}

	public String toPrettyString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("# Usage").append("\n");
		if(unit_date != null) {
			buffer.append("    date : ").append(DateUtil.dateToString(unit_date, DateUtil.SDF_DATETIME)).append("\n");
		}
		if(item_count > 0) {
			buffer.append("    count : ").append(item_count).append("\n");
		}
		if(cpu != -1D) {
			buffer.append("    cpu : ").append(cpu).append("\n");
		}
		if(memory != -1D) {
			buffer.append("    memory : ").append(memory).append("\n");
		}
		if(disk != -1D) {
			buffer.append("    disk : ").append(disk).append("\n");
		}
		if(network != -1D) {
			buffer.append("    network : ").append(network).append("\n");
		}
		return buffer.toString();
	}

}
