package org.midgardarmy.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public interface Command {

    Map<String, List<String>> helpMap = new HashMap<>();

    static final String ITEM_NAME = "itemName";
    static final String PAGE_NUM = "pageNum";

    // Interface for a command to be implemented in the command map
    void runCommand(MessageReceivedEvent event, List<String> args);

    static Map<String, String> getCached(Map<String, Map<String, String>> commandCache, String cacheKey, String itemName) {
        Map<String, String> result = new HashMap<>();
        Integer pageNum = 1;
        StringBuilder parsedItemName = new StringBuilder();

        if (commandCache.containsKey(cacheKey) &&
                (itemName.startsWith("next") || itemName.startsWith("prev") || itemName.startsWith("page"))) {
            Map<String, String> previousCommand = commandCache.get(cacheKey);
            if (itemName.startsWith("next")) {
                parsedItemName.append(previousCommand.get(ITEM_NAME));
                pageNum = Integer.parseInt(previousCommand.get(PAGE_NUM)) + 1;
            } else if (itemName.startsWith("prev")) {
                parsedItemName.append(previousCommand.get(ITEM_NAME));
                pageNum = Integer.parseInt(previousCommand.get(PAGE_NUM)) - 1;
                if (pageNum < 1) {
                    pageNum = 1;
                }
            } else if (itemName.startsWith("page")) {
                String command = itemName;
                parsedItemName.append(previousCommand.get(ITEM_NAME));
                pageNum = Integer.parseInt(command.substring(5));
            }
        }

        if (parsedItemName.length() < 1) {
            parsedItemName.append(itemName);
        }

        result.put(ITEM_NAME, parsedItemName.toString());
        result.put(PAGE_NUM, Integer.toString(pageNum));

        return result;
    }
}