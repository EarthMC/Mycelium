package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.messaging.MessageRecipient;

/**
 * Represents a backend server that players can connect to.
 */
public interface Server extends PlayerList, MessageRecipient {
    /**
     * @return The server name.
     */
    String name();
}
