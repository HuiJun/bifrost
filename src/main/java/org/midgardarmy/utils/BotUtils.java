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

    // Constants for use throughout the bot
    public static String BOT_PREFIX = ConfigUtils.get("discord.bot.prefix");

    // Handles the creation and getting of a IDiscordClient object for a token
    public static IDiscordClient getBuiltDiscordClient(String token){

        // The ClientBuilder object is where you will attach your params for configuring the instance of your bot.
        // Such as withToken, setDaemon etc
        return new ClientBuilder()
                .withToken(token)
                .withRecommendedShardCount()
                .build();

    }

    // Helper functions to make certain aspects of the bot easier to use.
    public static void sendRawMessage(IChannel channel, String message){

        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (DiscordException e){
                logger.debug("Message could not be sent with error: ", e);
            }
        });

    }

    public static void sendEmbeddedMessage(IChannel channel, EmbedObject message){

        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (DiscordException e){
                logger.debug("Message could not be sent with error: ", e);
            }
        });

    }
}
