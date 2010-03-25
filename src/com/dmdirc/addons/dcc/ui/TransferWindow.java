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

package com.dmdirc.addons.dcc.ui;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.dcc.TransferContainer;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.addons.ui_swing.SwingController;
import net.miginfocom.swing.MigLayout;

/**
 * A window for displaying the progress of DCC transfers.
 *
 * @author chris
 * @since 0.6.4
 */
public class TransferWindow extends EmptyWindow {

    /** A version number for this class. */
    private static final long serialVersionUID = 1l;

    /**
     * Creates a new transfer window for the specified UI controller and owner.
     *
     * @param controller The UIController that owns this window
     * @param owner The frame container that owns this window
     */
    public TransferWindow(SwingController controller, FrameContainer<?> owner) {
        super(controller, owner);
        
        final TransferContainer container = (TransferContainer) owner;
        final DCCTransfer dcc = container.getDCC();

        setLayout(new MigLayout("hidemode 0"));

        if (dcc.getType() == DCCTransfer.TransferType.SEND) {
            add(new JLabel("Sending: " + dcc.getShortFileName()), "wrap");
            add(new JLabel("To: " + targetNick), "wrap");
        } else {
            getContentPane().add(new JLabel("Recieving: " + dcc.getShortFileName()), "wrap");
            getContentPane().add(new JLabel("From: " + targetNick), "wrap");
        }
        getContentPane().add(status, "wrap");
        getContentPane().add(speed, "wrap");
        getContentPane().add(remaining, "wrap");
        getContentPane().add(taken, "wrap");
        getContentPane().add(progress, "growx, wrap");

        button.addActionListener(this);
        openButton.addActionListener(this);
        openButton.setVisible(false);

        getContentPane().add(openButton, "split 2, align right");
        getContentPane().add(button, "align right");
    }

}
