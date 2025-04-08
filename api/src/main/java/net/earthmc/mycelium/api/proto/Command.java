package net.earthmc.mycelium.api.proto;

import net.earthmc.mycelium.api.network.Player;
import org.jspecify.annotations.NullMarked;

/**
 * A class for holding information about a command.
 * @see Player#runCommand(Command)
 *
 * @implNote commandLine will always start with a slash.
 */
@NullMarked
public class Command {
    private final Target target;
    private final String commandLine;

    private Command(final Target target, final String commandLine) {
        this.target = target;
        this.commandLine = (commandLine.startsWith("/") ? commandLine : "/" + commandLine).trim();
    }

    /**
     * Constructs a new command that will be executed on the proxy.
     *
     * @param commandLine The command to run.
     * @return A new command instance.
     */
    public static Command proxy(String commandLine) {
        return new Command(Target.PROXY, commandLine);
    }

    /**
     * Constructs a new command that will be executed on the backend.
     *
     * @param commandLine The command to run.
     * @return A new command instance.
     */
    public static Command backend(String commandLine) {
        return new Command(Target.BACKEND, commandLine);
    }

    /**
     * @return The target.
     * @see Target
     */
    public Target target() {
        return this.target;
    }

    /**
     * @return The command, always preceded by a forward slash.
     */
    public String command() {
        return this.commandLine;
    }

    /**
     * The target for a command.
     */
    public enum Target {
        BACKEND, PROXY
    }
}
