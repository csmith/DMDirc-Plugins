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

package com.dmdirc.addons.channelwho;

import com.dmdirc.ClientModule;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.util.LoggingScheduledExecutorService;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger injection module for the Channel Who plugin.
 */
@Module(injects = ChannelWhoManager.class, addsTo = ClientModule.class)
public class ChannelWhoModule {
    private final String domain;

    public ChannelWhoModule(final String domain) {
        this.domain = domain;
    }

    @Provides
    @PluginDomain(ChannelWhoPlugin.class)
    public String getSettingsDomain() {
        return domain;
    }

    @Provides
    @Named("channelwho")
    public ScheduledExecutorService getExecutorService() {
        return new LoggingScheduledExecutorService(1, "channelwho");
    }
}
