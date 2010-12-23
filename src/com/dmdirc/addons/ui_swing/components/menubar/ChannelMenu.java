/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.ChannelJoinDialog;
import com.dmdirc.ui.WindowManager;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * A menu to provide channel related commands in the menu bar.
 */
public class ChannelMenu extends JMenu implements ActionListener,
        MenuListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Swing controller. */
    private final SwingController controller;
    /** Main frame. */
    private final MainFrame mainFrame;
    /** Menu items to be disabled/enabled. */
    private JMenuItem csd, join;

    /**
     * Creates a new channel menu.
     *
     * @param controller Parent swing controller.
     * @param mainFrame Parent mainframe
     */
    public ChannelMenu(final SwingController controller,
            final MainFrame mainFrame) {
        super("Channel");
        this.controller = controller;
        this.mainFrame = mainFrame;
        setMnemonic('c');
        addMenuListener(this);
        initChannelMenu();
        menuSelected(null);
    }

    /**
     * Initialises the channel menu.
     */
    private void initChannelMenu() {
        join = new JMenuItem();
        join.setText("Join Channel...");
        join.setMnemonic('j');
        join.setActionCommand("JoinChannel");
        join.addActionListener(this);
        add(join);

        csd = new JMenuItem();
        csd.setMnemonic('c');
        csd.setText("Channel Settings");
        csd.setActionCommand("ChannelSettings");
        csd.addActionListener(this);
        add(csd);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("JoinChannel")) {
            new ChannelJoinDialog(mainFrame, ModalityType.MODELESS,
                    "Join channel", "Enter the name of the channel to join.")
                    .display();
        } else if (e.getActionCommand().equals("ChannelSettings")) {
            final FrameContainer<?> activeWindow = WindowManager
                    .getActiveWindow();
            if (activeWindow instanceof Channel) {
                controller.showChannelSettingsDialog(((Channel) activeWindow));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void menuSelected(final MenuEvent e) {
        final FrameContainer<?> activeWindow = WindowManager.getActiveWindow();

        join.setEnabled(activeWindow != null && activeWindow.getServer()
                != null && activeWindow.getServer().getState()
                == ServerState.CONNECTED);
        csd.setEnabled(activeWindow instanceof Channel && activeWindow
                .getServer() != null && activeWindow.getServer().getState()
                == ServerState.CONNECTED);
    }

    /** {@inheritDoc} */
    @Override
    public final void menuDeselected(final MenuEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public final void menuCanceled(final MenuEvent e) {
        //Ignore
    }
}