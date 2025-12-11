package org.asundr.overlay;

import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.asundr.utility.CommonUtils;

import java.awt.*;

public class HighlightPlayerMinimap extends Overlay
{
    private static final int MINIMAP_DOT_SIZE = 4;

    private Player targetPlayer;

    HighlightPlayerMinimap()
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        CommonUtils.registerForEvents(this);
    }

    public void setTargetPlayer(final Player player) { this.targetPlayer = player; }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (targetPlayer != null && CommonUtils.getConfig().playerHighlightShowMinimap())
        {
            final LocalPoint targetLocalPos = LocalPoint.fromWorld(CommonUtils.getClient(), targetPlayer.getWorldLocation());
            final Point minimapPoint = Perspective.localToMinimap(CommonUtils.getClient(), targetLocalPos);
            graphics.setColor(CommonUtils.getConfig().playerHighlightColor());
            graphics.fillOval(minimapPoint.getX() - MINIMAP_DOT_SIZE/2, minimapPoint.getY() - MINIMAP_DOT_SIZE/2, MINIMAP_DOT_SIZE, MINIMAP_DOT_SIZE);
        }
        return null;
    }
}
