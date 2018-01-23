package org.midgardarmy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.midgardarmy.commands.AvatarCommand;
import org.midgardarmy.commands.CharCommand;
import org.midgardarmy.commands.HeadCommand;
import org.midgardarmy.commands.Command;
import org.midgardarmy.commands.EventsCommand;
import org.midgardarmy.commands.ForumsCommand;
import org.midgardarmy.commands.HelpCommand;
import org.midgardarmy.commands.IiCommand;
import org.midgardarmy.commands.MapCommand;
import org.midgardarmy.commands.MiCommand;
import org.midgardarmy.commands.PcCommand;
import org.midgardarmy.commands.PythonCommand;
import org.midgardarmy.commands.SigCommand;
import org.midgardarmy.commands.WsCommand;
import org.midgardarmy.commands.ZenyCommand;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import org.midgardarmy.utils.BotUtils;
import org.midgardarmy.utils.ConfigUtils;

public class CommandHandler implements IListener<MessageReceivedEvent> {

    private static Map<String, Command> commandMap = new HashMap<>();
    private static Map<Long, IMessage> processingMap = new HashMap<>();
    private static List<String> internalProcesses = new ArrayList<>(Arrays.asList("sig", "char", "head", "avatar", "help", "events"));

    private static EmbedBuilder processingMessage;

    static {
        processingMessage = new EmbedBuilder();
        String processingUrl = ConfigUtils.get("discord.bot.processing");
        if (!processingUrl.isEmpty()) {
            processingMessage.withImage(processingUrl);
        } else {
            processingMessage.withDescription("Processing...");
        }
    }

    static {
        commandMap.put("avatar", new AvatarCommand());
        commandMap.put("char", new CharCommand());
        commandMap.put("head", new HeadCommand());
        commandMap.put("events", new EventsCommand());
        commandMap.put("forums", new ForumsCommand());
        commandMap.put("help", new HelpCommand());
        commandMap.put("ii", new IiCommand());
        commandMap.put("map", new MapCommand());
        commandMap.put("mi", new MiCommand());
        commandMap.put("mvp", new PythonCommand());
        commandMap.put("mvphelp", new PythonCommand());
        commandMap.put("mvplist", new PythonCommand());
        commandMap.put("pc", new PcCommand());
        commandMap.put("sig", new SigCommand());
        commandMap.put("ws", new WsCommand());
        commandMap.put("zeny", new ZenyCommand());
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
