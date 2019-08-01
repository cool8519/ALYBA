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
import dal.tool.analyzer.alyba.parser.FieldIndex;
import dal.tool.analyzer.alyba.parser.ParserUtil;
import dal.tool.analyzer.alyba.setting.DefaultMapping;
import dal.tool.analyzer.alyba.setting.FieldMappingInfo;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.FileUtil;
import dal.util.NumberUtil;
import dal.util.StringUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class FieldMapping extends Composite {

	private FilterSetting filterSetting;
	private TableItem draggingItem;
	private List<String> fileEncoding;
	private HashMap<String, String> mappingData;
	private int fieldCount;
	private Locale timeLocale;

	private Group grp_customizeMapping;
	public CCombo cb_logType;
	public CCombo cb_elapsedUnit;
	public CCombo cb_timeFormat;
	public Spinner spn_offset;
	private Table tbl_line;
	private TableViewer tblv_line;
	public Text txt_delimeter;
	public Text txt_bracelet;
	private Text txt_uri;
	private Text txt_time;
	private Text txt_ip;
	private Text txt_method;
	private Text txt_version;
	private Text txt_code;
	private Text txt_bytes;
	private Text txt_elapsed;
	private Button btn_sampling;
	private Button btn_resetMapping;
	private Label lb_resultTimeChk;
	private DragSource ds_tableItem;
	private DropTarget dt_textUri;
	private DropTarget dt_textTime;
	private DropTarget dt_textIp;
	private DropTarget dt_textMethod;
	private DropTarget dt_textVersion;
	private DropTarget dt_textCode;
	private DropTarget dt_textBytes;
	private DropTarget dt_textElapsed;

	public FieldMapping(Composite parent, int style) {
		super(parent, style);
		createContents();
		addEventListener();
		fileEncoding = new ArrayList<String>();
		mappingData = new HashMap<String, String>();
	}
	
	public void setFilterSetting(FilterSetting filterSetting) {
		this.filterSetting = filterSetting;
	}
	
	public void setMappingData(HashMap<String, String> mappingData) {
		this.mappingData = mappingData;
	}
	
	public void setTimeLocale(Locale timeLocale) {
		this.timeLocale = timeLocale;
	}

	public String getLogType() {
		return cb_logType.getText();
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

	public String getElapsedUnit() {
		return cb_elapsedUnit.getText();
	}

	public List<String> getFileEncoding() {
		return fileEncoding;
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

		Label lb_logType = new Label(this, SWT.NONE);
		lb_logType.setText("Log Type");
		lb_logType.setAlignment(SWT.RIGHT);
		lb_logType.setBounds(10, 14, 55, 15);

		cb_logType = new CCombo(this, SWT.BORDER | SWT.READ_ONLY);
		cb_logType.setVisibleItemCount(6);
		cb_logType.setItems(Constant.LOG_TYPES);
		cb_logType.setText(Constant.LOG_TYPES[0]);
		cb_logType.setBounds(71, 11, 170, 21);

		Label lb_delimeter = new Label(this, SWT.NONE);
		lb_delimeter.setText("Delimeter");
		lb_delimeter.setAlignment(SWT.RIGHT);
		lb_delimeter.setBounds(276, 14, 55, 15);

		txt_delimeter = new Text(this, SWT.BORDER);
		txt_delimeter.setText(StringUtil.replaceMetaCharacter(Constant.LOG_DEFAULT_DELIMETER, true));
		txt_delimeter.setBounds(337, 11, 110, 21);

		btn_sampling = new Button(this, SWT.NONE);
		btn_sampling.setBounds(453, 10, 100, 23);
		btn_sampling.setText("Sampling");

		Label lb_bracelet = new Label(this, SWT.NONE);
		lb_bracelet.setText("Bracelet");
		lb_bracelet.setAlignment(SWT.RIGHT);
		lb_bracelet.setBounds(276, 38, 55, 15);

		txt_bracelet = new Text(this, SWT.BORDER);
		txt_bracelet.setText(StringUtil.getStringFromArray(Constant.LOG_DEFAULT_BRACELETS, " "));
		txt_bracelet.setBounds(337, 35, 110, 21);

		btn_resetMapping = new Button(this, SWT.NONE);
		btn_resetMapping.setText("Reset");
		btn_resetMapping.setBounds(596, 10, 100, 23);

		grp_customizeMapping = new Group(this, SWT.NONE);
		grp_customizeMapping.setBounds(10, 59, 686, 272);

		tblv_line = new TableViewer(grp_customizeMapping, SWT.BORDER | SWT.FULL_SELECTION);
		tbl_line = tblv_line.getTable();
		tbl_line.setLinesVisible(true);
		tbl_line.setHeaderVisible(true);
		tbl_line.setBounds(10, 17, 220, 245);
		TableColumn tblc_index = new TableColumn(tbl_line, SWT.CENTER);
		tblc_index.setText("Idx");
		tblc_index.setWidth(35);
		TableColumn tblc_data = new TableColumn(tbl_line, SWT.LEFT);
		tblc_data.setText("Data");
		tblc_data.setWidth(181);
		ds_tableItem = new DragSource(tbl_line, DND.DROP_MOVE | DND.DROP_COPY);
		ds_tableItem.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		Label lb_arrow = new Label(grp_customizeMapping, SWT.NONE);
		lb_arrow.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		lb_arrow.setFont(Utility.getFont(14, SWT.NONE));
		lb_arrow.setText("\u25B6");
		lb_arrow.setBounds(248, 120, 20, 27);

		Label lb_uri = new Label(grp_customizeMapping, SWT.NONE);
		lb_uri.setText("Request URI");
		lb_uri.setAlignment(SWT.RIGHT);
		lb_uri.setBounds(287, 28, 110, 15);

		txt_uri = new Text(grp_customizeMapping, SWT.BORDER);
		txt_uri.setEditable(false);
		txt_uri.setBounds(406, 25, 270, 21);
		dt_textUri = new DropTarget(txt_uri, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textUri.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		Label lb_time = new Label(grp_customizeMapping, SWT.NONE);
		lb_time.setText("Request Time");
		lb_time.setAlignment(SWT.RIGHT);
		lb_time.setBounds(287, 55, 110, 15);

		txt_time = new Text(grp_customizeMapping, SWT.BORDER);
		txt_time.setEditable(false);
		txt_time.setBounds(406, 52, 167, 21);
		dt_textTime = new DropTarget(txt_time, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textTime.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		spn_offset = new Spinner(grp_customizeMapping, SWT.BORDER);
		spn_offset.setEnabled(false);
		spn_offset.setTextLimit(5);
		spn_offset.setDigits(1);
		spn_offset.setMinimum(-240);
		spn_offset.setMaximum(240);
		spn_offset.setIncrement(5);
		spn_offset.setSelection(0);
		spn_offset.setBounds(579, 52, 55, 21);

		Label lb_hour = new Label(grp_customizeMapping, SWT.NONE);
		lb_hour.setToolTipText("offset");
		lb_hour.setText("Hour");
		lb_hour.setBounds(640, 55, 30, 15);

		cb_timeFormat = new CCombo(grp_customizeMapping, SWT.BORDER);
		cb_timeFormat.setEnabled(false);
		cb_timeFormat.setItems(Constant.TIME_FORMATS);
		cb_timeFormat.add(Constant.UNIX_TIME_STR);
		cb_timeFormat.setText(Constant.TIME_FORMATS[0]);
		cb_timeFormat.setBounds(406, 79, 167, 21);

		lb_resultTimeChk = new Label(grp_customizeMapping, SWT.NONE);
		lb_resultTimeChk.setText("Not checked");
		lb_resultTimeChk.setBounds(579, 82, 96, 15);

		Label lb_ip = new Label(grp_customizeMapping, SWT.NONE);
		lb_ip.setText("Request IP");
		lb_ip.setAlignment(SWT.RIGHT);
		lb_ip.setBounds(287, 109, 110, 15);

		txt_ip = new Text(grp_customizeMapping, SWT.BORDER);
		txt_ip.setEditable(false);
		txt_ip.setBounds(406, 106, 167, 21);
		dt_textIp = new DropTarget(txt_ip, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textIp.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		Label lb_method = new Label(grp_customizeMapping, SWT.NONE);
		lb_method.setText("Request Method");
		lb_method.setAlignment(SWT.RIGHT);
		lb_method.setBounds(287, 136, 110, 15);

		txt_method = new Text(grp_customizeMapping, SWT.BORDER);
		txt_method.setEditable(false);
		txt_method.setBounds(406, 133, 167, 21);
		dt_textMethod = new DropTarget(txt_method, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textMethod.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		Label lb_version = new Label(grp_customizeMapping, SWT.NONE);
		lb_version.setText("Request Version");
		lb_version.setAlignment(SWT.RIGHT);
		lb_version.setBounds(287, 163, 110, 15);

		txt_version = new Text(grp_customizeMapping, SWT.BORDER);
		txt_version.setEditable(false);
		txt_version.setBounds(406, 160, 167, 21);
		dt_textVersion = new DropTarget(txt_version, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textVersion.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		Label lb_code = new Label(grp_customizeMapping, SWT.NONE);
		lb_code.setText("Response Code");
		lb_code.setAlignment(SWT.RIGHT);
		lb_code.setBounds(287, 190, 110, 15);

		txt_code = new Text(grp_customizeMapping, SWT.BORDER);
		txt_code.setEditable(false);
		txt_code.setBounds(406, 187, 167, 21);
		dt_textCode = new DropTarget(txt_code, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textCode.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		Label lb_bytes = new Label(grp_customizeMapping, SWT.NONE);
		lb_bytes.setText("Response Bytes");
		lb_bytes.setAlignment(SWT.RIGHT);
		lb_bytes.setBounds(287, 217, 110, 15);

		txt_bytes = new Text(grp_customizeMapping, SWT.BORDER);
		txt_bytes.setEditable(false);
		txt_bytes.setBounds(406, 214, 167, 21);
		dt_textBytes = new DropTarget(txt_bytes, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textBytes.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		Label lb_elapsed = new Label(grp_customizeMapping, SWT.NONE);
		lb_elapsed.setText("Elapsed Time");
		lb_elapsed.setAlignment(SWT.RIGHT);
		lb_elapsed.setBounds(247, 244, 150, 15);

		txt_elapsed = new Text(grp_customizeMapping, SWT.BORDER);
		txt_elapsed.setEditable(false);
		txt_elapsed.setBounds(406, 241, 167, 21);
		dt_textElapsed = new DropTarget(txt_elapsed, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textElapsed.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		cb_elapsedUnit = new CCombo(grp_customizeMapping, SWT.BORDER | SWT.READ_ONLY);
		cb_elapsedUnit.setEnabled(false);
		cb_elapsedUnit.setItems(Constant.ELAPSED_TIME_UNITS);
		cb_elapsedUnit.setText(Constant.ELAPSED_TIME_UNITS[0]);
		cb_elapsedUnit.setBounds(579, 241, 97, 21);

		grp_customizeMapping.setTabList(new Control[] { txt_uri, txt_time, spn_offset, cb_timeFormat, txt_ip, txt_method, txt_version, txt_code, txt_bytes, txt_elapsed, cb_elapsedUnit });

		setTabList(new Control[] { cb_logType, txt_delimeter, btn_sampling, btn_resetMapping });

	}

	protected void addEventListener() {

		cb_logType.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				resetMappings();
				boolean isCustomize = cb_logType.getText().equals(Constant.LOG_TYPES[0]);
				grp_customizeMapping.setEnabled(isCustomize);
				FieldMappingInfo info = getDefaultMappingInfo(cb_logType.getText());
				if(info != null) {
					txt_delimeter.setText(StringUtil.replaceMetaCharacter(info.fieldDelimeter, true));
					txt_bracelet.setText(info.fieldBracelet);
				}
				txt_delimeter.setEnabled(isCustomize);
				txt_bracelet.setEnabled(isCustomize);
				resetMappings();
				autoMapping();
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
				boolean isCustomize = cb_logType.getText().equals(Constant.LOG_TYPES[0]);
				if(!isCustomize) {
					autoMapping();
				} else {
					addLine();
				}
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
					checkTimeString(cb_timeFormat.getItems(), timeStr);
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
					debug("Check TimeFormat : " + timeStr + ", " + cb_timeFormat.getText());
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

		dt_textUri.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					if(mappingDataByDND("URI", txt_uri)) {
						filterSetting.setURIEnabled(true);
					}
				}
			}
		});

		dt_textTime.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("TIME", txt_time);
				}
			}
		});

		dt_textIp.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					if(mappingDataByDND("IP", txt_ip)) {
						filterSetting.setIPEnabled(true);
					}
				}
			}
		});

		dt_textMethod.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					if(mappingDataByDND("METHOD", txt_method)) {
						filterSetting.setMethodEnabled(true);
					}
				}
			}
		});

		dt_textVersion.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					if(mappingDataByDND("VERSION", txt_version)) {
						filterSetting.setVersionEnabled(true);
					}
				}
			}
		});

		dt_textCode.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					if(mappingDataByDND("CODE", txt_code)) {
						filterSetting.setCodeEnabled(true);
					}
				}
			}
		});

		dt_textBytes.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("BYTES", txt_bytes);
				}
			}
		});

		dt_textElapsed.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					if(mappingDataByDND("ELAPSED", txt_elapsed)) {
						checkElapsedString();
					}
				}
			}
		});

	}
	
	protected void addKeyListeners() {

		txt_uri.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("URI", txt_uri);
					filterSetting.setURIEnabled(false);
				}
			}
		});

		txt_time.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("TIME", txt_time);
				}
			}
		});

		txt_ip.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("IP", txt_ip);
					filterSetting.setIPEnabled(false);
				}
			}
		});

		txt_method.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("METHOD", txt_method);
					filterSetting.setMethodEnabled(false);
				}
			}
		});

		txt_version.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("VERSION", txt_version);
					filterSetting.setVersionEnabled(false);
				}
			}
		});

		txt_code.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("CODE", txt_code);
					filterSetting.setCodeEnabled(false);
				}
			}
		});

		txt_bytes.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("BYTES", txt_bytes);
				}
			}
		});

		txt_elapsed.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("ELAPSED", txt_elapsed);
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
		if(key.equals("URI") && !txtCtrl.getText().equals("")) {
			String[] labels = new String[] { "URI", "Parameters" };
			int i = MessageUtil.showSelectMessage(getShell(), "Question", "What is this field?", labels);
			if(i == 1) {
				appendFlag = true;
			} else if(i < 0) {
				return false;
			}
		} else if(key.equals("TIME") && !txtCtrl.getText().equals("")) {
			String[] labels = new String[] { "Replace", "Append" };
			int i = MessageUtil.showSelectMessage(getShell(), "Question", "What would you do with this field?", labels);
			if(i == 1) {
				appendFlag = true;
			} else if(i < 0) {
				return false;
			}
		}
		if(appendFlag) {
			String joinChar = key.equals("URI") ? "?" : " ";
			String[] idx_arr = mappingData.get(key).split(",");
			if(idx_arr.length == 1) {
				mapStr = (String)mappingData.get(key) + "," + mapStr;
				fldStr = txtCtrl.getText() + joinChar + fldStr;
			} else {
				String orgIdx = (String)mappingData.get(key);
				String orgFld = txtCtrl.getText();
				mapStr = orgIdx.substring(0, orgIdx.indexOf(",") + 1) + mapStr;
				fldStr = orgFld.substring(0, orgFld.indexOf(joinChar) + 1) + fldStr;
			}
		}
		debug("mapping:" + mapStr + ", field:" + fldStr);
		mappingData.put(key, mapStr);
		txtCtrl.setText(fldStr);
		draggingItem = null;
		AlybaGUI.getInstance().toggleAnalyzingButton(checkParsingAvailable());
		return true;
	}
	
	protected void removeMappingData(String key, Text txtCtrl) {
		if(!txtCtrl.getText().equals("")) {
			debug("remove mapping:" + mappingData.remove(key) + ", field:" + txtCtrl.getText());
			txtCtrl.setText("");
			draggingItem = null;
			AlybaGUI.getInstance().toggleAnalyzingButton(checkParsingAvailable());
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
		if(key.equals("URI"))
			txtCtrl = txt_uri;
		else if(key.equals("TIME"))
			txtCtrl = txt_time;
		else if(key.equals("IP"))
			txtCtrl = txt_ip;
		else if(key.equals("METHOD"))
			txtCtrl = txt_method;
		else if(key.equals("VERSION"))
			txtCtrl = txt_version;
		else if(key.equals("CODE"))
			txtCtrl = txt_code;
		else if(key.equals("BYTES"))
			txtCtrl = txt_bytes;
		else if(key.equals("ELAPSED"))
			txtCtrl = txt_elapsed;
		String val;
		String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
		String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
		if(value.indexOf(',') > 0) {
			StringTokenizer st = new StringTokenizer(value, ",");
			String concatValue = "";
			if(key.equals("URI") || key.equals("TIME")) {
				String joinChar = key.equals("URI") ? "?" : " ";
				int cnt = 1;
				while(st.hasMoreTokens()) {
					FieldIndex fld_idx = new FieldIndex(key + "_" + cnt, st.nextToken());
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
			if(key.equals("ELAPSED") && val.endsWith("ms")) {
				val = val.substring(0, val.indexOf("ms"));
			}
			txtCtrl.setText(val);
		}
		AlybaGUI.getInstance().toggleAnalyzingButton(checkParsingAvailable());
		return true;
	}

	protected void addLine() {
		String log_line = "";
		try {
			log_line = getSampleLogLine();
		} catch(Exception e) {
			debug(e);
		}
		if(log_line == null) {
			MessageUtil.showErrorMessage(getShell(), "Failed to sample a line.");
			return;
		}

		String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
		String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
		List<String> tokenList = ParserUtil.getTokenList(log_line, delimeter, bracelets);
		if(tokenList == null) {
			return;
		}
		fieldCount = tokenList.size();
		for(int i = 0; i < tokenList.size(); i++) {
			TableItem item = new TableItem(tbl_line, SWT.NULL);
			item.setText(0, String.valueOf(i + 1));
			item.setText(1, tokenList.get(i));
			debug("TOKEN_" + (i + 1) + " : " + tokenList.get(i));
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
		AlybaGUI.getInstance().toggleAnalyzingButton(checkParsingAvailable());
	}

	protected void checkElapsedString() {
		String elapsedStr = txt_elapsed.getText();
		boolean ms = false;
		try {
			if(!StringUtil.isNumeric(elapsedStr, false)) {
				if(elapsedStr.endsWith("ms")) {
					elapsedStr = elapsedStr.substring(0, elapsedStr.length() - 2);
					ms = true;
				}
				int idx = ParserUtil.getStartIndexOfDigit(elapsedStr);
				elapsedStr = elapsedStr.substring(idx);
				idx = ParserUtil.getEndIndexOfDigit(elapsedStr);
				elapsedStr = elapsedStr.substring(0, idx);
			}
			if(elapsedStr.length() > 0) {
				txt_elapsed.setText(elapsedStr);
				cb_elapsedUnit.setEnabled(true);
				if(ms) {
					cb_elapsedUnit.setText(Constant.ELAPSED_TIME_UNITS[0]);
				}
			} else {
				cb_elapsedUnit.setEnabled(false);
			}
		} catch(Exception e) {
			cb_elapsedUnit.setEnabled(false);
		}
	}

	protected String getSampleLogLine() throws Exception {
		TableItem[] items = AlybaGUI.getInstance().tbl_files.getItems();
		File logfile = null;
		int idx = -1;
		int cnt = 0;
		do {
			if(cnt == Constant.MAX_SAMPLING_COUNT)
				break;
			idx = NumberUtil.getRandomNumber(items.length);
			logfile = (File)items[idx].getData("file");
			cnt++;
		} while(logfile.length() == 0);
		
		if(cnt == Constant.MAX_SAMPLING_COUNT) {
			debug("Failed to sample a file.");
			return null;
		} else {
			String logfile_encoding = FileUtil.getFileEncoding(logfile.getPath());
			if("WINDOWS-1252".equals(logfile_encoding)) {
				String default_encoding = System.getProperty("file.encoding");
				debug("Unknown file encoding : " + logfile_encoding + ". It will be set to default(" + default_encoding + ")");
				logfile_encoding = default_encoding;
			}
			debug("File encoding : path='" + logfile.getPath() + "', encoding=" + logfile_encoding);
			
			int bound = 100 * 2;
			int line_number = 0;
			String line = null;
			cnt = 0;
			do {
				if(cnt == Constant.MAX_SAMPLING_COUNT)
					break;
				int temp = (int)(bound / 2);
				bound = (temp < 1) ? 1 : temp;
				line_number = NumberUtil.getRandomNumber(bound) + 1;
				line = FileUtil.readFileLine(logfile, line_number, logfile_encoding);
				cnt++;
				debug("sample a line(" + line_number + ") : " + line);
			} while(line == null || line.trim().equals("") || line.startsWith("#") || line.startsWith("format="));
			if(cnt == Constant.MAX_SAMPLING_COUNT) {
				debug("Failed to sample a line : " + logfile.getCanonicalPath());
				return null;
			} else {
				debug("Sampled Line(" + logfile.getCanonicalPath() + ":" + line_number + ") : \n" + line);
				return line;
			}
		}
	}

	public int checkMappingValidation() {
		TableItem[] items = AlybaGUI.getInstance().tbl_files.getItems();
		for(int i = 0; i < items.length; i++) {
			File logfile = (File)items[i].getData("file");
			String logfile_encoding = FileUtil.getFileEncoding(logfile.getPath());
			fileEncoding.add(logfile_encoding);
			BufferedReader br = null;
			String line = null;
			try {
				String time_idx = (String)mappingData.get("TIME");
				String[] time_idx_arr = time_idx.split(",");
				String uri_idx = (String)mappingData.get("URI");
				String[] uri_idx_arr = (uri_idx==null) ? new String[0] : uri_idx.split(",");
				String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
				String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
				if(logfile_encoding == null) {
					br = new BufferedReader(new InputStreamReader(new FileInputStream(logfile)));
				} else {
					br = new BufferedReader(new InputStreamReader(new FileInputStream(logfile), logfile_encoding));
				}
				while((line = br.readLine()) != null) {
					if(line.startsWith("#") || line.startsWith("format=") || line.trim().equals("")) {
						continue;
					}
					debug("check a line : " + line);
					List<String> main_tokens = ParserUtil.getTokenList(line, delimeter, bracelets);
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
						debug("time field : " + time_str);
						if(cb_timeFormat.getText().equals(Constant.UNIX_TIME_STR)) {
							if(ParserUtil.isMatchedUnixTimeFormat(time_str) == false) {
								debug("INVALID TIME TOKEN(" + time_idx + ") : " + time_str);
								return i;
							}
						} else {
							if(ParserUtil.isMatchedTimeFormat(cb_timeFormat.getText(), time_str, timeLocale) == false) {
								debug("INVALID TIME TOKEN(" + time_idx + ") : " + time_str);
								return i;
							}
						}
						if(uri_idx_arr != null) {
							String uri_str = "";
							for(String temp_idx : uri_idx_arr) {
								FieldIndex fld_idx = new FieldIndex("", temp_idx);
								String temp_fld = fld_idx.getField(main_tokens, delimeter, bracelets);
								if(temp_fld == null) {
									uri_str = "";
									break;
								} else {
									uri_str += "?" + temp_fld;
								}
							}
							uri_str = uri_str.startsWith("?") ? uri_str.substring(1) : uri_str;
							debug("uri field : " + uri_str);
						}
					} catch(Exception e) {
						debug(e);
						return i;
					}
					break;
				}
			} catch(Exception e) {
				debug(e);
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
		txt_uri.setText("");
		txt_time.setText("");
		spn_offset.setSelection(0);
		cb_timeFormat.setText(Constant.TIME_FORMATS[0]);
		txt_ip.setText("");
		txt_method.setText("");
		txt_version.setText("");
		txt_code.setText("");
		txt_bytes.setText("");
		txt_elapsed.setText("");
		cb_elapsedUnit.setText(Constant.ELAPSED_TIME_UNITS[0]);
		AlybaGUI.getInstance().toggleAnalyzingButton(checkParsingAvailable());
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

	public void autoMapping(FieldMappingInfo info) {
		try {
			setTextValue(txt_delimeter, info.fieldDelimeter);
			setComboValue(cb_timeFormat, info.timeFormat);
			setSpinnerValue(spn_offset, (int)(info.offsetHour*10));
			setComboValue(cb_elapsedUnit, info.elapsedUnit);
			addLine();
			mappingData = info.mappingInfo;
			String idx_str;
			int cnt = 1;
			for(String key : mappingData.keySet()) {
				idx_str = mappingData.get(key);
				debug("auto mapping(" + key + ") : " + idx_str);
				boolean success = mappingField(key, idx_str);
				if(!success) {
					throw new Exception("Failed to map default setting automatically. key=" + key + ", idx=" + idx_str);
				}
				String[] idx_str_arr = idx_str.split(",");
				FieldIndex[] fld_idx_arr = new FieldIndex[idx_str_arr.length];
				String data = null;
				for(int i = 0; i < idx_str_arr.length; i++) {
					fld_idx_arr[i] = new FieldIndex(key + "_" + i, idx_str_arr[i]);
					String temp_data = fld_idx_arr[i].getField(tbl_line.getItem(fld_idx_arr[i].getMainIndex()).getText(1), info.fieldDelimeter, info.getFieldBracelets());
					if(i == 0) {
						data = temp_data;
					} else {
						data += "?" + temp_data;
					}
				}
				debug("[" + cnt + "] idx : " + idx_str + ", data : " + data);
				cnt++;
			}
			spn_offset.setEnabled(false);
			cb_timeFormat.setEnabled(false);
			cb_elapsedUnit.setEnabled(false);
			if(!lb_resultTimeChk.getText().equals("OK")) {
				MessageUtil.showErrorMessage(getShell(), "Time format is invalid.");
				resetMappings();
			}
		} catch(Exception e) {
			MessageUtil.showErrorMessage(getShell(), "Failed to map default setting automatically.");
			debug(e);
			resetMappings();
		}
	}

	protected void autoMapping() {
		String type = cb_logType.getText();
		FieldMappingInfo info = getDefaultMappingInfo(type);
		if(info != null) {
			autoMapping(info);
		}
	}

	protected FieldMappingInfo getDefaultMappingInfo(String type) {
		FieldMappingInfo info = null;
		if(type.equals("Apache")) {
			info = DefaultMapping.APACHE;
		} else if(type.equals("Tomcat")) {
			info = DefaultMapping.TOMCAT;
		} else if(type.equals("WebtoB")) {
			info = DefaultMapping.WEBTOB;
		} else if(type.equals("Nginx")) {
			info = DefaultMapping.NGINX;
		} else if(type.equals("JEUS")) {
			info = DefaultMapping.JEUS;
		} else if(type.equals("IIS")) {
			info = DefaultMapping.IIS;
		}
		return info;
	}

	protected boolean checkParsingAvailable() {
		if(!mappingData.containsKey("TIME") || !lb_resultTimeChk.getText().equals("OK")) {
			return false;
		} else {
			return true;
		}
	}

	public void reset() {
		cb_logType.setText(Constant.LOG_TYPES[0]);
		txt_delimeter.setText(StringUtil.replaceMetaCharacter(Constant.LOG_DEFAULT_DELIMETER, true));
		txt_bracelet.setText(StringUtil.getStringFromArray(Constant.LOG_DEFAULT_BRACELETS, " "));
		resetMappings();
	}

	protected void debug(String s) {
		AlybaGUI.getInstance().debug(s);
	}

	protected void debug(Throwable t) {
		AlybaGUI.getInstance().debug(t);
	}

}
