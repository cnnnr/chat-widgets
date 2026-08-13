package com.chatwidgets;

import com.chatwidgets.model.WidgetMessage;
import net.runelite.api.ChatMessageType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for {@link ChatWidgetPlugin#collapseDuplicate}, the shared duplicate-collapse used by
 * both capture-time collapsing ({@code onChatMessage}) and post-rewrite rebuilds
 * ({@code rebuildPooledMessage}). The helper is pure list logic over {@link WidgetMessage}, so it
 * needs no injected RuneLite services.
 */
public class CollapseDuplicateTest {

    private static WidgetMessage sender(String name, String body) {
        return sender(name, body, 1);
    }

    private static WidgetMessage sender(String name, String body, int count) {
        WidgetMessage m = WidgetMessage.senderMessage(name, null, body, 0L, ChatMessageType.PUBLICCHAT, false);
        m.setCount(count);
        return m;
    }

    private static WidgetMessage game(String body, int count) {
        WidgetMessage m = WidgetMessage.gameMessage(body, 0L, ChatMessageType.GAMEMESSAGE, false);
        m.setCount(count);
        return m;
    }

    /**
     * The comment-1 regression: two same-sender emoji lines that only match once both are rewritten
     * to {@code <img=N>}. The target is already in the pool, so its own slot is skipped and the
     * earlier duplicate is folded in — one line with count 2 instead of two identical lines.
     */
    @Test
    public void postRewriteFoldsDuplicateSurfacingAfterRewrite() {
        WidgetMessage msg1 = sender("Bob", "gg <img=29>");
        WidgetMessage msg2 = sender("Bob", "gg <img=29>");
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(msg1);
        pool.add(msg2);

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "gg <img=29>", "Bob", msg2.getCount(), 1);

        assertEquals(2, count);
        assertEquals(1, pool.size());
        assertSame("target keeps its slot; the earlier duplicate is removed", msg2, pool.get(0));
    }

    /** Capture-time semantics: target not yet in the pool (skipIndex -1), base 1, folds the match. */
    @Test
    public void captureTimeFoldsDuplicateOnTopOfBaseOne() {
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(sender("Bob", "gg"));

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "gg", "Bob", 1, -1);

        assertEquals(2, count);
        assertEquals(0, pool.size());
    }

    /** A matched entry that already carries a count is added on top, not replaced by 1. */
    @Test
    public void foldsExistingCountOnTopOfBase() {
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(sender("Bob", "gg", 3));

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "gg", "Bob", 1, -1);

        assertEquals(4, count);
        assertEquals(0, pool.size());
    }

    /** On a rewrite the target's preserved count is the base folded on top of the match. */
    @Test
    public void foldsPreservedTargetCountOnRewrite() {
        WidgetMessage other = sender("Bob", "gg", 4);
        WidgetMessage target = sender("Bob", "gg", 2);
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(other);
        pool.add(target);

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "gg", "Bob", target.getCount(), 1);

        assertEquals(6, count);
        assertEquals(1, pool.size());
        assertSame(target, pool.get(0));
    }

    /** Same body but a different sender is not a duplicate — no fold, pool untouched. */
    @Test
    public void differentSenderDoesNotCollapse() {
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(sender("Alice", "gg"));

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "gg", "Bob", 1, -1);

        assertEquals(1, count);
        assertEquals(1, pool.size());
    }

    /** No match returns the base count unchanged and leaves the pool alone. */
    @Test
    public void noMatchReturnsBaseAndLeavesPool() {
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(sender("Bob", "hi"));

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "bye", "Bob", 5, -1);

        assertEquals(5, count);
        assertEquals(1, pool.size());
    }

    /** Game messages have a null sender; two nulls collapse as equal. */
    @Test
    public void nullSendersCollapseAsGameMessages() {
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(game("You gained some XP.", 1));

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "You gained some XP.", null, 1, -1);

        assertEquals(2, count);
        assertEquals(0, pool.size());
    }

    /** Colour tags are stripped from pool entries before comparison. */
    @Test
    public void colourTagsStrippedBeforeCompare() {
        List<WidgetMessage> pool = new ArrayList<>();
        pool.add(sender("Bob", "<col=ff0000>gg</col>"));

        int count = ChatWidgetPlugin.collapseDuplicate(pool, "gg", "Bob", 1, -1);

        assertEquals(2, count);
        assertEquals(0, pool.size());
    }
}
