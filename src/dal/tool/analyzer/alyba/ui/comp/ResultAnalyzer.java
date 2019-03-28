package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;
import java.util.TimeZone;

import javax.persistence.EntityManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class ResultAnalyzer extends Shell {

	public static ProgressBar pbar_loading;

	private ResultAnalyzer instance;
	
	private Label lb_notloaded;
	private Label hline_top;
	private Text txt_title;
	private Button btn_openDB;
	private TabFolder tbf_view;
	private TabItem tbi_summary;
	private TabItem tbi_data;
	private TabItem tbi_chart;
	private ResultSummary summaryView;
	private ResultData dataView;
	private ResultChart chartView;
	private DropTarget droptarget_dbfile;

	private ObjectDBUtil db = null;
	private EntityManager em = null;

    public static void main(String[] args) {
        String dbFileName = null;
        
        if(args.length == 1) {
			dbFileName = args[0];
			if(!(new File(dbFileName).exists())) {
				System.out.println("File not exists.");
				return;
			}        	
        } else if(args.length > 1) {
			System.out.println("Too many arguments.");
			return;
        }

		TimeZone.setDefault(Constant.TIMEZONE_UTC);
        Display display = new Display();
        ResultAnalyzer analyzer = new ResultAnalyzer(display, SWT.SHELL_TRIM, dbFileName);

        while(!analyzer.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

	public static boolean hasMappingInfo(String key) {
		ObjectDBUtil db = null;
		EntityManager em = null;
		try {
			db = ObjectDBUtil.getInstance();
			em = db.createEntityManager();
			SettingEntryVO setting = db.select(em, SettingEntryVO.class);
			return setting.getMappingInfo().containsKey(key);
		} catch(Exception e) {
			return false;
		} finally {
			if(db != null && em != null) {
				db.close(em);
			}
		}
	}

	public ResultAnalyzer(Display display, int style) {
		super(display, style);
		createContents();
		addEventListener();
		open();
		layout(true, true);
	}
	
	public ResultAnalyzer(Display display, int style, String fileName) {
		super(display, style);
		createContents();
		addEventListener();
		open();
		layout(true, true);
		if(fileName != null) {
			loadDBFile(fileName);
		}
	}
	
	public void initDatabase(String fileName) {
		if(db != null) {
			db.close(em);
		}
		if(ObjectDBUtil.isRegistered()) {
			this.db = ObjectDBUtil.getInstance();
		} else {
			this.db = new ObjectDBUtil(fileName);
			ObjectDBUtil.register(db);
		}
		this.em = db.createEntityManager();		
	}

	public void closeDatabase() {
		if(db != null) {
			db.closeAll();
		}
		em = null;
		db = null;
	}

	protected void createContents() {
		
		instance = this;
		
		setSize(1280, 815);
		setMinimumSize(840, 815);
		setText("ALYBA " + Constant.PROGRAM_VERSION + " - Result Analyzer");
		Rectangle dispRect = getDisplay().getMonitors()[0].getBounds();
		Rectangle shellRect = getBounds();
		setLocation((dispRect.width - shellRect.width) / 2, (dispRect.height - shellRect.height) / 2);		

		Label lb_title = new Label(this, SWT.NONE);
		lb_title.setBounds(10, 17, 40, 15);
		lb_title.setAlignment(SWT.CENTER);
		lb_title.setText("Title");

		txt_title = new Text(this, SWT.BORDER | SWT.RESIZE);
		txt_title.setBounds(56, 15, 300, 19);
		txt_title.setText("");
		txt_title.setEnabled(false);	
		
		lb_notloaded = new Label(this, SWT.NONE);
		lb_notloaded.setAlignment(SWT.RIGHT);
		lb_notloaded.setBounds(1000, 17, 137, 15);
		lb_notloaded.setText("Database is not loaded.");
		lb_notloaded.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		
		pbar_loading = new ProgressBar(this, SWT.NONE);
		pbar_loading.setBounds(990, 14, 150, 20);
		pbar_loading.setMaximum(5);
		pbar_loading.setSelection(0);
		pbar_loading.setVisible(false);

		btn_openDB = new Button(this, SWT.NONE);
		btn_openDB.setText("Open DB");
		btn_openDB.setBounds(1154, 10, 100, 28);
		
		hline_top = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_top.setBounds(10, 45, 1244, 10);

		tbf_view = new TabFolder(this, SWT.NONE);
		tbf_view.setBounds(10, 60, 1244, 700);

		tbi_summary = new TabItem(tbf_view, SWT.NONE);
		tbi_summary.setText("Summary");
		summaryView = new ResultSummary(tbf_view, SWT.NONE);
		summaryView.setEnabled(false);
		tbi_summary.setControl(summaryView);
		
		tbi_data = new TabItem(tbf_view, SWT.NONE);
		tbi_data.setText("Data");
		dataView = new ResultData(tbf_view, SWT.NONE);
		dataView.setEnabled(false);
		tbi_data.setControl(dataView);

		tbi_chart = new TabItem(tbf_view, SWT.NONE);
		tbi_chart.setText("Chart");
		chartView = new ResultChart(tbf_view, SWT.NONE);
		chartView.setEnabled(false);
		tbi_chart.setControl(chartView);
		
		/* TODO: ResultRegression */
		
		droptarget_dbfile = new DropTarget(this, DND.DROP_MOVE | DND.DROP_DEFAULT);
		droptarget_dbfile.setTransfer(Constant.FILE_TRANSFER_TYPE);
		
	}
		
	protected void addEventListener() {
		
		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				if(AlybaGUI.instance != null) {
					AlybaGUI.instance.resultAnalyzer.setVisible(false);
					e.doit = false;
				} else {
					e.doit = MessageUtil.showConfirmMessage(instance, "Do you really want to exit?");
					if(e.doit) {
						if(ObjectDBUtil.isRegistered()) {
							ObjectDBUtil.getInstance().closeAll();
						}
					}
				}
			}
		});

		addListener(SWT.Resize, new Listener() {
			public void handleEvent (Event e) {
				Rectangle rect = getClientArea();
				resize(rect);
			}
		});

		btn_openDB.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if("Open DB".equals(btn_openDB.getText())) {
					String dir = ".";
					if(AlybaGUI.instance != null) {
						dir = AlybaGUI.getInstance().outputSetting.getOutputDirectory();
					}
					File file = FileDialogUtil.openReadDialogFile(instance, Constant.DB_FILTER_NAMES, Constant.DB_FILTER_EXTS, dir);
					if(file != null) {
						loadDBFile(file.getPath());
					}
				} else {
					closeDatabase();
					summaryView.setEnabled(false);
					dataView.setEnabled(false);
					chartView.setEnabled(false);
					btn_openDB.setText("Open DB");
					lb_notloaded.setVisible(true);
				}
			}
		});

		droptarget_dbfile.addDropListener(new DropTargetListener() {			
			public void drop(DropTargetEvent event) {
				String[] sourceFileList = (String[])event.data;
				if(sourceFileList != null && sourceFileList.length == 1) {
					String fileName = sourceFileList[0];
					String ext = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
					if(ext.equals("adb")) {
						loadDBFile(fileName);
					} else {
						MessageUtil.showErrorMessage(instance, "Only .adb files can be read.");
					}
				} else {
					MessageUtil.showErrorMessage(instance, "Multiple files can not be read.");
				}
			}
			public void dropAccept(DropTargetEvent event) {}
			public void dragOver(DropTargetEvent event) {}
			public void dragOperationChanged(DropTargetEvent event) {}
			public void dragLeave(DropTargetEvent event) {}
			public void dragEnter(DropTargetEvent event) {}
		});

	}

	protected void checkSubclass() {
	}

	private void resize(Rectangle rect) {
		hline_top.setSize(rect.width-20, hline_top.getBounds().height);
		lb_notloaded.setLocation(rect.width-264, 17);
		pbar_loading.setLocation(rect.width-275, 14);
		btn_openDB.setLocation(rect.width-110, 10);
		tbf_view.setSize(rect.width-20, rect.height-70);
		summaryView.resize(rect);
		dataView.resize(rect);
		chartView.resize(rect);
	}

	public void resetData() {
		txt_title.setText("");
		txt_title.setEnabled(false);	
		lb_notloaded.setVisible(true);
		pbar_loading.setVisible(false);
		btn_openDB.setText("Open DB");
		summaryView.setEnabled(false);
		dataView.setEnabled(false);
		chartView.setEnabled(false);		
		summaryView.resetData();
		dataView.resetData();
		chartView.resetData();
		tbf_view.setSelection(0);
	}

	public void loadDBFile(String fileName) {
		closeDatabase();
		resetData();

		try {
			initDatabase(fileName);
			lb_notloaded.setVisible(false);
			pbar_loading.setSelection(0);
			pbar_loading.setVisible(true);
			if(checkVersion()) {
				pbar_loading.setSelection(1);
				loadTitle();
				pbar_loading.setSelection(2);
				summaryView.load();
				pbar_loading.setSelection(3);
				dataView.load();
				pbar_loading.setSelection(4);
				chartView.load();
				pbar_loading.setSelection(5);
				dataView.setEnabled(true);
				chartView.setEnabled(true);
				btn_openDB.setText("Close DB");
			} else {
				closeDatabase();
			}
		} catch(Exception e) {
			Logger.error(e);
			MessageUtil.showErrorMessage(instance, "Failed to load the database.");
			dataView.setEnabled(false);
			chartView.setEnabled(false);
			lb_notloaded.setVisible(true);
			closeDatabase();
		} finally {
			pbar_loading.setVisible(false);
		}
	}
	
	private boolean checkVersion() throws Exception {
		SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
		if(Constant.PROGRAM_VERSION.equals(summaryVo.getVersion())) {
			return true;
		} else {
			String msg = "Version mismatch. Do you really want to continue?\n\n- Database : " + summaryVo.getVersion() + "\n- Program: " + Constant.PROGRAM_VERSION;
			return MessageUtil.showConfirmMessage(getShell(), msg);
		}
	}
	
	private void loadTitle() throws Exception {
		SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
		txt_title.setText(summaryVo.getTitle());
	}

}
