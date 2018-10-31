package net.zerofill.novaro.actions.jobs;

import com.mchange.v2.cfg.PropertiesConfigSource;
import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;
import net.zerofill.utils.DataUtils;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class EventsJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(EventsJob.class);

    private static IChannel channel;
    private static String eventId = ConfigUtils.get("novaro.remind.eventId");
    private static Map<String, Object> event;

    public static void setChannel(IChannel channel) {
        EventsJob.channel = channel;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        IRole role = channel.getGuild().getRolesByName(ConfigUtils.get("novaro.remind.role")).get(0);
        BotUtils.sendMessage(
                EventsJob.channel,
                String.format("%s %s is happening now", role.mention(), event.get("name")),
                true);
    }

    public static JobDetail getEventJob() {
        return newJob(EventsJob.class)
                .withIdentity("event_job", "event_group")
                .build();
    }

    public static Trigger getEventTrigger() {
        event = DataUtils.getEvents(eventId).get(0);
        try {
            CronExpression ce = new CronExpression(event.get("schedule").toString());
            ScheduleBuilder schedule = cronSchedule(ce);
            return newTrigger()
                    .withIdentity("event_trigger", "event_group")
                    .withSchedule(schedule)
                    .build();
        } catch (ParseException pe) {

        }

        return null;
    }

}
