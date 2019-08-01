package dal.tool.analyzer.alyba.ui.chart.extension;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;


public class MultiLineXYPointerAnnotation extends XYPointerAnnotation {
    
	private static final long serialVersionUID = 1L;
	
	private List<String> lines = new ArrayList<String>();
    private boolean fontMetricsCalculated = false;
    private int lineHeight;
    private int width;
    private int height;
    private int paddingLeft;
    private int paddingRight;
    private int paddingBottom;
    
    public MultiLineXYPointerAnnotation(String text, double x, double y, double angle) {
        super(text, x, y, angle);
        splitLines(text);        
    }
    
    private void splitLines(String text) {
        StringTokenizer st = new StringTokenizer(text, "\n");
        while (st.hasMoreTokens()) {
            lines.add(st.nextToken());
        }
    }

    public void addLine(String line) {
        lines.add(line);
    }
    
    public void setFont(Font font) {
        super.setFont(font);
        fontMetricsCalculated = false;
    }
    
    private void calculateFontMetrics(Graphics2D graphics) {
        if (!fontMetricsCalculated) {
            FontMetrics metrics = graphics.getFontMetrics(this.getFont());
            lineHeight = metrics.getHeight();            
            for (String line : lines) {
                int lineWidth = metrics.stringWidth(line);
                if (lineWidth > width) { 
                    width = lineWidth;
                } 
            }
            paddingLeft = metrics.charWidth(' ') * 2;
            paddingRight = metrics.charWidth(' ') * 2;
            paddingBottom = metrics.charWidth(' ') * 2;
            width += (paddingLeft + paddingRight);
            height = (lineHeight * lines.size()) + paddingBottom;
            fontMetricsCalculated = true;
        }
    }
    
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
        
		calculateFontMetrics(g2);

		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);

		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

		double j2DX = domainAxis.valueToJava2D(getX(), dataArea, domainEdge);
		double j2DY = rangeAxis.valueToJava2D(getY(), dataArea, rangeEdge);
		if(orientation == PlotOrientation.HORIZONTAL) {
			double temp = j2DX;
			j2DX = j2DY;
			j2DY = temp;
		}

		double angle = getAngle();
		double baseRadius = getBaseRadius();
		double tipRadius = getTipRadius();
		double arrowLength = getArrowLength();
		double arrowWidth = getArrowWidth();
		double labelOffset = getLabelOffset();
		Stroke arrowStroke = getArrowStroke();
		Paint arrowPaint = getArrowPaint();

		double startX = j2DX + Math.cos(angle) * baseRadius;
		double startY = j2DY + Math.sin(angle) * baseRadius;
		double endX = j2DX + Math.cos(angle) * tipRadius;
		double endY = j2DY + Math.sin(angle) * tipRadius;
		double arrowBaseX = endX + Math.cos(angle) * arrowLength;
		double arrowBaseY = endY + Math.sin(angle) * arrowLength;
		double arrowLeftX = arrowBaseX + Math.cos(angle + 1.5707963267948966D) * arrowWidth;
		double arrowLeftY = arrowBaseY + Math.sin(angle + 1.5707963267948966D) * arrowWidth;
		double arrowRightX = arrowBaseX - Math.cos(angle + 1.5707963267948966D) * arrowWidth;
		double arrowRightY = arrowBaseY - Math.sin(angle + 1.5707963267948966D) * arrowWidth;

		GeneralPath arrow = new GeneralPath();
		arrow.moveTo((float)endX, (float)endY);
		arrow.lineTo((float)arrowLeftX, (float)arrowLeftY);
		arrow.lineTo((float)arrowRightX, (float)arrowRightY);
		arrow.closePath();

		g2.setStroke(arrowStroke);
		g2.setPaint(arrowPaint);
		Line2D line = new java.awt.geom.Line2D.Double(startX, startY, arrowBaseX, arrowBaseY);
		g2.draw(line);
		g2.fill(arrow);

		double startOfArrowX = j2DX + Math.cos(angle) * (baseRadius + labelOffset);
		double startOfArrowY = j2DY + Math.sin(angle) * (baseRadius + labelOffset);
		
		double labelX = startOfArrowX;
		double labelY = startOfArrowY;
		double degree = Math.toDegrees(angle);
		if(degree > 0 && degree <= 90) {
			labelX = startOfArrowX;
			labelY = startOfArrowY;
		} else if(degree > 90 && degree <= 180) {
			labelX = startOfArrowX - width;
			labelY = startOfArrowY;
		} else if(degree > 180 && degree <= 270) {
			labelX = startOfArrowX - width;
			labelY = startOfArrowY - height;
		} else if(degree > 270 && (degree < 360 || degree == 0)) {
			labelX = startOfArrowX;
			labelY = startOfArrowY - height;
		}

		Shape hotspot = new Rectangle2D.Double(labelX, labelY, width, height);
		if(getBackgroundPaint() != null) {
			g2.setPaint(getBackgroundPaint());
			g2.fill(hotspot);
		}
		if(isOutlineVisible()) {
			g2.setStroke(getOutlineStroke());
			g2.setPaint(getOutlinePaint());
			g2.draw(hotspot);
		}

		g2.setFont(getFont());
		g2.setPaint(getPaint());
		labelX += paddingLeft;
		labelY += lineHeight;
        for(String s : lines) {
            g2.drawString(s, (int)labelX, (int)labelY);
            labelY += lineHeight;
        }
		
		String toolTip = getToolTipText();
		String url = getURL();
		if(toolTip != null || url != null) {
			addEntity(info, hotspot, rendererIndex, toolTip, url);
		}
		
    }
    
}