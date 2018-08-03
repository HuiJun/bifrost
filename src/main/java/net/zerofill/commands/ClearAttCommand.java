package net.zerofill.commands;

import net.zerofill.roster.actions.ClearAtt;
import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClearAttCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "clearatt"), new ArrayList<>(Arrays.asList("[name]", "Clears the attendance roster. Admins Only")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String woe = null;
        if (event.getChannel().getName().equalsIgnoreCase(ConfigUtils.getString("roster.woe1.channel"))) {
            woe = "woe1";
        } else if (event.getChannel().getName().equalsIgnoreCase(ConfigUtils.getString("roster.woe2.channel"))) {
            woe = "woe2";
        }
        if (woe != null) {
            for (IRole role : event.getAuthor().getRolesForGuild(event.getGuild())) {
                if (role.getName().equalsIgnoreCase(ConfigUtils.getString(String.format("roster.%s.admin", woe)))) {
                    try {
                        BotUtils.sendMessage(event.getChannel(), ClearAtt.clear(woe));
                    } catch (IOException | GeneralSecurityException e) {
                        e.getStackTrace();
                    }
                    return;
                }
            }
            BotUtils.sendMessage(event.getChannel(), "You're not an admin.");
        }
    }
}
