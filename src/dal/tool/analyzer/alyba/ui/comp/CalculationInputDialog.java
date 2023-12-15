package dal.tool.analyzer.alyba.ui.comp;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;

public class CalculationInputDialog extends Dialog {
	
	private static final String[][] operatorChar = { {"+", "-", "*", "/"}, {"＋", "－", "×", "÷"} };
	
	private Label lb_left;
	private Label lb_right;
    private Text txt_left;
    private Text txt_right;
    private Text txt_result;
    private Button btn_switch;
    private CCombo cb_operator;
    private VerifyListener numberVerifyListener;

    private boolean isLeftNew = false;
    private String currentValue = null;
    private String currentExpression = null;
    private Double currentNumber = -1D;
    private String newValue = null;
    private String newExpression = null;
    private Double newNumber = -1D;
    private String resultValue = null;
    private Double resultNumber = -1D;

    protected CalculationInputDialog(Shell parentShell) {
        super(parentShell);
    }

    public void setValues(String currentValue, String newValue) {
    	this.currentValue = currentValue;
    	this.newValue = newValue;
    	String currTokens[] = currentValue.split("=");
    	currentExpression = currTokens[0].substring(1, currTokens[0].length()-1);
    	currentNumber = Double.valueOf(currTokens[1]);
    	if(newValue != null) {
	    	String newTokens[] = newValue.split("=");
	    	newExpression = newTokens[0].substring(1, newTokens[0].length()-1);
	    	newNumber = Double.valueOf(newTokens[1]);
    	}
    }
    
    public boolean isResultValid() {
		return !(resultNumber.isInfinite() || resultNumber.isNaN());
    }
    
    public String getResultValue() {
    	return resultValue;
    }

