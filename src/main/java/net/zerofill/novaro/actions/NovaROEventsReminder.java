package net.zerofill.novaro.actions;

import net.zerofill.novaro.actions.jobs.EventsJob;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NovaROEventsReminder {

    private static final Logger logger = LoggerFactory.getLogger(NovaROEventsReminder.class);

    private static final SchedulerFactory sf = new StdSchedulerFactory();

    public static List<String> initializeScheduler() {
        List<String> eventResults = new ArrayList<>();

        try {
            Scheduler scheduler = sf.getScheduler();
            scheduler.scheduleJob(EventsJob.getEventJob(), EventsJob.getEventTrigger());
            scheduler.start();

            eventResults.add(
                    String.format("Successfully started scheduler %s (%s).",
                            scheduler.getSchedulerName(),
                            scheduler.getSchedulerInstanceId()
                    )
            );
        } catch (SchedulerException se) {
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduler error: ", se);
            }
            eventResults.add(String.format("%s", se.getMessage()));
        }

        return eventResults;
    }

    public static List<String> shutdownScheduler() {
        List<String> results = new ArrayList<>();
        try {
            Collection<Scheduler> schedulers = sf.getAllSchedulers();
            if (!schedulers.isEmpty()) {
                for (Scheduler scheduler : schedulers) {
                    scheduler.shutdown(true);
                    results.add(
                            String.format("Successfully shutdown scheduler %s (%s).",
                                    scheduler.getSchedulerName(),
                                    scheduler.getSchedulerInstanceId()
                            )
                    );
                }
            }
        } catch (SchedulerException se) {
            if (logger.isDebugEnabled()) {
                logger.debug("Scheduler shutdown error: ", se);
            }
        }

        return results;
    }

    private NovaROEventsReminder() {
    }

}
