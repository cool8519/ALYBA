package dal.tool.analyzer.alyba.ui.comp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.parse.FieldIndex;
import dal.tool.analyzer.alyba.parse.ParserUtil;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.FileUtil;
import dal.util.NumberUtil;
import dal.util.StringUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class TPMFieldMapping extends Composite {

	private static String[] FILE_TYPES = { "Customize" };
	private static String[] TIME_UNITS = { "minute(s)", "second(s)" };

	private TPMInsert owner;
	
	private TableItem draggingItem;
	private HashMap<String, String> mappingData;
	private int fieldCount;
	private Locale timeLocale;

	private Group grp_customizeMapping;
	public CCombo cb_fileType;
	public CCombo cb_timeFormat;
	public CCombo cb_unit;
	public Spinner spn_offset;
	public Spinner spn_unit;
	private Table tbl_line;
	private TableViewer tblv_line;
	public Text txt_delimeter;
	public Text txt_bracelet;
	private Text txt_time;
	private Text txt_count;

	private Button btn_sampling;
	private Button btn_resetMapping;
	private Label lb_resultTimeChk;
	private DragSource ds_tableItem;
	private DropTarget dt_textTime;
	private DropTarget dt_textCount;

	public TPMFieldMapping(Composite parent, int style, TPMInsert owner) {
		super(parent, style);
		this.owner = owner;
		createContents();
		addEventListener();
		mappingData = new HashMap<String, String>();
	}
	
	public void setMappingData(HashMap<String, String> mappingData) {
		this.mappingData = mappingData;
	}
	
	public void setTimeLocale(Locale timeLocale) {
		this.timeLocale = timeLocale;
	}

	public String getFileType() {
		return cb_fileType.getText();
	}
	
	public String getBracelet() {
		return txt_bracelet.getText();
	}

	public String getDelimeter() {
		return txt_delimeter.getText();
	}

	public String getOffsetHour() {
		return spn_offset.getText();
	}

	public String getTimeFormat() {
		return cb_timeFormat.getText();
	}
	
	public String getCountUnit() {
		return spn_unit.getText();
	}

	public boolean isTPS() {
		return cb_unit.getText().equals(TIME_UNITS[1]);
	}
	
	public HashMap<String, String> getMappingData() {
		return mappingData;
	}

	public int getFieldCount() {
		return fieldCount;
	}

	public Locale getTimeLocale() {
		return timeLocale;
	}
	
	protected void createContents() {

	    FormLayout forml_main = new FormLayout();
	    forml_main.marginHeight = 10;
	    forml_main.marginWidth = 10;
	    setLayout(forml_main);

		FormData fd_lb_fileType = new FormData();
		fd_lb_fileType.left = new FormAttachment(0);
		fd_lb_fileType.top = new FormAttachment(0, 6);
		fd_lb_fileType.width = 55;
		fd_lb_fileType.height = 15;
		Label lb_fileType = new Label(this, SWT.NONE);
		lb_fileType.setLayoutData(fd_lb_fileType);
		lb_fileType.setFont(Utility.getFont());
		lb_fileType.setText("File Type");
		lb_fileType.setAlignment(SWT.RIGHT);

		FormData fd_cb_fileType = new FormData();
		fd_cb_fileType.left = new FormAttachment(lb_fileType, 5, SWT.RIGHT);
		fd_cb_fileType.top = new FormAttachment(0, 5);
		fd_cb_fileType.width = 100;
		fd_cb_fileType.height = 15;
		cb_fileType = new CCombo(this, SWT.BORDER | SWT.READ_ONLY);
		cb_fileType.setLayoutData(fd_cb_fileType);
		cb_fileType.setVisibleItemCount(6);
		cb_fileType.setItems(FILE_TYPES);
		cb_fileType.setFont(Utility.getFont());
		cb_fileType.setText(FILE_TYPES[0]);
		cb_fileType.setBounds(71, 11, 140, 21);

		FormData fd_lb_delimeter = new FormData();
		fd_lb_delimeter.left = new FormAttachment(cb_fileType, 45, SWT.RIGHT);
		fd_lb_delimeter.top = new FormAttachment(0, 6);
		fd_lb_delimeter.width = 60;
		fd_lb_delimeter.height = 15;
		Label lb_delimeter = new Label(this, SWT.NONE);
		lb_delimeter.setLayoutData(fd_lb_delimeter);
		lb_delimeter.setFont(Utility.getFont());
		lb_delimeter.setText("Delimeter");
		lb_delimeter.setAlignment(SWT.RIGHT);

		FormData fd_txt_delimeter = new FormData();
		fd_txt_delimeter.left = new FormAttachment(lb_delimeter, 5, SWT.RIGHT);
		fd_txt_delimeter.top = new FormAttachment(0, 5);
		fd_txt_delimeter.width = 60;
		fd_txt_delimeter.height = 14;
		txt_delimeter = new Text(this, SWT.BORDER);
		txt_delimeter.setLayoutData(fd_txt_delimeter);
		txt_delimeter.setFont(Utility.getFont());
		txt_delimeter.setText(StringUtil.replaceMetaCharacter(Constant.FILE_DEFAULT_DELIMETER, true));

		FormData fd_lb_bracelet = new FormData();
		fd_lb_bracelet.left = new FormAttachment(lb_delimeter, 0, SWT.LEFT);
		fd_lb_bracelet.top = new FormAttachment(txt_delimeter, 6);
		fd_lb_bracelet.width = 60;
		fd_lb_bracelet.height = 15;
		Label lb_bracelet = new Label(this, SWT.NONE);
		lb_bracelet.setLayoutData(fd_lb_bracelet);
		lb_bracelet.setFont(Utility.getFont());
		lb_bracelet.setText("Bracelet");
		lb_bracelet.setAlignment(SWT.RIGHT);

		FormData fd_txt_bracelet = new FormData();
		fd_txt_bracelet.left = new FormAttachment(txt_delimeter, 0, SWT.LEFT);
		fd_txt_bracelet.top = new FormAttachment(txt_delimeter, 5);
		fd_txt_bracelet.width = 60;
		fd_txt_bracelet.height = 14;
		txt_bracelet = new Text(this, SWT.BORDER);
		txt_bracelet.setLayoutData(fd_txt_bracelet);
		txt_bracelet.setFont(Utility.getFont());
		txt_bracelet.setText(StringUtil.getStringFromArray(Constant.FILE_DEFAULT_BRACELETS, " "));

		FormData fd_btn_sampling = new FormData();
		fd_btn_sampling.left = new FormAttachment(txt_delimeter, 10, SWT.RIGHT);
		fd_btn_sampling.top = new FormAttachment(txt_delimeter, -1, SWT.TOP);
		fd_btn_sampling.bottom = new FormAttachment(txt_delimeter, 0, SWT.BOTTOM);
		fd_btn_sampling.width = 100;
		btn_sampling = new Button(this, SWT.NONE);
		btn_sampling.setLayoutData(fd_btn_sampling);
		btn_sampling.setFont(Utility.getFont());
		btn_sampling.setText("Sampling");
		
		FormData fd_btn_resetMapping = new FormData();
		fd_btn_resetMapping.right = new FormAttachment(100);
		fd_btn_resetMapping.top = new FormAttachment(btn_sampling, 0, SWT.TOP);
		fd_btn_resetMapping.bottom = new FormAttachment(btn_sampling, 0, SWT.BOTTOM);
		fd_btn_resetMapping.width = 100;
		btn_resetMapping = new Button(this, SWT.NONE);
		btn_resetMapping.setLayoutData(fd_btn_resetMapping);
		btn_resetMapping.setFont(Utility.getFont());
		btn_resetMapping.setText("Reset");

	    FormLayout forml_mapping = new FormLayout();
	    forml_mapping.marginHeight = 10;
	    forml_mapping.marginWidth = 10;

		FormData fd_grp_customizeMapping = new FormData();
		fd_grp_customizeMapping.left = new FormAttachment(0);
		fd_grp_customizeMapping.right = new FormAttachment(100);
		fd_grp_customizeMapping.top = new FormAttachment(txt_bracelet, 5, SWT.BOTTOM);
		fd_grp_customizeMapping.bottom = new FormAttachment(100);
		grp_customizeMapping = new Group(this, SWT.NONE);
		grp_customizeMapping.setLayoutData(fd_grp_customizeMapping);
		grp_customizeMapping.setLayout(forml_mapping);
		
		FormData fd_tbl_line = new FormData();
		fd_tbl_line.left = new FormAttachment(0);
		fd_tbl_line.top = new FormAttachment(0, -5);
		fd_tbl_line.bottom = new FormAttachment(100);
		fd_tbl_line.width = 200;
		tblv_line = new TableViewer(grp_customizeMapping, SWT.BORDER | SWT.FULL_SELECTION);
		tbl_line = tblv_line.getTable();
		tbl_line.setLayoutData(fd_tbl_line);
		tbl_line.setLinesVisible(true);
		tbl_line.setHeaderVisible(true);
		tbl_line.setFont(Utility.getFont());
		TableColumn tblc_index = new TableColumn(tbl_line, SWT.CENTER);
		tblc_index.setText("Idx");
		tblc_index.setWidth(35);
		TableColumn tblc_data = new TableColumn(tbl_line, SWT.LEFT);
		tblc_data.setText("Data");
		tblc_data.setWidth(161);
		ds_tableItem = new DragSource(tbl_line, DND.DROP_MOVE | DND.DROP_COPY);
		ds_tableItem.setTransfer(Constant.TEXT_TRANSFER_TYPE);
		
		FormData fd_lb_arrow = new FormData();
		fd_lb_arrow.top = new FormAttachment(0, 50);
		fd_lb_arrow.left = new FormAttachment(tbl_line, 20, SWT.RIGHT);
		fd_lb_arrow.width = 20;
		Label lb_arrow = new Label(grp_customizeMapping, SWT.NONE);
		lb_arrow.setLayoutData(fd_lb_arrow);
		lb_arrow.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		lb_arrow.setFont(Utility.getFont(Constant.DEFAULT_FONT_SIZE+5, SWT.NONE));
		lb_arrow.setText("\u25B6");

		FormData fd_lb_time = new FormData();
		fd_lb_time.top = new FormAttachment(0, 25);
		fd_lb_time.left = new FormAttachment(lb_arrow, 10, SWT.RIGHT);
		fd_lb_time.width = 50;
		Label lb_time = new Label(grp_customizeMapping, SWT.NONE);
		lb_time.setLayoutData(fd_lb_time);
		lb_time.setFont(Utility.getFont());
		lb_time.setText("TIME");
		lb_time.setAlignment(SWT.RIGHT);
		
		FormData fd_txt_time = new FormData();
		fd_txt_time.top = new FormAttachment(lb_time, -2, SWT.TOP);
		fd_txt_time.left = new FormAttachment(lb_time, 10, SWT.RIGHT);
		fd_txt_time.width = 170;
		txt_time = new Text(grp_customizeMapping, SWT.BORDER);
		txt_time.setLayoutData(fd_txt_time);
		txt_time.setEditable(false);
		txt_time.setFont(Utility.getFont());
		txt_time.setBounds(406, 52, 167, 21);
		dt_textTime = new DropTarget(txt_time, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textTime.setTransfer(Constant.TEXT_TRANSFER_TYPE);
		
		FormData fd_spn_offset = new FormData();
		fd_spn_offset.top = new FormAttachment(txt_time, 0, SWT.TOP);
		fd_spn_offset.left = new FormAttachment(txt_time, 5, SWT.RIGHT);
		fd_spn_offset.width = 55;
		spn_offset = new Spinner(grp_customizeMapping, SWT.BORDER);
		spn_offset.setLayoutData(fd_spn_offset);
		spn_offset.setEnabled(false);
		spn_offset.setTextLimit(5);
		spn_offset.setDigits(1);
		spn_offset.setMinimum(-240);
		spn_offset.setMaximum(240);
		spn_offset.setIncrement(5);
		spn_offset.setSelection(0);
		spn_offset.setFont(Utility.getFont());

		FormData fd_lb_hour = new FormData();
		fd_lb_hour.top = new FormAttachment(lb_time, 0, SWT.TOP);
		fd_lb_hour.left = new FormAttachment(spn_offset, 5, SWT.RIGHT);
		fd_lb_hour.width = 30;
		Label lb_hour = new Label(grp_customizeMapping, SWT.NONE);
		lb_hour.setLayoutData(fd_lb_hour);
		lb_hour.setToolTipText("offset");
		lb_hour.setFont(Utility.getFont());
		lb_hour.setText("Hour");

		FormData fd_cb_timeFormat = new FormData();
		fd_cb_timeFormat.top = new FormAttachment(txt_time, 5, SWT.BOTTOM);
		fd_cb_timeFormat.left = new FormAttachment(txt_time, 0, SWT.LEFT);
		fd_cb_timeFormat.right = new FormAttachment(txt_time, 0, SWT.RIGHT);
		cb_timeFormat = new CCombo(grp_customizeMapping, SWT.BORDER);
		cb_timeFormat.setLayoutData(fd_cb_timeFormat);
		cb_timeFormat.setEnabled(false);
		cb_timeFormat.setItems(Constant.TIME_FORMATS);
		cb_timeFormat.add(Constant.UNIX_TIME_STR);
		cb_timeFormat.setFont(Utility.getFont());
		cb_timeFormat.setText(Constant.TIME_FORMATS[0]);

		FormData fd_lb_resultTimeChk = new FormData();
		fd_lb_resultTimeChk.top = new FormAttachment(cb_timeFormat, 2, SWT.TOP);
		fd_lb_resultTimeChk.left = new FormAttachment(cb_timeFormat, 5, SWT.RIGHT);
		fd_lb_resultTimeChk.right = new FormAttachment(lb_hour, 0, SWT.RIGHT);
		lb_resultTimeChk = new Label(grp_customizeMapping, SWT.NONE);
		lb_resultTimeChk.setLayoutData(fd_lb_resultTimeChk);
		lb_resultTimeChk.setFont(Utility.getFont());
		lb_resultTimeChk.setText("Not checked");
		lb_resultTimeChk.setBounds(579, 82, 96, 15);

		FormData fd_lb_cpu = new FormData();
		fd_lb_cpu.top = new FormAttachment(cb_timeFormat, 20);
		fd_lb_cpu.left = new FormAttachment(lb_time, 0, SWT.LEFT);
		fd_lb_cpu.right = new FormAttachment(lb_time, 0, SWT.RIGHT);
		Label lb_cpu = new Label(grp_customizeMapping, SWT.NONE);
		lb_cpu.setLayoutData(fd_lb_cpu);
		lb_cpu.setFont(Utility.getFont());
		lb_cpu.setText("COUNT");
		lb_cpu.setAlignment(SWT.RIGHT);
		
		FormData fd_txt_count = new FormData();
		fd_txt_count.top = new FormAttachment(lb_cpu, -2, SWT.TOP);
		fd_txt_count.left = new FormAttachment(lb_cpu, 10, SWT.RIGHT);
		fd_txt_count.width = 170;
		txt_count = new Text(grp_customizeMapping, SWT.BORDER);
		txt_count.setLayoutData(fd_txt_count);
		txt_count.setEditable(false);
		txt_count.setFont(Utility.getFont());
		dt_textCount = new DropTarget(txt_count, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textCount.setTransfer(Constant.TEXT_TRANSFER_TYPE);		
		
		FormData fd_grp_type = new FormData();
		fd_grp_type.left = new FormAttachment(txt_count, 0, SWT.LEFT);
		fd_grp_type.top = new FormAttachment(txt_count, 0);
		Group grp_type = new Group(grp_customizeMapping, SWT.NONE);
		grp_type.setLayoutData(fd_grp_type);
		GridLayout gl_type = new GridLayout();
		gl_type.marginWidth = 10;
		gl_type.verticalSpacing = 2;
		gl_type.marginHeight = 0;
		gl_type.marginBottom = 5;
		gl_type.numColumns = 3;
		gl_type.horizontalSpacing = 10;
		grp_type.setLayout(gl_type);

		Label lb_unit1 = new Label(grp_type, SWT.NONE);
		lb_unit1.setFont(Utility.getFont());
		lb_unit1.setText("Transactions per");
		
		spn_unit = new Spinner(grp_type, SWT.BORDER);
		spn_unit.setTextLimit(4);
		spn_unit.setMaximum(1440);
		spn_unit.setMinimum(1);
		spn_unit.setFont(Utility.getFont());

		cb_unit = new CCombo(grp_type, SWT.BORDER | SWT.READ_ONLY);
		cb_unit.setVisibleItemCount(2);
		cb_unit.setItems(TIME_UNITS);
		cb_unit.setFont(Utility.getFont());
		cb_unit.setText(TIME_UNITS[0]);

		grp_customizeMapping.setTabList(new Control[] { txt_time, spn_offset, cb_timeFormat, txt_count, grp_type });

		setTabList(new Control[] { cb_fileType, txt_delimeter, txt_bracelet, btn_sampling, btn_resetMapping, grp_customizeMapping });
		
	}

	protected void addEventListener() {

		cb_fileType.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				resetMappings();
				spn_offset.setEnabled(false);
				cb_timeFormat.setEnabled(false);
			}
		});

		txt_delimeter.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				resetMappings();
			}
		});

		txt_bracelet.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				resetMappings();
			}
		});

		txt_bracelet.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				String[] arr = StringUtil.getArrayFromString(txt_bracelet.getText(), " ");
				for(String s : arr) {
					if(s.length() != 2) {
						MessageUtil.showErrorMessage(getShell(), "A bracelet must consist of 2-character.\nCheck the bracelet " + s);
						txt_bracelet.setFocus();
						break;
					}
				}
			}

			public void focusGained(FocusEvent e) {
			}
		});

		btn_sampling.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetMappings();
				addLine();
			}
		});

		btn_resetMapping.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				reset();
			}
		});

		txt_time.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String timeStr = txt_time.getText();
				spn_offset.setEnabled(timeStr.length() > 0);
				cb_timeFormat.setEnabled(timeStr.length() > 0);
				if(timeStr.length() > 0) {
					Logger.debug("Check TimeFormat : " + timeStr + ", " + cb_timeFormat.getText());
					checkTimeString(new String[] { cb_timeFormat.getText() }, timeStr);
					if(!lb_resultTimeChk.getText().equals("OK")) {
						checkTimeString(cb_timeFormat.getItems(), timeStr);
					}
				} else {
					lb_resultTimeChk.setText("Not checked");
					lb_resultTimeChk.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				}
			}
		});

		cb_timeFormat.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String timeStr = txt_time.getText();
				if(timeStr.length() > 0) {
					Logger.debug("Check TimeFormat : " + timeStr + ", " + cb_timeFormat.getText());
					checkTimeString(new String[] { cb_timeFormat.getText() }, timeStr);
				}
			}
		});

		addDragAndDropListeners();		
		addKeyListeners();

	}

	protected void addDragAndDropListeners() {

		ds_tableItem.addDragListener(new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent e) {
				draggingItem = ((Table)((DragSource)e.widget).getControl()).getSelection()[0];
				e.data = draggingItem.getText(1);
			}
		});

		dt_textTime.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("TIME", txt_time);
				}
			}
		});

		dt_textCount.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("COUNT", txt_count);
				}
			}
		});

	}
	
	protected void addKeyListeners() {

		txt_time.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("TIME", txt_time);
				}
			}
		});
		
		txt_count.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("COUNT", txt_count);
				}
			}
		});

	}

	protected boolean mappingDataByDND(String key, Text txtCtrl) {
		if(key == null)
			return false;
		String s = draggingItem.getText(1);
		String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
		String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
		StringTokenizer st = new StringTokenizer(s, delimeter);
		String mapStr;
		String fldStr;
		if(st.countTokens() > 1) {
			List<String> subField = new ArrayList<String>();
			subField.add(s);
			while(st.hasMoreTokens()) {
				subField.add(st.nextToken());
			}
			int sub_fld = getSubFieldIndex(subField);
			if(sub_fld == 0) {
				mapStr = draggingItem.getText(0);
				fldStr = draggingItem.getText(1);
			} else if(sub_fld > 0) {
				List<String> tokens = ParserUtil.getTokenList(s, delimeter, bracelets);
				mapStr = draggingItem.getText(0) + "-" + sub_fld;
				fldStr = tokens.get(sub_fld - 1);
			} else {
				return false;
			}
		} else {
			mapStr = draggingItem.getText(0);
			fldStr = draggingItem.getText(1);
		}
		boolean appendFlag = false;
		if(key.equals("TIME") && !txtCtrl.getText().equals("")) {
			String[] labels = new String[] { "Replace", "Append" };
			int i = MessageUtil.showSelectMessage(getShell(), "Question", "What would you do with this field?", labels);
			if(i == 1) {
				appendFlag = true;
			} else if(i < 0) {
				return false;
			}
		}
		if(appendFlag) {
			mapStr = (String)mappingData.get(key) + "," + mapStr;
			fldStr = txtCtrl.getText() + " " + fldStr;
		}
		Logger.debug("mapping:" + mapStr + ", field:" + fldStr);
		mappingData.put(key, mapStr);
		txtCtrl.setText(fldStr);
		draggingItem = null;
		owner.toggleAnalyzingButton(checkParsingAvailable());
		return true;
	}
	
	protected void removeMappingData(String key, Text txtCtrl) {
		if(!txtCtrl.getText().equals("")) {
			Logger.debug("remove mapping:" + mappingData.remove(key) + ", field:" + txtCtrl.getText());
			txtCtrl.setText("");
			draggingItem = null;
			owner.toggleAnalyzingButton(checkParsingAvailable());
		}
	}

	private int getSubFieldIndex(List<String> fields) {
		if(fields.size() > 1) {
			String[] labels = new String[fields.size()];
			String fld;
			for(int i = 0; i < fields.size(); i++) {
				fld = (String)fields.get(i);
				if(fld.length() > 20) {
					labels[i] = fld.substring(0, 20) + " ...";
				} else {
					labels[i] = fld;
				}
			}
			int i = MessageUtil.showSelectMessage(getShell(), "Question", "Select a field.", labels);
			if(i < 0) {
				return -1;
			} else {
				return i;
			}
		} else {
			return 0;
		}
	}

	protected boolean mappingField(String key, String value) {
		if(key == null)
			return false;
		if(value == null || value.equals(""))
			return false;
		Text txtCtrl = null;
		if(key.equals("TIME"))
			txtCtrl = txt_time;
		else if(key.equals("COUNT"))
			txtCtrl = txt_count;
		String val = null;
		String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
		String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
		if(value.indexOf(',') > 0) {
			StringTokenizer st = new StringTokenizer(value, ",");
			String concatValue = "";
			if(key.equals("TIME")) {
				String joinChar = " ";
				int cnt = 1;
				while(st.hasMoreTokens()) {
					String idx_str = st.nextToken();
					FieldIndex fld_idx = new FieldIndex(key + "_" + cnt, idx_str);
					String main_fld = tbl_line.getItem(fld_idx.getMainIndex()).getText(1);
					val = fld_idx.getField(main_fld, delimeter, bracelets);
					if(cnt == 1) {
						if(val == null)
							return false;
						concatValue = val;
					} else {
						concatValue += (val == null) ? "" : (joinChar + val);
					}
					cnt++;
				}
				concatValue = concatValue.trim();
				txtCtrl.setText(concatValue);
			}
		} else {
			FieldIndex fld_idx = new FieldIndex(key, value);
			String main_fld = tbl_line.getItem(fld_idx.getMainIndex()).getText(1);
			val = fld_idx.getField(main_fld, delimeter, bracelets);
			if(val == null) {
				return false;
			}
			txtCtrl.setText(val);
		}
		owner.toggleAnalyzingButton(checkParsingAvailable());
		return true;
	}

	protected void addLine() {
		String file_line = "";
		try {
			file_line = getSampleLine();
		} catch(Exception e) {
			Logger.debug("Failed to sample a line from logfile.");
			Logger.error(e);
		}
		if(file_line == null) {
			MessageUtil.showErrorMessage(getShell(), "Failed to sample a line.");
			return;
		}

		String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
		String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
		List<String> tokenList = ParserUtil.getTokenList(file_line, delimeter, bracelets, AlybaGUI.getInstance().optionSetting.checkStrictCheck());
		if(tokenList == null) {
			return;
		}
		fieldCount = tokenList.size();
		for(int i = 0; i < tokenList.size(); i++) {
			TableItem item = new TableItem(tbl_line, SWT.NULL);
			item.setText(0, String.valueOf(i + 1));
			item.setText(1, tokenList.get(i));
			Logger.debug("TOKEN_" + (i + 1) + " : " + tokenList.get(i));
		}
	}

	protected void addMappingData(String key, String itemIdx) {
		if(mappingData.containsKey(key)) {
			String s = (String)mappingData.get(key);
			mappingData.put(key, s + "," + itemIdx);
		} else {
			mappingData.put(key, itemIdx);
		}
	}

	protected void sortTableDataByIndex() {
		TableItem[] items = tbl_line.getItems();
		int value1;
		int value2;
		for(int i = 1; i < items.length; i++) {
			value1 = Integer.parseInt(items[i].getText(0));
			for(int j = 0; j < i; j++) {
				value2 = Integer.parseInt(items[j].getText(0));
				if(value1 < value2) {
					String[] values = { items[i].getText(0), items[i].getText(1) };
					items[i].dispose();
					TableItem item = new TableItem(tbl_line, SWT.NULL, j);
					item.setText(values);
					items = tbl_line.getItems();
					break;
				}
			}
		}
	}

	protected void checkTimeString(String[] formats, String timeStr) {
		Object[] matchFormat = ParserUtil.getMatchedTimeFormat(formats, timeStr, Constant.TIME_LOCALES);
		if(matchFormat == null) {
			lb_resultTimeChk.setText("Not OK");
			lb_resultTimeChk.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		} else {
			lb_resultTimeChk.setText("OK");
			lb_resultTimeChk.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
			if(formats.length > 1) {
				cb_timeFormat.setText((String)matchFormat[1]);
			}
			timeLocale = (Locale)matchFormat[0];
		}
		owner.toggleAnalyzingButton(checkParsingAvailable());
	}

	protected String getSampleLine() throws Exception {
		TableItem[] items = owner.tbl_files.getItems();
		File file = null;
		int idx = -1;
		int cnt = 0;
		do {
			if(cnt == Constant.MAX_SAMPLING_COUNT)
				break;
			idx = NumberUtil.getRandomNumber(items.length);
			file = (File)items[idx].getData("file");
			cnt++;
		} while(file.length() == 0);
		
		if(cnt == Constant.MAX_SAMPLING_COUNT) {
			Logger.debug("Failed to sample a file.");
			return null;
		} else {
			int line_number = 0;
			String line = null;
			cnt = 0;
			int line_number_from = 1;
			do {
				line = FileUtil.readFileLine(file, line_number_from++, null);
				if(!(line.trim().equals("") || line.startsWith("#"))) {
					break;
				}
			} while(line != null);
			if(line == null) {
				cnt = Constant.MAX_SAMPLING_COUNT;
			} else {
				line_number_from--;
				int line_number_to = line_number_from + 200;
				do {
					if(cnt == Constant.MAX_SAMPLING_COUNT || line_number_from == line_number_to)
						break;
					if(line == null) {
						int temp = (int)(line_number_to / 2);
						line_number_to = (temp < line_number_from) ? line_number_from : temp;
					}
					line_number = NumberUtil.getRandomNumber(line_number_from, line_number_to);
					line = FileUtil.readFileLine(file, line_number, null);
					cnt++;
					Logger.debug("sample a line(" + line_number + ") : " + line);
				} while(line == null || line.trim().equals("") || line.startsWith("#"));
			}
			if(cnt == Constant.MAX_SAMPLING_COUNT) {
				Logger.debug("Failed to sample a line : " + file.getCanonicalPath());
				return null;
			} else {
				Logger.debug("Sampled Line(" + file.getCanonicalPath() + ":" + line_number + ") : \n" + line);
				return line;
			}
		}
	}

	public int checkMappingValidation() {
		TableItem[] items = owner.tbl_files.getItems();
		for(int i = 0; i < items.length; i++) {
			File file = (File)items[i].getData("file");
			BufferedReader br = null;
			String line = null;
			try {
				String time_idx = (String)mappingData.get("TIME");
				String[] time_idx_arr = time_idx.split(",");
				String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
				String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				int count = 0;
				while((line = br.readLine()) != null) {
					count++;
					if(count < 3 || line == null || line.trim().equals("") || line.startsWith("#")) {
						continue;
					}
					Logger.debug("check a line : " + line);
					List<String> main_tokens = ParserUtil.getTokenList(line, delimeter, bracelets, AlybaGUI.getInstance().optionSetting.checkStrictCheck());
					try {
						String time_str = "";
						for(String temp_idx : time_idx_arr) {
							FieldIndex fld_idx = new FieldIndex("", temp_idx);
							String temp_fld = fld_idx.getField(main_tokens, delimeter, bracelets);
							if(temp_fld == null) {
								time_str = "";
								break;
							} else {
								time_str += " " + temp_fld;
							}
						}
						time_str = time_str.trim();
						Logger.debug("time field : " + time_str);
						if(cb_timeFormat.getText().equals(Constant.UNIX_TIME_STR)) {
							if(ParserUtil.isMatchedUnixTimeFormat(time_str) == false) {
								Logger.debug("INVALID TIME TOKEN(" + time_idx + ") : " + time_str);
								return i;
							}
						} else {
							if(ParserUtil.isMatchedTimeFormat(cb_timeFormat.getText(), time_str, timeLocale) == false) {
								Logger.debug("INVALID TIME TOKEN(" + time_idx + ") : " + time_str);
								return i;
							}
						}
					} catch(Exception e) {
						Logger.debug("Failed to validate field data for mapping.");
						Logger.error(e);
						return i;
					}
					break;
				}
			} catch(Exception e) {
				Logger.debug("Failed to check mapping validation.");
				Logger.error(e);
			} finally {
				if(br != null) {
					try {
						br.close();
					} catch(Exception e2) {
					}
				}
			}
		}
		return -1;
	}

	protected void resetMappings() {
		mappingData = new HashMap<String, String>();
		fieldCount = 0;
		timeLocale = null;
		tbl_line.removeAll();
		txt_time.setText("");
		spn_offset.setSelection(0);
		cb_timeFormat.setText(Constant.TIME_FORMATS[0]);
		txt_count.setText("");
		owner.toggleAnalyzingButton(checkParsingAvailable());
	}

	protected void setTextValue(Text txtCtrl, String value) {
		if(value != null) {
			txtCtrl.setText(value);
		}
	}

	protected void setComboValue(CCombo cbCtrl, String value) {
		if(value != null && !value.equals("")) {
			cbCtrl.setText(value);
		}
	}

	protected void setSpinnerValue(Spinner spnCtrl, int value) {
		spnCtrl.setSelection(value);
	}

	protected boolean checkParsingAvailable() {
		if(!mappingData.containsKey("TIME") || !lb_resultTimeChk.getText().equals("OK") || mappingData.keySet().size() < 2) {
			return false;
		} else {
			spn_offset.setEnabled(true);
			return true;
		}
	}

	public void reset() {
		cb_fileType.setText(FILE_TYPES[0]);
		txt_delimeter.setText(StringUtil.replaceMetaCharacter(Constant.FILE_DEFAULT_DELIMETER, true));
		txt_bracelet.setText(StringUtil.getStringFromArray(Constant.FILE_DEFAULT_BRACELETS, " "));
		resetMappings();
	}

}
