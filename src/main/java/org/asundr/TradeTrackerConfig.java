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

import net.runelite.client.config.*;
import org.asundr.recovery.ConfigKey;

import java.awt.*;

import static org.asundr.trade.TradeManager.MAX_HISTORY_COUNT;
import static org.asundr.recovery.SaveManager.SAVE_GROUP;

@ConfigGroup(SAVE_GROUP)
public interface TradeTrackerConfig extends Config
{
	@ConfigSection(
			name = "General",
			description = "General settings",
			position = 0
	)
	String SECTION_GENERAL = "general";

	@ConfigSection(
			name = "Display",
			description = "Change the plugin's visuals",
			position = 1
	)
	String SECTION_DISPLAY = "display";

	@ConfigSection(
			name = "History Limits",
			description = "Settings to manage when the trade history culls old trades",
			position = 2
	)
	String SECTION_HISTORY_LIMITS = "historyLimits";

	@ConfigSection(
			name = "Debug",
			description = "For advanced users or submitting bug reports",
			position = 3,
			closedByDefault = true
	)
	String SECTION_DEBUG = "Debug";

///////////////////////////////

	@ConfigItem(
			keyName = ConfigKey.AUTOLOAD_LAST_PROFILE,
			name = "Auto-load profile on launch",
			description = "If enabled, the last trade profile will be visible on the login screen when RuneLite is launched",
			section = SECTION_GENERAL
	)
	default boolean getAutoLoadLastProfile() { return true; }

	@ConfigItem(
			keyName = ConfigKey.USE_24_HOUR_TIME,
			name = "Display 24-hour time",
			description = "If enabled, displays 13:00 instead of 1:00 pm",
			section = SECTION_DISPLAY
	)
	default boolean use24HourTime() { return false; }

	@ConfigItem(
			keyName = ConfigKey.PLAYER_HIGHLIGHT_COLOR,
			name = "Player highlight color",
			description = "What color should the player be highlighted",
			section = SECTION_DISPLAY
	)
	default Color playerHighlightColor() { return Color.WHITE; }

	@Range(min = 0, max = 3600 * 6)
	@ConfigItem(
			keyName = ConfigKey.PLAYER_HIGHLIGHT_DURATION,
			name = "Player highlight duration",
			description = "How many seconds should the player remained highlighted",
			section = SECTION_DISPLAY
	)
	default int playerHighlightDuration() { return 30; }

	@ConfigItem(
			keyName = ConfigKey.PLAYER_HIGHLIGHT_SHOW_LINE,
			name = "Player highlight enable line",
			description = "How many seconds should the player remained highlighted",
			section = SECTION_DISPLAY
	)
	default boolean playerHighlightShowLine() { return true; }

	@ConfigItem(
			keyName = ConfigKey.PLAYER_HIGHLIGHT_SHOW_MINIMAP,
			name = "Player highlight on minimap",
			description = "Highlighted player dot on minimap is set to config color",
			section = SECTION_DISPLAY
	)
	default boolean playerHighlightShowMinimap() { return true; }

	@ConfigItem(
			keyName = ConfigKey.IGNORE_EMPTY_TRADES,
			name = "Ignore empty trades",
			description = "<html><span>If enabled, accepted trades with no items given or received are not tracked.</span><br><span>Setting to false does not clear exiting empty trades.</span>",
			section = SECTION_GENERAL
	)
	default boolean ignoreEmptyTrades() { return false; }

	@ConfigItem(
			keyName = ConfigKey.FILTER_ITEM_ID,
			name = "Filter matches for Item ID",
			description = "When filtering the trade history, item IDs will also be checked for a match",
			section = SECTION_DEBUG
	)
	default boolean filterMatchItemId() { return false; }

	@ConfigItem(
			keyName = ConfigKey.COPY_TRADE_DATE_MENU,
			name = "Enable copy trade data",
			description = "<html><span>Adds ability to copy trade data by right clicking on trade record</span><br><span>May require restarting RuneLite</span>",
			section = SECTION_DEBUG
	)
	default boolean canCopyTradeData() { return false; }

	@Range (
			min = 1, max = MAX_HISTORY_COUNT
	)
	@ConfigItem(
			keyName = ConfigKey.MAX_HISTORY,
			name = "Maximum trade history",
			description = "<html><span>Maximum number of trade records before the oldest is deleted</span><br><span>Valid range: [1, " + MAX_HISTORY_COUNT+ "]",
			section = SECTION_HISTORY_LIMITS,
			position = 1
	)
	default int maxHistoryCount() { return 256; }

	enum PurgeHistoryType {
		NEVER (Long.MAX_VALUE),
		MINUTE(60000L),
		HOUR (MINUTE.ms*60L),
		DAY (HOUR.ms * 24L),
		YEAR (DAY.ms * 365L);
		public final long ms;
		PurgeHistoryType(long ms) { this.ms = ms; }
	}

	@ConfigItem(
			keyName = ConfigKey.PURGE_HISTORY_TYPE,
			name = "Auto-remove type",
			description = "When should older trades be removed from the history?",
			section = SECTION_HISTORY_LIMITS,
			position = 2
	)
	default PurgeHistoryType getPurgeHistoryType() { return PurgeHistoryType.YEAR; }

	@Range (
			min = 0
	)
	@ConfigItem(
			keyName = ConfigKey.PURGE_HISTORY_MAGNITUDE,
			name = "Auto-remove length",
			description = "After how many of the 'Auto-remove type' should old trades be removed?",
			section =  SECTION_HISTORY_LIMITS,
			position = 3
	)
	default int getPurgeHistoryMagnitude() { return 1; }
}
