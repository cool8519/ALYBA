package dal.tool.analyzer.alyba.ui.comp;

import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.ui.AlybaGUI;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.swt.MessageUtil;

public class TPMAnalyzer extends Shell {

	private TPMAnalyzer instance;
	
	private Label hline_top;
	private Text txt_title;
	private TabFolder tbf_view;
	private TabItem tbi_insert;
	private TPMInsert tpmInsert;

    public static void main(String[] args) {
		TimeZone.setDefault(Constant.TIMEZONE_UTC);
        Display display = new Display();
		FontData fd = display.getSystemFont().getFontData()[0];
		Constant.DEFAULT_FONT_SIZE = fd.getHeight() - (int)Math.ceil((-fd.data.lfHeight-12)/2.0F);
        TPMAnalyzer analyzer = new TPMAnalyzer(display, SWT.SHELL_TRIM);

        while(!analyzer.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

	public TPMAnalyzer(Display display, int style) {
		super(display, style);
		createContents();
		addEventListener();
		open();
		layout(true, true);
	}
	
	protected void createContents() {
		
		instance = this;
		
		setSize(800, 750);
		setMinimumSize(800, 750);
		setText("ALYBA " + Constant.PROGRAM_VERSION + " - TPM Analyzer");
		Rectangle dispRect = getDisplay().getMonitors()[0].getBounds();
		Rectangle shellRect = getBounds();
		setLocation((dispRect.width - shellRect.width) / 2, (dispRect.height - shellRect.height) / 2);		

		FormLayout forml_main = new FormLayout();
		forml_main.marginHeight = 10;
		forml_main.marginWidth = 10;
		setLayout(forml_main);

		FormData fd_lb_title = new FormData();
		fd_lb_title.left = new FormAttachment(0);
		fd_lb_title.top = new FormAttachment(0, 7);
		fd_lb_title.width = 40;
		fd_lb_title.height = 15;
		Label lb_title = new Label(this, SWT.NONE);
		lb_title.setLayoutData(fd_lb_title);
		lb_title.setAlignment(SWT.CENTER);
		lb_title.setText("Title");
		lb_title.setFont(Utility.getFont());

		FormData fd_txt_title = new FormData();
		fd_txt_title.left = new FormAttachment(lb_title, 5, SWT.RIGHT);
		fd_txt_title.right = new FormAttachment(100, -25);
		fd_txt_title.top = new FormAttachment(0, 6);
		fd_txt_title.height = 15;
		txt_title = new Text(this, SWT.BORDER | SWT.RESIZE);
		txt_title.setLayoutData(fd_txt_title);
		txt_title.setText(Constant.OUTPUT_DEFAULT_TITLE);
		txt_title.setFont(Utility.getFont());
		
		FormData fd_hline_top = new FormData();
		fd_hline_top.left = new FormAttachment(0);
		fd_hline_top.right = new FormAttachment(100);
		fd_hline_top.top = new FormAttachment(txt_title, 7);
		fd_hline_top.height = 10;
		hline_top = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		hline_top.setLayoutData(fd_hline_top);

		FormData fd_tbf_view = new FormData();
		fd_tbf_view.left = new FormAttachment(0);
		fd_tbf_view.right = new FormAttachment(100);
		fd_tbf_view.top = new FormAttachment(hline_top, 5);
		fd_tbf_view.bottom = new FormAttachment(100);
		tbf_view = new TabFolder(this, SWT.NONE);
		tbf_view.setLayoutData(fd_tbf_view);
		tbf_view.setFont(Utility.getFont());

		tbi_insert = new TabItem(tbf_view, SWT.NONE);
		tbi_insert.setText("Parser");
		tpmInsert = new TPMInsert(tbf_view, SWT.NONE, this);
		tbi_insert.setControl(tpmInsert);
		
	}
		
	protected void addEventListener() {
		
		addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				if(AlybaGUI.instance != null) {
					AlybaGUI.instance.tpmAnalyzer.setVisible(false);
					e.doit = false;
				} else {
					e.doit = MessageUtil.showConfirmMessage(instance, "Do you really want to exit?");
					if(e.doit) {
						if(AlybaGUI.inProgressDbUtil != null) {
							AlybaGUI.inProgressDbUtil.closeAll();
							AlybaGUI.inProgressDbUtil = null;
						}
					}
				}
			}
		});

	}

	protected void checkSubclass() {
	}

	public String getTitle() {
		return txt_title.getText();
	}
	
	public void resetData() {
		txt_title.setText("");
		tpmInsert.resetData();
		tbf_view.setSelection(0);
	}

}
