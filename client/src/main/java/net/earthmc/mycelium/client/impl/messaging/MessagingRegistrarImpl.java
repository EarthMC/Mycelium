package net.earthmc.mycelium.client.impl.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import com.google.gson.JsonSyntaxException;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MessagingRegistrarImpl implements MessagingRegistrar {

    private final Logger logger = LoggerFactory.getLogger(MessagingRegistrarImpl.class);

    private final Map<String, ChannelIdentifier> identifierMap = new ConcurrentHashMap<>();
    private final Map<ChannelIdentifier, Consumer<IncomingMessage<?>>> listeners = new ConcurrentHashMap<>();

    private final MyceliumClient client;
    private final StatefulRedisPubSubConnection<String, InternalMessage> connection;

    public MessagingRegistrarImpl(MyceliumClient client) {
        this.client = client;
        this.connection = client.client().connectPubSub(InternalMessage.REDIS_CODEC);

        connection.addListener(new RedisPubSubAdapter<>() {
            @SuppressWarnings("unchecked")
            @Override
            public void message(String channel, InternalMessage message) {
                if (message.source.equals(client.clientId())) {
                    return;
                }

                final ChannelIdentifier identifier = identifierMap.get(channel);
                if (identifier == null) {
                    return;
                }

                final Consumer<IncomingMessage<?>> consumer = listeners.get(identifier);
                if (consumer == null) {
                    return;
                }

                if (identifier instanceof BoundChannelIdentifier<?> bound) {
                    try {
                        final Object deserialized = bound.gson().fromJson(message.payload, bound.codec().typeClass());

                        consumer.accept(new IncomingMessageImpl<>(client, message, (JsonCodec<? super Object>) bound.codec(), deserialized));
                    } catch (JsonSyntaxException e) {
                        logger.warn("Exception occurred while receiving message on channel {}, payload: {}", channel, message);
                    }
                } else {
                    consumer.accept(new IncomingMessageImpl<>(client, message, null, message.payload));
                }
            }
        });
    }

    @Override
    public void unregisterIncomingChannels(ChannelIdentifier... identifiers) {
        for (final ChannelIdentifier identifier : identifiers) {
            if (listeners.remove(identifier) != null) {
                this.connection.async().unsubscribe(identifier.channel());
            }
        }
    }

    @Override
    public <T> ChannelIdentifier.Bound<T> bind(ChannelIdentifier identifier, JsonCodec<T> codec) {
        return new BoundChannelIdentifier<>(identifier.channel(), this, codec);
    }

    @Override
    public <T> void registerIncomingChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver) {
        if (listeners.containsKey(identifier)) {
            return;
        }

        listeners.put(identifier, convert(receiver));
        identifierMap.put(identifier.channel(), identifier);

        this.connection.async().subscribe(identifier.channel());
    }

    @Override
    public <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, newMessageReference(), identifier.channel(), true, data, identifier.codec());
    }

    public <T> OutgoingMessageBuilder<Boolean, T> messageSync(ChannelIdentifier.Bound<T> identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, newMessageReference(), identifier.channel(), false, data, identifier.codec());
    }

    @SuppressWarnings("unchecked")
    private static <T> Consumer<IncomingMessage<?>> convert(Consumer<IncomingMessage<T>> original) {
        return (Consumer<IncomingMessage<?>>) (Consumer<?>) original;
    }

    public String newMessageReference() {
        return UUID.randomUUID().toString();
    }

    public void shutdown() {
        this.connection.close();
    }
}
