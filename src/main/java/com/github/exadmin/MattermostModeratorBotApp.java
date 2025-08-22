package com.github.exadmin;

import com.github.exadmin.aibot.mattermost.api.MattermostEventListener;
import com.github.exadmin.aibot.mattermost.api.MattermostPost;
import com.github.exadmin.aibot.mattermost.api.MattermostWebsocketDispatcher;
import com.github.exadmin.aibot.mattermost.async.MatterMostAsyncClientFactory;
import com.github.exadmin.aibot.mattermost.event.IMattermostEvent;
import com.github.exadmin.aibot.utils.StrUtils;
import net.bis5.mattermost.client4.ApiResponse;
import net.bis5.mattermost.model.Channel;
import net.bis5.mattermost.model.Post;
import net.bis5.mattermost.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class MattermostModeratorBotApp extends MattermostEventListener {
    private static final String BOT_SELF_EMAIL = "moderator-bot@localhost"; // todo: move to settings later
    private static final String MM_URL = "MATTERMOST_URL";
    private static final String MM_TOKEN = "MATTERMOST_TOKEN";
    private static final String ALLOWED_EMAIL = "ALLOWED_EMAIL";

    private static final Logger log = LoggerFactory.getLogger(MattermostModeratorBotApp.class);
    private User meBotUser;
    private final Map<String, String> usersMapCache = new ConcurrentHashMap<>(); // cache for userId -> userEmail
    private final MatterMostAsyncClientFactory mmClientFactory = new MatterMostAsyncClientFactory();
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> allowedEmails = new HashSet<>();

    public static void main(String[] args) throws Exception {
        // try read settings from Env variables
        String mmDomain = System.getenv(MM_URL);
        String mmToken = System.getenv(MM_TOKEN);
        String allowedEmail = System.getenv(ALLOWED_EMAIL);

        if (args.length == 1) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(args[0]));
            mmDomain = properties.getProperty(MM_URL);
            mmToken = properties.getProperty(MM_TOKEN);
            allowedEmail = properties.getProperty(ALLOWED_EMAIL);
        }

        // if settings file is specified - then use values from it with priority
        if (StrUtils.hasNoValueOneOf(mmDomain, mmToken)) {
            log.warn("The application must be started with required settings set. Either via System Environment variables or via properties file.");
            log.warn("If properties file is used then path to it must be provided as a program argument, i.e. java -jar xxx.jar ${PATH_TP_FILE}");
            log.warn("If system environment variables are used - then all variables must have values");
            log.warn("List of necessary variables with examples:");
            log.warn(MM_URL + "=mattermost.domain.org (don't add 'https://' prefix)");
            log.warn(MM_TOKEN + "=secret_token_value");
            log.warn(ALLOWED_EMAIL + "=example@gmail.com");

            System.exit(-1);
        }

        MattermostModeratorBotApp app = new MattermostModeratorBotApp();
        app.run(mmDomain, mmToken, allowedEmail);
    }

    private void addAllowedEmail(String email) {
        allowedEmails.add(email);
    }

    private void run(String mmDomain, String mmToken, String allowedEmail) {
        addAllowedEmail(allowedEmail);
        addAllowedEmail(BOT_SELF_EMAIL);

        String mmUrl = "https://" + mmDomain;
        log.debug("Connecting to Mattermost instance by {}", mmUrl);

        mmClientFactory.startMattermostClient(mmUrl, mmToken);

        MattermostWebsocketDispatcher wsClient = new MattermostWebsocketDispatcher(mmDomain, true, mmToken, this);
        wsClient.init();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ex) {
            log.warn("Terminating application");
            wsClient.destroy();
        }
    }

    @Override
    public void onMessage(Post post) {
        // test connection & remember bot-id
        if (meBotUser == null) {
            fetchThisBotData();
        }

        String senderId = post.getUserId();
        String senderEmail = fetchUserEmail(senderId);

        log.debug("Checking message from user {}", senderEmail);

        // if message is sent by allowed user - then do nothing
        if (allowedEmails.contains(senderEmail)) return;

        // otherwise - delete message and send it back to the sender (if it's valuable)
        ApiResponse<Boolean> booleanApiResponse = mmClientFactory.getClient().deletePost(post.getId());
        log.info("Deleting post from user {}, successfully = {}", senderEmail, booleanApiResponse.readEntity());

        ApiResponse<Channel> channelApiResponse = mmClientFactory.getClient().createDirectChannel(senderId, meBotUser.getId());
        Channel channel = channelApiResponse.readEntity();

        Post reply = new MattermostPost();
        reply.setMessage("Your message in a channel {} was deleted by moderator-bot due to rules of administrator.\n" +
                "Here is your original text:\n\n" +
                post.getMessage());

        reply.setChannelId(channel.getId());
        mmClientFactory.getClient().createPost(reply);
    }

    @Override
    public void onOther(IMattermostEvent post) {
        // log.debug("Other message: {}", post);
    }

    private String fetchUserEmail(final String userId) {
        String cachedValue = usersMapCache.get(userId);
        if (cachedValue != null) return cachedValue;

        ApiResponse<User> userApiResponse = mmClientFactory.getClient().getUser(userId);
        User user = userApiResponse.readEntity();
        String userEmail = user.getEmail();

        usersMapCache.put(userId, userEmail);
        return userEmail;
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