package net.earthmc.mycelium.api.network.command;

import net.earthmc.mycelium.api.network.Player;

/**
 * A class for holding information about a command.
 * @see Player#runCommand(Command)
 *
 * @implNote commandLine never starts with a slash
 */
public class Command {
    private final Target target;
    private final String commandLine;

    private Command(final Target target, String commandLine) {
        this.target = target;

        // Trim both before removing leading slashes and after
        commandLine = commandLine.trim();
        this.commandLine = (commandLine.startsWith("/") ? commandLine.substring(1) : commandLine).trim();

        if (this.commandLine.isEmpty()) {
            throw new IllegalArgumentException("command may not be empty.");
        }
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
     * {@return Where this command will be executed, backend or proxy}
     */
    public Target target() {
        return this.target;
    }

    /**
     * {@return The command, always preceded by a forward slash}
     */
    public String command() {
        return this.commandLine;
    }

    /**
     * The target for a command (where you want to run it).
     */
    public enum Target {
        /**
         * A backend target.
         */
        BACKEND,
        /**
         * A proxy target.
         */
        PROXY
    }
}
