package net.zerofill.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.zerofill.divinepride.DivinePrideClient;
import net.zerofill.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class MiCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "mi"), new ArrayList<>(Arrays.asList("<name or id>", "Returns information about a mob. Passing a name will return all matching entries.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String monsterName = args.isEmpty() ? "poring" : String.join(" ", args);
        List<EmbedObject> responses;

        if (!Character.isDigit(monsterName.charAt(0))) {
            responses = DivinePrideClient.getByName("m", monsterName);
        } else {
            responses = DivinePrideClient.getById("m", args);
        }

        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
