package dal.tool.analyzer.alyba.ui.comp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
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
import dal.tool.analyzer.alyba.setting.DefaultMapping;
import dal.tool.analyzer.alyba.setting.ResourceFieldMappingInfo;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.FileUtil;
import dal.util.MathUtil;
import dal.util.NumberUtil;
import dal.util.StringUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class ResourceFieldMapping extends Composite {

	private ResultResource owner;
	
	private TableItem draggingItem;
	private HashMap<String, String> mappingData;
	private int fieldCount;
	private Locale timeLocale;
	private String header;

	private Group grp_customizeMapping;
	public CCombo cb_fileType;
	public CCombo cb_timeFormat;
	public Spinner spn_offset;
	private Table tbl_line;
	private TableViewer tblv_line;
	public Text txt_delimeter;
	public Text txt_bracelet;
	private Text txt_time;
	private Text txt_cpu;
	private Text txt_mem;
	private Text txt_disk;
	private Text txt_network;
	private Button btn_sampling;
	private Button btn_resetMapping;
	private Button chk_cpu_isIdle;
	private Button chk_mem_isIdle;
	private Button chk_disk_isIdle;
	private Button chk_network_isIdle;
	private Label lb_resultTimeChk;
	private DragSource ds_tableItem;
	private DropTarget dt_textTime;
	private DropTarget dt_textCpu;
	private DropTarget dt_textMem;
	private DropTarget dt_textDisk;
	private DropTarget dt_textNetwork;

	public ResourceFieldMapping(Composite parent, int style, ResultResource owner) {
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

	public HashMap<String, String> getMappingData() {
		return mappingData;
	}

	public int getFieldCount() {
		return fieldCount;
	}

	public Locale getTimeLocale() {
		return timeLocale;
	}
	
	public boolean isCpuIdle() {
		return chk_cpu_isIdle.getSelection();
	}

	public boolean isMemoryIdle() {
		return chk_mem_isIdle.getSelection();
	}

	public boolean isDiskIdle() {
		return chk_disk_isIdle.getSelection();
	}

	public boolean isNetworkIdle() {
		return chk_network_isIdle.getSelection();
	}

	protected void createContents() {

		setSize(900, 400);		

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
		cb_fileType.setVisibleItemCount(10);
		cb_fileType.setItems(Constant.FILE_TYPES);
		cb_fileType.setFont(Utility.getFont());
		cb_fileType.setText(Constant.FILE_TYPES[0]);
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
		fd_lb_arrow.top = new FormAttachment(0, 90);
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
		fd_lb_cpu.top = new FormAttachment(cb_timeFormat, 10, SWT.BOTTOM);
		fd_lb_cpu.left = new FormAttachment(lb_time, 0, SWT.LEFT);
		fd_lb_cpu.right = new FormAttachment(lb_time, 0, SWT.RIGHT);
		Label lb_cpu = new Label(grp_customizeMapping, SWT.NONE);
		lb_cpu.setLayoutData(fd_lb_cpu);
		lb_cpu.setFont(Utility.getFont());
		lb_cpu.setText("CPU");
		lb_cpu.setAlignment(SWT.RIGHT);
		
		FormData fd_txt_cpu = new FormData();
		fd_txt_cpu.top = new FormAttachment(lb_cpu, -2, SWT.TOP);
		fd_txt_cpu.left = new FormAttachment(lb_cpu, 10, SWT.RIGHT);
		fd_txt_cpu.width = 170;
		txt_cpu = new Text(grp_customizeMapping, SWT.BORDER);
		txt_cpu.setLayoutData(fd_txt_cpu);
		txt_cpu.setEditable(false);
		txt_cpu.setFont(Utility.getFont());
		dt_textCpu = new DropTarget(txt_cpu, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textCpu.setTransfer(Constant.TEXT_TRANSFER_TYPE);		
		
		FormData fd_chk_cpu_isIdle = new FormData();
		fd_chk_cpu_isIdle.top = new FormAttachment(lb_cpu, 1, SWT.TOP);
		fd_chk_cpu_isIdle.left = new FormAttachment(txt_cpu, 5, SWT.RIGHT);
		chk_cpu_isIdle = new Button(grp_customizeMapping, SWT.CHECK);
		chk_cpu_isIdle.setLayoutData(fd_chk_cpu_isIdle);
		chk_cpu_isIdle.setText("Idle");
		chk_cpu_isIdle.setEnabled(false);

		FormData fd_lb_mem = new FormData();
		fd_lb_mem.top = new FormAttachment(txt_cpu, 10, SWT.BOTTOM);
		fd_lb_mem.left = new FormAttachment(lb_time, 0, SWT.LEFT);
		fd_lb_mem.right = new FormAttachment(lb_time, 0, SWT.RIGHT);
		Label lb_mem = new Label(grp_customizeMapping, SWT.NONE);
		lb_mem.setLayoutData(fd_lb_mem);
		lb_mem.setFont(Utility.getFont());
		lb_mem.setText("MEM");
		lb_mem.setAlignment(SWT.RIGHT);

		FormData fd_txt_mem = new FormData();
		fd_txt_mem.top = new FormAttachment(lb_mem, -2, SWT.TOP);
		fd_txt_mem.left = new FormAttachment(lb_mem, 10, SWT.RIGHT);
		fd_txt_mem.width = 170;
		txt_mem = new Text(grp_customizeMapping, SWT.BORDER);
		txt_mem.setLayoutData(fd_txt_mem);
		txt_mem.setEditable(false);
		txt_mem.setFont(Utility.getFont());
		txt_mem.setBounds(406, 106, 167, 21);
		dt_textMem = new DropTarget(txt_mem, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textMem.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		FormData fd_chk_mem_isIdle = new FormData();
		fd_chk_mem_isIdle.top = new FormAttachment(lb_mem, 1, SWT.TOP);
		fd_chk_mem_isIdle.left = new FormAttachment(txt_mem, 5, SWT.RIGHT);
		chk_mem_isIdle = new Button(grp_customizeMapping, SWT.CHECK);
		chk_mem_isIdle.setLayoutData(fd_chk_mem_isIdle);
		chk_mem_isIdle.setText("Idle");
		chk_mem_isIdle.setEnabled(false);

		FormData fd_lb_disk = new FormData();
		fd_lb_disk.top = new FormAttachment(txt_mem, 10, SWT.BOTTOM);
		fd_lb_disk.left = new FormAttachment(lb_time, 0, SWT.LEFT);
		fd_lb_disk.right = new FormAttachment(lb_time, 0, SWT.RIGHT);
		Label lb_disk = new Label(grp_customizeMapping, SWT.NONE);
		lb_disk.setLayoutData(fd_lb_disk);
		lb_disk.setFont(Utility.getFont());
		lb_disk.setText("DISK");
		lb_disk.setAlignment(SWT.RIGHT);

		FormData fd_txt_disk = new FormData();
		fd_txt_disk.top = new FormAttachment(lb_disk, -2, SWT.TOP);
		fd_txt_disk.left = new FormAttachment(lb_disk, 10, SWT.RIGHT);
		fd_txt_disk.width = 170;
		txt_disk = new Text(grp_customizeMapping, SWT.BORDER);
		txt_disk.setLayoutData(fd_txt_disk);
		txt_disk.setEditable(false);
		txt_disk.setFont(Utility.getFont());
		dt_textDisk = new DropTarget(txt_disk, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textDisk.setTransfer(Constant.TEXT_TRANSFER_TYPE);

		FormData fd_chk_disk_isIdle = new FormData();
		fd_chk_disk_isIdle.top = new FormAttachment(lb_disk, 1, SWT.TOP);
		fd_chk_disk_isIdle.left = new FormAttachment(txt_disk, 5, SWT.RIGHT);
		chk_disk_isIdle = new Button(grp_customizeMapping, SWT.CHECK);
		chk_disk_isIdle.setLayoutData(fd_chk_disk_isIdle);
		chk_disk_isIdle.setText("Idle");
		chk_disk_isIdle.setEnabled(false);

		FormData fd_lb_network = new FormData();
		fd_lb_network.top = new FormAttachment(txt_disk, 10, SWT.BOTTOM);
		fd_lb_network.left = new FormAttachment(lb_time, 0, SWT.LEFT);
		fd_lb_network.right = new FormAttachment(lb_time, 0, SWT.RIGHT);
		Label lb_network = new Label(grp_customizeMapping, SWT.NONE);
		lb_network.setLayoutData(fd_lb_network);
		lb_network.setFont(Utility.getFont());
		lb_network.setText("N/W");
		lb_network.setAlignment(SWT.RIGHT);

		FormData fd_txt_network = new FormData();
		fd_txt_network.top = new FormAttachment(lb_network, -2, SWT.TOP);
		fd_txt_network.left = new FormAttachment(lb_network, 10, SWT.RIGHT);
		fd_txt_network.width = 170;
		txt_network = new Text(grp_customizeMapping, SWT.BORDER);
		txt_network.setLayoutData(fd_txt_network);
		txt_network.setEditable(false);
		txt_network.setFont(Utility.getFont());
		dt_textNetwork = new DropTarget(txt_network, DND.DROP_MOVE | DND.DROP_COPY);
		dt_textNetwork.setTransfer(Constant.TEXT_TRANSFER_TYPE);
		
		FormData fd_chk_network_isIdle = new FormData();
		fd_chk_network_isIdle.top = new FormAttachment(lb_network, 1, SWT.TOP);
		fd_chk_network_isIdle.left = new FormAttachment(txt_network, 5, SWT.RIGHT);
		chk_network_isIdle = new Button(grp_customizeMapping, SWT.CHECK);
		chk_network_isIdle.setLayoutData(fd_chk_network_isIdle);
		chk_network_isIdle.setText("Idle");
		chk_network_isIdle.setEnabled(false);

		grp_customizeMapping.setTabList(new Control[] { txt_time, spn_offset, cb_timeFormat, txt_cpu, chk_cpu_isIdle, txt_mem, chk_mem_isIdle, txt_disk, chk_disk_isIdle, txt_network, chk_network_isIdle });

		setTabList(new Control[] { cb_fileType, txt_delimeter, txt_bracelet, btn_sampling, btn_resetMapping });
		
	}

	protected void addEventListener() {

		cb_fileType.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				resetMappings();
				boolean isCustomize = cb_fileType.getText().equals(Constant.FILE_TYPES[0]);
				grp_customizeMapping.setEnabled(isCustomize);
				ResourceFieldMappingInfo[] info = getDefaultMappingInfo(cb_fileType.getText());
				if(info == null || info.length == 0) {
					txt_delimeter.setText(StringUtil.replaceMetaCharacter(Constant.FILE_DEFAULT_DELIMETER, true));
					txt_bracelet.setText(StringUtil.getStringFromArray(Constant.FILE_DEFAULT_BRACELETS, " "));
				} else if(info.length == 1) {
					txt_delimeter.setText(StringUtil.replaceMetaCharacter(info[0].fieldDelimeter, true));
					txt_bracelet.setText(info[0].fieldBracelet);
				}
				txt_delimeter.setEnabled(isCustomize);
				txt_bracelet.setEnabled(isCustomize);
				resetMappings();
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
				boolean isCustomize = cb_fileType.getText().equals(Constant.FILE_TYPES[0]);
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
		addDoubleClickListeners();

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

		dt_textCpu.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("CPU", txt_cpu);
				}
			}
		});

		dt_textMem.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("MEM", txt_mem);
				}
			}
		});

		dt_textDisk.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("DISK", txt_disk);
				}
			}
		});

		dt_textNetwork.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent e) {
				if(TextTransfer.getInstance().isSupportedType(e.currentDataType)) {
					mappingDataByDND("NETWORK", txt_network);
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
		
		txt_cpu.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("CPU", txt_cpu);
				}
			}
		});
		
		txt_mem.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("MEM", txt_mem);
				}
			}
		});

		txt_disk.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("DISK", txt_disk);
				}
			}
		});

		txt_network.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(e.keyCode == 127) {
					removeMappingData("NETWORK", txt_network);
				}
			}
		});

	}

	protected void addDoubleClickListeners() {

		txt_cpu.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if(!txt_cpu.getText().isEmpty()) {
					calculatingDataByNumber("CPU", txt_cpu);
				}
			}
		});

		txt_mem.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if(!txt_mem.getText().isEmpty()) {
					calculatingDataByNumber("MEM", txt_mem);
				}
			}
		});

		txt_disk.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if(!txt_disk.getText().isEmpty()) {
					calculatingDataByNumber("DISK", txt_disk);
				}
			}
		});

		txt_network.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if(!txt_network.getText().isEmpty()) {
					calculatingDataByNumber("NETWORK", txt_network);
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
		if(!txtCtrl.getText().equals("")) {
			if(key.equals("TIME")) {
				String[] labels = new String[] { "Replace", "Append" };
				int i = MessageUtil.showSelectMessage(getShell(), "Question", "What would you do with this field?", labels);
				if(i == 1) {
					mapStr = (String)mappingData.get(key) + "," + mapStr;
					fldStr = txtCtrl.getText() + " " + fldStr;
				} else if(i < 0) {
					return false;
				}
			} else {
				String[] labels = new String[] { "Replace", "Calculate" };
				int i = MessageUtil.showSelectMessage(getShell(), "Question", "What would you do with this field?", labels);
				if(i == 1) {
		            CalculationInputDialog dialog = new CalculationInputDialog(getShell());
		            String currInput = ((String)mappingData.get(key)).startsWith("{") ? txtCtrl.getText() : "{$"+mappingData.get(key)+"}="+txtCtrl.getText(); 
		            dialog.setValues(currInput, "{$"+mapStr+"}="+fldStr);
		            if(dialog.open() == Dialog.OK) {
		            	if(dialog.isResultValid()) {
			            	fldStr = dialog.getResultValue();
			            	mapStr = fldStr.split("=")[0];
		            	} else {
		            		MessageUtil.showWarningMessage(getShell(), "The calculated value is invalid and will be ignored.");
		            		return false;
		            	}
		            } else {
		            	return false;
		            }
				} else if(i < 0) {
					return false;
				}
			}
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
	
	protected boolean calculatingDataByNumber(String key, Text txtCtrl) {
		if(key == null)
			return false;
		String mapStr;
		String fldStr;
		if(!MessageUtil.showConfirmMessage(getShell(), "Do you want to perform numeric operations on this field?")) {
			return false;
		}
        CalculationInputDialog dialog = new CalculationInputDialog(getShell());
        String currInput = ((String)mappingData.get(key)).startsWith("{") ? txtCtrl.getText() : "{$"+mappingData.get(key)+"}="+txtCtrl.getText(); 
        dialog.setValues(currInput, null);
        if(dialog.open() == Dialog.OK) {
        	if(dialog.isResultValid()) {
        		fldStr = dialog.getResultValue();
        		mapStr = fldStr.split("=")[0];
        	} else {
        		MessageUtil.showWarningMessage(getShell(), "The calculated value is invalid and will be ignored.");
        		return false;
        	}
        } else {
        	return false;
        }
		Logger.debug("mapping:" + mapStr + ", field:" + fldStr);
		mappingData.put(key, mapStr);
		txtCtrl.setText(fldStr);
		owner.toggleAnalyzingButton(checkParsingAvailable());
		return true;
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

	protected String autoMappingField(String mappingKey, String mappingIdxValue) {
		if(mappingKey == null)
			return null;
		if(mappingIdxValue == null || mappingIdxValue.equals(""))
			return null;
		Text txtCtrl = null;
		if(mappingKey.equals("TIME"))
			txtCtrl = txt_time;
		else if(mappingKey.equals("CPU"))
			txtCtrl = txt_cpu;
		else if(mappingKey.equals("MEM"))
			txtCtrl = txt_mem;
		else if(mappingKey.equals("DISK"))
			txtCtrl = txt_disk;
		else if(mappingKey.equals("NETWORK"))
			txtCtrl = txt_network;
		String mappedValue = null;
		String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
		String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
		if(mappingIdxValue.startsWith("{") && mappingIdxValue.endsWith("}")) {
			String expr = mappingIdxValue.substring(1, mappingIdxValue.length()-1);
			List<String> fieldValues = getFieldValues();
			try {
				List<Double> variables = NumberUtil.toDoubleList(fieldValues);
				Logger.debug("Evaluating : expression='" + expr + "', variables=" + variables);
				Double evalResult = MathUtil.evaluateExpression(expr, variables);
				String result = String.valueOf(evalResult.doubleValue());
				if(result.substring(result.indexOf('.')+1).length() > 4) {
					mappedValue = mappingIdxValue + "=" + Math.round(evalResult.doubleValue()*10000)/10000.0;
				} else {
					mappedValue = mappingIdxValue + "=" + evalResult.doubleValue();
				}
			} catch(Exception e) {
				Logger.debug("Failed to evaluate the expression : '" + expr + "' : " + e);
				return null;
			}
		} else if(mappingIdxValue.indexOf(',') > 0) {
			if(mappingKey.equals("TIME")) {
				StringTokenizer st = new StringTokenizer(mappingIdxValue, ",");
				String concatValue = "";
				String joinChar = " ";
				int cnt = 1;
				while(st.hasMoreTokens()) {
					String idx_str = st.nextToken();
					if(idx_str.startsWith("H")) {
						int header_idx = Integer.parseInt(idx_str.substring(1));
						StringTokenizer st2 = new StringTokenizer(header, delimeter);
						for(int i = 0; i < header_idx; i++) {
							mappedValue = st2.nextToken();
						}
					} else {
						FieldIndex fld_idx = new FieldIndex(mappingKey + "_" + cnt, idx_str);
						String main_fld = tbl_line.getItem(fld_idx.getMainIndex()).getText(1);
						mappedValue = fld_idx.getField(main_fld, delimeter, bracelets);
					}
					if(cnt == 1) {
						if(mappedValue == null) {
							return null;
						}
						concatValue = mappedValue;
					} else {
						concatValue += (mappedValue == null) ? "" : (joinChar + mappedValue);
					}
					cnt++;
				}
				mappedValue = concatValue.trim();
			} else {
				Logger.error("Only the TIME field supports multi-column mapping.");
				throw new RuntimeException("Only the TIME field supports multi-column mapping.");
			}
		} else {
			FieldIndex fld_idx = new FieldIndex(mappingKey, mappingIdxValue);
			String main_fld = tbl_line.getItem(fld_idx.getMainIndex()).getText(1);
			mappedValue = fld_idx.getField(main_fld, delimeter, bracelets);
			if(mappedValue == null) {
				return null;
			}
		}
		txtCtrl.setText(mappedValue);
		owner.toggleAnalyzingButton(checkParsingAvailable());
		return mappedValue;
	}
	
	protected List<String> getFieldValues() {
		List<String> result = new ArrayList<String>(tbl_line.getItemCount());
		for(TableItem item : tbl_line.getItems()) {
			result.add(item.getText(1));
		}
		return result;
	}

	protected void addLine() {
		String file_line = "";
		try {
			int cnt = 0;
			do {
				if(cnt == Constant.MAX_SAMPLING_COUNT)
					break;
				file_line = getSampleLine();
				if(file_line == null || file_line.trim().equals("") || file_line.startsWith("#")) {
					continue;
				}
				cnt++;
				if(getFileType().equals("vmstat")) {
					if(!headerOfVmstat(file_line)) {
						break;
					}
				} else {
					break;
				}
			} while(true);
		} catch(Exception e) {
			Logger.debug("Failed to sample a line from logfile.");
			Logger.error(e);
		}
		if(file_line == null) {
			MessageUtil.showErrorMessage(getShell(), "Failed to sample a line.");
			return;
		}
		fillTableDataWithLogLine(file_line);
	}
	
	protected void fillTableDataWithLogLine(String line) {
		tbl_line.removeAll();
		String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
		String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
		List<String> tokenList = ParserUtil.getTokenList(line, delimeter, bracelets, (AlybaGUI.instance==null)?false:AlybaGUI.getInstance().optionSetting.checkStrictCheck());
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
			int bound = 100 * 2;
			int line_number = 0;
			String line = null;
			boolean read_header = false;
			cnt = 0;
			do {
				if(cnt == Constant.MAX_SAMPLING_COUNT)
					break;
				int temp = (int)(bound / 2);
				bound = (temp < 1) ? 1 : temp;
				line_number = NumberUtil.getRandomNumber(bound) + 1;
				line = FileUtil.readFileLine(file, line_number, null);
				if(!read_header) {
					List<String> headers = FileUtil.head(file, 1, null);
					header = (headers == null || headers.size() < 1) ? null : headers.get(0);
					read_header = true;
				}
				cnt++;
				Logger.debug("sample a line(" + line_number + ") : " + line);
			} while(line == null || line.trim().equals("") || line.startsWith("#") || headerOfVmstat(line) || headerOfSar(line));
			if(cnt == Constant.MAX_SAMPLING_COUNT) {
				Logger.debug("Failed to sample a line : " + file.getCanonicalPath());
				return null;
			} else {
				Logger.debug("Sampled Line(" + file.getCanonicalPath() + ":" + line_number + ") : \n" + line);
				return line;
			}
		}
	}

	private boolean headerOfVmstat(String line) {
		if(cb_fileType.getText().equals("vmstat") == false) {
			return false;
		}
		if(line.indexOf("-cpu-") > -1 || line.indexOf("free") > -1) {
			return true;
		}
		return false;
	}

	private boolean headerOfSar(String line) {
		if(cb_fileType.getText().equals("sar") == false) {
			return false;
		}
		if((line.length() > 0 && line.charAt(0) != ' ' && !Character.isDigit(line.charAt(0))) || !Character.isDigit(line.charAt(line.length()-1))) {
			return true;
		}
		return false;
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
					if(count < 3 || line == null || line.trim().equals("") || line.startsWith("#") || headerOfVmstat(line) || headerOfSar(line)) {
						continue;
					}
					Logger.debug("check a line : " + line);
					List<String> main_tokens = ParserUtil.getTokenList(line, delimeter, bracelets, AlybaGUI.instance==null?false:AlybaGUI.getInstance().optionSetting.checkStrictCheck());
					try {
						String time_str = "";
						for(String temp_idx : time_idx_arr) {
							String temp_fld = null;
							if(temp_idx.startsWith("H")) {
								int header_idx = Integer.parseInt(temp_idx.substring(1));
								StringTokenizer st2 = new StringTokenizer(header, delimeter);
								for(int j = 0; j < header_idx; j++) {
									temp_fld = st2.nextToken();
								}
							} else {							
								FieldIndex fld_idx = new FieldIndex("", temp_idx);
								temp_fld = fld_idx.getField(main_tokens, delimeter, bracelets);
							}
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
		header = null;
		tbl_line.removeAll();
		txt_time.setText("");
		spn_offset.setSelection(0);
		cb_timeFormat.setText(Constant.TIME_FORMATS[0]);
		txt_cpu.setText("");
		txt_mem.setText("");
		txt_disk.setText("");
		txt_network.setText("");
		chk_cpu_isIdle.setSelection(false);
		chk_mem_isIdle.setSelection(false);
		chk_disk_isIdle.setSelection(false);
		chk_network_isIdle.setSelection(false);
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

	public boolean autoMapping(ResourceFieldMappingInfo info) {
		try {
			setTextValue(txt_delimeter, StringUtil.replaceMetaCharacter(info.fieldDelimeter, true));
			setComboValue(cb_timeFormat, info.timeFormat);
			setSpinnerValue(spn_offset, (int)(info.offsetHour*10));
			addLine();
			mappingData = info.mappingInfo;
			for(String key : mappingData.keySet()) {
				if("DEVICE".equals(key)) continue;
				getFieldDataByAutoMapping(key, info);
			}
			spn_offset.setEnabled(false);
			cb_timeFormat.setEnabled(false);
			chk_cpu_isIdle.setSelection(info.isCpuIdle());
			chk_mem_isIdle.setSelection(info.isMemoryIdle());
			chk_disk_isIdle.setSelection(info.isDiskIdle());
			chk_network_isIdle.setSelection(info.isNetworkIdle());
			if(!lb_resultTimeChk.getText().equals("OK")) {
				throw new Exception("Time format is invalid.");
			}
			return true;
		} catch(Exception e) {
			Logger.debug("Failed to map default setting automatically : " + info);
			resetMappings();
			return false;
		}
	}

	public boolean autoMappingFromSar(ResourceFieldMappingInfo info) {
		try {
			setTextValue(txt_delimeter, StringUtil.replaceMetaCharacter(info.fieldDelimeter, true));
			setComboValue(cb_timeFormat, info.timeFormat);
			setSpinnerValue(spn_offset, (int)(info.offsetHour*10));
			mappingData = info.mappingInfo;
			TableItem[] items = owner.tbl_files.getItems();
			File file = null;
			int cnt = 0;
			do {
				if(cnt == Constant.MAX_SAMPLING_COUNT)
					break;
				int idx = NumberUtil.getRandomNumber(items.length);
				file = (File)items[idx].getData("file");
				cnt++;
			} while(file.length() == 0);			
			int line_number = 0;
			String line = null;
			String resourceHeader = null;
			boolean[] meetHeader = new boolean[4];
			boolean[] completeMapping = new boolean[4];
			boolean completeTime = false;
			String key = null;
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				while((line = br.readLine()) != null) {
					line_number++;
					if(line_number == 1) {
						header = line;
					} else if(line.trim().equals("") || !Character.isDigit(line.charAt(0))) {
						continue;
					} else if(!completeMapping[0] && !meetHeader[0] && line.indexOf("%idle") > -1) {
						meetHeader[0] = true; meetHeader[1] = false; meetHeader[2] = false; meetHeader[3] = false; key = "CPU"; resourceHeader = line;
					} else if(!completeMapping[1] && !meetHeader[1] && line.indexOf("%memused") > -1) {
						meetHeader[0] = false; meetHeader[1] = true; meetHeader[2] = false; meetHeader[3] = false; key = "MEM"; resourceHeader = line;
					} else if(!completeMapping[2] && !meetHeader[2] && line.indexOf("%util") > -1) {
						meetHeader[0] = false; meetHeader[1] = false; meetHeader[2] = true; meetHeader[3] = false; key = "DISK"; resourceHeader = line;
					} else if(!completeMapping[3] && !meetHeader[3] && line.indexOf("rxkB/s") > -1) {
						meetHeader[0] = false; meetHeader[1] = false; meetHeader[2] = false; meetHeader[3] = true; key = "NETWORK"; resourceHeader = line;
					} else {
						if(key == null || !Character.isDigit(line.charAt(line.length()-1))) {
							continue;
						}
						fillTableDataWithLogLine(line);
						Logger.debug(key + " Sampled Line(" + file.getCanonicalPath() + ":" + line_number + ") : \n" + line);
						if(!completeTime) {
							getFieldDataByAutoMapping("TIME", info);
							completeTime = true;
						}
						convertVariableField(key, resourceHeader, info);
						getFieldDataByAutoMapping(key, info);
						if(meetHeader[0]) {
							meetHeader[0] = false; completeMapping[0] = true; key = null; resourceHeader = null;
						} else if(meetHeader[1]) {
							meetHeader[1] = false; completeMapping[1] = true; key = null; resourceHeader = null;
						} else if(meetHeader[2]) {
							meetHeader[2] = false; completeMapping[2] = true; key = null; resourceHeader = null;
						} else if(meetHeader[3]) {
							meetHeader[3] = false; completeMapping[3] = true; key = null; resourceHeader = null;
						}
					}					
				}
			} catch(Exception e) {
				throw e;
			} finally {
				try {
					br.close();
				} catch(Exception e2) {
				}
			}
			spn_offset.setEnabled(false);
			cb_timeFormat.setEnabled(false);
			chk_cpu_isIdle.setSelection(info.isCpuIdle());
			chk_mem_isIdle.setSelection(info.isMemoryIdle());
			chk_disk_isIdle.setSelection(info.isDiskIdle());
			chk_network_isIdle.setSelection(info.isNetworkIdle());
			if(!lb_resultTimeChk.getText().equals("OK")) {
				throw new Exception("Time format is invalid.");
			}
			cnt = 0;
			for(int i = 0; i < completeMapping.length; i++) {
				if(completeMapping[i]) {
					cnt++;
				} else {
					if(i == 0) info.mappingInfo.remove("CPU");
					else if(i == 1) info.mappingInfo.remove("MEM");
					else if(i == 2) info.mappingInfo.remove("DISK");
					else if(i == 3) info.mappingInfo.remove("NETWORK");
				}
			}
			if(cnt == 0) {
				throw new Exception("No mapped fields.");
			} else if(cnt > 1) {
				tbl_line.removeAll();
				fieldCount = -1;
			}
			return true;
		} catch(Exception e) {
			Logger.debug("Failed to map default setting automatically : " + e.getMessage() + " : " + info);
			resetMappings();
			return false;
		}
	}
	
	protected void convertVariableField(String key, String header, ResourceFieldMappingInfo info) throws Exception {
		if(key == null || header == null || !info.getMappingInfo().containsKey(key)) {
			return;
		}
		String idx_str = info.getMappingInfo().get(key);
		if(idx_str.startsWith("'") && idx_str.endsWith("'")) {
			idx_str = idx_str.substring(1, idx_str.length()-1);
			String delimeter = StringUtil.replaceMetaCharacter(info.getFieldDelimeter(), false);
			String[] bracelets = StringUtil.getArrayFromString(info.getFieldBracelet(), " ");
			List<String> tokenList = ParserUtil.getTokenList(header, delimeter, bracelets, (AlybaGUI.instance==null)?false:AlybaGUI.getInstance().optionSetting.checkStrictCheck());
			int idx = tokenList.indexOf(idx_str);
			if(idx < 0) {
				throw new Exception("No field name(" + idx_str + ") in header(" + header + ")");
			} else {
				info.getMappingInfo().put(key, String.valueOf(idx+1));
			}
		}
	}
	
	protected String getFieldDataByAutoMapping(String key, ResourceFieldMappingInfo info) throws Exception {
		String idx_str = info.getMappingInfo().get(key);
		Logger.debug("auto mapping(" + key + ") : " + idx_str);
		String data = autoMappingField(key, idx_str);
		if(data == null) {
			throw new Exception("Failed to map default setting automatically. key=" + key + ", idx=" + idx_str);
		} else if(key.equals("TIME")) {
			SimpleDateFormat sdf = new SimpleDateFormat(info.getTimeFormat(), info.getTimeLocale());
			sdf.setLenient(false);
			sdf.parse(data);
		}
		Logger.debug("[" + key + "] idx : " + idx_str + ", data : " + data);
		return data;
	}
	
	protected void autoMapping() {
		String type = cb_fileType.getText();
		ResourceFieldMappingInfo[] infos = getDefaultMappingInfo(type);
		if(infos != null) {
			boolean success = false;
			for(ResourceFieldMappingInfo info : infos) {
				txt_delimeter.setText(StringUtil.replaceMetaCharacter(info.fieldDelimeter, true));
				txt_bracelet.setText(info.fieldBracelet);
				if(type.equals("sar")) {
					success = autoMappingFromSar(info);
				} else {
					success = autoMapping(info);
				}
				if(success) {
					break;
				}
			}
			if(!success) {
				MessageUtil.showErrorMessage(getShell(), "Failed to map default setting automatically.");
			}
		}
	}

	protected ResourceFieldMappingInfo[] getDefaultMappingInfo(String type) {
		List<ResourceFieldMappingInfo> infos = new ArrayList<ResourceFieldMappingInfo>();
		if(type.equals(Constant.FILE_TYPES[1])) {
			infos.add(DefaultMapping.VMSTAT);
		} else if(type.equals(Constant.FILE_TYPES[2])) {
			infos.add(DefaultMapping.SAR_KO.copy());
			infos.add(DefaultMapping.SAR_EN.copy());
			infos.add(DefaultMapping.SAR_C.copy());			
		}
		return infos.toArray(new ResourceFieldMappingInfo[infos.size()]);
	}

	protected boolean checkParsingAvailable() {
		chk_cpu_isIdle.setEnabled(mappingData.containsKey("CPU"));
		chk_mem_isIdle.setEnabled(mappingData.containsKey("MEM"));
		chk_disk_isIdle.setEnabled(mappingData.containsKey("DISK"));
		chk_network_isIdle.setEnabled(mappingData.containsKey("NETWORK"));
		if(!mappingData.containsKey("TIME") || !lb_resultTimeChk.getText().equals("OK") || mappingData.keySet().size() < 2) {
			return false;
		} else {
			return true;
		}
	}

	public void reset() {
		cb_fileType.setText(Constant.FILE_TYPES[0]);
		txt_delimeter.setText(StringUtil.replaceMetaCharacter(Constant.FILE_DEFAULT_DELIMETER, true));
		txt_bracelet.setText(StringUtil.getStringFromArray(Constant.FILE_DEFAULT_BRACELETS, " "));
		resetMappings();
	}
	
	public String[] getDeviceNames(String type) throws Exception {
		TableItem[] items = owner.tbl_files.getItems();
		int fileIdx = NumberUtil.getRandomNumber(items.length);
		File file = (File)items[fileIdx].getData("file");
		int deviceIdx = Integer.parseInt(mappingData.get("DEVICE")) - 1;
		List<String> deviceList = new ArrayList<String>();
		String line = null;
		int cnt = 0;
		int line_number = 1;
		boolean found = false;
		do {
			if(cnt == Constant.MAX_SAMPLING_COUNT) {
				break;
			}
			line = FileUtil.readFileLine(file, line_number++, null);
			if(line == null) {
				break;
			}
			if(("CPU".equals(type) && line.indexOf("%idle")>-1) || ("MEM".equals(type) && line.indexOf("%memused")>-1) || ("DISK".equals(type) && line.indexOf("%util")>-1) || ("NETWORK".equals(type) && line.indexOf("rxkB/s")>-1)) {
				found = true;
				continue;
			}
			if(!found || line.trim().equals("") || !Character.isDigit(line.charAt(line.length()-1)) || line.startsWith("#") || headerOfSar(line)) {
				continue;
			}
			String delimeter = StringUtil.replaceMetaCharacter(getDelimeter(), false);
			String[] bracelets = StringUtil.getArrayFromString(getBracelet(), " ");
			List<String> tokenList = ParserUtil.getTokenList(line, delimeter, bracelets);
			String deviceName = tokenList.get(deviceIdx);
			if(deviceList.contains(deviceName)) {
				break;
			} else {
				deviceList.add(deviceName);
			}
			cnt++;			
		} while(true);
		if(cnt == Constant.MAX_SAMPLING_COUNT) {
			Logger.debug("Failed to sample a line : " + file.getCanonicalPath());
			return null;
		} else {
			Logger.debug("Sampled Devices(" + file.getCanonicalPath() + ") : " + deviceList);
			return deviceList.toArray(new String[deviceList.size()]);
		}
	}

}
