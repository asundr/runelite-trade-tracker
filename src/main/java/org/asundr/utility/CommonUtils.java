/*
 * Copyright (c) 2025, Arun <trade-tracker-plugin.acwel@dralias.com>
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

package org.asundr.utility;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.asundr.TradeTrackerConfig;
import org.asundr.TradeTrackerPlugin;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A static function library used for common functionality
@Slf4j
public class CommonUtils
{
    private static TradeTrackerConfig config;
    private static Client client;
    private static ClientThread clientThread;
    private static ChatboxPanelManager chatboxPanelManager;
    private static EventBus eventBus;
    private static OverlayManager overlayManager;

    public static TradeTrackerConfig getConfig() { return config; }
    public static ClientThread getClientThread() { return clientThread; }
    public static Client getClient() { return client; }
    public static OverlayManager getOverlayManager() { return overlayManager;}


    // Prepares the utility class with the various query class instances it needs to function
    public static void initialize(TradeTrackerConfig config, Client client, ClientThread clientThread, TradeTrackerPlugin plugin, ChatboxPanelManager chatboxPanelManager, EventBus eventBus, OverlayManager overlayManager)
    {
        CommonUtils.config = config;
        CommonUtils.client = client;
        CommonUtils.clientThread = clientThread;
        CommonUtils.chatboxPanelManager = chatboxPanelManager;
        CommonUtils.eventBus = eventBus;
        CommonUtils.overlayManager = overlayManager;
    }

    // Forwards the passed event to the event bus
    public static void postEvent(@Nonnull Object event)
    {
        eventBus.post(event);
    }
    public static void registerForEvents(@Nonnull Object object) { eventBus.register(object);}
    public static void unregisterForEvents(@Nonnull Object object) { eventBus.unregister(object);}

    public static final String WIKI_URL_PREFIX = "https://oldschool.runescape.wiki/w/";

    // Given the name of an item, opens the corresponding wiki page
    public static void openItemWiki(String itemName)
    {
        String name = itemName.replace(" (Members)", "").trim().replaceAll(" ", "_");
        try
        {
            Desktop.getDesktop().browse(new URI(WIKI_URL_PREFIX + name));
        }
        catch (Exception e)
        {
            log.error("Invalid item wiki url: " + WIKI_URL_PREFIX + name);
        }
    }

    // Prompts the player to enter text using the in-game message box and sends the input to the response
    public static void promptTextEntry(final String prompt, final String initialText, final Consumer<String> response)
    {
        chatboxPanelManager.openTextInput(prompt)
            .value(Strings.nullToEmpty(initialText))
            .onDone((content) ->
            {
                if (content == null)
                {
                    return;
                }
                content = Text.removeTags(content).trim();
                response.accept(content);
            }).build();
    }

    // Returns an Icon given a filepath to an image, or null if no such image exists.
    // Image will be resized to the specified dimensions using the passed hint as the algorithm.
    public static ImageIcon getIconFromName(final String filename, int width, int height, final int hints)
    {
        BufferedImage iconImg = getImageFromName(filename);
        if (iconImg == null)
        {
            return null;
        }
        if (width == -1 && height == -1)
        {
            return new ImageIcon(iconImg);
        }
        if (width == -1)
        {
            width = height;
        }
        if (height == -1)
        {
            height = width;
        }
        return new ImageIcon(iconImg.getScaledInstance(width, height, hints));
    }

    // Returns an Icon given a filepath to an image, or null if no such image exists
    public static BufferedImage getImageFromName(final String filename)
    {
        return ImageUtil.loadImageResource(client.getClass(), '/' + filename);
    }

    // Returns all enums that describe the current world
    public static EnumSet<WorldType> getWorldType()
    {
        return client.getWorldType();
    }

    // Returns a string matching the passed pattern if that pattern finds a match in the widget represented by the group and child IDs
    public static String extractPatternFromWidget(int groupId, int childId, final Pattern p)
    {
        final Widget tradingWithWidget = client.getWidget(groupId, childId);
        if (tradingWithWidget == null)
        {
            return null;
        }
        final String widgetText = StringUtils.sanitizeWidgetText(tradingWithWidget.getText());
        final Matcher m = p.matcher(widgetText);
        if (m.find())
        {
            return m.group(1);
        }
        return null;
    }

    // Returns the items container with the passed id from the client
    public static ItemContainer getItemContainer(final int containerId)
    {
        return client.getItemContainer(containerId);
    }

    // Returns the lifetime in milliseconds of a trade before it is auto-removed
    // If plugin settings for auto-remove are invalid, return -1
    public static long getRecordLifetime()
    {
        if (!isValidPurgeConfig())
        {
            return -1;
        }
        return config.getPurgeHistoryMagnitude() * config.getPurgeHistoryType().ms;
    }

    // Returns true if the plugin config settings are valid values to schedule auto-removing
    public static boolean isValidPurgeConfig()
    {
        return config.getPurgeHistoryType() != TradeTrackerConfig.PurgeHistoryType.NEVER && config.getPurgeHistoryMagnitude() > 0;
    }
}
