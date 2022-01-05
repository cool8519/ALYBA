package dal.util.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import dal.tool.analyzer.alyba.Constant;

public class ProgressBarDialog extends Dialog {

	private static Font TEXT_FONT = SWTResourceManager.getFont("Arial", 9, SWT.NONE);
	private static final int CHECK_INTERVAL = 500;

	private Display display;
	private Shell shell;
	private Button btn_cancel;
	private Composite comp_mainMsg;
	private Composite comp_totalmsg;
	private Composite comp_progressBar;
	private Composite comp_detailview;
	private Composite comp_cancel;
	private Label lb_totalPercent;
	private Label lb_totalTime;
	private Label[] lb_detailView;
	private Label lb_detailMsg;
	private Label lb_line;
	private CLabel clb_mainMsg;
	private ProgressBar pbar_progressBar;
	private ProgressBarTask task;

	protected int detailViewCount = 1;
	protected String title;
	protected String processMessage;
	protected Image processImage;
	protected boolean showCancel;
	protected boolean isCanceled = false;

	/**
	 * @wbp.parser.constructor
	 */
	public ProgressBarDialog(Shell parent) {
		super(parent);
		title = "Progress";
		processMessage = "Please wait....";
		processImage = ImageUtil.getImage(Constant.IMAGE_PATH_PROGRESS);
		showCancel = true;
	}

	public ProgressBarDialog(Shell parent, Font font) {
		super(parent);
		setFont(font);
		title = "Progress";
		processMessage = "Please wait....";
		processImage = ImageUtil.getImage(Constant.IMAGE_PATH_PROGRESS);
		showCancel = true;
	}

	public void setFont(Font font) {
		TEXT_FONT = font;
	}

	public void setTask(ProgressBarTask task) {
		this.task = task;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDetailViewCount(int count) {
		this.detailViewCount = count;
	}

	public void setProcessMessage(String processMessage) {
		this.processMessage = processMessage;
	}

	public void setProcessImage(String imagePath) {
		this.processImage = ImageUtil.getImage(imagePath);
	}

	public void showCancelButton(boolean showCancel) {
		this.showCancel = showCancel;
	}

	public boolean open() {
		display = getParent().getDisplay();
		shell = new Shell(getParent(), SWT.TITLE | SWT.PRIMARY_MODAL);
		createContents();
		addEventListener();
		shell.open();
		shell.layout();
		Utility.centralize(shell, getParent());
		new ProcessThread().start();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return task.isSuccessed();
	}

	protected void createContents() {

		GridLayout gl_progress = new GridLayout();
		gl_progress.verticalSpacing = 5;

		shell.setSize(480, 220);
		shell.setText(title);
		shell.setLayout(gl_progress);

		comp_mainMsg = new Composite(shell, SWT.NONE);
		comp_mainMsg.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		comp_mainMsg.setLayout(new GridLayout());

		clb_mainMsg = new CLabel(comp_mainMsg, SWT.NONE);
		clb_mainMsg.setImage(processImage);
		clb_mainMsg.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		clb_mainMsg.setFont(TEXT_FONT);
		clb_mainMsg.setText(" Please wait....");

		GridLayout gl_totalmsg = new GridLayout();
		gl_totalmsg.numColumns = 3;

		GridData gd_comp_totalmsg = new GridData(GridData.FILL, SWT.CENTER, false, false);
		gd_comp_totalmsg.heightHint = 25;
		comp_totalmsg = new Composite(shell, SWT.NONE);
		comp_totalmsg.setLayout(gl_totalmsg);
		comp_totalmsg.setLayoutData(gd_comp_totalmsg);

		GridData gd_lb_totalPercent = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
		gd_lb_totalPercent.widthHint = 250;
		lb_totalPercent = new Label(comp_totalmsg, SWT.NONE);
		lb_totalPercent.setAlignment(SWT.LEFT);
		lb_totalPercent.setLayoutData(gd_lb_totalPercent);
		lb_totalPercent.setFont(TEXT_FONT);

		GridData gd_lb_totalTime = new GridData(GridData.END, GridData.CENTER, true, false);
		gd_lb_totalTime.widthHint = 150;
		lb_totalTime = new Label(comp_totalmsg, SWT.NONE);
		lb_totalTime.setAlignment(SWT.RIGHT);
		lb_totalTime.setLayoutData(gd_lb_totalTime);
		lb_totalTime.setFont(TEXT_FONT);
		new Label(comp_totalmsg, SWT.NONE);

		GridData gd_comp_progressBar = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gd_comp_progressBar.heightHint = 17;
		comp_progressBar = new Composite(shell, SWT.NONE);
		comp_progressBar.setLayoutData(gd_comp_progressBar);
		comp_progressBar.setLayout(new FillLayout());
		pbar_progressBar = new ProgressBar(comp_progressBar, SWT.SMOOTH);

		GridLayout gl_detailview = new GridLayout(30, true);
		gl_detailview.marginHeight = 2;

		GridData gd_comp_detailview = new GridData(GridData.FILL, SWT.FILL, false, false);
		gd_comp_detailview.heightHint = (int)(20 * Math.ceil((double)detailViewCount/30));
		comp_detailview = new Composite(shell, SWT.NONE);
		comp_detailview.setLayout(gl_detailview);
		comp_detailview.setLayoutData(gd_comp_detailview);

		GridData gd_lb_detailView = new GridData(GridData.FILL, SWT.FILL, false, false);
		lb_detailView = new Label[detailViewCount];
		for(int i = 0; i < detailViewCount; i++) {
			lb_detailView[i] = new Label(comp_detailview, SWT.NONE);
			lb_detailView[i].setFont(TEXT_FONT);
			lb_detailView[i].setText("■");
			lb_detailView[i].setAlignment(SWT.CENTER);
			lb_detailView[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
			lb_detailView[i].setLayoutData(gd_lb_detailView);
		}

		lb_detailMsg = new Label(shell, SWT.NONE);
		GridData gd_lb_detailMsg = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gd_lb_detailMsg.heightHint = 20;
		lb_detailMsg.setAlignment(SWT.CENTER);
		lb_detailMsg.setLayoutData(gd_lb_detailMsg);
		lb_detailMsg.setFont(TEXT_FONT);

		GridData gd_lb_line = new GridData(GridData.FILL, GridData.CENTER, false, false);
		gd_lb_line.heightHint = 10;
		lb_line = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
		lb_line.setLayoutData(gd_lb_line);

		GridLayout gl_cancel = new GridLayout();
		gl_cancel.numColumns = 2;

		comp_cancel = new Composite(shell, SWT.NONE);
		GridData gd_comp_cancel = new GridData(GridData.END, GridData.CENTER, false, false);
		gd_comp_cancel.widthHint = 96;
		gd_comp_cancel.heightHint = 30;
		comp_cancel.setLayout(gl_cancel);
		comp_cancel.setLayoutData(gd_comp_cancel);

		if(showCancel) {
			btn_cancel = new Button(comp_cancel, SWT.NONE);
			btn_cancel.setLayoutData(new GridData(78, SWT.DEFAULT));
			btn_cancel.setFont(TEXT_FONT);
			btn_cancel.setText("Cancel");
			btn_cancel.setEnabled(this.showCancel);
		}

		shell.pack(true);

	}

	protected void addEventListener() {
		shell.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				if(e.detail == SWT.TRAVERSE_ESCAPE) {
					e.doit = false;
				}
			}
		});

