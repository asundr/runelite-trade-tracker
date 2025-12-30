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

import lombok.Getter;
import net.runelite.api.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageCapture;
import org.asundr.utility.CommonUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.regex.Matcher;

public class ScreenshotUtils
{
    private static final int PLAYER_TRADE_CONFIRMATION_GROUP_ID = 334;
    private static final int PLAYER_TRADE_CONFIRMATION_TRADING_WITH = 30;
    private static final String FOLDER_NAME = "Trades";

    private static BufferedImage reportButton;
    private static SpriteManager spriteManager;
    private static ScreenshotOverlay screenshotOverlay;
    private static ImageCapture imageCapture;
    private static OverlayManager overlayManager;

    private static Image tradeImage;

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void initialize(SpriteManager spriteManager, ImageCapture imageCapture, DrawManager drawManager, OverlayManager overlayManager)
    {
        ScreenshotUtils.spriteManager = spriteManager;
        ScreenshotUtils.imageCapture = imageCapture;
        ScreenshotUtils.overlayManager = overlayManager;
        screenshotOverlay = new ScreenshotOverlay(drawManager);
        ScreenshotUtils.overlayManager.add(screenshotOverlay);
        getReportButton();
    }

    public static void shutdown()
    {
        overlayManager.remove(screenshotOverlay);
    }

    public static void clearScreenshot() {
        tradeImage = null;
    }

    public static  BufferedImage getReportButton()
    {
        if (reportButton == null)
        {
            spriteManager.getSpriteAsync(SpriteID.CHATBOX_REPORT_BUTTON, 0, s -> reportButton = s);
        }
        return reportButton;
    }

    public static void takeScreenshot()
    {
        if (!CommonUtils.getConfig().getScreenshotOnTrade())
        {
            return;
        }
        screenshotOverlay.queueForTimestamp(image -> {
//            Widget nameWidget = CommonUtils.getClient().getWidget(PLAYER_TRADE_CONFIRMATION_GROUP_ID, PLAYER_TRADE_CONFIRMATION_TRADING_WITH);
//            trader = "unknown";
//            if (nameWidget != null) {
//                Matcher m = TRADING_WITH_PATTERN.matcher(nameWidget.getText());
//                if (m.matches()) {
//                    trader = m.group(2);
//                }
//            }
            tradeImage = image;
        });
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
            BufferedImage screenshot = new BufferedImage(tradeImage.getWidth(null), tradeImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = screenshot.getGraphics();
            int gameOffsetX = 0;
            int gameOffsetY = 0;
            graphics.drawImage(tradeImage, gameOffsetX, gameOffsetY, null);
            imageCapture.saveScreenshot(screenshot, tradeName, FOLDER_NAME, false, false);
            clearScreenshot();
        });
    }
}
