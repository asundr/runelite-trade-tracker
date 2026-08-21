package org.asundr.screenshot;

import net.runelite.api.GameState;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import org.asundr.utility.CommonUtils;

public class ScreenshotConflictManager
{

	public static final String TEXT_CONFIG_CHANGED_WARNING_1 = "Screenshot trades enabled on both Trade Tracker & RuneWatch.";
	public static final String TEXT_CONFIG_CHANGED_WARNING_2 = "Will default to RuneWatch. Enable for only one plugin!";

	@Subscribe
	private void onConfigChanged(ConfigChanged configChanged)
	{
		if (ScreenshotUtils.hasScreenshotConfigChanged(configChanged))
		{
			if (ScreenshotUtils.isRunewatchScreenshotEnabled() && CommonUtils.getConfig().getScreenshotOnTrade())
			{
				if (CommonUtils.getClient().getGameState() == GameState.LOGGED_IN)
				{
					CommonUtils.chatMessage(TEXT_CONFIG_CHANGED_WARNING_1, true);
					CommonUtils.chatMessage(TEXT_CONFIG_CHANGED_WARNING_2, true);
				}
			}
		}
	}
}
