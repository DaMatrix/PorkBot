package net.daporkchop.porkbot.command;

import net.daporkchop.porkbot.PorkBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.HashMap;

public abstract class CommandRegistry {

    /**
     * A HashMap containing all the commands and their prefix
     */
    private static final HashMap<String, Command> COMMANDS = new HashMap<>();

    /**
     * Counts all commands run this session
     */
    public static long COMMAND_COUNT = 0L;

    /**
     * Registers a command to the command registry.
     *
     * @param cmd
     * @return cmd again lul
     */
    public static final Command registerCommand(Command cmd) {
        COMMANDS.put(cmd.prefix, cmd);
        return cmd;
    }

    /**
     * Runs a comamnd
     *
     * @param evt
     */
    public static void runCommand(MessageReceivedEvent evt, String rawContent) {
        try {
            String[] split = rawContent.split(" ");
            Command cmd = COMMANDS.getOrDefault(split[0].substring(2), null);
            if (cmd != null) {
                evt.getTextChannel().sendTyping().queue();

                new Thread() {
                    @Override
                    public void run() {
                        cmd.excecute(evt, split, rawContent);
                        COMMAND_COUNT++;
                    }
                }.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            PorkBot.sendMessage("Error running command: `" + evt.getMessage().getRawContent() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getTextChannel());
        }
    }
}
