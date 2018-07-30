package net.zerofill.commands;

import net.zerofill.utils.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HelpCommand implements Command {

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "help"), new ArrayList<>(Arrays.asList("[command]", "Lists all help messages or specific help message.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        EmbedBuilder helpMessage = new EmbedBuilder();
        helpMessage.withTitle("Help Menu");

        if (!args.isEmpty() && args.get(0).equalsIgnoreCase("pretty")) {
            helpMessage.withDescription("<required> or [optional] arguments");

            for (Map.Entry<String, List<String>> entry : helpMap.entrySet()) {
                String commandId = entry.getKey();
                List<String> helpMessages = entry.getValue();
                if (helpMessages.size() > 1) {
                    helpMessage.appendField(String.format("%s %s", commandId, helpMessages.get(0)), helpMessages.get(1), false);
                }
            }
        } else if (!args.isEmpty()) {
            helpMessage.withDescription("```md");
            helpMessage.appendDescription(buildHelpMessage(args.get(0)));
            helpMessage.appendDescription("```");
        } else {
            helpMessage.withDescription("```md");

            for (Map.Entry<String, List<String>> entry : helpMap.entrySet()) {
                helpMessage.appendDescription(buildHelpMessage(entry.getKey(), entry.getValue()));
            }

            helpMessage.appendDescription("```");
        }
        BotUtils.sendMessage(event.getChannel(), helpMessage.build());
    }

    private String buildHelpMessage(String command) {
        return buildHelpMessage(command, new ArrayList<>());
    }

    private String buildHelpMessage(String command, List<String> helpMessages) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n"));
        String commandId = command.startsWith(BotUtils.BOT_PREFIX) ? command.toLowerCase() : String.format("%s%s", BotUtils.BOT_PREFIX, command.toLowerCase());
        if (helpMessages.isEmpty()) {
            helpMessages = helpMap.getOrDefault(commandId, new ArrayList<>());
        }
        sb.append(commandId);
        sb.append(" ");

        if (helpMessages.size() == 2) {
            sb.append(helpMessages.get(0));
            sb.append(String.format("%n"));
            sb.append(String.join("", Collections.nCopies(sb.length(), "-")));
            sb.append(String.format("%n"));
            sb.append(helpMessages.get(1));
        }

        sb.append(String.format("%n"));
        return sb.toString();
    }
}
