package net.earthmc.mycelium.api.event;

/**
 * Represents a registered listener for an event.
 */
public interface RegisteredEvent {
    /**
     * Attempts to unregister this event and returns whether it was successful.
     *
     * @return Whether event listener unregistration was successful.
     */
    boolean unregister();
}
