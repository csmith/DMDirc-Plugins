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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import javax.annotation.Nonnull;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Table for addons.
 */
public class AddonTable extends JTable {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new addon table.
     */
    public AddonTable() {
        super(new DefaultTableModel(0, 1));
        setTableHeader(null);
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        return new AddonInfoCellRenderer();
    }

    @Override
    public TableCellEditor getCellEditor(final int row, final int column) {
        return new AddonInfoCellEditor();
    }

    @Override
    public DefaultTableModel getModel() {
        return (DefaultTableModel) super.getModel();
    }

    @Override
    public void setModel(@Nonnull final TableModel dataModel) {
        if (!(dataModel instanceof DefaultTableModel)) {
            throw new IllegalArgumentException(
                    "Row sorter must be of type DefaultTableModel");
        }
        super.setModel(dataModel);
    }

    @Override
    public AddonSorter getRowSorter() {
        return (AddonSorter) super.getRowSorter();
    }

    @Override
    public void setRowSorter(final RowSorter<? extends TableModel> sorter) {
        if (sorter == null || sorter instanceof AddonSorter) {
            super.setRowSorter(sorter);
        } else {
            throw new IllegalArgumentException(
                    "Row sorter must be of type AddonSorter");
        }
    }

}
