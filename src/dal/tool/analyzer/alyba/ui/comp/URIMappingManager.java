package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
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
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.StringUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.MessageUtil;

public class URIMappingManager extends Shell {

	public URIMappingManager instance;
	private Group grp_ftype;
	private Button btn_openFiles;
	private Button btn_ftype_text;
	private Button btn_ftype_yaml;
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
		setSize(400, 500);
		setText("URI Mapping Manager");

		FormData fd_btn_openFiles = new FormData();
		fd_btn_openFiles.left = new FormAttachment(this, 0, SWT.LEFT);
		fd_btn_openFiles.top = new FormAttachment(this, 5);
		fd_btn_openFiles.width = 100;
		fd_btn_openFiles.height = 55;
		btn_openFiles = new Button(this, SWT.NONE);
		btn_openFiles.setLayoutData(fd_btn_openFiles);
		btn_openFiles.setFont(Utility.getFont());
		btn_openFiles.setText("Load from file");		
		
		FormData fd_btn_removeSelected = new FormData();
		fd_btn_removeSelected.right = new FormAttachment(100);
		fd_btn_removeSelected.top = new FormAttachment(btn_openFiles, 0, SWT.TOP);
		fd_btn_removeSelected.width = 120;
		fd_btn_removeSelected.height = 25;
		btn_removeSelected = new Button(this, SWT.NONE);
		btn_removeSelected.setLayoutData(fd_btn_removeSelected);
		btn_removeSelected.setFont(Utility.getFont());
		btn_removeSelected.setText("Remove Selected");

		FormData fd_btn_removeAll = new FormData();
		fd_btn_removeAll.right = new FormAttachment(btn_removeSelected, 0, SWT.RIGHT);
		fd_btn_removeAll.bottom = new FormAttachment(btn_openFiles, 0, SWT.BOTTOM);
		fd_btn_removeAll.width = 120;
		fd_btn_removeAll.height = 25;
		btn_removeAll = new Button(this, SWT.NONE);
		btn_removeAll.setLayoutData(fd_btn_removeAll);
		btn_removeAll.setFont(Utility.getFont());
		btn_removeAll.setText("Remove All");
		
	    FormData fd_grp_ftype = new FormData();
	    fd_grp_ftype.left = new FormAttachment(btn_openFiles, 5, SWT.RIGHT);
	    fd_grp_ftype.right = new FormAttachment(btn_removeSelected, -15, SWT.LEFT);
	    fd_grp_ftype.top = new FormAttachment(btn_openFiles, -5, SWT.TOP);
	    fd_grp_ftype.bottom = new FormAttachment(btn_openFiles, 0, SWT.BOTTOM);
	    RowLayout forml_grp_ftype = new RowLayout();
	    forml_grp_ftype.marginHeight = 2;
	    forml_grp_ftype.marginWidth = 5;
	    grp_ftype = new Group(this, SWT.NONE);
	    grp_ftype.setLayoutData(fd_grp_ftype);
	    grp_ftype.setLayout(forml_grp_ftype);
	    grp_ftype.setFont(Utility.getFont());
	    grp_ftype.setText(" File Type ");
	    
		btn_ftype_text = new Button(grp_ftype, SWT.RADIO);
		btn_ftype_text.setFont(Utility.getFont());
		btn_ftype_text.setText("Text");
		btn_ftype_text.setToolTipText("Plain text list of URI Patterns on each line.\n\nEx)\n/shop/product/{productId}\n/shop/book/{category}/list\n/shop/book/{bookId}/detail");
		btn_ftype_text.setSelection(true);

		btn_ftype_yaml = new Button(grp_ftype, SWT.RADIO);
		btn_ftype_yaml.setFont(Utility.getFont());
		btn_ftype_yaml.setText("OpenAPI Spec");
		btn_ftype_yaml.setToolTipText("OpenAPI Specification YAML.\n\nEx)\nopenapi: 3.0.0\ninfo:\n  title: Sample API\n  ...\nservers:\n  - url: /samples\\n    ...\npaths:\n  /user/{userId}:\n    get:\n      ...\n");
		
