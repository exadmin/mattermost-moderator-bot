package com.github.exadmin.aibot.report;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.utils.MiscUtils;
import net.bis5.mattermost.model.User;

import java.util.ArrayList;
import java.util.List;

public class MMReport {
    private final String taskName;
    private final List<String> logLines = new ArrayList<>();
    private MMReportStatus status = MMReportStatus.UNDEFINED;

    public MMReport(String taskName) {
        this.taskName = taskName;
    }

    public void addMsgToReport(String text) {
        logLines.add(text);
    }

    public MMReportStatus getStatus() {
        return status;
    }

    public void setStatus(MMReportStatus status) {
        this.status = status;
    }

    public void sendReport(MatterMostClientPomogator mmClientPomogator, AppContext appContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("### Report '**").append(taskName).append("**' ").append(getStatus().getMmIcon()).append("\n");
        sb.append("Date  : ").append(MiscUtils.getCurrentDateTimeStr()).append("\n");
        sb.append("Status: ").append(getStatus().getText()).append("\n");
        sb.append("```\n");
        for (String line : logLines) {
            sb.append(line).append("\n");
        }
        sb.append("```");

        User fromUser = mmClientPomogator.getBotProfile();
        for (String email : appContext.getEmailsToReportToUsersWith()) {
            User toUser = mmClientPomogator.getUserByEmail(email);
            String destChannelId = mmClientPomogator.defineChannelId(fromUser, toUser);
            mmClientPomogator.sendMessage(sb.toString(), destChannelId);
        }

        logLines.clear();
    }
}
