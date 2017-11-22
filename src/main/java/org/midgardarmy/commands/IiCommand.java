package org.midgardarmy.commands;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import org.midgardarmy.divinepride.DivinePrideClient;
import org.midgardarmy.utils.BotUtils;

public class IiCommand implements Command {

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String itemName = args.isEmpty() ? "jellopy" : String.join(" ", args);
        List<EmbedObject> responses;

        if (!Character.isDigit(itemName.charAt(0))) {
            responses = DivinePrideClient.getByName("i", itemName);
        } else {
            responses = DivinePrideClient.getById("i", args);
        }

        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
