package org.midgardarmy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
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

    public static void sendReply(IMessage initial, Object message){

        RequestBuffer.request(() -> {
            try{
                if (message instanceof EmbedObject) {
                    initial.reply("", (EmbedObject) message);
                } else {
                    initial.reply((String) message);
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
