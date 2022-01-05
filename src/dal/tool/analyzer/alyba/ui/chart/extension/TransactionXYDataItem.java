package dal.tool.analyzer.alyba.ui.chart.extension;

import org.jfree.data.xy.XYDataItem;

import dal.tool.analyzer.alyba.output.vo.TransactionEntryVO;

public class TransactionXYDataItem extends XYDataItem {

	private static final long serialVersionUID = -1L;

	private TransactionEntryVO transaction;
	
	public TransactionXYDataItem(double x, double y, TransactionEntryVO transaction) {
		super(x, y);
		this.transaction = transaction;
	}

	public TransactionEntryVO getTransaction() {
		return transaction;
	}

	public void setTransaction(TransactionEntryVO transaction) {
		this.transaction = transaction;
	}

}
