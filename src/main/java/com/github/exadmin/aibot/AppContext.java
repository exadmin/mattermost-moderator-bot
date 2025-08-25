package com.github.exadmin.aibot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppContext {
    private String mmDomain;
    private String mmToken;
    private List<String> allowedEmails;
    private List<String> monitoredChannels;
    private boolean successfullyLoaded;

    public String getMmDomain() {
        return mmDomain;
    }

    public String getMmToken() {
        return mmToken;
    }

    public List<String> getAllowedEmails() {
        return allowedEmails;
    }

    public List<String> getMonitoredChannels() {
        if (monitoredChannels == null) return Collections.emptyList();
        return monitoredChannels;
    }

    public void setMmDomain(String mmDomain) {
        this.mmDomain = mmDomain;
    }

    public void setMmToken(String mmToken) {
        this.mmToken = mmToken;
    }

    public void addAllowedEmails(String allowedEmail) {
        if (this.allowedEmails == null) this.allowedEmails = new ArrayList<>();
        this.allowedEmails.add(allowedEmail);
    }

    public void addMonitoredChannels(String monitoredChannel) {
        if (this.monitoredChannels == null) this.monitoredChannels = new ArrayList<>();
        this.monitoredChannels.add(monitoredChannel);
    }

    public boolean isSuccessfullyLoaded() {
        return successfullyLoaded;
    }

    public void setSuccessfullyLoaded(boolean successfullyLoaded) {
        this.successfullyLoaded = successfullyLoaded;
    }
}
