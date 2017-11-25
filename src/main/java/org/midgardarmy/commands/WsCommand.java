package org.midgardarmy.commands;

import org.midgardarmy.novaro.NovaROClient;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsCommand implements Command {

    private static Map<String, Map<String, String>> commandCache = new HashMap<>();
    private static final String ITEM_NAME = "itemName";
    private static final String PAGE_NUM = "pageNum";

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "ws"), new ArrayList<>(Arrays.asList("<name or id>", "Searches NovaRO Market and returns either search results or vendors selling the item.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String itemName = args.isEmpty() ? "jellopy" : String.join(" ", args);
        List<EmbedObject> responses;

        String cacheKey = event.getAuthor().getStringID();
        int pageNum = 1;

        if (commandCache.containsKey(cacheKey)) {
            Map<String, String> previousCommand = commandCache.get(cacheKey);
            if (itemName.startsWith("next")) {
                itemName = previousCommand.get(ITEM_NAME);
                pageNum = Integer.parseInt(previousCommand.get(PAGE_NUM)) + 1;
            } else if (itemName.startsWith("prev")) {
                itemName = previousCommand.get(ITEM_NAME);
                pageNum = Integer.parseInt(previousCommand.get(PAGE_NUM)) - 1;
                if (pageNum < 1) {
                    pageNum = 1;
                }
            } else if (itemName.startsWith("page")) {
                String command = itemName;
                itemName = previousCommand.get(ITEM_NAME);
                pageNum = Integer.parseInt(command.substring(5));
            }
        }
        Map<String, String> cache = new HashMap<>();
        cache.put(ITEM_NAME, itemName);
        cache.put(PAGE_NUM, Integer.toString(pageNum > 1 ? pageNum : 1));
        commandCache.put(cacheKey, cache);

        if (!Character.isDigit(itemName.charAt(0))) {
            responses = NovaROClient.getByName(itemName, pageNum);
        } else {
            List<String> ids = Arrays.asList(itemName);
            responses = NovaROClient.getById(ids, pageNum, 0);
        }

        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
