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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.history.HistoryManager;
import dal.tool.analyzer.alyba.ui.history.HistoryVO;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class HistoryView extends Shell {

	private HistoryView instance;
	private Composite comp_main;
	private int table_width;
	private boolean loaded;
	private List<Process> runningProcesses; 

	private TableViewer tblv_files;
	private Table tbl_files;
	private TableColumn tblc_title;
	private TableColumn tblc_created;
	private TableColumn tblc_fileSize;
	private TableColumn tblc_fileExists;
	private TableColumn tblc_fileName;
	private TableColumn tblc_dirName;
	private Button btn_open;
	private Button btn_remove;
	private Button btn_reloadAndSync;
	private DropTarget droptarget_dbfile;
	
	
	public HistoryView(Display display, int style) {
		super(display, style);
		createContents();
		addEventListener();
		open();
		layout(true, true);
		runningProcesses = new ArrayList<Process>();
	}
	
	protected void createContents() {
		
		instance = this;
		
		setSize(900, 300);
		setMinimumSize(900, 300);
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
		tblc_created = new TableColumn(tbl_files, SWT.CENTER);
		tblc_created.setText("Created");
		tblc_fileSize = new TableColumn(tbl_files, SWT.RIGHT);
		tblc_fileSize.setText("Size");
		tblc_fileExists = new TableColumn(tbl_files, SWT.CENTER);
		tblc_fileExists.setText("Exists");
		tblc_fileName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_fileName.setText("Filename");
		tblc_dirName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_dirName.setText("Directory");

		droptarget_dbfile = new DropTarget(this, DND.DROP_MOVE | DND.DROP_DEFAULT);
		droptarget_dbfile.setTransfer(Constant.FILE_TRANSFER_TYPE);

		toggleButton();		

	}
		
	protected void addEventListener() {
		
		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				if(AlybaGUI.instance != null) {
					AlybaGUI.instance.historyView.setVisible(false);
					e.doit = false;
				}
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
		
		comp_main.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				resizeTableColumn();
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
					removeHistory();
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

		tblc_fileSize.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByNumber(2);
			}
		});
		
		tblc_fileExists.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(3);
			}
		});
		
		tblc_fileName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(4);
			}
		});

		tblc_dirName.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sortTableDataByString(5);
			}
		});
		
		btn_remove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeHistory();
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
				historyVo.setFileSize(dbfile.length());
				historyVo.setTitle(summaryVo.getTitle());
				return addHistoryItem(historyVo);
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
	
	public TableItem addHistoryItem(HistoryVO historyVo) throws Exception {
		HistoryManager hm = null;
		try {
			hm = new HistoryManager();
			if(hm.getHistory(historyVo.getKey()) != null) {
				MessageUtil.showErrorMessage(instance, "The item already exists.");
				return null;
			}
			hm.addHistory(historyVo);
			TableItem item = new TableItem(tbl_files, SWT.NULL);
			item.setData(historyVo.getKey());
			item.setText(historyVo.getFieldArray());
			if(item.getText(3).equals("X")) {
				item.setForeground(3, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
			}
			return item;
		} catch(Exception e) {
			throw e;
		} finally {
			if(hm != null) {
				try { hm.close(); } catch(Exception ex) {}
			}
		}
	}
	
	private void removeHistory() {
		if(!MessageUtil.showConfirmMessage(instance, "Do you really want to delete the item?")) {
			return;
		}
		int idx = tbl_files.getSelectionIndex();
		TableItem item = tbl_files.getItem(idx);
		boolean deleteFile = "O".equals(item.getText(3)) && MessageUtil.showYesNoMessage(instance, "Do you want to delete the DB file together?");
		String key = (String)item.getData();
		HistoryManager hm = null;
		try {
			hm = new HistoryManager();
			hm.deleteHistory(key);
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
				String filepath = item.getText(5) + File.separator + item.getText(4);
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
		String filepath = item.getText(5) + File.separator + item.getText(4);
		ResultAnalyzer resultAnalyzer = AlybaGUI.instance.openResultAnalyzer();
		try {
			resultAnalyzer.loadDBFile(filepath);
			resultAnalyzer.forceActive();
		} catch(Exception ex) {
			Logger.debug("Failed to load the database : " + filepath);
			Logger.error(ex);
			MessageUtil.showErrorMessage(instance, "Failed to load the database.");
			resultAnalyzer.setVisible(false);
		}
	}

	private void resizeTableColumn() {
		Rectangle area = comp_main.getClientArea();
		int width = area.width - 2*tbl_files.getBorderWidth() - 40;
		if(table_width > 0 && tbl_files.getVerticalBar().getVisible()) {
			width -= tbl_files.getVerticalBar().getSize().x;
		}
		int size_title = 150;
		int size_created = 150;
		int size_filesize = 100;
		int size_exists = 60;
		int size_filename = 150;
		if(table_width > 0) {
			size_title = (int)(width*(tblc_title.getWidth()/(float)table_width));
			size_created = (int)(width*(tblc_created.getWidth()/(float)table_width));
			size_filesize = (int)(width*(tblc_fileSize.getWidth()/(float)table_width));
			size_exists = (int)(width*(tblc_fileExists.getWidth()/(float)table_width));
			size_filename = (int)(width*(tblc_fileName.getWidth()/(float)table_width));
		}
		tblc_title.setWidth(size_title);
		tblc_created.setWidth(size_created);
		tblc_fileSize.setWidth(size_filesize);
		tblc_fileExists.setWidth(size_exists);
		tblc_fileName.setWidth(size_filename);
		tblc_dirName.setWidth(width -(size_title+size_created+size_filesize+size_exists+size_filename));
		table_width = width;
	}
	
	public void toggleButton() {
		boolean flag = false;
		int idx = tbl_files.getSelectionIndex();
		if(idx > -1 && "O".equals(tbl_files.getItem(idx).getText(3))) {
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
				if(item.getText(3).equals("X")) {
					item.setForeground(3, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
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
		resizeTableColumn();
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
					String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5) };
					String key = (String)items[i].getData();
					items[i].dispose();
					TableItem item = new TableItem(tbl_files, SWT.NULL, j);
					item.setText(values);
					item.setData(key);
					if(item.getText(3).equals("X")) {
						item.setForeground(3, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
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
					String[] values = { items[i].getText(0), items[i].getText(1), items[i].getText(2), items[i].getText(3), items[i].getText(4), items[i].getText(5) };
					String key = (String)items[i].getData();
					items[i].dispose();
					TableItem item = new TableItem(tbl_files, SWT.NULL, j);
					item.setText(values);
					item.setData(key);
					if(item.getText(3).equals("X")) {
						item.setForeground(3, instance.getDisplay().getSystemColor(SWT.COLOR_RED));
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
