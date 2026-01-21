/*
 * Copyright (c) 2026, Arun <trade-tracker-plugin.acwel@dralias.com>
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

package org.asundr.ui;

import org.asundr.trade.TradeData;
import org.asundr.trade.TradeManager;
import org.asundr.utility.CommonUtils;

import javax.swing.*;

public class GuiUtils
{
    private static String preTradeFilterText = null;
    private static boolean preTradeFilterActive = false;

    private static TradeTrackerPluginPanel mainPanel = null;
    public static void initialize(TradeTrackerPluginPanel mainPanel)
    {
        GuiUtils.mainPanel = mainPanel;
    }

    public static void setFilter(final String text)
    {
        if (mainPanel == null)
        {
            return;
        }
        mainPanel.setFilter(text);
    }

    public static void setFilterAndEnabled(final String text)
    {
        setFilter(text);
        mainPanel.btnFilter.setActive(true);
    }

    public static void clearFilter()
    {
        if (mainPanel == null)
        {
            return;
        }
        mainPanel.clearFilter();
    }

    public static void filterOnTrade(final TradeData tradeData)
    {
        switch (CommonUtils.getConfig().getAutoFilterOnTrade())
        {
            case NEVER:
                return;
            case ALWAYS:
                break;
            case EMPTY:
                if (!mainPanel.filterText.getText().isBlank())
                {
                    return;
                }
                break;
            case INACTIVE:
                if (mainPanel.btnFilter.isActive())
                {
                    return;
                }
                break;
            case INACTIVE_EMPTY:
                if (!mainPanel.filterText.getText().isBlank() || mainPanel.btnFilter.isActive())
                {
                    return;
                }
                break;
        }
        if (TradeManager.getTradeHistory().stream().noneMatch(e -> e.tradedPlayer.tradeName.equalsIgnoreCase(tradeData.tradedPlayer.tradeName)))
        {
            return;
        }
        preTradeFilterText = mainPanel.filterText.getText();
        preTradeFilterActive = mainPanel.btnFilter.isActive();
        setFilterAndEnabled(tradeData.tradedPlayer.tradeName);
    }

    public static void restoreFilterPostTrade()
    {
        if (preTradeFilterText != null)
        {
            SwingUtilities.invokeLater(() ->
            {
                setFilter(preTradeFilterText);
                mainPanel.btnFilter.setActive(preTradeFilterActive);
                preTradeFilterText = null;
                preTradeFilterActive = false;
            });
        }
    }
}
