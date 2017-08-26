package net.daporkchop.porkbot.command;

import net.daporkchop.porkbot.PorkBot;
import net.daporkchop.porkbot.util.DataTag;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.File;
import java.util.HashMap;

public abstract class CommandRegistry {

    /**
     * A HashMap containing all the commands and their prefix
     */
    public static final HashMap<String, Command> COMMANDS = new HashMap<>();

    /**
     * Counts all commands run this session
     */
    public static long COMMAND_COUNT = 0L;

    /**
     * Counts all commands run
     */
    public static long COMMAND_COUNT_TOTAL;

    private static DataTag command_save;

    static {
        command_save = new DataTag(new File(System.getProperty("user.dir") + File.separatorChar + "command_info.dat"));
        COMMAND_COUNT_TOTAL = command_save.getLong("totalCommands", 0L);
    }

    /**
     * Registers a command to the command registry.
     *
     * @param cmd
     * @return cmd again lul
     */
    public static final Command registerCommand(Command cmd) {
        cmd.uses = command_save.getInteger(cmd.prefix + "_uses", 0);
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
            if (evt.getTextChannel() == null) {
                return;
            }

            String[] split = rawContent.split(" ");
            Command cmd = COMMANDS.getOrDefault(split[0].substring(2), null);
            if (cmd != null) {
                evt.getTextChannel().sendTyping().queue();

                new Thread() {
                    @Override
                    public void run() {
                        cmd.execute(evt, split, rawContent);
                        COMMAND_COUNT++;
                        COMMAND_COUNT_TOTAL++;
                        cmd.uses++;
                    }
                }.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            PorkBot.sendMessage("Error running command: `" + evt.getMessage().getRawContent() + "`:\n`" + e.getClass().getCanonicalName() + "`", evt.getTextChannel());
        }
    }

    public static void save() {
        for (Command cmd : COMMANDS.values()) {
            command_save.setInteger(cmd.prefix + "_uses", cmd.uses);
        }
        command_save.setLong("totalCommands", COMMAND_COUNT_TOTAL);
        command_save.save();
    }
}
