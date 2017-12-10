package org.midgardarmy.commands;

import org.midgardarmy.shivtr.actions.Forums;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForumsCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "forum"), new ArrayList<>(Arrays.asList("", "Returns last 4 active threads")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        for (EmbedObject response : Forums.getForumsList()) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
