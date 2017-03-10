package tk.daporkchop.porkbot;

import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import tk.daporkchop.porkbot.command.CommandRegistry;

/**
 * Created by daporkchop on 05.03.17.
 */
public class PorkListener extends ListenerAdapter {

    public static final MessageEmbed.Field PlayersHeader = new MessageEmbed.Field(null, "Test header!", false);
    public static final MessageEmbed.Field PlayersSubHeader = new MessageEmbed.Field(null, "", false);

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("asdf");
        if (event.getAuthor().isBot()) {
            //bots don't matter to us!
            return;
        }
        System.out.println("jklö");
        String message = event.getMessage().getRawContent();

        if (message.startsWith(".."))    {
            CommandRegistry.runCommand(event);
        }
    }
}
