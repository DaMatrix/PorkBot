package tk.daporkchop.porkbot;

import net.dv8tion.jda.core.entities.User;

/**
 * Created by daporkchop on 05.03.17.
 */
public abstract class Command {
    public abstract void execute(User user, String message);
}
