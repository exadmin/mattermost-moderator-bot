package com.github.exadmin.aibot;

import net.bis5.mattermost.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppContext {
    private String mmDomain;
    private String mmToken;
    private List<String> allowedEmails;
    private List<String> monitoredChannels;
    private List<String> emailsToReportToUsersWith;
    private User botUserProfile;
    private boolean isLocalDevMode;

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
        if (allowedEmails == null) allowedEmails = new ArrayList<>();
        allowedEmails.add(allowedEmail);
    }

    public void addMonitoredChannels(String monitoredChannel) {
        if (monitoredChannels == null) monitoredChannels = new ArrayList<>();
        monitoredChannels.add(monitoredChannel);
    }

    public boolean isSuccessfullyLoaded() {
        return successfullyLoaded;
    }

    public void setSuccessfullyLoaded(boolean successfullyLoaded) {
        this.successfullyLoaded = successfullyLoaded;
    }

    public void addEmailToReportToUserWith(String email) {
        if (emailsToReportToUsersWith == null) emailsToReportToUsersWith = new ArrayList<>();
        emailsToReportToUsersWith.add(email);
    }

    public List<String> getEmailsToReportToUsersWith() {
        return emailsToReportToUsersWith;
    }

    public User getBotUserProfile() {
        return botUserProfile;
    }

    public void setBotUserProfile(User botUserProfile) {
        this.botUserProfile = botUserProfile;
    }

    public boolean isLocalDevMode() {
        return isLocalDevMode;
    }

    public void setLocalDevMode(boolean localDevMode) {
        isLocalDevMode = localDevMode;
    }
}
