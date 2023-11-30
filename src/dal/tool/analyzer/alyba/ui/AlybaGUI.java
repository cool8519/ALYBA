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
import java.util.Map;
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
import org.eclipse.swt.graphics.FontData;
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
import dal.tool.analyzer.alyba.output.LogAnalyzeOutput;
import dal.tool.analyzer.alyba.parse.ParserUtil;
import dal.tool.analyzer.alyba.parse.parser.DefaultParser;
import dal.tool.analyzer.alyba.parse.parser.PostParser;
import dal.tool.analyzer.alyba.parse.task.FileEncodingCheckTask;
import dal.tool.analyzer.alyba.parse.task.LogAnalyzeTask;
import dal.tool.analyzer.alyba.parse.task.OutputTask;
import dal.tool.analyzer.alyba.setting.AlybaSetting;
import dal.tool.analyzer.alyba.setting.FilterSettingInfo;
import dal.tool.analyzer.alyba.setting.LogAnalyzerSetting;
import dal.tool.analyzer.alyba.setting.LogFieldMappingInfo;
import dal.tool.analyzer.alyba.ui.comp.ContentView;
import dal.tool.analyzer.alyba.ui.comp.DebugConsole;
import dal.tool.analyzer.alyba.ui.comp.FieldMapping;
import dal.tool.analyzer.alyba.ui.comp.FilterSetting;
import dal.tool.analyzer.alyba.ui.comp.HistoryView;
import dal.tool.analyzer.alyba.ui.comp.OptionSetting;
import dal.tool.analyzer.alyba.ui.comp.OutputSetting;
import dal.tool.analyzer.alyba.ui.comp.ResultAnalyzer;
import dal.tool.analyzer.alyba.ui.comp.TPMAnalyzer;
import dal.tool.analyzer.alyba.ui.history.HistoryManager;
import dal.tool.analyzer.alyba.ui.history.HistoryVO;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.DateUtil;
import dal.util.FileUtil;
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

	public static AlybaGUI instance = null;
	public static boolean debugMode = false;
	public static DebugConsole debugConsole = null;
	public static ObjectDBUtil inProgressDbUtil = null;
	
	public HistoryView historyView = null;
	public TPMAnalyzer tpmAnalyzer = null;
	public ResultAnalyzer resultAnalyzer = null;

	public FieldMapping fieldMapping;
	public FilterSetting filterSetting;
	public OptionSetting optionSetting;
	public OutputSetting outputSetting;
	public Map<String,String> fileEncodings;

	public Display display;
	public Shell shell;
	public Text txt_title;
	public Table tbl_files;
	public TableViewer tblv_files;
	private TabFolder tbf_setting;
	private TableColumn tblc_index;
	private TableColumn tblc_dirName;
	private TableColumn tblc_fileName;
	private TableColumn tblc_fileSize;
	private TabItem tbi_mapping;
	private TabItem tbi_filter;
	private TabItem tbi_option;
	private TabItem tbi_output;
	private Button btn_history;
	private Button btn_console;
	private Button btn_resetAll;
	private Button btn_openFiles;
	private Button btn_removeFiles;
	private Button btn_removeAll;
	private Button btn_analyzing;
	private Button btn_tpmAnalyzer;
	private Button btn_resultAnalyzer;
	private HeapStatus heapstatus;
	private int last_help;

	public static void main(String[] args) {
		try {
			boolean isResultAnalyzer = false;
			String dbFileName = null;
			
			String[] commandlineOptions = { "-help", "-version", "-debug", "-result" };
			CommandLineArguments cmdArgs = new CommandLineArguments(commandlineOptions, false);
			cmdArgs.setNonOptionArguments(0, 1);
			try {
				cmdArgs.parse(args);
			} catch(Exception e) {
				System.out.println(e.getMessage());
				printUsage();
				return;
			}			
			if(cmdArgs.isIncludeOption("-help")) {
				System.out.println("ALYBA (AccessLog & Your Bad Application)");
				printUsage();
				return;
			} else if(cmdArgs.isIncludeOption("-version")) {
				System.out.println(Constant.PROGRAM_VERSION);
				return;
			}			
			if(cmdArgs.isIncludeOption("-debug")) {
				debugMode = true;
			}
			if(cmdArgs.isIncludeOption("-result")) {
				isResultAnalyzer = true;
				if(cmdArgs.getNonOptionSize() == 1) {
					dbFileName = cmdArgs.getNonOption(0);
					if(!(new File(dbFileName).exists())) {
						System.out.println("File does not exists : " + dbFileName);
						return;
					}
				}
			} else if(cmdArgs.getNonOptionSize() == 1) {
				System.out.println("Allowed non-option agrument only with '-result'");
				printUsage();
				return;
			}

			TimeZone.setDefault(Constant.TIMEZONE_UTC);
			Display display = null;
			Shell shell = null;
			if(isResultAnalyzer) {
		        display = new Display();
				FontData fd = display.getSystemFont().getFontData()[0];
				Constant.DEFAULT_FONT_SIZE = fd.getHeight() - (int)Math.ceil((-fd.data.lfHeight-12)/2.0F);
		        shell = new ResultAnalyzer(display, SWT.SHELL_TRIM, dbFileName);
			} else {
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
						Logger.error(t);
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
                	instance = new AlybaGUI();
                }
            }
		}
		return instance;
	}

	public static void createDebugConsole(Display display) {
		if(debugMode && debugConsole == null) {
            synchronized(AlybaGUI.class) {
                if(debugConsole == null) {
        			debugConsole = new DebugConsole(display, SWT.SHELL_TRIM);
        			debugConsole.setLocation(0, 0);
        			debugConsole.setImage(ImageUtil.getImage(Constant.IMAGE_PATH_TRAYICON));
                }
            }
		}
	}

	public static DebugConsole getDebugConsole() {
		return debugConsole;
	}
	
	public AlybaGUI() {
		this.display = new Display();
		FontData fd = display.getSystemFont().getFontData()[0];
		Constant.DEFAULT_FONT_SIZE = fd.getHeight() - (int)Math.ceil((-fd.data.lfHeight-12)/2.0F);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void init() {
		try {
			createDebugConsole(display);
			createContents();
			addEventListener();
			shell.open();
			shell.layout();
		} catch(Exception e) {			
			System.out.println("Failed to start ALYBA GUI.");
			System.out.println();
			e.printStackTrace();
			System.exit(-1);
		}
	}

	protected void createContents() {

		shell = new Shell(display, SWT.SHELL_TRIM & ~SWT.RESIZE & ~SWT.MAX);
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
		lb_title.setFont(Utility.getFont(SWT.NONE));

		txt_title = new Text(shell, SWT.BORDER);
		txt_title.setBounds(56, 10, 220, 19);
		txt_title.setFont(Utility.getFont());
		txt_title.setText(Constant.OUTPUT_DEFAULT_TITLE);

		btn_history = new Button(shell, SWT.NONE);
		btn_history.setFont(Utility.getFont());
		btn_history.setText("History View");
		btn_history.setBounds(300, 8, 100, 23);
		
		btn_console = new Button(shell, SWT.NONE);
		btn_console.setFont(Utility.getFont());
		btn_console.setText("Hide Console");
		btn_console.setBounds(510, 8, 100, 23);
		btn_console.setVisible(debugMode);

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
		btn_removeFiles.setBounds(180, 227, 130, 23);

		btn_removeAll = new Button(shell, SWT.NONE);
		btn_removeAll.setBounds(180, 256, 130, 23);
		btn_removeAll.setFont(Utility.getFont());
		btn_removeAll.setText("Remove All");

		btn_tpmAnalyzer = new Button(shell, SWT.WRAP);
		btn_tpmAnalyzer.setFont(Utility.getFont(SWT.BOLD));
		btn_tpmAnalyzer.setText("TPM\nAnalyzer");
		btn_tpmAnalyzer.setBounds(355, 227, 70, 52);
		
		btn_resultAnalyzer = new Button(shell, SWT.WRAP);
		btn_resultAnalyzer.setFont(Utility.getFont(SWT.BOLD));
		btn_resultAnalyzer.setText("Result\nAnalyzer");
		btn_resultAnalyzer.setBounds(435, 227, 110, 52);
		
		btn_analyzing = new Button(shell, SWT.NONE);
		btn_analyzing.setFont(Utility.getFont(SWT.BOLD));
		btn_analyzing.setText("Start Analyzing");
		btn_analyzing.setBounds(564, 227, 160, 52);
		toggleAnalyzingButton(false);

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
		
		fileEncodings = new HashMap<String,String>();
		
		heapstatus = new HeapStatus(shell, 500, false, ImageUtil.getImageDescriptorFromURL(Constant.IMAGE_PATH_TRASH));
		heapstatus.setFont(Utility.getFont());
		heapstatus.setBounds(570, 668, 150, 18);

		shell.pack();
		shell.setSize(shell.getSize().x+10, shell.getSize().y+10);

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

		btn_history.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openHistoryView();
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
				if(files != null && files.size() > 0) {
					addTableItems(tbl_files, files);
				}
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
					updatedFileList();
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
									String[] fld_arr = ParserUtil.getTokenList(header, delimeter, bracelets, optionSetting.checkStrictCheck()).toArray(new String[0]);
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

		btn_tpmAnalyzer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openTPMAnalyzer();				
			}
		});

		btn_resultAnalyzer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openResultAnalyzer();
			}
		});
		
		btn_analyzing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				executeAnalyze();
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
					if(sourceFileList[0].endsWith(".alb")) {
						loadSetting(new File(sourceFileList[0]));
					} else if(sourceFileList[0].endsWith(".adb")) {
						openResultAnalyzer();
						try {
							resultAnalyzer.loadDBFile(sourceFileList[0]);
						} catch(Exception e) {
							Logger.debug("Failed to load the database : " + sourceFileList[0]);
							Logger.error(e);
							MessageUtil.showErrorMessage(shell, "Failed to load the database.");
							resultAnalyzer.setVisible(false);
						}
					} else {
						MessageUtil.showErrorMessage(shell, "Not Supported File type.");
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
		fieldMapping.setFilterSetting(filterSetting);

		outputSetting.dispose();
		outputSetting = new OutputSetting(tbf_setting, SWT.NONE);
		outputSetting.setEnabled(true);
		tbi_output.setControl(outputSetting);

		optionSetting.dispose();
		optionSetting = new OptionSetting(tbf_setting, SWT.NONE);
		optionSetting.setEnabled(true);
		tbi_option.setControl(optionSetting);
		
		fileEncodings = new HashMap<String,String>();

		resetSetting();
		tbf_setting.setSelection(0);
	}
	
	public void openHistoryView() {
		if(historyView == null) {
			historyView = new HistoryView(display, SWT.SHELL_TRIM);
			historyView.setVisible(false);
		}
		historyView.setVisible(true);
		if(!historyView.hasLoaded()) {
			historyView.loadHistoryList();
		}
		historyView.forceActive();
	}

	public void toggleDebugConsole() {
		debugConsole.setVisible(!debugConsole.getVisible());
		if(btn_console.getText().equals("Hide Console")) {
			btn_console.setText("Show Console");
		} else {
			btn_console.setText("Hide Console");
			debugConsole.setMinimized(false);
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

	public void openTPMAnalyzer() {
		if(tpmAnalyzer == null) {
			tpmAnalyzer = new TPMAnalyzer(display, SWT.SHELL_TRIM);
			tpmAnalyzer.setVisible(false);
		}
		tpmAnalyzer.setVisible(true);
		tpmAnalyzer.forceActive();
	}

	public ResultAnalyzer openResultAnalyzer() {
		if(resultAnalyzer == null) {
			resultAnalyzer = new ResultAnalyzer(display, SWT.SHELL_TRIM);
			resultAnalyzer.setVisible(false);
		}
		resultAnalyzer.setVisible(true);
		resultAnalyzer.forceActive();
		return resultAnalyzer;
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
			tbl_files.forceFocus();
			updatedFileList();
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

	protected void updatedFileList() {
		Map<String,String> newFileEncodings = new HashMap<String,String>();
		TableItem[] items = tbl_files.getItems();
		for(int i = 0; i < items.length; i++) {
			File logfile = (File)items[i].getData("file");
			newFileEncodings.put(logfile.getPath(), fileEncodings.get(logfile.getPath()));
		}
		fileEncodings = newFileEncodings;
	}
	
	public List<String> getFileEncodings() {
		return new ArrayList<String>(fileEncodings.values());
	}
	
	protected void removeFiles() {
		int[] indices = tbl_files.getSelectionIndices();
		if(indices.length < 1) {
			return;
		} else {
			tbl_files.remove(indices);
			updatedFileList();
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
					String logfile_encoding = getFileEncoding(logfile);
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
				Logger.debug("Failed to update setting of from/to date.");
				Logger.error(e);
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
				String msg = "The current setting will be replaced with this setting.\n\nDo you really want to load this setting?";
				if(MessageUtil.showConfirmMessage(shell, msg)) {
					try {
						loadSetting(new FileInputStream(f));
						Logger.debug("Loaded a setting : " + f.getAbsolutePath());
					} catch(Exception e) {
						Logger.debug("Failed to read setting." + f.getAbsolutePath());
						Logger.error(e);
					}
				}
			}
		}
	}	

	public void loadSetting(InputStream is) {
		List<Object> readData = FileUtil.readObjectFile(is);
		try {
			boolean loadMappingData = false;
			if(readData.size() != 2 || readData.get(0).getClass() != String.class || readData.get(1).getClass() != AlybaSetting.class || 
			   ((String)readData.get(0)).equals(Constant.SETTING_FILE_HEADER) == false) {
				throw new Exception("Invalid Setting File.");
			}
			AlybaSetting guiSetting = (AlybaSetting)readData.get(1);
			if(guiSetting.getMappingData() != null) {
				loadMappingData = MessageUtil.showYesNoMessage(shell, "The setting file contains mapping data.\n\nDo you want to load mapping data as well?");
			}
			
			filterSetting.chk_allRange.setSelection(guiSetting.isFilterCheckAllRangeEnabled());
			filterSetting.dtp_fromDate.setEnabled(!guiSetting.isFilterCheckAllRangeEnabled());
			filterSetting.dtp_toDate.setEnabled(!guiSetting.isFilterCheckAllRangeEnabled());
			if(guiSetting.getFilterRangeFromDate() != null) {
				filterSetting.dtp_fromDate.setDate(guiSetting.getFilterRangeFromDate());
			}
			if(guiSetting.getFilterRangeToDate() != null) {
				filterSetting.dtp_toDate.setDate(guiSetting.getFilterRangeToDate());
			}
			filterSetting.chk_includeFilter.setSelection(guiSetting.isFilterIncludeEnabled());
			filterSetting.grp_inclueFilter.setEnabled(guiSetting.isFilterIncludeEnabled());
			filterSetting.btn_inc_chkAnd.setSelection(guiSetting.isFilterIncludeAndChecked());
			filterSetting.btn_inc_chkOr.setSelection(!guiSetting.isFilterIncludeAndChecked());
			filterSetting.chk_inc_ignoreCase.setSelection(guiSetting.isFilterIncludeIgnoreCase());
			HashMap<String, String> incFilterData = guiSetting.getFilterIncludeData();
			filterSetting.txt_inc_uri.setText(StringUtil.NVL((String)(incFilterData.get("URI")), ""));
			filterSetting.txt_inc_ext.setText(StringUtil.NVL((String)(incFilterData.get("EXT")), ""));
			filterSetting.txt_inc_ip.setText(StringUtil.NVL((String)(incFilterData.get("IP")), ""));
			filterSetting.txt_inc_method.setText(StringUtil.NVL((String)(incFilterData.get("METHOD")), ""));
			filterSetting.txt_inc_version.setText(StringUtil.NVL((String)(incFilterData.get("VERSION")), ""));
			filterSetting.txt_inc_code.setText(StringUtil.NVL((String)(incFilterData.get("CODE")), ""));
			filterSetting.chk_excludeFilter.setSelection(guiSetting.isFilterExcludeEnabled());
			filterSetting.grp_exclueFilter.setEnabled(guiSetting.isFilterExcludeEnabled());
			filterSetting.btn_exc_chkAnd.setSelection(guiSetting.isFilterExcludeAndChecked());
			filterSetting.btn_exc_chkOr.setSelection(!guiSetting.isFilterExcludeAndChecked());
			filterSetting.chk_exc_ignoreCase.setSelection(guiSetting.isFilterExcludeIgnoreCase());
			HashMap<String, String> excFilterData = guiSetting.getFilterExcludeData();
			filterSetting.txt_exc_uri.setText(StringUtil.NVL((String)(excFilterData.get("URI")), ""));
			filterSetting.txt_exc_ext.setText(StringUtil.NVL((String)(excFilterData.get("EXT")), ""));
			filterSetting.txt_exc_ip.setText(StringUtil.NVL((String)(excFilterData.get("IP")), ""));
			filterSetting.txt_exc_method.setText(StringUtil.NVL((String)(excFilterData.get("METHOD")), ""));
			filterSetting.txt_exc_version.setText(StringUtil.NVL((String)(excFilterData.get("VERSION")), ""));
			filterSetting.txt_exc_code.setText(StringUtil.NVL((String)(excFilterData.get("CODE")), ""));			
			outputSetting.chk_excel.setSelection(guiSetting.isOutputExcelTypeChecked());
			outputSetting.chk_html.setSelection(guiSetting.isOutputHtmlTypeChecked());
			outputSetting.chk_text.setSelection(guiSetting.isOutputTextTypeChecked());
			outputSetting.checkCheckBox();
			outputSetting.txt_directory.setText(new File(StringUtil.NVL(guiSetting.getOutputDirectory(), Constant.OUTPUT_DEFAULT_DIRECTORY)).getAbsolutePath());
			outputSetting.btn_count.setSelection(guiSetting.isOutputSortByCountChecked());
			outputSetting.btn_name.setSelection(!guiSetting.isOutputSortByCountChecked());			
			optionSetting.chk_multiThread.setSelection(guiSetting.isOptionMultiThreadChecked());
			optionSetting.chk_fixedFields.setSelection(guiSetting.isOptionFixedFieldsChecked());
			optionSetting.chk_strictCheck.setSelection(guiSetting.isOptionStrictCheckChecked());
			optionSetting.chk_allowErrors.setSelection(guiSetting.isOptionAllowErrorsChecked());
			optionSetting.spn_allowErrors.setEnabled(guiSetting.isOptionAllowErrorsChecked());
			optionSetting.spn_allowErrors.setSelection(guiSetting.getOptionAllowErrorCount());
			optionSetting.chk_includeParams.setSelection(guiSetting.isOptionURLIncludeParamsChecked());
			optionSetting.chk_checkFileEncoding.setSelection(guiSetting.isOptionCheckFileEncodingChecked());
			optionSetting.chk_collectTPM.setSelection(guiSetting.isOptionCollectTPMChecked());
			optionSetting.spn_tpmUnit.setEnabled(guiSetting.isOptionCollectTPMChecked());
			optionSetting.spn_tpmUnit.setSelection(guiSetting.getOptionTPMUnitMinutes());
			optionSetting.chk_collectElapsed.setSelection(guiSetting.isOptionCollectElapsedTimeChecked());
			optionSetting.spn_collectElapsed.setEnabled(guiSetting.isOptionCollectElapsedTimeChecked());
			optionSetting.spn_collectElapsed.setSelection(guiSetting.getOptionCollectElapsedTimeMS());
			optionSetting.chk_collectBytes.setSelection(guiSetting.isOptionCollectResponseBytesChecked());
			optionSetting.spn_collectBytes.setEnabled(guiSetting.isOptionCollectResponseBytesChecked());
			optionSetting.spn_collectBytes.setSelection(guiSetting.getOptionCollectResponseBytesKB());
			optionSetting.chk_collectErrors.setSelection(guiSetting.isOptionCollectErrorsChecked());
			optionSetting.chk_collectIP.setSelection(guiSetting.isOptionCollectIPChecked());
			optionSetting.chk_collectTPS.setSelection(guiSetting.isOptionCollectTPSChecked());			
			if(loadMappingData) {
				LogFieldMappingInfo mappingInfo = new LogFieldMappingInfo();
				mappingInfo.setLogType(guiSetting.getMappingLogType());
				mappingInfo.setFieldDelimeter(guiSetting.getMappingDelimeter());
				mappingInfo.setFieldBracelet(guiSetting.getMappingBracelet());
				mappingInfo.setTimestampType(guiSetting.getMappingTimestampType());
				mappingInfo.setOffsetHour(guiSetting.getMappingOffsetHour());
				mappingInfo.setTimeFormat(guiSetting.getMappingTimeFormat());
				mappingInfo.setTimeLocale(guiSetting.getMappingTimeLocale());
				mappingInfo.setElapsedUnit(guiSetting.getMappingElapsedUnit());
				mappingInfo.setMappingInfo(guiSetting.getMappingData());
				fieldMapping.txt_delimeter.setText(mappingInfo.getFieldDelimeter());
				fieldMapping.txt_bracelet.setText(mappingInfo.getFieldBracelet());
				fieldMapping.cb_timestampType.setText(mappingInfo.getTimestampType());
				fieldMapping.spn_offset.setSelection((int)(mappingInfo.getOffsetHour()*10));
				fieldMapping.setTimeLocale(mappingInfo.getTimeLocale());
				fieldMapping.cb_elapsedUnit.setText(mappingInfo.getElapsedUnit());
				if(tbl_files.getItemCount() > 0) {
					fieldMapping.uriMappingManager.resetURIPatterns(guiSetting.getURIMappingPatterns());
					fieldMapping.cb_logType.setText(mappingInfo.getLogType());
					fieldMapping.autoMapping(mappingInfo);
				} else {
					MessageUtil.showWarningMessage(shell, "Fields can not be mapped without a log file.\nOpen the log file first.\n\nMapping data is ignored.");
				}
				fieldMapping.cb_timeFormat.setText(mappingInfo.getTimeFormat());
			}
		} catch(Exception e) {
			Logger.debug("Failed to load setting from the file.");
			Logger.error(e);
			MessageUtil.showErrorMessage(shell, "Invalid Setting File.");
		}
	}
	
	public void saveSetting(File f) {
		try {
			boolean saveMappingData = false;
			if(btn_analyzing.isEnabled()) {
				saveMappingData = MessageUtil.showYesNoMessage(shell, "Do you want to include the mapping data in the file?");
			}
			
			AlybaSetting guiSetting = new AlybaSetting();
			guiSetting.setFilterCheckAllRangeEnabled(filterSetting.checkAllRangeEnable());
			guiSetting.setFilterRangeFromDate(filterSetting.getRangeFromDate());
			guiSetting.setFilterRangeToDate(filterSetting.getRangeToDate());
			guiSetting.setFilterIncludeEnabled(filterSetting.getIncludeFilterEnable());
			guiSetting.setFilterIncludeAndChecked(filterSetting.checkIncludeFilterAndCheck());
			guiSetting.setFilterIncludeIgnoreCase(filterSetting.getIncludeFilterIgnoreCase());
			guiSetting.setFilterIncludeData(filterSetting.getIncludeFilterData());
			guiSetting.setFilterExcludeEnabled(filterSetting.getExcludeFilterEnable());
			guiSetting.setFilterExcludeAndChecked(filterSetting.checkExcludeFilterAndCheck());
			guiSetting.setFilterExcludeIgnoreCase(filterSetting.getExcludeFilterIgnoreCase());
			guiSetting.setFilterExcludeData(filterSetting.getExcludeFilterData());
			guiSetting.setOutputExcelTypeChecked(outputSetting.checkExcelType());
			guiSetting.setOutputHtmlTypeChecked(outputSetting.checkHtmlType());
			guiSetting.setOutputTextTypeChecked(outputSetting.checkTextType());
			guiSetting.setOutputDirectory(outputSetting.getOutputDirectory());
			guiSetting.setOutputSortByCountChecked(outputSetting.sortByCount());
			guiSetting.setOptionMultiThreadChecked(optionSetting.checkMultiThread());
			guiSetting.setOptionFixedFieldsChecked(optionSetting.checkFixedFields());
			guiSetting.setOptionStrictCheckChecked(optionSetting.checkStrictCheck());
			guiSetting.setOptionAllowErrorsChecked(optionSetting.checkAllowErrors());
			guiSetting.setOptionAllowErrorCount(Integer.parseInt(StringUtil.NVL(optionSetting.getAllowErrorCount(), "5")));
			guiSetting.setOptionURLIncludeParamsChecked(optionSetting.checkIncludeParams());
			guiSetting.setOptionCheckFileEncodingChecked(optionSetting.checkCheckFileEncoding());
			guiSetting.setOptionCollectTPMChecked(optionSetting.checkCollectTPM());
			guiSetting.setOptionTPMUnitMinutes(Integer.parseInt(StringUtil.NVL(optionSetting.getTPMUnitMinutes(), "1")));
			guiSetting.setOptionCollectElapsedTimeChecked(optionSetting.checkCollectElaspsedTime());
			guiSetting.setOptionCollectElapsedTimeMS(Integer.parseInt(StringUtil.NVL(optionSetting.getCollectElapsedTimeMS(), "10000")));
			guiSetting.setOptionCollectResponseBytesChecked(optionSetting.checkCollectResponseBytes());
			guiSetting.setOptionCollectResponseBytesKB(Integer.parseInt(StringUtil.NVL(optionSetting.getCollectResponseBytesKB(), "1000")));
			guiSetting.setOptionCollectErrorsChecked(optionSetting.checkCollectErrors());
			guiSetting.setOptionCollectIPChecked(optionSetting.checkCollectIP());
			guiSetting.setOptionCollectTPSChecked(optionSetting.checkCollectTPS());		
			if(saveMappingData) {
				guiSetting.setMappingLogType(fieldMapping.getLogType());
				guiSetting.setMappingDelimeter(fieldMapping.getDelimeter());
				guiSetting.setMappingBracelet(fieldMapping.getBracelet());
				guiSetting.setMappingTimestampType(fieldMapping.getTimestampType());
				guiSetting.setMappingOffsetHour(Float.parseFloat(StringUtil.NVL(fieldMapping.getOffsetHour(), "0.0")));
				guiSetting.setMappingTimeFormat(fieldMapping.getTimeFormat());
				guiSetting.setMappingTimeLocale(fieldMapping.getTimeLocale());
				guiSetting.setMappingElapsedUnit(fieldMapping.getElapsedUnit());
				guiSetting.setMappingData(fieldMapping.getMappingData());
				guiSetting.setURIMappingPatterns(fieldMapping.uriMappingManager.getURIPatterns());
			}
	
			List<Object> writeData = new ArrayList<Object>();
			writeData.add(Constant.SETTING_FILE_HEADER);
			writeData.add(guiSetting);
			
			FileUtil.writeObjectFile(f, writeData, false);
	
			Logger.debug("Saved this setting : " + f.getAbsolutePath());
		} catch(Exception e) {
			Logger.debug("Failed to save setting to the file : " + f.getAbsolutePath());
			Logger.error(e);
		}
	}

	public void resetSetting() {
		InputStream is = null;
		try {
			is = ClassLoader.getSystemResource(Constant.FILE_PATH_DEFAULTSETTING).openStream();
			loadSetting(is);
			outputSetting.txt_directory.setText(Constant.OUTPUT_DEFAULT_DIRECTORY);
		} catch(IOException ioe) {
			Logger.debug("Failed to reset setting from default values.");
			Logger.error(ioe);
		}
	}

	@SuppressWarnings("unchecked")
	protected boolean checkFileEncodings() {
		List<File> files = new ArrayList<File>();
		TableItem[] items = tbl_files.getItems();
		if(optionSetting.checkCheckFileEncoding()) {
			for(int i = 0; i < items.length; i++) {
				files.add((File)items[i].getData("file"));
			}
			FileEncodingCheckTask task = new FileEncodingCheckTask(files, fileEncodings);
			ProgressBarDialog progressBar = new ProgressBarDialog(shell, Utility.getFont());
			progressBar.setTitle("File Verification Progress");
			progressBar.setDetailViewCount(task.getFileCount());
			progressBar.setTask(task);
			progressBar.open();
			if(task.isSuccessed()) {
				fileEncodings = (Map<String,String>)task.getResultData();
				return true;
			}
			return false;
		} else {
			for(int i = 0; i < items.length; i++) {
				fileEncodings.put(((File)items[i].getData("file")).getPath(), "NULL");
			}
			return true;
		}
	}
	
	public String getFileEncoding(File f) {
		String encoding = fileEncodings.get(f.getPath());
		if(encoding == null) {
			if(optionSetting.checkCheckFileEncoding()) {
				encoding = FileUtil.getFileEncoding(f.getPath());
				if(encoding == null) {
					encoding = "NULL";
				} else if("WINDOWS-1252".equals(encoding)) {
					String default_encoding = System.getProperty("file.encoding");
					Logger.debug("Unknown file encoding : " + encoding + ". It will be set to default(" + default_encoding + ")");
					encoding = default_encoding;
				}			
				fileEncodings.put(f.getPath(), encoding);
			} else {
				fileEncodings.put(f.getPath(), "NULL");
			}
		}
		encoding = "NULL".equals(encoding) ? null : encoding;
		Logger.debug("File encoding : path='" + f.getPath() + "', encoding=" + encoding);
		return encoding;		
	}
	
	protected void executeAnalyze() {
		if(!checkFileEncodings()) {
			return;
		}
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
			Logger.debug(unit_cnt + " units of tx aggreation data will be created.");
			if(unit_cnt > Constant.WARNING_TXUNIT_COUNT) {
				if(!MessageUtil.showConfirmMessage(shell, "Too many("+unit_cnt+") tx aggreation data will be created by Time Range.\n"
						+ "Increase tpm aggregation minute, or ALYBA could slow down or down abnormally.\n\nDo you want to continue?")) {
					return;
				}
			}
		} catch(Exception e) {
			Logger.debug("Failed to update time range.");
			Logger.error(e);
		}
		
		filterSetting.resetDateUpdateCheck();
		
		LogAnalyzerSetting setting = getAnalyzerSetting();
		setting.setAnalyzeDate(new Date());
		LogAnalyzeOutput output = new LogAnalyzeOutput(setting);
		Logger.debug(setting.toString());
		
		if(!MessageUtil.showConfirmMessage(shell, "Do you want to analyze the files with current setting?")) {
			return;
		}

		String dbfile_path = outputSetting.getOutputDirectory() + File.separatorChar + output.getFileName() + ".adb";
		inProgressDbUtil = new ObjectDBUtil(dbfile_path, true);
		Logger.debug("Opened the database : dbfile=" + dbfile_path);
		
		ProgressBarDialog progressBar = new ProgressBarDialog(shell, Utility.getFont());
		progressBar.setTitle("Analyzer Progress");
		progressBar.setDetailViewCount(tbl_files.getItemCount());
		LogAnalyzeTask task = new LogAnalyzeTask(setting, DefaultParser.class);
		progressBar.setTask(task);
		progressBar.open();
		if(!task.isSuccessed()) {
			task.doCancel();
			String msg = task.getFailedMessage();
			if(msg != null) {
				MessageUtil.showWarningMessage(shell, msg);
			} else {
				MessageUtil.showWarningMessage(shell, "Unknown error");
			}
		}
		
		LogAnalyzeTask post_task = null;		
		if(task.isSuccessed() && setting.collectTPS) {
			progressBar = new ProgressBarDialog(shell);
			progressBar.setTitle("Analyzer Post-Progress");
			progressBar.setDetailViewCount(tbl_files.getItemCount());
			post_task = new LogAnalyzeTask(setting, PostParser.class);
			progressBar.setTask(post_task);
			progressBar.open();
			if(!post_task.isSuccessed()) {
				post_task.doCancel();
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
			Logger.debug("Closing the database : dbfile=" + dbfile_path);
			if(task.isSuccessed()) {
				inProgressDbUtil.closeAll();
			} else {
				inProgressDbUtil.closeAndDeleteDB();
			}
		} catch(Exception e) {
			Logger.debug("Failed to close the database.");
			Logger.error(e);
		} finally {
			inProgressDbUtil = null;
		}
		
		if(task.isSuccessed() && (output_task == null || output_task.isSuccessed())) {
			HistoryVO history = null;
			try {
				File f = new File(dbfile_path);
				history = new HistoryVO(f.getName(), f.getParent());
				history.setTitle(setting.getTitle()); 
				history.setCreated(setting.getAnalyzeDate().getTime());
				history.setVersion(Constant.PROGRAM_VERSION);
				if(historyView == null) {
					HistoryManager hm = null;
					try {
						hm = new HistoryManager();
						hm.addHistory(history);
					} catch(Exception e) {
						throw e;
					} finally {
						if(hm != null) {
							try { hm.close(); } catch(Exception ex) {}
						}
					}
				} else {
					historyView.addHistoryItem(history, true);
				}
			} catch(Exception e) {
				Logger.debug("Failed to add the result to the history : " + e.getMessage());
				Logger.error(e);
			}
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
							resultAnalyzer.loadDBFile(dbfile_path);
						} catch(Exception e) {
							Logger.debug("Failed to load the database : " + dbfile_path);
							Logger.error(e);
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
	
	private LogAnalyzerSetting getAnalyzerSetting() {
		LogFieldMappingInfo fieldMappingInfo = new LogFieldMappingInfo();
		fieldMappingInfo.setLogType(fieldMapping.getLogType());
		fieldMappingInfo.setJsonMapType(fieldMapping.isJsonMapType());
		fieldMappingInfo.setFieldDelimeter(StringUtil.replaceMetaCharacter(fieldMapping.getDelimeter(), false));
		fieldMappingInfo.setFieldBracelet(fieldMapping.getBracelet());
		fieldMappingInfo.setMappingInfo(fieldMapping.getMappingData());
		fieldMappingInfo.setURIMappingPatterns(fieldMapping.uriMappingManager.getURIPatterns());
		fieldMappingInfo.setTimestampType(fieldMapping.getTimestampType());
		fieldMappingInfo.setOffsetHour(Float.parseFloat(fieldMapping.getOffsetHour()));
		fieldMappingInfo.setTimeFormat(fieldMapping.getTimeFormat());
		fieldMappingInfo.setTimeLocale(fieldMapping.getTimeLocale());
		fieldMappingInfo.setElapsedUnit(fieldMapping.getElapsedUnit());
		fieldMappingInfo.setFieldCount(fieldMapping.getFieldCount());

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

		LogAnalyzerSetting setting = new LogAnalyzerSetting();
		setting.setTitle(txt_title.getText());
		setting.setAnalyzerTimezone(Constant.TIMEZONE_DEFAULT);
		setting.setLogFileList(getLogFileList());
		setting.setLogFileEncodingList(getFileEncodings());
		setting.setOutputExcelType(outputSetting.checkExcelType());
		setting.setOutputHtmlType(outputSetting.checkHtmlType());
		setting.setOutputTextType(outputSetting.checkTextType());
		setting.setOutputDirectory(outputSetting.getOutputDirectory());
		setting.setOutputSortBy((outputSetting.sortByCount() ? "COUNT" : "NAME"));
		setting.setMultiThreadParsing(optionSetting.checkMultiThread());
		setting.setCheckFieldCount(optionSetting.checkFixedFields());
		setting.setCheckStrict(optionSetting.checkStrictCheck());
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
		setting.setCollectIP(optionSetting.checkCollectIP());
		setting.setCollectTPS(optionSetting.checkCollectTPS());
		setting.setFieldMapping(fieldMappingInfo);
		setting.setFilterSetting(filterSettingInfo);
		
		return setting;
	}

	public static void printUsage() {
		System.out.println("");
		System.out.println("Usage: ALYBA [<option>]");
		System.out.println("       ALYBA -result [-debug] [Result_DB_File_to_Load]");
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
		System.out.println("     Open a ResultAnalyzer and load the file if Result_DB_File_to_Load is given.");
		System.out.println("");
	}
	
}
