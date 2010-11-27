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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.Main;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
import com.dmdirc.addons.ui_swing.components.SplitPane;
import com.dmdirc.addons.ui_swing.components.desktopPane.DMDircDesktopPane;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
import com.dmdirc.addons.ui_swing.framemanager.FramemanagerPosition;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.CoreUIUtils;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.interfaces.MainWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.util.ReturnableThread;

import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.MenuSelectionManager;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * The main application frame.
 */
public final class MainFrame extends JFrame implements WindowListener,
        MainWindow, ConfigChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    /** The main application icon. */
    private ImageIcon imageIcon;
    /** The frame manager that's being used. */
    private FrameManager mainFrameManager;
    /** Dekstop pane. */
    private DMDircDesktopPane desktopPane;
    /** Main panel. */
    private JPanel frameManagerPanel;
    /** Frame manager position. */
    private FramemanagerPosition position;
    /** Show version? */
    private boolean showVersion;
    /** Exit code. */
    private int exitCode = 0;
    /** Swing Controller. */
    private final SwingController controller;
    /** Status bar. */
    private SwingStatusBar statusBar;
    /** Client Version. */
    private final String version;
    /** Main split pane. */
    private SplitPane mainSplitPane;

    /**
     * Creates new form MainFrame.
     *
     * @param controller Swing controller
     */
    public MainFrame(final SwingController controller) {
        super();

        this.controller = controller;

        initComponents();

        imageIcon = new ImageIcon(IconManager.getIconManager().getImage("icon"));
        setIconImage(imageIcon.getImage());

        CoreUIUtils.centreWindow(this);

        addWindowListener(this);

        showVersion = IdentityManager.getGlobalConfig().getOptionBool("ui",
                "showversion");
        version = IdentityManager.getGlobalConfig().getOption("version",
                "version");
        IdentityManager.getGlobalConfig().addChangeListener("ui", "lookandfeel",
                this);
        IdentityManager.getGlobalConfig().addChangeListener("ui", "showversion",
                this);
        IdentityManager.getGlobalConfig().addChangeListener("ui",
                "framemanager", this);
        IdentityManager.getGlobalConfig().addChangeListener("ui",
                "framemanagerPosition", this);
        IdentityManager.getGlobalConfig().addChangeListener("icon", "icon",
                this);


        addWindowFocusListener(new WindowFocusListener() {

            /** {@inheritDoc} */
            @Override
            public void windowGainedFocus(final WindowEvent e) {
                ActionManager.processEvent(CoreActionType.CLIENT_FOCUS_GAINED,
                        null);
            }

            /** {@inheritDoc} */
            @Override
            public void windowLostFocus(final WindowEvent e) {
                ActionManager.processEvent(CoreActionType.CLIENT_FOCUS_LOST,
                        null);
                //TODO: Remove me when we switch to java7
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        });

        setTitle(getTitlePrefix());
    }

    /**
     * Returns the status bar for this frame.
     *
     * @return Status bar
     */
    public SwingStatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Returns the size of the frame manager.
     *
     * @return Frame manager size.
     */
    public int getFrameManagerSize() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Integer>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (position == FramemanagerPosition.LEFT || position
                        == FramemanagerPosition.RIGHT) {
                    setObject(frameManagerPanel.getWidth());
                } else {
                    setObject(frameManagerPanel.getHeight());
                }
            }
        });
    }

    /** {@inheritDoc}. */
    @Override
    public ImageIcon getIcon() {
        return UIUtilities.invokeAndWait(new ReturnableThread<ImageIcon>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(imageIcon);
            }
        });
    }

    /**
     * Returns the window that is currently active.
     *
     * @return The active window
     */
    public Window getActiveFrame() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Window>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(desktopPane.getSelectedWindow());
            }
        });
    }

    /** {@inheritDoc */
    @Override
    public MenuBar getJMenuBar() {
        return (MenuBar) super.getJMenuBar();
    }

    /** {@inheritDoc}. */
    @Override
    public void setMaximised(final boolean max) {
        //Ignore
    }

    /** {@inheritDoc}. */
    @Override
    public void setTitle(final String title) {
        if (title != null && getActiveFrame() != null && getActiveFrame().
                isMaximum()) {
            super.setTitle(getTitlePrefix() + " - " + title);
        } else {
            super.setTitle(getTitlePrefix());
        }
    }

    /** {@inheritDoc}. */
    @Override
    public String getTitlePrefix() {
        return "DMDirc" + (showVersion ? " " + version : "");
    }

    /** {@inheritDoc}. */
    @Override
    public boolean getMaximised() {
        return UIUtilities.invokeAndWait(new ReturnableThread<Boolean>() {

            /** {@inheritDoc}. */
            @Override
            public void run() {
                final Window window = getActiveFrame();
                if (window == null) {
                    setObject(false);
                } else {
                    setObject(getActiveFrame().isMaximum());
                }
            }
        });
    }

    /**
     * Returns the desktop pane for the frame.
     *
     * @return JDesktopPane for the frame
     */
    public DMDircDesktopPane getDesktopPane() {
        return desktopPane;
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowOpened(final WindowEvent windowEvent) {
        //ignore
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowClosing(final WindowEvent windowEvent) {
        quit(exitCode);
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowClosed(final WindowEvent windowEvent) {
        new Thread(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                Main.quit(exitCode);
            }
        }, "Quit thread").start();
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowIconified(final WindowEvent windowEvent) {
        ActionManager.processEvent(CoreActionType.CLIENT_MINIMISED, null);
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowDeiconified(final WindowEvent windowEvent) {
        ActionManager.processEvent(CoreActionType.CLIENT_UNMINIMISED, null);
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowActivated(final WindowEvent windowEvent) {
        //ignore
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowDeactivated(final WindowEvent windowEvent) {
        //ignore
    }

    /** Initialiases the frame managers. */
    private void initFrameManagers() {
        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                frameManagerPanel.removeAll();
                final String manager = IdentityManager.getGlobalConfig().
                        getOption("ui", "framemanager");
                try {
                    mainFrameManager = (FrameManager) Class.forName(manager).
                            getConstructor().newInstance();
                } catch (InvocationTargetException ex) {
                    Logger.appError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } catch (InstantiationException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } catch (NoSuchMethodException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } catch (SecurityException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } catch (IllegalAccessException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } catch (IllegalArgumentException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } catch (ClassNotFoundException ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } catch (LinkageError ex) {
                    Logger.userError(ErrorLevel.MEDIUM, "Unable to load frame "
                            + "manager, falling back to default.", ex);
                } finally {
                    if (mainFrameManager == null) {
                        mainFrameManager = new TreeFrameManager();
                    }
                }
                mainFrameManager.setController(controller);
                mainFrameManager.setParent(frameManagerPanel);
                controller.getWindowFactory().addWindowListener(
                        mainFrameManager);
            }
        });
    }

    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        statusBar = new SwingStatusBar(controller, this);
        frameManagerPanel = new JPanel();
        desktopPane = new DMDircDesktopPane(controller, this, controller.
                getDomain());
        mainSplitPane = new SplitPane(SplitPane.Orientation.HORIZONTAL);

        initFrameManagers();

        final MenuBar menu = new MenuBar(controller, this);
        Apple.getApple().setMenuBar(menu);
        setJMenuBar(menu);

        setPreferredSize(new Dimension(800, 600));

        getContentPane().setLayout(new MigLayout(
                "fill, ins rel, wrap 1, hidemode 2"));
        getContentPane().add(initSplitPane(mainSplitPane), "grow, push");
        getContentPane().add(statusBar,
                "hmax 20, wmax 100%-2*rel, wmin 100%-2*rel");

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        pack();
    }

    /**
     * Initialises the split pane.
     *
     * @param mainSplitPane Split pane to initialise
     *
     * @return Returns the initialised split pane
     */
    private JSplitPane initSplitPane(final SplitPane mainSplitPane) {
        position = FramemanagerPosition.getPosition(IdentityManager.
                getGlobalConfig().getOption("ui", "framemanagerPosition"));

        if (position == FramemanagerPosition.UNKNOWN) {
            position = FramemanagerPosition.LEFT;
        }

        if (!mainFrameManager.canPositionVertically() && (position
                == FramemanagerPosition.LEFT || position
                == FramemanagerPosition.RIGHT)) {
            position = FramemanagerPosition.BOTTOM;
        }
        if (!mainFrameManager.canPositionHorizontally() && (position
                == FramemanagerPosition.TOP || position
                == FramemanagerPosition.BOTTOM)) {
            position = FramemanagerPosition.LEFT;
        }

        switch (position) {
            case TOP:
                mainSplitPane.setTopComponent(frameManagerPanel);
                mainSplitPane.setBottomComponent(desktopPane);
                mainSplitPane.setResizeWeight(0.0);
                mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        Integer.MAX_VALUE, IdentityManager.getGlobalConfig().
                        getOptionInt("ui", "frameManagerSize")));
                break;
            case LEFT:
                mainSplitPane.setLeftComponent(frameManagerPanel);
                mainSplitPane.setRightComponent(desktopPane);
                mainSplitPane.setResizeWeight(0.0);
                mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        IdentityManager.getGlobalConfig().getOptionInt("ui",
                        "frameManagerSize"), Integer.MAX_VALUE));
                break;
            case BOTTOM:
                mainSplitPane.setTopComponent(desktopPane);
                mainSplitPane.setBottomComponent(frameManagerPanel);
                mainSplitPane.setResizeWeight(1.0);
                mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        Integer.MAX_VALUE, IdentityManager.getGlobalConfig().
                        getOptionInt("ui", "frameManagerSize")));
                break;
            case RIGHT:
                mainSplitPane.setLeftComponent(desktopPane);
                mainSplitPane.setRightComponent(frameManagerPanel);
                mainSplitPane.setResizeWeight(1.0);
                mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        IdentityManager.getGlobalConfig().getOptionInt("ui",
                        "frameManagerSize"), Integer.MAX_VALUE));
                break;
            default:
                break;
        }

        return mainSplitPane;
    }

    /** {@inheritDoc}. */
    @Override
    public void quit() {
        quit(0);
    }

    /**
     * Exit code call to quit.
     *
     * @param exitCode Exit code
     */
    public void quit(final int exitCode) {
        if (exitCode == 0 && IdentityManager.getGlobalConfig().getOptionBool(
                "ui", "confirmQuit")) {
            new StandardQuestionDialog(this, ModalityType.APPLICATION_MODAL,
                    "Quit confirm",
                    "You are about to quit DMDirc, are you sure?") {

                /**
                 * A version number for this class. It should be changed
                 * whenever the class structure is changed (or anything else
                 * that would prevent serialized objects being unserialized
                 * with the new class).
                 */
                private static final long serialVersionUID = 1;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    doQuit(exitCode);
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    // Do nothing
                }
            }.display();
            return;
        }
        doQuit(exitCode);
    }

    /**
     * Exit code call to quit.
     *
     * @param exitCode Exit code
     */
    private void doQuit(final int exitCode) {
        this.exitCode = exitCode;

        new LoggingSwingWorker() {

            /** {@inheritDoc} */
            @Override
            protected Object doInBackground() {
                ActionManager.processEvent(CoreActionType.CLIENT_CLOSING, null);
                ServerManager.getServerManager().closeAll(IdentityManager.
                        getGlobalConfig().getOption("general", "closemessage"));
                IdentityManager.getConfigIdentity().setOption("ui",
                        "frameManagerSize",
                        String.valueOf(getFrameManagerSize()));
                return null;
            }

            /** {@inheritDoc} */
            @Override
            protected void done() {
                super.done();
                dispose();
            }
        }.executeInExecutor();
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if ("ui".equals(domain)) {
            if ("lookandfeel".equals(key)) {
                controller.updateLookAndFeel();
            } else if ("framemanager".equals(key) || "framemanagerPosition".equals(key)) {
                UIUtilities.invokeLater(new Runnable() {

                    /** {@inheritDoc} */
                    @Override
                    public void run() {
                        initFrameManagers();
                        initSplitPane(mainSplitPane);
                        frameManagerPanel.repaint();
                    }
                });
            } else {
                showVersion = IdentityManager.getGlobalConfig().getOptionBool(
                        "ui", "showversion");
            }
        } else {
            imageIcon = new ImageIcon(IconManager.getIconManager().getImage(
                    "icon"));
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    setIconImage(imageIcon.getImage());
                }
            });
        }
    }
}
