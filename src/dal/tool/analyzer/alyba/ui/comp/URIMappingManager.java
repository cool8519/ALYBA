package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.yaml.snakeyaml.Yaml;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.urimapping.SpringProjectURIPatternExtractTask;
import dal.tool.analyzer.alyba.ui.urimapping.SpringScanResult;
import dal.tool.analyzer.alyba.ui.urimapping.SpringUriPatternExtractor;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.StringUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.ImageUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.ProgressBarDialog;

public class URIMappingManager extends Shell {

	public URIMappingManager instance;

	private static final String WRONG_TYPE_LEAD = "The selected file format does not match the content of this file.";
	private static final String MSG_LOAD_FILE_FAILED = "Failed to load URI patterns from the file.";
	private static final Pattern P_OPENAPI_OR_SWAGGER_KEY = Pattern.compile("(?m)^\\s*(openapi|swagger)\\s*:");
	private static final Pattern P_JAVA_PACKAGE = Pattern.compile("(?m)^\\s*package\\s+[a-zA-Z0-9_.]+\\s*;");
	private static final Pattern P_JAVA_CLASS = Pattern.compile("\\bclass\\s+[A-Za-z_][A-Za-z0-9_]*\\b");

	private Group grp_ftype;
	private Composite comp_loadButtons;
	private Button btn_openFiles;
	private Button btn_openDir;
	private Button btn_ftype_text;
	private Button btn_ftype_yaml;
	private Button btn_ftype_spring;
	private Button btn_removeSelected;
	private Button btn_removeAll;
	private Table tbl_uri_mapping;
	private TableViewer tblv_uri_mapping;
	private TableEditor tbl_editor;
	private TableColumn tblc_index;
	private TableColumn tblc_uri_mapping;

	public URIMappingManager(Display display, int style) {
		super(display, style);
		createContents();
		addEventListener();
		open();
		layout();
	}

