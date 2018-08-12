package net.zerofill.commands;

import net.zerofill.rochargen.ROChargenURLGen;
import net.zerofill.utils.BotUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminCommands {

    public class KickCommand implements Command {

        @Override
        public void runCommand(MessageReceivedEvent event, List<String> args) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.withDescription("```md");
            builder.appendDescription(String.format("%n"));

            if (!args.isEmpty()) {
                StringBuilder sb = new StringBuilder();

                IGuild guild = event.getGuild();
                IUser initiator = event.getAuthor();
                String userName = args.remove(0);
                List<IUser> users = new ArrayList<>();

                if (!Character.isDigit(userName.charAt(0))) {
                    users = guild.getUsersByName(userName);
                } else {
                    users.add(guild.getUserByID(Long.parseLong(userName)));
                }

                StringBuilder reason = new StringBuilder();

                if (!args.isEmpty()) {
                    reason.append(String.format("%s%n", String.join(" ", args)));
                } else {
                    reason.append("No Reason.");
                }

                switch (users.size()) {
                    case 0:
                        sb.append(String.format("No users named %s found, please try with an ID", userName));
                        break;
                    case 1:
                        guild.kickUser(users.get(0), reason.toString());
                        sb.append(String.format("%s kicked %s: %s", initiator.getDisplayName(guild), userName, reason.toString()));
                        break;
                    default:
                        sb.append(String.format("Multiple users found for %s, please try with an ID", userName));
                        break;
                }

                builder.appendDescription(sb.toString());
            } else {
                builder.appendDescription("Nothing to do.");
            }

            builder.appendDescription(String.format("%n"));
            builder.appendDescription("```");

            BotUtils.sendMessage(event.getChannel(), builder.build());
        }
    }

    public class BanCommand implements Command {

        @Override
        public void runCommand(MessageReceivedEvent event, List<String> args) {
            String charName = args.isEmpty() ? event.getAuthor().getDisplayName(event.getGuild()) : String.join(" ", args);
            EmbedObject response = ROChargenURLGen.generateSig(charName);
            BotUtils.sendMessage(event.getChannel(), response);
        }
    }
}
