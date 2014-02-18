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

package com.dmdirc.addons.serverlistdialog;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.serverlists.ServerGroup;
import com.dmdirc.addons.serverlists.ServerGroupItem;
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.ui.IconManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

/**
 * Panel for listing and adding settings to the group item.
 */
public class Settings extends JPanel implements ServerListListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Server list model. */
    private final ServerListModel model;
    /** Settings panel. */
    private final Map<ServerGroupItem, SettingsPanel> panels = new HashMap<>();
    /** Platform border. */
    private final Border border;
    /** Manager to use to retrieve icons. */
    private final IconManager iconManager;
    /** Factory to use to produce preference components. */
    private final PrefsComponentFactory componentFactory;
    /** Factory to use to produce identities. */
    private final IdentityFactory identityFactory;

    /**
     * Instantiates a new settings panel.
     *
     * @param model            Backing model
     * @param iconManager      Manager to use to retrieve icons.
     * @param componentFactory Factory to use to produce preference components.
     * @param identityFactory  Factory to use to produce identities.
     */
    @Inject
    public Settings(
            final ServerListModel model,
            @GlobalConfig final IconManager iconManager,
            final PrefsComponentFactory componentFactory,
            final IdentityFactory identityFactory) {
        super();
        this.model = model;
        this.iconManager = iconManager;
        this.componentFactory = componentFactory;
        this.identityFactory = identityFactory;
        addListeners();
        border = UIManager.getBorder("TitledBorder.border");
        setBorder(BorderFactory.createTitledBorder(border, "Network Settings"));
        setLayout(new MigLayout("fill, ins 0"));
        add(getSettingsPanel(model.getSelectedItem()), "grow, push");
    }

    /**
     * Adds required listeners.
     */
    private void addListeners() {
        model.addServerListListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void serverGroupChanged(final ServerGroupItem item) {
        setVisible(false);
        removeAll();
        if (item != null) {
            if (item.getGroup() == item) {
                setBorder(BorderFactory.createTitledBorder(border,
                        "Network settings"));
            } else {
                setBorder(BorderFactory.createTitledBorder(border,
                        "Server settings"));
            }
        }
        add(getSettingsPanel(item), "grow, push");
        setVisible(true);
    }

    /**
     * Gets a settings panel for the specified group item, creating it if required.
     *
     * @param item Group item panel
     *
     * @return Settings panel for group item
     */
    private SettingsPanel getSettingsPanel(final ServerGroupItem item) {
        if (!panels.containsKey(item)) {
            if (item instanceof ServerGroup) {
                panels.put(item, new SettingsPanel(iconManager, componentFactory, "", false));
                addSettings(panels.get(item),
                        identityFactory.createAggregateConfig("irc", "", item.getGroup().
                        getNetwork(), item.getName()),
                        identityFactory.createServerConfig(item.getName()));
            } else {
                panels.put(item, new SettingsPanel(iconManager, componentFactory, "", false));
            }
        }
        return panels.get(item);
    }

    /** {@inheritDoc} */
    @Override
    public void dialogClosed(final boolean save) {
        if (save) {
            for (Entry<ServerGroupItem, SettingsPanel> entry : panels.entrySet()) {
                entry.getValue().save();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void serverGroupAdded(final ServerGroupItem parent,
            final ServerGroupItem group) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void serverGroupRemoved(final ServerGroupItem parent,
            final ServerGroupItem group) {
        //Ignore
    }

    /**
     * Adds the settings to the panel.
     *
     * @param settingsPanel Settings panel to add settings to
     * @param manager       The config manager to read current settings from.
     * @param identity      The provider to write settings to.
     */
    private void addSettings(final SettingsPanel settingsPanel,
            final AggregateConfigProvider manager, final ConfigProvider identity) {
        settingsPanel.addOption(PreferencesManager.getPreferencesManager()
                .getServerSettings(manager, identity));
    }

}
