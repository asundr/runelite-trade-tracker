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

import org.asundr.trade.TradeItemData;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

class IconGridPanel extends JPanel
{
    private static final int MAX_COLUMNS = 6;
    private static final int MAX_ROWS = (int)Math.ceil((float)28/MAX_COLUMNS); // max inventory is 28 items, and empty trades should always show at least one row
    public static final int ICON_SIZE = ItemLabel.ICON_SIZE;
    private static final Color COLOR_GRID_BACKGROUND = new Color(18,18,40);
    private static final Color COLOR_ITEM_BORDER = new Color(40,40,40);
    private static final Border BORDER_ITEM = BorderFactory.createCompoundBorder(null, BorderFactory.createLineBorder(COLOR_ITEM_BORDER, 1));
    private static final Dimension PREFERRED_SIZE = new Dimension(230, 36);

    public IconGridPanel()
    {
        setPreferredSize(PREFERRED_SIZE);
        setBackground(COLOR_GRID_BACKGROUND);
    }

    public void updateIcons(List<TradeItemData> items)
    {
        // Calculate the number of visible rows based on the number of icons
        final int totalIcons = Math.min(items.size(), MAX_COLUMNS * MAX_ROWS);
        final int numRows = (int) Math.max(1, Math.ceil((float) totalIcons / MAX_COLUMNS));
        final int emptyIcons = totalIcons == 0 ? MAX_COLUMNS : (MAX_COLUMNS - (totalIcons % MAX_COLUMNS)) % MAX_COLUMNS;
        // Reset contents, layout and dimensions
        removeAll();
        setLayout(new GridLayout(numRows, MAX_COLUMNS));
        setSize(new Dimension(MAX_COLUMNS * ICON_SIZE, numRows * ICON_SIZE));
        // Add the item icons
        for (int i = 0; i < totalIcons; ++i)
        {
            final ItemLabel itemLabel = new ItemLabel(items.get(i));
            itemLabel.setBorder(BORDER_ITEM);
            add(itemLabel);
        }
        // Add empty labels to fill out the last row
        for (int i = 0; i < emptyIcons; ++i)
        {
            final JLabel emptyLabel = new JLabel();
            emptyLabel.setBorder(BORDER_ITEM);
            add(emptyLabel);
        }
        revalidate();
        repaint();
    }
}
