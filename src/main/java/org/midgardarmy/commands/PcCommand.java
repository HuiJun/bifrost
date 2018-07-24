package org.midgardarmy.commands;

import org.midgardarmy.novaro.actions.NovaROMarketHistory;
import org.midgardarmy.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PcCommand implements Command {

    private static Map<String, Map<String, String>> commandCache = new HashMap<>();

    static {
        helpMap.put(String.format("%s%s", BotUtils.BOT_PREFIX, "pc"), new ArrayList<>(Arrays.asList("<name or id>", "Searches NovaRO Market and returns either search results or market history of the item.")));
    }

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String itemName = args.isEmpty() ? "jellopy" : String.join(" ", args);
        List<EmbedObject> responses;

        String cacheKey = event.getAuthor().getStringID();

        int refine = 0;
        if (itemName.startsWith("+")) {
            int space = itemName.indexOf(' ');
            refine = Integer.parseInt(itemName.substring(1, space));
            itemName = itemName.substring(space + 1);
        }

        Map<String, String> cache = Command.getCached(commandCache, cacheKey, itemName, refine);

        if (!Character.isDigit(cache.get(ITEM_NAME).charAt(0))) {
            responses = NovaROMarketHistory.getByName(cache);
        } else {
            List<String> ids = Collections.singletonList(cache.get(ITEM_NAME));
            responses = NovaROMarketHistory.getById(ids, Integer.parseInt(cache.get(PAGE_NUM)), Integer.parseInt(cache.get(REFINE)));
        }

        commandCache.put(cacheKey, cache);
        for (EmbedObject response : responses) {
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
