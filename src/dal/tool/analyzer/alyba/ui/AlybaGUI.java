package dal.tool.analyzer.alyba.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.AnalyzeOutput;
import dal.tool.analyzer.alyba.parser.DefaultParser;
import dal.tool.analyzer.alyba.parser.ParserUtil;
import dal.tool.analyzer.alyba.parser.PostParser;
import dal.tool.analyzer.alyba.setting.AnalyzerSetting;
import dal.tool.analyzer.alyba.setting.FieldMappingInfo;
import dal.tool.analyzer.alyba.setting.FilterSettingInfo;
import dal.tool.analyzer.alyba.task.AnalyzeTask;
import dal.tool.analyzer.alyba.task.OutputTask;
import dal.tool.analyzer.alyba.ui.comp.ContentView;
import dal.tool.analyzer.alyba.ui.comp.DebugConsole;
import dal.tool.analyzer.alyba.ui.comp.FieldMapping;
import dal.tool.analyzer.alyba.ui.comp.FilterSetting;
import dal.tool.analyzer.alyba.ui.comp.OptionSetting;
import dal.tool.analyzer.alyba.ui.comp.OutputSetting;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.DateUtil;
import dal.util.FileUtil;
import dal.util.LoggingUtil;
import dal.util.NumberUtil;
import dal.util.StringUtil;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.HeapStatus;
import dal.util.swt.ImageUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.ProgressBarDialog;
import dal.util.swt.SWTResourceManager;

public class AlybaGUI {

	public static final TimeZone TIMEZONE = TimeZone.getDefault();

	public static AlybaGUI instance = null;
	public static boolean debugMode = false;
	public DebugConsole console = null;
	public ResultAnalyzer resultAnalyzer = null;

	public Display display;
	public Shell shell;
	public Text txt_title;
	public Table tbl_files;
	public Button btn_analyzing;
	public Button btn_resultAnalyzer;
	public FieldMapping fieldMapping;
	public FilterSetting filterSetting;
	public OptionSetting optionSetting;
	public OutputSetting outputSetting;

	private TableViewer tblv_files;
	private Button btn_resetAll;
	private Button btn_openFiles;
	private Button btn_removeFiles;
	private Button btn_removeAll;
	private TabFolder tbf_setting;
	private TableColumn tblc_index;
	private TableColumn tblc_dirName;
	private TableColumn tblc_fileName;
	private TableColumn tblc_fileSize;
	private TabItem tbi_mapping;
	private TabItem tbi_filter;
	private TabItem tbi_option;
	private TabItem tbi_output;
	private Button btn_console;
	private HeapStatus heapstatus;
	private int last_help;

	public static void main(String[] args) {
		try {
			boolean isDebug = false;
			boolean isResultAnalyzer = false;
			String dbFileName = null;
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("-result")) {
					isResultAnalyzer = true;
					if(args.length > 2) {
						System.out.println("Too many arguments.");
						printUsage();
						return;					
					} else if(args.length == 2) {
						dbFileName = args[1];
						if(!(new File(dbFileName).exists())) {
							System.out.println("File doesn't exists.");
							return;
						}						
					}
				} else if(args.length > 1) {
					System.out.println("Too many arguments.");
					printUsage();
					return;										
				} else {
					if(args[0].equalsIgnoreCase("-help")) {
						System.out.println("ALYBA (AccessLog & Your Bad Application)");
						printUsage();
						return;					
					} else if(args[0].equalsIgnoreCase("-version")) {
						System.out.println(Constant.PROGRAM_VERSION);
						return;
					} else if(args[0].equalsIgnoreCase("-debug")) {
						System.out.println("Runnig as debug mode.");
						isDebug = true;
					} else {
						System.out.println("Invalid option.");
						printUsage();
						return;					
					}
				}
			}

			TimeZone.setDefault(Constant.TIMEZONE_UTC);
			Display display = null;
			Shell shell = null;
			if(isResultAnalyzer) {
		        display = new Display();
		        shell = new ResultAnalyzer(display, SWT.SHELL_TRIM, dbFileName);
			} else {
				debugMode = isDebug;
				instance = getInstance();
				@SuppressWarnings("unused")
				Splash splashWindow = new Splash(instance.display);
				display = instance.display;
				shell = instance.shell;
			}
			
