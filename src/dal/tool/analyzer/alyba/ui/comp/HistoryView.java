package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.SummaryEntryVO;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.history.HistoryManager;
import dal.tool.analyzer.alyba.ui.history.HistoryVO;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.ImageUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class HistoryView extends Shell {

	public ResultAnalyzer resultAnalyzer;
	private HistoryView instance;
	private Composite comp_main;
	private boolean loaded;
	private List<Process> runningProcesses; 

	private TableViewer tblv_files;
	private Table tbl_files;
	private TableColumn tblc_title;
	private TableColumn tblc_created;
	private TableColumn tblc_version;
	private TableColumn tblc_fileSize;
	private TableColumn tblc_fileExists;
	private TableColumn tblc_fileName;
	private TableColumn tblc_dirName;
	private Button btn_open;
	private Button btn_remove;
	private Button btn_reloadAndSync;
	private DropTarget droptarget_dbfile;
	
	
	public HistoryView(Display display, int style, ResultAnalyzer owner) {
		super(display, style);
		this.resultAnalyzer = owner;
		createContents();
		addEventListener();
		open();
		layout(true, true);
		runningProcesses = new ArrayList<Process>();
	}
	
	protected void createContents() {
		
		instance = this;
		
		setSize(1024, 300);
		setMinimumSize(1024, 300);
		setImage(ImageUtil.getImage(Constant.IMAGE_PATH_TRAYICON));
		setText("ALYBA " + Constant.PROGRAM_VERSION + " - History View");
		Rectangle dispRect = getDisplay().getMonitors()[0].getBounds();
		Rectangle shellRect = getBounds();
		setLocation((dispRect.width - shellRect.width) / 2, (dispRect.height - shellRect.height) / 2);		

	    setLayout(new FillLayout());

		FormLayout forml_main = new FormLayout();
		forml_main.marginHeight = 20;
		forml_main.marginWidth = 20;
	    comp_main = new Composite(this, SWT.NONE);
	    comp_main.setLayout(forml_main);
		
		FormData fd_btn_remove = new FormData();
		fd_btn_remove.left = new FormAttachment(0);
		fd_btn_remove.bottom = new FormAttachment(100);
		fd_btn_remove.width = 120;
		fd_btn_remove.height = 30;
		btn_remove = new Button(comp_main, SWT.NONE);
		btn_remove.setLayoutData(fd_btn_remove);
		btn_remove.setFont(Utility.getFont());
		btn_remove.setText("Remove Selected");

		FormData fd_btn_reloadAndSync = new FormData();
		fd_btn_reloadAndSync.left = new FormAttachment(btn_remove, 10);
		fd_btn_reloadAndSync.bottom = new FormAttachment(100);
		fd_btn_reloadAndSync.width = 120;
		fd_btn_reloadAndSync.height = 30;
		btn_reloadAndSync = new Button(comp_main, SWT.NONE);
		btn_reloadAndSync.setLayoutData(fd_btn_reloadAndSync);
		btn_reloadAndSync.setFont(Utility.getFont());
		btn_reloadAndSync.setText("Reload && Sync");
		
		FormData fd_btn_open = new FormData();
		fd_btn_open.right = new FormAttachment(100);
		fd_btn_open.bottom = new FormAttachment(100);
		fd_btn_open.width = 120;
		fd_btn_open.height = 30;
		btn_open = new Button(comp_main, SWT.NONE);
		btn_open.setLayoutData(fd_btn_open);
		btn_open.setFont(Utility.getFont(SWT.BOLD));
		btn_open.setText("Open");
		
		FormData fd_tbl_files = new FormData();
		fd_tbl_files.left = new FormAttachment(0);
		fd_tbl_files.right = new FormAttachment(100);
		fd_tbl_files.top = new FormAttachment(0);
		fd_tbl_files.bottom = new FormAttachment(btn_open, -10, SWT.TOP);
		tblv_files = new TableViewer(comp_main, SWT.BORDER | SWT.FULL_SELECTION);
		tbl_files = tblv_files.getTable();
		tbl_files.setLayoutData(fd_tbl_files);
		tbl_files.setLinesVisible(true);
		tbl_files.setHeaderVisible(true);
		tbl_files.setFont(Utility.getFont());
		tblc_title = new TableColumn(tbl_files, SWT.LEFT);
		tblc_title.setText("Title");
		tblc_title.setWidth(145);
		tblc_created = new TableColumn(tbl_files, SWT.CENTER);
		tblc_created.setText("Created");
		tblc_created.setWidth(150);
		tblc_version = new TableColumn(tbl_files, SWT.CENTER);
		tblc_version.setText("Version");
		tblc_version.setWidth(80);
		tblc_fileSize = new TableColumn(tbl_files, SWT.RIGHT);
		tblc_fileSize.setText("Size");
		tblc_fileSize.setWidth(95);
		tblc_fileExists = new TableColumn(tbl_files, SWT.CENTER);
		tblc_fileExists.setText("Exists");
		tblc_fileExists.setWidth(60);
		tblc_fileName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_fileName.setText("Filename");
		tblc_fileName.setWidth(235);
		tblc_dirName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_dirName.setText("Directory");
		tblc_dirName.setWidth(175);
		
		droptarget_dbfile = new DropTarget(this, DND.DROP_MOVE | DND.DROP_DEFAULT);
		droptarget_dbfile.setTransfer(Constant.FILE_TRANSFER_TYPE);

		toggleButton();		

	}
		
	protected void addEventListener() {
		
		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				setVisible(false);
				e.doit = false;
			}
		});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for(Process p : runningProcesses) {
					if(p.isAlive()) {
						p.destroy();
					}
				}
			}
		});
		
		tbl_files.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(tbl_files.getSelectionCount() == 1) {
					toggleButton();
				}
			}
		});
		
		tbl_files.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == 127 && tbl_files.getSelectionCount() == 1) {
					if(MessageUtil.showConfirmMessage(instance, "Do you really want to delete the item?")) {
						removeHistory();
					}				
				}
			}
		});

		tblc_title.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(0);
			}
		});

		tblc_created.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(1);
			}
		});

		tblc_version.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(2);
			}
		});

		tblc_fileSize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByNumber(3);
			}
		});
		
		tblc_fileExists.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(4);
			}
		});
		
		tblc_fileName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(5);
			}
		});

		tblc_dirName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(6);
			}
		});
		
		btn_remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(MessageUtil.showConfirmMessage(instance, "Do you really want to delete the item?")) {
					removeHistory();
				}				
			}
		});

		btn_reloadAndSync.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tbl_files.clearAll();
				tblv_files.refresh();
				loadHistoryList(true);
			}
		});

		btn_open.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openHistory();
			}
		});
		
		tbl_files.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				openHistory();
			}
		});

		droptarget_dbfile.addDropListener(new DropTargetListener() {			
			public void drop(DropTargetEvent event) {
				String[] sourceFileList = (String[])event.data;
				TableItem last = null;
				if(sourceFileList != null) {
					for(String sourceFile : sourceFileList) {
						TableItem item = addHistory(sourceFile);
						if(item != null) {
							last = item;
						}
					}
					tbl_files.setSortColumn(null);
					tbl_files.setSortDirection(SWT.NONE);
					if(last != null) {
						tbl_files.showItem(last);
					}
				}
			}
			public void dropAccept(DropTargetEvent event) {}
			public void dragOver(DropTargetEvent event) {}
			public void dragOperationChanged(DropTargetEvent event) {}
			public void dragLeave(DropTargetEvent event) {}
			public void dragEnter(DropTargetEvent event) {}
		});

	}
	
	protected void checkSubclass() {}

	
	private TableItem addHistory(String fileName) {
		String ext = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
		if(ext.equals("adb")) {
			ObjectDBUtil db = null;
			EntityManager em = null;
			try {
				db = new ObjectDBUtil(fileName);
				em = db.createEntityManager();		
			} catch(RuntimeException re) {
				Logger.debug("Failed to open the database : " + re.getMessage());
				Logger.error(re);
				MessageUtil.showErrorMessage(instance, "Failed to open the database.");
				return null;
			}
			try {
				File dbfile = new File(fileName);
				HistoryVO historyVo = new HistoryVO(dbfile.getName(), dbfile.getParent());
				SummaryEntryVO summaryVo = db.select(em, SummaryEntryVO.class);
				historyVo.setCreated(summaryVo.getCreatedTime().getTime());
				historyVo.setVersion(summaryVo.getVersion());
				historyVo.setFileSize(dbfile.length());
				historyVo.setTitle(summaryVo.getTitle());
				return addHistoryItem(historyVo, true);
			} catch(Exception e) {
				Logger.debug("Failed to add an item : " + fileName);
				Logger.error(e);
				MessageUtil.showErrorMessage(instance, "Failed to add an item.");
				return null;
			} finally {
				if(db != null) {
					db.closeAll();
				}
			}
		} else {
			MessageUtil.showErrorMessage(instance, "Only .adb files can be added.");
			return null;
		}
	}
	
	public TableItem addHistoryItem(HistoryVO historyVo, boolean showMessage) throws Exception {
		HistoryManager hm = null;
		try {
			hm = new HistoryManager();
			String key = hm.getHistoryKeyByVO(historyVo);
			if(key != null && hm.getHistoryByKey(key) != null) {
				Logger.debug("Because the item already exists, adding it to the history view is ignored.");
				if(showMessage) {
					MessageUtil.showErrorMessage(instance, "The item already exists.");
				}
				return null;
			}
			boolean added = hm.addHistory(historyVo);
			if(added) {
				TableItem item = new TableItem(tbl_files, SWT.NULL);
				item.setData(historyVo.getKey());
				item.setText(historyVo.getFieldArray());
				if(item.getText(4).equals("X")) {
					item.setForeground(4, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
				}
				return item;
			} else {
				return null;
			}
		} catch(Exception e) {
			throw e;
		} finally {
			if(hm != null) {
				try { hm.close(); } catch(Exception ex) {}
			}
		}
	}
	
	private void removeHistory() {
		int idx = tbl_files.getSelectionIndex();
		TableItem item = tbl_files.getItem(idx);
		boolean deleteFile = "O".equals(item.getText(4)) && MessageUtil.showYesNoMessage(instance, "Do you want to delete the DB file together?");
		String key = (String)item.getData();
		HistoryManager hm = null;
		try {
			hm = new HistoryManager();
			hm.deleteHistoryByKey(key);
		} catch(Exception ex) {
			Logger.debug("Failed to remove the history item : " + ex.getMessage());
			Logger.error(ex);
			MessageUtil.showErrorMessage(instance, "Failed to remove the history item.");
		} finally {
			if(hm != null) {
				try { hm.close(); } catch(Exception ex) {}
			}
		}
		try {
			if(deleteFile) {
				String filepath = item.getText(6) + File.separator + item.getText(5);
				new File(filepath).delete();
			}					
		} catch(Exception ex) {
			Logger.debug("Failed to delete the DB file : " + ex.getMessage());
			Logger.error(ex);
			MessageUtil.showErrorMessage(instance, "Failed to delete the DB file.");
		}
		item.dispose();
		toggleButton();
	}
	
	private void openHistory() {
		int idx = tbl_files.getSelectionIndex();
		TableItem item = tbl_files.getItem(idx);
		if("O".equals(item.getText(4))) {
			String filepath = item.getText(6) + File.separator + item.getText(5);
			try {
				resultAnalyzer.loadDBFile(filepath, false);
				resultAnalyzer.forceActive();
			} catch(Exception ex) {
				Logger.debug("Failed to load the database : " + filepath);
				Logger.error(ex);
				MessageUtil.showErrorMessage(instance, "Failed to load the database.");
				resultAnalyzer.setVisible(false);
			}
		} else {
			if(MessageUtil.showConfirmMessage(instance, "DB File does not exists. Do you want to delete the item?")) {
				removeHistory();
			}				
		}
	}

	public void toggleButton() {
		boolean flag = false;
		int idx = tbl_files.getSelectionIndex();
		if(idx > -1 && "O".equals(tbl_files.getItem(idx).getText(4))) {
			flag = true;
		}
		btn_open.setEnabled(flag);
		if(flag) {
			btn_open.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		} else {
			btn_open.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		}
		btn_remove.setEnabled(idx>-1);
	}
	
	public boolean hasLoaded() {
		return loaded;
	}

	public void loadHistoryList() {
		loadHistoryList(false);
	}

	public void loadHistoryList(boolean sync) {
		HistoryManager hm = null;
		try {
			hm = new HistoryManager();
			List<HistoryVO> list = hm.getAllHistories();
			if(sync) {
				for(int idx = list.size()-1; idx >= 0; idx--) {
					HistoryVO vo = list.get(idx);
					if(!vo.getFileExists()) {
						list.remove(idx);
						hm.deleteHistory(vo);
					}
				}
			}
			Collections.sort(list, new Comparator<HistoryVO>() {
				@Override
				public int compare(HistoryVO o1, HistoryVO o2) {
					return Long.compare(o1.getCreated(), o2.getCreated());
				}
			});			
			for(HistoryVO vo : list) {
				TableItem item = new TableItem(tbl_files, SWT.NULL);
				item.setData(vo.getKey());
				item.setText(vo.getFieldArray());
				if(item.getText(4).equals("X")) {
					item.setForeground(4, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
				}
			}
			loaded = true;
		} catch(Exception e) {
			Logger.debug("Failed to load the history : " + e.getMessage());
			Logger.error(e);
			MessageUtil.showErrorMessage(instance, "Failed to load the history.");
		} finally {
			if(hm != null) {
				try { hm.close(); } catch(Exception e) {}
			}
		}
		tbl_files.getColumn(1).setData("asc", true);
		tbl_files.setSortColumn(tbl_files.getColumn(1));
		tbl_files.setSortDirection(SWT.UP);
		toggleButton();
	}

	protected void sortTableDataByString(int fieldIdx) {
		TableColumn column = tbl_files.getColumn(fieldIdx);
		Object obj = column.getData("asc");
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
					String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5), items[i].getText(6) };
					String key = (String)items[i].getData();
					items[i].dispose();
					TableItem item = new TableItem(tbl_files, SWT.NULL, j);
					item.setText(values);
					item.setData(key);
					if(item.getText(4).equals("X")) {
						item.setForeground(4, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
					}
					items = tbl_files.getItems();
					break;
				}
			}
		}		
		column.setData("asc", isAsc);
		if(column.equals(tbl_files.getSortColumn())) {
			tbl_files.setSortDirection(tbl_files.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
		} else {
			tbl_files.setSortColumn(column);
			tbl_files.setSortDirection(SWT.UP);
		}
		toggleButton();
	}

	protected void sortTableDataByNumber(int fieldIdx) {
		TableColumn column = tbl_files.getColumn(fieldIdx);
		Object obj = column.getData("asc");
		boolean isAsc = (obj == null) ? true : !(Boolean)obj;
		TableItem[] items = tbl_files.getItems();
		long value1;
		long value2;
		for(int i = 1; i < items.length; i++) {
			value1 = Long.parseLong(items[i].getText(fieldIdx));
			for(int j = 0; j < i; j++) {
				value2 = Long.parseLong(items[j].getText(fieldIdx));
				if((isAsc && (value1 < value2)) || (!isAsc && (value1 > value2))) {
					String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5), items[i].getText(6) };
					String key = (String)items[i].getData();
					items[i].dispose();
					TableItem item = new TableItem(tbl_files, SWT.NULL, j);
					item.setText(values);
					item.setData(key);
					if(item.getText(4).equals("X")) {
						item.setForeground(4, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
					}
					items = tbl_files.getItems();
					break;
				}
			}
		}
		column.setData("asc", isAsc);
		if(column.equals(tbl_files.getSortColumn())) {
			tbl_files.setSortDirection(tbl_files.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
		} else {
			tbl_files.setSortColumn(column);
			tbl_files.setSortDirection(SWT.UP);
		}
		toggleButton();
	}

}
