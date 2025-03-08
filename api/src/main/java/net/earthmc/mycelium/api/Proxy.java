package net.earthmc.mycelium.api;

/**
 * Represents a proxy frontend that players can connect to.
 */
public interface Proxy extends PlayerList {
    /**
     * @return The proxy id or name.
     */
    String id();
}
