package com.github.exadmin.aibot;

import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.mattermost.api.MattermostEventListener;
import com.github.exadmin.aibot.mattermost.api.MattermostPost;
import com.github.exadmin.aibot.mattermost.api.MattermostWebsocketDispatcher;
import com.github.exadmin.aibot.mattermost.async.MatterMostAsyncClientFactory;
import com.github.exadmin.aibot.mattermost.event.IMattermostEvent;
import com.github.exadmin.aibot.tasks.PeriodicalTasksRegistry;
import com.github.exadmin.utils.MiscUtils;
import net.bis5.mattermost.client4.ApiResponse;
import net.bis5.mattermost.model.Channel;
import net.bis5.mattermost.model.Post;
import net.bis5.mattermost.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TheBot extends MattermostEventListener {
    private static final Logger log = LoggerFactory.getLogger(TheBot.class);

    /* caches can be unified - in case all IDs are unique - but currently not sure about that */
    private final Map<String, String> usersCache = new ConcurrentHashMap<>(); // cache for userId -> userEmail
    private final Map<String, String> channelsCache = new ConcurrentHashMap<>(); // cache for channelId -> channel name
    private final Map<String, String> channelsWithBotCache = new ConcurrentHashMap<>(); // cache for bot_id + user_id -> channelId

    private final MatterMostAsyncClientFactory mmClientFactory = new MatterMostAsyncClientFactory();
    private final AppContext appContext;

    public TheBot(AppContext appContext) {
        this.appContext = appContext;
    }
    private MatterMostClientPomogator mmPomogator;

    public void run() {
        String mmUrl = "https://" + appContext.getMmDomain();
        log.debug("Connecting to Mattermost instance by {}", mmUrl);

        mmClientFactory.startMattermostClient(mmUrl, appContext.getMmToken());

        MattermostWebsocketDispatcher wsClient = new MattermostWebsocketDispatcher(appContext.getMmDomain(), true, appContext.getMmToken(), this);
        wsClient.init();

        // schedule periodical tasks execution
        int timeOutMilliSeconds = 10000;
        while (mmClientFactory.getClient() == null && timeOutMilliSeconds > 0) {
            MiscUtils.sleep(100);
            timeOutMilliSeconds = timeOutMilliSeconds - 100;
        }

        mmPomogator = new MatterMostClientPomogator(mmClientFactory.getClient());

        PeriodicalTasksRegistry tasksRegistry = new PeriodicalTasksRegistry(mmPomogator, appContext);
        tasksRegistry.scheduleAndRunAsync(appContext.isLocalDevMode());

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

        User botUser = mmPomogator.getBotProfile();

        // if sender - is the bot itself - then ignore this post
        final String senderId = post.getUserId();
        if (botUser.getId().equals(senderId)) return;

        // check if message was sent directly to the bot - then reply with current time string
        final String botAndUserChannelId = fetchChannelIdForBotAndUser(botUser, senderId);
        if (post.getChannelId().equals(botAndUserChannelId)) {
            sendMessage(botAndUserChannelId, "Hello! The bot is functioning well. Server time is " + MiscUtils.getCurrentTimeStr());
            return;
        }

        final String channelName = fetchChannelName(post.getChannelId());

        // check if channel belongs to list of monitored channels
        if (appContext.getMonitoredChannels().isEmpty() /*trigger in each channel*/ || appContext.getMonitoredChannels().contains(channelName)) {

            // if message is sent by allowed user - then do nothing
            String senderEmail = fetchUserEmail(senderId);
            if (appContext.getAllowedEmails().contains(senderEmail)) return;

            // otherwise - delete message and send it back to the sender (if it's valuable)
            ApiResponse<Boolean> booleanApiResponse = mmClientFactory.getClient().deletePost(post.getId());
            log.info("Deleting post from user '{}', in channel '{}', result = {}", senderEmail, channelName, booleanApiResponse.readEntity());


            sendMessage(botAndUserChannelId, "Your message in a channel '" + channelName + "' was deleted by moderator-bot due to rules of channel administrator.\n" +
                    "Here is your original text:\n\n" +
                    post.getMessage());
        }
    }

    @Override
    public void onOther(IMattermostEvent post) {
        log.trace("Other message: {}", post);
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

    private String fetchChannelIdForBotAndUser(final User bot, final String userId) {
        String commonId = bot.getId() + "_" + userId;
        String cachedValue = channelsWithBotCache.get(commonId);
        if (cachedValue != null) return cachedValue;

        ApiResponse<Channel> channelApiResponse = mmClientFactory.getClient().createDirectChannel(bot.getId(), userId);
        Channel channel = channelApiResponse.readEntity();
        channelsWithBotCache.put(commonId, channel.getId());
        return channel.getId();
    }

    private void sendMessage(String destChannelId, String msg) {
        Post reply = new MattermostPost();
        reply.setMessage(msg);
        reply.setChannelId(destChannelId);
        mmClientFactory.getClient().createPost(reply);
    }
}
