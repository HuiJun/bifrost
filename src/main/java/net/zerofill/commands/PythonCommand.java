package net.zerofill.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.zerofill.utils.BotUtils;
import net.zerofill.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class PythonCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(PythonCommand.class);

    @Override
    public void runCommand(MessageReceivedEvent event, List<String> args) {
        String script = ConfigUtils.get("bifrost.mvpbot.location");

        File file = new File(script);

        if (!file.exists()) {
            BotUtils.sendMessage(event.getChannel(), createMessage("File not found."));
            return;
        }

        String python = ConfigUtils.get("bifrost.python");
        String[] argArray = event.getMessage().getContent().split(" ");

        String commandStr = argArray[0].substring(BotUtils.BOT_PREFIX.length());
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.set(0, commandStr);
        String arguments = String.join(" ", argsList);

        String filePath = file.getAbsolutePath();
        String directory = filePath.substring(0, filePath.lastIndexOf(File.separator));

        try {
            Runtime.getRuntime().exec(String.format("git -C '%s' pull;", directory));
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Git Error: ", e);
            }
        }

        try {
            Process p = Runtime.getRuntime().exec(String.format("%s %s %s", python, script, arguments));

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String stdErr = stdError.lines().collect(Collectors.joining(String.format("%n")));
            if (stdErr.length() > 0) {
                if (logger.isDebugEnabled()) {
                    BotUtils.sendMessage(event.getChannel(), createMessage(stdErr));
                    logger.debug("Python Error: ", stdErr);
                }
                return;
            }
            String stdIn = stdInput.lines().collect(Collectors.joining(String.format("%n")));
            if (stdIn.length() > 0) {
                BotUtils.sendMessage(event.getChannel(), createMessage(stdIn));
                return;
            }
            BotUtils.sendMessage(event.getChannel(), createMessage("No Errors Or Output"));
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Python Error: ", e);
            }
        }
    }

    private static String createMessage(String message) {
        StringBuilder response = new StringBuilder();
        response.append("```python");
        response.append(String.format("%n"));
        response.append(message);
        response.append(String.format("%n"));
        response.append("```");

        return response.toString();
    }
}
