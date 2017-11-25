package org.midgardarmy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {

    private static final Logger logger = LoggerFactory.getLogger(BotUtils.class);

    public static final String BOT_PREFIX = ConfigUtils.get("discord.bot.prefix");

    public static IDiscordClient getBuiltDiscordClient(String token){
        return new ClientBuilder()
                .withToken(token)
                .withRecommendedShardCount()
                .build();
    }

    public static void sendMessage(IChannel channel, Object message){
        RequestBuffer.request(() -> {
            try{
                if (message instanceof EmbedObject) {
                    channel.sendMessage((EmbedObject) message);
                } else {
                    channel.sendMessage((String) message);
                }
            } catch (DiscordException e){
                if (logger.isDebugEnabled()) {
                    logger.debug("Message could not be sent with error: ", e);
                }
            }
        });
    }

    private BotUtils() {}

}
