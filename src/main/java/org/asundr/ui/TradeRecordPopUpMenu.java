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
import org.asundr.TradeUtils;

import javax.swing.*;
import java.awt.*;

class TradeRecordPopUpMenu extends JPopupMenu
{
    private static final String TEXT_TOGGLE_COLLAPSE = "Toggle collapsed";
    private static final String TEXT_DELETE_ITEM = "<html><body style='color:red'>Delete</style></html>";
    private static final String TEMPLATE_EDIT_NOTE = "Edit note for trade with %s";
    private TradeRecordPanel tradeRecordPanel;
    private final JMenuItem editNote = new JMenuItem("Edit note");
    private final JMenuItem copyTrade = new JMenuItem("Copy trade data");

    TradeRecordPopUpMenu()
    {
        final JMenuItem toggleCollapse = new JMenuItem(TEXT_TOGGLE_COLLAPSE);
        toggleCollapse.addActionListener(e -> { if (tradeRecordPanel != null) tradeRecordPanel.toggleCollapsed(); });
        add(toggleCollapse);

        editNote.addActionListener(e -> {
            if (tradeRecordPanel == null)
                return;
            TradeUtils.promptTextEntry(
                    String.format(TEMPLATE_EDIT_NOTE, tradeRecordPanel.getTradeData().tradedPlayer.tradeName),
                    tradeRecordPanel.getNote(),
                    input -> { if (tradeRecordPanel != null ) tradeRecordPanel.setNote(input); }
            );
        });
        add(editNote);

        copyTrade.addActionListener( e -> { if (tradeRecordPanel != null) TradeUtils.copyToClipboard(TradeUtils.stringify(tradeRecordPanel.getTradeData())); });
        add(copyTrade);

        final JMenuItem deleteItem = new JMenuItem(TEXT_DELETE_ITEM);
        deleteItem.addActionListener(e -> { if (tradeRecordPanel != null) TradeUtils.getTradeManager().removeTradeRecord(tradeRecordPanel.getTradeData()); });
        add(deleteItem);
    }

    public void setTradeRecordPanel(TradeRecordPanel tradeRecordPanel)
    {
        this.tradeRecordPanel = tradeRecordPanel;
    }

    @Override
    public void show(Component invoker, int x, int y)
    {
        copyTrade.setVisible(TradeUtils.getConfig().canCopyTradeData());
        editNote.setVisible(TradeUtils.getClient().getGameState() == GameState.LOGGED_IN);
        super.show(invoker, x, y);
    }

}
