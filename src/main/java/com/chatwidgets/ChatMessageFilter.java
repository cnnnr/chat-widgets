package com.chatwidgets;

import net.runelite.client.plugins.chatfilter.ChatFilterConfig;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Matches widget messages against RuneLite's built-in Chat Filter plugin's word/regex lists,
 * mirroring {@code ChatFilterPlugin.updateFilteredPatterns()} and the normalization in
 * {@code censorMessage(username, message)}. Only the Filtered-words and Filtered-regex lists (plus
 * stripAccents) are used; name-based filtering and per-type gating flags are not ported. A match
 * means the message should be removed from the widgets entirely.
 *
 * <p>Patterns are (re)compiled via {@link #rebuild(ChatFilterConfig)} whenever the Chat Filter
 * config changes; {@link #matches(String)} runs per captured message.
 */
public class ChatMessageFilter {

    private List<Pattern> patterns = new ArrayList<>();
    private boolean stripAccents = false;

    /** Rebuilds the compiled pattern list from the Chat Filter plugin's live config. */
    public void rebuild(ChatFilterConfig cfg) {
        List<Pattern> compiled = new ArrayList<>();
        this.stripAccents = cfg.stripAccents();

        Text.fromCSV(cfg.filteredWords()).stream()
                .map(this::maybeStripAccents)
                .map(s -> Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE))
                .forEach(compiled::add);

        for (String line : cfg.filteredRegex().split("\n")) {
            String trimmed = maybeStripAccents(line.trim());
            if (trimmed.isEmpty()) {
                continue;
            }
            Pattern p = compilePattern(trimmed);
            if (p != null) {
                compiled.add(p);
            }
        }

        this.patterns = compiled;
    }

    /** Returns true when the message matches any filtered word/regex (i.e. should be removed). */
    public boolean matches(String message) {
        if (message == null || patterns.isEmpty()) {
            return false;
        }

        // Normalize like ChatFilterPlugin: the first replace() arg is a non-breaking space (U+00A0).
        String normalized = maybeStripAccents(message
                .replace(' ', ' ')
                .replace("<lt>", "<")
                .replace("<gt>", ">"));

        for (Pattern pattern : patterns) {
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    private String maybeStripAccents(String input) {
        return stripAccents ? StringUtils.stripAccents(input) : input;
    }

    private static Pattern compilePattern(String pattern) {
        try {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ex) {
            return null;
        }
    }
}
