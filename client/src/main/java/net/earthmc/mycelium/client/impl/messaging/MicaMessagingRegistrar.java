package net.earthmc.mycelium.client.impl.messaging;

import com.google.gson.JsonSyntaxException;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.serialization.JsonSerializable;
import net.earthmc.mycelium.client.MyceliumClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MicaMessagingRegistrar implements MessagingRegistrar {
    private final Logger logger = LoggerFactory.getLogger(MicaMessagingRegistrar.class);

    private final Map<String, ChannelIdentifier> identifierMap = new ConcurrentHashMap<>();
    private final Map<ChannelIdentifier, Consumer<IncomingMessage<?>>> listeners = new ConcurrentHashMap<>();

    private final MyceliumClient client;
    private final StatefulRedisPubSubConnection<String, String> connection;

    public MicaMessagingRegistrar(MyceliumClient client) {
        this.client = client;
        this.connection = client.client().connectPubSub();

        connection.addListener(new RedisPubSubAdapter<>() {
            @SuppressWarnings("unchecked")
            @Override
            public void message(String channel, String message) {
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
                        final Object deserialized = bound.gson().fromJson(message, bound.codec().typeClass());
                        final Consumer<IncomingMessage<Object>> objectConsumer = (Consumer<IncomingMessage<Object>>) consumer;

                        objectConsumer.accept(deserialized);
                    } catch (JsonSyntaxException e) {
                        logger.warn("Exception occurred while receiving message on channel {}, payload: {}", channel, message);
                    }
                } else {
                    ((Consumer<IncomingMessage<?>>) consumer).accept(message);
                }
            }
        });
    }

    @Override
    public void registerIncomingChannel(ChannelIdentifier identifier, Consumer<IncomingMessage<String>> receiver) {
        if (listeners.containsKey(identifier)) {
            return;
        }

        listeners.put(identifier, receiver);

        registerRedisChannel(identifier);
        this.connection.async().subscribe("mycelium:messaging:" + identifier.channel());
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
    public <T extends JsonSerializable<T>> ChannelIdentifier.Bound<T> bind(ChannelIdentifier identifier, JsonCodec<T> codec) {
        return new BoundChannelIdentifier<>(identifier.channel(), this, codec);
    }

    @Override
    public <T extends JsonSerializable<T>> void registerBoundChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver) {
        if (listeners.containsKey(identifier)) {
            return;
        }

        listeners.put(identifier, receiver);

        registerRedisChannel(identifier);
        this.connection.async().subscribe("mycelium:messaging:" + identifier.channel());
    }

    private void registerRedisChannel(ChannelIdentifier identifier) {
        identifierMap.put("mycelium:messaging:" + identifier.channel(), identifier);
    }

    public void shutdown() {
        this.connection.close();
    }
}
