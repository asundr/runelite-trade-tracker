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
import com.google.gson.GsonBuilder;
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

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// A static function library used for common functionality
public class TradeUtils
{
    public enum ItemID
    {
        COINS(995),
        PLATINUM(13204);
        public final int id;
        ItemID(int id) {this.id = id;}
    }
    public final static long SECONDS_IN_DAY = 3600*24;
    public final static long MILLISECONDS_IN_DAY = SECONDS_IN_DAY * 1000L;

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
    private final static HashMap<Integer, String> itemNameMap = new HashMap<>();


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
        return itemNameMap.get(id);
    }
    public static String getOrDefaultStoredItemName(final int id, final String defaultValue)
    {
        return itemNameMap.getOrDefault(id, defaultValue);
    }

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

    // Returns the value limited to be no lower than  min or larger than max
    public static <T extends Comparable<T>> T clamp(T val, T min, T max)
    {
        return val.compareTo(min) < 0 ? min : val.compareTo(max) > 0 ? max : val;
    }

    // Returns true if the passed value is inclusively within the range of [min, max]
    public static <T extends Comparable<T>> boolean inRange(T val, T min, T max)
    {
        return val.compareTo(min) >= 0 && val.compareTo(max) <= 0;
    }

    // Returns a string representation of the timestamp using the passed pattern
    // https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns
    public static String timeStampToString(final long timestamp, final String pattern)
    {
        final Instant instant = Instant.ofEpochSecond(timestamp);
        final ZoneId zoneId = ZoneId.systemDefault();
        final ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, zoneId);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    // Returns a detailed representation of the time without the time zone
    public static String timeStampToString(final long timestamp)
    {
        return timeStampToString(timestamp, "yyyy-MM-dd H:mm:ss");
    }

    // Returns a string for the time depending on the age of the timestamp.
    // Timestamps from the current day show the time, and Yesterday for the previous day.
    // Timestamps within the year will show the month and date and anything older shows dd/MM/yy
    public static String timeStampToStringTime(final long timestamp)
    {
        if (isInCurrentDay(timestamp * 1000))
        {
            return timeStampToString(timestamp, config.use24HourTime() ? "H:mm" : "h:mm a");
        }
        else if (isInCurrentDay((timestamp + SECONDS_IN_DAY) * 1000))
        {
            return "Yesterday";
        }
        else if (isInCurrentYear(timestamp*1000))
        {
            return timeStampToString(timestamp, "LLL d");
        }
        else
        {
            return timeStampToString(timestamp, "dd/MM/yy");
        }
    }

    // Returns true if the passed timestamp in milliseconds is during the current year in the user's timezone
    public static boolean isInCurrentYear(long timestamp)
    {
        LocalDate date = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        final int currentYear = LocalDate.now().getYear();
        return date.getYear() == currentYear;
    }

    // Returns true if the passed timestamp in milliseconds is during the current day in the user's timezone
    public static boolean isInCurrentDay(long timestamp)
    {
        final LocalDate date = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        final int currentDay = LocalDate.now().getDayOfYear();
        final int currentYear = LocalDate.now().getYear();
        return date.getYear() == currentYear && date.getDayOfYear() == currentDay;
    }

    // Returns the number of milliseconds before midnight in the user's time zone
    public static long getTimeUntilMidnight()
    {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).toMillis();
    }

    // Removes all html tags from widget text
    public static String sanitizeWidgetText(final String s)
    {
        return s.replaceAll("<[^>]*>", "").trim();
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
            if (!itemNameMap.containsKey(itemData.getID()))
            {
                final ItemComposition comp = itemManager.getItemComposition(itemData.getID());
                if (comp.getNote() != -1)
                {
                    itemData.setOriginalID(comp.getLinkedNoteId());
                    if (itemNameMap.containsKey(itemData.getID()))
                    {
                        continue;
                    }
                }
                itemNameMap.put(itemData.getID(), comp.getMembersName());
            }
        }
    }

    // Note: from Quantity formatter
    private static final NumberFormat DECIMAL_FORMATTER = new DecimalFormat(
            "#,###.#",
            DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    );
    private static final NumberFormat PRECISE_DECIMAL_FORMATTER = new DecimalFormat(
            "#,###.###",
            DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    );
    private static final String[] QUANTITY_SUFFIXES = {"", "K", "M", "B", "T", "Q", "Qt"}; // 2^32 * 2^32 * 28 = 5.165 quintillion.
    // Returns an abbreviated string representation of the passed long in the style of QuantityFormatter but is capable of handling quantities up to Long.MAX_VALUE
    public static String quantityToRSDecimalStackLong(long quantity, boolean precise)
    {
        final String quantityStr = String.valueOf(quantity);
        if (quantityStr.length() <= 4 || (quantity < 0 && quantityStr.length() == 5))
        {
            return quantityStr;
        }
        final int power = (int) Math.log10(Math.abs(quantity));
        final NumberFormat numberFormat = precise && power >= 6 ? PRECISE_DECIMAL_FORMATTER : DECIMAL_FORMATTER;
        return numberFormat.format(quantity / (Math.pow(10, power - power%3))) + QUANTITY_SUFFIXES[power / 3];
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
            System.out.println("ERROR: Invalid item wiki url: " + WIKI_URL_PREFIX + name);
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

    // Converts the passed object to a json string
    public static <T> String stringify(T object)
    {
        return new GsonBuilder().create().toJson(object);
    }

    // Copies the passed String to the user's clipboard
    public static void copyToClipboard(final String content)
    {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
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

    // Returns true if the passed String is a Long type
    public static boolean isStringLong(final String str) {
        try
        {
            Long.parseLong(str);
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }

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
        final String widgetText = TradeUtils.sanitizeWidgetText(tradingWithWidget.getText());
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

    // Maps characters of to a list of all indexes where that character is the first letter of a word
    // note: converts all to lower case
    public static HashMap<Character, ArrayList<Integer>> getIndexesOfFirstLetterOfWord(final String str)
    {
        HashMap<Character, ArrayList<Integer>> indexMap = new HashMap<>();
        boolean addNextChar = true;
        final char[] characters = str.trim().toLowerCase().toCharArray();
        for (int i = 0; i < characters.length; ++i)
        {
            final char c = characters[i];
            if (Character.isWhitespace(c))
            {
                addNextChar = true;
            }
            else if (addNextChar)
            {
                indexMap.putIfAbsent(c, new ArrayList<>());
                indexMap.get(c).add(i);
                addNextChar = false;
            }
        }
        return indexMap;
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
