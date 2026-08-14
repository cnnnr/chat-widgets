package com.chatwidgets.overlay;

import com.chatwidgets.model.FontSize;
import com.chatwidgets.model.MessageCategory;
import com.chatwidgets.model.WidgetPosition;
import net.runelite.api.ChatMessageType;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Per-overlay configuration POJO. Each instance defines a single chat widget's behaviour:
 * which message types it shows, its position mode, fade duration, max visible messages, etc.
 *
 * <p>Serialized to/from JSON via Gson and stored as a single config key in RuneLite's
 * {@link net.runelite.client.config.ConfigManager}. On first run, two defaults are created
 * via {@link #defaultAllWidget()} and {@link #defaultPrivateWidget()}.
 */
public class OverlayConfig {
    /** Fully-transparent background — the "no background" / disabled state. */
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

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
    private boolean showTimestamp;
    private boolean showInputPreview;
    private boolean previewOnlyWhenTyping;
    private boolean show;
    private boolean alwaysVisible;
    private FontSize fontSize;
    private Color backgroundColour;
    private WidgetPosition position;

    public OverlayConfig() {
        this.id = UUID.randomUUID().toString();
        this.name = "New Widget";
        this.messageTypes = EnumSet.noneOf(ChatMessageType.class);
        this.maxMessages = 10;
        this.fadeOutDuration = 0;
        this.widgetWidth = 512;
        this.marginTop = 0;
        this.marginBottom = 0;
        this.dynamicHeight = false;
        this.hideDuplicateCount = false;
        this.contextualColours = true;
        this.showTimestamp = false;
        this.showInputPreview = false;
        this.previewOnlyWhenTyping = false;
        this.show = true;
        this.alwaysVisible = false;
        this.fontSize = FontSize.REGULAR;
        this.backgroundColour = TRANSPARENT;
        this.position = WidgetPosition.WIDGET;
    }

    public static OverlayConfig createDefault(String name, Set<ChatMessageType> types, boolean alwaysVisible) {
        OverlayConfig config = new OverlayConfig();
        config.name = name;
        config.messageTypes = EnumSet.copyOf(types);
        config.alwaysVisible = alwaysVisible;
        return config;
    }

    public static OverlayConfig defaultAllWidget() {
        EnumSet<ChatMessageType> types = EnumSet.noneOf(ChatMessageType.class);
        for (MessageCategory category : MessageCategory.values()) {
            if (category != MessageCategory.PRIVATE) {
                types.addAll(category.getTypes());
            }
        }
        OverlayConfig config = createDefault("All Messages", types, false);
        config.showInputPreview = true;
        return config;
    }

    public static OverlayConfig defaultPrivateWidget() {
        EnumSet<ChatMessageType> types = EnumSet.noneOf(ChatMessageType.class);
        types.addAll(MessageCategory.PRIVATE.getTypes());
        OverlayConfig config = createDefault("Private Messages", types, true);
        config.contextualColours = false;
        return config;
    }

    // Getters and setters

    public String getId() {
        return id;
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

    public boolean isShowTimestamp() {
        return showTimestamp;
    }

    public void setShowTimestamp(boolean showTimestamp) {
        this.showTimestamp = showTimestamp;
    }

    public boolean isShowInputPreview() {
        return showInputPreview;
    }

    public void setShowInputPreview(boolean showInputPreview) {
        this.showInputPreview = showInputPreview;
    }

    public boolean isPreviewOnlyWhenTyping() {
        return previewOnlyWhenTyping;
    }

    public void setPreviewOnlyWhenTyping(boolean previewOnlyWhenTyping) {
        this.previewOnlyWhenTyping = previewOnlyWhenTyping;
    }

    public FontSize getFontSize() {
        // Widgets serialized before this field existed deserialize fontSize as null.
        return fontSize != null ? fontSize : FontSize.REGULAR;
    }

    public void setFontSize(FontSize fontSize) {
        this.fontSize = fontSize;
    }

    public Color getBackgroundColour() {
        // Widgets serialized before this field existed deserialize backgroundColour as null.
        return backgroundColour != null ? backgroundColour : TRANSPARENT;
    }

    public void setBackgroundColour(Color backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean isAlwaysVisible() {
        return alwaysVisible;
    }

    public void setAlwaysVisible(boolean alwaysVisible) {
        this.alwaysVisible = alwaysVisible;
    }

    public WidgetPosition getPosition() {
        return position;
    }

    public void setPosition(WidgetPosition position) {
        this.position = position;
    }
}
