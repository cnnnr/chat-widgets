package com.chatwidgets;

import java.util.regex.Pattern;

public class MessageMergeRule {
    private final String previousPrefix;
    private final String nextPattern;
    private final boolean exactMatch;
    private final Pattern regexPattern;

    public MessageMergeRule(String previousPrefix, String nextPattern, boolean exactMatch) {
        this.previousPrefix = previousPrefix;
        this.nextPattern = nextPattern;
        this.exactMatch = exactMatch;
        this.regexPattern = null;
    }

    public MessageMergeRule(String previousPrefix, Pattern regexPattern) {
        this.previousPrefix = previousPrefix;
        this.nextPattern = null;
        this.exactMatch = false;
        this.regexPattern = regexPattern;
    }

    public boolean matches(String previousMessage, String newMessage) {
        if (!previousMessage.startsWith(previousPrefix)) {
            return false;
        }
        if (regexPattern != null) {
            return regexPattern.matcher(newMessage).matches();
        }
        return exactMatch ? newMessage.equals(nextPattern) : newMessage.startsWith(nextPattern);
    }

    public String merge(String previousMessage, String newMessage) {
        return previousMessage + " " + newMessage;
    }

    public boolean matchesPreviousPrefix(String message) {
        return message.startsWith(previousPrefix);
    }
}
