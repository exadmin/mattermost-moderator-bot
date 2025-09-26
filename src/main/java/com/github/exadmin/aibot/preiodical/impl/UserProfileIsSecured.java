package com.github.exadmin.aibot.preiodical.impl;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.preiodical.APeriodicalDailyTask;
import net.bis5.mattermost.model.User;
import net.bis5.mattermost.model.UserList;

import java.time.LocalTime;
import java.util.List;

/**
 * Checks that MM user profile has correct email assigned to the profile (from allowed domains only)
 * For the red-members a github nickname is provided.
 */
public class UserProfileIsSecured extends APeriodicalDailyTask {
    private List<String> KNOWN_EMAILS_TO_IGNORE = List.of(
            "adr@localhost",         // ADR bot
                "tlt.ermakov@gmail.com", // deactivated account
                "boards@localhost",      // bot
                "calendar@localhost",    // bot
                "calls@localhost",       // bot
                "cncf_bot@localhost"     // bot
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
        return "User Profiles Secure settings";
    }

    @Override
    public void runUnsafe() {
        UserList userList = getMmClientPomogator().getAllUsers();

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

        getReport().addMsgToReport("All users are analyzed");
        getReport().addMsgToReport("Total count of users = " + count);
        getReport().addMsgToReport("Secured user profiles= " + good);
        getReport().addMsgToReport("Insecured profiles   = " + bad);
        getReport().sendReport(getMmClientPomogator(), getAppContext());
    }

    private boolean checkIfEmailInAllowedDomain(User user) {
        String email = user.getEmail();
        if (email == null) return false;
        if (email.isBlank()) return false;

        email = email.toLowerCase();
        if (email.endsWith("@netcracker.com")) return true;
        if (email.endsWith(".qubership@gmail.com")) return true;

        if (KNOWN_EMAILS_TO_IGNORE.contains(email)) return true;

        return false;
    }
}
