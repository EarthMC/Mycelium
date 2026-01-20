package net.earthmc.mycelium.client.impl.messaging.callback;

import com.google.gson.JsonParseException;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.MessageSender;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.messaging.IncomingMessageImpl;
import net.earthmc.mycelium.client.impl.messaging.InternalMessage;
import net.earthmc.mycelium.client.impl.messaging.MessageSenderImpl;
import net.earthmc.mycelium.client.impl.serialization.GsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

import java.io.Closeable;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 *
 */
public class CallbackProvider implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackProvider.class);

    private final Map<String, Callback<?>> callbacks = new ConcurrentHashMap<>();
    private final MyceliumClient client;

    private final ScheduledExecutorService cleanupThread = Executors.newSingleThreadScheduledExecutor(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setName("Mycelium Callback Cleaner");
        return thread;
    });

    private final ExecutorService pollThread = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setName("Mycelium Callback Polling");
        return thread;
    });

    private final String channel;
    private final JedisPubSub listener;

    public CallbackProvider(MyceliumClient client) {
        this.client = client;
        this.channel = "m:" + client.network().id() + ":clients:" + client.clientId() + ":callback";

        this.cleanupThread.scheduleAtFixedRate(this::cleanupExpiredCallbacks, 10L, 10L, TimeUnit.SECONDS);

        this.listener = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                final InternalMessage internalMessage = InternalMessage.REDIS_CODEC.deserialize(message);

                final Callback<?> callback = callbacks.remove(internalMessage.messageReference);
                if (callback == null || callback.expiration.isBefore(Instant.now())) {
                    return;
                }

                final MessageSender sender = new MessageSenderImpl(internalMessage.source.equals(client.clientId()));

                try {
                    callback.handle(client, internalMessage, sender);
                } catch (Throwable throwable) {
                    LOGGER.error("An exception occurred while handling callback with id {}", internalMessage.messageReference, throwable);
                }
            }
        };

        this.pollThread.submit(() -> this.client.redis().subscribe(this.listener, this.channel));
    }

    public <T> void await(String messageUUID, JsonCodec<T> codec, CallbackOptions options, Consumer<IncomingMessage<T>> callback) {
        final Instant expireInstant = Instant.now().plus(options.lifetime());

        callbacks.put(messageUUID, new Callback<>(expireInstant, options, codec, callback));
    }

    /**
     * @return The channel we are listening for callbacks on.
     */
    public String channel() {
        return this.channel;
    }

    @Override
    public void close() {
        this.listener.unsubscribe();

        this.cleanupThread.shutdown();
        this.pollThread.shutdown();;
    }

    private void cleanupExpiredCallbacks() {
        final Instant now = Instant.now();

        final Iterator<Callback<?>> iterator = this.callbacks.values().iterator();
        while (iterator.hasNext()) {
            final Callback<?> callback = iterator.next();

            final boolean remove = callback == null || callback.expiration.isBefore(now);
            if (remove && callback != null && callback.options.onExpire() != null) {
                callback.options.onExpire().run();
            }

            if (remove) {
                iterator.remove();
            }
        }
    }

    private record Callback<T>(Instant expiration, CallbackOptions options, JsonCodec<T> codec, Consumer<IncomingMessage<T>> consumer) {
        void handle(MyceliumClient client, InternalMessage message, MessageSender sender) {
            T deserialized;
            try {
                deserialized = GsonHelper.forCodec(this.codec).fromJson(message.payload, this.codec.type());
            } catch (JsonParseException e) {
                LOGGER.error("Failed to deserialize callback with message reference {} (using codec {})", message.messageReference, this.codec, e);
                return;
            }

            consumer.accept(new IncomingMessageImpl<>(client, message, sender, this.codec, deserialized));
        }
    }
}
