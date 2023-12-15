/*******************************************************************************
 * Copyright (c) 2015 Patrik Dufresne Service Logiciel inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *     Patrik Dufresne (info at gmail dot com) - general modification
 *******************************************************************************/
package dal.util.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class are simple switch button.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 */
public class SwitchButton extends Canvas {
    /**
     * Blends two primary color components based on the provided ratio.
     * 
     * @param v1
     *            first component
     * @param v2
     *            second component
     * @param ratio
     *            percentage of the first component in the blend
     * @return
     */
    private static int blend(int v1, int v2, int ratio) {
        int b = (ratio * v1 + (100 - ratio) * v2) / 100;
        return Math.min(255, b);
    }

    /**
     * Blends c1 and c2 based in the provided ratio.
     * 
     * @param c1
     *            first color
     * @param c2
     *            second color
     * @param ratio
     *            percentage of the first color in the blend (0-100)
     * @return the RGB value of the blended color
     * @since 3.1
     */
    private static RGB blend(RGB c1, RGB c2, int ratio) {
        int r = blend(c1.red, c2.red, ratio);
        int g = blend(c1.green, c2.green, ratio);
        int b = blend(c1.blue, c2.blue, ratio);
        return new RGB(r, g, b);
    }

    /**
     * True when the button is ready to be armed.
     */
    private boolean armed;

    /**
     * Colour used to draw the button.
     */
    private Color buttonBackgroundColor;

    /**
     * if not null, displays a glow effect when the mouse is over the widget. Default value is null.
     */
    private Color focusColor;

    /**
     * Gap between the button and the text (default value is 5)
     */
    private Point gap = new Point(10, 3);

    private boolean hasFocus;

    /**
     * True when the mouse entered the widget
     */
    private boolean mouseInside = false;

    boolean paintFocus = true;

    /**
     * If true, display round rectangles instead of rectangles (default value is true)
     */
    private int round = 5;

    private Color selectedBackgroundColor;

    /**
     * Colour for the button
     */
    private Color selectedBorderColor;

    /**
     * Colour when the button is selected
     */
    private Color selectedForegroundColor;

    /**
     * Selection
     */
    private boolean selection = false;

    /**
     * List of selection listeners
     */
    private final List<SelectionListener> selectionlisteners = new ArrayList<SelectionListener>();

    /**
     * Text displayed for the unselected value (default = "no")
     */
    private String textOff = JFaceResources.getString("no");

    /**
     * Text displayed for the selected value (default = "yes")
     */
    private String textOn = JFaceResources.getString("yes");

    private Color unselectedBackgroundColor;

    private Color unselectedBorderColor;

    /**
     * Colour when the button is not selected
     */
    private Color unselectedForegroundColor;

    /**
     * Constructs a new instance of this class given its parent and a style value describing its behavior and
     * appearance.
     * <p>
     * The style value is either one of the style constants defined in class <code>SWT</code> which is applicable to
     * instances of this class, or must be built by <em>bitwise OR</em>'ing together (that is, using the
     * <code>int</code> "|" operator) two or more of those <code>SWT</code> style constants. The class description lists
     * the style constants that are applicable to the class. Style bits are also inherited from superclasses.
     * </p>
     * 
     * @param parent
     *            a composite control which will be the parent of the new instance (cannot be null)
     * @param style
     *            the style of control to construct
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *                </ul>
     * 
     */
    public SwitchButton(final Composite parent, int style) {
        super(parent, style | SWT.DOUBLE_BUFFERED);

        // Used System colour for text and background
        this.selectedForegroundColor = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
        this.selectedBackgroundColor = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
        this.unselectedForegroundColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        this.unselectedBackgroundColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);

        this.buttonBackgroundColor = getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        // From background colour generate the border colour.
        this.selectedBorderColor = new Color(getDisplay(), blend(new RGB(0, 0, 0), this.selectedBackgroundColor.getRGB(), 25));
        this.unselectedBorderColor = new Color(getDisplay(), blend(new RGB(0, 0, 0), this.unselectedBackgroundColor.getRGB(), 25));

        // Generate a focus color from system colors.
        this.focusColor = new Color(getDisplay(), blend(this.selectedBackgroundColor.getRGB(), buttonBackgroundColor.getRGB(), 50));

