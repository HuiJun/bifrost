package org.midgardarmy.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import org.midgardarmy.utils.BotUtils;
import org.midgardarmy.utils.ConfigUtils;

public class CommandHandler implements IListener<MessageReceivedEvent> {

    private static Map<String, Command> commandMap = new HashMap<>();
    private static Map<Long, IMessage> processingMap = new HashMap<>();
    private static List<String> internalProcesses = new ArrayList<>(Arrays.asList("sig", "char", "help", "events"));

    private static EmbedBuilder processingMessage;

    static {
        processingMessage = new EmbedBuilder();
        String processingUrl = ConfigUtils.get("discord.bot.processing");
        if (!processingUrl.isEmpty()) {
            processingMessage.withImage(ConfigUtils.get("discord.bot.processing"));
        } else {
            processingMessage.withDescription("Processing...");
        }
    }

    static {
        commandMap.put("char", new CharCommand());
        commandMap.put("events", new EventsCommand());
        commandMap.put("help", new HelpCommand());
        commandMap.put("ii", new IiCommand());
        commandMap.put("map", new MapCommand());
        commandMap.put("mi", new MiCommand());
        commandMap.put("mvp", new PythonCommand());
        commandMap.put("mvphelp", new PythonCommand());
        commandMap.put("mvplist", new PythonCommand());
        commandMap.put("sig", new SigCommand());
        commandMap.put("ws", new WsCommand());
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] argArray = event.getMessage().getContent().split(" ");

        if (argArray.length == 0 || !argArray[0].startsWith(BotUtils.BOT_PREFIX)) {
            return;
        }

        String commandStr = argArray[0].substring(BotUtils.BOT_PREFIX.length());

        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0);

        if (commandMap.containsKey(commandStr)) {
            if (!internalProcesses.contains(commandStr)) {
                IMessage initial = event.getChannel().sendMessage(processingMessage.build());
                processingMap.put(event.getMessageID(), initial);
            }

            commandMap.get(commandStr).runCommand(event, argsList);

            if (processingMap.get(event.getMessageID()) != null) {
                processingMap.get(event.getMessageID()).delete();
                processingMap.remove(event.getMessageID());
            }
        }
    }

}
