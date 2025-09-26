package com.github.exadmin.aibot.preiodical.impl;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.preiodical.APeriodicalDailyTask;
import com.github.exadmin.aibot.report.MMReportStatus;
import net.bis5.mattermost.model.User;

import java.time.LocalTime;
import java.util.List;

/**
 * Checks that MM user profile has correct email assigned to the profile (from allowed domains only)
 * For the red-members a github nickname is provided.
 */
public class UserProfileIsSecured extends APeriodicalDailyTask {
    private List<String> DEACTIVATED_ACCOUNTS = List.of(
                "tlt.ermakov@gmail.com",
                "divy.tripathy@gmail.com",
                "evg690@gmail.com",
                "n.kannan200@gmail.com",
                "pankratovsa@gmail.com",
                "jackson.raj.anthony.raj@gmail.com"
            );

    public UserProfileIsSecured(MatterMostClientPomogator matterMostClient, AppContext appContext) {
        super(matterMostClient, appContext);
    }

    @Override
    protected LocalTime getTimeOfTheDayToRun() {
        return LocalTime.of(17, 16, 15);
    }

    @Override
    public String getName() {
        return "Mattermost Profiles Email";
    }

    @Override
    public void runUnsafe() {
        getReport().setStatus(MMReportStatus.RED);
        List<User> userList = getMmClientPomogator().getAllUsers();

        int count = 0;
        int good  = 0;
        int bad   = 0;

        for (User user : userList) {
            count++;
            if (!checkIfEmailInAllowedDomain(user)) {
                getReport().addMsgToReport("User with incorrect email is detected: " + user.getEmail());
                bad++;
            } else
                good++;
        }

        getReport().setStatus(MMReportStatus.GREEN);
        if (bad > 0) getReport().setStatus(MMReportStatus.YELLOW);

        getReport().addMsgToReport("All users are analyzed");
        getReport().addMsgToReport("Total count of users = " + count);
        getReport().addMsgToReport("Secured user profiles= " + good);
        getReport().addMsgToReport("Unsecured profiles   = " + bad);
        getReport().sendReport(getMmClientPomogator(), getAppContext());
    }

    private boolean checkIfEmailInAllowedDomain(User user) {
        String email = user.getEmail();
        if (email == null) return false;
        if (email.isBlank()) return false;

        email = email.toLowerCase();
        if (email.endsWith("@netcracker.com")) return true;
        if (email.endsWith(".qubership@gmail.com")) return true;
        if (email.endsWith("@localhost")) return true;

        if (DEACTIVATED_ACCOUNTS.contains(email)) return true;

        return false;
    }
}
