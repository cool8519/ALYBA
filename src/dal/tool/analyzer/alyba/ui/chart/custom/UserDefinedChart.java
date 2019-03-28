package dal.tool.analyzer.alyba.ui.chart.custom;

import java.util.List;

import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.ui.chart.Chart;

public class UserDefinedChart extends Chart {

	public UserDefinedChart(Type chartType) {
		super(chartType);
	}

	protected <E extends EntryVO> void createDataset(List<E> dataList) {
	}

	@Override
	public boolean checkChartType(Type chartType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createChart() {
		// TODO Auto-generated method stub
		
	}

}
