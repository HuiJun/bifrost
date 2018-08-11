package net.zerofill.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import net.zerofill.rochargen.ROChargenURLGen;
import net.zerofill.utils.BotUtils;

public class CharCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "char"), new ArrayList<>(Arrays.asList("[name] [background, position]", "If provided, returns a generated character url for the name provided. Otherwise, uses the display name of the user.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String[] ids = null;
        EmbedObject response;
        if (args.get(args.size() - 1).contains(",")) {
            ids = args.get(args.size() - 1).split(",");
            args.remove(args.size() - 1);
        }
        String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);

        if (ids != null && ids.length == 2) {
            response = ROChargenURLGen.generateChar(charName, Integer.parseInt(ids[0]), Integer.parseInt(ids[1]));
        } else {
            response = ROChargenURLGen.generateChar(charName);
        }
        BotUtils.sendMessage(event.getChannel(), response);
    }
}
