package org.midgardarmy.commands;

import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.List;
import java.util.Map;

public class HelpPrettyCommand implements Command {

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        EmbedBuilder helpMessage = new EmbedBuilder();
        helpMessage.withTitle("Help Menu");
        helpMessage.withDescription("<required> or [optional] arguments");

        for (Map.Entry<String, List<String>> entry : HelpCommand.helpMap.entrySet()) {
            String commandId = entry.getKey();
            List<String> helpMessages = entry.getValue();
            if (helpMessages.size() > 1) {
                helpMessage.appendField(String.format("%s %s", commandId, helpMessages.get(0)), helpMessages.get(1), false);
            } else {
                helpMessage.appendField(commandId, helpMessages.get(0), false);
            }
        }
        BotUtils.sendMessage(event.getChannel(), helpMessage.build());
    }
}
