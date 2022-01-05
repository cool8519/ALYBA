package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;
import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.parse.FileInfo;
import dal.tool.analyzer.alyba.parse.parser.ResourceParser;
import dal.tool.analyzer.alyba.parse.task.ResourceAnalyzeTask;
import dal.tool.analyzer.alyba.setting.ResourceAnalyzerSetting;
import dal.tool.analyzer.alyba.setting.ResourceFieldMappingInfo;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.FileUtil;
import dal.util.StringUtil;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.ProgressBarDialog;
import dal.util.swt.SWTResourceManager;

public class ResultResource extends Composite {

	private	SimpleDateFormat sdf_datetime = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	private NumberFormat nf_thousand = NumberFormat.getInstance();

	private SashForm sf_main;
	private Composite comp_left;
	private Composite comp_right;	
	private Group grp_left1;
	private Group grp_left2;
	private Group grp_right;
	private Table tbl_list;
	private TableEditor tbl_editor;
	private Button btn_list_remove;
	private Button btn_list_removeAll;
	
	private Label lb_insert_date_title;
	private Label lb_total_rows_title;
	private Label lb_from_date_title;
	private Label lb_to_date_title;
	private Label lb_insert_date_value;
	private Label lb_total_rows_value;
	private Label lb_from_date_value;
	private Label lb_to_date_value;
	
	public TableViewer tblv_files;
	public Table tbl_files;
	private TableColumn tblc_index;
	private TableColumn tblc_dirName;
	private TableColumn tblc_fileName;
	private TableColumn tblc_fileSize;
	private TableColumn tblc_serverName;
	private TableColumn tblc_serverGroup;	
	private Button btn_file_open;
	private Button btn_file_remove;
	private Button btn_file_removeAll;
	private Button btn_analyzing;

	private ResultAnalyzer resultAnalyzer;
	private ResourceFieldMapping fieldMapping;
	private ObjectDBUtil db = null;
	private EntityManager em = null;
	private SettingEntryVO settingVo = null;

	public ResultResource(Composite parent, int style, ResultAnalyzer owner) {
		super(parent, style);
		this.resultAnalyzer = owner;
		createContents();
		addEventListener();
	}

	public void initDatabase() throws Exception {
		if(db != null) {
			db.close(em);
		}
		this.db = ObjectDBUtil.getInstance();
		this.em = db.createEntityManager();
		this.settingVo = db.select(em, SettingEntryVO.class);
	}

	public void closeDatabase() {
		if(db != null) {
			db.close(em);
		}
		em = null;
		db = null;
	}

