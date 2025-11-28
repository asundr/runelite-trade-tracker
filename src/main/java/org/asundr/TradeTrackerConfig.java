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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import org.asundr.recovery.ConfigKey;

import static org.asundr.trade.TradeManager.MAX_HISTORY_COUNT;
import static org.asundr.recovery.SaveManager.SAVE_GROUP;

@ConfigGroup(SAVE_GROUP)
public interface TradeTrackerConfig extends Config
{
	final class Section
	{
		public static final String DEBUG = "Debug";
		public static final String HISTORY_LIMITS = "historyLimits";
	}

	@ConfigItem(
			keyName = ConfigKey.AUTOLOAD_LAST_PROFILE,
			name = "Auto-load profile on launch",
			description = "If enabled, the last trade profile will be visible on the login screen when RuneLite is launched"
	)
	default boolean getAutoLoadLastProfile() { return true; }

	@ConfigItem(
			keyName = ConfigKey.USE_24_HOUR_TIME,
			name = "Display 24-hour time",
			description = "If enabled, displays 13:00 instead of 1:00 pm"
	)
	default boolean use24HourTime() { return false; }

	@ConfigItem(
			keyName = ConfigKey.IGNORE_EMPTY_TRADES,
			name = " Ignore empty trades",
			description = "<html><span>If enabled, accepted trades with no items given or received are not tracked.</span><br><span>Setting to false does not clear exiting empty trades.</span>"
	)
	default boolean ignoreEmptyTrades() { return false; }

	@ConfigItem(
			keyName = ConfigKey.COPY_TRADE_DATE_MENU,
			name = "Enable copy trade data",
			description = "<html><span>Adds ability to copy trade data by right clicking on trade record</span><br><span>May require restarting RuneLite</span>",
			section = Section.DEBUG
	)
	default boolean canCopyTradeData() { return false; }

	@ConfigItem(
			keyName = ConfigKey.MAX_HISTORY,
			name = "Maximum trade history",
			description = "<html><span>Maximum number of trade records before the oldest is deleted</span><br><span>Valid range: [1, " + MAX_HISTORY_COUNT+ "]",
			section = Section.HISTORY_LIMITS
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
			section = Section.HISTORY_LIMITS
	)
	default PurgeHistoryType getPurgeHistoryType() { return PurgeHistoryType.YEAR; }

	@ConfigItem(
			keyName = ConfigKey.PURGE_HISTORY_MAGNITUDE,
			name = "Auto-remove length",
			description = "After how many of the 'Auto-remove type' should old trades be removed?",
			section =  Section.HISTORY_LIMITS
	)
	default int getPurgeHistoryMagnitude() { return 1; }
}
