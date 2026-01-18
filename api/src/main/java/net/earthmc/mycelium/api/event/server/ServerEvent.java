package net.earthmc.mycelium.api.event.server;

import net.earthmc.mycelium.api.event.Event;
import net.earthmc.mycelium.api.network.Server;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an event that has something to do with a server.
 */
@NullMarked
public interface ServerEvent extends Event {
    /**
     * {@return the server attached to this event}
     */
    Server server();
}