    @Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Input for Expression");
	}

    @Override
    protected Control createDialogArea(Composite parent) {
    	GridLayout gl_main = new GridLayout(5, false);
    	gl_main.marginBottom = 20;
    	gl_main.marginRight = 20;
    	gl_main.marginLeft = 20;
    	gl_main.marginTop = 20;
    	gl_main.horizontalSpacing = 20;
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(gl_main);

        GridData gd_lb_left = new GridData(SWT.FILL, SWT.CENTER, true, false);
        lb_left = new Label(container, SWT.CENTER);
        lb_left.setFont(Utility.getFont());
        lb_left.setText("Current");
        lb_left.setLayoutData(gd_lb_left);
        
        GridData gd_btn_switch = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        gd_btn_switch.widthHint = 80;
        btn_switch = new Button(container, SWT.NONE);
        btn_switch.setFont(Utility.getFont("System", 12, SWT.BOLD));
        btn_switch.setText("↔");
        btn_switch.setLayoutData(gd_btn_switch);
        
        GridData gd_lb_right = new GridData(SWT.FILL, SWT.CENTER, true, false);
        lb_right = new Label(container, SWT.CENTER);
        lb_right.setFont(Utility.getFont());
        lb_right.setText("New");
        lb_right.setLayoutData(gd_lb_right);

        GridData gd_lb_blank = new GridData(SWT.FILL, SWT.CENTER, true, false);
        Label lb_blank = new Label(container, SWT.CENTER);
        lb_blank.setFont(Utility.getFont());
        lb_blank.setText("");
        lb_blank.setLayoutData(gd_lb_blank);
        
        GridData gd_lb_result = new GridData(SWT.FILL, SWT.CENTER, true, false);
        Label lb_result = new Label(container, SWT.CENTER);
        lb_result.setFont(Utility.getFont());
        lb_result.setText("Result");
        lb_result.setLayoutData(gd_lb_result);
        
        GridData gd_txt_left = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_txt_left.widthHint = 100;
        txt_left = new Text(container, SWT.BORDER | SWT.CENTER);
        txt_left.setFont(Utility.getFont());
        txt_left.setEditable(false);
        txt_left.setLayoutData(gd_txt_left);
		if(currentValue != null) {
			txt_left.setText(currentValue);
		}

        GridData gd_cb_operator = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        cb_operator = new CCombo(container, SWT.BORDER);
        cb_operator.setItems(operatorChar[1]);
        cb_operator.setFont(Utility.getFont());
        cb_operator.select(0);
        cb_operator.setLayoutData(gd_cb_operator);
       
        GridData gd_txt_right = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_txt_right.widthHint = 100;
        txt_right = new Text(container, SWT.BORDER | SWT.CENTER);
        txt_right.setFont(Utility.getFont());
    	txt_right.setEditable(newValue==null);
        txt_right.setLayoutData(gd_txt_right);
		if(newValue != null) {
			txt_right.setText(newValue);
		}
        
        GridData gd_lb_arrow = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_lb_arrow.widthHint = 30;
        Label lb_arrow = new Label(container, SWT.CENTER);
        lb_arrow.setFont(Utility.getFont());
        lb_arrow.setText("＝");
        lb_arrow.setLayoutData(gd_lb_arrow);

        GridData gd_txt_result = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_txt_result.widthHint = 150;
        txt_result = new Text(container, SWT.BORDER | SWT.CENTER);
        txt_result.setFont(Utility.getFont());
        txt_result.setEditable(false);
        txt_result.setLayoutData(gd_txt_result);
      
        addEventListener();
        applyResultValue();

        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button btn_ok = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        btn_ok.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		Logger.debug("CalculationInputDialog return : " + resultValue);
        	}
		});
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

	protected void addEventListener() {
		txt_left.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(txt_left.getEditable()) {
					applyResultValue();
				}
			}
		});

		txt_right.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(txt_right.getEditable()) {
					applyResultValue();
				}
			}
		});

		cb_operator.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyResultValue();
			}
		});
		
		btn_switch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				switchLeftRight();
				applyResultValue();
			}
		});

        numberVerifyListener = new VerifyListener() {
			public void verifyText(VerifyEvent e) {
	            String currentText = ((Text)e.widget).getText();
	            String newText = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
	            if(!newText.matches("^\\d*\\.?\\d*$")) {
	                e.doit = false;
	            }
			}
		};
		if(newValue == null) {
			if(isLeftNew) {
				txt_left.addVerifyListener(numberVerifyListener);
			} else {
				txt_right.addVerifyListener(numberVerifyListener);
			}
		}
	}
	
	protected void switchLeftRight() {
		String temp_str = lb_left.getText();
		lb_left.setText(lb_right.getText());
		lb_right.setText(temp_str);
		isLeftNew = "New".equals(lb_left.getText());

		txt_left.removeVerifyListener(numberVerifyListener);
		txt_right.removeVerifyListener(numberVerifyListener);
		txt_left.setEditable(false);
		txt_right.setEditable(false);
		temp_str = txt_left.getText();
		txt_left.setText(txt_right.getText());
		txt_right.setText(temp_str);
		
		if(newValue == null) {
			if(isLeftNew) {
				txt_right.setEditable(false);
				txt_left.setEditable(true);
				txt_left.addVerifyListener(numberVerifyListener);
			} else {
				txt_left.setEditable(false);
				txt_right.setEditable(true);
				txt_right.addVerifyListener(numberVerifyListener);
			}
		}
	}
	
	protected void applyResultValue() {
		String currentEx = currentExpression;
		if(currentExpression.contains("+") || currentExpression.contains("-") || currentExpression.contains("*") || currentExpression.contains("/")) {
			currentEx = "(" + currentExpression + ")";
		}
		String operator = operatorChar[0][cb_operator.getSelectionIndex()];
		String valueString;
		if(newValue == null) {
			if(txt_left.getText().length() < 1 || txt_right.getText().length() < 1) {
				resultValue = currentEx;
				resultNumber = currentNumber;
				valueString = currentNumber.toString();
			} else {
				if(isLeftNew) {
					resultValue = txt_left.getText() + operator + currentEx;
					valueString = getCalcutatedString(Double.valueOf(txt_left.getText()), currentNumber);
				} else {
					resultValue = currentEx + operator + txt_right.getText();
					valueString = getCalcutatedString(currentNumber, Double.valueOf(txt_right.getText()));
				}
			}
		} else {
			if(isLeftNew) {
				resultValue = newExpression + operator + currentEx;
				valueString = getCalcutatedString(newNumber, currentNumber);
			} else {
				resultValue = currentEx + operator + newExpression;
				valueString = getCalcutatedString(currentNumber, newNumber);				
			}
		}
		resultValue = "{" + resultValue + "}=" + valueString;
		txt_result.setText(resultValue);
	}
	
	protected String getCalcutatedString(Double value1, Double value2) {
		String operator = operatorChar[0][cb_operator.getSelectionIndex()];
		Double result = null;
		if("+".equals(operator)) {
			result = value1 + value2;
		} else if("-".equals(operator)) {
			result = value1 - value2;
		} else if("*".equals(operator)) {
			result = value1 * value2;
		} else if("/".equals(operator)) {
			result = value1 / value2;
		}
		resultNumber = result;
		String resultString = String.valueOf(result);
		if(isResultValid() && resultString.substring(resultString.indexOf('.')+1).length() > 4) {
			return Double.valueOf(Math.round(result*10000)/10000.0).toString();
		} else {
			return resultString;
		}
	}

}
