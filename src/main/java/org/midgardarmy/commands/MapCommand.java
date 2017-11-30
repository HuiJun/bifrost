package org.midgardarmy.commands;

import org.midgardarmy.divinepride.DivinePrideClient;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public class MapCommand implements Command {

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        List<EmbedObject> responses = DivinePrideClient.getById("ma", args);

        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
