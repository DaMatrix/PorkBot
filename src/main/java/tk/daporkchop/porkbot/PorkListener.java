package tk.daporkchop.porkbot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Created by daporkchop on 05.03.17.
 */
public class PorkListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("asdf");
        if (event.getAuthor().isBot()) {
            //bots don't matter to us!
            return;
        }

        String message = event.getMessage().getRawContent();

        if (message.startsWith(".."))    {
            event.getChannel().sendMessage("Command recieved! Nothing happens tho because @DaPorkchop_#2459 hasn't finished me yet").queue();
        }
    }
}
