package dal.tool.analyzer.alyba.ui.comp;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.output.vo.BadTransactionEntryVO;
import dal.tool.analyzer.alyba.output.vo.EntryVO;
import dal.tool.analyzer.alyba.output.vo.KeyEntryVO;
import dal.tool.analyzer.alyba.output.vo.ResourceUsageEntryVO;
import dal.tool.analyzer.alyba.output.vo.SettingEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPMEntryVO;
import dal.tool.analyzer.alyba.output.vo.TPSEntryVO;
import dal.tool.analyzer.alyba.output.vo.TimeAggregationEntryVO;
import dal.tool.analyzer.alyba.output.vo.TransactionEntryVO;
import dal.tool.analyzer.alyba.setting.LogFieldMappingInfo;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.DateUtil;
import dal.util.NumberUtil;
import dal.util.ReflectionUtil;
import dal.util.csv.CSVWriter;
import dal.util.db.ObjectDBUtil;
import dal.util.swt.FileDialogUtil;
import dal.util.swt.MessageUtil;
import dal.util.swt.SWTResourceManager;

public class ResultData extends Composite {

	private static LinkedHashMap<String,Class<?>> map_table = new LinkedHashMap<String,Class<?>>(13); 

	private SashForm sf_main;
	private Composite comp_left;
	private Composite comp_right;	
	private Group grp_left1;
	private Group grp_left2;
	private Group grp_right;
	private Group grp_result;
	private Table tbl_tables;
	private Table tbl_columns;
	private Table tbl_data;
	private TableViewer tblv_data;
	private TableCursor cursor_data;
	private Clipboard clipboard_data;
	private Text txt_cquery;
	private Text txt_squery;
	private Button btn_execute;
	private Button btn_export;
	private Label lb_table_cnt;
	private Label lb_column_cnt;
	private Label lb_cquery;
	private Label lb_squery;
	private Label lb_rows;
	private Label lb_elapsed;
	private Label lb_result_s;
	private Label lb_result_f;

	private int table_idx = -1;
	private long total_rows = 0;
	private int start_row = 0;
	private String sort_query = "";
	private ObjectDBUtil db = null;
	private EntityManager em = null;
	private String last_saved_dir = null;
	private SettingEntryVO settingVo = null;

	static {
		map_table.put("Transaction Per Second", TPSEntryVO.class);
		map_table.put("Transaction Per Minute", TPMEntryVO.class);
		map_table.put("Daily Transaction", TimeAggregationEntryVO.class);
		map_table.put("Houly Transaction", TimeAggregationEntryVO.class);
		map_table.put("URI Aggregation", KeyEntryVO.class);
		map_table.put("EXT Aggregation", KeyEntryVO.class);
		map_table.put("IP Aggregation", KeyEntryVO.class);
		map_table.put("METHOD Aggregation", KeyEntryVO.class);
		map_table.put("VERSION Aggregation", KeyEntryVO.class);
		map_table.put("CODE Aggregation", KeyEntryVO.class);
		map_table.put("Over-Time Aggregation", BadTransactionEntryVO.class);
		map_table.put("Over-Size Aggregation", BadTransactionEntryVO.class);
		map_table.put("Error-Code Aggregation", BadTransactionEntryVO.class);
		map_table.put("System Resource", ResourceUsageEntryVO.class);
	}
	
	public ResultData(Composite parent, int style) {
		super(parent, style);
		createContents();
		addEventListener();
	}

	public void initDatabase() throws Exception {
		if(db != null) {
			db.close(em);
		}
		this.db = ObjectDBUtil.getInstance();
		this.em = db.createEntityManager();
		this.settingVo = db.select(em, SettingEntryVO.class);
	}

	public void closeDatabase() {
		if(db != null) {
			db.close(em);
		}
		em = null;
		db = null;
	}

