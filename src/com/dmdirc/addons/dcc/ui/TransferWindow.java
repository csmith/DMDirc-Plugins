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
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.addons.dcc.TransferContainer;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

/**
 * A window for displaying the progress of DCC transfers.
 *
 * @author chris
 * @since 0.6.4
 */
public class TransferWindow extends EmptyWindow implements ActionListener,
        SocketCloseListener {

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

    /** The transfer that this window is showing. */
    private final DCCTransfer dcc;

    /**
     * Creates a new transfer window for the specified UI controller and owner.
     *
     * @param controller The UIController that owns this window
     * @param owner The frame container that owns this window
     */
    public TransferWindow(final SwingController controller, final FrameContainer<?> owner) {
        super(controller, owner);
        
        final TransferContainer container = (TransferContainer) owner;
        dcc = container.getDCC();

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
        if (e.getActionCommand().equals("Cancel")) {
            if (dcc.getType() == DCCTransfer.TransferType.SEND) {
                button.setText("Resend");
            } else {
                button.setText("Close Window");
            }
            status.setText("Status: Cancelled");
            dcc.close();
        } else if (e.getActionCommand().equals("Resend")) {
            button.setText("Cancel");
            status.setText("Status: Resending...");
            synchronized (this) {
                transferCount = 0;
            }
            dcc.reset();

            final Server server = ((TransferContainer) frameParent).getServer();

            if (server != null && server.getState() == ServerState.CONNECTED) {
                final String myNickname = server.getParser().getLocalClient().getNickname();
                // Check again incase we have changed nickname to the same nickname that
                // this send is for.
                if (server.getParser().getStringConverter().equalsIgnoreCase(
                        ((TransferContainer) frameParent).getOtherNickname(), myNickname)) {
                    final Thread errorThread = new Thread(new Runnable() {

                        /** {@inheritDoc} */
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null,
                                    "You can't DCC yourself.", "DCC Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    });
                    errorThread.start();
                    return;
                } else {
                    if (IdentityManager.getGlobalConfig().getOptionBool(
                            plugin.getDomain(), "send.reverse")) {
                        parser.sendCTCP(otherNickname, "DCC", "SEND \"" +
                                new File(dcc.getFileName()).getName() + "\" "
                                + DCC.ipToLong(myPlugin.getListenIP(parser))
                                + " 0 " + dcc.getFileSize() + " " + dcc.makeToken()
                                + ((dcc.isTurbo()) ? " T" : ""));
                        return;
                    } else if (plugin.listen(dcc)) {
                        parser.sendCTCP(otherNickname, "DCC", "SEND \""
                                + new File(dcc.getFileName()).getName() + "\" "
                                + DCC.ipToLong(myPlugin.getListenIP(parser)) + " "
                                + dcc.getPort() + " " + dcc.getFileSize()
                                + ((dcc.isTurbo()) ? " T" : ""));
                        return;
                    }
                }
            } else {
                status.setText("Status: Resend failed.");
                button.setText("Close Window");
            }
        } else if (e.getActionCommand().equals("Close Window")) {
            close();
        } else if (e.getSource() == openButton) {
            final File file = new File(dcc.getFileName());
            try {
                Desktop.getDesktop().open(file);
            } catch (IllegalArgumentException ex) {
                Logger.userError(ErrorLevel.LOW, "Unable to open file: " + file, ex);
                openButton.setEnabled(false);
            } catch (IOException ex) {
                try {
                    Desktop.getDesktop().open(file.getParentFile());
                } catch (IllegalArgumentException ex1) {
                    Logger.userError(ErrorLevel.LOW, "Unable to open folder: " +
                            file.getParentFile(), ex1);
                    openButton.setEnabled(false);
                } catch (IOException ex1) {
                    Logger.userError(ErrorLevel.LOW, "No associated handler " +
                            "to open file or directory.", ex1);
                    openButton.setEnabled(false);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onSocketClosed(final Parser parser, final Date date) {
        // Can't resend without the parser.
        if ("Resend".equals(button.getText())) {
            button.setText("Close Window");
        }
    }

}
