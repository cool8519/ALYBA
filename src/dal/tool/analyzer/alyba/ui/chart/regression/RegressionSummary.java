package dal.tool.analyzer.alyba.ui.chart.regression;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.ui.Logger;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.StringUtil;

public class RegressionSummary extends Composite {

	private static final DecimalFormat DF_INT = new DecimalFormat("###,###");
	private static final DecimalFormat DF_FLOAT = new DecimalFormat("#,##0.#");

    private ScrolledComposite sc_summary;
    private Composite comp_contents;
	private Group grp_summary;
	private Group grp_equation;
	private Label lb_title_1;
	private Label lb_title_2;
	private Label lb_title_3;
	private Label lb_equation;
	private Label lb_rsquare;
	private TableColumn tblc_coefficient_hidden;
	private TableColumn tblc_coefficient_name;
	private TableColumn tblc_coefficient_slope;
	private TableColumn tblc_coefficient_inter;
	private TableColumn tblc_coefficient_rsqr;
	private TableColumn tblc_predict_hidden;
	private TableColumn tblc_predict_ratio;
	private TableColumn tblc_predict_var_x;
	private TableColumn tblc_predict_var_y;
	private Table tbl_coefficient;
	private Table tbl_predict;
	private TableEditor tbl_predict_editor;
	private Button btn_predict_def;
	
	private Map<String,SimpleRegression> regression_map;
	private double max_x;
	
	public RegressionSummary(Composite parent) {
		super(parent, SWT.NONE);
		createContents();
		addEventListener();
	}

