package com.github.exadmin.aibot.mattermost.event;

import com.github.exadmin.aibot.mattermost.api.MattermostPost;

import java.util.Map;
import java.util.Optional;

public interface IMattermostEvent {
    Optional<MattermostPost> getPost();

    Map<String, Object> getData();
}