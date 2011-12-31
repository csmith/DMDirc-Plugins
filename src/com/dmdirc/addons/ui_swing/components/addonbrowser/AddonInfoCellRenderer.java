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

package com.dmdirc.addons.ui_swing.components.addonbrowser;


import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;


/**
 * Addon list cell renderer.
 */
public class AddonInfoCellRenderer implements TableCellRenderer {

    /**
     * {@inheritDoc}
     *
     * @return Returns the component for this cell
     */
    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value, final boolean isSelected, final boolean hasFocus,
            final int row, final int column) {
        if (value instanceof AddonInfoLabel) {
            final AddonInfoLabel label = (AddonInfoLabel) value;

            final Color colour = (row & 1) == 1 ? new Color(0xEE, 0xEE, 0xFF) : Color.WHITE;
            if (!label.getBackground().equals(colour)) {
                label.setBackground(colour);
            }

            final int height = label.getPreferredSize().height;
            if (table.getRowHeight(row) != height) {
                table.setRowHeight(row, height);
            }

            return label;
        } else {
            return new JLabel(value.toString());
        }
    }
}
