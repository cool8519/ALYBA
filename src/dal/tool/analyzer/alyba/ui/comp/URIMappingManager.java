package dal.tool.analyzer.alyba.ui.comp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.swt.FileDialogUtil;

public class URIMappingManager extends Shell {

	public URIMappingManager instance;
	private Button btn_openFiles;
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
		setSize(360, 500);
		setText("URI Mapping Manager");

		FormData fd_btn_openFiles = new FormData();
		fd_btn_openFiles.left = new FormAttachment(this, 0, SWT.LEFT);
		fd_btn_openFiles.top = new FormAttachment(this, 0);
		fd_btn_openFiles.width = 120;
		fd_btn_openFiles.height = 50;
		btn_openFiles = new Button(this, SWT.NONE);
		btn_openFiles.setLayoutData(fd_btn_openFiles);
		btn_openFiles.setFont(Utility.getFont());
		btn_openFiles.setText("Load from file");		
		
		FormData fd_btn_removeSelected = new FormData();
		fd_btn_removeSelected.right = new FormAttachment(100);
		fd_btn_removeSelected.top = new FormAttachment(btn_openFiles, 0, SWT.TOP);
		fd_btn_removeSelected.width = 120;
		fd_btn_removeSelected.height = 23;
		btn_removeSelected = new Button(this, SWT.NONE);
		btn_removeSelected.setLayoutData(fd_btn_removeSelected);
		btn_removeSelected.setFont(Utility.getFont());
		btn_removeSelected.setText("Remove Selected");

		FormData fd_btn_removeAll = new FormData();
		fd_btn_removeAll.right = new FormAttachment(btn_removeSelected, 0, SWT.RIGHT);
		fd_btn_removeAll.bottom = new FormAttachment(btn_openFiles, 0, SWT.BOTTOM);
		fd_btn_removeAll.width = 120;
		fd_btn_removeAll.height = 23;
		btn_removeAll = new Button(this, SWT.NONE);
		btn_removeAll.setLayoutData(fd_btn_removeAll);
		btn_removeAll.setFont(Utility.getFont());
		btn_removeAll.setText("Remove All");
		
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
		tblc_uri_mapping.setWidth(270);
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
				tbl_uri_mapping.removeAll();
				Logger.debug("Removed all pattern(s).");
				TableItem blank = new TableItem(tbl_uri_mapping, SWT.NONE);
				blank.setText(0, "");
				blank.setText(1, "");
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
	
	private void addPatternItems(List<File> files) {
		int from_idx = tbl_uri_mapping.getItemCount();
		int cnt = 0;
		for(int i = 0; i < files.size(); i++) {
			File f = (File)files.get(i);
			BufferedReader br = null;
			LineNumberReader lr = null;
			String line;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				lr = new LineNumberReader(br);
				while((line = lr.readLine()) != null) {
					String pattern = removeQuotation(line);					
					if(addPattern(pattern)) {
						cnt++;
					} else {
						Logger.debug("Failed to add a pattern from line " + lr.getLineNumber() + " (" + f.getCanonicalPath() + ") : " + line);
					}
				}
			} catch(Exception ex) {
				Logger.debug("Failed to read patterns from the file : " + f.getAbsolutePath());
				Logger.error(ex);
			}
		}
		if(cnt > 0) {
			Logger.debug(cnt + " items are added successfully.");
			tbl_uri_mapping.deselectAll();
			tbl_uri_mapping.select(from_idx-1, tbl_uri_mapping.getItemCount() - 2);
			tbl_uri_mapping.showItem(tbl_uri_mapping.getItem(tbl_uri_mapping.getItemCount() - 2));
			tbl_uri_mapping.forceFocus();
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
				if(indices[i] < tbl_uri_mapping.getItemCount() && tbl_uri_mapping.getItem(indices[i]).getText(1).length() > 0) {
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
		if(!validatePatternValue(value, -1)) {
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
	
	private boolean validatePatternValue(String value, int ignoreIdx) {
		// check syntax
		if(value.startsWith("/") == false) {
			Logger.debug("Failed to register a pattern. The URI must start with '/'.");
			return false;
		} else if(value.indexOf('{') < 0 || value.indexOf('}') < 0) {
			Logger.debug("Failed to register a pattern. The pattern must contain with '{' and '}'.");
			return false;
		}		
		// check duplicate
		TableItem[] items = tbl_uri_mapping.getItems();
		for(int i = 0; i < items.length; i++) {
			if(i != ignoreIdx && items[i].getText(1).equals(value)) {
				Logger.debug("Failed to register a pattern. The input value already exists.");
				return false;
			}
		}
		return true;
	}
	
	private void checkPatternValueAndSet(String value, TableItem item, int index) {
		value = value.trim();
		if(!value.equals("")) {
			if(!validatePatternValue(value, index)) {
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
