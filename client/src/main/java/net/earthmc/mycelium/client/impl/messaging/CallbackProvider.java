package net.earthmc.mycelium.client.impl.messaging;

import com.google.gson.JsonParseException;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.serialization.GsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private final ScheduledExecutorService cleanupPool = Executors.newSingleThreadScheduledExecutor(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setName("Mycelium Callback Cleaner");

        return thread;
    });

    private final StatefulRedisPubSubConnection<String, InternalMessage> connection;
    private final String channel;

    public CallbackProvider(MyceliumClient client) {
        this.client = client;
        this.channel = "m:" + client.network().id() + ":clients:" + client.clientId() + ":callback";

        this.cleanupPool.scheduleAtFixedRate(this::cleanupExpiredCallbacks, 10L, 10L, TimeUnit.SECONDS);

        this.connection = client.client().connectPubSub(InternalMessage.REDIS_CODEC);

        this.connection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, InternalMessage message) {
                if (message.source.equals(client.clientId())) {
                    return;
                }

                final Callback<?> callback = callbacks.remove(message.messageReference);
                if (callback == null) {
                    return;
                }

                try {
                    callback.handle(client, message);
                } catch (Throwable throwable) {
                    LOGGER.error("An exception occurred while handling callback with id {}", message.messageReference, throwable);
                }
            }
        });

        this.connection.async().subscribe(this.channel);
    }

    public <T> void await(String messageUUID, JsonCodec<T> codec, long expireTime, TimeUnit unit, Consumer<IncomingMessage<T>> callback) {
        final Instant expireInstant = Instant.now().plus(expireTime, unit.toChronoUnit());

        callbacks.put(messageUUID, new Callback<>(expireInstant, codec, callback));
    }

    /**
     * @return The channel we are listening for callbacks on.
     */
    public String channel() {
        return this.channel;
    }

    @Override
    public void close() {
        this.cleanupPool.shutdown();
        this.connection.close();
    }

    private void cleanupExpiredCallbacks() {
        final Instant now = Instant.now();

        callbacks.values().removeIf(next -> next.expiration.isBefore(now));
    }

    private record Callback<T>(Instant expiration, JsonCodec<T> codec, Consumer<IncomingMessage<T>> consumer) {
        void handle(MyceliumClient client, InternalMessage message) {
            T deserialized;
            try {
                deserialized = GsonHelper.forCodec(this.codec).fromJson(message.payload, this.codec.type());
            } catch (JsonParseException e) {
                LOGGER.error("Failed to deserialize callback with message reference {} (using codec {})", message.messageReference, this.codec, e);
                return;
            }

            consumer.accept(new IncomingMessageImpl<>(client, message, this.codec, deserialized));
        }
    }
}