		if(showCancel) {
			btn_cancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(!MessageUtil.showConfirmMessage(shell, "Do you really want to stop the task?")) {
						return;
					}
					task.cancel();
					isCanceled = true;
				}
			});
		}
	}

	protected void updateDetailView() {
		int[] status = task.getDetailStatus();
		int[] percents = task.getTasksPercent();
		String[] details = task.getTasksDetail();
		if(status == null || status.length != detailViewCount) {
			return;
		}
		Color rgbColor = SWTResourceManager.getColor(SWT.COLOR_GRAY);
		for(int i = 0; i < detailViewCount; i++) {
			switch(status[i]) {
				case ProgressBarTask.STATUS_READY:
					rgbColor = SWTResourceManager.getColor(SWT.COLOR_GRAY);
					lb_detailView[i].setToolTipText("Waiting");
					break;
				case ProgressBarTask.STATUS_PROCESSING:
					int green = (int)(255 * (1 - (double)percents[i]/100));
					int blue = (int)(255 * ((double)percents[i]/100));
					rgbColor = new Color(display, 0, green, blue);
					lb_detailView[i].setToolTipText(percents[i] + "%" + (details[i]==null?"":(" ["+details[i]+"]")));
					break;
				case ProgressBarTask.STATUS_COMPLETE:
					rgbColor = SWTResourceManager.getColor(SWT.COLOR_BLUE);
					lb_detailView[i].setToolTipText("Complete" + (details[i]==null?"":(" ["+details[i]+"]")));
					break;
				case ProgressBarTask.STATUS_ERROR:
					rgbColor = SWTResourceManager.getColor(SWT.COLOR_RED);
					lb_detailView[i].setToolTipText("Error");
					break;
			}
			lb_detailView[i].setForeground(rgbColor);
		}
	}

	private class ProcessThread extends Thread {

		protected void cleanUp() {
		}

		protected void doBefore() {
		}

		protected void doAfter() {
		}

		public void run() {
			doBefore();
			Thread thr = new Thread(task);
			thr.start();
			do {
				if(display.isDisposed()) {
					break;
				}
				display.asyncExec(new Runnable() {
					public void run() {
						if(pbar_progressBar.isDisposed()) {
							return;
						}
						task.updatePercentAndTimeMessage();
						String total_pmsg = task.getTotalPercentMessage();
						String total_tmsg = task.getTotalTimeMessage();
						String detail_msg = task.getDetailMessage();
						int percent = (int)task.getPercent();
						lb_totalPercent.setText(total_pmsg);
						lb_totalTime.setText(total_tmsg);
						lb_detailMsg.setText(detail_msg);
						updateDetailView();
						pbar_progressBar.setSelection(percent);
						if(task.isComplete() || isCanceled) {
							shell.close();
						}
					}
				});
				if(task.isComplete() || isCanceled) {
					if(isCanceled) {
						cleanUp();
					}
				}
				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch(InterruptedException e) {
				}
			} while(!task.isComplete() && !isCanceled);
			doAfter();
			if(!display.isDisposed()) {
				display.syncExec(new Runnable() {
					public void run() {
						if(!shell.isDisposed()) {
							shell.close();
						}
					}
				});
			}
		}
	}

}