        addListener(SWT.KeyDown, new Listener() {

            @Override
            public void handleEvent(Event e) {
                if (e.character == '\r') {
                    handleActivate(e);
                }
            }
        });
        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                paint(e);
            }
        });
        addListener(SWT.Traverse, new Listener() {
            @Override
            public void handleEvent(Event e) {
                switch (e.detail) {
                case SWT.TRAVERSE_RETURN:
                    e.doit = false;
                    return;
                }
                e.doit = true;
            }
        });
        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                switch (e.type) {
                case SWT.FocusIn:
                    hasFocus = true;
                    redraw();
                    break;
                case SWT.FocusOut:
                    hasFocus = false;
                    redraw();
                    break;
                case SWT.DefaultSelection:
                    handleActivate(e);
                    break;
                case SWT.MouseEnter:
                    mouseInside = true;
                    redraw();
                    break;
                case SWT.MouseExit:
                    mouseInside = false;
                    redraw();
                    break;
                case SWT.MouseDown:
                    handleMouseDown(e);
                    break;
                case SWT.MouseUp:
                    handleMouseUp(e);
                    break;
                case SWT.MouseMove:
                    // handleMouseMove(e);
                    break;
                }
            }
        };
        addListener(SWT.MouseEnter, listener);
        addListener(SWT.MouseExit, listener);
        addListener(SWT.MouseDown, listener);
        addListener(SWT.MouseUp, listener);
        addListener(SWT.MouseMove, listener);
        addListener(SWT.FocusIn, listener);
        addListener(SWT.FocusOut, listener);

    }

    /**
     * Adds the listener to the collection of listeners who will be notified when the control is selected by the user,
     * by sending it one of the messages defined in the <code>SelectionListener</code> interface.
     * <p>
     * <code>widgetSelected</code> is called when the control is selected by the user.
     * <code>widgetDefaultSelected</code> is not called.
     * </p>
     * 
     * @param listener
     *            the listener which should be notified
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     * 
     * @see SelectionListener
     * @see #removeSelectionListener
     * @see SelectionEvent
     */
    public void addSelectionListener(final SelectionListener listener) {
        this.checkWidget();
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        this.selectionlisteners.add(listener);
    }

    /**
     * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
     */
    @Override
    public Point computeSize(final int wHint, final int hHint, final boolean changed) {
        this.checkWidget();

        GC gc = new GC(this);
        // Compute size for the left part
        final Point leftSize = gc.textExtent(this.textOn, SWT.DRAW_MNEMONIC);
        // Compute size for the right part
        final Point rightSize = gc.textExtent(this.textOff, SWT.DRAW_MNEMONIC);
        gc.dispose();

        // Compute whole size
        final int width = Math.max(leftSize.x, rightSize.x) * 2 + 4 * gap.x;
        final int height = Math.max(leftSize.y, rightSize.y) + 2 * gap.y;

        return new Point(width, height);

    }

    /**
     * @return the first color of the toggle button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getButtonBackgroundColor1() {
        this.checkWidget();
        return this.buttonBackgroundColor;
    }

    /**
     * @return the focus color. If null, no focus effect is displayed.
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getFocusColor() {
        this.checkWidget();
        return this.focusColor;
    }

    /**
     * @return the gap value
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Point getGap() {
        this.checkWidget();
        return this.gap;
    }

    /**
     * @return the round flag
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public int getRound() {
        this.checkWidget();
        return this.round;
    }

    /**
     * @return the background color of the left part of the widget (selection is on)
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getSelectedBackgroundColor() {
        this.checkWidget();
        return this.selectedBackgroundColor;
    }

    /**
     * @return the border color of the switch button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getSelectedBorderColor() {
        this.checkWidget();
        return this.selectedBorderColor;
    }

    /**
     * @return the foreground color of the left part of the widget (selection is on)
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getSelectedForegroundColor() {
        this.checkWidget();
        return this.selectedForegroundColor;
    }

    /**
     * @return the selection state of the button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public boolean getSelection() {
        //this.checkWidget();
        return this.selection;
    }

    /**
     * @return the text used to display the unselected option
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public String getTextOff() {
        this.checkWidget();
        return this.textOff;
    }

    /**
     * @return the text used to display the selection
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public String getTextOn() {
        this.checkWidget();
        return this.textOn;
    }

    /**
     * @return the background color of the left part of the widget (selection is on)
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getUnselectedBackgroundColor() {
        this.checkWidget();
        return this.unselectedBackgroundColor;
    }

    /**
     * @return the border color of the switch button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getUnselectedBorderColor() {
        this.checkWidget();
        return this.unselectedBorderColor;
    }

    /**
     * @return the foreground color of the left part of the widget (selection is on)
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public Color getUnselectedForegroundColor() {
        this.checkWidget();
        return this.unselectedForegroundColor;
    }

    /**
     * Called when hyperlink has been activated. Subclasses that override this method must call 'super'.
     */
    protected void handleActivate(Event e) {
        armed = false;
        boolean doit = true;
        for (final SelectionListener listener : this.selectionlisteners) {
            final Event event = new Event();

            event.button = e.button;
            event.display = this.getDisplay();
            event.item = null;
            event.widget = this;
            event.data = null;
            event.time = e.time;
            event.x = e.x;
            event.y = e.y;

            final SelectionEvent selEvent = new SelectionEvent(event);
            listener.widgetSelected(selEvent);
            if (!selEvent.doit) {
                doit = false;
            }
        }
        if (!isDisposed()) {
            triggerAccessible();
        }
        if (doit) {
            selection = !SwitchButton.this.selection;
            redraw();
        }
    }

    protected void handleMouseDown(Event e) {
        if (e.button != 1) return;
        // armed and ready to activate on mouseup
        armed = true;
    }

    protected void handleMouseUp(Event e) {
        if (!armed || e.button != 1) return;
        Point size = getSize();
        // Filter out mouse up events outside
        // the link. This can happen when mouse is
        // clicked, dragged outside the link, then
        // released.
        if (e.x < 0) return;
        if (e.y < 0) return;
        if (e.x >= size.x) return;
        if (e.y >= size.y) return;
        handleActivate(e);
    }

    /**
     * Paint the widget
     * 
     * @param event
     *            paint event
     */
    private void paint(final PaintEvent event) {
        final Rectangle rect = this.getClientArea();
        if (rect.width == 0 || rect.height == 0) {
            return;
        }
        // For ease of use declare width and height.
        final int x = rect.x;
        final int y = rect.y;
        final int width = rect.width;
        final int height = rect.height;

        GC gc = event.gc;
        gc.setAntialias(SWT.ON);

        /*
         * Draw background
         */
        // Draw back color
        gc.setForeground(!this.selection ? this.unselectedBackgroundColor : this.selectedBackgroundColor);
        gc.setBackground(!this.selection ? this.unselectedBackgroundColor : this.selectedBackgroundColor);
        if (!getEnabled()) {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
            gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        }
        gc.fillRoundRectangle(x, y, width - 1, height - 1, this.round, this.round);
        // Draw back border
        gc.setForeground(!this.selection ? getUnselectedBorderColor() : getSelectedBorderColor());
        if (!getEnabled()) {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        }
        gc.drawRoundRectangle(x, y, width - 1, height - 1, this.round, this.round);

        // Draw Left text
        gc.setForeground(this.selectedForegroundColor);
        if (!getEnabled()) {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        }
        final Point onSize = gc.textExtent(this.textOn, SWT.DRAW_MNEMONIC);
        gc.drawText(this.textOn, x + width / 4 - onSize.x / 2, y + height / 2 - onSize.y / 2, SWT.DRAW_MNEMONIC);

        // Draw Right text
        gc.setForeground(this.unselectedForegroundColor);
        if (!getEnabled()) {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        }
        final Point offSize = gc.textExtent(this.textOff, SWT.DRAW_MNEMONIC);
        gc.drawText(this.textOff, x + 3 * width / 4 - offSize.x / 2, y + height / 2 - offSize.y / 2, SWT.DRAW_MNEMONIC);

        // Draw toggle button.
        Rectangle b;
        gc.setBackground(this.buttonBackgroundColor);
        if (!this.selection) {
            b = new Rectangle(x, y, width / 2, height);
        } else {
            b = new Rectangle(x + width - (width / 2), y, width / 2, height);
        }
        gc.fillRoundRectangle(b.x, b.y, b.width, b.height, this.round, this.round);
        gc.setForeground(!this.selection ? this.unselectedBorderColor : this.selectedBorderColor);
        if (!getEnabled()) {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        }
        gc.drawRoundRectangle(b.x, b.y, b.width - 1, b.height - 1, this.round, this.round);

        // Draw hover line inside the button.
        if (this.focusColor != null && this.mouseInside) {
            gc.setForeground(this.focusColor);
            gc.setLineWidth(1);
            gc.drawRoundRectangle(b.x + 1, b.y + 1, b.width - 3, b.height - 3, this.round, this.round);
            gc.setLineWidth(1);
        }

        // Draw focus line in button
        if (paintFocus && hasFocus) {
            gc.setForeground(getForeground());
            if (Util.getWS().equals(Util.WS_GTK)) {
                gc.drawFocus(b.x + 3, b.y + 3, b.width - 7, b.height - 7);
            } else {
                gc.drawFocus(b.x + 3, b.y + 3, b.width - 6, b.height - 6);
            }
        }

    }

    /**
     * Removes the listener from the collection of listeners who will be notified when the control is selected by the
     * user.
     * 
     * @param listener
     *            the listener which should no longer be notified
     * 
     * @exception IllegalArgumentException
     *                <ul>
     *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     *                </ul>
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     * 
     * @see SelectionListener
     * @see #addSelectionListener
     */
    public void removeSelectionListener(final SelectionListener listener) {
        this.checkWidget();
        if (listener == null) {
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        }
        this.selectionlisteners.remove(listener);
    }

    /**
     * @param buttonBackgroundColor
     *            the first color of the toggle button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setButtonBackgroundColor(final Color buttonBackgroundColor) {
        this.checkWidget();
        this.buttonBackgroundColor = buttonBackgroundColor;
    }

    /**
     * @param focusColor
     *            the focus color to set. If null, no focus effect is displayed.
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setFocusColor(final Color focusColor) {
        this.checkWidget();
        this.focusColor = focusColor;
    }

    /**
     * @param gap
     *            the gap value to set
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setGap(final Point gap) {
        this.checkWidget();
        this.gap = gap;
    }

    /**
     * @param round
     *            the round flag to set. If true, the widget is composed of round rectangle instead of rectangles
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setRound(final int round) {
        this.checkWidget();
        this.round = round;
    }

    /**
     * @param the
     *            background color of the left part of the widget (selection is on)
     */
    public void setSelectedBackgroundColor(final Color selectedBackgroundColor) {
        this.checkWidget();
        this.selectedBackgroundColor = selectedBackgroundColor;
    }

    /**
     * @param buttonBorderColor
     *            the border color of the switch button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setSelectedButtonBorderColor(final Color selectedBorderColor) {
        this.checkWidget();
        this.selectedBorderColor = selectedBorderColor;
    }

    /**
     * @param the
     *            foreground color of the left part of the widget (selection is on)
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setSelectedForegroundColor(final Color selectedForegroundColor) {
        this.checkWidget();
        this.selectedForegroundColor = selectedForegroundColor;
    }

    /**
     * @param selection
     *            the selection state of the button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setSelection(final boolean selection) {
        this.checkWidget();
        this.selection = selection;
    }

    /**
     * @param textOff
     *            the text used to display the unselected option
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setTextOff(final String textOff) {
        this.checkWidget();
        this.textOff = textOff;
    }

    /**
     * @param textOn
     *            the text used to display the selection
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setTextOn(final String textOn) {
        this.checkWidget();
        this.textOn = textOn;
    }

    /**
     * @param unselectedBackgroundColor
     *            the background color of the left part of the widget (selection is on)
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setUnselectedBackgroundColor(final Color unselectedBackgroundColor) {
        this.checkWidget();
        this.unselectedBackgroundColor = unselectedBackgroundColor;
    }

    /**
     * @param buttonBorderColor
     *            the border color of the switch button
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setUnselectedButtonBorderColor(final Color unselectedBorderColor) {
        this.checkWidget();
        this.unselectedBorderColor = unselectedBorderColor;
    }

    /**
     * @param unselectedForegroundColor
     *            the foreground color of the left part of the widget (selection is on)
     * @exception SWTException
     *                <ul>
     *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     *                </ul>
     */
    public void setUnselectedForegroundColor(final Color unselectedForegroundColor) {
        this.checkWidget();
        this.unselectedForegroundColor = unselectedForegroundColor;
    }

    void triggerAccessible() {
        getAccessible().setFocus(ACC.CHILDID_SELF);
    }

}