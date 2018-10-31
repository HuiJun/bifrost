package net.zerofill.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
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

    public static synchronized void sendMessage(IChannel channel, Object message){
        sendMessage(channel, message, false);
    }

    public static synchronized void sendMessage(IChannel channel, Object message, boolean raw){
        if (raw) {
            parseMessage(channel, message);
        } else {
            RequestBuffer.request(() -> parseMessage(channel, message));
        }
    }

    private static synchronized void parseMessage(IChannel channel, Object message) {
        try {
            if (message instanceof EmbedObject) {
                channel.sendMessage((EmbedObject) message);
            } else {
                channel.sendMessage(message.toString());
            }
        } catch (DiscordException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Message could not be sent with error: ", e);
            }
        }
    }

    public static synchronized void assignRole(MessageReceivedEvent event, IRole role) {
        IUser user = event.getAuthor();

        if (role != null) {
            try {
                user.addRole(role);
                String response = String.format("%s, added role %s", user.mention(), role.getName());
                sendMessage(event.getChannel(), response);
            } catch (MissingPermissionsException mpe) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Permissions error: ", mpe);
                }
                sendMessage(event.getChannel(), String.format("Failed with message: %s", mpe.getErrorMessage()));
            }
        }
    }

    public static synchronized void removeRole(MessageReceivedEvent event, IRole role) {
        IUser user = event.getAuthor();

        if (role != null) {
            try {
                user.removeRole(role);
                String response = String.format("%s, removed role %s", user.mention(), role.getName());
                sendMessage(event.getChannel(), response);
            } catch (MissingPermissionsException mpe) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Permissions error: ", mpe);
                }
                sendMessage(event.getChannel(), String.format("Failed with message: %s", mpe.getErrorMessage()));
            }
        }

    }

    private BotUtils() {}

}
