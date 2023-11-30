package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.LogAnalyzeOutput;
import dal.tool.analyzer.alyba.parse.parser.TransactionCountParser;
import dal.tool.analyzer.alyba.parse.task.TPMAnalyzeTask;
import dal.tool.analyzer.alyba.setting.TPMAnalyzerSetting;
import dal.tool.analyzer.alyba.setting.TPMFieldMappingInfo;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.FileUtil;
import dal.util.StringUtil;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.ProgressBarDialog;
import dal.util.swt.SWTResourceManager;

public class TPMInsert extends Composite {

	private Composite comp_main;	

	public TableViewer tblv_files;
	public Table tbl_files;
	private TableColumn tblc_index;
	private TableColumn tblc_dirName;
	private TableColumn tblc_fileName;
	private TableColumn tblc_fileSize;
	private Button btn_file_open;
	private Button btn_file_remove;
	private Button btn_file_removeAll;
	private Button btn_analyzing;

	private TPMAnalyzer tpmAnalyzer;
	private TPMFieldMapping fieldMapping;

	public TPMInsert(Composite parent, int style, TPMAnalyzer owner) {
		super(parent, style);
		this.tpmAnalyzer = owner;
		createContents();
		addEventListener();
	}

	protected void createContents() {
		
	    setLayout(new FillLayout());

		FormLayout forml_main = new FormLayout();
		forml_main.marginHeight = 20;
		forml_main.marginWidth = 20;
	    comp_main = new Composite(this, SWT.NONE);
	    comp_main.setLayout(forml_main);
		
		FormData fd_tbl_files = new FormData();
		fd_tbl_files.left = new FormAttachment(0);
		fd_tbl_files.right = new FormAttachment(100);
		fd_tbl_files.top = new FormAttachment(0);
		fd_tbl_files.height = 140;
		tblv_files = new TableViewer(comp_main, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
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

		FormData fd_btn_file_open = new FormData();
		fd_btn_file_open.top = new FormAttachment(tbl_files, 10);
		fd_btn_file_open.width = 160;
		fd_btn_file_open.height = 52;
	    btn_file_open = new Button(comp_main, SWT.NONE);
	    btn_file_open.setLayoutData(fd_btn_file_open);
		btn_file_open.setFont(Utility.getFont());
		btn_file_open.setText("Open File(s)");

		FormData fd_btn_file_remove = new FormData();
		fd_btn_file_remove.top = new FormAttachment(tbl_files, 10);
		fd_btn_file_remove.left = new FormAttachment(btn_file_open, 30);
		fd_btn_file_remove.width = 130;
		fd_btn_file_remove.height = 23;
		btn_file_remove = new Button(comp_main, SWT.NONE);
		btn_file_remove.setLayoutData(fd_btn_file_remove);
		btn_file_remove.setFont(Utility.getFont());
		btn_file_remove.setText("Remove Selected");
		
		FormData fd_btn_file_removeAll = new FormData();
		fd_btn_file_removeAll.bottom = new FormAttachment(btn_file_open, 0, SWT.BOTTOM);
		fd_btn_file_removeAll.left = new FormAttachment(btn_file_open, 30);
		fd_btn_file_removeAll.width = 130;
		fd_btn_file_removeAll.height = 23;
		btn_file_removeAll = new Button(comp_main, SWT.NONE);
		btn_file_removeAll.setLayoutData(fd_btn_file_removeAll);
		btn_file_removeAll.setFont(Utility.getFont());
		btn_file_removeAll.setText("Remove All");
		
		FormData fd_btn_analyzing = new FormData();
		fd_btn_analyzing.right = new FormAttachment(tbl_files, 0, SWT.RIGHT);
		fd_btn_analyzing.top = new FormAttachment(tbl_files, 10);
		fd_btn_analyzing.width = 160;
		fd_btn_analyzing.height = 52;
		btn_analyzing = new Button(comp_main, SWT.NONE);
		btn_analyzing.setLayoutData(fd_btn_analyzing);
		btn_analyzing.setFont(Utility.getFont(SWT.BOLD));
		btn_analyzing.setText("Start Analyzing");
		toggleAnalyzingButton(false);

		FormData fd_hline_middle = new FormData();
		fd_hline_middle.left = new FormAttachment(tbl_files, 0, SWT.LEFT);
		fd_hline_middle.right = new FormAttachment(tbl_files, 0, SWT.RIGHT);
		fd_hline_middle.top = new FormAttachment(btn_analyzing, 10);
		fd_hline_middle.height = 10;
		Label hline_middle = new Label(comp_main, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_middle.setLayoutData(fd_hline_middle);
		
		FormData fd_fieldMapping = new FormData();
		fd_fieldMapping.left = new FormAttachment(0);
		fd_fieldMapping.right = new FormAttachment(100);
		fd_fieldMapping.top = new FormAttachment(hline_middle, 5);
		fd_fieldMapping.bottom = new FormAttachment(100);
		fieldMapping = new TPMFieldMapping(comp_main, SWT.NONE, this);
		fieldMapping.setLayoutData(fd_fieldMapping);	
		
	}

	protected void addEventListener() {

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
		
		btn_analyzing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				executeAnalyze();
			}
		});

	}

	public void resetData() {
		tbl_files.removeAll();
		tblc_index.setWidth(35);
		tblc_dirName.setWidth(390);
		tblc_fileName.setWidth(160);
		tblc_fileSize.setWidth(90);
		fieldMapping.reset();
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
		int from_idx = tbl_files.getItemCount();
		int cnt = 0;
		for(int i = 0; i < list.size(); i++) {
			File f = (File)list.get(i);
			try {
				int idx = existsTableItem(table, f);
				if(idx > -1) {
					table.getItem(idx).setText(3, FileUtil.getFileSizeString(f));
					table.getItem(idx).setData("file", f);
					Logger.debug("Duplicated Item(size updated) : " + f.getCanonicalPath());
				} else {
					String fullpath = f.getCanonicalPath();
					int lastidx = fullpath.lastIndexOf(File.separatorChar);
					TableItem item = new TableItem(table, SWT.NULL);
					item.setText(0, String.valueOf(table.getItemCount()));
					item.setText(1, fullpath.substring(0, lastidx));
					item.setText(2, fullpath.substring(lastidx + 1));
					item.setText(3, FileUtil.getFileSizeString(f));
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
		tbl_files.getColumn(3).setData("asc", isAsc);
	}

	protected void executeAnalyze() {
		int idx = fieldMapping.checkMappingValidation();
		if(idx > -1) {
			MessageUtil.showInfoMessage(getShell(), "Invalid format of file has been detected.\nCheck the file No." + (idx + 1));
			return;
		}

		TPMAnalyzerSetting setting = getAnalyzerSetting();
		setting.setAnalyzeDate(new Date());
		Logger.debug(setting.toString());
		
		if(!MessageUtil.showConfirmMessage(getShell(), "Do you want to analyze the files with current mapping?")) {
			return;
		}

		String output_dir = (AlybaGUI.instance != null) ? AlybaGUI.getInstance().outputSetting.getOutputDirectory() : Constant.OUTPUT_DEFAULT_DIRECTORY;
		String output_filename = LogAnalyzeOutput.getFileName(tpmAnalyzer.getTitle());
		String dbfile_path = output_dir + File.separatorChar + output_filename + ".adb";
		AlybaGUI.inProgressDbUtil = new ObjectDBUtil(dbfile_path, true);
		Logger.debug("Opened the database : dbfile=" + dbfile_path);
		
		ProgressBarDialog progressBar = new ProgressBarDialog(getShell(), Utility.getFont());
		progressBar.setTitle("Analyzer Progress");
		progressBar.setDetailViewCount(tbl_files.getItemCount());
		TPMAnalyzeTask task = new TPMAnalyzeTask(setting, TransactionCountParser.class);
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

		try {
			Logger.debug("Closing the database : dbfile=" + dbfile_path);
			if(task.isSuccessed()) {
				AlybaGUI.inProgressDbUtil.closeAll();
			} else {
				AlybaGUI.inProgressDbUtil.closeAndDeleteDB();
			}
		} catch(Exception e) {
			Logger.debug("Failed to close the database.");
			Logger.error(e);
		} finally {
			AlybaGUI.inProgressDbUtil = null;
		}

		if(task.isSuccessed()) {
			if(AlybaGUI.instance != null) {
				boolean loadAnalyzer = false;
				loadAnalyzer = MessageUtil.showConfirmMessage(getShell(), "Transaction count data has been successfully inserted to the new database.\n\n"
						+ "Do you want to load the database into the result analyzer?");
				try {
					if(loadAnalyzer) {
						AlybaGUI.instance.openResultAnalyzer();
						AlybaGUI.instance.resultAnalyzer.loadDBFile(dbfile_path);
					}
				} catch(Exception e) {
					Logger.debug("Failed to load the database : " + dbfile_path);
					Logger.error(e);
					MessageUtil.showErrorMessage(getShell(), "Failed to load the database.");
					AlybaGUI.instance.resultAnalyzer.setVisible(false);
				}
			} else {
				MessageUtil.showInfoMessage(getShell(), "Transaction Count data has been successfully inserted to the new database.\n\n" + dbfile_path);				
			}
		}

		task = null;
	}
	
	private TPMAnalyzerSetting getAnalyzerSetting() {
		TPMFieldMappingInfo fieldMappingInfo = new TPMFieldMappingInfo();
		fieldMappingInfo.setLogType(fieldMapping.getFileType());
		fieldMappingInfo.setFieldDelimeter(StringUtil.replaceMetaCharacter(fieldMapping.getDelimeter(), false));
		fieldMappingInfo.setFieldBracelet(fieldMapping.getBracelet());
		fieldMappingInfo.setMappingInfo(fieldMapping.getMappingData());
		fieldMappingInfo.setOffsetHour(Float.parseFloat(fieldMapping.getOffsetHour()));
		fieldMappingInfo.setTimeFormat(fieldMapping.getTimeFormat());
		fieldMappingInfo.setTimeLocale(fieldMapping.getTimeLocale());
		fieldMappingInfo.setFieldCount(fieldMapping.getFieldCount());
		fieldMappingInfo.setCountUnit(Integer.parseInt(fieldMapping.getCountUnit()));
		fieldMappingInfo.setTPS(fieldMapping.isTPS());

		TPMAnalyzerSetting setting = new TPMAnalyzerSetting();
		setting.setTitle(tpmAnalyzer.getTitle());
		setting.setAnalyzerTimezone(Constant.TIMEZONE_DEFAULT);
		setting.setFieldMapping(fieldMappingInfo);
		List<String> fileList = new ArrayList<String>();
		for(TableItem item : tbl_files.getItems()) {
			fileList.add(((File)item.getData("file")).getAbsolutePath());
		}		
		setting.setLogFileList(fileList);
		
		return setting;
	}

}
