package com.github.exadmin.aibot.mattermost.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bis5.mattermost.model.Post;

import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MattermostPost extends Post {

    private static final ObjectMapper mapper = new ObjectMapper();

    public MattermostPost() {
        super();
    }

    public MattermostPost(String channelId, String message) {
        super(channelId, message);
    }

    public static MattermostPost fromString(String postString) {
        try {
            return mapper.readValue(postString, MattermostPost.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}