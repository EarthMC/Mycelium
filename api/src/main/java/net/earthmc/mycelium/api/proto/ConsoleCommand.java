package net.earthmc.mycelium.api.proto;

import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.NullMarked;

/**
 * Represents a command that will be executed from console.
 *
 * @implNote Like actual console commands, commandLine does not start with a slash.
 * @see Server#runConsoleCommand(ConsoleCommand)
 * @see Proxy#runConsoleCommand(ConsoleCommand)
 */
@NullMarked
public class ConsoleCommand {
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
     * @param commandLine The command to run.
     * @return A new console command instance.
     */
    public static ConsoleCommand command(final String commandLine) {
        return new ConsoleCommand(commandLine);
    }

    /**
     * @return The command to run.
     */
    public String command() {
        return this.commandLine;
    }
}
