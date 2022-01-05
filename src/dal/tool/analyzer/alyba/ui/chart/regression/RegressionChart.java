package dal.tool.analyzer.alyba.ui.chart.regression;

import dal.tool.analyzer.alyba.output.vo.RegressionEntryVO;
import dal.tool.analyzer.alyba.ui.chart.MultiChart;

public class RegressionChart extends MultiChart {

	public static enum VariableX { TX, IP }
	public static enum VariableY { CPU, MEMORY, DISK, NETWORK, AVG_RESPONSE, ERROR }
	public static enum AggregationType { ALL, GROUP, NAME }
	public static enum RegressionType { LINEAR/*, EXPONENTIAL, LOGARITHMIC, POWER, POLYNOMIAL*/ }
	public static enum ResourceMergeType { AVG, SUM }

	protected VariableX var_x = VariableX.TX;
	protected VariableY var_y = VariableY.CPU;
	protected AggregationType aggregation_type = AggregationType.NAME;
	protected ResourceMergeType resource_merge_type = ResourceMergeType.AVG;
	protected RegressionType regression_type = RegressionType.LINEAR;
	protected boolean show_regression_line = true;
	protected boolean show_regression_equation = true;
	protected boolean resource_axis_to_100 = false;
	
	public RegressionChart() {
		super("Regression Analysis", 2);
	}

	public void setVariableX(VariableX var) {
		this.var_x = var;		
	}

	public void setVariableY(VariableY var) {
		this.var_y = var;		
	}

	public void setAggregationType(AggregationType type) {
		this.aggregation_type = type;		
	}

	public void setResourceMergeType(ResourceMergeType type) {
		this.resource_merge_type = type;		
	}

	public void setRegressionType(RegressionType type) {
		this.regression_type = type;
	}

	public void setShowRegressionLine(boolean flag) {
		this.show_regression_line = flag;		
	}

	public void setShowRegressionEquation(boolean flag) {
		this.show_regression_equation = flag;		
	}
	
	public void setResourceAxisTo100(boolean flag) {
		this.resource_axis_to_100 = flag;		
	}

	public void initCharts() {
		setChart(0, new RegressionVariablesChart(title, var_x, var_y, aggregation_type, resource_merge_type, resource_axis_to_100));
		setChart(1, new RegressionAnalysisChart(null, var_x, var_y, aggregation_type, resource_merge_type, regression_type, show_regression_line, show_regression_equation, resource_axis_to_100));
	}

	public static Number getVariableData(RegressionEntryVO vo, String name) {
		Number val = -1;
		if(name.equals(VariableX.TX.name())) {
			val = vo.getRequestTxCount();
		} else if(name.equals(VariableX.IP.name())) {
			val = vo.getRequestIpCount();
		} else if(name.equals(VariableY.CPU.name())) {
			val = vo.getCpuUsage();
		} else if(name.equals(VariableY.MEMORY.name())) {
			val = vo.getMemoryUsage();
		} else if(name.equals(VariableY.DISK.name())) {
			val = vo.getDiskUsage();
		} else if(name.equals(VariableY.NETWORK.name())) {
			val = vo.getNetworkUsage();
		} else if(name.equals(VariableY.AVG_RESPONSE.name())) {
			val = vo.getAverageResponseTimeMS();
		} else if(name.equals(VariableY.ERROR.name())) {
			val = vo.getErrorCount();
		}		
		return (val.doubleValue() < 0.0D) ? null : val;
	}

	public static boolean isVariableResource(String name) {
		if(name.equals(VariableY.CPU.name()) || name.equals(VariableY.MEMORY.name()) || name.equals(VariableY.DISK.name()) || name.equals(VariableY.NETWORK.name())) {
			return true;
		} else {
			return false;
		}
	}
	
}
