package org.midgardarmy.commands;

import org.midgardarmy.novaro.NovaRORanking;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZenyCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "zeny"), new ArrayList<>(Arrays.asList("", "Returns richest players on NovaRO.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        for (EmbedObject response : NovaRORanking.getZenyRanking()) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
