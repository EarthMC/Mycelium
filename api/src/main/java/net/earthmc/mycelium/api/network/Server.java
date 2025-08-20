package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.messaging.MessageRecipient;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;

/**
 * Represents a backend server that players can connect to.
 */
public interface Server extends PlayerList, MessageRecipient {
    /**
     * {@return the server name}
     */
    String name();

    /**
     * Runs a console command on this server.
     * @param command The console command to run.
     */
    void runConsoleCommand(ConsoleCommand command);
}
