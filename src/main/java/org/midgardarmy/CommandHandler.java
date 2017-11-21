package org.midgardarmy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import org.midgardarmy.divinepride.DivinePrideClient;
import org.midgardarmy.novaro.NovaROClient;
import org.midgardarmy.novaro.NovaROEvents;
import org.midgardarmy.rochargen.ROChargenURLGen;
import org.midgardarmy.utils.BotUtils;
import org.midgardarmy.utils.ConfigUtils;

public class CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private static Map<String, Command> commandMap = new HashMap<>();
    private static Map<Long, IMessage> processingMap = new HashMap<>();
    private static List<String> internalProcesses = new ArrayList<>(Arrays.asList("sig", "char", "help", "events"));

    private static Map<String, Map<String, String>> commandCache = new HashMap<>();

    private static Map<String, List<String>> helpMap = new HashMap<>();

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "sig"), new ArrayList<>(Arrays.asList("[name or id]", "If provided, returns a generated signature url for the name provided. Otherwise, uses the display name of the user.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "char"), new ArrayList<>(Arrays.asList("[name or id]", "If provided, returns a generated character url for the name provided. Otherwise, uses the display name of the user.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "events"), new ArrayList<>(Arrays.asList("Lists event countdowns.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "mi"), new ArrayList<>(Arrays.asList("<name or id>", "Returns information about a mob. Passing a name will return all matching entries.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "ii"), new ArrayList<>(Arrays.asList("<name or id>", "Returns information about an item. Passing a name will return all matching entries.")));
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "ws"), new ArrayList<>(Arrays.asList("<name or id>", "Searches NovaRO Market and returns either search results or vendors selling the item.")));
    }

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
        commandMap.put("help", (event, args) -> {
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
        });

        commandMap.put("sig", (event, args) -> {
            String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
            EmbedObject response = ROChargenURLGen.generateSig(charName);
            BotUtils.sendMessage(event.getChannel(), response);
        });

        commandMap.put("char", (event, args) -> {
            String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
            EmbedObject response = ROChargenURLGen.generateChar(charName);
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

        commandMap.put("map", (event, args) -> {
            String mapName = args.isEmpty() ? "prontera" : String.join(" ", args);
            List<EmbedObject> responses;

            if (!Character.isDigit(mapName.charAt(0))) {
                responses = DivinePrideClient.getByName("m", mapName);
            } else {
                responses = DivinePrideClient.getById("m", args);
            }

            processingMap.get(event.getMessageID()).delete();
            processingMap.remove(event.getMessageID());

            for (EmbedObject response : responses) {
                BotUtils.sendMessage(event.getChannel(), response);
            }
        });

        commandMap.put("events", (event, args) -> {
            List<EmbedObject> results = NovaROEvents.getEvents();

            for (EmbedObject result : results) {
                BotUtils.sendMessage(event.getChannel(), result);
            }
        });

        commandMap.put("ws", (event, args) -> {
            String itemName = args.isEmpty() ? "jellopy" : String.join(" ", args);
            List<EmbedObject> responses;

            String cacheKey = event.getAuthor().getStringID();
            int pageNum = 1;

            if (commandCache.containsKey(cacheKey)) {
                Map<String, String> previousCommand = commandCache.get(cacheKey);
                if (itemName.startsWith("next")) {
                    itemName = previousCommand.get("itemName");
                    pageNum = Integer.parseInt(previousCommand.get("pageNum")) + 1;
                } else if (itemName.startsWith("prev")) {
                    itemName = previousCommand.get("itemName");
                    pageNum = Integer.parseInt(previousCommand.get("pageNum")) - 1;
                    if (pageNum < 1) {
                        pageNum = 1;
                    }
                } else if (itemName.startsWith("page")) {
                    String command = itemName;
                    itemName = previousCommand.get("itemName");
                    pageNum = Integer.parseInt(command.substring(5));
                }
            }
            Map<String, String> cache = new HashMap<>();
            cache.put("itemName", itemName);
            cache.put("pageNum", Integer.toString(pageNum > 1 ? pageNum : 1));
            commandCache.put(cacheKey, cache);

            if (!Character.isDigit(itemName.charAt(0))) {
                responses = NovaROClient.getByName(itemName, pageNum);
            } else {
                responses = NovaROClient.getById(args, pageNum, 0);
            }

            processingMap.get(event.getMessageID()).delete();
            processingMap.remove(event.getMessageID());

            for (EmbedObject response : responses) {
                BotUtils.sendMessage(event.getChannel(), response);
            }
        });

        commandMap.put("helppretty", (event, args) -> {
            EmbedBuilder helpMessage = new EmbedBuilder();
            helpMessage.withTitle("Help Menu");
            helpMessage.withDescription("<required> or [optional] arguments");

            for (Map.Entry<String, List<String>> entry : helpMap.entrySet()) {
                String commandId = entry.getKey();
                List<String> helpMessages = entry.getValue();
                if (helpMessages.size() > 1) {
                    helpMessage.appendField(String.format("%s %s", commandId, helpMessages.get(0)), helpMessages.get(1), false);
                } else {
                    helpMessage.appendField(commandId, helpMessages.get(0), false);
                }
            }

            processingMap.get(event.getMessageID()).delete();
            processingMap.remove(event.getMessageID());

            BotUtils.sendMessage(event.getChannel(), helpMessage.build());
        });
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
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
        }
    }

}
