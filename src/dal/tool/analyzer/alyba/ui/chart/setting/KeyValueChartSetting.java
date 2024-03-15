package dal.tool.analyzer.alyba.ui.chart.setting;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.ui.chart.Chart;
import dal.tool.analyzer.alyba.ui.chart.DistributionChart;
import dal.tool.analyzer.alyba.ui.chart.KeyValueChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.IpChart;
import dal.tool.analyzer.alyba.ui.chart.keyvalue.UriChart;
import dal.tool.analyzer.alyba.ui.comp.ResultChart;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.StringUtil;

public class KeyValueChartSetting extends ChartSetting {

	private static final DecimalFormat DF = new DecimalFormat("###,###");

	private Group grp_setting;
	private Button chk_merge;
	private Button chk_item_label;
	private Button chk_merge_methods;
	private Button chk_location;
	private Button btn_boudary_def;
	private Button btn_apply;
	private Spinner sp_merge_max;
	private Spinner sp_merge_pct;
	private Label lb_merge_max;
	private Label lb_merge_item;
	private Label lb_merge_less;
	private Label lb_merge_pct;
	private Table tbl_boundary_values;
	private TableEditor tbl_editor;

	public KeyValueChartSetting(Composite parent, ResultChart result_chart) {
		super(parent, result_chart);
		createContents();
		addEventListener();
	}

