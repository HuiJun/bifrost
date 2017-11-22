package org.midgardarmy.commands;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.midgardarmy.utils.BotUtils;

public class HelpCommand implements Command {

    static Map<String, List<String>> helpMap = new HashMap<>();
    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "sig"), new ArrayList<>(Arrays.asList("[name]", "If provided, returns a generated signature url for the name provided. Otherwise, uses the display name of the user.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "char"), new ArrayList<>(Arrays.asList("[name]", "If provided, returns a generated character url for the name provided. Otherwise, uses the display name of the user.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "events"), new ArrayList<>(Arrays.asList("Lists event countdowns.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "mi"), new ArrayList<>(Arrays.asList("<name or id>", "Returns information about a mob. Passing a name will return all matching entries.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "ii"), new ArrayList<>(Arrays.asList("<name or id>", "Returns information about an item. Passing a name will return all matching entries.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "ws"), new ArrayList<>(Arrays.asList("<name or id>", "Searches NovaRO Market and returns either search results or vendors selling the item.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        EmbedBuilder helpMessage = new EmbedBuilder();
        helpMessage.withTitle("Help Menu");
        helpMessage.withDescription("```md");

        for (Map.Entry<String, List<String>> entry : helpMap.entrySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%n"));
            String commandId = entry.getKey();
            List<String> helpMessages = entry.getValue();
            sb.append(commandId);
            sb.append(" ");

            if (helpMessages.size() > 1) {
                sb.append(helpMessages.get(0));
                sb.append(String.format("%n"));
                sb.append(String.join("", Collections.nCopies(sb.length(), "-")));
                sb.append(String.format("%n"));
                sb.append(helpMessages.get(1));
            } else {
                sb.append(String.format("%n"));
                sb.append(String.join("", Collections.nCopies(sb.length(), "-")));
                sb.append(String.format("%n"));
                sb.append(helpMessages.get(0));
            }

            sb.append(String.format("%n"));
            helpMessage.appendDescription(sb.toString());
        }

        helpMessage.appendDescription("```");
        BotUtils.sendMessage(event.getChannel(), helpMessage.build());
    }
}
