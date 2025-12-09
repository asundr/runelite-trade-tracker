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

package org.asundr;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.asundr.trade.TradeItemData;
import org.asundr.trade.TradeManager;
import org.asundr.utility.StringUtils;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// A static function library used for common functionality
@Slf4j
public class TradeUtils
{
    public enum ItemID
    {
        COINS(995),
        PLATINUM(13204);
        public final int id;
        ItemID(int id) {this.id = id;}
    }

    private static TradeTrackerConfig config;
    private static ItemManager itemManager;
    private static Client client;
    private static ClientThread clientThread;
    private static ChatboxPanelManager chatboxPanelManager;
    private static  EventBus eventBus;
    private static TradeManager tradeManager;

    public static TradeTrackerConfig getConfig() { return config; }
    public static ClientThread getClientThread()
    {
        return clientThread;
    }
    public static Client getClient() { return client; }
    public static TradeManager getTradeManager() { return tradeManager; }

    public static ImageIcon iconNote = null;
    private final static HashMap<Integer, String> itemNameCache = new HashMap<>();


    // Prepares the utility class with the various query class instances it needs to function
    public static void initialize(ItemManager itemManager, TradeTrackerConfig config, Client client, ClientThread clientThread, TradeTrackerPlugin plugin, ChatboxPanelManager chatboxPanelManager, EventBus eventBus, TradeManager tradeManager)
    {
        TradeUtils.itemManager = itemManager;
        TradeUtils.config = config;
        TradeUtils.client = client;
        TradeUtils.clientThread = clientThread;
        TradeUtils.chatboxPanelManager = chatboxPanelManager;
        TradeUtils.eventBus = eventBus;
        TradeUtils.tradeManager = tradeManager;

        final BufferedImage iconImg = ImageUtil.loadImageResource(plugin.getClass(), "/net/runelite/client/plugins/friendnotes/note_icon.png");
        iconNote = new ImageIcon(iconImg.getScaledInstance(14,14, Image.SCALE_SMOOTH));
    }

    public static String getStoredItemName(final int id)
    {
        return itemNameCache.get(id);
    }
    public static String getOrDefaultCachedItemName(final int id, final String defaultValue) { return itemNameCache.getOrDefault(id, defaultValue); }

    // Forwards the passed event to the event bus
    public static void postEvent(@Nonnull Object event)
    {
        eventBus.post(event);
    }

    // Returns the image for the passed item with the quantity count
    public static AsyncBufferedImage getItemImage(final int itemId, final int quantity, final boolean stackable)
    {
        return itemManager.getImage(itemId, quantity, stackable);
    }

    // Returns the price of the item with the passed ID
    // Note: Should be called via clientThread.invokeLater()
    public static int getItemPrice(int itemID)
    {
        return itemManager.getItemPrice(itemID);
    }

    // Fetches and assigns the Grand Exchange prices of the passed items
    // Note: Should be called via clientThread.invokeLater()
    public static void fetchGePrices(final Collection<TradeItemData> itemDataList)
    {
        for (TradeItemData itemData : itemDataList)
        {
            itemData.setGEValue(getItemPrice(itemData.getID()));
        }
    }

    // Fetches item names and will update the id of noted items
    // Note: Should be called via clientThread.invokeLater()
    public static void fetchItemNames(final Collection<TradeItemData> itemDataList)
    {
        for (final TradeItemData itemData : itemDataList)
        {
            if (!itemNameCache.containsKey(itemData.getID()))
            {
                final ItemComposition comp = itemManager.getItemComposition(itemData.getID());
                if (comp.getNote() != -1)
                {
                    itemData.setOriginalID(comp.getLinkedNoteId());
                    if (itemNameCache.containsKey(itemData.getID()))
                    {
                        continue;
                    }
                }
                itemNameCache.put(itemData.getID(), comp.getMembersName());
            }
        }
    }

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
        File iconFile = new File(filename);
        try
        {
            BufferedImage iconImg = ImageIO.read(iconFile);
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
            return new ImageIcon(iconImg.getScaledInstance(width,height, hints));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // Returns an Icon given a filepath to an image, or null if no such image exists
    public static ImageIcon getIconFromName(final String filename){ return getIconFromName(filename, -1, -1, 0); }


    // Returns all enums that describe the current world
    public static EnumSet<WorldType> getWorldType()
    {
        return client.getWorldType();
    }

    // Returns the aggregate quantity of all items with the specified ID in the passed item collection
    public static long getTotalItemQuantity(final Collection<TradeItemData> items, int id)
    {
        return items.stream().filter(i->i.getID() == id).reduce(0L, (a, i) -> a + i.getQuantity(), Long::sum);
    }

    // Evaluates the aggregate Grand Exchange value of all passed item stacks
    public static long totalGEValue(final Collection<TradeItemData> items)
    {
        return items.stream().reduce(0L, (Acc, item) -> Acc + (item.getGEValue() * (long)item.getQuantity()), Long::sum);
    }

    // Returns true if the only items in the passed collection currency such as coins or platinum
    public static boolean isOnlyCurrency(final Collection<TradeItemData> items)
    {
        for (TradeItemData itemData : items)
        {
            if (itemData.getID() != ItemID.PLATINUM.id && itemData.getID() != ItemID.COINS.id)
            {
                return false;
            }
        }
        return true;
    }

    // Returns true if all items in the passed collection have the same ID.
    // Empty collections return true.
    public static boolean hasOnlyOneTypeOfItem(final Collection<TradeItemData> items)
    {
        if (items.isEmpty())
        {
            return true;
        }
        Iterator<TradeItemData> itr = items.iterator();
        final int id = itr.next().getID();
        while (itr.hasNext())
        {
            if (id != itr.next().getID())
            {
                return false;
            }
        }
        return true;
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

    // Returns a map of item IDs to the aggregate quantity of items with that id in the passed collection
    public static HashMap<Integer, Long> getItemCounts(final Collection<TradeItemData> items)
    {
        HashMap<Integer, Long> counts = new HashMap<>();
        for (final TradeItemData item : items)
        {
            final int id = item.getID();
            Long count = counts.getOrDefault(id, 0L);
            counts.put(id, count + item.getQuantity());
        }
        return counts;
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
