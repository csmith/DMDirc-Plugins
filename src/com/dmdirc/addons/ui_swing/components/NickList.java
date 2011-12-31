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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.components.renderers.NicklistRenderer;
import com.dmdirc.addons.ui_swing.textpane.ClickType;
import com.dmdirc.addons.ui_swing.textpane.ClickTypeValue;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.NicklistListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Nicklist class.
 */
public class NickList extends JScrollPane implements ConfigChangeListener,
        MouseListener, NicklistListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 10;
    /** Nick list. */
    private final JList nickList;
    /** Parent frame. */
    private final ChannelFrame frame;
    /** Config. */
    private final ConfigManager config;
    /** Nick list model. */
    private final NicklistListModel nicklistModel;

    /**
     * Creates a nicklist.
     *
     * @param frame Frame
     * @param config Config
     */
    public NickList(final ChannelFrame frame, final ConfigManager config) {
        super();
        this.frame = frame;
        this.config = config;

        nickList = new JList();

        nickList.setBackground(UIUtilities.convertColour(
                config.getOptionColour(
                "ui", "nicklistbackgroundcolour",
                "ui", "backgroundcolour")));
        nickList.setForeground(UIUtilities.convertColour(
                config.getOptionColour(
                "ui", "nicklistforegroundcolour",
                "ui", "foregroundcolour")));
        nickList.setFont(new Font(config.getOption("ui", "textPaneFontName"),
                Font.PLAIN, getFont().getSize()));
        config.addChangeListener("ui", "nicklistforegroundcolour", this);
        config.addChangeListener("ui", "foregroundcolour", this);
        config.addChangeListener("ui", "nicklistbackgroundcolour", this);
        config.addChangeListener("ui", "backgroundcolour", this);
        config.addChangeListener("ui", "nickListAltBackgroundColour", this);
        config.addChangeListener("ui", "textPaneFontName", this);

        nickList.setCellRenderer(new NicklistRenderer(config, nickList));
        nickList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        nickList.addMouseListener(this);

        nicklistModel = new NicklistListModel(config);

        nickList.setModel(nicklistModel);
        setViewportView(nickList);

        final int splitPanePosition = config.getOptionInt("ui",
                "channelSplitPanePosition");
        setPreferredSize(new Dimension(splitPanePosition, 0));
        setMinimumSize(new Dimension(75, 0));

        ((Channel) frame.getContainer()).addNicklistListener(this);
        clientListUpdated(((Channel) frame.getContainer()).getChannelInfo()
                .getChannelClients());
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        processMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        processMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        processMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    /**
     * Processes every mouse button event to check for a popup trigger.
     *
     * @param e mouse event
     */
    @Override
    public void processMouseEvent(final MouseEvent e) {
        if (!e.isPopupTrigger()
                || e.getSource() != nickList
                || nickList.getMousePosition() == null) {
            return;
        }
        if (checkCursorInSelectedCell() || selectNickUnderCursor()) {
            final Object[] values = nickList.getSelectedValues();
            final StringBuilder builder = new StringBuilder();

            for (Object value : values) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }

                builder.append(((ChannelClientInfo) value).getClient()
                        .getNickname());
            }

            frame.showPopupMenu(new ClickTypeValue(ClickType.NICKNAME,
                    builder.toString()), new Point(e.getXOnScreen(),
                    e.getYOnScreen()));
        } else {
            nickList.clearSelection();
        }

        super.processMouseEvent(e);
    }

    /**
     * Checks whether the mouse cursor is currently over a cell in the nicklist
     * which has been previously selected.
     *
     * @return True if the cursor is over a selected cell, false otherwise
     */
    private boolean checkCursorInSelectedCell() {
        boolean showMenu = false;
        final Point mousePos = nickList.getMousePosition();
        if (mousePos != null) {
            for (int i = 0; i < nickList.getModel().getSize(); i++) {
                if (nickList.getCellBounds(i, i) != null && nickList.
                        getCellBounds(i, i).
                        contains(mousePos) && nickList.isSelectedIndex(i)) {
                    showMenu = true;
                    break;
                }
            }
        }
        return showMenu;
    }

    /**
     * If the mouse cursor is over a nick list cell, sets that cell to be
     * selected and returns true. If the mouse is not over any cell, the
     * selection is unchanged and the method returns false.
     *
     * @return True if an item was selected
     */
    private boolean selectNickUnderCursor() {
        boolean suceeded = false;
        final Point mousePos = nickList.getMousePosition();
        if (mousePos != null) {
            for (int i = 0; i < nickList.getModel().getSize(); i++) {
                if (nickList.getCellBounds(i, i) != null && nickList.
                        getCellBounds(i, i).
                        contains(mousePos)) {
                    nickList.setSelectedIndex(i);
                    suceeded = true;
                    break;
                }
            }
        }
        return suceeded;
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("nickListAltBackgroundColour".equals(key)
                || "nicklistbackgroundcolour".equals(key)
                || "backgroundcolour".equals(key)
                || "nicklistforegroundcolour".equals(key)
                || "foregroundcolour".equals(key)
                || "textPaneFontName".equals(key)) {
            nickList.setBackground(UIUtilities.convertColour(
                    config.getOptionColour(
                    "ui", "nicklistbackgroundcolour",
                    "ui", "backgroundcolour")));
            nickList.setForeground(UIUtilities.convertColour(
                    config.getOptionColour(
                    "ui", "nicklistforegroundcolour",
                    "ui", "foregroundcolour")));
            nickList.setFont(new Font(config.getOption("ui", "textPaneFontName"),
                Font.PLAIN, getFont().getSize()));
            nickList.repaint();
        }
        nickList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    /** {@inheritDoc} */
    @Override
    public void clientListUpdated(final Collection<ChannelClientInfo> clients) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                nicklistModel.replace(clients);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void clientListUpdated() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                nicklistModel.sort();
                repaint();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void clientAdded(final ChannelClientInfo client) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                nicklistModel.add(client);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void clientRemoved(final ChannelClientInfo client) {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                nicklistModel.remove(client);
            }
        });
    }
}

