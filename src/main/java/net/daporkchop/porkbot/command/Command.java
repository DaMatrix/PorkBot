package net.daporkchop.porkbot.command;

import net.daporkchop.porkbot.PorkBot;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class Command {

    public String prefix;

    public Command(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Does command logic!
     *
     * @param evt The MessageReceivedEvent to be parsed
     */
    public abstract void excecute(MessageReceivedEvent evt, String[] split, String rawContent);

    /**
     * Gets the command's usage
     *
     * you should probably override this
     *
     * @return
     */
    public String getUsage() {
        return ".." + prefix;
    }

    /**
     * Gets and example of using the command
     *
     * you should probably override this
     *
     * @return
     */
    public String getUsageExample() {
        return ".." + prefix;
    }

    public void sendErrorMessage(TextChannel channel, String message) {
        PorkBot.sendMessage((message == null ? "" : message + "\n") + "Usage: `" + getUsage() + "`\nExample: `" + getUsageExample() + "`", channel);
    }
}
