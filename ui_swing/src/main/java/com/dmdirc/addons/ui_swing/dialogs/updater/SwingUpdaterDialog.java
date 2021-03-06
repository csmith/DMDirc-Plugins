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

package com.dmdirc.addons.ui_swing.dialogs.updater;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.PackingTable;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.DialogModule.ForUpdates;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.updater.manager.UpdateManagerListener;
import com.dmdirc.updater.manager.UpdateManagerStatus;
import com.dmdirc.updater.manager.UpdateStatus;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * The updater dialog informs the user of the new update that is available, and walks them through
 * the process of downloading the update.
 */
public class SwingUpdaterDialog extends StandardDialog implements
        ActionListener, UpdateManagerListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 3;
    /** The update manager to use. */
    private final CachingUpdateManager updateManager;
    /** Update table. */
    private JTable table;
    /** Table scrollpane. */
    private JScrollPane scrollPane;
    /** The label we use for the dialog header. */
    private TextLabel header;
    /** UpdateComponent renderer. */
    private UpdateComponentTableCellRenderer updateComponentRenderer;
    /** Update.Status renderer. */
    private UpdateStatusTableCellRenderer updateStatusRenderer;
    /** Provider of restart dialogs. */
    private final DialogProvider<SwingRestartDialog> restartDialogProvider;
    /** The event bus to post errors to. */
    private final EventBus eventBus;

    /**
     * Creates a new instance of the updater dialog.
     *
     * @param updateManager         The update manager to use for information
     * @param parentWindow          Parent window
     * @param restartDialogProvider Provider of restart dialogs.
     * @param eventBus              The event bus to post errors to
     */
    @Inject
    public SwingUpdaterDialog(
            final CachingUpdateManager updateManager,
            @MainWindow final Window parentWindow,
            @ForUpdates final DialogProvider<SwingRestartDialog> restartDialogProvider,
            final EventBus eventBus) {
        super(parentWindow, ModalityType.MODELESS);

        this.updateManager = updateManager;
        this.restartDialogProvider = restartDialogProvider;
        this.eventBus = eventBus;

        initComponents();
        layoutComponents();

        updateManager.addUpdateManagerListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        setTitle("Update available");
        setSize(new Dimension(450, 400));
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        updateStatusRenderer = new UpdateStatusTableCellRenderer();
        updateComponentRenderer = new UpdateComponentTableCellRenderer();

        header = new TextLabel("An update is available for one or more "
                + "components of DMDirc:");

        final List<UpdateComponent> updates = updateManager.getComponents().stream()
                .filter(component -> updateManager.getStatus(component) != UpdateStatus.IDLE
                        && updateManager.getStatus(component) != UpdateStatus.CHECKING_NOT_PERMITTED)
                .collect(Collectors.toList());

        scrollPane = new JScrollPane();
        table = new PackingTable(new UpdateTableModel(updateManager, updates), scrollPane) {
            /** Serialisation version ID. */
            private static final long serialVersionUID = 1;

            @Override
            public TableCellRenderer getCellRenderer(final int row,
                    final int column) {
                switch (column) {
                    case 1:
                        return updateComponentRenderer;
                    case 3:
                        return updateStatusRenderer;
                    default:
                        return super.getCellRenderer(row, column);
                }
            }
        };

        table.setAutoCreateRowSorter(true);
        table.setAutoCreateColumnsFromModel(true);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFillsViewportHeight(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getRowSorter().toggleSortOrder(0);

        scrollPane.setViewportView(table);

        orderButtons(new JButton(), new JButton());
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wmin 450, hmin 400, wmax 450, hmax 400, "
                + "hidemode 3"));

        add(header, "wrap 1.5*unrel, growx, pushx");
        add(scrollPane, "grow, push, wrap");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(getOkButton())) {
            getOkButton().setEnabled(false);
            getCancelButton().setVisible(false);

            header.setText("DMDirc is updating the following components:");

            UIUtilities.invokeOffEDT(
                    () -> ((UpdateTableModel) table.getModel()).getUpdates().stream()
                    .filter(((UpdateTableModel) table.getModel())::isEnabled)
                    .forEach(updateManager::install));

            if (updateManager.getManagerStatus() == UpdateManagerStatus.IDLE_RESTART_NEEDED) {
                restartDialogProvider.displayOrRequestFocus();
                dispose();
            }
        } else if (e.getSource().equals(getCancelButton())) {
            dispose();
        }
    }

    @Override
    public boolean enterPressed() {
        executeAction(getOkButton());
        return true;
    }

    @Override
    public void updateManagerStatusChanged(final UpdateManager manager,
            final UpdateManagerStatus status) {
        UIUtilities.invokeLater(() -> {
            getOkButton().setEnabled(status != UpdateManagerStatus.WORKING);

            if (status == UpdateManagerStatus.IDLE_RESTART_NEEDED) {
                if (isVisible()) {
                    restartDialogProvider.displayOrRequestFocus();
                }
                dispose();
            } else {
                getCancelButton().setVisible(true);
            }
        });
    }

}
