package com.github.exadmin.aibot.preiodical.impl;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.preiodical.APeriodicalTask;
import com.github.exadmin.aibot.report.MMReportStatus;
import com.github.exadmin.utils.MiscUtils;

public class BotIsActive extends APeriodicalTask {

    public BotIsActive(MatterMostClientPomogator mmClientPomogator, AppContext appContext) {
        super(mmClientPomogator, appContext);
    }

    @Override
    public String getName() {
        return "Bot is running";
    }

    @Override
    protected int getRepeatingPeriodInSeconds() {
        return 4 * 60 * 60; // 4 hours
    }

    @Override
    protected void runUnsafe() throws Exception {
        long curTime = System.currentTimeMillis();

        getReport().setStatus(MMReportStatus.GREEN);
        getReport().addMsgToReport("I am up and running already for " + MiscUtils.secondsToHMS((curTime - getAppContext().getStartTime()) / 1000));

        getReport().sendReport(getMmClientPomogator(), getAppContext());
    }
}
