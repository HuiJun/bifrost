package org.midgardarmy.commands;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import org.midgardarmy.divinepride.DivinePrideClient;
import org.midgardarmy.utils.BotUtils;

public class MiCommand implements Command {

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String monsterName = args.isEmpty() ? "poring" : String.join(" ", args);
        List<EmbedObject> responses;

        if (!Character.isDigit(monsterName.charAt(0))) {
            responses = DivinePrideClient.getByName("m", monsterName);
        } else {
            responses = DivinePrideClient.getById("m", args);
        }

        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
