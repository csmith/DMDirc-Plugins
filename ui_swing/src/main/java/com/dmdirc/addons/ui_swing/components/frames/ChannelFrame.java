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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.components.NickList;
import com.dmdirc.addons.ui_swing.components.SplitPane;
import com.dmdirc.addons.ui_swing.components.TopicBar;
import com.dmdirc.addons.ui_swing.components.TopicBarFactory;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.config.binding.ConfigBinder;
import com.dmdirc.config.binding.ConfigBinding;
import com.dmdirc.events.ClientClosingEvent;
import com.dmdirc.events.FrameClosingEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Dimension;

import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

import net.engio.mbassy.listener.Handler;

import static com.dmdirc.addons.ui_swing.SwingPreconditions.checkOnEDT;

/**
 * The groupChat frame is the GUI component that represents a groupChat to the user.
 */
public final class ChannelFrame extends InputTextFrame {

    /** A version number for this class. */
    private static final long serialVersionUID = 10;
    /** Identity. */
    private final ConfigProvider identity;
    /** split pane. */
    private SplitPane splitPane;
    /** popup menu item. */
    private JMenuItem settingsMI;
    /** Nicklist. */
    private NickList nicklist;
    /** Topic bar. */
    private TopicBar topicBar;
    /** Event bus to dispatch events on. */
    private final EventBus eventBus;
    /** Config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Channel settings dialog provider. */
    private final KeyedDialogProvider<GroupChat, ChannelSettingsDialog> dialogProvider;
    /** Group chat instance. */
    private final GroupChat groupChat;
    /** Config binder. */
    private final ConfigBinder binder;

    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers, and default options
     * for the form.
     *
     * @param deps               The dependencies required by text frames.
     * @param inputFieldProvider The provider to use to create a new input field.
     * @param identityFactory    The factory to use to create a group chat identity.
     * @param topicBarFactory    The factory to use to create topic bars.
     * @param owner              The group chat object that owns this frame
     * @param domain             The domain to read settings from
     * @param dialogProvider     The dialog provider to get the group chat settings dialog from.
     */
    public ChannelFrame(
            final String domain,
            final TextFrameDependencies deps,
            final Provider<SwingInputField> inputFieldProvider,
            final IdentityFactory identityFactory,
            final KeyedDialogProvider<GroupChat, ChannelSettingsDialog> dialogProvider,
            final InputTextFramePasteActionFactory inputTextFramePasteActionFactory,
            final TopicBarFactory topicBarFactory,
            final GroupChat owner) {
        super(deps, inputFieldProvider, inputTextFramePasteActionFactory, owner.getWindowModel());

        this.eventBus = deps.eventBus;
        this.globalConfig = deps.globalConfig;
        this.dialogProvider = dialogProvider;
        this.groupChat = owner;

        initComponents(topicBarFactory, deps.colourManagerFactory);
        binder = getContainer().getConfigManager().getBinder().withDefaultDomain(domain);

        identity = identityFactory.createChannelConfig(owner.getConnection().get().getNetwork(),
                owner.getName());
    }

    /**
     * Initialises the instance, adding any required listeners.
     */
    @Override
    public void init() {
        binder.bind(this, ChannelFrame.class);
        eventBus.subscribe(this);
        super.init();
    }

    /**
     * Initialises the components in this frame.
     *
     * @param topicBarFactory The factory to use to produce topic bars.
     * @param colourManagerFactory The colour manager factory
     */
    private void initComponents(final TopicBarFactory topicBarFactory,
            final ColourManagerFactory colourManagerFactory) {
        topicBar = topicBarFactory.getTopicBar((GroupChat) getContainer(), this);

        nicklist = new NickList(this, getContainer().getConfigManager(), colourManagerFactory);
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(l -> dialogProvider.displayOrRequestFocus(groupChat));

        splitPane = new SplitPane(globalConfig, SplitPane.Orientation.HORIZONTAL);

        setLayout(new MigLayout("fill, ins 0, hidemode 3, wrap 1"));

        add(topicBar, "growx");
        add(splitPane, "grow, push");
        add(getSearchBar(), "growx");
        add(inputPanel, "growx");

        splitPane.setLeftComponent(getTextPane());
        splitPane.setResizeWeight(1);
        splitPane.setDividerLocation(-1);
    }

    @ConfigBinding(domain = "ui", key = "channelSplitPanePosition",
            invocation = EDTInvocation.class)
    public void handleSplitPanePosition(final int value) {
        checkOnEDT();
        nicklist.setPreferredSize(new Dimension(value, 0));
        splitPane.setDividerLocation(splitPane.getWidth() - splitPane.getDividerSize() - value);
    }

    @ConfigBinding(key = "shownicklist", invocation = EDTInvocation.class)
    public void handleShowNickList(final boolean value) {
        checkOnEDT();
        if (value) {
            splitPane.setRightComponent(nicklist);
        } else {
            splitPane.setRightComponent(null);
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleClientClosing(final ClientClosingEvent event) {
        saveSplitPanePosition();
    }

    private void saveSplitPanePosition() {
        checkOnEDT();
        if (getContainer().getConfigManager().getOptionInt("ui",
                "channelSplitPanePosition") != nicklist.getWidth()) {
            identity.setOption("ui", "channelSplitPanePosition", nicklist.getWidth());
        }
    }

    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.CHAN_NICK;
    }

    @Override
    public PopupType getChannelPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.CHAN_HYPERLINK;
    }

    @Override
    public PopupType getNormalPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        if (groupChat.getConnection().get().getState() == ServerState.CONNECTED) {
            settingsMI.setEnabled(true);
        } else {
            settingsMI.setEnabled(false);
        }
        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }
        popupMenu.add(settingsMI);
    }

    @Override
    @Handler(invocation = EdtHandlerInvocation.class)
    public void windowClosing(final FrameClosingEvent event) {
        if (event.getSource().equals(getContainer())) {
            saveSplitPanePosition();
            topicBar.close();
            dialogProvider.dispose(groupChat);
            super.windowClosing(event);
        }
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(this);
        binder.unbind(this);
        super.dispose();
    }

}
