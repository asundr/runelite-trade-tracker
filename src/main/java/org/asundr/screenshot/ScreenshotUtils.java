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

import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageCapture;
import org.asundr.utility.CommonUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScreenshotUtils
{
    private static final String FOLDER_NAME = "Trades";

    private static BufferedImage reportButton;
    private static SpriteManager spriteManager;
    private static ScreenshotOverlay screenshotOverlay;
    private static ImageCapture imageCapture;
    private static OverlayManager overlayManager;

    private static Image tradeImage;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void initialize(SpriteManager spriteManager, ImageCapture imageCapture, DrawManager drawManager, OverlayManager overlayManager)
    {
        ScreenshotUtils.spriteManager = spriteManager;
        ScreenshotUtils.imageCapture = imageCapture;
        ScreenshotUtils.overlayManager = overlayManager;
        ScreenshotUtils.screenshotOverlay = new ScreenshotOverlay(drawManager);
        ScreenshotUtils.overlayManager.add(screenshotOverlay);
        getReportButton();
    }

    public static void shutdown()
    {
        overlayManager.remove(screenshotOverlay);
    }

    public static void clearScreenshot()
    {
        tradeImage = null;
    }

    public static  BufferedImage getReportButton()
    {
        if (reportButton == null)
        {
            spriteManager.getSpriteAsync(SpriteID.ReportButton._0, 0, s -> reportButton = s);
        }
        return reportButton;
    }

    public static void takeScreenshot()
    {
        if (!CommonUtils.getConfig().getScreenshotOnTrade())
        {
            return;
        }
        screenshotOverlay.queueForTimestamp(image -> tradeImage = image);
    }

    public static void saveScreenshot(final String tradeName)
    {
        if (!CommonUtils.getConfig().getScreenshotOnTrade() || tradeImage == null || tradeName == null)
        {
            clearScreenshot();
            return;
        }
        // Draw the game onto the screenshot off of the game thread
        executor.submit(() -> {
            final BufferedImage screenshot = new BufferedImage(tradeImage.getWidth(null), tradeImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            final Graphics graphics = screenshot.getGraphics();
            graphics.drawImage(tradeImage, 0, 0, null);
            imageCapture.saveScreenshot(screenshot, tradeName, FOLDER_NAME, false, false);
            clearScreenshot();
        });
    }
}
