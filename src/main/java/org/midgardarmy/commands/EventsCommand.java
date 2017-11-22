package org.midgardarmy.commands;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import org.midgardarmy.novaro.NovaROEvents;
import org.midgardarmy.utils.BotUtils;

public class EventsCommand implements Command {

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        List<EmbedObject> results = NovaROEvents.getEvents();

        for (EmbedObject result : results) {
            BotUtils.sendMessage(event.getChannel(), result);
        }
    }
}