		FormData fd_tbl_uri_mapping = new FormData();
		fd_tbl_uri_mapping.left = new FormAttachment(0);
		fd_tbl_uri_mapping.right = new FormAttachment(100);
		fd_tbl_uri_mapping.top = new FormAttachment(btn_openFiles, 10);
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
		tblc_uri_mapping.setResizable(false);
		tblc_uri_mapping.setText("URI Pattern");
		tbl_editor = new TableEditor(tbl_uri_mapping);
		tbl_editor.horizontalAlignment = SWT.LEFT;
		tbl_editor.grabHorizontal = true;
		TableItem item = new TableItem(tbl_uri_mapping, SWT.NONE);
		item.setText(0, "");
		item.setText(1, "");

		setTabList(new Control[] { btn_openFiles, btn_removeSelected, btn_removeAll });

	}

	protected void addEventListener() {

		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				AlybaGUI.instance.fieldMapping.uriMappingManager.setVisible(false);
				e.doit = false;
			}
		});

		btn_openFiles.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				List<File> files = FileDialogUtil.openReadDialogFiles(instance, Constant.ALLFILES_FILTER_NAMES, Constant.ALLFILES_FILTER_EXTS, Constant.DIALOG_INIT_PATH);
				if(files != null && files.size() > 0) {
					addPatternItems(files);
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
				String[] sourceFileList = (String[])event.data;
				if(sourceFileList != null && sourceFileList.length > 0) {
					List<File> files = new ArrayList<File>(sourceFileList.length);
					for(String f : sourceFileList) {
						files.add(new File(f));
					}
					addPatternItems(files);
				}
			}
			public void dropAccept(DropTargetEvent event) {}
			public void dragOver(DropTargetEvent event) {}
			public void dragOperationChanged(DropTargetEvent event) {}
			public void dragLeave(DropTargetEvent event) {}
			public void dragEnter(DropTargetEvent event) {}
		});
				
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
			for(String pattern : patterns) {
				addPattern(pattern);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addPatternItems(List<File> files) {
		int from_idx = tbl_uri_mapping.getItemCount();
		int s_cnt = 0;
		int f_cnt = 0;
		for(int i = 0; i < files.size(); i++) {
			File f = (File)files.get(i);			
			Reader reader = null;
			try {
				if(btn_ftype_text.getSelection()) {
					// Text type
					reader = new LineNumberReader(new InputStreamReader(new FileInputStream(f)));
					LineNumberReader lr = (LineNumberReader)reader;
					String line;
					while((line = lr.readLine()) != null) {
						String pattern = removeQuotation(line);					
						if(addPattern(pattern)) {
							s_cnt++;
						} else {
							f_cnt++;
							Logger.debug("Ignored the pattern from line " + lr.getLineNumber() + " (" + f.getCanonicalPath() + ") : " + line);
						}
					}
				} else {
					// OpenAPI type
					reader = new FileReader(f);
					Yaml yaml = new Yaml();
					Map<String, Object> yamlMaps = yaml.load(reader);
					Object o_version = yamlMaps.get("swagger") != null ? yamlMaps.get("swagger") : yamlMaps.get("openapi");
					if(o_version == null) {
						throw new Exception("Cannot get version of OpenAPI Spec.");
					}
					String version = (String)o_version;
					String basePath = null;
					Set<String> patterns = null;
					if(version.startsWith("2")) {
						basePath = (String)yamlMaps.get("basePath");
						basePath = (basePath==null ? "" : basePath);
						patterns = ((Map<String,Object>)yamlMaps.get("paths")).keySet();
					} else if(version.startsWith("3")) {
						List<Map<String,Object>> basePaths = ((List<Map<String,Object>>)yamlMaps.get("servers"));
						basePath = (String)((Map<String,Object>)basePaths.get(0)).get("url");
						basePath = (basePath==null ? "" : basePath);
						basePath = (basePath.indexOf("://")>0) ? new URL(basePath).getPath() : basePath;
						patterns = ((Map<String,Object>)yamlMaps.get("paths")).keySet();
					} else {
						throw new Exception("Not support version of OpenAPI Spec.");
					}
					basePath = basePath.endsWith("/") ? basePath.substring(0, basePath.length()-1) : basePath;
					for(String item : patterns) {
						String pattern = basePath + item;					
						if(addPattern(pattern)) {
							s_cnt++;
						} else {
							f_cnt++;
							Logger.debug("Ignored the pattern : " + pattern);
						}
					}
				}
			} catch(Exception ex) {
				Logger.debug("Failed to read patterns from the file : " + f.getAbsolutePath());
				Logger.error(ex);
			} finally {
				if(reader != null) {
					try { reader.close(); } catch(Exception e) {}
				}
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
