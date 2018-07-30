package net.zerofill.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.zerofill.rochargen.ROChargenURLGen;
import net.zerofill.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class SigCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "sig"), new ArrayList<>(Arrays.asList("[name]", "If provided, returns a generated signature url for the name provided. Otherwise, uses the display name of the user.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
        EmbedObject response = ROChargenURLGen.generateSig(charName);
        BotUtils.sendMessage(event.getChannel(), response);
    }
}
