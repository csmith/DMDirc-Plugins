/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.events.FrameComponentAddedEvent;
import com.dmdirc.events.FrameComponentRemovedEvent;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.core.components.WindowComponent;

import javax.swing.JLabel;

import net.engio.mbassy.listener.Handler;

/**
 * Simple panel to show when a user is typing.
 */
public class TypingLabel extends JLabel implements ConfigChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** typingindicator string for compiler optimisation. */
    private static final String CONFIG_KEY = "typingindicator";
    /** Parent frame container. */
    private final FrameContainer container;
    /** Whether or not to show the typing indicator. */
    private boolean useTypingIndicator;

    /**
     * Creates a new typing label for the specified container.
     *
     * @param container Parent frame container
     */
    public TypingLabel(final FrameContainer container) {
        super("[Typing...]");
        this.container = container;
        container.getConfigManager().addChangeListener("ui", CONFIG_KEY, this);
        setVisible(false);
        useTypingIndicator = container.getConfigManager().getOptionBool("ui", CONFIG_KEY);
        if (container.getComponents().contains(WindowComponent.TYPING_INDICATOR.getIdentifier())) {
            setVisible(true);
        }
    }

    @Override
    public void configChanged(final String domain, final String key) {
        useTypingIndicator = container.getConfigManager().getOptionBool("ui", CONFIG_KEY);
        if (!useTypingIndicator) {
            UIUtilities.invokeLater(() -> setVisible(false));
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void componentAdded(final FrameComponentAddedEvent event) {
        if (event.getContainer().equals(container) &&
                WindowComponent.TYPING_INDICATOR.getIdentifier().equals(event.getComponent()) &&
                useTypingIndicator) {
            setVisible(true);
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void componentRemoved(final FrameComponentRemovedEvent event) {
        if (event.getContainer().equals(container) &&
                WindowComponent.TYPING_INDICATOR.getIdentifier().equals(event.getComponent()) &&
                useTypingIndicator) {
            setVisible(false);
        }
    }

}
