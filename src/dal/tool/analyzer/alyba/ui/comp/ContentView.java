package dal.tool.analyzer.alyba.ui.comp;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import dal.tool.analyzer.alyba.Constant;
import dal.tool.analyzer.alyba.util.Utility;
import dal.util.swt.ImageUtil;

public class ContentView extends Shell {

	private Text txt_content;
	private int min_width = 640;
	private int max_width = 1024;
	private int min_height = 150;
	private int max_height = 600;
	private float char_width = 5.4F;
	private float char_height = 16.0F;
	private int max_length = 0;
	private int line_count = 0;

	public ContentView(Display display) {
		super(display);
		createContents();
		open();
		layout();
	}

	protected void createContents() {

		setSize(min_width, min_height);
		setImage(ImageUtil.getImage(Constant.IMAGE_PATH_TRAYICON));
		setText("Content View");
		setLayout(new GridLayout());

		txt_content = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		txt_content.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true));
		txt_content.setFont(Utility.getFont());

	}

	protected void checkSubclass() {
	}

	public void addLine(String line) {
		max_length = (line.length() > max_length) ? line.length() : max_length;
		line_count++;
		txt_content.append(line + "\n");
	}

	public void addLines(List<String> lines) {
		for(String s : lines) {
			addLine(s);
		}
	}

	public void autoSize() {
		double width = (double)max_length * char_width + 100;
		if(width < min_width) {
			width = min_width;
		} else if(width > max_width) {
			width = max_width;
		}
		double height = (double)(line_count + 1) * char_height + 75;
		if(height < min_height) {
			height = min_height;
		} else if(height > max_height) {
			height = max_height;
		}
		setSize((int)width, (int)height);
	}

}