	protected void createContents() {
		setLayout(new FillLayout(SWT.HORIZONTAL));

	    FormLayout forml_grp_setting = new FormLayout();
	    forml_grp_setting.marginHeight = 15;
	    forml_grp_setting.marginWidth = 15;
		grp_setting = new Group(this, SWT.NONE);
		grp_setting.setLayout(forml_grp_setting);

		FormData fd_chk_merge = new FormData();
		fd_chk_merge.left = new FormAttachment(grp_setting, 0, SWT.LEFT);
		fd_chk_merge.top = new FormAttachment(grp_setting, 0);
		chk_merge = new Button(grp_setting, SWT.CHECK);
		chk_merge.setLayoutData(fd_chk_merge);
		chk_merge.setFont(Utility.getFont());
		chk_merge.setText(" Merge as \"Others\"");
		chk_merge.setSelection(true);
		
		FormData fd_lb_merge_max = new FormData();
		fd_lb_merge_max.left = new FormAttachment(chk_merge, 20, SWT.LEFT);
		fd_lb_merge_max.top = new FormAttachment(chk_merge, 10);
		lb_merge_max = new Label(grp_setting, SWT.NONE);
		lb_merge_max.setLayoutData(fd_lb_merge_max);
		lb_merge_max.setAlignment(SWT.RIGHT);
		lb_merge_max.setFont(Utility.getFont());
		lb_merge_max.setText("max");

		FormData fd_sp_merge_max = new FormData();
		fd_sp_merge_max.top = new FormAttachment(lb_merge_max, -2, SWT.TOP);
		fd_sp_merge_max.left = new FormAttachment(lb_merge_max, 6);
		fd_sp_merge_max.width = 25;
		fd_sp_merge_max.height = 16;
		sp_merge_max = new Spinner(grp_setting, SWT.BORDER);
		sp_merge_max.setLayoutData(fd_sp_merge_max);
		sp_merge_max.setFont(Utility.getFont());
		sp_merge_max.setTextLimit(3);
		sp_merge_max.setMaximum(999);
		sp_merge_max.setMinimum(1);
		sp_merge_max.setSelection(20);
		
		FormData fd_lb_merge_item = new FormData();
		fd_lb_merge_item.left = new FormAttachment(sp_merge_max, 6);
		fd_lb_merge_item.top = new FormAttachment(chk_merge, 10);
		lb_merge_item = new Label(grp_setting, SWT.NONE);
		lb_merge_item.setLayoutData(fd_lb_merge_item);
		lb_merge_item.setFont(Utility.getFont());
		lb_merge_item.setText("items");	
		
		FormData fd_lb_merge_less = new FormData();
		fd_lb_merge_less.top = new FormAttachment(sp_merge_max, 12);
		fd_lb_merge_less.left = new FormAttachment(chk_merge, 20, SWT.LEFT);
		lb_merge_less = new Label(grp_setting, SWT.NONE);
		lb_merge_less.setLayoutData(fd_lb_merge_less);
		lb_merge_less.setAlignment(SWT.RIGHT);
		lb_merge_less.setFont(Utility.getFont());
		lb_merge_less.setText("less than");

		FormData fd_sp_merge_pct = new FormData();
		fd_sp_merge_pct.top = new FormAttachment(lb_merge_less, -2, SWT.TOP);
		fd_sp_merge_pct.left = new FormAttachment(lb_merge_less, 6);
		fd_sp_merge_pct.width = 25;
		fd_sp_merge_pct.height = 16;
		sp_merge_pct = new Spinner(grp_setting, SWT.BORDER);
		sp_merge_pct.setLayoutData(fd_sp_merge_pct);
		sp_merge_pct.setFont(Utility.getFont());
		sp_merge_pct.setTextLimit(4);
		sp_merge_pct.setDigits(1);
		sp_merge_pct.setMinimum(0);
		sp_merge_pct.setMaximum(999);
		sp_merge_pct.setIncrement(10);
		sp_merge_pct.setSelection(10);
		
		FormData fd_lb_merge_pct = new FormData();
		fd_lb_merge_pct.top = new FormAttachment(sp_merge_max, 12);
		fd_lb_merge_pct.left = new FormAttachment(sp_merge_pct, 6);
		lb_merge_pct = new Label(grp_setting, SWT.NONE);
		lb_merge_pct.setLayoutData(fd_lb_merge_pct);
		lb_merge_pct.setFont(Utility.getFont());
		lb_merge_pct.setText("%");

		FormData fd_chk_item_label = new FormData();
		fd_chk_item_label.top = new FormAttachment(sp_merge_pct, 30);
		fd_chk_item_label.left = new FormAttachment(chk_merge, 0, SWT.LEFT);
		chk_item_label = new Button(grp_setting, SWT.CHECK);
		chk_item_label.setLayoutData(fd_chk_item_label);
		chk_item_label.setFont(Utility.getFont());
		chk_item_label.setText(" Item label");


		FormData fd_chk_merge_methods = new FormData();
		fd_chk_merge_methods.top = new FormAttachment(chk_item_label, 26);
		fd_chk_merge_methods.left = new FormAttachment(chk_merge, 0, SWT.LEFT);
		chk_merge_methods = new Button(grp_setting, SWT.CHECK);
		chk_merge_methods.setLayoutData(fd_chk_merge_methods);
		chk_merge_methods.setFont(Utility.getFont());
		chk_merge_methods.setText(" Merge Methods");
		chk_merge_methods.setVisible(false);
		
		FormData fd_chk_location = new FormData();
		fd_chk_location.top = new FormAttachment(chk_item_label, 26);
		fd_chk_location.left = new FormAttachment(chk_merge, 0, SWT.LEFT);
		chk_location = new Button(grp_setting, SWT.CHECK);
		chk_location.setLayoutData(fd_chk_location);
		chk_location.setFont(Utility.getFont());
		chk_location.setText(" by IP Location");
		chk_location.setVisible(false);

		FormData fd_boundary = new FormData();
		fd_boundary.width = 123;
		fd_boundary.height = 140;
		fd_boundary.top = new FormAttachment(chk_item_label, 26);
		fd_boundary.left = new FormAttachment(chk_merge, 0, SWT.LEFT);		
		tbl_boundary_values = new Table(grp_setting, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tbl_boundary_values.setLayoutData(fd_boundary);
		tbl_boundary_values.setFont(Utility.getFont());
		tbl_boundary_values.setHeaderVisible(true);
		tbl_boundary_values.setLinesVisible(true);
		tbl_boundary_values.getHorizontalBar().setVisible(false);
		tbl_boundary_values.setVisible(false);
		TableColumn tblc_hidden = new TableColumn(tbl_boundary_values, SWT.NONE);
		tblc_hidden.setWidth(0);
		TableColumn tblc_value = new TableColumn(tbl_boundary_values, SWT.RIGHT);
		tblc_value.setText("Boundary Values");
		tblc_value.setResizable(false);
		tblc_value.setWidth(120);
		tbl_editor = new TableEditor(tbl_boundary_values);
		tbl_editor.horizontalAlignment = SWT.RIGHT;
		tbl_editor.grabHorizontal = true;
		
		FormData fd_btn_boudary_def = new FormData();
		fd_btn_boudary_def.top = new FormAttachment(tbl_boundary_values, 8);
		fd_btn_boudary_def.right = new FormAttachment(tbl_boundary_values, 0, SWT.RIGHT);		
		fd_btn_boudary_def.width = 80;
		btn_boudary_def = new Button(grp_setting, SWT.NONE);
		btn_boudary_def.setLayoutData(fd_btn_boudary_def);
		btn_boudary_def.setFont(Utility.getFont());
		btn_boudary_def.setText("Default");
		btn_boudary_def.setVisible(false);

		FormData fd_btn_apply = new FormData();
		fd_btn_apply.right = new FormAttachment(100);
		fd_btn_apply.bottom = new FormAttachment(100);
		fd_btn_apply.width = 80;
		btn_apply = new Button(grp_setting, SWT.NONE);
		btn_apply.setLayoutData(fd_btn_apply);
		btn_apply.setFont(Utility.getFont());
		btn_apply.setText("Apply");
		
		grp_setting.setTabList(new Control[]{ chk_merge, sp_merge_max, sp_merge_pct, chk_item_label, btn_apply });

	}

	protected void addEventListener() {

		btn_apply.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		result_chart.clickApplyButton();
	    	}
	    });

		chk_merge.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		sp_merge_max.setEnabled(chk_merge.getSelection());
	    		sp_merge_pct.setEnabled(chk_merge.getSelection());
	    	}
	    });
		
		tbl_boundary_values.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Control oldEditor = tbl_editor.getEditor();
				if(oldEditor != null) {
					oldEditor.dispose();
				}
				TableItem item = (TableItem)e.item;
				if(item == null) {
					return;
				}

				final Text newEditor = new Text(tbl_boundary_values, SWT.RIGHT);
				newEditor.setText(item.getData()==null ? "" : toStringForBoundaryValue((Double)item.getData(), true));
				newEditor.setFont(tbl_boundary_values.getFont());
				newEditor.selectAll();
				newEditor.setFocus();

				Listener textListener = new Listener() {
					public void handleEvent(Event e) {
						if(e.type == SWT.FocusOut) {
							checkBoundaryValueAndSet(newEditor.getText(), tbl_editor.getItem());
							newEditor.dispose();
							setBoundaryValueTable(getBoundaryValues());
						} else if(e.type == SWT.Traverse) {
							if(e.detail == SWT.TRAVERSE_RETURN) {
								checkBoundaryValueAndSet(newEditor.getText(), tbl_editor.getItem());
								newEditor.dispose();
								setBoundaryValueTable(getBoundaryValues());
							} else if(e.detail == SWT.TRAVERSE_ESCAPE) {
								TableItem item = tbl_editor.getItem();
								item.setText(1, (item.getData()==null ? "" : String.valueOf((Double)item.getData())));
								newEditor.dispose();
								e.doit = false;
							}
						}
					}
				};		
				newEditor.addListener(SWT.FocusOut, textListener);
				newEditor.addListener(SWT.Traverse, textListener);
				tbl_editor.minimumHeight = tbl_boundary_values.getItemHeight();
				tbl_editor.minimumWidth = tbl_boundary_values.getColumn(1).getWidth();				
				tbl_editor.setEditor(newEditor, item, 1);
			}
		});

		btn_boudary_def.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		setBoundaryValueTable(((DistributionChart)result_chart.getCurrentChart()).getDefaultBoundaryValues());
	    	}
	    });

	}
	
	protected void checkBoundaryValueAndSet(String value, TableItem item) {
		value = value.trim();
		if(tbl_boundary_values.getItemCount() == 2 && "".equals(value.trim())) {
			item.setText(1, String.valueOf((Double)item.getData()));
			Logger.debug("The last item can not be deleted.");
		} else if(!"".equals(value.trim()) && !StringUtil.isNumeric(value, true)) {
			item.setText(1, String.valueOf((Double)item.getData()));
			Logger.debug("Not a number : " + value);
		} else {
			if("".equals(value)) {
				item.setData(null);
				item.setText("");
			} else {
				Double data = Double.valueOf(value);
				if(String.valueOf(data).indexOf('E') > -1) {
					item.setText(1, String.valueOf((Double)item.getData()));
					Logger.debug("Greater than maximum(9,999,999) number : " + value);
				} else {
					item.setData(data);
					item.setText(1, toStringForBoundaryValue(data, false));
				}
			}
		}
	}
	
	public void init() {
	}

	public void reset(Chart chart) {
		if(chart != null) {
			if(chart instanceof DistributionChart) {
				DistributionChart distChart = (DistributionChart)chart;
				chk_merge.setSelection(false);
				chk_merge.setEnabled(false);
				sp_merge_max.setEnabled(false);
				sp_merge_pct.setEnabled(false);
				chk_item_label.setSelection(distChart.getShowItemLabel());
				chk_merge_methods.setVisible(false);
				chk_location.setVisible(false);
				tbl_boundary_values.setVisible(true);
				btn_boudary_def.setVisible(true);
				setBoundaryValueTable(distChart.getBoundaryValues());
			} else {
				KeyValueChart kvChart = (KeyValueChart)chart;
				chk_merge.setSelection(kvChart.getMergeToOthers());
				chk_merge.setEnabled(true);
				sp_merge_max.setSelection(kvChart.getMaxItemCount());
				sp_merge_max.setEnabled(kvChart.getMergeToOthers());
				sp_merge_pct.setSelection((int)(kvChart.getMinItemPercent()*Math.pow(10, sp_merge_pct.getDigits())));
				sp_merge_pct.setEnabled(kvChart.getMergeToOthers());
				chk_item_label.setSelection(kvChart.getShowItemLabel());
				tbl_boundary_values.setVisible(false);
				btn_boudary_def.setVisible(false);
				chk_merge_methods.setVisible(false);
				chk_location.setVisible(false);
				if(chart instanceof IpChart) {
					IpChart ipChart = (IpChart)chart;
					chk_location.setSelection(ipChart.getShowByLocation());
					chk_location.setVisible(true);
				} else if(chart instanceof UriChart) {
					UriChart uriChart = (UriChart)chart;
					chk_merge_methods.setSelection(uriChart.getMergeMethods());
					chk_merge_methods.setVisible(true);
				}
			}
		} else {
			chk_merge.setSelection(true);
			chk_merge.setEnabled(true);
			sp_merge_max.setSelection(20);
			sp_merge_max.setEnabled(true);
			sp_merge_pct.setSelection(10);
			sp_merge_pct.setEnabled(true);
			chk_item_label.setSelection(true);
			chk_merge_methods.setSelection(true);
			chk_merge_methods.setVisible(false);
			chk_location.setSelection(true);
			chk_location.setVisible(false);
			tbl_boundary_values.removeAll();
			tbl_boundary_values.setVisible(false);
			btn_boudary_def.setVisible(false);
		}		
	}
	
	protected Double[] getBoundaryValues() {
		if(tbl_boundary_values.getItemCount() == 0) {
			return null;
		}
	    Set<Double> sortedSet = new TreeSet<Double>();
		for(TableItem item : tbl_boundary_values.getItems()) {
			if(item.getData() != null) {
				sortedSet.add((Double)item.getData());
			}
		}
		Double[] sortedBoundaryValues = new Double[sortedSet.size()];
		return sortedSet.toArray(sortedBoundaryValues);
	}
	
	protected void setBoundaryValueTable(Double[] boundary_values) {
		tbl_boundary_values.removeAll();
		for(Double value : boundary_values) {
			TableItem item = new TableItem(tbl_boundary_values, SWT.NONE);
			item.setData(value);
			item.setText(1, toStringForBoundaryValue(value, false));
		}
		TableItem item = new TableItem(tbl_boundary_values, SWT.NONE);
		item.setText(1, "");
	}

	private String toStringForBoundaryValue(Double doubleValue, boolean forEditor) {
		String strValue = String.valueOf(doubleValue);
		boolean hasFloat = !strValue.endsWith(".0"); 
		if(forEditor) {
			return !hasFloat ? strValue.substring(0, strValue.length()-2) : strValue;
		} else {
			if(hasFloat) {
				int idx = strValue.indexOf('.');
				String left = strValue.substring(0, idx);
				String right = strValue.substring(idx+1);
				Long longValue = Long.valueOf(left);
				return DF.format(longValue) + "." + right;
			} else {
				Long longValue = (long)(double)doubleValue;
				return DF.format(longValue);
			}
		}
	}

	public void configure(Chart chart) {
		if(chart instanceof DistributionChart) {
			DistributionChart dist_chart = (DistributionChart)chart;
			dist_chart.setMergeToOthers(false);
			dist_chart.setMaxItemCount(sp_merge_max.getSelection());
			dist_chart.setMinItemPercent((float)(sp_merge_pct.getSelection()/Math.pow(10, sp_merge_pct.getDigits())));
			dist_chart.setShowItemLabel(chk_item_label.getSelection());
			Double[] boundary_values = getBoundaryValues();
			if(boundary_values != null) {
				dist_chart.setBoundaryValues(boundary_values);
			}
		} else {
			KeyValueChart kv_chart = (KeyValueChart)chart;
			kv_chart.setMergeToOthers(chk_merge.getSelection());
			kv_chart.setMaxItemCount(sp_merge_max.getSelection());
			kv_chart.setMinItemPercent((float)(sp_merge_pct.getSelection()/Math.pow(10, sp_merge_pct.getDigits())));
			kv_chart.setShowItemLabel(chk_item_label.getSelection());
			if(chart instanceof IpChart) {
				IpChart ip_chart = (IpChart)chart;
				ip_chart.setShowByLocation(chk_location.getSelection());
			}  else if(chart instanceof UriChart) {
				UriChart uriChart = (UriChart)chart;
				uriChart.setMergeMethods(chk_merge_methods.getSelection());
			} 
		}
	}
}
