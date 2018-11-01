package net.zerofill.commands;

import net.zerofill.novaro.actions.NovaRORanking;
import net.zerofill.utils.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuildRankingCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "guildranking"), new ArrayList<>(Arrays.asList("", "Returns guild ranking on NovaRO.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        for (String response : NovaRORanking.getGuildRanking()) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
