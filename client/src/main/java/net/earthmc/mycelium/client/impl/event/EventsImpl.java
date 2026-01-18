package net.earthmc.mycelium.client.impl.event;

import net.earthmc.mycelium.api.event.Event;
import net.earthmc.mycelium.api.event.Events;
import net.earthmc.mycelium.api.event.RegisteredEvent;
import net.earthmc.mycelium.api.event.player.PlayerJoinedServerEvent;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.impl.messaging.MessagingRegistrarImpl;
import net.earthmc.mycelium.client.impl.messaging.RegisteredListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventsImpl implements Events {
    private final Map<Class<?>, List<RegisteredEventImpl<?>>> eventListeners = new ConcurrentHashMap<>();
    private final Map<Class<?>, JsonCodec<?>> registeredEvents = new ConcurrentHashMap<>();
    private final Map<Class<?>, Class<?>> implToInterface = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();
    private final MessagingRegistrarImpl messagingRegistrar;

    public EventsImpl(MessagingRegistrarImpl messagingRegistrar) {
        this.messagingRegistrar = messagingRegistrar;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> RegisteredEvent registerEvent(Class<T> eventClass, Consumer<T> listener) {
        final JsonCodec<T> codec = (JsonCodec<T>) registeredEvents.get(eventClass);
        if (codec == null) {
            throw new IllegalArgumentException("Event class '" + eventClass.getName() + "' is not a registered event.");
        }

        final ChannelIdentifier.Bound<T> bound = messagingRegistrar.bind(ChannelIdentifier.identifier("events:" + eventClass.getName()), codec);
        final RegisteredListener<T> registeredListener = (RegisteredListener<T>) messagingRegistrar.registerChannel(bound, incoming -> listener.accept(incoming.data()));

        final RegisteredEventImpl<T> registered = new RegisteredEventImpl<>(eventClass, this, codec, listener, registeredListener);

        synchronized (this.writeLock) {
            this.eventListeners.computeIfAbsent(eventClass, k -> Collections.synchronizedList(new ArrayList<>())).add(registered);
        }

        return registered;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void broadcast(Event event) {
        final Class<?> eventClass = implToInterface.get(event.getClass());
        if (eventClass == null) {
            throw new IllegalArgumentException("Event class '" + event.getClass().getName() + "' is not a registered event.");
        }

        // first execute all listeners for this event on this instance
        final List<RegisteredEventImpl<?>> listeners = this.eventListeners.getOrDefault(event.getClass(), List.of());

        for (final RegisteredEventImpl<?> registeredEvent : listeners) {
            registeredEvent.onEventReceived(event);
        }

        final JsonCodec<Event> codec = (JsonCodec<Event>) registeredEvents.get(eventClass);

        messagingRegistrar.message(messagingRegistrar.bind(ChannelIdentifier.identifier("events:" + eventClass.getName()), codec), event).send();
    }

    public boolean unregister(final RegisteredEventImpl<?> registeredEvent) {
        synchronized (this.writeLock) {
            final List<RegisteredEventImpl<?>> registeredEvents = this.eventListeners.get(registeredEvent.eventClass());
            if (registeredEvents == null) {
                return false;
            }

            final boolean successful = registeredEvents.remove(registeredEvent);
            if (registeredEvents.isEmpty()) {
                this.eventListeners.remove(registeredEvent.eventClass());
            }

            return successful;
        }
    }

    public void shutdown() {
        synchronized (this.writeLock) {
            this.eventListeners.clear();
        }
    }

    public void registerEventClass(final Class<?> interfaceClass, final Class<?> implementationClass, final JsonCodec<?> codec) {
        registeredEvents.put(PlayerJoinedServerEvent.class, codec);
        implToInterface.put(implementationClass, interfaceClass);
    }

    {
        registerEventClass(PlayerJoinedServerEvent.class, net.earthmc.mycelium.client.impl.event.type.player.PlayerJoinedServerEvent.class, net.earthmc.mycelium.client.impl.event.type.player.PlayerJoinedServerEvent.jsonCodec());
    }
}
