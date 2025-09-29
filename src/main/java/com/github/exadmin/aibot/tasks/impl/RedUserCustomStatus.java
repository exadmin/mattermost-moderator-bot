package com.github.exadmin.aibot.tasks.impl;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.config.RedTeamStorage;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.report.MMReportStatus;
import com.github.exadmin.aibot.tasks.ASameTimeEachDayTask;
import net.bis5.mattermost.model.User;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class RedUserCustomStatus extends ASameTimeEachDayTask {
    public RedUserCustomStatus(MatterMostClientPomogator mmClientPomogator, AppContext appContext) {
        super(mmClientPomogator, appContext);
    }

    @Override
    public String getName() {
        return "Red member has custom status set";
    }

    @Override
    protected LocalTime getTimeOfTheDayToRun() {
        return LocalTime.of(12, 5, 0);
    }

    @Override
    protected void runUnsafe() throws Exception {
        getReport().setStatus(MMReportStatus.UNDEFINED);

        List<User> allUsers = getMmClientPomogator().getAllUsers();
        for (User user : allUsers) {
            // check correct status for the red-member
            String email = user.getEmail();
            if (RedTeamStorage.ALIAS_TO_EMAIL.containsValue(email)) {
                boolean statusCheckPassed = false;

                Map<String, String> properties = user.getProps();
                if (properties != null) {
                    String customStatus = properties.get("customStatus");
                    if (customStatus != null) {
                        statusCheckPassed = customStatus.contains("\"emoji\":\"red_circle\"");
                    }
                }

                if (!statusCheckPassed) {
                    getReport().addMsgToReport("Red-user '" + user.getEmail()  + "' has not set correct status (red circle)");
                    getReport().setStatus(MMReportStatus.YELLOW);
                }
            }
        }

        getReport().sendReport(getMmClientPomogator(), getAppContext());
    }
}