	protected void createContents() {
	    setLayout(new GridLayout(1, false));

		grp_summary = new Group(this, SWT.NONE);
		grp_summary.setLayout(new GridLayout(1, false));
	    grp_summary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	    sc_summary = new ScrolledComposite(grp_summary, SWT.V_SCROLL | SWT.H_SCROLL);
	    sc_summary.setLayout(new GridLayout(1, false));
	    sc_summary.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	    comp_contents = new Composite(sc_summary, SWT.NONE);
	    GridLayout gl_contents = new GridLayout(1, false);
	    gl_contents.verticalSpacing = 8;
	    gl_contents.marginBottom = 0;
	    gl_contents.marginTop = 10;
	    gl_contents.marginRight = 10;
	    gl_contents.marginLeft = 10;
	    comp_contents.setLayout(gl_contents);
	    GridData gd_contents = new GridData(SWT.FILL, SWT.FILL, true, true);
	    comp_contents.setLayoutData(gd_contents);		
		
		lb_title_1 = new Label(comp_contents, SWT.NONE);
		lb_title_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lb_title_1.setText("■ Regression Coefficient");
		lb_title_1.setFont(Utility.getFont(SWT.BOLD));		
		
	    GridData gd_tbl_coefficient = new GridData(SWT.FILL, SWT.FILL, true, true);
	    tbl_coefficient = new Table(comp_contents, SWT.BORDER | SWT.FULL_SELECTION);
	    tbl_coefficient.setLayoutData(gd_tbl_coefficient);
	    tbl_coefficient.setFont(Utility.getFont(Constant.DEFAULT_FONT_SIZE-1, SWT.NONE));
	    tbl_coefficient.setHeaderVisible(true);
	    tbl_coefficient.setLinesVisible(true);
	    tbl_coefficient.getHorizontalBar().setVisible(false);
	    tblc_coefficient_hidden = new TableColumn(tbl_coefficient, SWT.NONE);
	    tblc_coefficient_name = new TableColumn(tbl_coefficient, SWT.CENTER);
	    tblc_coefficient_name.setText("Name");
	    tblc_coefficient_name.setData(new StringTableItemComparator(1));
	    tblc_coefficient_slope = new TableColumn(tbl_coefficient, SWT.RIGHT);
	    tblc_coefficient_slope.setText("Slope");
	    tblc_coefficient_slope.setData(new ValueTableItemComparator(2));
	    tblc_coefficient_inter = new TableColumn(tbl_coefficient, SWT.RIGHT);
	    tblc_coefficient_inter.setText("Intercept");
	    tblc_coefficient_inter.setData(new ValueTableItemComparator(3));
	    tblc_coefficient_rsqr = new TableColumn(tbl_coefficient, SWT.RIGHT);
	    tblc_coefficient_rsqr.setText("R-square");
	    tblc_coefficient_rsqr.setData(new ValueTableItemComparator(4));
		
	    new Label(comp_contents, SWT.NONE);

		lb_title_2 = new Label(comp_contents, SWT.NONE);
		lb_title_2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lb_title_2.setText("■ Regression Equation");
		lb_title_2.setFont(Utility.getFont(SWT.BOLD));
		
		grp_equation = new Group(comp_contents, SWT.NONE);
		GridLayout gl_grp_equation = new GridLayout(1, false);
		gl_grp_equation.marginHeight = 0;
		gl_grp_equation.verticalSpacing = 0;
		grp_equation.setLayout(gl_grp_equation);
		grp_equation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		lb_equation = new Label(grp_equation, SWT.NONE);
		GridData gd_lb_equation = new GridData(SWT.LEFT, SWT.TOP, true, true);
		gd_lb_equation.widthHint = 200;
		gd_lb_equation.heightHint = 18;
		gd_lb_equation.horizontalIndent = 20;
		lb_equation.setLayoutData(gd_lb_equation);
		lb_equation.setFont(Utility.getFont(Constant.DEFAULT_FONT_SIZE-1, SWT.ITALIC));

		lb_rsquare = new Label(grp_equation, SWT.NONE);
		GridData gd_lb_rsquare = new GridData(SWT.LEFT, SWT.TOP, true, true);
		gd_lb_rsquare.widthHint = 200;
		gd_lb_rsquare.heightHint = 18;
		gd_lb_rsquare.horizontalIndent = 20;
		lb_rsquare.setLayoutData(gd_lb_rsquare);
		lb_rsquare.setFont(Utility.getFont(Constant.DEFAULT_FONT_SIZE-1, SWT.ITALIC));

	    new Label(comp_contents, SWT.NONE);

		lb_title_3 = new Label(comp_contents, SWT.NONE);
		lb_title_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lb_title_3.setText("■ Prediction");
		lb_title_3.setFont(Utility.getFont(SWT.BOLD));		
		
	    GridData gd_tbl_predict = new GridData(SWT.FILL, SWT.FILL, true, true);
		tbl_predict = new Table(comp_contents, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		tbl_predict.setLayoutData(gd_tbl_predict);
		tbl_predict.setFont(Utility.getFont(Constant.DEFAULT_FONT_SIZE-1, SWT.NONE));
		tbl_predict.setHeaderVisible(true);
		tbl_predict.setLinesVisible(true);
		tbl_predict.getHorizontalBar().setVisible(false);
		tblc_predict_hidden = new TableColumn(tbl_predict, SWT.NONE);
		tblc_predict_ratio = new TableColumn(tbl_predict, SWT.RIGHT);
		tblc_predict_ratio.setText("Ratio");
		tblc_predict_var_x = new TableColumn(tbl_predict, SWT.RIGHT);
		tblc_predict_var_y = new TableColumn(tbl_predict, SWT.RIGHT);
		tbl_predict_editor = new TableEditor(tbl_predict);
		tbl_predict_editor.horizontalAlignment = SWT.RIGHT;
		tbl_predict_editor.grabHorizontal = true;
		
		GridData gd_btn_predict_def = new GridData(SWT.RIGHT, SWT.DEFAULT, true, true);
		gd_btn_predict_def.widthHint = -1;
		btn_predict_def = new Button(comp_contents, SWT.NONE);
		btn_predict_def.setLayoutData(gd_btn_predict_def);
		btn_predict_def.setFont(Utility.getFont());
		btn_predict_def.setText("Default");

	    sc_summary.setContent(comp_contents);
	    sc_summary.setExpandHorizontal(true);
	    sc_summary.setExpandVertical(true);

	}

	protected void addEventListener() {

		tbl_coefficient.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SimpleRegression regression = regression_map.get(((TableItem)e.item).getText(1));				
				StringBuffer eq = new StringBuffer();
				eq.append("y = ");
				eq.append(String.format("%f", regression.getSlope())).append("x");
				if(regression.hasIntercept()) {
					eq.append(" + ").append(String.format("%f", regression.getIntercept()));
				}
				StringBuffer r2 = new StringBuffer();
				r2.append("R² = ").append(String.format("%f", regression.getRSquare()));
				lb_equation.setText(eq.toString());
				lb_rsquare.setText(r2.toString());
				resetPredictValueTable();
				toggleDetailVisible(true);
			}			
		});
		
		tbl_coefficient.addListener(SWT.MouseWheel, new Listener() {
			public void handleEvent(Event event) {
				Point origin = sc_summary.getOrigin();
				origin.y -= event.count;
				sc_summary.setOrigin(origin);
			}
		});

		tblc_coefficient_name.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				sortCoefficientTable(tbl_coefficient, (TableColumn)e.widget);
			}
		});

		tblc_coefficient_slope.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				sortCoefficientTable(tbl_coefficient, (TableColumn)e.widget);
			}
		});

		tblc_coefficient_inter.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				sortCoefficientTable(tbl_coefficient, (TableColumn)e.widget);
			}
		});

		tblc_coefficient_rsqr.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				sortCoefficientTable(tbl_coefficient, (TableColumn)e.widget);
			}
		});

		tbl_predict.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				Point pt = new Point(e.x, e.y);
				TableItem selectedItem = null;
				int columnIdx = 0; 
				for(int i = tbl_predict.getTopIndex(); i < tbl_predict.getItemCount(); i++) {
					selectedItem = tbl_predict.getItem(i);
					for(int j = 1; j < 4; j++) {
						Rectangle rect = selectedItem.getBounds(j);
						if(rect.contains(pt)) {
							columnIdx = j;
							break;
						}
					}
					if(columnIdx > 0) break;
				}

				if(columnIdx == 0) {
					return;
				}

				Control oldEditor = tbl_predict_editor.getEditor();
				final Text newEditor = new Text(tbl_predict, SWT.RIGHT);
				newEditor.setText(selectedItem.getData()==null ? "" : DF_FLOAT.format(((EditorEntry)selectedItem.getData()).get(columnIdx)));
				newEditor.setFont(tbl_predict.getFont());
				newEditor.selectAll();
				newEditor.setFocus();

				final int idx = columnIdx;
				final TableItem item = selectedItem;
				final String oldText = selectedItem.getText(columnIdx);
				Listener textListener = new Listener() {
					public void handleEvent(Event e) {
						String newText = newEditor.getText();
						newEditor.dispose();
						if(e.type == SWT.FocusOut) {
							if(oldText.equals(newText)) {
								e.doit = false;
							} else {
								checkPredictValueAndSet(idx, newText, item);
								setPredictValueTable(getPredictValues());
							}
						} else if(e.type == SWT.Traverse) {
							if(e.detail == SWT.TRAVERSE_RETURN) {
								checkPredictValueAndSet(idx, newText, item);
								setPredictValueTable(getPredictValues());
							} else if(e.detail == SWT.TRAVERSE_ESCAPE) {
								e.doit = false;
							}
						}
					}
				};		
				newEditor.addListener(SWT.FocusOut, textListener);
				newEditor.addListener(SWT.Traverse, textListener);				
				tbl_predict_editor.minimumHeight = tbl_predict.getItemHeight();
				tbl_predict_editor.minimumWidth = tbl_predict.getColumn(idx).getWidth();				
				tbl_predict_editor.setEditor(newEditor, item, idx);
				
				if(oldEditor != null) {
					oldEditor.dispose();
				}
			}
		});
		
		tbl_predict.addListener(SWT.MouseWheel, new Listener() {
			public void handleEvent(Event event) {
				Point origin = sc_summary.getOrigin();
				origin.y -= event.count;
				sc_summary.setOrigin(origin);
			}
		});

		btn_predict_def.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	    		resetPredictValueTable();
	    	}
	    });

	}
	
	public void apply(RegressionAnalysisChart chart) {
		reset();
		XYPlot plot = (XYPlot)chart.getJFreeChart().getPlot();
		XYDataset dataset = plot.getDataset();
		if(dataset.getItemCount(0) < 1) return;
		max_x = dataset.getXValue(0, dataset.getItemCount(0)-1);
		List<SimpleRegression> regressions = chart.getRegressions();
		regression_map = new HashMap<String, SimpleRegression>(regressions.size());
		for(int i = 0; i < regressions.size(); i++) {
			SimpleRegression regression = regressions.get(i);
			String key = (String)dataset.getSeriesKey(i);
			regression_map.put(key, regression);
			TableItem item = new TableItem(tbl_coefficient, SWT.NONE);
			item.setText(1, key);
			item.setText(2, String.format("%f", regression.getSlope()));
			item.setText(3, String.format("%f", regression.getIntercept()));
			item.setText(4, String.format("%f", regression.getRSquare()));
		}
		tblc_coefficient_name.pack();
		tblc_coefficient_slope.pack();
		tblc_coefficient_inter.pack();
		tblc_coefficient_rsqr.pack();
		tblc_predict_var_x.setText(chart.getLableX());
		tblc_predict_var_y.setText(chart.getLableY());
		sc_summary.setMinSize(comp_contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public void reset() {
		tblc_coefficient_hidden.setWidth(0);
		tblc_coefficient_name.setWidth(100);
		tblc_coefficient_slope.setWidth(80);
		tblc_coefficient_inter.setWidth(80);
		tblc_coefficient_rsqr.setWidth(80);
		tbl_coefficient.removeAll();
		lb_equation.setText("");
		lb_rsquare.setText("");		
		tblc_predict_var_x.setText("Variable-X");
		tblc_predict_var_y.setText("Variable-Y");
		tblc_predict_hidden.setWidth(0);
		tblc_predict_ratio.setWidth(80);
		tblc_predict_var_x.setWidth(80);
		tblc_predict_var_y.setWidth(80);
		tbl_predict.removeAll();
		toggleDetailVisible(false);
	}
	
	protected void toggleDetailVisible(boolean flag) {
		lb_title_2.setVisible(flag);
		grp_equation.setVisible(flag);
		lb_title_3.setVisible(flag);
		tbl_predict.setVisible(flag);
		btn_predict_def.setVisible(flag);
		sc_summary.setMinSize(comp_contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	private void sortCoefficientTable(Table table, TableColumn selectedColumn) {
	    TableColumn sortColumn = table.getSortColumn();
	    int dir = table.getSortDirection();
	    if(sortColumn == selectedColumn) {
	        dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
	    } else {
	        table.setSortColumn(selectedColumn);
	        dir = SWT.UP;
	    }
	    TableItem[] items = table.getItems();
	    String selectedKey = null;
	    if(table.getSelection() != null) {
	    	selectedKey = table.getSelection()[0].getText(1);
	    }
	    @SuppressWarnings("unchecked")
		Comparator<TableItem> comparator = (Comparator<TableItem>)selectedColumn.getData();
	    for(int i = 1; i < items.length; i++) {
	        for(int j = 0; j < i; j++) {
	            if((comparator.compare(items[i], items[j]) < 0 && dir == SWT.UP) || (comparator.compare(items[i], items[j]) > 0 && dir == SWT.DOWN)) {
	                String[] oldItem = new String[table.getColumnCount()];
	                for(int h = 0; h < table.getColumnCount(); h++) {
	                	oldItem[h] = items[i].getText(h);
	                }
	                items[i].dispose();
	                TableItem newItem = new TableItem(table, SWT.NONE, j);
	                newItem.setText(oldItem);
	                items = table.getItems();
	                break;
	            }
	        }
	    }
	    table.setSortDirection(dir);
	    if(selectedKey != null) {
	    	for(int i = 0; i < table.getItemCount(); i++) {
	    		if(selectedKey.equals(table.getItem(i).getText(1))) {
	    			table.setSelection(i);
	    		}
	    	}	    	
	    }
	}

	protected void resetPredictValueTable() {
		tbl_predict.removeAll();
		String key = tbl_coefficient.getSelection()[0].getText(1);
		SimpleRegression regression = regression_map.get(key);
		float[] defValues = { 0.5f, 1.0f, 1.5f, 2.0f };
		for(float val : defValues) {			
			TableItem item = new TableItem(tbl_predict, SWT.NONE);
			double ratio = val * 100;
			double x = max_x * val;
			double y = regression.predict(x);
			item.setText(1, DF_INT.format((long)ratio)+"%");
			item.setText(2, DF_INT.format((long)x));
			item.setText(3, DF_FLOAT.format(y));
			item.setData(new EditorEntry(new double[]{ 0.0d, ratio, x, y }));
		}
		TableItem item = new TableItem(tbl_predict, SWT.NONE);
		item.setText(new String[] {"", "", "", ""});
		item.setData(null);
	}
	
	protected EditorEntry[] getPredictValues() {
		if(tbl_predict.getItemCount() == 0) {
			return null;
		}
	    List<EditorEntry> list = new ArrayList<EditorEntry>();
		for(TableItem item : tbl_predict.getItems()) {
			if(item.getData() != null) {
				list.add((EditorEntry)item.getData());
			}
		}
		Collections.sort(list, new EditorEntryComparator(2));
		EditorEntry[] sortedValues = new EditorEntry[list.size()];
		return list.toArray(sortedValues);
	}
	
	protected void setPredictValueTable(EditorEntry[] values) {
		tbl_predict.removeAll();
		for(EditorEntry value : values) {
			TableItem item = new TableItem(tbl_predict, SWT.NONE);
			item.setText(1, DF_INT.format((long)value.get(1))+"%");
			item.setText(2, DF_INT.format((long)value.get(2)));
			item.setText(3, DF_FLOAT.format(value.get(3)));
			item.setData(value);
		}
		TableItem item = new TableItem(tbl_predict, SWT.NONE);
		item.setText(new String[] {"", "", "", ""});
		item.setData(null);
	}
	
	protected void checkPredictValueAndSet(int index, String value, TableItem item) {
		value = value.trim();
		if(tbl_predict.getItemCount() == 2 && "".equals(value.trim())) {
			Logger.debug("The last item can not be deleted.");
		} else if(!"".equals(value.trim()) && !StringUtil.isNumeric(value, true)) {
			Logger.debug("Not a number : " + value);
		} else {
			if("".equals(value)) {
				item.setText(new String[] {"", "", "", ""});
				item.setData(null);
			} else {
				String key = tbl_coefficient.getSelection()[0].getText(1);
				SimpleRegression regression = regression_map.get(key);
				double ratio, x, y;
				if(index == 1) {
					ratio = Double.parseDouble(value);
					x = max_x * (ratio/100.0f);
					y = regression.predict(x);					
				} else if(index == 2) {
					x = Double.parseDouble(value);
					ratio = x / max_x * 100;
					y = (int)regression.predict(x);
				} else {
					y = Double.parseDouble(value);
					x = (y - regression.getIntercept()) / regression.getSlope();
					ratio = x / max_x * 100;
				}
				item.setText(1, DF_INT.format(ratio)+"%");
				item.setText(2, DF_INT.format(x));
				item.setText(3, DF_FLOAT.format(y));
				item.setData(new EditorEntry(new double[]{ 0.0d, ratio, x, y }));
				TableItem blankItem = new TableItem(tbl_predict, SWT.NONE);
				blankItem.setText(new String[] {"", "", "", ""});
				blankItem.setData(null);
			}
		}
	}

	
    class ValueTableItemComparator implements Comparator<TableItem> {
    	int idx = 0;
    	public ValueTableItemComparator(int idx) { this.idx = idx; }
        public int compare(TableItem t1, TableItem t2) {
            double i1 = Double.parseDouble(t1.getText(idx));
            double i2 = Double.parseDouble(t2.getText(idx));
            if(i1 < i2) return -1;
            if(i1 > i2) return 1;
            return 0;
        }
    }

    class StringTableItemComparator implements Comparator<TableItem> {
    	int idx = 0;
    	public StringTableItemComparator(int idx) { this.idx = idx; }
        public int compare(TableItem t1, TableItem t2) {
            return t1.getText(idx).compareTo(t2.getText(idx));
        }
    }
    
    class EditorEntry {
    	public double[] data;
    	public EditorEntry(double[] data) {
    		this.data = data;
    	}
    	public double get(int i) {
    		return data[i];
    	}
    }

    class EditorEntryComparator implements Comparator<EditorEntry> {
    	int idx = 0;
    	public EditorEntryComparator(int idx) { this.idx = idx; }
        public int compare(EditorEntry t1, EditorEntry t2) {
            if(t1.get(idx) < t2.get(idx)) return -1;
            if(t1.get(idx) > t2.get(idx)) return 1;
            return 0;
        }
    }

}
