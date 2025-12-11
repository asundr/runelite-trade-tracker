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

package org.asundr.overlay;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.*;
import org.asundr.TradeTrackerConfig;
import org.asundr.utility.CommonUtils;
import org.asundr.utility.StringUtils;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class HighlightPlayerOverlay extends Overlay
{
    private static final String TRADE_MENU_OPTION = "Trade with";

    private Player targetPlayer;
    private HighlightPlayerMinimap minimapOverlay = new HighlightPlayerMinimap();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledPurgeFuture = null;

    public HighlightPlayerOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        CommonUtils.registerForEvents(this);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded entry) {

        if (entry.getMenuEntry().getOption().equals(TRADE_MENU_OPTION))
        {
            if (targetPlayer == null)
            {
                return;
            }
            final String playerName = StringUtils.sanitizeWidgetText(entry.getMenuEntry().getTarget().replaceAll(" \\(level-\\d+\\)", "").replace('\u00A0',' '));
            if (playerName.equalsIgnoreCase(targetPlayer.getName()))
            {
                final String colorStr = StringUtils.colorToHexString(CommonUtils.getConfig().playerHighlightColor(), false);
                entry.getMenuEntry().setOption("<col=" + colorStr + ">" + entry.getMenuEntry().getOption() + "</col>");
            }
        }
    }

    public void setTargetPlayer(final Player target)
    {
        targetPlayer = target;
        CommonUtils.getOverlayManager().add(this);
        minimapOverlay.setTargetPlayer(targetPlayer);
        CommonUtils.getOverlayManager().add(minimapOverlay);
        startDisplayTimer();
    }

        public void clearTargetPlayer()
    {
        if (targetPlayer == null)
            return;
        targetPlayer = null;
        CommonUtils.getOverlayManager().remove(this);
        CommonUtils.getOverlayManager().remove(minimapOverlay);
        minimapOverlay.setTargetPlayer(null);
        if (!scheduledPurgeFuture.isDone() && !scheduledPurgeFuture.isCancelled())
        {
            scheduledPurgeFuture.cancel(false);
        }
    }

    private void startDisplayTimer()
    {
        if (scheduledPurgeFuture != null && !scheduledPurgeFuture.isCancelled() && !scheduledPurgeFuture.isDone())
        {
            scheduledPurgeFuture.cancel(false);
        }
        scheduledPurgeFuture = scheduler.schedule(() -> {
            targetPlayer = null;
            CommonUtils.getOverlayManager().remove(this);
            CommonUtils.getOverlayManager().remove(minimapOverlay);
            minimapOverlay.setTargetPlayer(null);
            }, CommonUtils.getConfig().playerHighlightDuration(), TimeUnit.SECONDS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (targetPlayer == null)
        {
            clearTargetPlayer();
            return null;
        }
        final TradeTrackerConfig config = CommonUtils.getConfig();
        OverlayUtil.renderActorOverlay(graphics, targetPlayer, targetPlayer.getName(), config.playerHighlightColor());
        final Client client = CommonUtils.getClient();
        final LocalPoint targetLocalPos = LocalPoint.fromWorld(client, targetPlayer.getWorldLocation());
        if (config.playerHighlightShowLine() && client.getLocalPlayer() != null)
        {
            final LocalPoint playerLocalPos = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
            final Point myScreenPos = Perspective.localToCanvas(client, playerLocalPos, client.getPlane());
            final Point targetScreenPos = Perspective.localToCanvas(client, targetLocalPos, client.getPlane());
            graphics.setColor(CommonUtils.getConfig().playerHighlightColor());
            graphics.drawLine(myScreenPos.getX(), myScreenPos.getY(), targetScreenPos.getX(), targetScreenPos.getY());
        }
        return null;
    }

}
