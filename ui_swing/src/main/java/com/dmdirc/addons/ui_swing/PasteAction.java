/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

/**
 * Paste action.
 */
public final class PasteAction extends AbstractAction {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Clipboard to paste with. */
    private final Clipboard clipboard;
    /** Text component to be acted upon. */
    private final JTextComponent comp;

    /**
     * Instantiates a new paste action.
     *
     * @param clipboard Clipboard to paste with
     * @param comp      Component to be acted upon
     */
    public PasteAction(final Clipboard clipboard, final JTextComponent comp) {
        super("Paste");

        this.clipboard = clipboard;
        this.comp = comp;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        comp.paste();
    }

    @Override
    public boolean isEnabled() {
        if (comp.isEditable() && comp.isEnabled()) {
            final Transferable contents = clipboard.getContents(this);
            return contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        } else {
            return false;
        }
    }

}