	protected void createContents() {

		instance = this;
		
	    FormLayout forml_grp_setting = new FormLayout();
	    forml_grp_setting.marginHeight = 10;
	    forml_grp_setting.marginWidth = 10;
	    setLayout(forml_grp_setting);
		setSize(400, 600);
		setMinimumSize(400, 600);
		setImage(ImageUtil.getImage(Constant.IMAGE_PATH_TRAYICON));
		setText("URI Mapping Manager");

		FormData fd_comp_load = new FormData();
		fd_comp_load.top = new FormAttachment(this, 10);
		fd_comp_load.left = new FormAttachment(this, 0, SWT.LEFT);
		fd_comp_load.width = 110;
		comp_loadButtons = new Composite(this, SWT.NONE);
		comp_loadButtons.setLayoutData(fd_comp_load);
		GridLayout gl_load = new GridLayout(1, false);
		gl_load.verticalSpacing = 6;
		gl_load.marginWidth = 0;
		gl_load.marginHeight = 0;
		comp_loadButtons.setLayout(gl_load);

		btn_openFiles = new Button(comp_loadButtons, SWT.NONE);
		GridData gd_btn_openFiles = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_btn_openFiles.heightHint = 30;
		btn_openFiles.setLayoutData(gd_btn_openFiles);
		btn_openFiles.setFont(Utility.getFont());
		btn_openFiles.setText("Load from file(s)");

		btn_openDir = new Button(comp_loadButtons, SWT.NONE);
		GridData gd_btn_openDir = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_btn_openDir.heightHint = 30;
		btn_openDir.setLayoutData(gd_btn_openDir);
		btn_openDir.setFont(Utility.getFont());
		btn_openDir.setText("Load from folder");
		
		FormData fd_btn_removeSelected = new FormData();
		fd_btn_removeSelected.right = new FormAttachment(100);
		fd_btn_removeSelected.top = new FormAttachment(comp_loadButtons, 0, SWT.TOP);
		fd_btn_removeSelected.width = 120;
		fd_btn_removeSelected.height = 30;
		btn_removeSelected = new Button(this, SWT.NONE);
		btn_removeSelected.setLayoutData(fd_btn_removeSelected);
		btn_removeSelected.setFont(Utility.getFont());
		btn_removeSelected.setText("Remove Selected");

		FormData fd_btn_removeAll = new FormData();
		fd_btn_removeAll.right = new FormAttachment(btn_removeSelected, 0, SWT.RIGHT);
		fd_btn_removeAll.bottom = new FormAttachment(comp_loadButtons, 0, SWT.BOTTOM);
		fd_btn_removeAll.width = 120;
		fd_btn_removeAll.height = 30;
		btn_removeAll = new Button(this, SWT.NONE);
		btn_removeAll.setLayoutData(fd_btn_removeAll);
		btn_removeAll.setFont(Utility.getFont());
		btn_removeAll.setText("Remove All");
		
	    FormData fd_grp_ftype = new FormData();
	    fd_grp_ftype.top = new FormAttachment(comp_loadButtons, -12, SWT.TOP);
	    fd_grp_ftype.height = 65;
	    fd_grp_ftype.left = new FormAttachment(comp_loadButtons, 8, SWT.RIGHT);
	    fd_grp_ftype.right = new FormAttachment(btn_removeSelected, -15, SWT.LEFT);
	    GridLayout forml_grp_ftype = new GridLayout();
	    forml_grp_ftype.numColumns = 1;
	    forml_grp_ftype.marginHeight = 2;
	    forml_grp_ftype.marginWidth = 5;
	    grp_ftype = new Group(this, SWT.NONE);
	    grp_ftype.setLayoutData(fd_grp_ftype);
	    grp_ftype.setLayout(forml_grp_ftype);
	    grp_ftype.setFont(Utility.getFont());
	    grp_ftype.setText(" File Type ");

		btn_ftype_spring = new Button(grp_ftype, SWT.RADIO);
		btn_ftype_spring.setFont(Utility.getFont());
		btn_ftype_spring.setText("Spring Project");
		btn_ftype_spring.setToolTipText("Scan Springframework Controller source code based on annotations.");
		btn_ftype_spring.setSelection(true);

		btn_ftype_text = new Button(grp_ftype, SWT.RADIO);
		btn_ftype_text.setFont(Utility.getFont());
		btn_ftype_text.setText("Text");
		btn_ftype_text.setToolTipText("Plain text list of URI Patterns on each line.\n\nEx)\n/shop/product/{productId}\n/shop/book/{category}/list\n/shop/book/{bookId}/detail");

		btn_ftype_yaml = new Button(grp_ftype, SWT.RADIO);
		btn_ftype_yaml.setFont(Utility.getFont());
		btn_ftype_yaml.setText("OpenAPI Spec");
		btn_ftype_yaml.setToolTipText("OpenAPI Specification YAML.\n\nEx)\nopenapi: 3.0.0\ninfo:\n  title: Sample API\n  ...\nservers:\n  - url: /samples\\n    ...\npaths:\n  /user/{userId}:\n    get:\n      ...\n");
		
		FormData fd_tbl_uri_mapping = new FormData();
		fd_tbl_uri_mapping.top = new FormAttachment(grp_ftype, 8);
		fd_tbl_uri_mapping.left = new FormAttachment(0);
		fd_tbl_uri_mapping.right = new FormAttachment(100);
		fd_tbl_uri_mapping.bottom = new FormAttachment(100);
		tblv_uri_mapping = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		tbl_uri_mapping = tblv_uri_mapping.getTable();
		tbl_uri_mapping.setLayoutData(fd_tbl_uri_mapping);
		tbl_uri_mapping.setLinesVisible(true);
		tbl_uri_mapping.setHeaderVisible(true);
		tbl_uri_mapping.setFont(Utility.getFont());
		tblc_index = new TableColumn(tbl_uri_mapping, SWT.CENTER);
		tblc_index.setText("No.");
		tblc_index.setWidth(35);
		tblc_uri_mapping = new TableColumn(tbl_uri_mapping, SWT.LEFT);
		tblc_uri_mapping.setWidth(320);
		tblc_uri_mapping.setText("URI Pattern");
		tbl_editor = new TableEditor(tbl_uri_mapping);
		tbl_editor.horizontalAlignment = SWT.LEFT;
		tbl_editor.grabHorizontal = true;
		TableItem item = new TableItem(tbl_uri_mapping, SWT.NONE);
		item.setText(0, "");
		item.setText(1, "");

		setTabList(new Control[] { comp_loadButtons, btn_removeSelected, btn_removeAll });

	}

