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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import net.miginfocom.swing.MigLayout;

/**
 * A window for displaying the progress of DCC transfers.
 *
 * @author chris
 * @since 0.6.4
 */
public class TransferWindow extends EmptyWindow implements ActionListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1l;

    /** Progress Bar */
    private final JProgressBar progress = new JProgressBar();

    /** Status Label */
    private final JLabel status = new JLabel("Status: Waiting");

    /** Speed Label */
    private final JLabel speed = new JLabel("Speed: Unknown");

    /** Time Label */
    private final JLabel remaining = new JLabel("Time Remaining: Unknown");

    /** Time Taken */
    private final JLabel taken = new JLabel("Time Taken: 00:00");

    /** Button */
    private final JButton button = new JButton("Cancel");

    /** Open Button */
    private final JButton openButton = new JButton("Open");

    /**
     * Creates a new transfer window for the specified UI controller and owner.
     *
     * @param controller The UIController that owns this window
     * @param owner The frame container that owns this window
     */
    public TransferWindow(final SwingController controller, final FrameContainer<?> owner) {
        super(controller, owner);
        
        final TransferContainer container = (TransferContainer) owner;
        final DCCTransfer dcc = container.getDCC();

        setLayout(new MigLayout("hidemode 0"));

        if (dcc.getType() == DCCTransfer.TransferType.SEND) {
            add(new JLabel("Sending: " + dcc.getShortFileName()), "wrap");
            add(new JLabel("To: " + container.getOtherNickname()), "wrap");
        } else {
            add(new JLabel("Recieving: " + dcc.getShortFileName()), "wrap");
            add(new JLabel("From: " + container.getOtherNickname()), "wrap");
        }

        add(status, "wrap");
        add(speed, "wrap");
        add(remaining, "wrap");
        add(taken, "wrap");
        add(progress, "growx, wrap");

        button.addActionListener(this);
        openButton.addActionListener(this);
        openButton.setVisible(false);

        add(openButton, "split 2, align right");
        add(button, "align right");
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
