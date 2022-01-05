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
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
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
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class ResultAnalyzer extends Shell {

	public static String title_prefix = "ALYBA " + Constant.PROGRAM_VERSION + " - Result Analyzer";
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
	private TabItem tbi_resource;
	private ResultSummary summaryView;
	private ResultData dataView;
	private ResultChart chartView;
	private ResultResource resourceView;
	private DropTarget droptarget_dbfile;

	private String fileName = null;
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
		FontData fd = display.getSystemFont().getFontData()[0];
		Constant.DEFAULT_FONT_SIZE = fd.getHeight() - (int)Math.ceil((-fd.data.lfHeight-12)/2.0F);
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
			return setting.getLogMappingInfo().containsKey(key);
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
		this.fileName = fileName;
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
		setText(title_prefix);
	}

	protected void createContents() {
		
		instance = this;
		
		setSize(1280, 815);
		setMinimumSize(1100, 815);
		setText(title_prefix);
		Rectangle dispRect = getDisplay().getMonitors()[0].getBounds();
		Rectangle shellRect = getBounds();
		setLocation((dispRect.width - shellRect.width) / 2, (dispRect.height - shellRect.height) / 2);		

		FormLayout forml_main = new FormLayout();
		forml_main.marginHeight = 10;
		forml_main.marginWidth = 10;
		setLayout(forml_main);

		FormData fd_lb_title = new FormData();
		fd_lb_title.left = new FormAttachment(0);
		fd_lb_title.top = new FormAttachment(0, 7);
		fd_lb_title.width = 40;
		fd_lb_title.height = 15;
		Label lb_title = new Label(this, SWT.NONE);
		lb_title.setLayoutData(fd_lb_title);
		lb_title.setAlignment(SWT.CENTER);
		lb_title.setText("Title");
		lb_title.setFont(Utility.getFont());

		FormData fd_txt_title = new FormData();
		fd_txt_title.left = new FormAttachment(lb_title, 5, SWT.RIGHT);
		fd_txt_title.top = new FormAttachment(0, 6);
		fd_txt_title.width = 300;
		fd_txt_title.height = 15;
		txt_title = new Text(this, SWT.BORDER | SWT.RESIZE);
		txt_title.setLayoutData(fd_txt_title);
		txt_title.setText("");
		txt_title.setFont(Utility.getFont());
		txt_title.setEnabled(false);		
		
		FormData fd_btn_openDB = new FormData();
		fd_btn_openDB.right = new FormAttachment(100);
		fd_btn_openDB.top = new FormAttachment(0);
		fd_btn_openDB.width = 100;
		fd_btn_openDB.height = 28;
		btn_openDB = new Button(this, SWT.NONE);
		btn_openDB.setLayoutData(fd_btn_openDB);
		btn_openDB.setText("Open DB");
		btn_openDB.setFont(Utility.getFont());

		FormData fd_lb_notloaded = new FormData();
		fd_lb_notloaded.top = new FormAttachment(0, 7);
		fd_lb_notloaded.right = new FormAttachment(btn_openDB, -15);
		fd_lb_notloaded.width = 137;
		fd_lb_notloaded.height = 15;
		lb_notloaded = new Label(this, SWT.NONE);
		lb_notloaded.setLayoutData(fd_lb_notloaded);
		lb_notloaded.setAlignment(SWT.RIGHT);
		lb_notloaded.setText("Database is not loaded.");
		lb_notloaded.setFont(Utility.getFont());
		lb_notloaded.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));

		FormData fd_pbar_loading = new FormData();
		fd_pbar_loading.top = new FormAttachment(0, 4);
		fd_pbar_loading.right = new FormAttachment(btn_openDB, -15);
		fd_pbar_loading.width = 150;
		fd_pbar_loading.height = 20;
		pbar_loading = new ProgressBar(this, SWT.NONE);
		pbar_loading.setLayoutData(fd_pbar_loading);
		pbar_loading.setBounds(990, 14, 150, 20);
		pbar_loading.setMaximum(6);
		pbar_loading.setSelection(0);
		pbar_loading.setVisible(false);
		
		FormData fd_hline_top = new FormData();
		fd_hline_top.left = new FormAttachment(0);
		fd_hline_top.right = new FormAttachment(100);
		fd_hline_top.top = new FormAttachment(btn_openDB, 7);
		fd_hline_top.height = 10;
		hline_top = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_top.setLayoutData(fd_hline_top);

		FormData fd_tbf_view = new FormData();
		fd_tbf_view.left = new FormAttachment(0);
		fd_tbf_view.right = new FormAttachment(100);
		fd_tbf_view.top = new FormAttachment(hline_top, 5);
		fd_tbf_view.bottom = new FormAttachment(100);
		tbf_view = new TabFolder(this, SWT.NONE);
		tbf_view.setLayoutData(fd_tbf_view);
		tbf_view.setFont(Utility.getFont());

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
		
		tbi_resource = new TabItem(tbf_view, SWT.NONE);
		tbi_resource.setText("Resource");
		resourceView = new ResultResource(tbf_view, SWT.NONE, this);
		resourceView.setEnabled(false);
		tbi_resource.setControl(resourceView);
		
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
					resourceView.setEnabled(false);
					btn_openDB.setText("Open DB");
					lb_notloaded.setVisible(true);
				}
			}
		});

		droptarget_dbfile.addDropListener(new DropTargetListener() {			
			public void drop(DropTargetEvent event) {
				String[] sourceFileList = (String[])event.data;
				if(sourceFileList != null && sourceFileList.length == 1) {
					String dropFileName = sourceFileList[0];
					String ext = dropFileName.substring(dropFileName.lastIndexOf(".")+1, dropFileName.length());
					if(ext.equals("adb")) {
						loadDBFile(dropFileName);
						fileName = dropFileName;
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
		summaryView.resize(rect);
		dataView.resize(rect);
		chartView.resize(rect);
		resourceView.resize(rect);
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
		resourceView.setEnabled(false);
		summaryView.resetData();
		dataView.resetData();
		chartView.resetData();
		resourceView.resetData();
		tbf_view.setSelection(0);
	}

	public void loadDBFile(String fileName) {
		closeDatabase();
		initDatabase(fileName);
		resetData();

		try {
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
				resourceView.load();
				pbar_loading.setSelection(6);
				dataView.setEnabled(true);
				chartView.setEnabled(true);
				resourceView.setEnabled(true);
				btn_openDB.setText("Close DB");
				setText(title_prefix + " (" + fileName + ")");
			} else {
				closeDatabase();
			}
		} catch(Exception e) {
			Logger.debug("Failed to load the database : " + fileName);
			Logger.error(e);
			MessageUtil.showErrorMessage(instance, "Failed to load the database.");
			dataView.setEnabled(false);
			chartView.setEnabled(false);
			resourceView.setEnabled(false);
			lb_notloaded.setVisible(true);
			closeDatabase();
		} finally {
			pbar_loading.setVisible(false);
		}
	}
	
	public void reloadDBFile() {
		loadDBFile(this.fileName);
	}
	
	private boolean checkVersion() throws Exception {
		SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
		String programVersion = Constant.PROGRAM_VERSION.indexOf('_') < 0 ? Constant.PROGRAM_VERSION : Constant.PROGRAM_VERSION.split("_")[0];
		String databaseVersion = summaryVo.getVersion().indexOf('_') < 0 ? summaryVo.getVersion() : summaryVo.getVersion().split("_")[0]; 
		if(programVersion.equals(databaseVersion)) {
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
