package net.zerofill.cronrunner;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import net.zerofill.cronrunner.jobs.Reminder;
import net.zerofill.utils.ConfigUtils;
import net.zerofill.utils.DataUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class CronRunner {

    private static final Logger logger = LoggerFactory.getLogger(CronRunner.class);

    private static List<Map<String, Object>> events = DataUtils.getEvents();

    private static List<String> remindList = ConfigUtils.getList("reminder.ids");
    private static String remindBefore = ConfigUtils.getString("reminder.before");

    public void run() {
        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler scheduler = sf.getScheduler();

            for (Map<String, Object> event : events) {
                if (remindList.contains(event.get("id").toString())) {

                    String[] expression = event.get("schedule").toString().split(" ");
                    expression[1] = getMinutesFrom(expression[1], remindBefore).toString();
                    JobDetail job = newJob(Reminder.class).withIdentity(event.get("name").toString(), String.format("event%s", event.get("id").toString())).build();
                    CronTrigger trigger = newTrigger().withIdentity("trigger", String.format("event%s", event.get("id").toString()))
                            .withSchedule(cronSchedule(String.join(" ", expression)))
                            .build();

                    Date ft = scheduler.scheduleJob(job, trigger);
                    logger.info(String.format("%s has been scheduled to run at: %s and repeat based on expression: %s", job.getKey(), ft, trigger.getCronExpression()));
                }
            }

            scheduler.start();
            addShutdownHook(scheduler);

        } catch (SchedulerException se) {
            logger.error(se.getLocalizedMessage());
        }
    }

    private void addShutdownHook(Scheduler scheduler) {
        Thread t = new Thread("Quartz Shutdown-Hook") {
            @Override public void run() {
               logger.info("Shutting down Quartz...");
                try {
                    scheduler.shutdown();
                } catch (SchedulerException e) {
                    logger.info(
                            "Error shutting down Quartz: " + e.getMessage(), e);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(t);
    }

    private Integer getMinutesFrom(String startString, String minusString) {
        int start = Integer.parseInt(startString);
        int minus = Integer.parseInt(minusString);
        return start - minus < 0 ? 60 - start - minus : start - minus;
    }

    public CronRunner() {}
}
