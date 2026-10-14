package com.chatwidgets;

import com.chatwidgets.model.MessageVariant;
import com.chatwidgets.model.WidgetMessage;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.PluginManager;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/** Exercises the combined pipe/macro pipeline through capture and delayed rewrites. */
public class ChatWidgetPipelineTest {
    private final AtomicBoolean inGim = new AtomicBoolean(true);

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private ChatWidgetPlugin plugin() throws Exception {
        ClanSettings settings = proxy(ClanSettings.class, (p, m, a) -> null);
        ClanChannel channel = proxy(ClanChannel.class, (p, m, a) -> null);
        Client client = proxy(Client.class, (p, m, a) -> {
            switch (m.getName()) {
                case "macroExpand":
                    return ((String) a[0]).replace("@red@", "<col=ff0000>");
                case "getClanSettings":
                    return inGim.get() ? settings : null;
                case "getClanChannel":
                    return inGim.get() ? channel : null;
                default:
                    return null;
            }
        });
        // An empty real manager makes Emojis disabled, exercising the event capture path.
        Constructor<?> constructor = PluginManager.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        PluginManager manager = (PluginManager) constructor.newInstance(
                false, false, new EventBus(), null, null, null, null);
        ChatWidgetPlugin plugin = new ChatWidgetPlugin();
        set(plugin, "client", client);
        set(plugin, "config", new ChatWidgetConfig() {});
        set(plugin, "pluginManager", manager);
        return plugin;
    }

    private static void set(ChatWidgetPlugin plugin, String name, Object value) throws Exception {
        Field field = ChatWidgetPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(plugin, value);
    }

    @SuppressWarnings("unchecked")
    private static List<WidgetMessage> pool(ChatWidgetPlugin plugin) throws Exception {
        Field field = ChatWidgetPlugin.class.getDeclaredField("messages");
        field.setAccessible(true);
        return (List<WidgetMessage>) field.get(plugin);
    }

    private static void rewrite(ChatWidgetPlugin plugin, WidgetMessage old, String body,
            boolean eventText) throws Exception {
        Method method = ChatWidgetPlugin.class.getDeclaredMethod(
                "rebuildPooledMessage", WidgetMessage.class, String.class, boolean.class);
        method.setAccessible(true);
        method.invoke(plugin, old, body, eventText);
    }

    @Test
    public void captureKeepsLiteralPipeInGimEvent() throws Exception {
        ChatWidgetPlugin plugin = plugin();
        plugin.onChatMessage(new ChatMessage(null, ChatMessageType.CLAN_GIM_CHAT,
                "Alice", "|a|b", "Group", 0));
        assertEquals("|a|b", pool(plugin).get(0).getMessage());
        assertEquals(MessageVariant.GIM, pool(plugin).get(0).getVariant());
    }

    @Test
    public void captureOutsideGimRestoresPipe() throws Exception {
        inGim.set(false);
        ChatWidgetPlugin plugin = plugin();
        plugin.onChatMessage(new ChatMessage(null, ChatMessageType.CLAN_GIM_CHAT,
                "Alice", "hello", "Group", 0));
        assertEquals("|hello", pool(plugin).get(0).getMessage());
        assertEquals(MessageVariant.NORMAL, pool(plugin).get(0).getVariant());
    }

    @Test
    public void captureSplitsPayloadBeforeExpandingMacros() throws Exception {
        ChatWidgetPlugin plugin = plugin();
        plugin.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE,
                "", "CA_ID:565|@red@Done", null, 0));
        WidgetMessage message = pool(plugin).get(0);
        assertEquals("<col=ff0000>Done", message.getMessage());
        assertEquals(Integer.valueOf(565), message.getAchievementTaskId());
    }

    @Test
    public void nodeRewriteKeepsMembershipSnapshotMacrosAndMetadata() throws Exception {
        ChatWidgetPlugin plugin = plugin();
        WidgetMessage old = WidgetMessage.senderMessage("Alice", "Group", "old", 123,
                ChatMessageType.CLAN_GIM_CHAT, false, MessageVariant.GIM);
        old.setCount(3);
        pool(plugin).add(old);
        inGim.set(false);
        rewrite(plugin, old, "||a|b @red@new<img=10>", false);
        WidgetMessage updated = pool(plugin).get(0);
        assertEquals("|a|b <col=ff0000>new<img=10>", updated.getMessage());
        assertEquals(MessageVariant.GIM, updated.getVariant());
        assertEquals("Group", updated.getChannelName());
        assertEquals(123, updated.getTimestamp());
        assertEquals(3, updated.getCount());
    }

    @Test
    public void nodeRewriteOutsideGimKeepsPipeAfterJoiningGroup() throws Exception {
        ChatWidgetPlugin plugin = plugin();
        WidgetMessage old = WidgetMessage.senderMessage("Alice", "Group", "|old", 123,
                ChatMessageType.CLAN_GIM_CHAT, false, MessageVariant.NORMAL);
        pool(plugin).add(old);
        rewrite(plugin, old, "|new<img=10>", false);
        assertEquals("|new<img=10>", pool(plugin).get(0).getMessage());
        assertEquals(MessageVariant.NORMAL, pool(plugin).get(0).getVariant());
    }

    @Test
    public void delayedGimCommandResultUsesEventText() throws Exception {
        ChatWidgetPlugin plugin = plugin();
        AtomicReference<String> result = new AtomicReference<>();
        MessageNode node = proxy(MessageNode.class,
                (p, m, a) -> m.getName().equals("getRuneLiteFormatMessage") ? result.get() : null);
        plugin.onChatMessage(new ChatMessage(node, ChatMessageType.CLAN_GIM_CHAT,
                "Alice", "!test", "Group", 0));
        result.set("|literal @red@result");
        plugin.onGameTick(new GameTick());
        assertEquals("|literal <col=ff0000>result", pool(plugin).get(0).getMessage());
    }

    @Test
    public void blankPayloadRewriteDropsMessage() throws Exception {
        ChatWidgetPlugin plugin = plugin();
        WidgetMessage old = WidgetMessage.gameMessage("old", 123, ChatMessageType.GAMEMESSAGE, false);
        pool(plugin).add(old);
        rewrite(plugin, old, "CA_ID:1|", false);
        assertTrue(pool(plugin).isEmpty());
    }
}
