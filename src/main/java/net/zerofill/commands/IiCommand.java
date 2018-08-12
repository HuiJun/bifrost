package net.zerofill.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.zerofill.divinepride.DivinePrideClient;
import net.zerofill.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class IiCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "ii"), new ArrayList<>(Arrays.asList("<name or id>", "Returns information about an item. Passing a name will return all matching entries.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String itemName = args.isEmpty() ? "jellopy" : String.join(" ", args);
        List<EmbedObject> responses;

        if (!Character.isDigit(itemName.charAt(0))) {
            responses = DivinePrideClient.getByName("i", itemName);
        } else {
            responses = DivinePrideClient.getById("i", args);
        }

        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
