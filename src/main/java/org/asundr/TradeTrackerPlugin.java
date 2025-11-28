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

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import org.asundr.recovery.SaveManager;
import org.asundr.trade.TradeManager;
import org.asundr.ui.TradeTrackerPluginPanel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.imageio.ImageIO;
import java.io.File;

@Slf4j
@PluginDescriptor(
	name = "Trade Tracker",
	description = "Records a searchable history of past trades with players",
	tags = {"trade", "history", "track", "item", "log", "logger", "memory"},
	enabledByDefault = true
)
public class TradeTrackerPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ItemManager itemManager;
	@Inject
	private TradeTrackerConfig config;
	@Inject
	private ClientThread clientThread;
	@Inject
	private ConfigManager configManager;
	@Inject
	private ChatboxPanelManager chatboxPanelManager;
	@Inject
	private EventBus eventBus;

	private final SaveManager saveManager = new SaveManager();
	private NavigationButton navigationButton;
	private Collection<Object> eventSubscribers;

	@Provides TradeTrackerConfig provideConfig(ConfigManager configManager) { return configManager.getConfig(TradeTrackerConfig.class); }

	@Override
	protected void startUp() throws Exception
	{
		TradeUtils.initialize(itemManager, config, client, clientThread, this, chatboxPanelManager, eventBus, TradeManager.getInstance());
		SaveManager.initialize(configManager);
		SaveManager.restoreCommonData();
		TradeTrackerPluginPanel mainPanel = new TradeTrackerPluginPanel();
		eventSubscribers = Arrays.asList(mainPanel, TradeManager.getInstance(), saveManager);
		eventSubscribers.forEach(e -> eventBus.register(e));
		if (!config.getAutoLoadLastProfile())
		{
			SaveManager.forgetActiveHistoryProfile();
		}
		SaveManager.restoreTradeHistoryData();
		addNavigationButton(mainPanel);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navigationButton);
		eventSubscribers.forEach(e -> eventBus.unregister(e));
		TradeManager.getInstance().shutdown();
	}

	private void addNavigationButton(final TradeTrackerPluginPanel mainPanel) throws IOException
	{
		BufferedImage icon;
		File iconFile = new File("resources/nav_icon.png");
		icon = ImageIO.read(iconFile);
		navigationButton = NavigationButton.builder()
				.tooltip("Trade Tracker")
				.icon(icon)
				.priority(3)
				.panel(mainPanel)
				.build();
		clientToolbar.addNavigation(navigationButton);
	}

}
