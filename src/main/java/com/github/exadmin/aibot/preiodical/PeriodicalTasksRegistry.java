package com.github.exadmin.aibot.preiodical;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.preiodical.impl.BotIsActive;
import com.github.exadmin.aibot.preiodical.impl.UserProfileIsSecured;
import com.github.exadmin.utils.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class PeriodicalTasksRegistry {
    private static final Logger log = LoggerFactory.getLogger(PeriodicalTasksRegistry.class);

    private final List<APeriodicalTask> tasks;
    private final Timer timer;

    public PeriodicalTasksRegistry(MatterMostClientPomogator matterMostClient, AppContext appContext) {
        this.tasks = new ArrayList<>();
        this.timer = new Timer(true);

        // register here all necessary tasks
        tasks.add(new UserProfileIsSecured(matterMostClient, appContext));
        tasks.add(new BotIsActive(matterMostClient, appContext));
    }

    /**
     * Schedules registered tasks for regular execution.
     * @param runTasksASAP in case is true - then all tasks will be executed withing next 15 seconds in random order
     *                     this mode is used for local-development only - allowing not to wait real execution time
     */
    public void scheduleAndRunAsync(boolean runTasksASAP) {
        for (APeriodicalTask task : tasks) {
            long delay = task.getDelayToStartTaskFromNowMillis();
            if (runTasksASAP) {
                delay = MiscUtils.getRandomLong(5 * 1000L);
            }
            timer.scheduleAtFixedRate(task, delay, task.getRepeatingPeriodInSeconds() * 1000L);
            log.info("Task '{}' is scheduled with delay from now = {} sec.", task.getName(), delay / 1000L);
        }
    }
}