	protected void addEventListener() {

		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				AlybaGUI.instance.fieldMapping.uriMappingManager.setVisible(false);
				e.doit = false;
			}
		});

		btn_ftype_text.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLoadButtonsForFileType();
			}
		});
		btn_ftype_yaml.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLoadButtonsForFileType();
			}
		});
		btn_ftype_spring.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLoadButtonsForFileType();
			}
		});

		btn_openFiles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(btn_ftype_spring.getSelection()) {
					promptAndRunSpringScan(false);
				} else {
					List<File> files = FileDialogUtil.openReadDialogFiles(instance, Constant.ALLFILES_FILTER_NAMES, Constant.ALLFILES_FILTER_EXTS, Constant.DIALOG_INIT_PATH);
					if(files != null && files.size() > 0) {
						addPatternItems(files);
					}
				}
			}
		});

		btn_openDir.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(btn_ftype_spring.getSelection()) {
					promptAndRunSpringScan(true);
				}
			}
		});

		tbl_uri_mapping.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.keyCode == 127) {
					removeItems(tbl_uri_mapping.getSelectionIndices());
				}
			}
		});

		btn_removeSelected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeItems(tbl_uri_mapping.getSelectionIndices());
			}
		});

		btn_removeAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeAllPatterns();
			}
		});
		
		tbl_uri_mapping.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				Control oldEditor = tbl_editor.getEditor();
				if(oldEditor != null) {
					oldEditor.dispose();
				}
				Point pt = new Point(e.x, e.y);
				TableItem item = tbl_uri_mapping.getItem(pt);
				if(item == null) {
					return;
				}

				final Text newEditor = new Text(tbl_uri_mapping, SWT.LEFT);
				newEditor.setText(item.getText(1));
				newEditor.setFont(tbl_uri_mapping.getFont());
				newEditor.selectAll();
				newEditor.setFocus();

				Listener textListener = new Listener() {
					public void handleEvent(Event e) {
						if(e.type == SWT.FocusOut) {
							checkPatternValueAndSet(newEditor.getText(), tbl_editor.getItem(), tbl_uri_mapping.getSelectionIndex());
							newEditor.dispose();
						} else if(e.type == SWT.Traverse) {
							if(e.detail == SWT.TRAVERSE_RETURN) {
								checkPatternValueAndSet(newEditor.getText(), tbl_editor.getItem(), tbl_uri_mapping.getSelectionIndex());
								newEditor.dispose();
							} else if(e.detail == SWT.TRAVERSE_ESCAPE) {
								newEditor.dispose();
								e.doit = false;
							}
						}
					}
				};		
				newEditor.addListener(SWT.FocusOut, textListener);
				newEditor.addListener(SWT.Traverse, textListener);
				tbl_editor.minimumHeight = tbl_uri_mapping.getItemHeight();
				tbl_editor.minimumWidth = tbl_uri_mapping.getColumn(1).getWidth();				
				tbl_editor.setEditor(newEditor, item, 1);
			}
		});
		
		tblv_uri_mapping.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, Constant.FILE_TRANSFER_TYPE, new DropTargetListener() {
			public void drop(DropTargetEvent event) {
				try {
					Object data = event.data;
					if(data instanceof String[]) {
						String[] sourceFileList = (String[])data;
						if(sourceFileList != null && sourceFileList.length > 0) {
							if(btn_ftype_spring.getSelection()) {
								List<File> roots = new ArrayList<File>(sourceFileList.length);
								for(String f : sourceFileList) {
									roots.add(new File(f));
								}
								instance.getDisplay().asyncExec(new Runnable() {
									public void run() {
										try {
											Shell s = instance;
											if(s != null && !s.isDisposed()) {
												s.forceActive();
												s.forceFocus();
											}
											promptAndRunSpringScanFromRoots(roots);
										} catch(Exception ex) {
											Logger.error(ex);
											String msg = ex.getMessage();
											msg = (msg == null || msg.trim().equals("")) ? null : msg.trim();
											if(msg != null) {
												msg = msg.replaceAll("\\.\\s", "\\.\n");
												MessageUtil.showErrorMessage(instance, msg);
											} else {
												MessageUtil.showErrorMessage(instance, "Failed to load URI patterns from dropped file(s).");
											}
										}
									}
								});
							} else {
								List<File> files = new ArrayList<File>(sourceFileList.length);
								int dir_cnt = 0;
								for(String f : sourceFileList) {
									File file = new File(f);
									if(file.isDirectory()) {
										dir_cnt++;
										continue;
									}
									files.add(file);
								}
								if(dir_cnt > 0) {
									MessageUtil.showWarningMessage(instance, dir_cnt + " folder(s) are not supported. Please drop files only.");
								}
								if(files.size() > 0) {
									addPatternItems(files);
								}
							}
						}
					} else {
						MessageUtil.showErrorMessage(instance, "Failed to load URI patterns from dropped file(s).");
					}
				} catch(Exception ex) {
					Logger.error(ex);
					String msg = ex.getMessage();
					msg = (msg == null || msg.trim().equals("")) ? null : msg.trim();
					if(msg != null) {
						msg = msg.replaceAll("\\.\\s", "\\.\n");
						MessageUtil.showErrorMessage(instance, msg);
					} else {
						MessageUtil.showErrorMessage(instance, "Failed to load URI patterns from dropped file(s).");
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

	private void updateLoadButtonsForFileType() {
		boolean spring = btn_ftype_spring.getSelection();
		btn_openDir.setEnabled(spring);
	}

	private String promptContextPath() {
		InputDialog dlg = new InputDialog(instance, "Context Path", "Enter context path prefix for all URI patterns (e.g. /myapp or empty):", "", new IInputValidator() {
			public String isValid(String newText) {
				if(newText == null) {
					return null;
				}
				String t = newText.trim();
				if(t.length() > 0 && !t.startsWith("/")) {
					return "Context path must start with '/' or be empty.";
				}
				return null;
			}
		}) {
			@Override
			protected Point getInitialLocation(Point initialSize) {
				if(initialSize == null) {
					return super.getInitialLocation(initialSize);
				}
				return dal.util.swt.Utility.computeCenteredLocationOnMonitorOf(instance, initialSize.x, initialSize.y);
			}
		};
		if(dlg.open() != InputDialog.OK) {
			return null;
		}
		return dlg.getValue() != null ? dlg.getValue().trim() : "";
	}

	private void promptAndRunSpringScan(boolean directoryMode) {
		List<File> roots = new ArrayList<File>();
		if(directoryMode) {
			File d = FileDialogUtil.openReadDialogDirectory(instance, Constant.DIALOG_INIT_PATH);
			if(d != null) {
				roots.add(d);
			}
		} else {
			List<File> picked = FileDialogUtil.openReadDialogFiles(instance, Constant.JAVA_SOURCE_FILTER_NAMES, Constant.JAVA_SOURCE_FILTER_EXTS, Constant.DIALOG_INIT_PATH);
			if(picked != null && !picked.isEmpty()) {
				roots.addAll(picked);
			}
		}
		if(roots.isEmpty()) {
			return;
		}
		String ctx = promptContextPath();
		if(ctx == null) {
			return;
		}
		runSpringProjectScan(roots, ctx);
	}

	private void promptAndRunSpringScanFromRoots(List<File> roots) {
		if(roots == null || roots.isEmpty()) {
			return;
		}
		String ctx = promptContextPath();
		if(ctx == null) {
			return;
		}
		runSpringProjectScan(roots, ctx);
	}

	private void runSpringProjectScan(List<File> roots, String contextPath) {
		SpringProjectURIPatternExtractTask task = new SpringProjectURIPatternExtractTask(roots, contextPath);
		ProgressBarDialog dlg = new ProgressBarDialog(instance, Utility.getFont());
		dlg.setTitle("Spring Controller Scan");
		dlg.setDetailViewCount(1);
		dlg.setShowDetailIndicators(false);
		dlg.setConfirmOnComplete(true);
		dlg.setCompleteDecisionButtonLabels("Apply", "Cancel");
		dlg.setCompleteEmptySummaryMessage("No issues found during extraction.");
		dlg.setResizable(true);
		dlg.setDialogSize(620, 300);
		dlg.setMinimumDialogSize(620, 300);
		dlg.setTask(task);
		dlg.open();
		if(!task.isSuccessed()) {
			String fm = task.getFailedMessage();
			if(fm != null && fm.length() > 0) {
				MessageUtil.showWarningMessage(instance, fm);
			}
			return;
		}
		Object rd = task.getResultData();
		if(rd instanceof SpringScanResult) {
			if(dlg.isCompletionAccepted()) {
				applySpringPatterns(((SpringScanResult)rd).getPatterns());
			}
		}
	}

	private void applySpringPatterns(List<String> patterns) {
		if(patterns == null || patterns.isEmpty()) {
			MessageUtil.showInfoMessage(instance, "No URI patterns with { } were extracted.");
			return;
		}
		int from_idx = tbl_uri_mapping.getItemCount();
		int added = 0;
		int ignored = 0;
		for(String p : patterns) {
			if(addPattern(p)) {
				added++;
			} else {
				ignored++;
			}
		}
		if(added > 0) {
			Logger.debug(added + " Spring URI pattern(s) added.");
			tbl_uri_mapping.deselectAll();
			if(from_idx > 0 && tbl_uri_mapping.getItemCount() > 1) {
				tbl_uri_mapping.select(Math.max(0, from_idx - 1), tbl_uri_mapping.getItemCount() - 2);
				tbl_uri_mapping.showItem(tbl_uri_mapping.getItem(tbl_uri_mapping.getItemCount() - 2));
			}
			tbl_uri_mapping.forceFocus();
		}
		if(ignored > 0) {
			MessageUtil.showWarningMessage(instance, ignored + " pattern(s) were skipped (invalid or duplicate).");
		}
	}
	
	public void removeAllPatterns() {
		tbl_uri_mapping.removeAll();
		Logger.debug("Removed all pattern(s).");
		TableItem blank = new TableItem(tbl_uri_mapping, SWT.NONE);
		blank.setText(0, "");
		blank.setText(1, "");
	}
	
	public List<String> getURIPatterns() {
		if(tbl_uri_mapping.getItemCount() == 1 && tbl_uri_mapping.getItem(0).getText(1).equals("")) {
			return null;
		}
		TableItem[] items = tbl_uri_mapping.getItems();
		List<String> list = new ArrayList<String>(items.length-1);
		for(TableItem item : items) {
			if(item.getText(1).length() > 0) {
				list.add(item.getText(1));
			}
		}
		return list;
	}
	
	public void resetURIPatterns(List<String> patterns) {
		removeAllPatterns();
		if(patterns != null) {
			List<String> ready = SpringUriPatternExtractor.sortAndDedupe(patterns);
			for(String pattern : ready) {
				addPattern(pattern);
			}
		}
	}
	
	private void addPatternItems(List<File> files) {
		int from_idx = tbl_uri_mapping.getItemCount();
		List<String> collected = new ArrayList<String>();
		int file_fail_cnt = 0;
		String firstError = null;
		for(int i = 0; i < files.size(); i++) {
			File f = (File)files.get(i);
			String head = peekFileHeadUtf8(f, 8192);
			Reader reader = null;
			try {
				if(btn_ftype_text.getSelection()) {
					if(looksLikeOpenApiYaml(head) || looksLikeJavaWithClass(head)) {
						file_fail_cnt++;
						if(firstError == null) {
							firstError = WRONG_TYPE_LEAD;
						}
						continue;
					}
					reader = new LineNumberReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
					LineNumberReader lr = (LineNumberReader)reader;
					String line;
					while((line = lr.readLine()) != null) {
						String pattern = removeQuotation(line);
						if(!pattern.isEmpty()) {
							collected.add(pattern);
						}
					}
				} else if(btn_ftype_yaml.getSelection()) {
					if(looksLikeJavaWithClass(head) || (looksLikePlainUriPathList(head) && !looksLikeOpenApiYaml(head))) {
						file_fail_cnt++;
						if(firstError == null) {
							firstError = WRONG_TYPE_LEAD;
						}
						continue;
					}
					reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
					Yaml yaml = new Yaml();
					Map<String, Object> yamlMaps = yaml.load(reader);
					List<String> patterns = extractUriPatternsFromOpenApiYaml(yamlMaps);
					if(patterns == null) {
						file_fail_cnt++;
						if(firstError == null) {
							firstError = WRONG_TYPE_LEAD;
						}
						continue;
					}
					collected.addAll(patterns);
				} else {
					Logger.debug("addPatternItems called with Spring type — use Spring scan flow.");
				}
			} catch(Exception ex) {
				file_fail_cnt++;
				Logger.debug("Failed to read patterns from the file : " + f.getAbsolutePath());
				Logger.error(ex);
				if(firstError == null) {
					firstError = btn_ftype_yaml.getSelection() ? WRONG_TYPE_LEAD : MSG_LOAD_FILE_FAILED;
				}
			} finally {
				if(reader != null) {
					try { reader.close(); } catch(Exception e) {}
				}
			}
		}
		List<String> ready = SpringUriPatternExtractor.sortAndDedupe(collected);
		int preTableFiltered = collected.size() - ready.size();
		int s_cnt = 0;
		int f_cnt = preTableFiltered;
		for(String p : ready) {
			if(addPattern(p)) {
				s_cnt++;
			} else {
				f_cnt++;
				Logger.debug("Ignored the pattern after sort/dedupe/regex filter: " + p);
			}
		}
		if(s_cnt > 0) {
			Logger.debug(s_cnt + " items are added successfully.");
			tbl_uri_mapping.deselectAll();
			tbl_uri_mapping.select(from_idx-1, tbl_uri_mapping.getItemCount() - 2);
			tbl_uri_mapping.showItem(tbl_uri_mapping.getItem(tbl_uri_mapping.getItemCount() - 2));
			tbl_uri_mapping.forceFocus();
		}
		if(f_cnt > 0) {
			MessageUtil.showWarningMessage(instance, f_cnt + " pattern(s) has been ignored.");
		}
		if(file_fail_cnt > 0 && firstError != null) {
			MessageUtil.showErrorMessage(instance, firstError);
		}
	}

	private static String peekFileHeadUtf8(File f, int maxLen) {
		try(FileInputStream in = new FileInputStream(f)) {
			byte[] buf = new byte[maxLen];
			int n = in.read(buf);
			if(n <= 0) {
				return "";
			}
			return new String(buf, 0, n, StandardCharsets.UTF_8);
		} catch(IOException e) {
			return "";
		}
	}

	private static boolean looksLikeOpenApiYaml(String head) {
		return head != null && P_OPENAPI_OR_SWAGGER_KEY.matcher(head).find();
	}

	private static boolean looksLikeJavaWithClass(String head) {
		return head != null && P_JAVA_PACKAGE.matcher(head).find() && P_JAVA_CLASS.matcher(head).find();
	}

	private static boolean looksLikePlainUriPathList(String head) {
		if(head == null || head.isEmpty()) {
			return false;
		}
		int slash = 0;
		int other = 0;
		for(String line : head.split("\\R")) {
			String t = line.trim();
			if(t.isEmpty() || t.startsWith("#")) {
				continue;
			}
			if(t.startsWith("/")) {
				slash++;
			} else {
				other++;
			}
			if(slash + other >= 10) {
				break;
			}
		}
		return slash >= 2 && slash > other;
	}

	@SuppressWarnings("unchecked")
	private static List<String> extractUriPatternsFromOpenApiYaml(Map<String, Object> yamlMaps) {
		if(yamlMaps == null) {
			return null;
		}
		Object o_version = yamlMaps.get("swagger") != null ? yamlMaps.get("swagger") : yamlMaps.get("openapi");
		if(o_version == null) {
			return null;
		}
		String version = o_version.toString();
		Object pathsObj = yamlMaps.get("paths");
		if(!(pathsObj instanceof Map)) {
			return null;
		}
		Map<String, ?> pathsMap = (Map<String, ?>)pathsObj;
		String basePath;
		try {
			if(version.startsWith("2")) {
				Object bp = yamlMaps.get("basePath");
				basePath = bp != null ? bp.toString() : "";
			} else if(version.startsWith("3")) {
				Object serversObj = yamlMaps.get("servers");
				if(!(serversObj instanceof List) || ((List<?>)serversObj).isEmpty()) {
					return null;
				}
				Object firstServer = ((List<?>)serversObj).get(0);
				if(!(firstServer instanceof Map)) {
					return null;
				}
				Object urlObj = ((Map<String, Object>)firstServer).get("url");
				basePath = urlObj != null ? urlObj.toString() : "";
				if(basePath.indexOf("://") > 0) {
					basePath = new URL(basePath).getPath();
				}
			} else {
				return null;
			}
		} catch(Exception e) {
			return null;
		}
		if(basePath.endsWith("/") && basePath.length() > 1) {
			basePath = basePath.substring(0, basePath.length() - 1);
		}
		List<String> out = new ArrayList<String>(pathsMap.size());
		for(String item : pathsMap.keySet()) {
			out.add(basePath + item);
		}
		return out;
	}

	private String removeQuotation(String value) {
		String s = value.trim();
		s = (s.startsWith("'") || s.startsWith("\"")) ? s.substring(1) : s;
		s = (s.endsWith("'") || s.endsWith("\"")) ? s.substring(0, s.length()-1) : s;
		return s.trim();
	}
	
	private void removeItems(int[] indices) {
		if(indices.length < 1 || tbl_uri_mapping.getItemCount() == 1) {
			return;
		} else {
			int cnt = 0;
			for(int i = indices.length-1; i >= 0; i--) {
				if(indices[i] < tbl_uri_mapping.getItemCount() || tbl_uri_mapping.getItem(indices[i]).getText(1).length() > 0) {
					tbl_uri_mapping.remove(indices[i]);
					cnt++;
				}
			}
			for(int i = 0; i < tbl_uri_mapping.getItemCount()-1; i++) {
				tbl_uri_mapping.getItem(i).setText(0, String.valueOf(i+1));
			}
			if(tbl_uri_mapping.getItemCount() == 1) {
				tbl_uri_mapping.getItem(0).setText(0, "");
			}
			if(cnt > 0) {
				Logger.debug("Removed " + cnt + " item(s).");
			}
		}
		if(tbl_uri_mapping.getItemCount() == 0) {
			TableItem blank = new TableItem(tbl_uri_mapping, SWT.NONE);
			blank.setText(0, "");
			blank.setText(1, "");
		}
		tbl_uri_mapping.deselectAll();
	}
	
	private boolean addPattern(String value) {
		try {
			validatePatternValue(value, -1);
		} catch(Exception e) {
			return false;
		}		
		TableItem item = tbl_uri_mapping.getItem(tbl_uri_mapping.getItemCount()-1);
		item.setText(0, String.valueOf(tbl_uri_mapping.getItemCount()));
		item.setText(1, value);
		TableItem blank = new TableItem(tbl_uri_mapping, SWT.NONE);
		blank.setText(0, "");
		blank.setText(1, "");
		return true;
	}
	
	private void validateRegexCompilationForUriPattern(String patternValue) {
		String pattern_str = patternValue;
		String regex = "";
		int prev = 0;
		int init = pattern_str.indexOf("{");
		int end = pattern_str.indexOf("}", init);
		while(init > -1 && end > -1) {
			String before = pattern_str.substring(prev, init).replaceAll("/", "\\/").replaceAll("\\.", "\\\\.");
			String pattern_str_in = pattern_str.substring(init + 1, end);
			String regex_str = "[^/]+";
			if(pattern_str_in.indexOf(":") > -1) {
				regex_str = pattern_str_in.substring(pattern_str_in.indexOf(":") + 1);
				int openCnt;
				String added = regex_str;
				while((openCnt = StringUtil.countCharacters(added, '{')) > 0) {
					added = "";
					for(int i = 0; i < openCnt; i++) {
						prev = end;
						init = end;
						end = pattern_str.indexOf("}", init + 1);
						added += pattern_str.substring(init, end);
					}
					regex_str += added;
				}
				regex_str = regex_str.replaceAll("\\\\\\\\", "\\\\");
			}
			regex += before + "(" + regex_str + ")";
			prev = end + 1;
			init = pattern_str.indexOf("{", prev);
			end = pattern_str.indexOf("}", init);
		}
		if(prev < pattern_str.length()) {
			regex += pattern_str.substring(prev).replaceAll("/", "\\/").replaceAll("\\.", "\\\\.");
		}
		Pattern.compile("^" + regex + "$");
	}

	private void validatePatternValue(String value, int ignoreIdx) throws Exception {
		// check syntax
		if(value.startsWith("/") == false) {
			String msg = "Failed to validate a pattern. The URI must start with '/'.";
			Logger.debug(msg);
			throw new Exception(msg);
		} else if(value.indexOf('{') < 0 || value.indexOf('}') < 0) {
			String msg = "Failed to validate a pattern. The pattern must contain with '{' and '}'.";
			Logger.debug(msg);
			throw new Exception(msg);
		}
		if(StringUtil.checkParentheses(value, "{}") == false) {
			String msg = "Failed to validate a pattern. The parentheses in the pattern do not match.";
			Logger.debug(msg);
			throw new Exception(msg);
		}
		// check regex compilation (Spring-style {var:regex})
		try {
			validateRegexCompilationForUriPattern(value);
		} catch(Exception e) {
			String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			String msg = "Failed to validate a pattern. Invalid regex in the pattern: " + reason;
			Logger.debug(msg);
			throw new Exception(msg);
		}
		// check duplicate
		TableItem[] items = tbl_uri_mapping.getItems();
		for(int i = 0; i < items.length; i++) {
			if(i != ignoreIdx && items[i].getText(1).equals(value)) {
				String msg = "Failed to validate a pattern. The input value already exists.";
				Logger.debug(msg);
				throw new Exception(msg);
			}
		}
	}
	
	private void checkPatternValueAndSet(String value, TableItem item, int index) {
		value = value.trim();
		if(!value.equals("")) {
			try {
				validatePatternValue(value, index);
			} catch(Exception e) {
				MessageUtil.showErrorMessage(instance, e.getMessage().replaceAll("\\.\\s", "\\.\n"));
				return;
			}
		}
		item.setText(1, value);
		int count = tbl_uri_mapping.getItemCount();
		if(value.equals("")) {
			removeItems(new int[]{index});
		} else {
			if(index == count-1) {
				tbl_uri_mapping.getItem(count-1).setText(0, String.valueOf(count));
				TableItem blank = new TableItem(tbl_uri_mapping, SWT.NONE);
				blank.setText(0, "");
				blank.setText(1, "");
			}
		}
	}
	
	protected void checkSubclass() {
	}

}
