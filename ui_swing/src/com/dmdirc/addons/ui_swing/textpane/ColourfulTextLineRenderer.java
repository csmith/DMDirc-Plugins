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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.ui.messages.IRCDocument;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;

/**
 * Test line renderer that adds padding and alternates background colour.
 */
public class ColourfulTextLineRenderer extends BasicTextLineRenderer {

    private static final int BORDER_VERTICAL = 10;
    private static final int BORDER_HORIZONTAL = 10;

    public ColourfulTextLineRenderer(final TextPane textPane, final TextPaneCanvas textPaneCanvas,
            final IRCDocument document) {
        super(textPane, textPaneCanvas, document);
    }

    @Override
    protected void renderLine(final Graphics2D graphics, final int canvasWidth, final int line,
            final float drawPosX, final float drawPosY, final int numberOfWraps, final int chars,
            final TextLayout layout) {
        graphics.setColor(line % 2 == 0 ? Color.ORANGE : Color.PINK);
        graphics.fillRect(0, (int) (drawPosY - layout.getAscent() - 1.5 - layout.getDescent()),
                canvasWidth, (int) (layout.getAscent() + 1.5 + layout.getDescent()));

        super.renderLine(graphics, canvasWidth - BORDER_HORIZONTAL * 2, line,
                drawPosX + BORDER_HORIZONTAL, drawPosY - BORDER_VERTICAL / 2, numberOfWraps,
                chars, layout);
    }

    @Override
    public RenderResult render(final Graphics2D graphics, final float canvasWidth,
            final float canvasHeight, final float drawPosY, final int line,
            final boolean bottomLine) {
        graphics.setColor(Color.GRAY);
        graphics.drawLine(0, (int) (drawPosY - 2), (int) canvasWidth, (int) (drawPosY - 2));
        graphics.setColor(line % 2 == 0 ? Color.ORANGE : Color.PINK);
        graphics.fillRect(0, (int) (drawPosY - 15), (int) canvasWidth, 12);

        final RenderResult result = super.render(graphics, canvasWidth, canvasHeight,
                drawPosY, line, bottomLine);
        result.totalHeight += BORDER_VERTICAL * 2;

        graphics.setColor(line % 2 == 0 ? Color.ORANGE : Color.PINK);
        graphics.fillRect(0, (int) (drawPosY - result.totalHeight), (int) canvasWidth, 8);

        return result;
    }

}
