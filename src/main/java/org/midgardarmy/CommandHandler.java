package org.midgardarmy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.midgardarmy.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import org.midgardarmy.divinepride.DivinePrideClient;
import org.midgardarmy.rochargen.ROChargenURLGen;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private static Map<String, Command> commandMap = new HashMap<>();
    private static Map<Long, IMessage> processingMap = new HashMap<>();
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

        commandMap.put("sig", (event, args) -> {

            String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
            String response = ROChargenURLGen.generateSig(charName);
            processingMap.get(event.getMessageID()).delete();
            processingMap.remove(event.getMessageID());
            BotUtils.sendMessage(event.getChannel(), response);

        });

        commandMap.put("ii", (event, args) -> {

            String itemName = args.isEmpty() ? "jellopy" : String.join(" ", args);
            List<EmbedObject> responses;
            if (!Character.isDigit(itemName.charAt(0))) {
                responses = DivinePrideClient.getByName("i", itemName);
            } else {
                responses = DivinePrideClient.getById("i", args);
            }
            processingMap.get(event.getMessageID()).delete();
            processingMap.remove(event.getMessageID());
            for (EmbedObject response : responses) {
                BotUtils.sendMessage(event.getChannel(), response);
            }
        });

        commandMap.put("mi", (event, args) -> {

            String monsterName = args.isEmpty() ? "poring" : String.join(" ", args);
            List<EmbedObject> responses;
            if (!Character.isDigit(monsterName.charAt(0))) {
                responses = DivinePrideClient.getByName("m", monsterName);
            } else {
                responses = DivinePrideClient.getById("m", args);
            }
            processingMap.get(event.getMessageID()).delete();
            processingMap.remove(event.getMessageID());
            for (EmbedObject response : responses) {
                BotUtils.sendMessage(event.getChannel(), response);
            }
        });
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {

        String[] argArray = event.getMessage().getContent().split(" ");

        if (argArray.length == 0)
            return;

        if (!argArray[0].startsWith(BotUtils.BOT_PREFIX)) {
            return;
        }

        String commandStr = argArray[0].substring(BotUtils.BOT_PREFIX.length());

        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0);

        if (commandMap.containsKey(commandStr)) {
            IMessage initial = event.getChannel().sendMessage(processingMessage.build());
            processingMap.put(event.getMessageID(), initial);
            commandMap.get(commandStr).runCommand(event, argsList);
        }

    }

}
