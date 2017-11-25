package org.midgardarmy;

import sx.blah.discord.api.IDiscordClient;

import org.midgardarmy.commands.CommandHandler;
import org.midgardarmy.utils.BotUtils;
import org.midgardarmy.utils.ConfigUtils;
import org.midgardarmy.utils.DataUtils;

public class MainRunner {

    public static void main(String[] args){
        String token;
        if(args.length != 1){
            token = ConfigUtils.get("discord.bot.token");
        } else {
            token = args[0];
        }

        DataUtils.loadAll();
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(token);
        cli.getDispatcher().registerListener(new CommandHandler());
        cli.login();
    }

}