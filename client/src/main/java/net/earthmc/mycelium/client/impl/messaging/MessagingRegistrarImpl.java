package net.earthmc.mycelium.client.impl.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.Listener;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessagingRegistrarImpl implements MessagingRegistrar {

    private final Logger logger = LoggerFactory.getLogger(MessagingRegistrarImpl.class);

    private final Map<String, List<RegisteredListener<?>>> listeners = new ConcurrentHashMap<>();

    private final MyceliumClient client;
    private final StatefulRedisPubSubConnection<String, InternalMessage> connection;

    public MessagingRegistrarImpl(MyceliumClient client) {
        this.client = client;
        this.connection = client.client().connectPubSub(InternalMessage.REDIS_CODEC);

        connection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, InternalMessage message) {
                if (message.source.equals(client.clientId())) {
                    return;
                }

                final List<RegisteredListener<?>> registeredListeners = listeners.get(channel);
                if (registeredListeners == null) {
                    return;
                }

                for (final RegisteredListener<?> listener : registeredListeners) {
                    try {
                        listener.processIncoming(client, message);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while receiving message on channel {}, payload: {}", channel, throwable);
                    }
                }
            }
        });
    }

    @Override
    public boolean unregisterIncomingChannels(ChannelIdentifier... identifiers) {
        boolean removed = false;

        for (final ChannelIdentifier identifier : identifiers) {
            if (listeners.remove(identifier.channel()) != null) {
                removed = true;
                this.connection.async().unsubscribe(identifier.channel());
            }
        }

        return removed;
    }

    @Override
    public <T> ChannelIdentifier.Bound<T> bind(ChannelIdentifier identifier, JsonCodec<T> codec) {
        return new BoundChannelIdentifier<>(identifier.channel(), this, codec);
    }

    @Override
    public <T> Listener registerIncomingChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver) {
        final RegisteredListener<T> listener = new RegisteredListener<>(this, (BoundChannelIdentifier<T>) identifier, receiver);

        // Synchronize on listeners as a write lock
        synchronized (this.listeners) {
            listeners.computeIfAbsent(identifier.channel(), k -> new ArrayList<>()).add(listener);
        }

        this.connection.async().subscribe(identifier.channel());
        return listener;
    }

    @Override
    public <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, newMessageReference(), identifier.channel(), true, data, identifier.codec());
    }

    public String newMessageReference() {
        return UUID.randomUUID().toString();
    }

    public boolean unregisterListener(RegisteredListener<?> listener) {
        synchronized (this.listeners) {
            final List<RegisteredListener<?>> channelListeners = this.listeners.get(listener.identifier().channel());
            if (channelListeners == null) {
                return false;
            }

            final boolean removed = channelListeners.remove(listener);
            if (channelListeners.isEmpty()) {
                this.listeners.remove(listener.identifier().channel());
            }

            return removed;
        }
    }

    public void shutdown() {
        this.connection.close();
    }
}
