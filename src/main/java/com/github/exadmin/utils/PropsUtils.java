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
    private static final String PREFIX_EMAILS_TO_REPORT_TO_USERS_WITH = "USER_WITH_EMAIL_TO_REPORT_TO[";
    private static final String IS_LOCAL_DEV_MODE = "LOCAL_DEV_MODE";

    public static AppContext loadAppContext(String filePath) {
        AppContext appContext = new AppContext();
        appContext.setSuccessfullyLoaded(false);

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(filePath));

            appContext.setMmDomain(getCleanStrValue(properties.getProperty(MM_URL)));
            appContext.setMmToken(getCleanStrValue(properties.getProperty(MM_TOKEN)));
            appContext.setLocalDevMode(getCleanBoolValue(properties.getProperty(IS_LOCAL_DEV_MODE), false));

            Set<Object> keys = properties.keySet();
            for (Object key : keys) {
                String keyName = key.toString();
                if (keyName.startsWith(PREFIX_ALLOWED_EMAIL) && keyName.endsWith("]")) {
                    appContext.addAllowedEmails(getCleanStrValue(properties.getProperty(keyName)));
                    continue;
                }

                if (keyName.startsWith(PREFIX_MONITORED_CHANNEL) && keyName.endsWith("]")) {
                    appContext.addMonitoredChannels(getCleanStrValue(properties.getProperty(keyName)));
                    continue;
                }

                if (keyName.startsWith(PREFIX_EMAILS_TO_REPORT_TO_USERS_WITH) && keyName.endsWith("]")) {
                    appContext.addEmailToReportToUserWith(getCleanStrValue(properties.getProperty(keyName)));
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

    private static String getCleanStrValue(String strValue) {
        if (strValue == null) return null;
        return strValue.trim();
    }

    private static Boolean getCleanBoolValue(String strValue, boolean defaultValue) {
        String str = getCleanStrValue(strValue);
        if (str == null || str.isBlank()) return defaultValue;

        return Boolean.parseBoolean(str);
    }


}
