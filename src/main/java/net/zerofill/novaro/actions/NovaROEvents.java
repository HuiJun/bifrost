package net.zerofill.novaro.actions;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.zerofill.utils.DataUtils;

import static com.cronutils.model.CronType.QUARTZ;

public class NovaROEvents {

    private static final Logger logger = LoggerFactory.getLogger(NovaROEvents.class);

    private static final int PADDING = 4;

    private static final String BEGIN_DELIMITER = "[";
    private static final String END_DELIMITER = "]";

    private static CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));

    public static List<String> getEvents() {
        List<String> eventResults = new ArrayList<>();
        List<Map<String, Object>> events = DataUtils.getEvents();
        List<Map<String, Object>> autoEvents = new ArrayList<>();
        List<Map<String, Object>> specialEvents = new ArrayList<>();
        int longest = 0;

        for (Map<String, Object> event : events) {

            if (event.get("end") != null) {
                specialEvents.add(event);
            } else {
                autoEvents.add(event);
            }

            if (event.get("name").toString().length() > longest) {
                longest = event.get("name").toString().length();
            }

        }

        StringBuilder builder = new StringBuilder();
        builder.append("Auto Events");
        builder.append(String.format("%n"));

        builder.append("```haskell");
        builder.append(String.format("%n"));
        List<String> formatted = buildEventTable(autoEvents, longest);
        for (String format : formatted) {
            builder.append(format);
        }

        List<String> specialFormatted = buildEventTable(specialEvents, longest);
        for (String format : specialFormatted) {
            builder.append(format);
        }

        builder.append("```");

        eventResults.add(builder.toString());
        return eventResults;
    }

    private static List<String> buildEventTable(List<Map<String, Object>> events, int longest) {
        List<String> eventTable = new ArrayList<>();

        for (Map<String, Object> event : events) {
            StringBuilder sb = new StringBuilder();
            sb.append(BEGIN_DELIMITER);
            sb.append(event.get("name").toString());
            sb.append(END_DELIMITER);
            sb.append(String.join("", Collections.nCopies(longest + PADDING - sb.length(), " ")));

            ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(event.get("schedule").toString()));
            ZonedDateTime now = ZonedDateTime.now();
            Optional<ZonedDateTime> nextTime = executionTime.nextExecution(now);
            Optional<Duration> sinceLast = executionTime.timeFromLastExecution(now);

            long timeDiff = 0;

            if (sinceLast.isPresent() && sinceLast.get().toMillis() < Long.parseLong(event.get("duration").toString())) {
                timeDiff = Long.parseLong(event.get("duration").toString()) - sinceLast.get().toMillis();
                sb.append("for ");
            } else if (nextTime.isPresent()) {
                timeDiff = now.until(nextTime.get(), ChronoUnit.MILLIS);
                sb.append("in ");
            }

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;
            long weeksInMilli = daysInMilli * 7;

            long weeks = timeDiff / weeksInMilli;
            timeDiff = timeDiff % weeksInMilli;

            long days = timeDiff / daysInMilli;
            timeDiff = timeDiff % daysInMilli;

            long hours = timeDiff / hoursInMilli;
            timeDiff = timeDiff % hoursInMilli;

            long minutes = timeDiff / minutesInMilli;
            timeDiff = timeDiff % minutesInMilli;

            long seconds = timeDiff / secondsInMilli;

            if (weeks > 0) {
                sb.append(Long.toString(weeks));
                sb.append(" weeks ");
            }

            if (days > 0) {
                sb.append(Long.toString(days));
                sb.append(" days ");
            }

            if (hours > 0 && weeks < 1) {
                sb.append(Long.toString(hours));
                sb.append(" hours ");
            }

            if (minutes > 0 && (days < 1 || hours < 1)) {
                sb.append(Long.toString(minutes));
                sb.append(" minutes ");
            }

            if (seconds >= 0 && (hours < 1 || minutes < 1)) {
                sb.append(Long.toString(seconds));
                sb.append(" seconds ");
            }

            sb.append(String.format("%n"));
            eventTable.add(sb.toString());
        }

        return eventTable;
    }

    private NovaROEvents() {
    }

}
