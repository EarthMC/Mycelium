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
     * @return A {@link EventListener} instance that can be used to remove this registration.
     * @param <T> The type of the event.
     * @deprecated Use {@link #registerListener(Class, Consumer)} instead.
     */
    @Deprecated(forRemoval = true)
    default <T extends Event> EventListener registerEvent(Class<T> eventClass, Consumer<T> listener) {
        return registerEvent(eventClass, listener);
    }

    /**
     * Registers a listener for an event.
     *
     * @param eventClass The class of the event to listen to.
     * @param listener A consumer that is invoked with an instance of the event
     * @return A {@link EventListener} instance that can be used to remove this registration.
     * @param <T> The type of the event.
     */
    <T extends Event> EventListener registerListener(Class<T> eventClass, Consumer<T> listener);

    /**
     * Broadcasts the given event to all listeners across every instance.
     *
     * @param event The event to broadcast.
     * @throws IllegalArgumentException if the event is not registered properly internally.
     * @deprecated Use {@link #broadcastEvent(Event)} instead.
     */
    @Deprecated(forRemoval = true)
    default void broadcast(final Event event) {
        broadcastEvent(event);
    }

    /**
     * Broadcasts the given event to all listeners across every instance.
     *
     * @param event The event to broadcast.
     * @throws IllegalArgumentException if the event is not registered properly internally.
     */
    void broadcastEvent(final Event event);
}
