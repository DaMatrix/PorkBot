package net.daporkchop.porkbot;

import net.daporkchop.porkbot.command.CommandRegistry;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

public class PorkListener extends ListenerAdapter {

//    public static final MessageEmbed.Field PlayersHeader = new MessageEmbed.Field(null, "Test header!", false);
//   public static final MessageEmbed.Field PlayersSubHeader = new MessageEmbed.Field(null, "", false);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            //bots don't matter to us!
            return;
        }
        String message = event.getMessage().getRawContent();

        if (message.startsWith("..")) {
            CommandRegistry.runCommand(event, message);
        } else if (event.getChannelType().ordinal() == ChannelType.PRIVATE.ordinal()) {
            if (event.getAuthor().getId().equals("226975061880471552")) {
                switch (message) {
                    case ",,instareboot":
                        event.getChannel().sendMessage("Rebooting...").queue();
                        System.out.println("Rebooting...");
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                    PorkBot.INSTANCE.jda.shutdown();
                                    System.exit(0);
                                } catch (InterruptedException e) {

                                }
                            }
                        }.start();
                        return;
                }

                if (message.startsWith(",,announce ")) {
                    String toAnnouce = message.substring(11);

                    List<Guild> servers = PorkBot.INSTANCE.jda.getGuilds();
                    for (Guild server : servers) {
                        try {
                            server.getPublicChannel().sendMessage(toAnnouce).queue();
                        } catch (PermissionException e) {
                            //who cares, we can't do anything about it
                        }
                    }
                }
            }
        }
    }
}
