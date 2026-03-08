package com.chatwidgets.overlay;

import java.awt.Color;

/**
 * A single renderable piece of a message line — either a text string with a colour, an icon
 * reference (identified by {@code iconId} into the client's modIcons array), or a line break
 * sentinel ({@link #LINE_BREAK}).
 */
public class TextSegment {
    public static final int LINE_BREAK = -2;

    public final String text;
    public final int iconId;
    public final int width;
    public final Color color;

    public TextSegment(String text, int iconId, int width, Color color) {
        this.text = text;
        this.iconId = iconId;
        this.width = width;
        this.color = color;
    }
}
