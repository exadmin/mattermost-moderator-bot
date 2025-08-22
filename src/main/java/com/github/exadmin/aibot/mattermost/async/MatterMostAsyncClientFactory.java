package com.github.exadmin.aibot.mattermost.async;

import net.bis5.mattermost.client4.MattermostClient;

import java.util.concurrent.ExecutorService;

public class MatterMostAsyncClientFactory {
    ExecutorService executor;
    MattermostClient client;
    boolean run;

    public void startMattermostClient(String mmURL, String mmToken) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                client = MattermostClient.builder()
                        .url(mmURL).ignoreUnknownProperties()
                        .build();

                client.setAccessToken(mmToken);

                run = true;

                while (run) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception ex) {
                        run = false;
                    }
                }
            }
        });
        thread.start();
    }

    public void shutdown() {
        run = false;

        if (client != null) {
            client.close();
            client = null;
        }

        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public MattermostClient getClient() {
        return client;
    }
}
