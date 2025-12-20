package org.asundr.trade;

import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

final public class TradeUtils
{
    public enum ItemID
    {
        COINS(995),
        PLATINUM(13204);
        public final int id;
        ItemID(int id) {this.id = id;}
    }

    private final static HashMap<Integer, String> itemNameCache = new HashMap<>();

    private static ItemManager itemManager;

    public static void initialize(final ItemManager itemManager)
    {
        TradeUtils.itemManager = itemManager;
    }

    public static String getStoredItemName(final int id)
    {
        return itemNameCache.get(id);
    }
    public static String getOrDefaultCachedItemName(final int id, final String defaultValue) { return itemNameCache.getOrDefault(id, defaultValue); }

    // Returns the price of the item with the passed ID
    // Note: Should be called via clientThread.invokeLater()
    public static int getItemPrice(final int itemID)
    {
        return itemManager.getItemPrice(itemID);
    }

    // Fetches and assigns the Grand Exchange prices of the passed items
    // Note: Should be called via clientThread.invokeLater()
    public static void fetchGePrices(final Collection<TradeItemData> itemDataList)
    {
        for (TradeItemData itemData : itemDataList)
        {
            itemData.setGEValue(getItemPrice(itemData.getUnnotedID()));
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
                    itemData.setUnnotedId(comp.getLinkedNoteId());
                    if (itemNameCache.containsKey(itemData.getUnnotedID()))
                    {
                        continue;
                    }
                }
                itemNameCache.put(itemData.getUnnotedID(), comp.getMembersName());
            }
        }
    }

    // Returns the image for the passed item with the quantity count
    public static AsyncBufferedImage getItemImage(final int itemId, final int quantity, final boolean stackable)
    {
        return itemManager.getImage(itemId, quantity, stackable);
    }

    // Returns the aggregate quantity of all items with the specified ID in the passed item collection
    public static long getTotalItemQuantity(final Collection<TradeItemData> items, int id)
    {
        return items.stream().filter(i->i.getUnnotedID() == id).reduce(0L, (a, i) -> a + i.getQuantity(), Long::sum);
    }

    // Evaluates the aggregate Grand Exchange value of all passed item stacks
    public static long totalGEValue(final Collection<TradeItemData> items)
    {
        return items.stream().reduce(0L, (Acc, item) -> Acc + (item.getGEValue() * (long)item.getQuantity()), Long::sum);
    }

    // Returns true if the only items in the passed collection currency such as coins or platinum
    public static boolean isOnlyCurrency(final Collection<TradeItemData> items)
    {
        if (items.isEmpty())
        {
            return false;
        }
        for (TradeItemData itemData : items)
        {
            if (itemData.getUnnotedID() != ItemID.PLATINUM.id && itemData.getUnnotedID() != ItemID.COINS.id)
            {
                return false;
            }
        }
        return true;
    }

    // Returns true if all items in the passed collection have the same ID.
    public static boolean hasOnlyOneTypeOfItem(final Collection<TradeItemData> items)
    {
        if (items.isEmpty())
        {
            return false;
        }
        Iterator<TradeItemData> itr = items.iterator();
        final int id = itr.next().getUnnotedID();
        while (itr.hasNext())
        {
            if (id != itr.next().getUnnotedID())
            {
                return false;
            }
        }
        return true;
    }

    // Returns a map of item IDs to the aggregate quantity of items with that id in the passed collection
    public static HashMap<Integer, Long> getItemCounts(final Collection<TradeItemData> items)
    {
        final HashMap<Integer, Long> counts = new HashMap<>();
        for (final TradeItemData item : items)
        {
            final int id = item.getUnnotedID();
            Long count = counts.getOrDefault(id, 0L);
            counts.put(id, count + item.getQuantity());
        }
        return counts;
    }
}
