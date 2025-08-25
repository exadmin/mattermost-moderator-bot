package com.github.exadmin.aibot;

import com.github.exadmin.aibot.mattermost.api.MattermostEventListener;
import com.github.exadmin.aibot.mattermost.api.MattermostPost;
import com.github.exadmin.aibot.mattermost.api.MattermostWebsocketDispatcher;
import com.github.exadmin.aibot.mattermost.async.MatterMostAsyncClientFactory;
import com.github.exadmin.aibot.mattermost.event.IMattermostEvent;
import net.bis5.mattermost.client4.ApiResponse;
import net.bis5.mattermost.model.Channel;
import net.bis5.mattermost.model.Post;
import net.bis5.mattermost.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TheBot extends MattermostEventListener {
    private static final Logger log = LoggerFactory.getLogger(TheBot.class);

    private User meBotUser;
    private final Map<String, String> usersCache = new ConcurrentHashMap<>(); // cache for userId -> userEmail
    private final Map<String, String> channelsCache = new ConcurrentHashMap<>(); // cache for channelId -> channel name

    private final MatterMostAsyncClientFactory mmClientFactory = new MatterMostAsyncClientFactory();
    private final ReentrantLock lock = new ReentrantLock();
    private final AppContext appContext;

    public TheBot(AppContext appContext) {
        this.appContext = appContext;
    }

    public void run() {
        String mmUrl = "https://" + appContext.getMmDomain();
        log.debug("Connecting to Mattermost instance by {}", mmUrl);

        mmClientFactory.startMattermostClient(mmUrl, appContext.getMmToken());

        MattermostWebsocketDispatcher wsClient = new MattermostWebsocketDispatcher(appContext.getMmDomain(), true, appContext.getMmToken(), this);
        wsClient.init();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ex) {
            log.warn("Terminating application");
        } finally {
            wsClient.destroy();
        }
    }

    @Override
    public void onMessage(Post post) {
        // test connection & remember bot-id
        if (meBotUser == null) {
            fetchThisBotData();
        }

        String channelId = post.getChannelId();
        String channelName = fetchChannelName(channelId);

        // check if channel belongs to list of monitored channels
        if (appContext.getMonitoredChannels().isEmpty() /*trigger in each channel*/ || appContext.getMonitoredChannels().contains(channelName)) {

            // if message is sent by allowed user - then do nothing
            String senderId = post.getUserId();
            String senderEmail = fetchUserEmail(senderId);

            if (appContext.getAllowedEmails().contains(senderEmail)) return;

            // otherwise - delete message and send it back to the sender (if it's valuable)
            ApiResponse<Boolean> booleanApiResponse = mmClientFactory.getClient().deletePost(post.getId());
            log.info("Deleting post from user '{}', in channel '{}', result = {}", senderEmail, channelName, booleanApiResponse.readEntity());

            ApiResponse<Channel> channelApiResponse = mmClientFactory.getClient().createDirectChannel(senderId, meBotUser.getId());
            Channel channel = channelApiResponse.readEntity();

            Post reply = new MattermostPost();
            reply.setMessage("Your message in a channel '" + channelName + "' was deleted by moderator-bot due to rules of administrator.\n" +
                    "Here is your original text:\n\n" +
                    post.getMessage());

            reply.setChannelId(channel.getId());
            mmClientFactory.getClient().createPost(reply);
        }
    }

    @Override
    public void onOther(IMattermostEvent post) {
        // log.debug("Other message: {}", post);
    }

    private String fetchUserEmail(final String userId) {
        String cachedValue = usersCache.get(userId);
        if (cachedValue != null) return cachedValue;

        ApiResponse<User> userApiResponse = mmClientFactory.getClient().getUser(userId);
        User user = userApiResponse.readEntity();
        String userEmail = user.getEmail();

        usersCache.put(userId, userEmail);
        return userEmail;
    }

    private String fetchChannelName(final String channelId) {
        String cachedValue = channelsCache.get(channelId);
        if (cachedValue != null) return cachedValue;

        ApiResponse<Channel> channelApiResponse = mmClientFactory.getClient().getChannel(channelId);
        Channel channel = channelApiResponse.readEntity();

        String channelName = channel.getName();
        channelsCache.put(channelId, channelName);

        return channelName;
    }

    private void fetchThisBotData() {
        try {
            lock.lock();
            if (meBotUser == null) {
                ApiResponse<User> meApiResponse = mmClientFactory.getClient().getMe();
                meBotUser = meApiResponse.readEntity();
            }
        } finally {
            lock.unlock();
        }

    }
}
