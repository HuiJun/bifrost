package net.zerofill;

import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;
import sx.blah.discord.api.IDiscordClient;

import net.zerofill.utils.DataUtils;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.Image;

public class MainRunner {

    public static void main(String[] args){
        String token;
        if(args.length != 1){
            token = ConfigUtils.get("discord.bot.token");
        } else {
            token = args[0];
        }

        DataUtils.loadAll();
        IDiscordClient client = BotUtils.getBuiltDiscordClient(token);
        client.getDispatcher().registerListener(new CommandHandler());
        client.getDispatcher().registerListener(readyEvent -> {
            if (client.isLoggedIn()) {
                String name = ConfigUtils.get("discord.bot.name");
                String playing = ConfigUtils.get("discord.bot.playing");
                String avatar = ConfigUtils.get("discord.bot.avatar.url");
                String avatarType = ConfigUtils.get("discord.bot.avatar.type");

                if (name != null && !name.isEmpty()) {
                    client.changeUsername(name);
                }
                if (playing != null && !playing.isEmpty()) {
                    client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, playing);
                }
                if (avatar != null && !avatar.isEmpty() && avatarType != null && !avatarType.isEmpty()) {
                    client.changeAvatar(Image.forUrl(avatarType, avatar));
                }
            }
        });

        client.login();
    }

}