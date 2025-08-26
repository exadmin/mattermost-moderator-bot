package com.github.exadmin.aibot.mattermost.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.aibot.mattermost.event.MattermostEvent;
import com.github.exadmin.utils.MiscUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MattermostWebsocketDispatcher {
    private static final Logger log = LoggerFactory.getLogger(MattermostWebsocketDispatcher.class);

    private final ObjectMapper mapper;
    private final WebSocketClient wsClient;
    private long triesCount;
    private final MattermostEventListener listener;
    private boolean reconnect;
    private ExecutorService executorService;

    public MattermostWebsocketDispatcher(String host, boolean secure, String accessToken, MattermostEventListener listener) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);

        String protocol = secure ? "wss" : "ws";

        try {
            wsClient = new WebSocketClientImpl(new URI(protocol + "://" + host + "/api/v4/websocket"), headers, this);
        } catch (URISyntaxException e) {
            log.error("Error while creating websocket", e);
            throw new IllegalArgumentException(e);
        }

        this.mapper = new ObjectMapper();
        this.triesCount = 5;
        this.listener = listener;
        this.reconnect = true;
    }

    public void init() {
        executorService = Executors.newFixedThreadPool(10);
        wsClient.connect();
    }

    public void destroy() {
        reconnect = false;
        wsClient.close();
        executorService.shutdown();
    }

    protected void onOpen(ServerHandshake handshakedata) {
        log.trace("Connection opened");
    }

    protected void onClose(int code, String reason, boolean remote) {
        triesCount--;
        log.warn("closed with exit code {}, additional info: {}", code, reason);

        if (reconnect && triesCount > 0) {
            log.info("Reconnecting in 5 seconds... Tries left = {}", triesCount);
            MiscUtils.sleep(5000);

            new Thread(this.wsClient::reconnect).start();
        } else {
            log.info("Terminating application");
            System.exit(1);
        }
    }

    protected void onMessage(String messageString) {
        log.trace("received message: {}", messageString);

        try {
            MattermostEvent message = mapper.readValue(messageString, MattermostEvent.class);
            executorService.submit(() -> listener.onEvent(message));
        } catch (IOException e) {
            log.error("Error while processing message", e);
        }
    }

    protected void onError(Exception ex) {
        log.warn("Error happened", ex);
    }

    private static class WebSocketClientImpl extends WebSocketClient {
        final MattermostWebsocketDispatcher callback;

        public WebSocketClientImpl(URI serverUri, Map<String, String> httpHeaders, MattermostWebsocketDispatcher callback) {
            super(serverUri, httpHeaders);
            this.callback = callback;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            callback.onOpen(handshakedata);
        }

        @Override
        public void onMessage(String message) {
            callback.onMessage(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            callback.onClose(code, reason, remote);
        }

        @Override
        public void onError(Exception ex) {
            callback.onError(ex);
        }
    }
}
