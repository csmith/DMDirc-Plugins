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

import com.dmdirc.interfaces.actions.ActionComponent;
import com.dmdirc.parser.interfaces.ClientInfo;

/**
 * Action component to retrieve a client's global access level.
 *
 * @author chris
 */
public class AccessLevelComponent implements ActionComponent {

    /** {@inheritDoc} */
    @Override
    public Object get(final Object argument) {
        UserLevelPlugin.doGlobalLevel((ClientInfo) argument);
        return ((ClientInfo) argument).getMap().get("level");
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> appliesTo() {
        return ClientInfo.class;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getType() {
        return Integer.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "access level";
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return "CLIENT_ACCESSLEVEL";
    }

}
