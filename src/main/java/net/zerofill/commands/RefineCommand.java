package net.zerofill.commands;

import net.zerofill.domains.UserMetadata;
import net.zerofill.game.RefineGame;
import net.zerofill.utils.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RefineCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "refine"), new ArrayList<>(Arrays.asList("", "Refines your weapon.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        UserMetadata userMetadata = new UserMetadata(event.getAuthor().getName(), event.getAuthor().getDisplayName(event.getGuild()));
        boolean safe = !args.isEmpty() && String.join("", args).equalsIgnoreCase("safe");

        List<String> results = RefineGame.refineWeapon(userMetadata, safe);

        for (String result : results) {
            BotUtils.sendMessage(event.getChannel(), result);
        }
    }
}
