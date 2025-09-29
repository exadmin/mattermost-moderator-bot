package com.github.exadmin.aibot.tasks.impl;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.tasks.APeriodBasedRepeatingTask;

/**
 * Terminates application after 3 minutes are passed since program start
 */
public class LocalDevTerminator extends APeriodBasedRepeatingTask {
    private boolean isSecondCall;

    public LocalDevTerminator(MatterMostClientPomogator mmClientPomogator, AppContext appContext) {
        super(mmClientPomogator, appContext);
    }

    @Override
    public String getName() {
        return "Terminates application after all tasks are done";
    }

    @Override
    protected void runUnsafe() throws Exception {
        if (getAppContext().isLocalDevMode() && isSecondCall) {
            System.exit(0);
        }

        isSecondCall = true;
    }

    @Override
    protected int getRepeatingPeriodInSeconds() {
        return 3 * 60;
    }
}
