package org.midgardarmy.commands;

import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import org.midgardarmy.rochargen.ROChargenURLGen;
import org.midgardarmy.utils.BotUtils;

public class SigCommand implements Command {

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
        EmbedObject response = ROChargenURLGen.generateSig(charName);
        BotUtils.sendMessage(event.getChannel(), response);
    }
}
