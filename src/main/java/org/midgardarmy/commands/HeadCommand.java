package org.midgardarmy.commands;

import org.midgardarmy.rochargen.ROChargenURLGen;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeadCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "char"), new ArrayList<>(Arrays.asList("[name]", "If provided, returns a generated character head url for the name provided. Otherwise, uses the display name of the user.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
        EmbedObject response = ROChargenURLGen.generateCharhead(charName);
        BotUtils.sendMessage(event.getChannel(), response);
    }
}
