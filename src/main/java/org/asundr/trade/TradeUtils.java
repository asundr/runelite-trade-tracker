package org.asundr.trade;

import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;
import org.asundr.utility.CommonUtils;

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

    // Contains cached item data
    private static class CachedItemComposition
    {
        private final String name;
        private final int price;
        CachedItemComposition(final String membersName, final int storePrice)
        {
            this.name = membersName;
            this.price = storePrice;
        }
        public String getName() { return name; }
        public int getStorePrice() { return price; }
        public int getHaPrice() { return (int)(price * 0.8f);}
        public int getLaPrice() { return (int)(price * 0.6f); }
    }

    private final static HashMap<Integer, CachedItemComposition> itemCompositionMap = new HashMap<>();

    private static ItemManager itemManager;

    public static void initialize(final ItemManager itemManager)
    {
        TradeUtils.itemManager = itemManager;
    }

    // Returns the cached item name of the passed id. Assumes the cached value exists.
    public static String getCachedItemName(final int id)
    {
        return itemCompositionMap.get(id).getName();
    }

    // Returns the cached item name of the passed id, or the passed default value if no ached value is found
    public static String getOrDefaultCachedItemName(final int id, final String defaultValue)
    {
        final CachedItemComposition comp = itemCompositionMap.get(id);
        return comp == null ? defaultValue : comp.getName();
    }

    // Returns the cached high alchemy price of the passed item id
    public static int getHaPrice(final int id)
    {
        final CachedItemComposition cachedComp = itemCompositionMap.get(id);
        return cachedComp == null ? 0 : cachedComp.getHaPrice();
    }

    // Returns the cached low alchemy price of the passed item
    public static int getLaPrice(final int id)
    {
        final CachedItemComposition cachedComp = itemCompositionMap.get(id);
        return cachedComp == null ? 0 : cachedComp.getLaPrice();
    }

    // Returns either the GE price at the time of the trade, or the cached HA / LA price
    public static int getConfiguredPrice(final TradeItemData itemData)
    {
        switch (CommonUtils.getConfig().getDefaultPriceType())
        {
            case LOW_ALCHEMY:
                return getLaPrice(itemData.getID());
            case HIGH_ALCHEMY:
                return  getHaPrice(itemData.getID());
            case GRAND_EXCHANGE:
                return itemData.getGEValue();
        }
        return -1;
    }

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
    public static void fetchCompositionData(final Collection<TradeItemData> itemDataList)
    {
        for (final TradeItemData itemData : itemDataList)
        {
            if (!itemCompositionMap.containsKey(itemData.getID()))
            {
                final ItemComposition comp = itemManager.getItemComposition(itemData.getID());
                comp.getPrice();comp.getHaPrice();
                if (comp.getNote() != -1)
                {
                    itemData.setUnnotedId(comp.getLinkedNoteId());
                    if (itemCompositionMap.containsKey(itemData.getUnnotedID()))
                    {
                        continue;
                    }
                }
                itemCompositionMap.put(itemData.getUnnotedID(), new CachedItemComposition(comp.getMembersName(), comp.getPrice()));
            }
        }
    }

    // Returns the image for the passed item with the quantity count,
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
