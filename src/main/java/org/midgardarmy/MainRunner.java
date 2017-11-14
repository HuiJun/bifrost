package org.midgardarmy;

import org.midgardarmy.utils.BotUtils;
import org.midgardarmy.utils.ConfigUtils;
import org.midgardarmy.utils.DataUtils;
import sx.blah.discord.api.IDiscordClient;

public class MainRunner {

    public static void main(String[] args){

        String token;
        if(args.length != 1){
            token = ConfigUtils.get("discord.bot.token");
        } else {
            token = args[0];
        }

        DataUtils.load();
        IDiscordClient cli = BotUtils.getBuiltDiscordClient(token);
        cli.getDispatcher().registerListener(new CommandHandler());
        cli.login();

    }

}