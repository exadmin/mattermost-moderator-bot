package com.github.exadmin.aibot.tasks;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;

import java.time.LocalTime;

/**
 * An abstract class to call regular daily task.
 */
public abstract class ASameTimeEachDayTask extends APeriodBasedRepeatingTask {
    public ASameTimeEachDayTask(MatterMostClientPomogator matterMostClient, AppContext appContext) {
        super(matterMostClient, appContext);
    }

    /**
     * Return instance of LocalTime with Hour, Minute and Second set.
     * This time will be used each day to call run() method of this task.
     * @return
     */
    protected abstract LocalTime getTimeOfTheDayToRun();

    @Override
    protected final int getRepeatingPeriodInSeconds() {
        return 86400;
    }

    @Override
    protected long getDelayToStartTaskFromNowMillis() {
        LocalTime timeToStart = getTimeOfTheDayToRun();
        int startH = timeToStart.getHour();
        int startM = timeToStart.getMinute();
        int startS = timeToStart.getSecond();

        LocalTime now = LocalTime.now();
        int nowH = now.getHour();
        int nowM = now.getMinute();
        int nowS = now.getSecond();

        // calculate number of seconds from the start of the day (midnight)
        int startPosInSeconds = startS + startM * 60 + startH * 60 * 60;
        int nowPosInSeconds   = nowS + nowM * 60 + nowH * 60 * 60;

        // calculate delay depending on the current time
        // if start-time is passed for today - then start ASAP, else calc difference to wait
        int deltaSecs = (startPosInSeconds - nowPosInSeconds);
        if (deltaSecs < 0) deltaSecs = deltaSecs + 24 * 60 * 60;
        return (deltaSecs) * 1000;
    }
}
