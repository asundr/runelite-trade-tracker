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

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.RuneScapeProfileType;

import org.apache.commons.lang3.StringUtils;

// Contains data identifying the player profile associated with a trade histories
@Slf4j
public class TradeHistoryProfile
{
    private final long hash;
    private final String playerName;
    private final RuneScapeProfileType type;

    public TradeHistoryProfile(final Long hash, final String playerName, final RuneScapeProfileType type)
    {
        this.hash = hash;
        this.playerName = playerName;
        this.type = type;
    }

    public final String getPlayerName() { return playerName; }

    public final String toString() { return getKeyString(); }

    // Returns the string in the form "HASH+TYPE" which is used as a key for data recovery
    public final String getKeyString() { return Long.toHexString(hash) + "+" + type.toString(); }

    // Returns the type as a capitalized space-separated string
    public String getTypeString()
    {
        if (type == null || type == RuneScapeProfileType.STANDARD)
        {
            return "";
        }
        return "(" + StringUtils.capitalize(type.toString().replace("_"," ").toLowerCase()) + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof TradeHistoryProfile))
        {
            return false;
        }
        final TradeHistoryProfile other = (TradeHistoryProfile) o;
        return this.hash == other.hash && this.type == other.type;
    }

    public static TradeHistoryProfile parse(final String s)
    {
        final String[] parts = s.split("\\+");
        if (s.length() != 2)
        {
            log.error("Parsed TradeHistoryProfile is missing name or type");
            return null;
        }
        return new TradeHistoryProfile(Long.valueOf(parts[0], 16), null, RuneScapeProfileType.valueOf(parts[1]));
    }

}
