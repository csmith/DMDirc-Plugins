/*
 * Copyright (c) 2006-2012 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.dcc.ui;

import com.dmdirc.addons.ui_swing.components.frames.SwingFrameComponent;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * A panel which serves as a top-level placeholder for other DCC windows.
 *
 * @since 0.6.6
 */
public class PlaceholderPanel extends JPanel implements SwingFrameComponent {

    /** A version number for this class. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new placeholder window for the specified UI controller and
     * owner.
     */
    public PlaceholderPanel() {
        super(new MigLayout("fill, alignx center, aligny center"));
        add(new TextLabel(
                "This is a placeholder window to group DCCs together."
                + "\n\nClosing this window will close all the active DCCs"));
    }
}
