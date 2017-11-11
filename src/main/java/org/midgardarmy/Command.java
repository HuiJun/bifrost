package org.midgardarmy;

import java.util.List;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public interface Command {

    // Interface for a command to be implemented in the command map
    void runCommand(MessageReceivedEvent event, List<String> args);

}
