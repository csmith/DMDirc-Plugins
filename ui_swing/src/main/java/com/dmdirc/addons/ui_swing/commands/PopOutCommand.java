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

package com.dmdirc.addons.ui_swing.commands;

import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Command to pop out windows.
 */
public class PopOutCommand extends BaseCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("popout",
            "popout - Makes the current window pop out of the client as a "
            + "free floating window on your desktop.",
            CommandType.TYPE_GLOBAL);
    /** Factory to use to locate windows. */
    private final SwingWindowFactory windowFactory;

    /**
     * Create a new instance of PopOutCommand.
     *
     * @param windowFactory     Factory to use to locate windows.
     * @param commandController The command controller to use for command info.
     */
    @Inject
    public PopOutCommand(
            final SwingWindowFactory windowFactory,
            final CommandController commandController) {
        super(commandController);
        this.windowFactory = windowFactory;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin, final CommandArguments args,
            final CommandContext context) {
        UIUtilities.invokeLater(() -> {
            final TextFrame swingWindow = windowFactory.getSwingWindow(origin);
            if (swingWindow == null) {
                showError(origin, args.isSilent(), "There is currently no window to pop out.");
            } else {
                swingWindow.setPopout(true);
            }
        });
    }

}
