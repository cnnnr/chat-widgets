package com.chatwidgets.overlay;

import com.chatwidgets.model.FontSize;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Covers the {@code <at>} escape decode in {@link ChatRenderUtils}. RuneLite escapes a literal
 * {@code @} as {@code <at>} in message text (so it isn't parsed as an {@code @col@} colour code);
 * both parsers must turn it back into {@code @}, alongside the existing {@code <lt>} / {@code <gt>}
 * decoding. The helper needs only a headless {@link FontMetrics}; no injected RuneLite services.
 */
public class AtEscapeTest {

    private static FontMetrics metrics;

    @BeforeClass
    public static void setUp() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        metrics = g.getFontMetrics(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.dispose();
    }

    /** Concatenates the text of every non-icon, non-break segment. */
    private static String textOf(List<TextSegment> segments) {
        StringBuilder sb = new StringBuilder();
        for (TextSegment s : segments) {
            if (s.iconId == -1) {
                sb.append(s.text);
            }
        }
        return sb.toString();
    }

    private static String renderBody(String input) {
        return textOf(ChatRenderUtils.parseTextWithColoursAndIcons(
                input, metrics, null, true, Color.WHITE, FontSize.REGULAR, null));
    }

    private static String renderName(String input) {
        return textOf(ChatRenderUtils.parseTextWithIcons(
                input, metrics, null, Color.WHITE, FontSize.REGULAR));
    }

    /** The reported bug: "Gzzz@@@" arrives escaped as "Gzzz<at><at><at>" and rendered literally. */
    @Test
    public void bodyDecodesAtEscape() {
        assertEquals("Gzzz@@@", renderBody("Gzzz<at><at><at>"));
    }

    /** The <at> decode must not regress the existing <lt> / <gt> decoding in the same parser. */
    @Test
    public void bodyDecodesAtAlongsideLtGt() {
        assertEquals("a@<b>@", renderBody("a<at><lt>b<gt><at>"));
    }

    /** Sender names go through parseTextWithIcons, which also escapes literal @. */
    @Test
    public void nameDecodesAtEscape() {
        assertEquals("Bob@Home", renderName("Bob<at>Home"));
    }

    /** Plain text with no escapes is unchanged. */
    @Test
    public void plainTextUnchanged() {
        assertEquals("hello world", renderBody("hello world"));
    }
}
