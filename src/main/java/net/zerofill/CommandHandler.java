package net.zerofill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import net.zerofill.commands.AddAttCommand;
import net.zerofill.commands.CharCommand;
import net.zerofill.commands.ClearAttCommand;
import net.zerofill.commands.EventsCommand;
import net.zerofill.commands.EventsReminderCommand;
import net.zerofill.commands.HeadCommand;
import net.zerofill.commands.SetAttCommand;
import net.zerofill.commands.AvatarCommand;
import net.zerofill.commands.Command;
import net.zerofill.commands.HelpCommand;
import net.zerofill.commands.IiCommand;
import net.zerofill.commands.MapCommand;
import net.zerofill.commands.MiCommand;
import net.zerofill.commands.PcCommand;
import net.zerofill.commands.PvmCommand;
import net.zerofill.commands.SigCommand;
import net.zerofill.commands.WsCommand;
import net.zerofill.commands.ZenyCommand;
import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;

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
        commandMap.put("addatt", new AddAttCommand());
        commandMap.put("avatar", new AvatarCommand());
        commandMap.put("char", new CharCommand());
        commandMap.put("clearatt", new ClearAttCommand());
        commandMap.put("head", new HeadCommand());
        commandMap.put("events", new EventsCommand());
        commandMap.put("help", new HelpCommand());
        commandMap.put("ii", new IiCommand());
        commandMap.put("map", new MapCommand());
        commandMap.put("mi", new MiCommand());
        commandMap.put("pc", new PcCommand());
        commandMap.put("pvm", new PvmCommand());
        commandMap.put("remind", new EventsReminderCommand());
        commandMap.put("setatt", new SetAttCommand());
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
