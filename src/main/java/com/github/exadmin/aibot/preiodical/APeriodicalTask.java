package com.github.exadmin.aibot.preiodical;

import com.github.exadmin.aibot.AppContext;
import com.github.exadmin.aibot.mattermost.MatterMostClientPomogator;
import com.github.exadmin.aibot.report.MMReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * An abstract class to represent task for periodical execution.
 * The period must be provided by getPeriodOfRepeatingInSeconds() method.
 */
public abstract class APeriodicalTask extends TimerTask {
    private final MatterMostClientPomogator mmClientPomogator;
    private final AppContext appContext;
    private final Logger log;
    private final MMReport report;

    /**
     * Returns name of the task - suitable for the logging purposes mostly
     * @return String
     */
    public abstract String getName();

    /**
     * Returns number of seconds between task executions
     * @return
     */
    protected abstract int getPeriodOfRepeatingInSeconds();

    /**
     * Main logic of the task must be implemented here.
     * Can throw exceptions.
     */
    protected abstract void runUnsafe() throws Exception;

    public APeriodicalTask(MatterMostClientPomogator mmClientPomogator, AppContext appContext) {
        this.mmClientPomogator = mmClientPomogator;
        this.appContext = appContext;
        this.log = LoggerFactory.getLogger(getClass());
        this.report = new MMReport(getName());
    }

    protected MatterMostClientPomogator getMmClientPomogator() {
        return mmClientPomogator;
    }

    protected long getDelayToStartTaskFromNowMillis() {
        return 0;
    }

    protected Logger getLog() {
        return log;
    }

    public MMReport getReport() {
        return report;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    @Override
    public final void run() {
        getLog().info("Start executing task '{}'", getName());
        try {
            runUnsafe();
        } catch (Exception ex) {
            getLog().error("Error while executing task '{}'", getName(), ex);
        } finally {
            getLog().info("Finish executing task '{}'", getName());
        }
    }
}
