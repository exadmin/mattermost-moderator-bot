package com.github.exadmin.aibot.report;

public enum MMReportStatus {
    GREEN(":white_check_mark:", "OK"),
    YELLOW(":warning:", "WARN"),
    RED(":interrobang:", "BAD"),
    UNDEFINED(":grey_question:", "UNDEFINED");

    private final String mmIcon;
    private final String text;

    MMReportStatus(String mmIcon, String text) {
        this.mmIcon = mmIcon;
        this.text   = text;
    }

    public String getMmIcon() {
        return mmIcon;
    }

    public String getText() {
        return text;
    }
}
