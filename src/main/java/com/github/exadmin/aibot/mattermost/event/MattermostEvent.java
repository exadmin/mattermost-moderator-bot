package com.github.exadmin.aibot.mattermost.event;

import com.github.exadmin.aibot.mattermost.api.MattermostPost;

import java.util.Map;
import java.util.Optional;

public class MattermostEvent implements IMattermostEvent {
    private String event;
    private Map<String, Object> data;
    private MattermostEventBroadcast broadcast;
    private long seq;

    public Optional<MattermostPost> getPost()
    {
        if (null == data)
            return Optional.empty();

        final Object post = data.get("post");
        if (null == post)
            return Optional.empty();

        if (post instanceof String) {
            return Optional.of(MattermostPost.fromString((String)post));
        }

        return Optional.empty();
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public MattermostEventBroadcast getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(MattermostEventBroadcast broadcast) {
        this.broadcast = broadcast;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
