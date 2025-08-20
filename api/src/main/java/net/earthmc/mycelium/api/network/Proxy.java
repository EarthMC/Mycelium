package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.messaging.MessageRecipient;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;

/**
 * Represents a proxy that players can connect to.
 */
public interface Proxy extends PlayerList, MessageRecipient {
    /**
     * {@return the proxy id or name}
     */
    String id();

    /**
     * Runs a console command on this proxy.
     * @param command The console command to run.
     */
    void runConsoleCommand(ConsoleCommand command);
}
