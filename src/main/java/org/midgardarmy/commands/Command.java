package org.midgardarmy.commands;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Command {

    Map<String, String> commandMap = new HashMap<>();
    Map<String, List<String>> helpMap = new HashMap<>();

    Map<String, Integer> refineCache = new HashMap<>();

    String ITEM_NAME = "itemName";
    String PAGE_NUM = "pageNum";
    String REFINE = "refine";

    // Interface for a command to be implemented in the command map
    void runCommand(MessageReceivedEvent event, List<String> args);

    static Map<String, String> getCached(Map<String, Map<String, String>> commandCache, String cacheKey, String itemName, int refine) {
        Map<String, String> result = new HashMap<>();
        Integer pageNum = 1;
        StringBuilder parsedItemName = new StringBuilder();

        if (!refineCache.containsKey(cacheKey)) {
            refineCache.put(cacheKey, refine);
        }

        if (commandCache.containsKey(cacheKey) &&
                (itemName.startsWith("next") || itemName.startsWith("prev") || itemName.startsWith("page"))) {
            Map<String, String> previousCommand = commandCache.get(cacheKey);
            result.put(REFINE, Integer.toString(refineCache.get(cacheKey)));

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
                parsedItemName.append(previousCommand.get(ITEM_NAME));
                pageNum = Integer.parseInt(itemName.substring(5));
            }
        } else if (commandCache.containsKey(cacheKey)) {
            refineCache.remove(cacheKey);
            result.put(REFINE, "0");
        } else {
            result.put(REFINE, "0");
        }

        if (parsedItemName.length() < 1) {
            parsedItemName.append(itemName);
        }

        result.put(ITEM_NAME, parsedItemName.toString());
        result.put(PAGE_NUM, Integer.toString(pageNum));

        return result;
    }
}