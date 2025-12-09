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

package org.asundr.trade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

// Contains additional data for trades of coins/platinum for a single type of items
public class SimpleTradeData
{
    public enum Type
    {
        Invalid,        // This is not a simple trade
        Bought_Item,    // A single type of item bought by the player for currency
        Sold_Item       // A single type of item sold by the player for currency
    }

    private final Type tradeType;
    private TradeItemData item = null;
    private long quantity = 0;
    private float pricePerItem = 0f;

    public SimpleTradeData(final TradeData tradeData)
    {
        tradeType = determineTradeType(tradeData.givenItems, tradeData.receivedItems);
        switch (tradeType)
        {
            case Bought_Item:
                calculateTradeData(tradeData.receivedItems, tradeData.givenTotalValueGE);
                break;
            case Sold_Item:
                calculateTradeData(tradeData.givenItems, tradeData.receivedTotalValueGE);
                break;
            case Invalid: default:
                return;
        }
    }

    private void calculateTradeData(Collection<TradeItemData> itemsTraded, final long currencyExchanged)
    {
        final Optional<TradeItemData> sampleItem = itemsTraded.stream().findFirst();
        if (sampleItem.isEmpty())
        {
            return;
        }
        quantity = TradeUtils.getTotalItemQuantity(itemsTraded, sampleItem.get().getID());
        pricePerItem = currencyExchanged / (float)quantity;
        item = new TradeItemData(sampleItem.get().getID(), (int)Math.min(quantity, Integer.MAX_VALUE), sampleItem.get().getGEValue());
    }

    private Type determineTradeType(final ArrayList<TradeItemData> given, final ArrayList<TradeItemData> received)
    {
        final boolean isGivenCurrency = TradeUtils.isOnlyCurrency(given), isReceivedCurrency = TradeUtils.isOnlyCurrency(received);
        if (isGivenCurrency && !isReceivedCurrency && TradeUtils.hasOnlyOneTypeOfItem(received))
        {
            return Type.Bought_Item;
        }
        else if (isReceivedCurrency && !isGivenCurrency && TradeUtils.hasOnlyOneTypeOfItem(given))
        {
            return Type.Sold_Item;
        }
        return Type.Invalid;
    }

    public final boolean isValid() { return tradeType != Type.Invalid; }
    public final boolean isType(final Type type) { return tradeType == type; }
    public final long getQuantity() { return quantity; }
    public final TradeItemData getItem() { return new TradeItemData(item); }
    public final float getPricePerItem() { return pricePerItem; }
}
