package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.proto.ConsoleCommand;

/**
 * Represents a proxy frontend that players can connect to.
 */
public interface Proxy extends PlayerList {
    /**
     * @return The proxy id or name.
     */
    String id();

    /**
     * Runs a console command on this proxy.
     * @param command The console command to run.
     */
    void runConsoleCommand(ConsoleCommand command);
}
