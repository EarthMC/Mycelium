package net.earthmc.mycelium.api;

/**
 * Represents a backend server that players can connect to.
 */
public interface Server extends PlayerList {
    /**
     * @return The server name.
     */
    String name();
}
