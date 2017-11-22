package org.midgardarmy.commands;

import org.midgardarmy.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MvpCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(MvpCommand.class);

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String s = null;
        try {
            String mvpbotLocation = ConfigUtils.get("mvpbot.location");
            Process p = Runtime.getRuntime().exec(String.format("python %s", mvpbotLocation));

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("MVP Error: ", e);
            }
        }
    }
}
