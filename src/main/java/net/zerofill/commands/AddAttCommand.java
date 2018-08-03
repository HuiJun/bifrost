package net.zerofill.commands;

import net.zerofill.roster.actions.AddAtt;
import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IRole;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddAttCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "addatt"), new ArrayList<>(Arrays.asList("[name]", "Adds user to the roster.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String woe = null;
        if (event.getChannel().getName().equalsIgnoreCase(ConfigUtils.getString("roster.woe1.channel"))) {
            woe = "woe1";
        } else if (event.getChannel().getName().equalsIgnoreCase(ConfigUtils.getString("roster.woe2.channel"))) {
            woe = "woe2";
        }
        boolean permission = false;
        if (woe != null) {
            for (IRole role : event.getAuthor().getRolesForGuild(event.getGuild())) {
                if (role.getName().equalsIgnoreCase(ConfigUtils.getString(String.format("roster.%s.signup", woe)))) {
                    String roleToAdd = args.remove(args.size() - 1);
                    String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
                    try {
                        BotUtils.sendMessage(event.getChannel(), AddAtt.add(woe, charName.replaceAll("^\"|\"$", ""), roleToAdd.replaceAll("^\"|\"$", "")));
                        permission = true;
                    } catch (IOException | GeneralSecurityException e) {
                        e.getStackTrace();
                    }
                }
            }
            if (!permission) {
                BotUtils.sendMessage(event.getChannel(), String.format("You're not allowed to signup for %s", woe));
            }
        }
    }
}
