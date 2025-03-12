package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.messaging.MessageRecipient;

/**
 * Represents a proxy frontend that players can connect to.
 */
public interface Proxy extends PlayerList, MessageRecipient {
    /**
     * @return The proxy id or name.
     */
    String id();
}
