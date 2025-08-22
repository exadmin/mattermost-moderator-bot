package com.github.exadmin.aibot.mattermost.api;

import com.github.exadmin.aibot.mattermost.event.IMattermostEvent;
import com.github.exadmin.aibot.mattermost.event.MattermostEvent;
import net.bis5.mattermost.model.Post;

import java.util.Optional;

import static com.github.exadmin.aibot.mattermost.event.EventType.POSTED;

public abstract class MattermostEventListener {
    public abstract void onMessage(Post post);
    public abstract void onOther(IMattermostEvent post);

    public final void onEvent(MattermostEvent event) {
        String eventType = event.getEvent();
        Optional<MattermostPost> postOpt = event.getPost();

        if (postOpt.isPresent()) {
            MattermostPost post = postOpt.get();

            if (POSTED.toString().equals(eventType)) {
                onMessage(post);
                return;
            }
        }

        onOther(event);
    }
}