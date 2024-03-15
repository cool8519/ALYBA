package dal.tool.analyzer.alyba.ui.chart.extension;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

public class SuccessErrorCountToolTipGenerator implements CategoryToolTipGenerator {
	
	private NumberFormat df = DecimalFormat.getNumberInstance();
	
	public String generateToolTip(CategoryDataset dataset, int row, int column) {
		String columnKey = (String)dataset.getColumnKey(column);
		if(dataset.getRowCount() == 1) {
			return columnKey + ": " + df.format(dataset.getValue(row, column));
		}
		Number successVal = dataset.getValue(0, column);
		Number errorVal = dataset.getValue(1, column);
		int total = 0;
		int success = 0;
		int error = 0;
		if(successVal != null) {
			success = successVal.intValue();
			total += success;
		}
		if(errorVal != null) {
			error = errorVal.intValue();
			total += error;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(columnKey).append(": ").append(df.format(total)).append(" (");
		if(success > 0) {
			sb.append("Success:").append(df.format(success));
		}
		if(error > 0) {
			if(success > 0) {
				sb.append(", ");
			}
			sb.append("Error:").append(df.format(error));
		}
		sb.append(")");
		return sb.toString();
	}
}
