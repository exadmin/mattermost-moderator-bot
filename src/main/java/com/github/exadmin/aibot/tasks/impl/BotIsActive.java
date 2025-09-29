package com.github.exadmin.aibot.tasks.impl;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.tasks.ASameTimeEachDayTask;
import com.github.exadmin.aibot.report.MMReportStatus;
import com.github.exadmin.utils.MiscUtils;

import java.time.LocalTime;

public class BotIsActive extends ASameTimeEachDayTask {

    public BotIsActive(MatterMostClientPomogator mmClientPomogator, AppContext appContext) {
        super(mmClientPomogator, appContext);
    }

    @Override
    public String getName() {
        return "Bot is running";
    }

    @Override
    protected LocalTime getTimeOfTheDayToRun() {
        return LocalTime.of(8, 0, 0);
    }

    @Override
    protected void runUnsafe() throws Exception {
        long curTime = System.currentTimeMillis();

        getReport().setStatus(MMReportStatus.GREEN);
        getReport().addMsgToReport("I am up and running already for " + MiscUtils.secondsToHMS((curTime - getAppContext().getStartTime()) / 1000));

        getReport().sendReport(getMmClientPomogator(), getAppContext());
    }
}
