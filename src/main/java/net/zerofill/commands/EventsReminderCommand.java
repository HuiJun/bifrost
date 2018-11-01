package net.zerofill.commands;

import net.zerofill.novaro.actions.NovaROEventsReminder;
import net.zerofill.novaro.actions.jobs.EventsJob;
import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventsReminderCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "remind"), new ArrayList<>(Arrays.asList("<none or remove>", "Add or removes the defined remind role")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String action = args.isEmpty() ? "" : args.get(0);
        List<String> results = new ArrayList<>();
        IRole role = event.getGuild().getRolesByName(ConfigUtils.get("novaro.remind.role")).get(0);

        switch (action) {
            case "start":
                EventsJob.setChannel(event.getChannel());
                results.addAll(NovaROEventsReminder.initializeScheduler());
                break;

            case "stop":
                results.addAll(NovaROEventsReminder.shutdownScheduler());
                break;

            case "restart":
                results.addAll(NovaROEventsReminder.shutdownScheduler());
                EventsJob.setChannel(event.getChannel());
                results.addAll(NovaROEventsReminder.initializeScheduler());
                break;

            case "remove":
                if (event.getAuthor().hasRole(role)) {
                    BotUtils.removeRole(event, role);
                } else {
                    results.add("What is it that you want me to do?");
                }
                break;

            default:
                if (!event.getAuthor().hasRole(role)) {
                    BotUtils.assignRole(event, role);
                    results.add(String.format("%s assigned role %s for reminders", event.getAuthor().mention(true), role.getName()));
                } else {
                    BotUtils.removeRole(event, role);
                }
                break;
        }

        for (String result : results) {
            BotUtils.sendMessage(event.getChannel(), result);
        }
    }
}
