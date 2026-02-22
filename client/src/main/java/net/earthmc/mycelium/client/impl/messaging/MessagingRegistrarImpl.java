package net.earthmc.mycelium.client.impl.messaging;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.Listener;
import net.earthmc.mycelium.api.messaging.MessageSender;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.platform.PlatformType;
import net.earthmc.mycelium.client.AbstractPlatform;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MessagingRegistrarImpl implements MessagingRegistrar {

    private final Logger logger = LoggerFactory.getLogger(MessagingRegistrarImpl.class);

    private final Map<String, List<RegisteredListener<?>>> listeners = new ConcurrentHashMap<>();

    private final MyceliumClient client;
    private final JedisPubSub listener;

    private final ExecutorService pollThread = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setName("Mycelium PubSub Polling");
        return thread;
    });

    public MessagingRegistrarImpl(MyceliumClient client) {
        this.client = client;

        this.listener = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                final InternalMessage internalMessage = InternalMessage.REDIS_CODEC.deserialize(message);

                final List<RegisteredListener<?>> registeredListeners = listeners.get(channel);
                if (registeredListeners == null) {
                    return;
                }

                final MessageSender sender = new MessageSenderImpl(internalMessage.source.equals(client.clientId()));

                for (final RegisteredListener<?> listener : registeredListeners) {
                    try {
                        listener.processIncoming(client, internalMessage, sender);
                    } catch (Throwable throwable) {
                        logger.warn("Exception occurred while receiving message on channel {}, payload: {}", channel, message, throwable);
                    }
                }
            }
        };

        this.pollThread.submit(() -> this.client.redis().psubscribe(this.listener, "m:*"));
    }

    @Override
    public boolean unregisterIncomingChannels(ChannelIdentifier... identifiers) {
        boolean removed = false;

        for (final ChannelIdentifier identifier : identifiers) {
            final String canonicalChannel = makePlatformKey(identifier.channel());
            final String globalChannel = makeGlobalKey(identifier.channel());

            if (listeners.remove(canonicalChannel) != null) {
                removed = true;
                this.listener.unsubscribe(canonicalChannel);
            } else if (listeners.remove(globalChannel) != null) {
                removed = true;
                this.listener.unsubscribe(globalChannel);
            }
        }

        return removed;
    }

    public String globalChannelPrefix() {
        return RedisKey.create(this.client.network().id(), "channels");
    }

    public String makeGlobalKey(String channel) {
        return globalChannelPrefix() + ":" + channel;
    }

    public String makePlatformKey(String channel) {
        return this.client.platform().key("channels:" + channel);
    }

    @Override
    public <T> ChannelIdentifier.Bound<T> bind(ChannelIdentifier identifier, JsonCodec<T> codec) {
        return new BoundChannelIdentifier<>(identifier.channel(), this, codec);
    }

    @Override
    public <T> Listener registerPlatformChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver) {
        final AbstractPlatform platform = this.client.platform();

        if (platform.type() == PlatformType.STANDALONE) {
            throw new IllegalStateException("Cannot register a platform-relative channel on a standalone platform.");
        }

        return registerInternal(makePlatformKey(identifier.channel()), identifier, receiver);
    }

    @Override
    public <T> Listener registerChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver) {
        return registerInternal(makeGlobalKey(identifier.channel()), identifier, receiver);
    }

    private <T> Listener registerInternal(String fullChannel, ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver) {
        final RegisteredListener<T> listener = new RegisteredListener<>(this, (BoundChannelIdentifier<T>) identifier, fullChannel, receiver);

        // Synchronize on listeners as a write lock
        synchronized (this.listeners) {
            listeners.computeIfAbsent(fullChannel, k -> new ArrayList<>()).add(listener);
        }

        this.listener.subscribe(fullChannel);
        return listener;
    }

    @Override
    public <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, newMessageReference(), makeGlobalKey(identifier.channel()), true, data, identifier.codec());
    }

    public String newMessageReference() {
        return UUID.randomUUID().toString();
    }

    public boolean unregisterListener(RegisteredListener<?> listener) {
        synchronized (this.listeners) {
            final List<RegisteredListener<?>> channelListeners = this.listeners.get(listener.canonicalChannel());
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
        this.listener.unsubscribe();
        this.pollThread.shutdown();
    }
}
