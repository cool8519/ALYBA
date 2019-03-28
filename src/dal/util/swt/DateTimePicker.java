package dal.util.swt;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

public class DateTimePicker extends Composite {

	public static Font TEXT_FONT = SWTResourceManager.getFont("Arial", 7, SWT.BOLD);
	public static int DATE_AND_TIME = 0;
	public static int DATE_ONLY = 1;
	public static int TIME_ONLY = 2;

	protected int pickerType = 0;
	protected DateTime dt_Date = null;
	protected DateTime dt_Time = null;
	protected Button btn_today = null;
	protected Button btn_first = null;
	protected Button btn_last = null;

	public DateTimePicker(Composite parent, int style) {
		super(parent, SWT.NONE);
		createContents();
	}

	public DateTimePicker(Composite parent, int style, int pickerType) {
		super(parent, style);
		setType(pickerType);
		createContents();
	}

	protected void createContents() {

		GridLayout gl = new GridLayout(4, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.horizontalSpacing = 2;
		setLayout(gl);

		GridData gd_btn_today = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		gd_btn_today.widthHint = 16;
		gd_btn_today.heightHint = 16;

		if(pickerType == DATE_AND_TIME || pickerType == DATE_ONLY) {
			btn_today = new Button(this, SWT.NONE);
			btn_today.setToolTipText("Today");
			btn_today.setFont(TEXT_FONT);
			btn_today.setLayoutData(gd_btn_today);
			btn_today.setText("T");

			dt_Date = new DateTime(this, SWT.BORDER | SWT.DATE);
			dt_Date.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			dt_Date.setSize(100, 24);
		}

		if(pickerType == DATE_AND_TIME || pickerType == TIME_ONLY) {
			dt_Time = new DateTime(this, SWT.BORDER | SWT.TIME);
			dt_Time.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1));
			dt_Time.setSize(100, 24);

			GridLayout gl_firstlast = new GridLayout(1, true);
			gl_firstlast.marginWidth = 0;
			gl_firstlast.marginHeight = 0;
			gl_firstlast.verticalSpacing = 0;

			GridData gd_firstlast = new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1);
			gd_firstlast.heightHint = 26;
			Composite comp_firstlast = new Composite(this, SWT.NONE);
			comp_firstlast.setLayoutData(gd_firstlast);
			comp_firstlast.setLayout(gl_firstlast);

			GridData gd_btn_first = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
			gd_btn_first.widthHint = 16;
			gd_btn_first.heightHint = 13;

			btn_first = new Button(comp_firstlast, SWT.CENTER);
			btn_first.setToolTipText("First of day");
			btn_first.setLayoutData(gd_btn_first);
			btn_first.setText("F");
			btn_first.setFont(TEXT_FONT);

			GridData gd_btn_last = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
			gd_btn_last.widthHint = 16;
			gd_btn_last.heightHint = 13;

			btn_last = new Button(comp_firstlast, SWT.CENTER);
			btn_last.setToolTipText("Last of day");
			btn_last.setLayoutData(gd_btn_last);
			btn_last.setText("L");
			btn_last.setFont(TEXT_FONT);
		}

		pack();
		addEventListener();

	}

	protected void addEventListener() {

		if(btn_today != null) {
			btn_today.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date());
					dt_Date.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
				}
			});
		}

		if(btn_first != null) {
			btn_first.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					dt_Time.setTime(0, 0, 0);
				}
			});
		}

		if(btn_last != null) {
			btn_last.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					dt_Time.setTime(23, 59, 59);
				}
			});
		}

	}

	public void setEnabled(boolean enabled) {
		if(btn_today != null) {
			btn_today.setEnabled(enabled);
		}
		if(dt_Date != null) {
			dt_Date.setEnabled(enabled);
		}
		if(dt_Time != null) {
			dt_Time.setEnabled(enabled);
		}
		if(btn_first != null) {
			btn_first.setEnabled(enabled);
		}
		if(btn_last != null) {
			btn_last.setEnabled(enabled);
		}
	}

	public void setDate(Date dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		if(dt_Date != null) {
			dt_Date.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
		}
		if(dt_Time != null) {
			dt_Time.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
		}
	}

	public void setType(int pickerType) {
		this.pickerType = pickerType;
	}

	public int getType() {
		return pickerType;
	}

	public void setDateSize(int width, int height) {
		if(dt_Date != null) {
			dt_Date.setSize(width, height);
			pack();
		}
	}

	public void setTimeSize(int width, int height) {
		if(dt_Time != null) {
			dt_Time.setSize(width, height);
			pack();
		}
	}

	public Date getDate() {
		Calendar cal = Calendar.getInstance();
		if(dt_Date != null) {
			cal.set(Calendar.YEAR, dt_Date.getYear());
			cal.set(Calendar.MONTH, dt_Date.getMonth());
			cal.set(Calendar.DATE, dt_Date.getDay());
		} else {
			cal.set(Calendar.YEAR, 0);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DATE, 0);
		}
		if(dt_Time != null) {
			cal.set(Calendar.HOUR_OF_DAY, dt_Time.getHours());
			cal.set(Calendar.MINUTE, dt_Time.getMinutes());
			cal.set(Calendar.SECOND, dt_Time.getSeconds());
		} else {
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}
		return cal.getTime();
	}

	public DateTime getDateControl() {
		return dt_Date;
	}

	public DateTime getTimeControl() {
		return dt_Time;
	}

	protected void checkSubclass() {
	}
}
