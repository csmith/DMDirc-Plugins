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

package com.dmdirc.addons.userlevel;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.plugins.BasePlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows the client to assign user levels to users (based on hostname matches),
 * and for actions/plugins to check those levels.
 */
public class UserLevelPlugin extends BasePlugin implements ActionListener,
        ConfigChangeListener {

    /** The domain used for userlevels. */
    private static final String DOMAIN = "userlevels";
    /** A map of hostmasks to associated level numbers. */
    private static final Map<String, Integer> LEVELS = new HashMap<String, Integer>();

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.CHANNEL_JOIN);
        ActionManager.getActionManager().registerComponents(
                new ActionComponent[]{
                    new AccessLevelComponent(),
                    new ChannelAccessLevelComponent()
                });
        IdentityManager.getIdentityManager().getGlobalConfiguration()
                .addChangeListener(DOMAIN, this);
        loadLevels();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        ActionManager.getActionManager().unregisterListener(this);
        IdentityManager.getIdentityManager().getGlobalConfiguration().removeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
                             final Object... arguments) {
        switch ((CoreActionType) type) {
            case CHANNEL_JOIN:
                doChannelLevel((ChannelClientInfo) arguments[1]);
                break;
        }
    }

    /**
     * Updates the specified channel client's channel user level.
     *
     * @param client The client whose user level is to be updated
     */
    protected static void doChannelLevel(final ChannelClientInfo client) {
        doGlobalLevel(client.getClient());
    }

    /**
     * Updates the specified client's global user level.
     *
     * @param client The client whose user level is to be updated
     */
    @SuppressWarnings("unchecked")
    protected static void doGlobalLevel(final ClientInfo client) {
        final String host = client.getNickname() + "!" + client.getUsername()
                + "@" + client.getHostname();

        int level = 0;

        synchronized (LEVELS) {
            for (Map.Entry<String, Integer> entry : LEVELS.entrySet()) {
                if (host.matches(entry.getKey())) {
                    level = Math.max(level, entry.getValue());
                }
            }
        }

        client.getMap().put("level", level);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (DOMAIN.equals(domain)) {
            loadLevels();
        }
    }

    /**
     * Loads all levels from the config file into our map.
     */
    private void loadLevels() {
        LEVELS.clear();

        for (Map.Entry<String, String> item : IdentityManager.getIdentityManager()
                .getGlobalConfiguration().getOptions(DOMAIN).entrySet()) {
            try {
                LEVELS.put(item.getKey(), Integer.parseInt(item.getValue()));
            } catch (NumberFormatException ex) {
                LEVELS.put(item.getKey(), 0);
            }
        }
    }

}
