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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.io.Downloader;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link InstallWorker}s.
 */
@Singleton
public class InstallWorkerFactory {

    private final Downloader downloader;
    private final String tempDirectory;
    private final String pluginDirectory;
    private final String themeDirectory;
    private final PluginManager pluginManager;

    @Inject
    public InstallWorkerFactory(final Downloader downloader,
            @Directory(DirectoryType.TEMPORARY) final String tempDirectory,
            @Directory(DirectoryType.PLUGINS) final String pluginDirectory,
            @Directory(DirectoryType.THEMES) final String themeDirectory,
            final PluginManager pluginManager) {
        this.downloader = downloader;
        this.tempDirectory = tempDirectory;
        this.pluginDirectory = pluginDirectory;
        this.themeDirectory = themeDirectory;
        this.pluginManager = pluginManager;
    }
    public InstallWorker getInstallWorker(final AddonInfo info, final InstallerWindow installer) {
        return new InstallWorker(downloader, tempDirectory, pluginDirectory,
                themeDirectory, pluginManager, info, installer);
    }
}
