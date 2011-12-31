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
package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;

import com.dmdirc.actions.ActionGroup;

import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Action group information panel.
 */
public final class ActionGroupInformationPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Action group. */
    private ActionGroup group;
    /** Description field. */
    private TextLabel infoLabel;
    /** Version label label. */
    private JLabel versionLabel;
    /** Version label. */
    private JLabel version;
    /** Author label label. */
    private JLabel authorLabel;
    /** Author label. */
    private JLabel author;

    /**
     * Initialises a new action group information panel.
     *
     * @param group Action group
     */
    public ActionGroupInformationPanel(final ActionGroup group) {
        super();

        this.group = group;

        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        infoLabel = new TextLabel("");
        versionLabel = new JLabel("Version: ");
        version = new JLabel();
        authorLabel = new JLabel("Author: ");
        author = new JLabel();

        setActionGroup(group);
    }

    /**
     * Adds listeners.
     */
    private void addListeners() {
        //Empty
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 2, hidemode 2"));

        add(infoLabel, "spanx 2, grow, push");
        add(authorLabel, "");
        add(author, "growx, pushx");
        add(versionLabel, "");
        add(version, "growx, pushx");
    }

    /**
     * Sets the action group for the panel.
     *
     * @param group New action group
     */
    public void setActionGroup(final ActionGroup group) {
        this.group = group;

        if (shouldDisplay()) {
            //Prevent HTML text displaying, replace newlines with <br>
            infoLabel.setText(group.getDescription().replace("<", "&lt;")
                    .replace("\n", "<br>"));
            author.setText(group.getAuthor());
            version.setText(group.getVersion() == null ? "" : group.getVersion().toString());

            author.setVisible(group.getAuthor() != null);
            version.setVisible(group.getVersion() != null);
            authorLabel.setVisible(group.getAuthor() != null);
            versionLabel.setVisible(group.getVersion() != null);
        } else {
            //Value require for mig/swing layouting bug
            infoLabel.setText("Non empty string");
            author.setText("");
            version.setText("");
        }
    }

    /**
     * Should the info panel be shown?
     *
     * @return true iif the panel should be shown
     */
    public boolean shouldDisplay() {
        return group != null && group.getDescription() != null;
    }
}
