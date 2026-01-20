package net.earthmc.mycelium.api.event.server;

import net.earthmc.mycelium.api.event.Event;
import net.earthmc.mycelium.api.network.Server;

/**
 * Represents an event that has something to do with a server.
 */
public interface ServerEvent extends Event {
    /**
     * {@return the server attached to this event}
     */
    Server server();
}
