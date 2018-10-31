package net.zerofill.commands;

import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PvmCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "pvm"), new ArrayList<>(Arrays.asList("", "Add or removes the defined pvm role")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        IRole role = event.getGuild().getRolesByName(ConfigUtils.get("discord.bot.assign.role")).get(0);
        if (event.getAuthor().hasRole(role)) {
            BotUtils.removeRole(event, role);
        } else {
            BotUtils.assignRole(event, role);
        }
    }
}
