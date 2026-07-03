package com.chatwidgets.overlay;

import com.chatwidgets.ChatWidgetConfig;
import com.chatwidgets.ChatWidgetPlugin;
import com.chatwidgets.model.FontSize;
import com.chatwidgets.model.MessageCategory;
import com.chatwidgets.model.WidgetMessage;
import com.chatwidgets.model.WidgetPosition;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.IndexedSprite;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.config.ChatColorConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A single chat overlay instance. Each one is backed by an {@link OverlayConfig} that
 * determines which message types it displays, its position mode, fade behaviour, and
 * other per-widget settings.
 *
 * <p>On each frame, the overlay asks the plugin for the filtered message list, builds
 * render lines via {@link ChatRenderUtils#buildMessageLines}, and paints them bottom-up.
 */
public class DynamicChatOverlay extends Overlay {

    private static final int MIN_ZOOM = -22;
    private static final int MAX_ZOOM = 1400;
    private static final int BELOW_OFFSET_MIN_ZOOM = 40;
    private static final int BELOW_OFFSET_MAX_ZOOM = 150;
    private static final int ABOVE_OFFSET_MIN_ZOOM = -40;
    private static final int ABOVE_OFFSET_MAX_ZOOM = -120;

    private final ChatWidgetPlugin plugin;
    private final ChatWidgetConfig globalConfig;
    private final Client client;
    private final ChatColorConfig chatColorConfig;
    private OverlayConfig overlayConfig;

    public DynamicChatOverlay(ChatWidgetPlugin plugin, ChatWidgetConfig globalConfig, Client client,
            ChatColorConfig chatColorConfig, OverlayConfig overlayConfig) {
        this.plugin = plugin;
        this.globalConfig = globalConfig;
        this.client = client;
        this.chatColorConfig = chatColorConfig;
        this.overlayConfig = overlayConfig;
        setPosition(client.isResized() ? OverlayPosition.ABOVE_CHATBOX_RIGHT : OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        setResizable(true);
        setMovable(true);
        setMinimumSize(150);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Clear",
                overlayConfig.getName() + " history"));
    }

    public OverlayConfig getOverlayConfig() {
        return overlayConfig;
    }

    public void setOverlayConfig(OverlayConfig overlayConfig) {
        this.overlayConfig = overlayConfig;
        getMenuEntries().clear();
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Clear",
                overlayConfig.getName() + " history"));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!shouldRender()) {
            return null;
        }

        List<WidgetMessage> messages = plugin.getMessagesForOverlay(overlayConfig);
        if (messages.isEmpty()) {
            return null;
        }

        FontSize fontSize = overlayConfig.getFontSize();
        FontMetrics metrics = ChatRenderUtils.setupGraphics(graphics, fontSize);

        Dimension preferredSize = getPreferredSize();
        int widgetWidth = (preferredSize != null && preferredSize.width > 0)
                ? preferredSize.width
                : overlayConfig.getWidgetWidth();
        int lineHeight = metrics.getHeight() - (fontSize == FontSize.SMALL ? 2 : 3) + 1;
        long currentTime = System.currentTimeMillis();
        boolean drawShadow = globalConfig.textShadow();
        boolean wrapText = globalConfig.wrapText();
        boolean retainContextualColours = overlayConfig.isContextualColours();
        boolean hideDuplicateCount = overlayConfig.isHideDuplicateCount();
        WidgetPosition positionMode = overlayConfig.getPosition();
        boolean followPlayer = positionMode != WidgetPosition.WIDGET;
        boolean useDynamicHeight = followPlayer || overlayConfig.isDynamicHeight();

        int fadeOutDuration = overlayConfig.getFadeOutDuration();
        long fadeOutMs = fadeOutDuration * 1000L;
        long fadeOutThreshold = fadeOutMs + 5000;
        int maxMessages = overlayConfig.getMaxMessages();

        int messageCount = messages.size();
        int startIndex = Math.max(0, messageCount - maxMessages);

        List<RenderLine> renderableLines = new ArrayList<>();
        for (int i = startIndex; i < messageCount; i++) {
            WidgetMessage msg = messages.get(i);

            int msgMaxFade = msg.getMaxFadeSeconds();
            long msgFadeThreshold;
            if (msgMaxFade > 0) {
                msgFadeThreshold = msgMaxFade * 1000L + 5000;
            } else {
                msgFadeThreshold = fadeOutThreshold;
            }

            if ((fadeOutDuration == 0 && msgMaxFade == 0)
                    || (currentTime - msg.getTimestamp()) < msgFadeThreshold) {
                Color msgColor = getCategoryColour(msg.getType());
                List<RenderLine> msgLines = ChatRenderUtils.buildMessageLines(msg, metrics,
                        widgetWidth, currentTime, fadeOutMs, wrapText, msgColor,
                        retainContextualColours, hideDuplicateCount,
                        fontSize, client.getModIcons(),
                        overlayConfig.isShowTimestamp(), globalConfig.timestampFormat(),
                        globalConfig.showChannelName(),
                        chatColorConfig);

                for (RenderLine line : msgLines) {
                    if (line.alpha > 0) {
                        renderableLines.add(line);
                    }
                }
            }
        }

        if (renderableLines.isEmpty()) {
            return null;
        }

        int widgetHeight;
        if (useDynamicHeight) {
            widgetHeight = renderableLines.size() * lineHeight;
        } else {
            widgetHeight = maxMessages * lineHeight;
        }

        int marginTop = overlayConfig.getMarginTop();
        int marginBottom = overlayConfig.getMarginBottom();

        widgetHeight += marginTop + marginBottom;

        if (followPlayer) {
            Player localPlayer = client.getLocalPlayer();
            if (localPlayer != null) {
                Point playerPoint;
                if (positionMode == WidgetPosition.BELOW_PLAYER) {
                    playerPoint = localPlayer.getCanvasTextLocation(graphics, "", 0);
                } else {
                    playerPoint = localPlayer.getCanvasTextLocation(graphics, "", localPlayer.getLogicalHeight());
                }
                if (playerPoint != null) {
                    int zoomOffset = calculateZoomOffset(positionMode);
                    int x = playerPoint.getX() - widgetWidth / 2;
                    int y = playerPoint.getY() + zoomOffset - widgetHeight / 2;
                    graphics.translate(x - getBounds().x, y - getBounds().y);
                }
            }
        }

        Shape originalClip = graphics.getClip();
        graphics.setClip(0, 0, widgetWidth, widgetHeight + 4);

        int y = widgetHeight - marginBottom - metrics.getDescent();
        IndexedSprite[] modIcons = client.getModIcons();

        for (int i = renderableLines.size() - 1; i >= 0; i--) {
            RenderLine line = renderableLines.get(i);
            if (line.alpha <= 0) {
                continue;
            }

            int lineWidth = calculateLineWidth(line.segments, metrics);
            int x = followPlayer ? (widgetWidth - lineWidth) / 2 : 0;

            for (TextSegment segment : line.segments) {
                if (segment.iconId >= 0) {
                    BufferedImage img = ChatRenderUtils.getModIconImage(segment.iconId, modIcons);
                    if (img != null) {
                        x += ChatRenderUtils.drawIcon(graphics, img, fontSize, metrics, x, y);
                    } else {
                        x += segment.width;
                    }
                } else {
                    Color segmentColor = segment.color != null ? segment.color : Color.WHITE;
                    x += ChatRenderUtils.drawText(graphics, segment.text, segmentColor, line.alpha, x, y,
                            drawShadow, metrics);
                }
            }
            y -= lineHeight;
        }

        graphics.setClip(originalClip);

        if (followPlayer) {
            return null;
        }
        return new Dimension(widgetWidth, widgetHeight);
    }

    private boolean shouldRender() {
        if (!overlayConfig.isShow()) {
            return false;
        }
        if (client.getGameState() != GameState.LOGGED_IN) {
            return false;
        }
        if (overlayConfig.isAlwaysVisible()) {
            return true;
        }
        return plugin.isChatboxHidden();
    }

    private int calculateZoomOffset(WidgetPosition positionMode) {
        int zoom = client.get3dZoom();
        double normalizedZoom = (double) (zoom - MIN_ZOOM) / (MAX_ZOOM - MIN_ZOOM);
        normalizedZoom = Math.max(0, Math.min(1, normalizedZoom));

        int minOffset, maxOffset;
        if (positionMode == WidgetPosition.BELOW_PLAYER) {
            minOffset = BELOW_OFFSET_MIN_ZOOM;
            maxOffset = BELOW_OFFSET_MAX_ZOOM;
        } else {
            minOffset = ABOVE_OFFSET_MIN_ZOOM;
            maxOffset = ABOVE_OFFSET_MAX_ZOOM;
        }

        return (int) (minOffset + normalizedZoom * (maxOffset - minOffset));
    }

    private Color getCategoryColour(ChatMessageType type) {
        if (type == ChatMessageType.DIDYOUKNOW) {
            return globalConfig.didYouKnowColour();
        }
        if (type == ChatMessageType.BROADCAST) {
            return globalConfig.broadcastColour();
        }

        switch (MessageCategory.fromType(type)) {
            case GAME:
            case GAME_CLAN:
                return globalConfig.gameColour();
            case TRADE:
                return globalConfig.tradeColour();
            case CHALLENGE:
                return globalConfig.challengeColour();
            case PUBLIC_CHAT:
                return globalConfig.publicColour();
            case AUTO:
                return globalConfig.autoColour();
            case FRIENDS_CHAT:
                return globalConfig.friendsColour();
            case CLAN_CHAT:
                return globalConfig.clanColour();
            case GUEST_CLAN_CHAT:
                return globalConfig.guestClanColour();
            case GIM_CLAN_CHAT:
                return globalConfig.gimClanColour();
            case PRIVATE:
                return globalConfig.privateColour();
            default:
                return globalConfig.gameColour();
        }
    }

    private int calculateLineWidth(List<TextSegment> segments, FontMetrics metrics) {
        int width = 0;
        for (TextSegment segment : segments) {
            if (segment.iconId >= 0) {
                width += segment.width;
            } else {
                width += metrics.stringWidth(segment.text);
            }
        }
        return width;
    }
}
