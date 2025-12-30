/*
 * Copyright (c) 2025, Arun <trade-tracker-plugin.acwel@dralias.com>, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.asundr.screenshot;

import net.runelite.api.MainBufferProvider;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.asundr.utility.CommonUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;


public class ScreenshotOverlay extends Overlay
{
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM. dd, yyyy");
    private static final int REPORT_BUTTON_X_OFFSET = 404;

    private final DrawManager drawManager;

    private final Queue<Consumer<Image>> consumers = new ConcurrentLinkedQueue<>();

    ScreenshotOverlay(DrawManager drawManager)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(PRIORITY_HIGH);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.drawManager = drawManager;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (consumers.isEmpty())
        {
            return null;
        }

        final MainBufferProvider bufferProvider = (MainBufferProvider) CommonUtils.getClient().getBufferProvider();
        final int imageHeight = ((BufferedImage) bufferProvider.getImage()).getHeight();
        final int y = imageHeight - ScreenshotUtils.getReportButton().getHeight() - 1;

        graphics.drawImage(ScreenshotUtils.getReportButton(), REPORT_BUTTON_X_OFFSET, y, null);

        graphics.setFont(FontManager.getRunescapeSmallFont());
        FontMetrics fontMetrics = graphics.getFontMetrics();

        String date = DATE_FORMAT.format(new Date());
        final int dateWidth = fontMetrics.stringWidth(date);
        final int dateHeight = fontMetrics.getHeight();

        final int textX = REPORT_BUTTON_X_OFFSET + ScreenshotUtils.getReportButton().getWidth() / 2 - dateWidth / 2;
        final int textY = y + ScreenshotUtils.getReportButton().getHeight() / 2 + dateHeight / 2;

        graphics.setColor(Color.BLACK);
        graphics.drawString(date, textX + 1, textY + 1);

        graphics.setColor(Color.WHITE);
        graphics.drawString(date, textX, textY);

        Consumer<Image> consumer;
        while ((consumer = consumers.poll()) != null)
        {
            drawManager.requestNextFrameListener(consumer);
        }

        return null;
    }

    void queueForTimestamp(Consumer<Image> screenshotConsumer)
    {
        if (ScreenshotUtils.getReportButton() == null)
        {
            return;
        }
        consumers.add(screenshotConsumer);
    }

}
