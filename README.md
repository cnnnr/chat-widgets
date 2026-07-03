# Chat Widgets

**Version 1.2.0**

Create custom chat widgets that display any combination of message types. Each widget is independently configurable with its own message filters, positioning, fade behaviour, and more. Manage widgets from the sidebar panel — add, remove, reorder, and configure as many as you need.

## Examples

Display options are quite flexible. Below are some configuration examples.

### Positioning

**Standard Overlay**

- **Game Messages:** Position set to "Default" (user-specified) with a bottom margin applied to clear infoboxes.
- **Private Messages:** Anchored to the top-left with a top margin applied.

![default](https://github.com/user-attachments/assets/ebfb08a7-c049-480c-ad75-618f8cf4c056)

**Relative to Player**

- **Position:** Above Player (0 margin applied)
- **Fade Out Duration:** 3s
- **Max Messages:** 1

<video src="https://github.com/user-attachments/assets/61abed62-1deb-4149-988c-a731826156f1" style="width: 100%"></video>

## Configuration

Settings are split between the RuneLite config panel (global) and the sidebar panel (per-widget).

### Global Settings (Config Panel)

| Setting                  | Description                                                                  |
| :----------------------- | :--------------------------------------------------------------------------- |
| **Hide Side Panel**      | Hide the Chat Widgets panel from the sidebar.                                |
| **Text Shadow**          | Draw a shadow behind text for better readability.                            |
| **Wrap Text**            | Wrap long messages to multiple lines instead of truncating.                  |
| **Hide Private Chat**    | Hide the default split private chat widget.                                  |
| **Smart Positioning**    | Automatically reposition widgets based on client mode and chatbox state.     |
| **Collapse Duplicates**  | Merge consecutive identical messages into one with a count.                  |
| **Show Channel Names**   | Show the channel name prefix for friends and clan chat messages.             |
| **Use Chat Filter**      | Hide/censor widget messages using RuneLite's Chat Filter plugin's Filtered words & regex lists and its Filter Type.  |
| **Timestamp Format**     | Format string for timestamps, shared by all widgets (e.g., `[HH:mm:ss]`, `[HH:mm]`). Enable timestamps per widget below. |

#### Message Colours

Per-category text colours for: Game, Public, Private, Friends, Clan, Guest Clan, GIM Clan, Trade, Challenge, Did You Know, Broadcast, and Autochat.

### Per-Widget Settings (Sidebar Panel)

Each widget is configured independently from the sidebar panel.

| Setting                  | Description                                                                                                                                                                                     |
| :----------------------- |:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Title**                | Display name for the widget.                                                                                                                                                                    |
| **Show**                 | Enable or disable the widget.                                                                                                                                                                   |
| **Message Types**        | Which message categories this widget displays (Game, Public Chat, Private Chat, Friends Chat, Clan Chat, etc.).                                                                                 |
| **Always Visible**       | When enabled, the widget is always shown. When disabled, it only appears when the chatbox is minimized.                                                                                         |
| **Contextual Colours**   | Retain colour formatting from in-game messages (e.g. coloured boss kill messages, music tracks).                                                                                                |
| **Background**           | Background colour drawn behind this widget's text. Supports transparency via the colour picker's alpha slider; fully transparent (default) = no background.                                     |
| **Show Timestamps**      | Prefix this widget's messages with a timestamp (default off). Uses the global **Timestamp Format**.                                                                                             |
| **Show Input Preview**   | Draw a live preview of what you're typing (icon, name, cursor) at the bottom of this widget.                                                                                                    |
| **Font Size**            | Font size for this widget's messages (Regular, Small). Default Regular.                                                                                                                         |
| **Max Messages**         | Maximum number of messages visible in the widget (1–20).                                                                                                                                        |
| **Fade Out (sec)**       | Seconds before messages start fading out (0 = never fade). Messages fully disappear after 2x this duration.                                                                                     |
| **Position**             | Widget position mode. `Widget` uses standard overlay positioning. `Below Player` and `Above Player` position the widget relative to your character (works best with fade and low max messages). |
| **Dynamic Height**       | Widget height adjusts based on message count rather than using fixed height. Disable this if you notice a subtle jitter effect when Fade Out is enabled.                                        |
| **Margin Top**           | Extra spacing above the widget (0–200).                                                                                                                                                         |
| **Margin Bottom**        | Extra spacing below the widget (0–200).                                                                                                                                                         |

## Tips

If you want to anchor a chat widget in the bottom left above the chatbox, use the anchor fixed to the **right** of the chatbox. This anchor point stacks widgets vertically, unlike the bottom left which stacks widgets horizontally.

<img width="1036" height="250" alt="image" src="https://github.com/user-attachments/assets/fa24e015-b43d-48ef-90b9-e843fc3651ce" />
