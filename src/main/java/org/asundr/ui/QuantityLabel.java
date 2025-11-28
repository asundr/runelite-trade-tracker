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

import net.runelite.client.util.QuantityFormatter;
import org.asundr.TradeUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class QuantityLabel extends JLabel
{

    private static final String TEXT_MENU_ITEM_COPY = "Copy";
    private static JPopupMenu menu = null;
    private static long activeQuantity = 0L;

    private static String getActiveQuantityString() { return Long.toString(activeQuantity); }

    QuantityLabel(long quantity, final String template, final String tooltipFormat)
    {
        super(String.format(template, TradeUtils.quantityToRSDecimalStackLong(quantity, true)));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON3)
                {
                    if (menu == null)
                    {
                        menu = new JPopupMenu();
                        final JMenuItem copyValue = new JMenuItem(TEXT_MENU_ITEM_COPY);
                        copyValue.addActionListener(evt -> TradeUtils.copyToClipboard(getActiveQuantityString()));
                        menu.add(copyValue);
                    }
                    activeQuantity = quantity;
                    menu.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    super.mouseClicked(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setToolTipText(String.format(tooltipFormat, QuantityFormatter.formatNumber(quantity)));
                getToolTipLocation(e);
            }
        });
    }

}
