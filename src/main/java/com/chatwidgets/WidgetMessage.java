package com.chatwidgets;

import net.runelite.api.ChatMessageType;

public class WidgetMessage {
    private final String message;
    private final long timestamp;
    private final ChatMessageType type;
    private final boolean bossKc;
    private final String sender;
    private final boolean outgoing;
    private final int maxFadeSeconds;
    private int count = 1;

    public static WidgetMessage create(String message, long timestamp, ChatMessageType type,
            String sender, boolean outgoing, boolean bossKc, int maxFadeSeconds) {
        return new WidgetMessage(message, timestamp, type, bossKc, sender, outgoing, maxFadeSeconds);
    }

    public static WidgetMessage gameMessage(String message, long timestamp, ChatMessageType type, boolean bossKc) {
        return new WidgetMessage(message, timestamp, type, bossKc, null, false, 0);
    }

    public static WidgetMessage senderMessage(String sender, String message, long timestamp,
            ChatMessageType type, boolean outgoing) {
        return new WidgetMessage(message, timestamp, type, false, sender, outgoing, 0);
    }

    public static WidgetMessage loginNotification(String sender, String message, long timestamp, int maxFadeSeconds) {
        return new WidgetMessage(message, timestamp, ChatMessageType.LOGINLOGOUTNOTIFICATION,
                false, sender, false, maxFadeSeconds);
    }

    private WidgetMessage(String message, long timestamp, ChatMessageType type, boolean bossKc,
            String sender, boolean outgoing, int maxFadeSeconds) {
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.bossKc = bossKc;
        this.sender = sender;
        this.outgoing = outgoing;
        this.maxFadeSeconds = maxFadeSeconds;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ChatMessageType getType() {
        return type;
    }

    public boolean isBossKc() {
        return bossKc;
    }

    public String getSender() {
        return sender;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        count++;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getMaxFadeSeconds() {
        return maxFadeSeconds;
    }
}
