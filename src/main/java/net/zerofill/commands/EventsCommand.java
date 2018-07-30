package net.zerofill.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import net.zerofill.novaro.actions.NovaROEvents;
import net.zerofill.utils.BotUtils;

public class EventsCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "events"), new ArrayList<>(Arrays.asList("", "Lists event countdowns.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        List<EmbedObject> results = NovaROEvents.getEvents();

        for (EmbedObject result : results) {
            BotUtils.sendMessage(event.getChannel(), result);
        }
    }
}
