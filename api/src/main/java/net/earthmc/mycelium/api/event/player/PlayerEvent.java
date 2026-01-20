package net.earthmc.mycelium.api.event.player;

import net.earthmc.mycelium.api.event.Event;
import net.earthmc.mycelium.api.network.Player;

/**
 * Represents an event that has to do with a specific player.
 */
public interface PlayerEvent extends Event {
    /**
     * {@return the player connected to this event}
     */
    Player player();
}