			boolean isRead = false;
			while(!shell.isDisposed()) {
				try {
					isRead = display.readAndDispatch();
				} catch(Throwable t) {
					if(instance != null) {
						instance.debug(t);
					}
					isRead = false;
				}
				if(!isRead) {
					display.sleep();
				}
			}
			display.dispose();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		System.exit(0);
	}

	public static AlybaGUI getInstance() {
		if(instance == null) {
            synchronized(AlybaGUI.class) {
                if(instance == null) {
                	instance  = new AlybaGUI(debugMode);
                }
            }
		}
		return instance;
	}
	
	public AlybaGUI(boolean debugMode) {
		this.display = new Display();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void init() {
		createContents();
		addEventListener();
		shell.open();
		shell.layout();
	}

	protected void createContents() {

		shell = new Shell(display, SWT.SHELL_TRIM & ~SWT.RESIZE & ~SWT.MAX);
		shell.setSize(740, 720);
		Rectangle dispRect = display.getMonitors()[0].getBounds();
		Rectangle shellRect = shell.getBounds();
		shell.setLocation((dispRect.width - shellRect.width) / 2, (dispRect.height - shellRect.height) / 2);
		shell.setText("ALYBA " + Constant.PROGRAM_VERSION);
		shell.setImage(ImageUtil.getImage(Constant.IMAGE_PATH_TRAYICON));
		shell.forceActive();


		Label lb_title = new Label(shell, SWT.NONE);
		lb_title.setAlignment(SWT.CENTER);
		lb_title.setFont(Utility.getFont());
		lb_title.setText("Title");
		lb_title.setBounds(10, 12, 40, 15);

		txt_title = new Text(shell, SWT.BORDER);
		txt_title.setBounds(56, 10, 300, 19);
		txt_title.setFont(Utility.getFont());
		txt_title.setText(Constant.OUTPUT_DEFAULT_TITLE);

		btn_console = new Button(shell, SWT.NONE);
		btn_console.setFont(Utility.getFont());
		btn_console.setText("Hide Console");
		btn_console.setBounds(510, 8, 100, 23);
		if(debugMode) {
			console = new DebugConsole(display, SWT.SHELL_TRIM);
			console.setLocation(0, 0);
			btn_console.setVisible(true);
		} else {
			btn_console.setVisible(false);
		}

		btn_resetAll = new Button(shell, SWT.NONE);
		btn_resetAll.setFont(Utility.getFont());
		btn_resetAll.setText("Reset All");
		btn_resetAll.setBounds(624, 8, 100, 23);

		Label hline_top = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_top.setLocation(10, 35);
		hline_top.setSize(714, 10);

		tblv_files = new TableViewer(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tbl_files = tblv_files.getTable();
		tbl_files.setLinesVisible(true);
		tbl_files.setHeaderVisible(true);
		tbl_files.setBounds(10, 46, 714, 175);
		tbl_files.setFont(Utility.getFont());
		tblc_index = new TableColumn(tbl_files, SWT.CENTER);
		tblc_index.setText("No.");
		tblc_index.setWidth(35);
		tblc_dirName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_dirName.setText("Directory");
		tblc_dirName.setWidth(390);
		tblc_fileName = new TableColumn(tbl_files, SWT.LEFT);
		tblc_fileName.setText("Filename");
		tblc_fileName.setWidth(175);
		tblc_fileSize = new TableColumn(tbl_files, SWT.RIGHT);
		tblc_fileSize.setText("Size");
		tblc_fileSize.setWidth(90);

		btn_openFiles = new Button(shell, SWT.NONE);
		btn_openFiles.setFont(Utility.getFont());
		btn_openFiles.setText("Open File(s)");
		btn_openFiles.setBounds(10, 227, 160, 52);

		btn_removeFiles = new Button(shell, SWT.NONE);
		btn_removeFiles.setFont(Utility.getFont());
		btn_removeFiles.setText("Remove Selected");
		btn_removeFiles.setBounds(195, 227, 130, 23);

		btn_removeAll = new Button(shell, SWT.NONE);
		btn_removeAll.setBounds(195, 256, 130, 23);
		btn_removeAll.setFont(Utility.getFont());
		btn_removeAll.setText("Remove All");

		btn_analyzing = new Button(shell, SWT.NONE);
		btn_analyzing.setFont(Utility.getFont(SWT.BOLD));
		btn_analyzing.setText("Start Analyzing");
		btn_analyzing.setBounds(564, 227, 160, 52);
		toggleAnalyzingButton(false);

		btn_resultAnalyzer = new Button(shell, SWT.NONE);
		btn_resultAnalyzer.setFont(Utility.getFont(SWT.BOLD));
		btn_resultAnalyzer.setText("Result Analyzer");
		btn_resultAnalyzer.setBounds(386, 227, 160, 52);

		Label hline_middle = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_middle.setBounds(10, 285, 714, 10);

		tbf_setting = new TabFolder(shell, SWT.NONE);
		tbf_setting.setFont(Utility.getFont());
		tbf_setting.setBounds(10, 300, 714, 365);

		tbi_mapping = new TabItem(tbf_setting, SWT.NONE);
		tbi_mapping.setText("Mapping");
		fieldMapping = new FieldMapping(tbf_setting, SWT.NONE);
		fieldMapping.setEnabled(false);
		tbi_mapping.setControl(fieldMapping);

		tbi_filter = new TabItem(tbf_setting, SWT.NONE);
		tbi_filter.setText("Filter");
		filterSetting = new FilterSetting(tbf_setting, SWT.NONE);
		filterSetting.setEnabled(true);
		fieldMapping.setFilterSetting(filterSetting);
		tbi_filter.setControl(filterSetting);

		tbi_output = new TabItem(tbf_setting, SWT.NONE);
		tbi_output.setText("Output");
		outputSetting = new OutputSetting(tbf_setting, SWT.NONE);
		outputSetting.setEnabled(true);
		tbi_output.setControl(outputSetting);

		tbi_option = new TabItem(tbf_setting, SWT.NONE);
		tbi_option.setText("Option");
		optionSetting = new OptionSetting(tbf_setting, SWT.NONE);
		optionSetting.setEnabled(true);
		tbi_option.setControl(optionSetting);
		
		/* TODO: History*/
		
		heapstatus = new HeapStatus(shell, 500, false, ImageUtil.getImageDescriptorFromURL(Constant.IMAGE_PATH_TRASH));
		heapstatus.setFont(Utility.getFont());
		heapstatus.setBounds(570, 668, 150, 18);
		
		resetSetting();
	}

	protected void addEventListener() {

		shell.addHelpListener(new HelpListener() {
			public void helpRequested(HelpEvent e) {
				if(e.time > last_help) {
					StringBuffer msg = new StringBuffer();
					msg.append("ALYBA (AccessLog & Your Bad Applications)\n");
					msg.append("\n");
					msg.append("Version: " + Constant.PROGRAM_VERSION + "\n");
					msg.append("\n");
					msg.append("Copyright 2012-2019 Youngdal,Kwon. All rights reserved.");
					MessageUtil.showInfoMessage(shell, msg.toString());
					last_help = OS.GetMessageTime();
				}
			}
		});

		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				shell.setMinimized(false);
				shell.forceActive();
				e.doit = MessageUtil.showConfirmMessage(shell, "Do you really want to exit?");
				if(e.doit) {
					if(ObjectDBUtil.isRegistered()) {
						ObjectDBUtil.getInstance().closeAll();
					}
				}
			}
		});

		if(debugMode) {
			btn_console.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					toggleDebugConsole();
				}
			});
		}

		btn_resetAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetAll();
			}
		});

		btn_openFiles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				List<File> files = FileDialogUtil.openReadDialogFiles(shell, Constant.LOG_FILTER_NAMES, Constant.LOG_FILTER_EXTS, Constant.DIALOG_INIT_PATH);
				addTableItems(tbl_files, files);
			}
		});

		btn_removeFiles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeFiles();
			}
		});

		btn_removeAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int cnt = tbl_files.getItemCount();
				if(cnt > 0) {
					tbl_files.removeAll();
					debug("Removed " + cnt + " item(s).\n");
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
					int i = MessageUtil.showSelectMessage(shell, "Question", "What do you want to do?", labels);
					switch(i) {
						case 0:
							List<String> headers = ParserUtil.getHeaders(f);
							if(headers == null || headers.size() == 0) {
								MessageUtil.showInfoMessage(shell, "No header in the file.");
							} else {
								String header = null;
								for(int idx = 0; idx < headers.size(); idx++) {
									String s = (String)headers.get(idx);
									if(s.startsWith("#Fields:") || s.startsWith("format=")) {
										header = s;
										break;
									}
								}
								ContentView view = new ContentView(display);
								view.setText("Header View - " + f.getName());
								view.addLines(headers);
								if(header != null) {
									header = StringUtil.removePrefix(header, "#Fields:");
									header = StringUtil.removePrefix(header, "format=");
									String delimeter = StringUtil.replaceMetaCharacter(fieldMapping.getDelimeter(), false);
									String[] bracelets = StringUtil.getArrayFromString(fieldMapping.getBracelet(), " ");
									String[] fld_arr = ParserUtil.getTokenList(header, delimeter, bracelets).toArray(new String[0]);
									for(int idx = 0; idx < fld_arr.length; idx++) {
										fld_arr[idx] = "Field-" + NumberUtil.getTwoDigitNumber(idx + 1) + " : " + fld_arr[idx];
									}
									List<String> fldList = Arrays.asList(fld_arr);
									view.addLine("");
									view.addLines(fldList);
								}
								view.autoSize();
								dal.util.swt.Utility.centralize(view, shell);
							}
							break;
						case 1:
							Program.launch(f.getAbsolutePath());
							break;
					}
				}
			}
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

		btn_resultAnalyzer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openResultAnalyzer();
			}
		});

		tblv_files.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, Constant.FILE_TRANSFER_TYPE, new DropTargetListener() {
			public void drop(DropTargetEvent event) {
				String[] sourceFileList = (String[])event.data;
				String filename_pattern = null;
				if(event.detail == 1) {
					InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Input", "Enter pattern of filenames (* and ? characters are available)", "*", new IInputValidator() {
						public String isValid(String s) {
							if(s.length() < 1)
								return "Too short";
							return null;
						}
					});
					if(dlg.open() == Window.OK) {
						filename_pattern = dlg.getValue();
					} else {
						return;
					}
				}
				addTableItems(sourceFileList, filename_pattern);
			}
			public void dropAccept(DropTargetEvent event) {}
			public void dragOver(DropTargetEvent event) {}
			public void dragOperationChanged(DropTargetEvent event) {}
			public void dragLeave(DropTargetEvent event) {}
			public void dragEnter(DropTargetEvent event) {}
		});

		DropTarget confDropTarget = new DropTarget(shell, DND.DROP_MOVE | DND.DROP_COPY);
		confDropTarget.setTransfer(Constant.FILE_TRANSFER_TYPE);
		confDropTarget.addDropListener(new DropTargetListener() {
			public void drop(DropTargetEvent event) {
				String[] sourceFileList = (String[])event.data;
				if(sourceFileList.length > 1) {
					MessageUtil.showErrorMessage(shell, "More than one file is not allowed.");					
				} else {
					loadSetting(new File(sourceFileList[0]));
				}
			}
			public void dropAccept(DropTargetEvent event) {}
			public void dragOver(DropTargetEvent event) {}
			public void dragOperationChanged(DropTargetEvent event) {}
			public void dragLeave(DropTargetEvent event) {}
			public void dragEnter(DropTargetEvent event) {}
		});

	}

	protected void resetAll() {
		txt_title.setText(Constant.OUTPUT_DEFAULT_TITLE);
		tbl_files.removeAll();
		tblc_index.setWidth(35);
		tblc_dirName.setWidth(390);
		tblc_fileName.setWidth(165);
		tblc_fileSize.setWidth(85);
		toggleAnalyzingButton(false);

		fieldMapping.dispose();
		fieldMapping = new FieldMapping(tbf_setting, SWT.NONE);
		fieldMapping.setEnabled(false);
		tbi_mapping.setControl(fieldMapping);

		filterSetting.dispose();
		filterSetting = new FilterSetting(tbf_setting, SWT.NONE);
		filterSetting.setEnabled(true);
		tbi_filter.setControl(filterSetting);

		outputSetting.dispose();
		outputSetting = new OutputSetting(tbf_setting, SWT.NONE);
		outputSetting.setEnabled(true);
		tbi_output.setControl(outputSetting);

		optionSetting.dispose();
		optionSetting = new OptionSetting(tbf_setting, SWT.NONE);
		optionSetting.setEnabled(true);
		tbi_option.setControl(optionSetting);

		resetSetting();
		tbf_setting.setSelection(0);
	}

	public void debug(String s) {
		if(debugMode && console.isDisposed() == false) {
			console.addDebugMessage(s);
		}
	}

	public void debug(Throwable t) {
		debug(LoggingUtil.getTraceString(t));
	}

	public void toggleDebugConsole() {
		console.setVisible(!console.getVisible());
		if(btn_console.getText().equals("Hide Console")) {
			btn_console.setText("Show Console");
		} else {
			btn_console.setText("Hide Console");
			console.setMinimized(false);
		}
	}
	
	public void toggleAnalyzingButton(boolean flag) {
		btn_analyzing.setEnabled(flag);
		if(flag) {
			btn_analyzing.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		} else {
			btn_analyzing.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
		}
	}

	public void openResultAnalyzer() {
		if(resultAnalyzer == null) {
			resultAnalyzer = new ResultAnalyzer(display, SWT.SHELL_TRIM);
			resultAnalyzer.setVisible(false);
		}
		resultAnalyzer.setVisible(true);
		resultAnalyzer.forceActive();
	}

	public List<String> getLogFileList() {
		List<String> fileList = new ArrayList<String>();
		TableItem[] items = tbl_files.getItems();
		for(TableItem item : items) {
			fileList.add(((File)item.getData("file")).getAbsolutePath());
		}
		return fileList;
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
					debug("Duplicated Item(size updated) : " + f.getCanonicalPath());
				} else {
					String fullpath = f.getCanonicalPath();
					int lastidx = fullpath.lastIndexOf(File.separatorChar);
					TableItem item = new TableItem(table, SWT.NULL);
					item.setText(0, String.valueOf(table.getItemCount()));
					item.setText(1, fullpath.substring(0, lastidx));
					item.setText(2, fullpath.substring(lastidx + 1));
					item.setText(3, FileUtil.getFileSizeString(f));
					item.setData("file", f);
					debug("Added Item : " + f.getCanonicalPath());
					cnt++;
				}
			} catch(Exception ex) {
				debug(ex);
			}
		}
		if(cnt > 0) {
			debug(cnt + " files are added successfully.\n");
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
	
	public void addTableItems(String[] list, String filename_pattern) {
		List<File> filelist = new ArrayList<File>();
		File f;
		for(String s : list) {
			f = new File(s);
			if(f.isDirectory()) {
				List<File> subfiles = FileUtil.getAllFiles(s, filename_pattern, true);
				for(File subfile : subfiles) {
					filelist.add(subfile);
				}
			} else if(f.isFile()) {
				if(filename_pattern == null || FileUtil.isMatchFile(f.getName(), filename_pattern)) {
					filelist.add(f);
				}
			}
		}
		addTableItems(tbl_files, filelist);
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
			debug("Removed " + indices.length + " item(s).\n");
		}
		if(tbl_files.getItemCount() == 0) {
			fieldMapping.reset();
			fieldMapping.setEnabled(false);
		}
	}
	
	protected void updateDate(Table tableData) {
		if(tableData.getItemCount() > 0) {
			TableItem[] items = tableData.getItems();
			TableItem item;
			List<String> lines;
			Date from = null;
			Date to = null;
			try {
				DefaultParser parser = new DefaultParser(getAnalyzerSetting());
				for(int i = 0; i < items.length; i++) {
					item = items[i];
					File logfile = (File)item.getData("file");
					String logfile_encoding = FileUtil.getFileEncoding(logfile.getPath());
					debug("File encoding : path='" + logfile.getPath() + "', encoding=" + logfile_encoding);
					lines = FileUtil.head(logfile, 10, logfile_encoding);
					Date dt;
					for(String line : lines) {
						dt = parser.getParsedDate(line);
						if(dt == null) continue;
						if(from == null || dt.before(from)) from = DateUtil.getFirstOfDay(dt);
						if(to == null || dt.after(to)) to = DateUtil.getLastOfDay(dt);
					}
					lines = FileUtil.tail(logfile, 10, logfile_encoding);
					for(String line : lines) {
						dt = parser.getParsedDate(line);
						if(dt == null) continue;
						if(from == null || dt.before(from)) from = DateUtil.getFirstOfDay(dt);
						if(to == null || dt.after(to)) to = DateUtil.getLastOfDay(dt);
					}
				}
				filterSetting.updateFromDate(from);
				filterSetting.updateToDate(to);
			} catch(Exception e) {
				debug(e);
			}
		} else {
			filterSetting.updateFromDate(DateUtil.getFirstOfDay(new Date()));
			filterSetting.updateToDate(DateUtil.getLastOfDay(new Date()));
		}
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

	public void loadSetting(File f) {
		if(f == null) {
			MessageUtil.showErrorMessage(shell, "Invalid Setting File.");
		} else {
			int idx = f.getName().lastIndexOf('.');
			if(idx < 1 || !f.getName().substring(idx+1).equals("alb")) {
				MessageUtil.showErrorMessage(shell, "The extension of Setting File must be '.alb'.");
			} else {
				String msg = "The current setting will be replaced with this setting.\nThe mapping data won't be applied.\n\nDo you really want to load this setting?";
				if(MessageUtil.showConfirmMessage(shell, msg)) {
					try {
						loadSetting(new FileInputStream(f));
						debug("Load a setting : " + f.getAbsolutePath());
					} catch(Exception e) {
						debug(e);
					}
				}
			}
		}
	}	

	@SuppressWarnings("unchecked")
	public void loadSetting(InputStream is) {
		List<Object> readData = FileUtil.readObjectFile(is);
		try {
			boolean loadMappingData = false;
			if(readData.size() > 31) {
				loadMappingData = MessageUtil.showYesNoMessage(shell, "The setting file contains mapping data.\n\nDo you want to load mapping data as well?");
			}
			if(readData.size() < 31 || (loadMappingData && readData.size() < 39)) {
				throw new Exception("The number of items in the setting file is insufficient : size=" + readData.size());
			}
			int i = 0;
			for(; i < 31; i++) {
				Object obj = readData.get(i);
				switch(i) {
					case 0:
						if(((String)obj).equals(Constant.SETTING_FILE_HEADER) == false) {
							throw new Exception("Wrong header of the file.");
						}
						break;
					case 1:
						filterSetting.chk_allRange.setSelection((Boolean)obj);
						filterSetting.dtp_fromDate.setEnabled(!(Boolean)obj);
						filterSetting.dtp_toDate.setEnabled(!(Boolean)obj);
						break;
					case 2:
						filterSetting.dtp_fromDate.setDate((Date)obj);
						break;
					case 3:
						filterSetting.dtp_toDate.setDate((Date)obj);
						break;
					case 4:
						filterSetting.chk_includeFilter.setSelection((Boolean)obj);
						filterSetting.grp_inclueFilter.setEnabled((Boolean)obj);
						break;
					case 5:
						filterSetting.btn_inc_chkAnd.setSelection((Boolean)obj);
						filterSetting.btn_inc_chkOr.setSelection(!(Boolean)obj);
						break;
					case 6:
						filterSetting.chk_inc_ignoreCase.setSelection((Boolean)obj);
						break;
					case 7:
						HashMap<String, String> incFilterData = (HashMap<String, String>)obj;
						filterSetting.txt_inc_uri.setText(StringUtil.NVL((String)(incFilterData.get("URI")), ""));
						filterSetting.txt_inc_ext.setText(StringUtil.NVL((String)(incFilterData.get("EXT")), ""));
						filterSetting.txt_inc_ip.setText(StringUtil.NVL((String)(incFilterData.get("IP")), ""));
						filterSetting.txt_inc_method.setText(StringUtil.NVL((String)(incFilterData.get("METHOD")), ""));
						filterSetting.txt_inc_version.setText(StringUtil.NVL((String)(incFilterData.get("VERSION")), ""));
						filterSetting.txt_inc_code.setText(StringUtil.NVL((String)(incFilterData.get("CODE")), ""));
						break;
					case 8:
						filterSetting.chk_excludeFilter.setSelection((Boolean)obj);
						filterSetting.grp_exclueFilter.setEnabled((Boolean)obj);
						break;
					case 9:
						filterSetting.btn_exc_chkAnd.setSelection((Boolean)obj);
						filterSetting.btn_exc_chkOr.setSelection(!(Boolean)obj);
						break;
					case 10:
						filterSetting.chk_exc_ignoreCase.setSelection((Boolean)obj);
						break;
					case 11:
						HashMap<String, String> excFilterData = (HashMap<String, String>)obj;
						filterSetting.txt_exc_uri.setText(StringUtil.NVL((String)(excFilterData.get("URI")), ""));
						filterSetting.txt_exc_ext.setText(StringUtil.NVL((String)(excFilterData.get("EXT")), ""));
						filterSetting.txt_exc_ip.setText(StringUtil.NVL((String)(excFilterData.get("IP")), ""));
						filterSetting.txt_exc_method.setText(StringUtil.NVL((String)(excFilterData.get("METHOD")), ""));
						filterSetting.txt_exc_version.setText(StringUtil.NVL((String)(excFilterData.get("VERSION")), ""));
						filterSetting.txt_exc_code.setText(StringUtil.NVL((String)(excFilterData.get("CODE")), ""));
						break;
					case 12:
						outputSetting.chk_excel.setSelection((Boolean)obj);
						break;
					case 13:
						outputSetting.chk_html.setSelection((Boolean)obj);
						break;
					case 14:
						outputSetting.chk_text.setSelection((Boolean)obj);
						break;
					case 15:
						outputSetting.txt_directory.setText(StringUtil.NVL((String)obj, ""));
						break;
					case 16:
						outputSetting.btn_count.setSelection((Boolean)obj);
						outputSetting.btn_name.setSelection(!(Boolean)obj);
						break;
					case 17:
						optionSetting.chk_multiThread.setSelection((Boolean)obj);
						break;
					case 18:
						optionSetting.chk_fixedFields.setSelection((Boolean)obj);
						break;						
					case 19:
						optionSetting.chk_allowErrors.setSelection((Boolean)obj);
						optionSetting.spn_allowErrors.setEnabled((Boolean)obj);
						break;
					case 20:
						optionSetting.spn_allowErrors.setSelection(Integer.parseInt(StringUtil.NVL((String)obj, "5")));
						break;
					case 21:
						optionSetting.chk_includeParams.setSelection((Boolean)obj);
						break;
					case 22:
						optionSetting.chk_collectTPM.setSelection((Boolean)obj);
						optionSetting.spn_tpmUnit.setEnabled((Boolean)obj);
						break;
					case 23:
						optionSetting.spn_tpmUnit.setSelection(Integer.parseInt(StringUtil.NVL((String)obj, "1")));
						break;
					case 24:
						optionSetting.chk_collectElapsed.setSelection((Boolean)obj);
						optionSetting.spn_collectElapsed.setEnabled((Boolean)obj);
						break;
					case 25:
						optionSetting.spn_collectElapsed.setSelection(Integer.parseInt(StringUtil.NVL((String)obj, "10000")));
						break;
					case 26:
						optionSetting.chk_collectBytes.setSelection((Boolean)obj);
						optionSetting.spn_collectBytes.setEnabled((Boolean)obj);
						break;
					case 27:
						optionSetting.spn_collectBytes.setSelection(Integer.parseInt(StringUtil.NVL((String)obj, "1000")));
						break;
					case 28:
						optionSetting.chk_collectErrors.setSelection((Boolean)obj);
						break;
					case 29:
						optionSetting.chk_collectIP.setSelection((Boolean)obj);
						break;
					case 30:
						optionSetting.chk_collectTPS.setSelection((Boolean)obj);
						break;
				}
			}
			
			if(loadMappingData) {
				FieldMappingInfo mappingInfo = new FieldMappingInfo();
				mappingInfo.setLogType((String)readData.get(i++));
				mappingInfo.setFieldDelimeter((String)readData.get(i++));
				mappingInfo.setFieldBracelet((String)readData.get(i++));
				mappingInfo.setOffsetHour(Float.valueOf((String)readData.get(i++)));
				mappingInfo.setTimeFormat((String)readData.get(i++));
				mappingInfo.setTimeLocale((Locale)readData.get(i++));
				mappingInfo.setElapsedUnit((String)readData.get(i++));
				mappingInfo.setMappingInfo((HashMap<String, String>)readData.get(i++));				

				fieldMapping.txt_delimeter.setText(mappingInfo.getFieldDelimeter());
				fieldMapping.txt_bracelet.setText(mappingInfo.getFieldBracelet());
				fieldMapping.spn_offset.setSelection((int)(mappingInfo.getOffsetHour()*10));
				fieldMapping.cb_timeFormat.setText(mappingInfo.getTimeFormat());
				fieldMapping.setTimeLocale(mappingInfo.getTimeLocale());
				fieldMapping.cb_elapsedUnit.setText(mappingInfo.getElapsedUnit());				
				if(tbl_files.getItemCount() > 0) {
					fieldMapping.cb_logType.setText(mappingInfo.getLogType());
					fieldMapping.autoMapping(mappingInfo);
				} else {
					MessageUtil.showWarningMessage(shell, "Fields can not be mapped without a log file.\nOpen the log file first.\n\nMapping data is ignored.");
				}
			}
		} catch(Exception e) {
			debug(e);
			MessageUtil.showErrorMessage(shell, "Invalid Setting File.");
		}
	}

	public void saveSetting(File f) {
		boolean saveMappingData = MessageUtil.showYesNoMessage(shell, "Do you want to include the mapping data in the file?");
		List<Object> writeData = new ArrayList<Object>();
		writeData.add(Constant.SETTING_FILE_HEADER);
		writeData.add(filterSetting.checkAllRangeEnable());
		writeData.add(filterSetting.getRangeFromDate());
		writeData.add(filterSetting.getRangeToDate());
		writeData.add(filterSetting.getIncludeFilterEnable());
		writeData.add(filterSetting.checkIncludeFilterAndCheck());
		writeData.add(filterSetting.getIncludeFilterIgnoreCase());
		writeData.add(filterSetting.getIncludeFilterData());
		writeData.add(filterSetting.getExcludeFilterEnable());
		writeData.add(filterSetting.checkExcludeFilterAndCheck());
		writeData.add(filterSetting.getExcludeFilterIgnoreCase());
		writeData.add(filterSetting.getExcludeFilterData());
		writeData.add(outputSetting.checkExcelType());
		writeData.add(outputSetting.checkHtmlType());
		writeData.add(outputSetting.checkTextType());
		writeData.add(outputSetting.getOutputDirectory());
		writeData.add(outputSetting.sortByCount());
		writeData.add(optionSetting.checkMultiThread());
		writeData.add(optionSetting.checkFixedFields());
		writeData.add(optionSetting.checkAllowErrors());
		writeData.add(optionSetting.getAllowErrorCount());
		writeData.add(optionSetting.checkIncludeParams());
		writeData.add(optionSetting.checkCollectTPM());
		writeData.add(optionSetting.getTPMUnitMinutes());
		writeData.add(optionSetting.checkCollectElaspsedTime());
		writeData.add(optionSetting.getCollectElapsedTimeMS());
		writeData.add(optionSetting.checkCollectResponseBytes());
		writeData.add(optionSetting.getCollectResponseBytesKB());
		writeData.add(optionSetting.checkCollectErrors());
		writeData.add(optionSetting.checkCollectIP());
		writeData.add(optionSetting.checkCollectTPS());
		if(saveMappingData) {
			writeData.add(fieldMapping.getLogType());
			writeData.add(fieldMapping.getDelimeter());
			writeData.add(fieldMapping.getBracelet());
			writeData.add(fieldMapping.getOffsetHour());
			writeData.add(fieldMapping.getTimeFormat());
			writeData.add(fieldMapping.getTimeLocale());
			writeData.add(fieldMapping.getElapsedUnit());
			writeData.add(fieldMapping.getMappingData());
		}
		FileUtil.writeObjectFile(f, writeData, false);

		debug("Save this setting : " + f.getAbsolutePath());
	}

	public void resetSetting() {
		InputStream is = null;
		try {
			is = ClassLoader.getSystemResource(Constant.FILE_PATH_DEFAULTSETTING).openStream();
			loadSetting(is);
			outputSetting.txt_directory.setText(Constant.OUTPUT_DEFAULT_DIRECTORY);
		} catch(IOException ioe) {
			debug(ioe);
		}
	}

	protected void executeAnalyze() {
		int idx = fieldMapping.checkMappingValidation();
		if(idx > -1) {
			MessageUtil.showInfoMessage(shell, "Invalid format of file has been detected.\nCheck the file No." + (idx + 1));
			return;
		}
		try {
			updateDate(tbl_files);
			if(!filterSetting.checkAllRangeEnable() && filterSetting.getRangeFromDate().compareTo(filterSetting.getRangeToDate()) >= 0) {
				MessageUtil.showInfoMessage(shell, "Invalid range of time has been set.\nCheck the Time Range of Filter tab.");
				return;
			}
			long from = filterSetting.getRangeFromDate().getTime();
			long to = filterSetting.getRangeToDate().getTime();
			int unit_cnt = (int)((to - from) / 1000) / (60 * Integer.parseInt(optionSetting.getTPMUnitMinutes()));
			debug(unit_cnt + " units of tx aggreation data will be created.");
			if(unit_cnt > Constant.WARNING_TXUNIT_COUNT) {
				if(!MessageUtil.showConfirmMessage(shell, "Too many("+unit_cnt+") tx aggreation data will be created by Time Range.\n"
						+ "Increase tpm aggregation minute, or ALYBA could slow down or down abnormally.\n\nDo you want to continue?")) {
					return;
				}
			}
		} catch(Exception e) {
			debug(e);
		}
		if(!MessageUtil.showConfirmMessage(shell, "Do you want to analyze the files with current setting?")) {
			return;
		}

		filterSetting.resetDateUpdateCheck();

		AnalyzerSetting setting = getAnalyzerSetting();
		setting.setAnalyzeDate(new Date());
		AnalyzeOutput output = new AnalyzeOutput(setting);
		debug(setting.toString());

		String dbfile_path = outputSetting.getOutputDirectory() + File.separatorChar + output.getFileName() + ".adb";
		ObjectDBUtil db = new ObjectDBUtil(dbfile_path, true);
		ObjectDBUtil.register(db);
		try {
			debug("Opened the database : dbfile=" + db.getDBFilePath());
		} catch(Exception e) {
			debug(e);
		}
		
		ProgressBarDialog progressBar = new ProgressBarDialog(shell);
		progressBar.setTitle("Analyzer Progress");
		progressBar.setDetailViewCount(tbl_files.getItemCount());
		AnalyzeTask task = new AnalyzeTask(setting, DefaultParser.class);
		progressBar.setTask(task);
		progressBar.open();
		if(!task.isSuccessed()) {
			String msg = task.getFailedMessage();
			if(msg != null) {
				MessageUtil.showWarningMessage(shell, msg);
			} else {
				MessageUtil.showWarningMessage(shell, "Unknown error");
			}
		}
		
		AnalyzeTask post_task = null;		
		if(task.isSuccessed() && setting.collectTPS) {
			progressBar = new ProgressBarDialog(shell);
			progressBar.setTitle("Analyzer Post-Progress");
			progressBar.setDetailViewCount(tbl_files.getItemCount());
			post_task = new AnalyzeTask(setting, PostParser.class);
			progressBar.setTask(post_task);
			progressBar.open();
			if(!post_task.isSuccessed()) {
				String msg = post_task.getFailedMessage();
				if(msg != null) {
					MessageUtil.showWarningMessage(shell, msg);
				} else {
					MessageUtil.showWarningMessage(shell, "Unknown error");
				}
			}
		}
		
		OutputTask output_task = null;
		if(task.isSuccessed() && (post_task == null || post_task.isSuccessed())) {
			progressBar = new ProgressBarDialog(shell);
			progressBar.setTitle("Generate Progress");
			progressBar.setDetailViewCount(setting.getOutputCount()+1);
			output_task = new OutputTask(output);
			progressBar.setTask(output_task);
			progressBar.open();
			if(!output_task.isSuccessed()) {
				String msg = post_task.getFailedMessage();
				if(msg != null) {
					MessageUtil.showWarningMessage(shell, msg);
				} else {
					MessageUtil.showWarningMessage(shell, "Unknown error");
				}
			}
		}
		
		try {
			debug("Closing the database : dbfile=" + db.getDBFilePath());
			if(task.isSuccessed()) {
				db.closeAll();
			} else {
				db.closeAndDeleteDB();
			}
		} catch(Exception e) {
			debug(e);
		}
		
		if(task.isSuccessed() && (output_task == null || output_task.isSuccessed())) {
			List<String> filenames = output_task.getGeneratedFiles();
			if(filenames != null) {
				String msg = "Generated " + filenames.size() + " file(s). What do you want to do?";
				String[] labels;
				if(filenames.size() > 0) {
					labels = new String[] { "Result Analyzer", "Open directory", "Open files", "Close" };
				} else {
					labels = new String[] { "Result Analyzer", "Open directory", "Close" };
				}
				int i = MessageUtil.showSelectMessage(shell, "Question", msg, labels);
				switch(i) {
					case 0:
						openResultAnalyzer();
						try {
							resultAnalyzer.loadDBFile(db.getDBFilePath());
						} catch(Exception e) {
							debug(e);
							MessageUtil.showErrorMessage(shell, "Failed to load the database.");
							resultAnalyzer.setVisible(false);
						}
						break;
					case 1:
						Program.launch((new File(setting.outputDirectory)).getAbsolutePath());
						break;
					case 2:
						ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3));
						for(final String f : filenames) {
							threadPool.execute(new Runnable() {
								public void run() {
									Program.launch((new File(f)).getAbsolutePath());
								}
							});
						}
						threadPool.shutdown();
						break;
				}
			}
		}

		task = null;
		post_task = null;
		output_task = null;
	}
	
	private AnalyzerSetting getAnalyzerSetting() {
		FieldMappingInfo fieldMappingInfo = new FieldMappingInfo();
		fieldMappingInfo.setLogType(fieldMapping.getLogType());
		fieldMappingInfo.setFieldDelimeter(StringUtil.replaceMetaCharacter(fieldMapping.getDelimeter(), false));
		fieldMappingInfo.setFieldBracelet(fieldMapping.getBracelet());
		fieldMappingInfo.setMappingInfo(fieldMapping.getMappingData());
		fieldMappingInfo.setOffsetHour(Float.parseFloat(fieldMapping.getOffsetHour()));
		fieldMappingInfo.setTimeFormat(fieldMapping.getTimeFormat());
		fieldMappingInfo.setTimeLocale(fieldMapping.getTimeLocale());
		fieldMappingInfo.setElapsedUnit(fieldMapping.getElapsedUnit());
		fieldMappingInfo.setLogFieldCount(fieldMapping.getFieldCount());

		FilterSettingInfo filterSettingInfo = new FilterSettingInfo();
		filterSettingInfo.setAllRangeEnable(filterSetting.checkAllRangeEnable());
		filterSettingInfo.setFromDateRange(filterSetting.getRangeFromDate());
		filterSettingInfo.setToDateRange(filterSetting.getRangeToDate());
		filterSettingInfo.setIncludeFilterEnable(filterSetting.getIncludeFilterEnable());
		filterSettingInfo.setIncludeFilterAndCheck(filterSetting.checkIncludeFilterAndCheck());
		filterSettingInfo.setIncludeFilterIgnoreCase(filterSetting.getIncludeFilterIgnoreCase());
		filterSettingInfo.setIncludeFilterInfo(filterSetting.getIncludeFilterData());
		filterSettingInfo.setExcludeFilterEnable(filterSetting.getExcludeFilterEnable());
		filterSettingInfo.setExcludeFilterAndCheck(filterSetting.checkExcludeFilterAndCheck());
		filterSettingInfo.setExcludeFilterIgnoreCase(filterSetting.getExcludeFilterIgnoreCase());
		filterSettingInfo.setExcludeFilterInfo(filterSetting.getExcludeFilterData());

		AnalyzerSetting setting = new AnalyzerSetting();
		setting.setTitle(txt_title.getText());
		setting.setAnalyzerTimezone(TIMEZONE);
		setting.setLogFileList(getLogFileList());
		setting.setLogFileEncodingList(fieldMapping.getFileEncoding());
		setting.setOutputExcelType(outputSetting.checkExcelType());
		setting.setOutputHtmlType(outputSetting.checkHtmlType());
		setting.setOutputTextType(outputSetting.checkTextType());
		setting.setOutputDirectory(outputSetting.getOutputDirectory());
		setting.setOutputSortBy((outputSetting.sortByCount() ? "COUNT" : "NAME"));
		setting.setMultiThreadParsing(optionSetting.checkMultiThread());
		setting.setAllowErrors(optionSetting.checkAllowErrors());
		setting.setAllowErrorCount(Integer.parseInt(optionSetting.getAllowErrorCount()));
		setting.setUriIncludeParams(optionSetting.checkIncludeParams());
		setting.setCollectTPM(optionSetting.checkCollectTPM());
		setting.setTPMUnitMinutes(Integer.parseInt(optionSetting.getTPMUnitMinutes()));
		setting.setCollectElapsedTime(optionSetting.checkCollectElaspsedTime());
		setting.setCollectElapsedTimeMS(Long.parseLong(optionSetting.getCollectElapsedTimeMS()));
		setting.setCollectResponseBytes(optionSetting.checkCollectResponseBytes());
		setting.setCollectResponseBytesKB(Integer.parseInt(optionSetting.getCollectResponseBytesKB()));
		setting.setCollectErrors(optionSetting.checkCollectErrors());
		setting.setCheckFieldCount(optionSetting.checkFixedFields());
		setting.setCollectIP(optionSetting.checkCollectIP());
		setting.setCollectTPS(optionSetting.checkCollectTPS());
		setting.setFieldMapping(fieldMappingInfo);
		setting.setFilterSetting(filterSettingInfo);
		
		return setting;
	}

	public static void printUsage() {
		System.out.println("");
		System.out.println("Usage: ALYBA [<option>]");
		System.out.println("       ALYBA -result [<result_db_file>]");
		System.out.println("");
		System.out.println("option");
		System.out.println("------");
		System.out.println("  -help");
		System.out.println("     Show help");
		System.out.println("");
		System.out.println("  -version");
		System.out.println("     Displays version information");
		System.out.println("");
		System.out.println("  -debug");
		System.out.println("     Run as debug mode");
		System.out.println("");
		System.out.println("  -result");
		System.out.println("     Open a ResultAnalyzer");
		System.out.println("     Open a ResultAnalyzer and load the file if <result_db_file> is given.");
		System.out.println("");
	}
	
}
