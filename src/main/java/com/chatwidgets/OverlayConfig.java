package com.chatwidgets;

import net.runelite.api.ChatMessageType;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class OverlayConfig {
    private String id;
    private String name;
    private Set<ChatMessageType> messageTypes;
    private int maxMessages;
    private int fadeOutDuration;
    private int widgetWidth;
    private int marginTop;
    private int marginBottom;
    private boolean dynamicHeight;
    private boolean hideDuplicateCount;
    private boolean contextualColours;
    private WidgetPosition position;

    public OverlayConfig() {
        this.id = UUID.randomUUID().toString();
        this.name = "New Widget";
        this.messageTypes = EnumSet.noneOf(ChatMessageType.class);
        this.maxMessages = 5;
        this.fadeOutDuration = 0;
        this.widgetWidth = 512;
        this.marginTop = 0;
        this.marginBottom = 0;
        this.dynamicHeight = false;
        this.hideDuplicateCount = false;
        this.contextualColours = true;
        this.position = WidgetPosition.WIDGET;
    }

    public static OverlayConfig createDefault(String name, Set<ChatMessageType> types) {
        OverlayConfig config = new OverlayConfig();
        config.name = name;
        config.messageTypes = EnumSet.copyOf(types);
        return config;
    }

    public static OverlayConfig defaultGameOverlay() {
        return createDefault("Game Messages",
                EnumSet.of(
                        ChatMessageType.GAMEMESSAGE,
                        ChatMessageType.SPAM,
                        ChatMessageType.CONSOLE,
                        ChatMessageType.WELCOME,
                        ChatMessageType.BROADCAST,
                        ChatMessageType.DIDYOUKNOW,
                        ChatMessageType.ENGINE,
                        ChatMessageType.FRIENDNOTIFICATION,
                        ChatMessageType.FRIENDSCHATNOTIFICATION,
                        ChatMessageType.IGNORENOTIFICATION,
                        ChatMessageType.ITEM_EXAMINE,
                        ChatMessageType.NPC_EXAMINE,
                        ChatMessageType.OBJECT_EXAMINE,
                        ChatMessageType.PLAYERRELATED,
                        ChatMessageType.SNAPSHOTFEEDBACK,
                        ChatMessageType.TRADE,
                        ChatMessageType.TRADE_SENT,
                        ChatMessageType.TRADEREQ,
                        ChatMessageType.UNKNOWN
                )
        );
    }

    public static OverlayConfig defaultPrivateOverlay() {
        return createDefault("Private Messages",
                EnumSet.of(
                        ChatMessageType.PRIVATECHAT,
                        ChatMessageType.PRIVATECHATOUT,
                        ChatMessageType.MODPRIVATECHAT,
                        ChatMessageType.LOGINLOGOUTNOTIFICATION
                )
        );
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ChatMessageType> getMessageTypes() {
        return messageTypes;
    }

    public void setMessageTypes(Set<ChatMessageType> messageTypes) {
        this.messageTypes = messageTypes;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = Math.max(1, Math.min(20, maxMessages));
    }

    public int getFadeOutDuration() {
        return fadeOutDuration;
    }

    public void setFadeOutDuration(int fadeOutDuration) {
        this.fadeOutDuration = Math.max(0, Math.min(300, fadeOutDuration));
    }

    public int getWidgetWidth() {
        return widgetWidth;
    }

    public void setWidgetWidth(int widgetWidth) {
        this.widgetWidth = Math.max(150, Math.min(1024, widgetWidth));
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = Math.max(0, Math.min(200, marginTop));
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = Math.max(0, Math.min(200, marginBottom));
    }

    public boolean isDynamicHeight() {
        return dynamicHeight;
    }

    public void setDynamicHeight(boolean dynamicHeight) {
        this.dynamicHeight = dynamicHeight;
    }

    public boolean isHideDuplicateCount() {
        return hideDuplicateCount;
    }

    public void setHideDuplicateCount(boolean hideDuplicateCount) {
        this.hideDuplicateCount = hideDuplicateCount;
    }

    public boolean isContextualColours() {
        return contextualColours;
    }

    public void setContextualColours(boolean contextualColours) {
        this.contextualColours = contextualColours;
    }

    public WidgetPosition getPosition() {
        return position;
    }

    public void setPosition(WidgetPosition position) {
        this.position = position;
    }
}
