package com.chatwidgets;

public enum WidgetPosition {
    WIDGET("Widget"),
    BELOW_PLAYER("Below Player"),
    ABOVE_PLAYER("Above Player");

    private final String name;

    WidgetPosition(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
