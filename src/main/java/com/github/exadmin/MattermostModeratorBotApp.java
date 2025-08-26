package com.github.exadmin;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.TheBot;
import com.github.exadmin.utils.PropsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MattermostModeratorBotApp  {
    private static final Logger log = LoggerFactory.getLogger(MattermostModeratorBotApp.class);


    public static void main(String[] args) {
        // check if path to the settings file is provided
        if (args.length == 0) {
            log.error(
                    """
                        Mandatory settings must be provided via properties file.
                        Application run command example: java -jar bot.jar ${PATH_TO_PROPERTIES_FILE}
                        See parameters description at https://github.com/exadmin/mattermost-moderator-bot
                        Current program arguments are """ + args
            );

            System.exit(1);
        }

        // try loading settings
        AppContext appContext = PropsUtils.loadAppContext(args[0]);
        if (!appContext.isSuccessfullyLoaded()) {
            log.error("Not all required parameters where specified in the properties file {}", args[0]);
            System.exit(2);
        }

        TheBot theBot = new TheBot(appContext);
        theBot.run();
    }


}