	protected void createContents() {
		
		setSize(1244, 690);		
	    setLayout(new FillLayout());
	    
	    sf_main = new SashForm(this, SWT.HORIZONTAL);

	    FormLayout forml_comp_left = new FormLayout();
	    forml_comp_left.marginHeight = 10;
	    forml_comp_left.marginWidth = 20;
	    comp_left = new Composite(sf_main, SWT.NONE);
		comp_left.setLayout(forml_comp_left);

		FillLayout fl_comp_right = new FillLayout(SWT.VERTICAL);
		fl_comp_right.marginWidth = 15;
		fl_comp_right.marginHeight = 15;
	    comp_right = new Composite(sf_main, SWT.NONE);
	    comp_right.setLayout(fl_comp_right);

	    FormData fd_grp_left1 = new FormData();
	    fd_grp_left1.left = new FormAttachment(0);
	    fd_grp_left1.right = new FormAttachment(100);
	    fd_grp_left1.top = new FormAttachment(0, 5);
	    fd_grp_left1.height = 290;
	    FormLayout forml_grp_left1 = new FormLayout();
	    forml_grp_left1.marginHeight = 10;
	    forml_grp_left1.marginWidth = 15;
		grp_left1 = new Group(comp_left, SWT.NONE);
		grp_left1.setLayoutData(fd_grp_left1);
		grp_left1.setLayout(forml_grp_left1);
		grp_left1.setFont(Utility.getFont(SWT.BOLD));
		grp_left1.setText(" TABLE ");
		
		FormData fd_tbl_tables = new FormData();
		fd_tbl_tables.left = new FormAttachment(0);
		fd_tbl_tables.right = new FormAttachment(100);
		fd_tbl_tables.top = new FormAttachment(0);
		fd_tbl_tables.height = 247;
	    tbl_tables = new Table(grp_left1, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.NO_SCROLL);
	    tbl_tables.setLayoutData(fd_tbl_tables);
	    tbl_tables.setFont(Utility.getFont());
		TableColumn tblc_table_name = new TableColumn(tbl_tables, SWT.LEFT);
		tblc_table_name.setWidth(198);
		
		FormData fd_lb_table_cnt = new FormData();
		fd_lb_table_cnt.top = new FormAttachment(tbl_tables, 5);
		fd_lb_table_cnt.left = new FormAttachment(0, 5);
		fd_lb_table_cnt.right = new FormAttachment(100, -5);
		lb_table_cnt = new Label(grp_left1, SWT.LEFT);
		lb_table_cnt.setLayoutData(fd_lb_table_cnt);
		lb_table_cnt.setFont(Utility.getFont(SWT.ITALIC));
		lb_table_cnt.setText("Count : 0");
		lb_table_cnt.setVisible(false);
	    
	    FormData fd_grp_left2 = new FormData();
	    fd_grp_left2.top = new FormAttachment(grp_left1, 10);
	    fd_grp_left2.left = new FormAttachment(0);
	    fd_grp_left2.right = new FormAttachment(100, 0);
	    fd_grp_left2.bottom = new FormAttachment(100, -5);
	    FormLayout forml_grp_left2 = new FormLayout();
	    forml_grp_left2.marginHeight = 10;
	    forml_grp_left2.marginWidth = 15;
		grp_left2 = new Group(comp_left, SWT.NONE);
		grp_left2.setLayoutData(fd_grp_left2);
		grp_left2.setLayout(forml_grp_left2);
		grp_left2.setFont(Utility.getFont(SWT.BOLD));
		grp_left2.setText(" COLUMN ");
		
		FormData fd_tbl_columns = new FormData();
		fd_tbl_columns.left = new FormAttachment(0);
		fd_tbl_columns.right = new FormAttachment(100);
		fd_tbl_columns.top = new FormAttachment(0);
		fd_tbl_columns.bottom = new FormAttachment(100, -25);
	    tbl_columns = new Table(grp_left2, SWT.BORDER | SWT.CHECK | SWT.NO_SCROLL | SWT.V_SCROLL | SWT.HIDE_SELECTION);
	    tbl_columns.setHeaderVisible(true);
	    tbl_columns.setLayoutData(fd_tbl_columns);
	    tbl_columns.setFont(Utility.getFont());
	    TableColumn tblc_column_name = new TableColumn(tbl_columns, SWT.LEFT);
		tblc_column_name.setWidth(198);

		FormData fd_lb_column_cnt = new FormData();
		fd_lb_column_cnt.top = new FormAttachment(tbl_columns, 5);
		fd_lb_column_cnt.left = new FormAttachment(0, 5);
		fd_lb_column_cnt.right = new FormAttachment(100, -5);
		lb_column_cnt = new Label(grp_left2, SWT.LEFT);
		lb_column_cnt.setLayoutData(fd_lb_column_cnt);
		lb_column_cnt.setFont(Utility.getFont(SWT.ITALIC));
		lb_column_cnt.setText("Count : 0 / 0");
		lb_column_cnt.setVisible(false);
		
		FormLayout forml_grp_right = new FormLayout();
		forml_grp_right.marginHeight = 10;
		forml_grp_right.marginWidth = 20;
		grp_right = new Group(comp_right, SWT.NONE);
		grp_right.setLayout(forml_grp_right);
		grp_right.setFont(Utility.getFont(SWT.BOLD));
		grp_right.setText(" DATA ");

		FormData fd_lb_cquery = new FormData();
		fd_lb_cquery.left = new FormAttachment(0, 5);
		fd_lb_cquery.top = new FormAttachment(0, 2);
		lb_cquery = new Label(grp_right, SWT.LEFT);
		lb_cquery.setLayoutData(fd_lb_cquery);
		lb_cquery.setFont(Utility.getFont());
		lb_cquery.setText("Condition Query");
		
		FormData fd_txt_cquery = new FormData();
		fd_txt_cquery.left = new FormAttachment(lb_cquery, 10);
		fd_txt_cquery.right = new FormAttachment(100, -500);
		fd_txt_cquery.top = new FormAttachment(0);
		txt_cquery = new Text(grp_right, SWT.BORDER | SWT.NO_SCROLL | SWT.SINGLE);
		txt_cquery.setLayoutData(fd_txt_cquery);
		txt_cquery.setFont(Utility.getFont());
		txt_cquery.setText("");
		txt_cquery.setEnabled(false);
		txt_cquery.setToolTipText("Insert condition query.\nUse table alias as t.\n\nEX_1)\nt.req_count > 100 and t.err_count = 0\n\nEX_2)\nt.unit_date >= {ts '2018-12-01 00:00:00'}");

		FormData fd_lb_squery = new FormData();
		fd_lb_squery.left = new FormAttachment(txt_cquery, 15);
		fd_lb_squery.top = new FormAttachment(0, 2);
		lb_squery = new Label(grp_right, SWT.LEFT);
		lb_squery.setLayoutData(fd_lb_squery);
		lb_squery.setFont(Utility.getFont());
		lb_squery.setText("Sort Query");
		
		FormData fd_txt_squery = new FormData();
		fd_txt_squery.left = new FormAttachment(lb_squery, 10);
		fd_txt_squery.right = new FormAttachment(100, -200);
		fd_txt_squery.top = new FormAttachment(0);
		txt_squery = new Text(grp_right, SWT.BORDER | SWT.NO_SCROLL | SWT.SINGLE);
		txt_squery.setLayoutData(fd_txt_squery);
		txt_squery.setFont(Utility.getFont());
		txt_squery.setText("");
		txt_squery.setEnabled(false);
		txt_squery.setToolTipText("Insert sort query.\nUse table alias as t.\n\nEX)\nt.request_ip, t.response_date DESC");	    
	    
		FormData fd_btn_execute = new FormData();
		fd_btn_execute.left = new FormAttachment(txt_squery, 10);
		fd_btn_execute.top = new FormAttachment(0, -2);
		fd_btn_execute.width = 70;
	    btn_execute = new Button(grp_right, SWT.NONE);
	    btn_execute.setLayoutData(fd_btn_execute);
	    btn_execute.setFont(Utility.getFont());
	    btn_execute.setText("Execute");
	    btn_execute.setEnabled(false);

		FormData fd_btn_export = new FormData();
		fd_btn_export.right = new FormAttachment(100);
		fd_btn_export.top = new FormAttachment(0, -2);
		fd_btn_export.width = 70;
	    btn_export = new Button(grp_right, SWT.NONE);
	    btn_export.setLayoutData(fd_btn_export); 
	    btn_export.setFont(Utility.getFont());
	    btn_export.setText("Export");
	    btn_export.setEnabled(false);

	    FormData fd_grp_result = new FormData();
	    fd_grp_result.left = new FormAttachment(0);
	    fd_grp_result.right = new FormAttachment(100, 0);
	    fd_grp_result.bottom = new FormAttachment(100);
	    fd_grp_result.height = 15;
		FormLayout forml_grp_result = new FormLayout();
		grp_result = new Group(grp_right, SWT.NONE);
		grp_result.setLayoutData(fd_grp_result);
		grp_result.setLayout(forml_grp_result);
		
	    FormData fd_lb_result_s = new FormData();
	    fd_lb_result_s.left = new FormAttachment(0, 10);
	    fd_lb_result_s.top = new FormAttachment(0, -3);
	    fd_lb_result_s.width = 250;
		lb_result_s = new Label(grp_result, SWT.NONE);
		lb_result_s.setLayoutData(fd_lb_result_s);
		lb_result_s.setFont(Utility.getFont());
		lb_result_s.setText("Successfully executed.");
		lb_result_s.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		lb_result_s.setVisible(false);
		
	    FormData fd_lb_result_f = new FormData();
	    fd_lb_result_f.left = new FormAttachment(0, 10);
	    fd_lb_result_f.top = new FormAttachment(0, -3);
	    fd_lb_result_f.width = 250;
		lb_result_f = new Label(grp_result, SWT.NONE);
		lb_result_f.setLayoutData(fd_lb_result_f);
		lb_result_f.setFont(Utility.getFont());
		lb_result_f.setText("Failed to execute.");
		lb_result_f.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lb_result_f.setVisible(false);
		
	    FormData fd_lb_elapsed = new FormData();
	    fd_lb_elapsed.top = new FormAttachment(0, -3);
	    fd_lb_elapsed.right = new FormAttachment(100, -10);
	    fd_lb_elapsed.width = 150;
		lb_elapsed = new Label(grp_result, SWT.RIGHT);
		lb_elapsed.setLayoutData(fd_lb_elapsed);
		lb_elapsed.setFont(Utility.getFont(SWT.ITALIC));
		lb_elapsed.setText("Elapsed : 0 ms");
		lb_elapsed.setVisible(false);

		FormData fd_lb_rows = new FormData();
		fd_lb_rows.top = new FormAttachment(0, -3);
		fd_lb_rows.right = new FormAttachment(lb_elapsed, -10);
		fd_lb_rows.width = 180;
		lb_rows = new Label(grp_result, SWT.RIGHT);
		lb_rows.setLayoutData(fd_lb_rows);
		lb_rows.setText("Row : 0 / 0");
		lb_rows.setFont(Utility.getFont(SWT.ITALIC));
		lb_rows.setVisible(false);
		
		FormData fd_tbl_data = new FormData();
		fd_tbl_data.left = new FormAttachment(0);
		fd_tbl_data.right = new FormAttachment(100);
		fd_tbl_data.top = new FormAttachment(txt_cquery, 10);
		fd_tbl_data.bottom = new FormAttachment(grp_result, -5);
	    tblv_data = new TableViewer(grp_right, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
	    tbl_data = tblv_data.getTable();
	    tbl_data.setLayoutData(fd_tbl_data);
		tbl_data.setHeaderVisible(true);
		tbl_data.setLinesVisible(true);
		tbl_data.setFont(Utility.getFont());
		cursor_data = new TableCursor(tbl_data, SWT.NONE);
		cursor_data.setFont(Utility.getFont());
		clipboard_data = new Clipboard(this.getDisplay());
	    
		resize(new Rectangle(-1, -1, getSize().x+21, getSize().y));
	    
	}

	protected void addEventListener() {
		tbl_tables.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if(table_idx < 0 || table_idx != tbl_tables.getSelectionIndex()) {
					table_idx = tbl_tables.getSelectionIndex();
					selectTable(tbl_tables.getSelection()[0].getText());
				}
			}
		});

		tbl_columns.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        if(e.detail == SWT.CHECK) {
		        	toggleColumnVisible(tbl_data, ((TableItem)e.item).getText(), ((TableItem)e.item).getChecked());
		        	refreshColumnCount();
		        }
			}
		});
		
		tbl_columns.getColumn(0).addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				TableColumn column = (TableColumn)e.widget;
				boolean checkFlag = !"Uncheck all".equals(column.getText());
				for(TableItem item : tbl_columns.getItems()) {
					if(item.getChecked() != checkFlag) {
						toggleColumnVisible(tbl_data, item.getText(), checkFlag);
						item.setChecked(checkFlag);
					}
				}
				refreshColumnCount();
				column.setText(checkFlag ? "Uncheck all" : "Check all");
			}
		});
		
	    btn_execute.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		sort_query = txt_squery.getText().trim();
	    		loadDataFromDB(tbl_tables.getSelection()[0].getText(), txt_cquery.getText().trim(), sort_query, false, true);
	    	}
	    });

	    btn_export.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
				String msg = "What data do you want to export?";
				String[] labels = new String[] { "All Rows", "Table Rows", "Cancel" };
				int i = MessageUtil.showSelectMessage(getShell(), "Question", msg, labels);
				if(i == 2) {
					return;
				} else {
					String dir;
					if(last_saved_dir == null) {
			    		dir = ".";
						if(AlybaGUI.instance != null) {
							dir = AlybaGUI.getInstance().outputSetting.getOutputDirectory();
						}
					} else {
						dir = last_saved_dir;
					}
					File file = FileDialogUtil.openReadDialogFile(getShell(), Constant.CSV_FILTER_NAMES, Constant.CSV_FILTER_EXTS, dir);
					if(file != null) {
						last_saved_dir = new File(file.getPath()).getParent();
						exportTableDataToCSV(file.getPath(), i==0);
					}
				}
	    	}
	    });

	    final ScrollBar scrollBar = tbl_data.getVerticalBar();
	    scrollBar.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		if(scrollBar.getSelection() + scrollBar.getThumb() == scrollBar.getMaximum()) {
	    			loadDataFromDB(tbl_tables.getSelection()[0].getText(), txt_cquery.getText().trim(), sort_query, true, true);
	    		}
	    	}
	    } );
	    
	    txt_cquery.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            if(e.stateMask == SWT.CTRL && e.keyCode == 'a') {
	            	txt_cquery.selectAll();
	                e.doit = false;
	            } else if(e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
	            	sort_query = txt_squery.getText().trim();
		    		loadDataFromDB(tbl_tables.getSelection()[0].getText(), txt_cquery.getText().trim(), sort_query, false, true);
	            }
	        }
	    });

	    txt_squery.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
	            if(e.stateMask == SWT.CTRL && e.keyCode == 'a') {
	            	txt_squery.selectAll();
	                e.doit = false;
	            } else if(e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
	            	sort_query = txt_squery.getText().trim();
		    		loadDataFromDB(tbl_tables.getSelection()[0].getText(), txt_cquery.getText().trim(), sort_query, false, true);
	            }
	        }
	    });

	    cursor_data.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	TableItem item_cursor = cursor_data.getRow();
	        	tbl_data.setSelection(new TableItem[]{item_cursor});
	        	TableItem[] items = tbl_data.getItems();
	        	if(items != null && items.length > 0 && item_cursor.equals(items[items.length-1])) {
	    			loadDataFromDB(tbl_tables.getSelection()[0].getText(), txt_cquery.getText().trim(), sort_query, true, true);
	        	}
	        }
	    });
		cursor_data.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if(e.stateMask == SWT.CTRL && e.keyCode == 'c') {
					clipboard_data.setContents(new Object[]{cursor_data.getRow().getText(cursor_data.getColumn())}, Constant.TEXT_TRANSFER_TYPE);
				}
			}
		});		
		
	    tbl_data.addListener(SWT.MouseHover, new Listener() {
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				TableItem item = tbl_data.getItem(point);
				if(item != null) {
					int columnIdx = 0;
					for(int i = 0; i < tbl_data.getColumnCount(); i++) {
						if(item.getBounds(i) != null && item.getBounds(i).contains(point)) {
							columnIdx = i;
							break;
						}
					}
					String itemValue = item.getText(columnIdx);
					String column_name = tbl_data.getColumn(columnIdx).getText();
					Object hidden_data = item.getData(column_name);
					if(hidden_data != null && hidden_data instanceof TransactionEntryVO) {
						tbl_data.setToolTipText(((TransactionEntryVO)hidden_data).toPrettyString());
					} else if(itemValue.startsWith("[") && itemValue.endsWith("]")) {
						String listValue = itemValue.substring(1, itemValue.length()-1);
						if(listValue.length() > 0) {
							String[] listArr = listValue.split(",");
							int max = (listArr==null || listArr.length<1) ? 0 : ((listArr.length>5)?5:listArr.length);
							StringBuffer buffer = new StringBuffer("");
							for(int i = 0; i < max; i++) {
								buffer.append(listArr[i].trim()).append("\n");
							}
							if(listArr.length > 5) {
								buffer.append("\n... ").append((listArr.length-5)).append(" more");
							}
							tbl_data.setToolTipText(buffer.toString());
						} else {
							tbl_data.setToolTipText("");
						}
					} else {
						tbl_data.setToolTipText(itemValue);
					}					
				}				
			}
		});
	}

	public void load() throws Exception {
		initDatabase();
		loadTables();
	}

	private void selectTable(String table_name) {
		txt_cquery.setEnabled(true);
		txt_cquery.setText("");
		txt_squery.setEnabled(true);
		txt_squery.setText("");
		btn_execute.setEnabled(true);
		loadColumns(table_name);
		sort_query = "";
		loadDataFromDB(table_name, null, sort_query, false, true);
	}
	
	private void loadTables() throws Exception {
		for(String name : map_table.keySet()) {			
			String type = getDataTypeByName(name);
			String condition = "";
			if(type != null) {
				condition = "WHERE o.type = '" + type + "'";
			}
			String count_query = "SELECT COUNT(o) FROM " + map_table.get(name).getName() + " AS o " + condition;			
			long count = db.select(em, count_query, Long.class, null);
			if(count > 0) {
				TableItem item = new TableItem(tbl_tables, SWT.NONE);
				item.setText(0, name);
			}
		}
		lb_table_cnt.setText("Count : " + map_table.size());
		lb_table_cnt.setVisible(true);
	}
	
	private void loadColumns(String table_name) {
		tbl_columns.removeAll();
		tbl_data.removeAll();
		while(tbl_data.getColumnCount() > 0) {
			tbl_data.getColumns()[0].dispose();
		}		
	    tbl_columns.getColumns()[0].setText("Uncheck all");
	    int columnIdx = 0;
		List<Field> fields = ReflectionUtil.getFields(map_table.get(table_name));
		for(Field f : fields) {
			if(!Modifier.isStatic(f.getModifiers()) && !"type".equals(f.getName())) {
				TableItem tbli = new TableItem(tbl_columns, SWT.NONE);
				tbli.setText(f.getName());
				tbli.setChecked(true);
				int align = (ReflectionUtil.isNumberType(f)||f.getType()==TransactionEntryVO.class) ? SWT.RIGHT : SWT.NONE;
				TableColumn tblc = new TableColumn(tbl_data, align);
				tblc.setText(f.getName());
				tblc.setMoveable(true);
				if(columnIdx == 0 || tblc.getText().endsWith("_date")) {
					tblc.setWidth(130);
				} else if(tblc.getText().equals("request_uri")) {
					tblc.setWidth(120);
				} else if(tblc.getText().equals("request_ip_list")) {
					tblc.setWidth(100);
				} else if(tblc.getText().equals("description")) {
					tblc.setWidth(150);
				} else if(tblc.getText().equals("name")) {
					tblc.setWidth(100);
				} else if(tblc.getText().equals("group")) {
					tblc.setWidth(100);
				} else {
					tblc.pack();
				}
				columnIdx++;
			}
		}
		addColumnListener();
		refreshColumnCount();
		tbl_data.setSortDirection(SWT.NONE);
	}
	
	private void refreshColumnCount() {
    	TableItem[] items = tbl_columns.getItems();
    	int cnt = 0;
    	for(TableItem item : items) {
    		if(item.getChecked()) {
    			cnt++;
    		}
    	}
		lb_column_cnt.setText("Count : " + cnt + " / " + items.length);
		lb_column_cnt.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	private <E extends EntryVO> List<E> getDataFromDB(String table_name, String condition_query, String sort_query, int start_row, int max_rows) throws Exception {
		String type = getDataTypeByName(table_name);
		Class<E> tbl_class = (Class<E>)map_table.get(table_name);
		condition_query = (condition_query==null) ? "" : condition_query;
		if(type != null) {
			condition_query = ("t.type = '" + type + "'") + (condition_query.length()>0?" AND ":"") + condition_query;
		}
		if(condition_query.length() > 0) {
			condition_query = "WHERE " + condition_query;
		}
		sort_query = (sort_query==null) ? "" : sort_query;
		if(sort_query.length() > 0) {
			sort_query = "ORDER BY " + sort_query;
		}
		String full_query = "SELECT t FROM " + tbl_class.getSimpleName() + " AS t " + condition_query + " " + sort_query;
		TypedQuery<E> tquery = (TypedQuery<E>)em.createQuery(full_query, tbl_class).setFirstResult(start_row);
		if(max_rows > 0) {
			tquery.setMaxResults(max_rows);
		}
		return tquery.getResultList();
	}

		
	@SuppressWarnings("unchecked")
	private <E extends EntryVO> void loadDataFromDB(String table_name, String condition_query, String sort_query, boolean append, boolean paging) {
		lb_result_f.setToolTipText(null);
		lb_result_s.setVisible(false);
		lb_result_f.setVisible(false);
		lb_rows.setVisible(false);
		lb_elapsed.setVisible(false);
		
		String type = getDataTypeByName(table_name);
		Class<E> tbl_class = (Class<E>)map_table.get(table_name);
		try {
			if(!append) {
				start_row = 0;
			}
			long start_tm = System.currentTimeMillis();
			condition_query = replaceColumnNames(condition_query);
			sort_query = replaceColumnNames(sort_query);
			List<E> results = getDataFromDB(table_name, condition_query, sort_query, start_row, (paging?Constant.ANALYZER_DATA_PAGESIZE:0));
			start_row += results.size();
			bindDataToTable(results, append);
			long elapsed_tm = System.currentTimeMillis() - start_tm;
			condition_query = (condition_query==null) ? "" : condition_query;
			if(type != null) {
				condition_query = ("t.type = '" + type + "'") + (condition_query.length()>0?" AND ":"") + condition_query;
			}
			if(condition_query.length() > 0) {
				condition_query = "WHERE " + condition_query;
			}
			if(!append) {
				String count_query = "SELECT COUNT(t) FROM " + tbl_class.getSimpleName() + " AS t " + condition_query;
				total_rows = db.select(em, count_query, Long.class, null);
			}
			if(sort_query == null || sort_query.length() <= 0) {
				tbl_data.setSortDirection(SWT.NONE);
			} else {
				String sort_fld = sort_query.trim().split(" ")[0];
				for(int i = 0; i < tbl_data.getColumnCount(); i++) {
					TableColumn col = tbl_data.getColumn(i);
					if(sort_fld.equals(col.getText())) {
						tbl_data.setSortDirection(i);
						break;
					}
				}
			}
			uncheckUselessColumn(table_name);
			lb_rows.setText("Row : " + tbl_data.getItemCount() + " / " + total_rows);
			lb_rows.setVisible(true);
			lb_elapsed.setText("Elapsed : " + elapsed_tm + " ms");
			lb_elapsed.setVisible(true);
			lb_result_s.setVisible(true);
		} catch(Exception e) {
			Logger.debug("Failed to load data from DB : " + type);
			Logger.error(e);
			lb_result_f.setVisible(true);
			lb_result_f.setToolTipText(e.getMessage());
			tbl_data.removeAll();
		}
	}
	
	private void uncheckUselessColumn(String table_name) {
		if(tbl_data.getItemCount() < 1) return;
		for(int idx = 0; idx < tbl_columns.getItemCount(); idx++) {
			TableItem item = tbl_columns.getItem(idx);
			TableItem firstRow = tbl_data.getItem(0);
			if((item.getText().equals("request_date") && firstRow.getText(idx).isEmpty()) ||
			   (item.getText().equals("response_date") && firstRow.getText(idx).isEmpty()) ||
			   (item.getText().equals("request_uri") && !LogFieldMappingInfo.isMappedURI(settingVo.getLogMappingInfo())) ||
			   (item.getText().equals("request_ip") && !LogFieldMappingInfo.isMappedIP(settingVo.getLogMappingInfo())) ||
			   (item.getText().indexOf("response_time") > -1 && !LogFieldMappingInfo.isMappedElapsed(settingVo.getLogMappingInfo())) ||
			   (item.getText().indexOf("response_byte") > -1 && !LogFieldMappingInfo.isMappedBytes(settingVo.getLogMappingInfo())) ||
			   (item.getText().equals("response_code") && !LogFieldMappingInfo.isMappedCode(settingVo.getLogMappingInfo())) ||
			   (item.getText().equals("request_method") && !LogFieldMappingInfo.isMappedMethod(settingVo.getLogMappingInfo())) ||
			   (item.getText().equals("request_version") && !LogFieldMappingInfo.isMappedVersion(settingVo.getLogMappingInfo())) ||
			   (item.getText().equals("request_ext") && !LogFieldMappingInfo.isMappedURI(settingVo.getLogMappingInfo())) ||
			   (item.getText().equals("description") && (!table_name.equals("IP Aggregation") && !table_name.equals("CODE Aggregation"))) ||
			   (item.getText().equals("request_ip_count") && (table_name.equals("IP Aggregation") || !LogFieldMappingInfo.isMappedIP(settingVo.getLogMappingInfo()))) ||
			   (item.getText().equals("request_ip_list")) ||
			   (item.getText().indexOf("total") > -1) ||
			   (item.getText().indexOf("error") > -1 && !LogFieldMappingInfo.isMappedCode(settingVo.getLogMappingInfo())) ||
			   (item.getText().indexOf("err_") > -1 && !settingVo.isCollectErrors()) ||
			   (item.getText().indexOf("filter") > -1 && (!settingVo.isDateFilterEnable() && !settingVo.isIncludeFilterEnable() && !settingVo.isExcludeFilterEnable()))) {
				item.setChecked(false);
				toggleColumnVisible(tbl_data, item.getText(), false);
			}
		}		
	}

	private <E extends EntryVO> void bindDataToTable(List<E> results, boolean append) throws Exception {
		List<Field> fields = null;
		if(!append) {
			tbl_data.removeAll();
		}
		for(int i = 0; i < results.size(); i++) {
			E entry = results.get(i);
			int columnIdx = 0;
			TableItem item = new TableItem(tbl_data, SWT.NONE);
			if(fields == null) {
				fields = ReflectionUtil.getFields(entry.getClass());
			}
			for(Field f : fields) {
				if(!Modifier.isStatic(f.getModifiers()) && !"type".equals(f.getName())) {
					f.setAccessible(true);
					Object value = f.get(entry);
					item.setText(columnIdx++, objectToStringByColumn(f.getName(), value));
					if(value instanceof TransactionEntryVO) {
						item.setData(f.getName(), value);
					}
				}
			}
		}
		if(results.size() > 0) {
			tbl_data.setFocus();
			if(!append) {
				tbl_data.setSelection(-1);
				cursor_data.setSelection(0, 0);
			}
		}
		btn_export.setEnabled(tbl_data.getItemCount() > 0);
	}
	
	private void addColumnListener() {
		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				TableColumn column = (TableColumn)e.widget;
				if(tbl_data.getSortColumn() != null && column.equals(tbl_data.getSortColumn())) {
					tbl_data.setSortDirection(tbl_data.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
				} else {
					tbl_data.setSortColumn(column);
					tbl_data.setSortDirection(SWT.DOWN);
				}
				String sort_column = column.getText();
				sort_query = "t." + sort_column + (tbl_data.getSortDirection() == SWT.DOWN ? " DESC" : "");
				txt_squery.setText(sort_query);
				loadDataFromDB(tbl_tables.getSelection()[0].getText(), txt_cquery.getText().trim(), sort_query, false, true);
			}
		};
		for(TableColumn column : tbl_data.getColumns()) {
			column.addListener(SWT.Selection, sortListener);
		}
	}
	
	private void toggleColumnVisible(Table tbl, String column_name, boolean flag) {
		for(TableColumn column : tbl.getColumns()) {
			if(column_name.equals(column.getText())) {
				if(flag) {
					column.setWidth((Integer)column.getData());
					column.setData(null);
				} else {
					column.setData(new Integer(column.getWidth()));
					column.setWidth(0);
				}
				break;
			}
		}
	}
	
	public void resize(Rectangle rect) {
		int width = rect.width - 24;
	    sf_main.setWeights(new int[] {280, width-280});
	}
	
	public void resetData() {
		tbl_tables.removeAll();
		tbl_columns.removeAll();
		tbl_data.removeAll();
		while(tbl_data.getColumnCount() > 0) {
			tbl_data.getColumns()[0].dispose();
		}
		txt_cquery.setEnabled(false);
		txt_cquery.setText("");
		txt_squery.setEnabled(false);
		txt_squery.setText("");		
		btn_execute.setEnabled(false);
		btn_export.setEnabled(false);
		lb_table_cnt.setVisible(false);
		lb_column_cnt.setVisible(false);
		lb_result_s.setVisible(false);
		lb_result_f.setVisible(false);
		lb_rows.setVisible(false);
		lb_elapsed.setVisible(false);
		table_idx = -1;
	}

	private String objectToStringByColumn(String column, Object obj) {
		if(obj instanceof TransactionEntryVO) {
			TransactionEntryVO vo = (TransactionEntryVO)obj;
			if("max_response_time".equals(column)) {
				return objectToString(vo.getResponseTime());
			} else if("max_response_byte".equals(column)) {
				return objectToString(vo.getResponseBytes());
			} else if("last_error".equals(column)) {
				return objectToString(vo.getResponseCode());
			}
		}
		return objectToString(obj);
	}

	@SuppressWarnings("rawtypes")
	private String objectToString(Object obj) {
		if(obj == null) {
			return "";
		} else if(obj instanceof Collection && ((Collection)obj).size() < 1) {
			return "";
		} else if(obj instanceof Date) {
			return DateUtil.dateToString((Date)obj, DateUtil.SDF_DATETIME);
		} else if(obj instanceof Float) {
			return (((Float)obj).isNaN() ? "NaN" : NumberUtil.DF_PERCENT_DOT3.format(obj));
		} else if(obj instanceof Double) {
			return (((Double)obj).isNaN() ? "NaN" : NumberUtil.DF_NO_DOT.format(obj));
		} else {
			return obj.toString();
		}
	}
	
	private <E extends EntryVO> void exportTableDataToCSV(final String filename, boolean all) {
		try {
			CSVWriter csvWriter = new CSVWriter(filename);
			List<Integer> visibleIdx = new ArrayList<Integer>();
			List<String> visibleCol = new ArrayList<String>();
			List<String> header = new ArrayList<String>();
			List<List<?>> data = new ArrayList<List<?>>();
			int[] dataColIdx = tbl_data.getColumnOrder(); 
			for(int i = 0; i < tbl_columns.getItemCount(); i++) {
				if(tbl_columns.getItem(i).getChecked()) {
					int realIdx = dataColIdx[i];
					visibleIdx.add(realIdx);
					visibleCol.add(tbl_data.getColumn(realIdx).getText());
					header.add(tbl_data.getColumn(realIdx).getText());
				}
			}
			if(visibleIdx.size() < 1 || tbl_data.getItemCount() < 1) {
				throw new Exception("No data to export.");
			}
			if(all) {
				List<E> results = getDataFromDB(tbl_tables.getSelection()[0].getText(), txt_cquery.getText().trim(), sort_query, 0, -1);
				List<Field> visibleFields = null;
				for(int i = 0; i < results.size(); i++) {
					E entry = results.get(i);
					if(visibleFields == null) {
						visibleFields = new ArrayList<Field>();
						List<Field> fields = ReflectionUtil.getFields(entry.getClass());
						for(Field f : fields) {
							for(String col : visibleCol) {
								if(col.equals(f.getName())) {
									f.setAccessible(true);
									visibleFields.add(f);
									break;
								}
							}
						}
					}
					List<String> row = new ArrayList<String>();
					for(Field f : visibleFields) {
						row.add(objectToStringByColumn(f.getName(), f.get(entry)));
					}
					data.add(row);
				}
			} else {
				for(TableItem item : tbl_data.getItems()) {
					List<String> row = new ArrayList<String>();
					for(int idx : visibleIdx) {
						row.add(item.getText(idx));				
					}
					data.add(row);
				}
			}
			csvWriter.setHeader(header);		
			csvWriter.setData(data);
			csvWriter.write();
			
			String msg = "Generated a CSV file. What do you want to do?";
			String[] labels = new String[] { "Open directory", "Open files", "Close" };
			int i = MessageUtil.showSelectMessage(getShell(), "Question", msg, labels);
			switch(i) {
				case 0:
					Program.launch(new File(filename).getParent());
					break;
				case 1:
			        getDisplay().asyncExec(new Runnable() {
						public void run() {
							Program.launch((new File(filename)).getAbsolutePath());
						}
					});
					break;
			}
		} catch(Exception e) {
			Logger.debug("Failed to export data to file : " + filename);
			Logger.error(e);
			MessageUtil.showErrorMessage(getShell(), "Failed to export data to file.");
		}
	}
	
	private String getDataTypeByName(String name) {
		if(name == null) {
			return null;
		} else if(name.equals("Daily Transaction")) {
			return "DAY";
		} else if(name.equals("Houly Transaction")) {
			return "HOUR";
		} else if(name.equals("URI Aggregation")) {
			return "URI";
		} else if(name.equals("EXT Aggregation")) {
			return "EXT";
		} else if(name.equals("IP Aggregation")) {
			return "IP";
		} else if(name.equals("METHOD Aggregation")) {
			return "METHOD";
		} else if(name.equals("VERSION Aggregation")) {
			return "VERSION";
		} else if(name.equals("CODE Aggregation")) {
			return "CODE";
		} else if(name.equals("Over-Time Aggregation")) {
			return "TIME";
		} else if(name.equals("Over-Size Aggregation")) {
			return "SIZE";
		} else if(name.equals("Error-Code Aggregation")) {
			return "CODE";
		}
		return null;
	}
	
	private String replaceColumnNames(String str) {
		if(str == null) {
			return null;
		}
		Pattern p = Pattern.compile("(max_\\w+|last_error)");
		Matcher m = p.matcher(str);
		StringBuffer sb = new StringBuffer();
		int idx = 0;
		while(m.find()) {
			sb.append(str.substring(idx, m.start()));
			String text = m.group(1);
			if(text.startsWith("max_")) {
				text += "." + text.substring(4);
			} else if(text.startsWith("last_error")) {
				text += ".response_code"; 
			}
			sb.append(text);
			idx = m.end();
		}
		sb.append(str.substring(idx));		
		return sb.toString();
	}
	
}
