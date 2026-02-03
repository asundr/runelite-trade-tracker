package org.asundr.menu;

import net.runelite.api.IndexedObjectSet;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.menus.MenuManager;
import org.asundr.recovery.ConfigKey;
import org.asundr.recovery.SaveManager;
import org.asundr.ui.GuiUtils;
import org.asundr.utility.CommonUtils;

import java.util.Arrays;
import java.util.List;

public class TradeLookupMenuManager
{
    private static final String TEXT_MENU_ITEM_FILTER = "Filter trades";

    private final MenuManager menuManager;

    public TradeLookupMenuManager(MenuManager menuManager)
    {
        this.menuManager = menuManager;
        if (CommonUtils.getConfig().getPlayerLookupCharacter())
        {
            updatePlayerMenuItem(true);
        }
    }

    public void shutdown()
    {
        if (CommonUtils.getConfig().getPlayerLookupCharacter())
        {
            updatePlayerMenuItem(false);
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals(SaveManager.SAVE_GROUP))
        {
            return;
        }
        if (event.getKey().equals(ConfigKey.PLAYER_LOOKUP_CHARACTER))
        {
            updatePlayerMenuItem(CommonUtils.getConfig().getPlayerLookupCharacter());
        }
    }

    // Note: adapted from WOM plugin
    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuAction() != MenuAction.RUNELITE_PLAYER || !event.getMenuOption().equals(TEXT_MENU_ITEM_FILTER))
        {
            return;
        }
        final IndexedObjectSet<? extends Player> players = CommonUtils.getClient().getTopLevelWorldView().players();
        final Player player = players.byIndex(event.getId());
        if (player == null)
        {
            return;
        }
        GuiUtils.setFilterAndEnabled(player.getName());
    }

    private void updatePlayerMenuItem(boolean add)
    {
        if (add)
        {
            menuManager.addPlayerMenuItem(TEXT_MENU_ITEM_FILTER);
        }
        else
        {
            menuManager.removePlayerMenuItem(TEXT_MENU_ITEM_FILTER);
        }
    }

}
