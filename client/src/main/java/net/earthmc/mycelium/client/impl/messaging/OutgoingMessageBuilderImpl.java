package net.earthmc.mycelium.client.impl.messaging;

import com.google.gson.JsonParseException;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.serialization.GsonHelper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@NullMarked
public class OutgoingMessageBuilderImpl<R, T> implements OutgoingMessageBuilder<R, T> {
    private static final Duration DEFAULT_CALLBACK_LIFETIME = Duration.ofMinutes(15);

    private final MyceliumClient client;
    private final String messageReference;
    private final String destinationChannel;
    private final boolean async;
    private final String serializedPayload;
    private final @Nullable JsonCodec<T> codec;

    public @Nullable ChannelIdentifier replyChannel;
    public @Nullable Runnable registerCallback;

    // This impl might need a builder itself at some point
    public OutgoingMessageBuilderImpl(final MyceliumClient client, final String messageReference, final String destinationChannel, boolean async, T data, @Nullable JsonCodec<T> codec) {
        this.client = client;
        this.messageReference = messageReference;
        this.destinationChannel = destinationChannel;
        this.async = async;
        this.codec = codec;
        this.serializedPayload = createPayload(data, codec);
    }

    @Override
    public OutgoingMessageBuilder<R, T> replyChannel(@Nullable ChannelIdentifier identifier) {
        this.replyChannel = identifier;
        return this;
    }

    @Override
    public <N> OutgoingMessageBuilder<R, T> callback(TemporalAmount duration, JsonCodec<N> codec, Consumer<IncomingMessage<N>> consumer) {
        registerCallback = () -> {
            replyChannel(ChannelIdentifier.identifier(client.callbacks().channel()));
            final Duration d = duration instanceof Duration d1 ? d1 : Duration.from(duration);

            client.callbacks().await(this.messageReference, codec, d.toMillis(), TimeUnit.MILLISECONDS, consumer);
        };

        return this;
    }

    @Override
    public <N> OutgoingMessageBuilder<R, T> callback(JsonCodec<N> codec, Consumer<IncomingMessage<N>> consumer) {
        return callback(DEFAULT_CALLBACK_LIFETIME, codec, consumer);
    }

    @Override
    public OutgoingMessageBuilder<R, T> callback(TemporalAmount duration, Consumer<IncomingMessage<T>> consumer) {
        if (this.codec == null) {
            throw new IllegalArgumentException("A codec is required when listening for a callback.");
        }

        registerCallback = () -> {
            replyChannel(ChannelIdentifier.identifier(client.callbacks().channel()));
            final Duration d = duration instanceof Duration d1 ? d1 : Duration.from(duration);

            client.callbacks().await(this.messageReference, this.codec, d.toMillis(), TimeUnit.MILLISECONDS, consumer);
        };

        return this;
    }

    @Override
    public OutgoingMessageBuilder<R, T> callback(Consumer<IncomingMessage<T>> consumer) {
        return callback(DEFAULT_CALLBACK_LIFETIME, consumer);
    }

    @Override
    public OutgoingMessageBuilder<R, T> clearCallback() {
        this.registerCallback = null;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R send() {
        if (registerCallback != null) {
            registerCallback.run();
        }

        if (async) {
            return (R) createAsyncCallback(createMessageForPayload(this.serializedPayload));
        } else {
            return (R) getSyncResult(createMessageForPayload(this.serializedPayload));
        }
    }

    private String createPayload(T data, @Nullable JsonCodec<T> codec) {
        try {
            if (data instanceof String string) {
                return GsonHelper.DEFAULT_INSTANCE.toJson(string, String.class);
            } else {
                return GsonHelper.forCodec(codec).toJson(data, codec == null ? data.getClass() : codec.type());
            }
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("Failed to serialize provided data for outgoing message (codec: " + codec + ")", e);
        }
    }

    /**
     * Constructs the actual message that will be sent from the given string payload.
     */
    private InternalMessage createMessageForPayload(String payload) {
        String responseAddress = null;

        if (this.replyChannel != null) {
            responseAddress = this.replyChannel.channel();
        }

        return new InternalMessage(responseAddress, client.clientId(), messageReference, payload);
    }

    private Boolean getSyncResult(InternalMessage message) {
        return client.client().publish(destinationChannel, InternalMessage.REDIS_CODEC.serialize(message)) > 0;
    }

    private CompletableFuture<Boolean> createAsyncCallback(InternalMessage message) {
        return CompletableFuture.supplyAsync(() -> getSyncResult(message));
    }
}
