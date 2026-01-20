package net.earthmc.mycelium.api.network.command;

import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.serialization.JsonCodec;

/**
 * Represents a command that will be executed from console.
 *
 * @implNote Like actual console commands, commandLine does not start with a slash.
 * @see Server#runConsoleCommand(ConsoleCommand)
 * @see Proxy#runConsoleCommand(ConsoleCommand)
 */
public class ConsoleCommand {
    /**
     * A codec capable of serializing a {@link ConsoleCommand}.
     */
    public static final JsonCodec<ConsoleCommand> CODEC = JsonCodec.simple();

    private final String commandLine;

    private ConsoleCommand(String commandLine) {
        commandLine = commandLine.trim();
        this.commandLine = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;

        if (this.commandLine.isEmpty()) {
            throw new IllegalArgumentException("command may not be empty.");
        }
    }

    /**
     * Returns a new {@link ConsoleCommand} for the given command.
     *
     * @param commandLine The command to run.
     * @return A new console command instance.
     */
    public static ConsoleCommand command(final String commandLine) {
        return new ConsoleCommand(commandLine);
    }

    /**
     * {@return the command to run}
     */
    public String command() {
        return this.commandLine;
    }
}
