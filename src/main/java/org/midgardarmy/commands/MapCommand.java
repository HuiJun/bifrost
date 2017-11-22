package org.midgardarmy.commands;

import org.midgardarmy.divinepride.DivinePrideClient;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

public class MapCommand implements Command {

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String mapName = args.isEmpty() ? "prontera" : String.join(" ", args);
        List<EmbedObject> responses;

        if (!Character.isDigit(mapName.charAt(0))) {
            responses = DivinePrideClient.getByName("m", mapName);
        } else {
            responses = DivinePrideClient.getById("m", args);
        }

        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
