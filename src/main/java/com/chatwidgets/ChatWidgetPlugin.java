package com.chatwidgets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ResizeableChanged;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ChatColorConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

@PluginDescriptor(name = "Chat Widgets", description = "Displays chat messages in customizable overlay widgets.", tags = {
        "game", "private", "public", "clan", "friends", "chat", "pm", "message", "widget", "overlay", "split",
        "move", "custom", "customize", "resizable", "transparent" })
public class ChatWidgetPlugin extends Plugin {

    private static final String CONFIG_GROUP = "chatwidgets";
    private static final String OVERLAY_CONFIGS_KEY = "overlayConfigs";
    private static final int MAX_POOL_SIZE = 200;

    private static final Pattern BOSS_KC_PATTERN = Pattern.compile("Your .+ count is:");

    private static final MessageMergeRule[] MESSAGE_MERGE_RULES = {
            new MessageMergeRule("You eat", "It heals some health.", true),
            new MessageMergeRule("You drink", Pattern.compile("You have [0-9].*")),
            new MessageMergeRule("You are now a guest of", "To talk, start each", true),
            new MessageMergeRule("You drink", "You have finished your potion.", true)
    };

    private static final Set<ChatMessageType> PM_TYPES = EnumSet.of(
            ChatMessageType.PRIVATECHAT,
            ChatMessageType.PRIVATECHATOUT,
            ChatMessageType.MODPRIVATECHAT
    );

    private static final Set<ChatMessageType> SENDER_TYPES = EnumSet.of(
            ChatMessageType.PRIVATECHAT,
            ChatMessageType.PRIVATECHATOUT,
            ChatMessageType.MODPRIVATECHAT,
            ChatMessageType.PUBLICCHAT,
            ChatMessageType.MODCHAT,
            ChatMessageType.AUTOTYPER,
            ChatMessageType.MODAUTOTYPER,
            ChatMessageType.FRIENDSCHAT,
            ChatMessageType.CLAN_CHAT,
            ChatMessageType.CLAN_GUEST_CHAT,
            ChatMessageType.CLAN_GIM_CHAT
    );

    /** All message types the plugin will capture into the shared pool. */
    private static final Set<ChatMessageType> ALL_SUPPORTED_TYPES = EnumSet.of(
            // Game
            ChatMessageType.GAMEMESSAGE, ChatMessageType.SPAM, ChatMessageType.CONSOLE,
            ChatMessageType.WELCOME, ChatMessageType.BROADCAST, ChatMessageType.DIDYOUKNOW,
            ChatMessageType.ENGINE, ChatMessageType.UNKNOWN,
            ChatMessageType.PLAYERRELATED, ChatMessageType.SNAPSHOTFEEDBACK,
            ChatMessageType.FRIENDNOTIFICATION, ChatMessageType.FRIENDSCHATNOTIFICATION,
            ChatMessageType.IGNORENOTIFICATION,
            ChatMessageType.ITEM_EXAMINE, ChatMessageType.NPC_EXAMINE, ChatMessageType.OBJECT_EXAMINE,
            ChatMessageType.CLAN_MESSAGE, ChatMessageType.CLAN_GUEST_MESSAGE,
            ChatMessageType.CLAN_GIM_MESSAGE, ChatMessageType.CLAN_CREATION_INVITATION,
            // Trade
            ChatMessageType.TRADE, ChatMessageType.TRADE_SENT, ChatMessageType.TRADEREQ,
            ChatMessageType.CHALREQ_TRADE, ChatMessageType.CHALREQ_FRIENDSCHAT,
            // Public
            ChatMessageType.PUBLICCHAT, ChatMessageType.MODCHAT,
            ChatMessageType.AUTOTYPER, ChatMessageType.MODAUTOTYPER,
            // Friends chat
            ChatMessageType.FRIENDSCHAT,
            // Clan
            ChatMessageType.CLAN_CHAT, ChatMessageType.CLAN_GUEST_CHAT, ChatMessageType.CLAN_GIM_CHAT,
            ChatMessageType.CHALREQ_CLANCHAT,
            // Private
            ChatMessageType.PRIVATECHAT, ChatMessageType.PRIVATECHATOUT, ChatMessageType.MODPRIVATECHAT,
            ChatMessageType.LOGINLOGOUTNOTIFICATION
    );

    @Inject
    private Client client;

    @Inject
    private ChatWidgetConfig config;

    @Inject
    private ConfigManager configManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ChatColorConfig chatColorConfig;

    @Inject
    private Gson gson;

    // Shared message pool
    private final CopyOnWriteArrayList<WidgetMessage> messages = new CopyOnWriteArrayList<>();

