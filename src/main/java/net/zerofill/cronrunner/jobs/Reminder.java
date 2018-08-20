package net.zerofill.cronrunner.jobs;

import net.zerofill.utils.BotUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Reminder implements Job {

    private static final Logger logger = LoggerFactory.getLogger(Reminder.class);

    public void execute(JobExecutionContext context) {
    }

    private Reminder() {}
}
