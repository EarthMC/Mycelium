package net.earthmc.mycelium.client.impl.event;

import net.earthmc.mycelium.api.event.Event;
import net.earthmc.mycelium.api.event.RegisteredEvent;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.impl.messaging.RegisteredListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Represents a subscription to a single event
 */
public record RegisteredEventImpl<T extends Event>(Class<T> eventClass, EventsImpl eventsManager, JsonCodec<T> codec, Consumer<T> listener, RegisteredListener<T> registeredListener) implements RegisteredEvent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredEventImpl.class);

    @SuppressWarnings("unchecked")
    public void onEventReceived(final Event eventInstance) {
        try {
            this.listener.accept((T) eventInstance);
        } catch (RuntimeException e) {
            LOGGER.warn("An exception occurred while executing a listener for event {}", eventClass.getName());
        }
    }

    @Override
    public boolean unregister() {
        return eventsManager.unregister(this) && this.registeredListener.unregister();
    }
}
