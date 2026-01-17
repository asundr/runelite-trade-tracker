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

import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;
import org.asundr.trade.TradeItemData;
import org.asundr.trade.TradeUtils;
import org.asundr.utility.CommonUtils;
import org.asundr.utility.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

class ItemLabel extends JLabel
{
    public static final int ICON_SIZE = 36;
    private static final Dimension PREFERRED_SIZE = new Dimension(ICON_SIZE, ICON_SIZE);
    private static final String ITEM_TOOLTIP_TEMPLATE = "<html><body style><b style='color:#A0A0FF'>%s</b><br>Quantity: %s<br>GE: %s<br>Total: %s</body></html>";

    private static final String TEXT_MENU_ITEM_NAME = "Item name";
    private static final String TEXT_MENU_QUANTITY = "Quantity";
    private static final String TEXT_MENU_PRICE = "GE price at trade time";
    private static final String TEXT_MENU_PRICE_TOTAL = "GE total price at trade time";
    private static final String TEXT_MENU_ID_UNNOTED = "Item id (un-noted)";

    static TradeTrackerPluginPanel mainPanel;
    private static JPopupMenu popupMenu = null;
    private static String popupItemName = null;
    private static int popupItemId;
    private static TradeItemData popupItemData = null;

    private final TradeItemData itemData;
    private final long quantityOverride;


    ItemLabel(final TradeItemData itemData, final Consumer<MouseEvent> mouseClickedCallback, final long quantityOverride)
    {
        this.itemData = itemData;
        this.quantityOverride = quantityOverride;
        final int displayId = itemData.getID();
        final AsyncBufferedImage img = TradeUtils.getItemImage(displayId, itemData.getQuantity(), true);
        img.addTo(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                ToolTipManager.sharedInstance().setEnabled(true);
                final String tipStr = String.format(
                        ITEM_TOOLTIP_TEMPLATE,
                        TradeUtils.getOrDefaultCachedItemName(itemData.getUnnotedID(), "Item"),
                        QuantityFormatter.formatNumber(getTrueQuantity()),
                        QuantityFormatter.formatNumber(itemData.getGEValue()),
                        QuantityFormatter.formatNumber((long)itemData.getGEValue() * getTrueQuantity())
                );
                setToolTipText(tipStr);
            }
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON3)
                {
                    popupItemId = itemData.getUnnotedID();
                    popupItemName = TradeUtils.getStoredItemName(popupItemId);
                    popupItemData = new TradeItemData(itemData);
                    if (popupItemName != null)
                    {
                        if (popupMenu == null)
                        {
                            popupMenu = new JPopupMenu();

                            final JMenu openSubmenu = new JMenu("Open in");
                            final JMenuItem openWiki = new JMenuItem("Wiki page");
                            openWiki.addActionListener(evt -> CommonUtils.openItemWiki(getCurrentItemName()));
                            openSubmenu.add(openWiki);
                            final JMenuItem openWikiGE = new JMenuItem("Wiki price history");
                            openWikiGE.addActionListener(evt -> CommonUtils.openItemPriceHistory(popupItemId));
                            openSubmenu.add(openWikiGE);
                            popupMenu.add(openSubmenu);

                            final JMenu copySubmenu = new JMenu("Copy");
                            final JMenuItem copyName = new JMenuItem(TEXT_MENU_ITEM_NAME);
                            copyName.addActionListener(evt-> StringUtils.copyToClipboard(getCurrentItemName()));
                            copySubmenu.add(copyName);
                            final JMenuItem copyQuantity = new JMenuItem(TEXT_MENU_QUANTITY);
                            copyQuantity.addActionListener(evt-> StringUtils.copyToClipboard(Long.toString(getTrueQuantity())));
                            copySubmenu.add(copyQuantity);
                            final JMenuItem copyGEPrice = new JMenuItem(TEXT_MENU_PRICE);
                            copyGEPrice.addActionListener(evt-> StringUtils.copyToClipboard(Integer.toString(popupItemData.getGEValue())));
                            copySubmenu.add(copyGEPrice);
                            final JMenuItem copyGETotal = new JMenuItem(TEXT_MENU_PRICE_TOTAL);
                            copyGETotal.addActionListener(evt-> StringUtils.copyToClipboard(Long.toString((long)popupItemData.getGEValue() * getTrueQuantity())));
                            copySubmenu.add(copyGETotal);
                            final JMenuItem copyUnnotedId = new JMenuItem(TEXT_MENU_ID_UNNOTED);
                            copyUnnotedId.addActionListener(evt-> StringUtils.copyToClipboard(Integer.toString(popupItemData.getUnnotedID())));
                            copySubmenu.add(copyUnnotedId);
                            popupMenu.add(copySubmenu);
                        }


                        popupMenu.show(e.getComponent(),e.getX(),e.getY());
                    }
                    return;
                }
                if (mouseClickedCallback != null)
                {
                    mouseClickedCallback.accept(e);
                }
            }
        });
        setPreferredSize(PREFERRED_SIZE);
        setMinimumSize(PREFERRED_SIZE);
    }

    private static String getCurrentItemName() { return popupItemName; }

    ItemLabel(TradeItemData itemData) { this(itemData, null, -1L); }

    public long getTrueQuantity() { return quantityOverride == -1 ? itemData.getQuantity() : quantityOverride; }

}
