package dal.tool.analyzer.alyba.ui.chart.extension;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

public class TotalCountItemLabelGenerator implements CategoryItemLabelGenerator {
	
	private NumberFormat df = DecimalFormat.getNumberInstance();
	
	public String generateLabel(CategoryDataset dataset, int row, int column) {
		if(dataset.getRowCount() == 1) {
			return df.format(dataset.getValue(row, column));
		}
		Number numVal = dataset.getValue(row, column);
		if(numVal == null) return null;
		int row_other = row==0 ? 1 : 0;
		Number numVal_other = dataset.getValue(row_other, column);
		Number resultVal;
		if(numVal_other == null) {
			resultVal = numVal;
		} else {
			if(row == 0) return null;
			resultVal = numVal.intValue() + numVal_other.intValue();
		}
		return df.format(resultVal);
	}

	public String generateRowLabel(CategoryDataset dataset, int row) {
		return null;
	}

	public String generateColumnLabel(CategoryDataset dataset, int column) {
		return null;
	}
}
