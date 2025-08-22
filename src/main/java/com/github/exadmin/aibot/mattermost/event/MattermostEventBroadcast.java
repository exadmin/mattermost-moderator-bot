package com.github.exadmin.aibot.mattermost.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MattermostEventBroadcast {
    @JsonProperty("omit_users")
    private Map<String, Boolean> omitUsers;
    private String userId;
    private String channelId;
    private String teamId;

    public Map<String, Boolean> getOmitUsers() {
        return omitUsers;
    }

    public void setOmitUsers(Map<String, Boolean> omitUsers) {
        this.omitUsers = omitUsers;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
