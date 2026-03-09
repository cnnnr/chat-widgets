package com.chatwidgets.model;

import net.runelite.api.ChatMessageType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Groups {@link ChatMessageType}s into user-facing categories for the sidebar panel's
 * message type picker and for the chatbox tab "Clear history" feature.
 */
public enum MessageCategory {
    GAME("Game",
            ChatMessageType.GAMEMESSAGE,
            ChatMessageType.SPAM,
            ChatMessageType.CONSOLE,
            ChatMessageType.WELCOME,
            ChatMessageType.BROADCAST,
            ChatMessageType.DIDYOUKNOW,
            ChatMessageType.ENGINE,
            ChatMessageType.UNKNOWN,
            ChatMessageType.PLAYERRELATED,
            ChatMessageType.SNAPSHOTFEEDBACK,
            ChatMessageType.FRIENDNOTIFICATION,
            ChatMessageType.FRIENDSCHATNOTIFICATION,
            ChatMessageType.IGNORENOTIFICATION,
            ChatMessageType.ITEM_EXAMINE,
            ChatMessageType.NPC_EXAMINE,
            ChatMessageType.OBJECT_EXAMINE,
            ChatMessageType.CLAN_MESSAGE,
            ChatMessageType.CLAN_GUEST_MESSAGE,
            ChatMessageType.CLAN_GIM_MESSAGE,
            ChatMessageType.CLAN_CREATION_INVITATION,
            ChatMessageType.NPC_SAY,
            ChatMessageType.MESBOX
    ),
    PUBLIC_CHAT("Public Chat",
            ChatMessageType.PUBLICCHAT,
            ChatMessageType.MODCHAT
    ),
    PRIVATE("Private Chat",
            ChatMessageType.PRIVATECHAT,
            ChatMessageType.PRIVATECHATOUT,
            ChatMessageType.MODPRIVATECHAT,
            ChatMessageType.LOGINLOGOUTNOTIFICATION
    ),
    FRIENDS_CHAT("Friends Chat",
            ChatMessageType.FRIENDSCHAT
    ),
    CLAN_CHAT("Clan Chat",
            ChatMessageType.CLAN_CHAT
    ),
    GUEST_CLAN_CHAT("Guest Chat",
            ChatMessageType.CLAN_GUEST_CHAT
    ),
    GIM_CLAN_CHAT("GIM Chat",
            ChatMessageType.CLAN_GIM_CHAT
    ),
    TRADE("Trade",
            ChatMessageType.TRADE,
            ChatMessageType.TRADE_SENT,
            ChatMessageType.TRADEREQ
    ),
    CHALLENGE("Challenge",
            ChatMessageType.CHALREQ_TRADE,
            ChatMessageType.CHALREQ_CLANCHAT,
            ChatMessageType.CHALREQ_FRIENDSCHAT
    ),
    AUTO("Autochat",
            ChatMessageType.AUTOTYPER,
            ChatMessageType.MODAUTOTYPER
    );

    private final String displayName;
    private final List<ChatMessageType> types;

    MessageCategory(String displayName, ChatMessageType... types) {
        this.displayName = displayName;
        this.types = Collections.unmodifiableList(Arrays.asList(types));
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<ChatMessageType> getTypes() {
        return types;
    }

    public static MessageCategory fromType(ChatMessageType type) {
        for (MessageCategory category : values()) {
            if (category.types.contains(type)) {
                return category;
            }
        }
        return GAME;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
