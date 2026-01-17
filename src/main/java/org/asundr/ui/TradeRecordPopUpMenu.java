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

package org.asundr.ui;

import net.runelite.api.GameState;
import org.asundr.recovery.SaveManager;
import org.asundr.trade.TradeManager;
import org.asundr.utility.CommonUtils;
import org.asundr.utility.StringUtils;
import org.asundr.utility.TimeUtils;

import javax.swing.*;
import java.awt.*;

class TradeRecordPopUpMenu extends JPopupMenu
{
    static TradeTrackerPluginPanel mainPanel = null;
    private static final String TEXT_TOGGLE_COLLAPSE = "Toggle collapsed";
    private static final String TEXT_DELETE_ITEM = "<html><body style='color:red'>Delete</style></html>";
    private static final String TEMPLATE_EDIT_NOTE = "Edit note for trade with %s";
    private TradeRecordPanel tradeRecordPanel;
    private final JMenuItem editNote = new JMenuItem("Edit note");
    private final JMenuItem copyTrade = new JMenuItem("Trade data");
    private final JMenuItem highlightPlayer = new JMenuItem("Highlight player");

    TradeRecordPopUpMenu()
    {
        final JMenuItem toggleCollapse = new JMenuItem(TEXT_TOGGLE_COLLAPSE);
        toggleCollapse.addActionListener(e -> { if (tradeRecordPanel != null) tradeRecordPanel.toggleCollapsed(); });
        add(toggleCollapse);

        editNote.addActionListener(e -> {
            if (tradeRecordPanel == null)
                return;
            CommonUtils.promptTextEntry(
                    String.format(TEMPLATE_EDIT_NOTE, tradeRecordPanel.getTradeData().tradedPlayer.tradeName),
                    tradeRecordPanel.getNote(),
                    input -> { if (tradeRecordPanel != null ) tradeRecordPanel.setNote(input); }
            );
        });
        add(editNote);

        final JMenu copySubmenu = new JMenu("Copy");
        final JMenuItem copyName = new JMenuItem("Player name");
        copyName.addActionListener(e -> StringUtils.copyToClipboard(tradeRecordPanel.getTradeData().tradedPlayer.tradeName));
        copySubmenu.add(copyName);
        final JMenuItem copyTime = new JMenuItem("Date and Time");
        copyTime.addActionListener(e -> StringUtils.copyToClipboard(TimeUtils.timestampToString(tradeRecordPanel.getTradeData().tradeTime)));
        copySubmenu.add(copyTime);

        copyTrade.addActionListener(e -> { if (tradeRecordPanel != null) StringUtils.copyToClipboard(
                StringUtils.stringify(tradeRecordPanel.getTradeData()).replaceAll(SaveManager.REGEX_EMPTY_NOTES, "")
        ); });
        copySubmenu.add(copyTrade);
        add(copySubmenu);

        final JMenu filterSubmenu = new JMenu("Filter by");
        final JMenuItem filterName = new JMenuItem("Player name");
        filterName.addActionListener(e -> mainPanel.SetFilter(tradeRecordPanel.getTradeData().tradedPlayer.tradeName));
        filterSubmenu.add(filterName);
        add(filterSubmenu);

        addSeparator();

        final JMenuItem deleteItem = new JMenuItem(TEXT_DELETE_ITEM);
        deleteItem.addActionListener(e -> { if (tradeRecordPanel != null) TradeManager.requestRemoveTradeRecord(tradeRecordPanel.getTradeData()); });
        add(deleteItem);
    }

    public void setTradeRecordPanel(TradeRecordPanel tradeRecordPanel)
    {
        this.tradeRecordPanel = tradeRecordPanel;
    }

    @Override
    public void show(Component invoker, int x, int y)
    {
        copyTrade.setVisible(CommonUtils.getConfig().canCopyTradeData());
        editNote.setVisible(CommonUtils.getClient().getGameState() == GameState.LOGGED_IN);
        highlightPlayer.setVisible(CommonUtils.getClient().getGameState() == GameState.LOGGED_IN);
        super.show(invoker, x, y);
    }

}
