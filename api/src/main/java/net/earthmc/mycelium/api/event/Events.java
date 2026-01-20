package net.earthmc.mycelium.api.event;

import java.util.function.Consumer;

/**
 * An interface for registering and executing listeners for events.
 */
public interface Events {
    /**
     * Registers a listener for an event.
     *
     * @param eventClass The class of the event to listen to.
     * @param listener A consumer that is invoked with an instance of the event
     * @return A {@link RegisteredEvent} instance that can be used to remove this registration.
     * @param <T> The type of the event.
     */
    <T extends Event> RegisteredEvent registerEvent(Class<T> eventClass, Consumer<T> listener);

    /**
     * Broadcasts the given event to all listeners across every instance.
     *
     * @param event The event to broadcast.
     * @throws IllegalArgumentException if event is not registered properly internally.
     */
    void broadcast(final Event event);
}
