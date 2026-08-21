package org.asundr.menu;

import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.util.Text;
import org.asundr.ui.GuiUtils;
import org.asundr.utility.CommonUtils;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradeLookupMenuManager
{
	private static final Pattern PATTERN_MENU_PLAYER_NAME = Pattern.compile("(.+)\\s*\\([^\\)]*\\)");
	private static final String TEMPLATE_CHAT_TRADE_OFFER_WITH_NAME = "Sending %s a trade offer...";
	private static final String MESSAGE_OFFERED_TRADE = "Sending trade offer...";
	private static final String TEXT_MENU_ITEM_TRADE_WITH = "Trade with";
	private static final String TEXT_MENU_ITEM_ACCEPT_TRADE = "Accept trade";
	private static final String TEXT_MENU_ITEM_FILTER = "Filter trades";
	private static final String TEXT_MENU_OPTION_KICK = "Kick";
	private static final String TEXT_MENU_OPTION_DELETE = "Delete";
	private static final List<String> AFTER_OPTIONS = Arrays.asList("Message", "Add ignore", "Remove friend", TEXT_MENU_OPTION_DELETE, TEXT_MENU_OPTION_KICK);

	private final MenuManager menuManager;
	private boolean playerLookupEnabled = false;
	private String lastOfferedPlayerName = null;

	public TradeLookupMenuManager(MenuManager menuManager)
	{
		this.menuManager = menuManager;
	}

	public void shutdown()
	{
		setPlayerLookupEnabled(false);
	}

	private void updatePlayerLookupEnabled()
	{
		final boolean newEnabled;
		switch (CommonUtils.getConfig().getPlayerLookupCharacter())
		{
			case DISABLED:
				newEnabled = false;
				break;
			case ENABLED:
				newEnabled = true;
				break;
			default:
			case REQUIRE_SHIFT:
				newEnabled = CommonUtils.getClient().isKeyPressed(KeyCode.KC_SHIFT);
				break;
		}
		setPlayerLookupEnabled(newEnabled);
	}

	private void setPlayerLookupEnabled(boolean newEnabled)
	{
		if (newEnabled == playerLookupEnabled)
		{
			return;
		}
		playerLookupEnabled = newEnabled;
		if (playerLookupEnabled)
		{
			menuManager.addPlayerMenuItem(TEXT_MENU_ITEM_FILTER);
		} else
		{
			menuManager.removePlayerMenuItem(TEXT_MENU_ITEM_FILTER);
		}
	}

	// Note: adapted from WOM plugin
	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		updatePlayerLookupEnabled();
		switch (CommonUtils.getConfig().getPlayerLookupMenu())
		{
			case REQUIRE_SHIFT:
				if (CommonUtils.getClient().isKeyPressed(KeyCode.KC_SHIFT))
					break;
			case DISABLED:
				return;
		}
		final int groupId = WidgetUtil.componentToInterface(event.getActionParam1());
		final String option = event.getOption();
		if (!AFTER_OPTIONS.contains(option) || option.equals(TEXT_MENU_OPTION_DELETE) && groupId != InterfaceID.IGNORE)
		{
			return;
		}
		switch (groupId)
		{
			case InterfaceID.CHATBOX:
				// prevent from adding for Kick option (interferes with the raiding party one)
				if (option.equals(TEXT_MENU_OPTION_KICK))
					return;
			case InterfaceID.CHATCHANNEL_CURRENT:
			case InterfaceID.CLANS_SIDEPANEL:
			case InterfaceID.CLANS_GUEST_SIDEPANEL:
			case InterfaceID.RAIDS_SIDEPANEL:
			case InterfaceID.PM_CHAT:
			case InterfaceID.FRIENDS:
			case InterfaceID.IGNORE:
			{
				CommonUtils.getClient().getMenu().createMenuEntry(-1)
						.setTarget(event.getTarget())
						.setOption(TEXT_MENU_ITEM_FILTER)
						.setType(MenuAction.RUNELITE)
						.setIdentifier(event.getIdentifier())
						.onClick(e ->
						{
							GuiUtils.setFilterAndEnabled(Text.removeTags(event.getTarget()));
							SwingUtilities.invokeLater(GuiUtils::openPanel);
						});
				break;
			}
		}
	}


	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		switch (Text.removeTags(event.getMenuOption()).trim())
		{
			case TEXT_MENU_ITEM_FILTER:
			{
				if (event.getMenuAction() == MenuAction.RUNELITE_PLAYER)
				{
					final IndexedObjectSet<? extends Player> players = CommonUtils.getClient().getTopLevelWorldView().players();
					final Player player = players.byIndex(event.getId());
					if (player != null)
					{
						GuiUtils.setFilterAndEnabled(player.getName());
						SwingUtilities.invokeLater(GuiUtils::openPanel);
					}
				}
				break;
			}
			case TEXT_MENU_ITEM_TRADE_WITH:
			{
				if (CommonUtils.getConfig().addNameToTradeOfferChat())
				{
					final Matcher m = PATTERN_MENU_PLAYER_NAME.matcher(Text.removeTags(event.getMenuTarget()));
					if (m.find())
					{
						lastOfferedPlayerName = m.group(1).trim();
					}
				}
				break;
			}
			case TEXT_MENU_ITEM_ACCEPT_TRADE:
				if (CommonUtils.getConfig().addNameToTradeOfferChat())
				{
					lastOfferedPlayerName = Text.removeTags(event.getMenuTarget()).trim();
				}
				break;
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		// Updates trade offer message with offered player's name
		if (event.getType() == ChatMessageType.TRADE && event.getMessage().equals(MESSAGE_OFFERED_TRADE))
		{
			if (lastOfferedPlayerName != null)
			{
				// Delayed just in case other plugins need to match the original text
				SwingUtilities.invokeLater(() ->
				{
					event.getMessageNode().setValue(String.format(TEMPLATE_CHAT_TRADE_OFFER_WITH_NAME, lastOfferedPlayerName));
					lastOfferedPlayerName = null;
				});
			}
		}
	}

}
