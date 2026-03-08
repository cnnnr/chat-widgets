package com.chatwidgets;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

@ConfigGroup("chatwidgets")
public interface ChatWidgetConfig extends Config {

    @ConfigSection(name = "Appearance", description = "Global appearance settings shared across all widgets", position = 0, closedByDefault = false)
    String appearanceSection = "appearance";

    @ConfigSection(name = "Message Colours", description = "Text colour per message category", position = 1, closedByDefault = false)
    String coloursSection = "colours";

    @ConfigItem(keyName = "fontSize", name = "Font Size", description = "Font size for all messages", section = appearanceSection, position = 0)
    default FontSize fontSize() {
        return FontSize.REGULAR;
    }

    @ConfigItem(keyName = "textShadow", name = "Text Shadow", description = "Draw shadow behind text", section = appearanceSection, position = 1)
    default boolean textShadow() {
        return true;
    }

    @ConfigItem(keyName = "wrapText", name = "Wrap Text", description = "Wrap long messages to multiple lines", section = appearanceSection, position = 2)
    default boolean wrapText() {
        return true;
    }

    @ConfigItem(keyName = "showTimestamp", name = "Show Timestamps", description = "Prefix messages with a timestamp", section = appearanceSection, position = 3)
    default boolean showTimestamp() {
        return false;
    }

    @ConfigItem(keyName = "timestampFormat", name = "Timestamp Format", description = "Format for timestamps (e.g. [HH:mm:ss], [HH:mm])", section = appearanceSection, position = 4)
    default String timestampFormat() {
        return "[HH:mm]";
    }

    @ConfigItem(keyName = "smartPositioning", name = "Smart Positioning", description = "Automatically reposition widgets based on client mode and chatbox state", section = appearanceSection, position = 5)
    default boolean smartPositioning() {
        return true;
    }

    @ConfigItem(keyName = "collapseDuplicates", name = "Collapse Duplicates", description = "Merge consecutive identical messages into one with a count", section = appearanceSection, position = 6)
    default boolean collapseDuplicates() {
        return false;
    }

    @ConfigItem(keyName = "gameColour", name = "Game", description = "Text colour for game messages", section = coloursSection, position = 1)
    default Color gameColour() {
        return Color.WHITE;
    }

    @ConfigItem(keyName = "tradeColour", name = "Trade", description = "Text colour for trade messages", section = coloursSection, position = 2)
    default Color tradeColour() {
        return new Color(223, 32, 255);
    }

    @ConfigItem(keyName = "publicColour", name = "Public Chat", description = "Text colour for public chat messages", section = coloursSection, position = 3)
    default Color publicColour() {
        return new Color(148, 148, 255);
    }

    @ConfigItem(keyName = "friendsColour", name = "Friends Chat", description = "Text colour for friends chat messages", section = coloursSection, position = 4)
    default Color friendsColour() {
        return new Color(239, 80, 80);
    }

    @ConfigItem(keyName = "clanColour", name = "Clan Chat", description = "Text colour for clan chat messages", section = coloursSection, position = 5)
    default Color clanColour() {
        return new Color(127, 0, 0);
    }

    @ConfigItem(keyName = "privateColour", name = "Private", description = "Text colour for private messages", section = coloursSection, position = 9)
    default Color privateColour() {
        return new Color(0, 255, 255);
    }

    @ConfigItem(keyName = "timestampColour", name = "Timestamp", description = "Colour for message timestamps", section = coloursSection, position = 10)
    default Color timestampColour() {
        return Color.WHITE;
    }
}