    // Dynamic overlays
    private final List<OverlayConfig> overlayConfigs = new ArrayList<>();
    private final List<DynamicChatOverlay> overlays = new ArrayList<>();

    private NavigationButton navButton;
    private ChatWidgetPanel panel;
    private boolean pmWidgetsHidden = false;

    @Override
    protected void startUp() {
        loadOverlayConfigs();

        for (OverlayConfig oc : overlayConfigs) {
            addOverlay(oc);
        }

        updatePmWidgetVisibility();

        panel = new ChatWidgetPanel(this);
        BufferedImage icon;
        try {
            icon = ImageUtil.loadImageResource(getClass(), "/chat_icon.png");
        } catch (Exception e) {
            icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        }
        navButton = NavigationButton.builder()
                .tooltip("Chat Widgets")
                .icon(icon)
                .priority(10)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown() {
        for (DynamicChatOverlay overlay : overlays) {
            overlayManager.remove(overlay);
        }
        overlays.clear();

        if (pmWidgetsHidden) {
            setPmWidgetsHidden(false);
        }

        if (navButton != null) {
            clientToolbar.removeNavigation(navButton);
        }
    }

    // --- Overlay config management ---

    public List<OverlayConfig> getOverlayConfigs() {
        return overlayConfigs;
    }

    public void addNewOverlay() {
        OverlayConfig oc = new OverlayConfig();
        overlayConfigs.add(oc);
        addOverlay(oc);
        saveOverlayConfigs();
        if (panel != null) {
            panel.rebuild();
        }
    }

    public void removeOverlay(OverlayConfig oc) {
        overlayConfigs.remove(oc);
        DynamicChatOverlay toRemove = null;
        for (DynamicChatOverlay overlay : overlays) {
            if (overlay.getOverlayConfig() == oc) {
                toRemove = overlay;
                break;
            }
        }
        if (toRemove != null) {
            overlayManager.remove(toRemove);
            overlays.remove(toRemove);
        }
        saveOverlayConfigs();
        updatePmWidgetVisibility();
        if (panel != null) {
            panel.rebuild();
        }
    }

    public void onOverlayConfigChanged() {
        saveOverlayConfigs();
        updatePmWidgetVisibility();
        for (DynamicChatOverlay overlay : overlays) {
            overlay.setOverlayConfig(overlay.getOverlayConfig());
        }
    }

    private void addOverlay(OverlayConfig oc) {
        DynamicChatOverlay overlay = new DynamicChatOverlay(this, config, client,
                chatColorConfig, oc);
        overlays.add(overlay);
        overlayManager.add(overlay);
    }

    // --- Persistence ---

    private void loadOverlayConfigs() {
        overlayConfigs.clear();
        String json = configManager.getConfiguration(CONFIG_GROUP, OVERLAY_CONFIGS_KEY);
        if (json != null && !json.isEmpty()) {
            try {
                Type listType = new TypeToken<List<OverlayConfig>>() {}.getType();
                List<OverlayConfig> loaded = gson.fromJson(json, listType);
                if (loaded != null && !loaded.isEmpty()) {
                    overlayConfigs.addAll(loaded);
                    return;
                }
            } catch (Exception e) {
                // Fall through to defaults
            }
        }
        // First run — create defaults
        overlayConfigs.add(OverlayConfig.defaultGameOverlay());
        overlayConfigs.add(OverlayConfig.defaultPrivateOverlay());
        saveOverlayConfigs();
    }

    public void saveOverlayConfigs() {
        String json = gson.toJson(overlayConfigs);
        configManager.setConfiguration(CONFIG_GROUP, OVERLAY_CONFIGS_KEY, json);
    }

    // --- PM widget visibility ---

    private void updatePmWidgetVisibility() {
        boolean shouldHide = false;
        for (OverlayConfig oc : overlayConfigs) {
            for (ChatMessageType type : oc.getMessageTypes()) {
                if (PM_TYPES.contains(type)) {
                    shouldHide = true;
                    break;
                }
            }
            if (shouldHide) break;
        }
        if (shouldHide != pmWidgetsHidden) {
            setPmWidgetsHidden(shouldHide);
            pmWidgetsHidden = shouldHide;
        }
    }

    // --- Event handlers ---

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            updatePmWidgetVisibility();
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == InterfaceID.PM_CHAT && pmWidgetsHidden) {
            setPmWidgetsHidden(true);
        }
    }

    @Subscribe
    public void onVarClientIntChanged(VarClientIntChanged event) {
        if (event.getIndex() == VarClientID.CHAT_VIEW) {
            for (DynamicChatOverlay overlay : overlays) {
                updateSmartPosition(overlay);
            }
        }
    }

    @Subscribe
    public void onResizeableChanged(ResizeableChanged event) {
        for (DynamicChatOverlay overlay : overlays) {
            updateDefaultPosition(overlay);
            updateSmartPosition(overlay);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        String option = event.getMenuOption();
        String target = event.getMenuTarget();
        if (option == null || target == null || !option.equals("Clear")) {
            return;
        }

        if (target.endsWith(" history")) {
            String overlayName = target.substring(0, target.length() - " history".length());
            for (OverlayConfig oc : overlayConfigs) {
                if (oc.getName().equals(overlayName)) {
                    clearMessagesForTypes(oc.getMessageTypes());
                    return;
                }
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        ChatMessageType type = event.getType();

        if (!ALL_SUPPORTED_TYPES.contains(type)) {
            return;
        }

        String message = event.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        message = message.trim();

        String sender = cleanSender(event.getName());
        boolean isOutgoing = type == ChatMessageType.PRIVATECHATOUT;
        boolean isBossKc = BOSS_KC_PATTERN.matcher(message).find();

        // Handle message merging for game-type messages
        if (!SENDER_TYPES.contains(type) && type != ChatMessageType.LOGINLOGOUTNOTIFICATION) {
            for (MessageMergeRule rule : MESSAGE_MERGE_RULES) {
                if (rule.matchesPreviousPrefix(message)) {
                    message = message.replace("<br>", " ");
                    break;
                }
            }

            if (!messages.isEmpty()) {
                WidgetMessage lastMsg = messages.get(messages.size() - 1);
                String merged = tryMergeMessages(lastMsg.getMessage(), message);
                if (merged != null) {
                    WidgetMessage mergedMsg = WidgetMessage.gameMessage(
                            merged, System.currentTimeMillis(), lastMsg.getType(), lastMsg.isBossKc());
                    messages.set(messages.size() - 1, mergedMsg);
                    return;
                }
            }
        }

        // Handle login notifications with custom fade
        if (type == ChatMessageType.LOGINLOGOUTNOTIFICATION) {
            int maxFade = 5;
            messages.add(WidgetMessage.loginNotification(
                    sender != null ? sender : "System", message, System.currentTimeMillis(), maxFade));
        } else if (SENDER_TYPES.contains(type)) {
            messages.add(WidgetMessage.senderMessage(
                    sender != null ? sender : "Unknown", message, System.currentTimeMillis(), type, isOutgoing));
        } else {
            messages.add(WidgetMessage.gameMessage(message, System.currentTimeMillis(), type, isBossKc));
        }

        while (messages.size() > MAX_POOL_SIZE) {
            messages.remove(0);
        }
    }

    // --- Message access for overlays ---

    public List<WidgetMessage> getMessagesForOverlay(OverlayConfig overlayConfig) {
        Set<ChatMessageType> types = overlayConfig.getMessageTypes();
        int size = messages.size();
        if (size == 0 || types.isEmpty()) {
            return new ArrayList<>(0);
        }

        long currentTime = System.currentTimeMillis();
        int fadeOutDuration = overlayConfig.getFadeOutDuration();
        long fadeOutThreshold = fadeOutDuration > 0 ? (fadeOutDuration * 2000L) + 2000 : 0;
        boolean gameFilterEnabled = isGameFilterEnabled();
        boolean bossKcFilterEnabled = isBossKcFilterEnabled();

        int maxMessages = overlayConfig.getMaxMessages();
        List<WidgetMessage> filtered = new ArrayList<>(maxMessages);
        int msgCount = 0;

        for (int i = size - 1; i >= 0; i--) {
            WidgetMessage msg = messages.get(i);

            if (!types.contains(msg.getType())) {
                continue;
            }

            if (fadeOutThreshold > 0) {
                int msgMaxFade = msg.getMaxFadeSeconds();
                long threshold = msgMaxFade > 0 ? (msgMaxFade * 1000L) + 2000 : fadeOutThreshold;
                if (currentTime - msg.getTimestamp() >= threshold) {
                    continue;
                }
            }

            if (gameFilterEnabled && msg.getType() == ChatMessageType.SPAM) {
                continue;
            }

            if (bossKcFilterEnabled && msg.isBossKc()) {
                continue;
            }

            // Login notifications don't count against max
            boolean isLoginNotification = msg.getType() == ChatMessageType.LOGINLOGOUTNOTIFICATION;
            if (!isLoginNotification && msgCount >= maxMessages) {
                continue;
            }

            filtered.add(0, msg);
            if (!isLoginNotification) {
                msgCount++;
            }
        }

        return filtered;
    }

    public void clearMessagesForTypes(Set<ChatMessageType> types) {
        messages.removeIf(msg -> types.contains(msg.getType()));
    }

    public void clearAllMessages() {
        messages.clear();
    }

    // --- Utility ---

    private String cleanSender(String name) {
        if (name == null) {
            return null;
        }
        return name.replace('\u00A0', ' ').trim();
    }

    private String tryMergeMessages(String previousMessage, String newMessage) {
        for (MessageMergeRule rule : MESSAGE_MERGE_RULES) {
            if (rule.matches(previousMessage, newMessage)) {
                return rule.merge(previousMessage, newMessage);
            }
        }
        return null;
    }

    public boolean isChatboxHidden() {
        return isChatboxMinimized() || isChatboxWidgetHidden();
    }

    private boolean isChatboxMinimized() {
        return client.getVarcIntValue(VarClientID.CHAT_VIEW) == 1337;
    }

    private boolean isChatboxWidgetHidden() {
        Widget chatboxWidget = client.getWidget(InterfaceID.Chatbox.CHATAREA);
        return (chatboxWidget != null && chatboxWidget.isHidden());
    }

    public boolean isGameFilterEnabled() {
        return client.getVarbitValue(VarbitID.GAME_FILTER) == 1;
    }

    public boolean isBossKcFilterEnabled() {
        return client.getVarbitValue(VarbitID.BOSS_KILLCOUNT_FILTERED) == 1;
    }

    private void updateDefaultPosition(net.runelite.client.ui.overlay.Overlay overlay) {
        OverlayPosition defaultPos = client.isResized()
                ? OverlayPosition.ABOVE_CHATBOX_RIGHT
                : OverlayPosition.BOTTOM_LEFT;
        overlay.setPosition(defaultPos);
    }

    private void updateSmartPosition(net.runelite.client.ui.overlay.Overlay overlay) {
        if (!config.smartPositioning()) {
            return;
        }

        OverlayPosition currentPos = overlay.getPreferredPosition() != null
                ? overlay.getPreferredPosition()
                : overlay.getPosition();

        if (isTopPosition(currentPos)) {
            return;
        }

        OverlayPosition targetPos;
        if (!client.isResized()) {
            targetPos = OverlayPosition.BOTTOM_LEFT;
        } else if (isChatboxHidden()) {
            targetPos = OverlayPosition.ABOVE_CHATBOX_RIGHT;
        } else {
            return;
        }

        if (currentPos != targetPos) {
            overlay.setPreferredPosition(targetPos);
        }
    }

    private boolean isTopPosition(OverlayPosition position) {
        return position == OverlayPosition.TOP_CENTER
                || position == OverlayPosition.TOP_RIGHT
                || position == OverlayPosition.TOP_LEFT
                || position == OverlayPosition.CANVAS_TOP_RIGHT;
    }

    private void setPmWidgetsHidden(boolean hidden) {
        setGameframePmContainerHidden(InterfaceID.TOPLEVEL, 36, hidden);
        setGameframePmContainerHidden(InterfaceID.TOPLEVEL_OSRS_STRETCH, 93, hidden);
        setGameframePmContainerHidden(InterfaceID.TOPLEVEL_PRE_EOC, 90, hidden);

        Widget pmContainer = client.getWidget(InterfaceID.PM_CHAT, 0);
        if (pmContainer != null) {
            pmContainer.setHidden(hidden);
            Widget[] dynamicChildren = pmContainer.getDynamicChildren();
            if (dynamicChildren != null) {
                for (Widget child : dynamicChildren) {
                    if (child != null) {
                        child.setHidden(hidden);
                    }
                }
            }
        }
    }

    private void setGameframePmContainerHidden(int group, int child, boolean hidden) {
        Widget container = client.getWidget(group, child);
        if (container != null) {
            container.setHidden(hidden);
        }
    }

    @Provides
    ChatWidgetConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChatWidgetConfig.class);
    }

    boolean isHoveringGameChatControls() {
        final Point mouse = client.getMouseCanvasPosition();
        return isHoveringWidget(mouse, client.getWidget(InterfaceID.Chatbox.CHAT_ALL)) ||
                isHoveringWidget(mouse, client.getWidget(InterfaceID.Chatbox.CHAT_GAME)) ||
                isHoveringWidget(mouse, client.getWidget(InterfaceID.Chatbox.CHAT_PUBLIC));
    }

    boolean isHoveringWidget(Point mouse, Widget target) {
        if (target == null || target.isHidden()) {
            return false;
        }
        return target.getBounds().contains(mouse.getX(), mouse.getY());
    }
}