	protected void createContents() {
		
		setSize(1244, 690);		
	    setLayout(new FillLayout());
	    
	    sf_main = new SashForm(this, SWT.HORIZONTAL);

	    FormLayout forml_comp_left = new FormLayout();
	    forml_comp_left.marginHeight = 10;
	    forml_comp_left.marginWidth = 20;
	    comp_left = new Composite(sf_main, SWT.NONE);
		comp_left.setLayout(forml_comp_left);

		FillLayout fl_comp_right = new FillLayout(SWT.VERTICAL);
		fl_comp_right.marginWidth = 15;
		fl_comp_right.marginHeight = 15;
	    comp_right = new Composite(sf_main, SWT.NONE);
	    comp_right.setLayout(fl_comp_right);

	    FormData fd_grp_left1 = new FormData();
	    fd_grp_left1.left = new FormAttachment(0);
	    fd_grp_left1.right = new FormAttachment(100);
	    fd_grp_left1.top = new FormAttachment(0, 5);
	    fd_grp_left1.height = 260;
	    FormLayout forml_grp_left1 = new FormLayout();
	    forml_grp_left1.marginHeight = 10;
	    forml_grp_left1.marginWidth = 15;
		grp_left1 = new Group(comp_left, SWT.NONE);
		grp_left1.setLayoutData(fd_grp_left1);
		grp_left1.setLayout(forml_grp_left1);
		grp_left1.setFont(Utility.getFont(SWT.BOLD));
		grp_left1.setText(" LIST ");
		
		FormData fd_tbl_list = new FormData();
		fd_tbl_list.left = new FormAttachment(0);
		fd_tbl_list.right = new FormAttachment(100);
		fd_tbl_list.top = new FormAttachment(0);
		fd_tbl_list.bottom = new FormAttachment(100, -40);
	    tbl_list = new Table(grp_left1, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
	    tbl_list.setLayoutData(fd_tbl_list);
	    tbl_list.setFont(Utility.getFont());
	    tbl_list.setHeaderVisible(true);
	    tbl_list.setLinesVisible(true);
	    tbl_list.getHorizontalBar().setVisible(false);
	    tbl_list.setVisible(true);
		TableColumn tblc_name = new TableColumn(tbl_list, SWT.NONE);
		tblc_name.setText("Server Name");
		tblc_name.setResizable(false);
		tblc_name.setWidth(110);
		TableColumn tblc_group = new TableColumn(tbl_list, SWT.RIGHT);
		tblc_group.setText("Group");
		tblc_group.setResizable(false);
		tblc_group.setWidth(80);
		tbl_editor = new TableEditor(tbl_list);
		tbl_editor.horizontalAlignment = SWT.RIGHT;
		tbl_editor.grabHorizontal = true;
		tbl_editor.minimumWidth = 100;	    
	    
		FormData fd_btn_list_remove = new FormData();
		fd_btn_list_remove.width = 80;
		fd_btn_list_remove.top = new FormAttachment(tbl_list, 10);
	    btn_list_remove = new Button(grp_left1, SWT.NONE);
	    btn_list_remove.setLayoutData(fd_btn_list_remove);
	    btn_list_remove.setText("Remove");
	    
	    FormData fd_btn_list_removeAll = new FormData();
	    fd_btn_list_removeAll.width = 80;
	    fd_btn_list_removeAll.top = new FormAttachment(tbl_list, 10);
	    fd_btn_list_removeAll.bottom = new FormAttachment(btn_list_remove, 0, SWT.BOTTOM);
	    fd_btn_list_removeAll.right = new FormAttachment(tbl_list, 0, SWT.RIGHT);
	    btn_list_removeAll = new Button(grp_left1, SWT.NONE);
	    btn_list_removeAll.setText("Remove All");
	    btn_list_removeAll.setLayoutData(fd_btn_list_removeAll);


	    FormData fd_grp_left2 = new FormData();
	    fd_grp_left2.top = new FormAttachment(grp_left1, 20);
	    fd_grp_left2.left = new FormAttachment(0);
	    fd_grp_left2.right = new FormAttachment(100, 0);
	    fd_grp_left2.bottom = new FormAttachment(100, -5);
	    FormLayout forml_grp_left2 = new FormLayout();
	    forml_grp_left2.marginHeight = 10;
	    forml_grp_left2.marginWidth = 15;
		grp_left2 = new Group(comp_left, SWT.NONE);
		grp_left2.setLayoutData(fd_grp_left2);
		grp_left2.setLayout(forml_grp_left2);
		grp_left2.setFont(Utility.getFont(SWT.BOLD));
		grp_left2.setText(" DATA Summary ");
		
		FormData fd_tbl_columns = new FormData();
		fd_tbl_columns.left = new FormAttachment(0);
		fd_tbl_columns.right = new FormAttachment(100);
		fd_tbl_columns.top = new FormAttachment(0);
		fd_tbl_columns.bottom = new FormAttachment(100, -25);

		FormData fd_lb_insert_date_title = new FormData();
		fd_lb_insert_date_title.top = new FormAttachment(0, 20);
		fd_lb_insert_date_title.left = new FormAttachment(0);
		fd_lb_insert_date_title.width = 80;
		lb_insert_date_title = new Label(grp_left2, SWT.RIGHT);
		lb_insert_date_title.setLayoutData(fd_lb_insert_date_title);
		lb_insert_date_title.setText("Insert Date : ");
		lb_insert_date_title.setFont(Utility.getFont(SWT.ITALIC));
		lb_insert_date_title.setVisible(false);

		FormData fd_lb_insert_date_value = new FormData();
		fd_lb_insert_date_value.top = new FormAttachment(lb_insert_date_title, 0, SWT.TOP);
		fd_lb_insert_date_value.left = new FormAttachment(lb_insert_date_title, 5, SWT.RIGHT);
		fd_lb_insert_date_value.right = new FormAttachment(100);
		lb_insert_date_value = new Label(grp_left2, SWT.LEFT);
		lb_insert_date_value.setLayoutData(fd_lb_insert_date_value);
		lb_insert_date_value.setText("0000.00.00 00:00:00");
		lb_insert_date_value.setFont(Utility.getFont(SWT.ITALIC));
		lb_insert_date_value.setVisible(false);

		FormData fd_lb_total_rows_title = new FormData();
		fd_lb_total_rows_title.top = new FormAttachment(lb_insert_date_title, 20);
		fd_lb_total_rows_title.left = new FormAttachment(0);
		fd_lb_total_rows_title.width = 80;
		lb_total_rows_title = new Label(grp_left2, SWT.RIGHT);
		lb_total_rows_title.setLayoutData(fd_lb_total_rows_title);
		lb_total_rows_title.setText("Total Row : ");
		lb_total_rows_title.setFont(Utility.getFont(SWT.ITALIC));
		lb_total_rows_title.setVisible(false);

		FormData fd_lb_total_rows_value = new FormData();
		fd_lb_total_rows_value.top = new FormAttachment(lb_total_rows_title, 0, SWT.TOP);
		fd_lb_total_rows_value.left = new FormAttachment(lb_total_rows_title, 5, SWT.RIGHT);
		fd_lb_total_rows_value.right = new FormAttachment(100);
		lb_total_rows_value = new Label(grp_left2, SWT.LEFT);
		lb_total_rows_value.setLayoutData(fd_lb_total_rows_value);
		lb_total_rows_value.setText("0,000");
		lb_total_rows_value.setFont(Utility.getFont(SWT.ITALIC));
		lb_total_rows_value.setVisible(false);

		FormData fd_lb_from_date_title = new FormData();
		fd_lb_from_date_title.top = new FormAttachment(lb_total_rows_title, 20);
		fd_lb_from_date_title.left = new FormAttachment(0);
		fd_lb_from_date_title.width = 80;
		lb_from_date_title = new Label(grp_left2, SWT.RIGHT);
		lb_from_date_title.setLayoutData(fd_lb_from_date_title);
		lb_from_date_title.setText("From Date : ");
		lb_from_date_title.setFont(Utility.getFont(SWT.ITALIC));
		lb_from_date_title.setVisible(false);

		FormData fd_lb_from_date_value = new FormData();
		fd_lb_from_date_value.top = new FormAttachment(lb_from_date_title, 0, SWT.TOP);
		fd_lb_from_date_value.left = new FormAttachment(lb_from_date_title, 5, SWT.RIGHT);
		fd_lb_from_date_value.right = new FormAttachment(100);
		lb_from_date_value = new Label(grp_left2, SWT.LEFT);
		lb_from_date_value.setLayoutData(fd_lb_from_date_value);
		lb_from_date_value.setText("0000.00.00 00:00:00");
		lb_from_date_value.setFont(Utility.getFont(SWT.ITALIC));
		lb_from_date_value.setVisible(false);

		FormData fd_lb_to_date_title = new FormData();
		fd_lb_to_date_title.top = new FormAttachment(lb_from_date_title, 20);
		fd_lb_to_date_title.left = new FormAttachment(0);
		fd_lb_to_date_title.width = 80;
		lb_to_date_title = new Label(grp_left2, SWT.RIGHT);
		lb_to_date_title.setLayoutData(fd_lb_to_date_title);
		lb_to_date_title.setText("To Date : ");
		lb_to_date_title.setFont(Utility.getFont(SWT.ITALIC));
		lb_to_date_title.setVisible(false);

		FormData fd_lb_to_date_value = new FormData();
		fd_lb_to_date_value.top = new FormAttachment(lb_to_date_title, 0, SWT.TOP);
		fd_lb_to_date_value.left = new FormAttachment(lb_to_date_title, 5, SWT.RIGHT);
		fd_lb_to_date_value.right = new FormAttachment(100);
		lb_to_date_value = new Label(grp_left2, SWT.LEFT);
		lb_to_date_value.setLayoutData(fd_lb_to_date_value);
		lb_to_date_value.setText("0000.00.00 00:00:00");
		lb_to_date_value.setFont(Utility.getFont(SWT.ITALIC));
		lb_to_date_value.setVisible(false);

		
		FormLayout forml_grp_right = new FormLayout();
		forml_grp_right.marginHeight = 10;
		forml_grp_right.marginWidth = 20;
		grp_right = new Group(comp_right, SWT.NONE);
		grp_right.setLayout(forml_grp_right);
		grp_right.setFont(Utility.getFont(SWT.BOLD));
		grp_right.setText(" FILE ATTACH ");
		
		FormData fd_tbl_files = new FormData();
		fd_tbl_files.left = new FormAttachment(0);
		fd_tbl_files.right = new FormAttachment(100);
		fd_tbl_files.top = new FormAttachment(0);
		fd_tbl_files.height = 170;
		tblv_files = new TableViewer(grp_right, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tbl_files = tblv_files.getTable();
		tbl_files.setLayoutData(fd_tbl_files);
		tbl_files.setLinesVisible(true);
		tbl_files.setHeaderVisible(true);
		tbl_files.setFont(Utility.getFont());
		tblc_index = new TableColumn(tbl_files, SWT.CENTER);
		tblc_index.setText("No.");
		tblc_index.setWidth(35);
		tblc_dirName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_dirName.setText("Directory");
		tblc_dirName.setWidth(390);
		tblc_fileName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_fileName.setText("Filename");
		tblc_fileName.setWidth(160);
		tblc_fileSize = new TableColumn(tbl_files, SWT.RIGHT);
		tblc_fileSize.setText("Size");
		tblc_fileSize.setWidth(90);
		tblc_serverName = new TableColumn(tbl_files, SWT.CENTER);
		tblc_serverName.setText("Server Name");
		tblc_serverName.setWidth(100);
		tblc_serverGroup = new TableColumn(tbl_files, SWT.CENTER);
		tblc_serverGroup.setText("Group");
		tblc_serverGroup.setWidth(70);		

		FormData fd_btn_file_open = new FormData();
		fd_btn_file_open.top = new FormAttachment(tbl_files, 10);
		fd_btn_file_open.width = 160;
		fd_btn_file_open.height = 52;
	    btn_file_open = new Button(grp_right, SWT.NONE);
	    btn_file_open.setLayoutData(fd_btn_file_open);
		btn_file_open.setFont(Utility.getFont());
		btn_file_open.setText("Open File(s)");

		FormData fd_btn_file_remove = new FormData();
		fd_btn_file_remove.top = new FormAttachment(tbl_files, 10);
		fd_btn_file_remove.left = new FormAttachment(btn_file_open, 30);
		fd_btn_file_remove.width = 130;
		fd_btn_file_remove.height = 23;
		btn_file_remove = new Button(grp_right, SWT.NONE);
		btn_file_remove.setLayoutData(fd_btn_file_remove);
		btn_file_remove.setFont(Utility.getFont());
		btn_file_remove.setText("Remove Selected");
		
		FormData fd_btn_file_removeAll = new FormData();
		fd_btn_file_removeAll.bottom = new FormAttachment(btn_file_open, 0, SWT.BOTTOM);
		fd_btn_file_removeAll.left = new FormAttachment(btn_file_open, 30);
		fd_btn_file_removeAll.width = 130;
		fd_btn_file_removeAll.height = 23;
		btn_file_removeAll = new Button(grp_right, SWT.NONE);
		btn_file_removeAll.setLayoutData(fd_btn_file_removeAll);
		btn_file_removeAll.setFont(Utility.getFont());
		btn_file_removeAll.setText("Remove All");
		
		FormData fd_btn_analyzing = new FormData();
		fd_btn_analyzing.right = new FormAttachment(tbl_files, 0, SWT.RIGHT);
		fd_btn_analyzing.top = new FormAttachment(tbl_files, 10);
		fd_btn_analyzing.width = 160;
		fd_btn_analyzing.height = 52;
		btn_analyzing = new Button(grp_right, SWT.NONE);
		btn_analyzing.setLayoutData(fd_btn_analyzing);
		btn_analyzing.setFont(Utility.getFont(SWT.BOLD));
		btn_analyzing.setText("Start Analyzing");
		toggleAnalyzingButton(false);

		FormData fd_hline_middle = new FormData();
		fd_hline_middle.left = new FormAttachment(tbl_files, 0, SWT.LEFT);
		fd_hline_middle.right = new FormAttachment(tbl_files, 0, SWT.RIGHT);
		fd_hline_middle.top = new FormAttachment(btn_analyzing, 10);
		fd_hline_middle.height = 10;
		Label hline_middle = new Label(grp_right, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_middle.setLayoutData(fd_hline_middle);
		
		FormData fd_fieldMapping = new FormData();
		fd_fieldMapping.left = new FormAttachment(0);
		fd_fieldMapping.right = new FormAttachment(100);
		fd_fieldMapping.top = new FormAttachment(hline_middle, 5);
		fd_fieldMapping.bottom = new FormAttachment(100);
		fieldMapping = new ResourceFieldMapping(grp_right, SWT.NONE, this);
		fieldMapping.setLayoutData(fd_fieldMapping);	
		
		resize(new Rectangle(-1, -1, getSize().x+21, getSize().y));
	    
	}

	protected void addEventListener() {
		tbl_list.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				selectServer(tbl_list.getSelection()[0].getText(0), tbl_list.getSelection()[0].getText(1));
			}
		});
		
		btn_list_remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeServerData();
			}
		});

		btn_list_removeAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeAllServerData();
			}
		});

		btn_file_open.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				List<File> files = FileDialogUtil.openReadDialogFiles(getShell(), Constant.ALLFILES_FILTER_NAMES, Constant.ALLFILES_FILTER_EXTS, Constant.DIALOG_INIT_PATH);
				if(files != null && files.size() > 0) {
					addTableItems(tbl_files, files);
				}
			}
		});

		btn_file_remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeFiles();
			}
		});

		btn_file_removeAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int cnt = tbl_files.getItemCount();
				if(cnt > 0) {
					tbl_files.removeAll();
					Logger.debug("Removed " + cnt + " item(s).");
					fieldMapping.reset();
					fieldMapping.setEnabled(false);
				}
			}
		});
		
		tbl_files.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				Point pt = new Point(e.x, e.y);
				TableItem item = tbl_files.getItem(pt);
				File f = (item == null) ? null : (File)item.getData("file");
				if(item != null && f != null) {
					String[] labels = new String[] { "Show headers", "Open file", "Cancel" };
					int i = MessageUtil.showSelectMessage(getShell(), "Question", "What do you want to do?", labels);
					switch(i) {
						case 0:
							List<String> headers = FileUtil.head(f, 5, null);
							ContentView view = new ContentView(getDisplay());
							view.setText("Header View - " + f.getName());
							view.addLines(headers);
							view.autoSize();
							dal.util.swt.Utility.centralize(view, getShell());
							break;
						case 1:
							Program.launch(f.getAbsolutePath());
							break;
					}
				}
			}
		});

		tblv_files.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, Constant.FILE_TRANSFER_TYPE, new DropTargetListener() {
			public void drop(DropTargetEvent event) {
				String[] sourceFileList = (String[])event.data;
				addTableItems(sourceFileList);
			}
			public void dropAccept(DropTargetEvent event) {}
			public void dragOver(DropTargetEvent event) {}
			public void dragOperationChanged(DropTargetEvent event) {}
			public void dragLeave(DropTargetEvent event) {}
			public void dragEnter(DropTargetEvent event) {}
		});
		
		tbl_files.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == 127) {
					removeFiles();
				}
			}
		});

		tblc_index.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByNumber(0);
			}
		});

		tblc_dirName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(1);
			}
		});

		tblc_fileName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(2);
			}
		});

		tblc_fileSize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataBySize();
			}
		});
		
		tblc_serverName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(4);
			}
		});

		tblc_serverGroup.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(5);
			}
		});		
		
		btn_analyzing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				executeAnalyze();
			}
		});

	}

	public void load() throws Exception {
		initDatabase();
		loadTables();
	}

	private void loadTables() throws Exception {
		tbl_list.removeAll();
		try {
			String query = "SELECT DISTINCT o.name,o.group FROM ResourceUsageEntryVO AS o ORDER BY o.name,o.group";
			List<Object[]> list = db.selectList(em, query, Object[].class, null);
			if(list != null) {
				for(Object[] pair : list) {
					TableItem item = new TableItem(tbl_list, SWT.NONE);
					item.setText(0, pair[0].toString());
					item.setText(1, pair[1].toString());
				}
			}
		} catch(Exception e) {
			Logger.debug("Failed to get list of resource name/group.");
			Logger.error(e);
		}
	}
	
	private void selectServer(String name, String group) {
		try {
			SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
			Date insertDate = summaryVo.getLastResourceInsertTime();
			
			String countQuery = "SELECT COUNT(o) FROM ResourceUsageEntryVO AS o WHERE o.name = :name AND o.group = :group AND o.count > 0";
			Map<String,Object> params = new HashMap<String,Object>(2);
			params.put("name", name);
			params.put("group", group);
			long totalRows = db.select(em, countQuery, Long.class, params);

			String fromQuery = "SELECT MIN(o.unit_date) FROM ResourceUsageEntryVO AS o WHERE o.name = :name AND o.group = :group AND o.count > 0";
			Date fromDate = db.select(em, fromQuery, Date.class, params);

			String toQuery = "SELECT MAX(o.unit_date) FROM ResourceUsageEntryVO AS o WHERE o.name = :name AND o.group = :group AND o.count > 0";
			Date toDate = db.select(em, toQuery, Date.class, params);

			lb_insert_date_value.setText(sdf_datetime.format(insertDate));
			lb_total_rows_value.setText(nf_thousand.format(totalRows));
			lb_from_date_value.setText(sdf_datetime.format(fromDate));
			lb_to_date_value.setText(sdf_datetime.format(toDate));
			toggleSummaryVisible(true);
		} catch(Exception e) {
			Logger.debug("Failed to select resource data : name=" + name + ", group=" + group); 
			Logger.error(e);
			toggleSummaryVisible(false);
		}
	}
	
	public void resize(Rectangle rect) {
		int width = rect.width - 24;
	    sf_main.setWeights(new int[] {300, width-300});
	}
	
	public void resetData() {
		tbl_list.removeAll();
		toggleSummaryVisible(false);
		tbl_files.removeAll();
		tblc_index.setWidth(35);
		tblc_dirName.setWidth(390);
		tblc_fileName.setWidth(160);
		tblc_fileSize.setWidth(90);
		tblc_serverName.setWidth(100);
		tblc_serverGroup.setWidth(70);		
		fieldMapping.reset();
	}
	
	private void toggleSummaryVisible(boolean flag) {
		lb_insert_date_title.setVisible(flag);
		lb_total_rows_title.setVisible(flag);
		lb_from_date_title.setVisible(flag);
		lb_to_date_title.setVisible(flag);		
		lb_insert_date_value.setVisible(flag);
		lb_total_rows_value.setVisible(flag);
		lb_from_date_value.setVisible(flag);
		lb_to_date_value.setVisible(flag);		
	}
	
	public void toggleAnalyzingButton(boolean flag) {
		btn_analyzing.setEnabled(flag);
		if(flag) {
			btn_analyzing.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		} else {
			btn_analyzing.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		}
	}
	
	protected void addTableItems(Table table, List<File> list) {
		String name = getStringFromInputDialog("Server Name", "Enter name of server", "", 1);
		if(name == null) return;
		String group = getServerGroup(name);
		if(group == null) return;		
		int from_idx = tbl_files.getItemCount();
		int cnt = 0;
		for(int i = 0; i < list.size(); i++) {
			File f = (File)list.get(i);
			try {
				int idx = existsTableItem(table, f);
				if(idx > -1) {
					table.getItem(idx).setText(3, FileUtil.getFileSizeString(f));
					table.getItem(idx).setText(4, name);
					table.getItem(idx).setText(5, group);				
					table.getItem(idx).setData("file", f);
					Logger.debug("Duplicated Item(size/name/group updated) : " + f.getCanonicalPath());
				} else {
					String fullpath = f.getCanonicalPath();
					int lastidx = fullpath.lastIndexOf(File.separatorChar);
					TableItem item = new TableItem(table, SWT.NULL);
					item.setText(0, String.valueOf(table.getItemCount()));
					item.setText(1, fullpath.substring(0, lastidx));
					item.setText(2, fullpath.substring(lastidx + 1));
					item.setText(3, FileUtil.getFileSizeString(f));
					item.setText(4, name);
					item.setText(5, group);
					item.setData("file", f);
					Logger.debug("Added Item : " + f.getCanonicalPath());
					cnt++;
				}
			} catch(Exception ex) {
				Logger.debug("Failed to add file item(s).");
				Logger.error(ex);
			}
		}
		if(cnt > 0) {
			Logger.debug(cnt + " files are added successfully.");
			tblv_files.getTable().deselectAll();
			tblv_files.getTable().select(from_idx, tbl_files.getItemCount() - 1);
			tbl_files.showItem(tbl_files.getItem(tbl_files.getItemCount() - 1));
		}
		
		if(tbl_files.getItemCount() > 0) {
			fieldMapping.setEnabled(true);
		} else {
			fieldMapping.setEnabled(false);
		}
	}
	
	public int existsTableItem(Table tableData, File f) throws IOException {
		TableItem[] items = tableData.getItems();
		TableItem item;
		for(int i = 0; i < items.length; i++) {
			item = items[i];
			if(f.getCanonicalPath().equals(((File)item.getData("file")).getCanonicalPath())) {
				return tableData.indexOf(item);
			}
		}
		return -1;
	}
	
	protected void removeServerData() {
		int[] indices = tbl_list.getSelectionIndices();
		if(indices.length < 1) {
			return;
		}
		if(!MessageUtil.showConfirmMessage(getShell(), "The data for the server will be deleted.\n\nDo you want to continue?")) {
			return;
		}
		for(int idx : indices) {
			TableItem item = tbl_list.getItem(idx);
			String name = item.getText(0);
			String group = item.getText(1);
			try {
				String delete_query = "DELETE FROM ResourceUsageEntryVO AS o WHERE o.name = :name AND o.group = :group";
				Map<String,Object> params = new HashMap<String,Object>(2);
				params.put("name", name);
				params.put("group", group);
				int count = db.deleteWithTransaction(em, delete_query, params, true);
				if(count > 0) {
					Logger.debug("Removed server(" + name + ":" + group + ") resource data : " + count + " row(s).");				
				}
				tbl_list.remove(idx);
			} catch(Exception e) {
				Logger.debug("Failed to remove resource data for the server : name=" + name + ", group=" + group);
				Logger.error(e);
			}
		}
		checkExistServerData();
		toggleSummaryVisible(false);
	}
	
	protected void removeAllServerData() {
		if(!MessageUtil.showConfirmMessage(getShell(), "All resource data will be deleted.\n\nDo you want to continue?")) {
			return;
		}
		try {
			String delete_query = "DELETE FROM ResourceUsageEntryVO";
			Map<String,Object> params = null;
			int count = db.deleteWithTransaction(em, delete_query, params, true);
			if(count > 0) {
				Logger.debug("Removed all resource data : " + count + " row(s).");				
			}
			tbl_list.removeAll();
		} catch(Exception e) {
			Logger.debug("Failed to remove all resource data.");
			Logger.error(e);
		}
		checkExistServerData();
		toggleSummaryVisible(false);
	}

	private void checkExistServerData() {
		try {
			long count = db.count(em, ResourceUsageEntryVO.class);
			if(count < 1) {
				SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
				summaryVo.setLastResourceInsertTime(null);
				Logger.debug("Updating Summary Data to DB.");
				db.insertWithTransaction(em, summaryVo, true);
			}
		} catch(Exception e) {
			Logger.debug("Failed to update summary data.");
			Logger.error(e);
		}		
	}

	protected void removeFiles() {
		int[] indices = tbl_files.getSelectionIndices();
		if(indices.length < 1) {
			return;
		} else {
			tbl_files.remove(indices);
			TableItem[] items = tbl_files.getItems();
			for(TableItem item : items) {
				item.setText(0, String.valueOf(tbl_files.indexOf(item) + 1));
			}
			if(indices.length == 1) {
				int last_idx = indices[0];
				int cnt = tbl_files.getItemCount();
				int idx;
				if(cnt < last_idx) {
					switch(cnt) {
						case 0:
							return;
						case 1:
							idx = 0;
							break;
						default:
							idx = -1;
					}
				} else {
					idx = (cnt > last_idx + 1) ? last_idx : (cnt - 1);
				}
				tblv_files.getTable().select(idx);
			} else {
				tblv_files.getTable().deselectAll();
			}
			Logger.debug("Removed " + indices.length + " item(s).");
		}
		if(tbl_files.getItemCount() == 0) {
			fieldMapping.reset();
			fieldMapping.setEnabled(false);
		}
	}

	public void addTableItems(String[] list) {
		List<File> filelist = new ArrayList<File>();
		File f;
		for(String s : list) {
			f = new File(s);
			if(f.isDirectory()) {
				List<File> subfiles = FileUtil.getAllFiles(s, null, true);
				for(File subfile : subfiles) {
					filelist.add(subfile);
				}
			} else if(f.isFile()) {
				filelist.add(f);
			}
		}
		addTableItems(tbl_files, filelist);
	}

	protected void sortTableDataByNumber(int fieldIdx) {
		Object obj = tbl_files.getColumn(fieldIdx).getData("asc");
		boolean isAsc = (obj == null) ? true : !(Boolean)obj;
		TableItem[] items = tbl_files.getItems();
		int value1;
		int value2;
		for(int i = 1; i < items.length; i++) {
			value1 = Integer.parseInt(items[i].getText(fieldIdx));
			for(int j = 0; j < i; j++) {
				value2 = Integer.parseInt(items[j].getText(fieldIdx));
				if((isAsc && (value1 < value2)) || (!isAsc && (value1 > value2))) {
					String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3) };
					File f = (File)items[i].getData("file");
					items[i].dispose();
					TableItem item = new TableItem(tbl_files, SWT.NULL, j);
					item.setText(values);
					item.setData("file", f);
					items = tbl_files.getItems();
					break;
				}
			}
		}
		tbl_files.getColumn(fieldIdx).setData("asc", isAsc);
	}

	protected void sortTableDataByString(int fieldIdx) {
		Object obj = tbl_files.getColumn(fieldIdx).getData("asc");
		boolean isAsc = (obj == null) ? true : !(Boolean)obj;
		TableItem[] items = tbl_files.getItems();
		Collator collator = Collator.getInstance(Locale.getDefault());
		String value1;
		String value2;
		for(int i = 1; i < items.length; i++) {
			value1 = items[i].getText(fieldIdx);
			for(int j = 0; j < i; j++) {
				value2 = items[j].getText(fieldIdx);
				if((isAsc && collator.compare(value1, value2) < 0) || (!isAsc && collator.compare(value1, value2) > 0)) {
					String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5) };
					File f = (File)items[i].getData("file");
					items[i].dispose();
					TableItem item = new TableItem(tbl_files, SWT.NULL, j);
					item.setText(values);
					item.setData("file", f);
					items = tbl_files.getItems();
					break;
				}
			}
		}
		tbl_files.getColumn(fieldIdx).setData("asc", isAsc);
	}

	protected void sortTableDataBySize() {
		Object obj = tbl_files.getColumn(3).getData("asc");
		boolean isAsc = (obj == null) ? true : !(Boolean)obj;
		TableItem[] items = tbl_files.getItems();
		long value1;
		long value2;
		for(int i = 1; i < items.length; i++) {
			value1 = ((File)items[i].getData("file")).length();
			for(int j = 0; j < i; j++) {
				value2 = ((File)items[j].getData("file")).length();
				if((isAsc && (value1 < value2)) || (!isAsc && (value1 > value2))) {
					String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5) };
					File f = (File)items[i].getData("file");
					items[i].dispose();
					TableItem item = new TableItem(tbl_files, SWT.NULL, j);
					item.setText(values);
					item.setData("file", f);
					items = tbl_files.getItems();
					break;
				}
			}
		}
		tbl_files.getColumn(3).setData("asc", isAsc);
	}

	private String getStringFromInputDialog(String title, String message, String init_value, final int min_length) {
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), title, message, init_value, new IInputValidator() {
			public String isValid(String s) {
				if(s.length() < min_length)
					return "Too short";
				return null;
			}
		});
		if(dlg.open() == Window.OK) {
			return dlg.getValue();
		} else {
			return null;
		}
	}

	protected void executeAnalyze() {
		int idx = fieldMapping.checkMappingValidation();
		if(idx > -1) {
			MessageUtil.showInfoMessage(getShell(), "Invalid format of file has been detected.\nCheck the file No." + (idx + 1));
			return;
		}

		ResourceAnalyzerSetting setting = getAnalyzerSetting();
		setting.setAnalyzeDate(new Date());
		Logger.debug(setting.toString());
		
		if(!MessageUtil.showConfirmMessage(getShell(), "Do you want to analyze the files with current mapping?")) {
			return;
		}

		try {
			boolean exists = false;
			for(TableItem item : tbl_files.getItems()) {
				String name = item.getText(4);
				String group = item.getText(5);
				for(TableItem l_item : tbl_list.getItems()) {
					if(name.equalsIgnoreCase(l_item.getText(0)) && group.equalsIgnoreCase(l_item.getText(1))) {
						exists = true;
						break;
					}
				}
			}
			if(exists) {
				if(!MessageUtil.showConfirmMessage(getShell(), "The data for the server name exists.\n"
						+ "If the data at any point already exists, it will be overwritten.\n\nDo you want to continue?")) {
					return;
				}
			}
		} catch(Exception e) {
			Logger.debug("Failed to check if data exist.");
			Logger.error(e);
		}
		
		ProgressBarDialog progressBar = new ProgressBarDialog(getShell(), Utility.getFont());
		progressBar.setTitle("Analyzer Progress");
		progressBar.setDetailViewCount(tbl_files.getItemCount());
		ResourceAnalyzeTask task = new ResourceAnalyzeTask(setting, ResourceParser.class);
		progressBar.setTask(task);
		progressBar.open();
		if(!task.isSuccessed()) {
			String msg = task.getFailedMessage();
			if(msg != null) {
				MessageUtil.showWarningMessage(getShell(), msg);
			} else {
				MessageUtil.showWarningMessage(getShell(), "Unknown error");
			}
		}

		boolean reloadAnalyzer = false;
		if(task.isSuccessed()) {
			reloadAnalyzer = MessageUtil.showConfirmMessage(getShell(), "Resource data of the server has been successfully added to the database.\n"
					+ "Functions related to the data are activated only when the database is reloaded.\n\nDo you want to reload the analyzer?");
		}
		try {
			if(reloadAnalyzer) {
				resultAnalyzer.reloadDBFile();
			} else {
				loadTables();
			}
		} catch(Exception e) {
			Logger.debug("Failed to reload data.");
			Logger.error(e);
		}

		task = null;
	}
	
	private ResourceAnalyzerSetting getAnalyzerSetting() {
		ResourceFieldMappingInfo fieldMappingInfo = new ResourceFieldMappingInfo();
		fieldMappingInfo.setFileType(fieldMapping.getFileType());
		fieldMappingInfo.setFieldDelimeter(StringUtil.replaceMetaCharacter(fieldMapping.getDelimeter(), false));
		fieldMappingInfo.setFieldBracelet(fieldMapping.getBracelet());
		fieldMappingInfo.setMappingInfo(fieldMapping.getMappingData());
		fieldMappingInfo.setOffsetHour(Float.parseFloat(fieldMapping.getOffsetHour()));
		fieldMappingInfo.setTimeFormat(fieldMapping.getTimeFormat());
		fieldMappingInfo.setTimeLocale(fieldMapping.getTimeLocale());
		fieldMappingInfo.setFieldCount(fieldMapping.getFieldCount());
		fieldMappingInfo.setCpuIdle(fieldMapping.isCpuIdle());
		fieldMappingInfo.setMemoryIdle(fieldMapping.isMemoryIdle());
		fieldMappingInfo.setDiskIdle(fieldMapping.isDiskIdle());
		fieldMappingInfo.setNetworkIdle(fieldMapping.isNetworkIdle());

		ResourceAnalyzerSetting setting = new ResourceAnalyzerSetting();
		setting.setAnalyzerTimezone(Constant.TIMEZONE_DEFAULT);
		setting.setFieldMapping(fieldMappingInfo);
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
		for(TableItem item : tbl_files.getItems()) {
			FileInfo fileInfo = new FileInfo((File)item.getData("file"));
			fileInfo.setFileMeta("name", item.getText(4));
			fileInfo.setFileMeta("group", item.getText(5));
			fileInfoList.add(fileInfo);
		}		
		setting.setFileInfoList(fileInfoList);
		setting.setUnitMinutes(settingVo.getTPMUnitMinutes());
		
		return setting;
	}

	private String getServerGroup(String name) {
		String init_group = "default";
		boolean exists = false;
		int cnt = 0;
		for(TableItem item : tbl_files.getItems()) {
			if(cnt++ == 0 || (item.getText(4) != null && item.getText(4).equalsIgnoreCase(name))) {
				init_group = item.getText(5);
				exists = true;
			}
		}
		if(!exists) {
			cnt = 0;
			for(TableItem item : tbl_list.getItems()) {
				if(cnt++ == 0 || name.equalsIgnoreCase(item.getText(0))) {
					init_group = item.getText(1);
				}
			}
		}
		return getStringFromInputDialog("Group", "Enter group of server", init_group, 1);
	}

}
