package com.chatwidgets.overlay;

import java.util.List;

/**
 * A single horizontal line of {@link TextSegment}s ready to be painted, along with its
 * computed alpha (used for fade-out animation).
 */
public class RenderLine {
    public final List<TextSegment> segments;
    public final int alpha;

    public RenderLine(List<TextSegment> segments, int alpha) {
        this.segments = segments;
        this.alpha = alpha;
    }
}
