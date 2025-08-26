package com.github.exadmin.utils;

import com.github.exadmin.aibot.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

import static com.github.exadmin.utils.StrUtils.hasValue;

public class PropsUtils {
    private static final Logger log = LoggerFactory.getLogger(PropsUtils.class);

    private static final String MM_URL = "MATTERMOST_URL";
    private static final String MM_TOKEN = "MATTERMOST_TOKEN";
    private static final String PREFIX_ALLOWED_EMAIL = "ALLOWED_EMAIL[";
    private static final String PREFIX_MONITORED_CHANNEL = "MONITORED_CHANNEL[";

    public static AppContext loadAppContext(String filePath) {
        AppContext appContext = new AppContext();
        appContext.setSuccessfullyLoaded(false);

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(filePath));

            appContext.setMmDomain(getCleanValue(properties.getProperty(MM_URL)));
            appContext.setMmToken(getCleanValue(properties.getProperty(MM_TOKEN)));

            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                String keyName = key.toString();
                if (keyName.startsWith(PREFIX_ALLOWED_EMAIL) && keyName.endsWith("]")) {
                    appContext.addAllowedEmails(getCleanValue(properties.getProperty(keyName)));
                    continue;
                }

                if (keyName.startsWith(PREFIX_MONITORED_CHANNEL) && keyName.endsWith("]")) {
                    appContext.addMonitoredChannels(getCleanValue(properties.getProperty(keyName)));
                    continue;
                }
            }

            // check that all required properties are loaded
            if (hasValue(appContext.getMmDomain()) && hasValue(appContext.getMmToken())
                && hasValue(appContext.getAllowedEmails())) {

                appContext.setSuccessfullyLoaded(true);
            }
        } catch (Exception ex) {
            log.error("Error while loading properties from file {}", filePath, ex);
        }

        return appContext;
    }

    private static String getCleanValue(String strValue) {
        if (strValue == null) return null;
        return strValue.trim();
    }


}
