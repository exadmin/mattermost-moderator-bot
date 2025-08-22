package com.github.exadmin.aibot.mattermost.event;

public enum EventType {
    HELLO("hello"),
    STATUS_CHANGE("status_change"),
    TYPING("typing"),
    POSTED("posted");

    private final String name;

    EventType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
