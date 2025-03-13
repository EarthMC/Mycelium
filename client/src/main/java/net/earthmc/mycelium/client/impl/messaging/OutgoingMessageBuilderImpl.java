package net.earthmc.mycelium.client.impl.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.serialization.GsonHelper;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OutgoingMessageBuilderImpl<R, T> implements OutgoingMessageBuilder<R, T> {
    private final MyceliumClient client;
    private final String messageReference;
    private final String destinationChannel;
    private final boolean async;
    private final T data;
    private final JsonCodec<T> codec;

    public ChannelIdentifier replyChannel;
    public Runnable registerCallback;

    // This impl might need a builder itself at some point
    public OutgoingMessageBuilderImpl(final MyceliumClient client, final String messageReference, final String destinationChannel, boolean async, T data, @Nullable JsonCodec<T> codec) {
        this.client = client;
        this.messageReference = messageReference;
        this.destinationChannel = destinationChannel;
        this.async = async;
        this.data = data;
        this.codec = codec;
    }

    @Override
    public OutgoingMessageBuilder<R, T> replyChannel(@Nullable ChannelIdentifier identifier) {
        this.replyChannel = identifier;
        return this;
    }

    @Override
    public <N> OutgoingMessageBuilder<R, T> callback(JsonCodec<N> codec, long expireTime, TimeUnit expireUnit, Consumer<IncomingMessage<N>> consumer) {
        registerCallback = () -> {
            replyChannel(ChannelIdentifier.absolute(client.callbacks().channel()));
            client.callbacks().await(this.messageReference, codec, expireTime, expireUnit, consumer);
        };

        return this;
    }

    @Override
    public OutgoingMessageBuilder<R, T> callback(long expireTime, TimeUnit expireUnit, Consumer<IncomingMessage<T>> consumer) {
        if (this.codec == null) {
            throw new IllegalArgumentException("A codec is required when listening for a callback.");
        }

        registerCallback = () -> {
            replyChannel(ChannelIdentifier.absolute(client.callbacks().channel()));
            client.callbacks().await(this.messageReference, this.codec, expireTime, expireUnit, consumer);
        };

        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R send() {
        if (registerCallback != null) {
            registerCallback.run();
        }

        if (async) {
            return (R) createAsyncCallback(createMessage(this.data));
        } else {
            return (R) getSyncResult(createMessage(this.data));
        }
    }

    /**
     * Converts the given data to a message using this class' already known codec.
     */
    private InternalMessage createMessage(T data) {
        String payload;
        if (data instanceof String string) {
            payload = GsonHelper.DEFAULT_INSTANCE.toJson(string, String.class);
        } else {
            payload = GsonHelper.forCodec(this.codec).toJson(data, this.codec == null ? data.getClass() : this.codec.type());
        }

        return createMessageForPayload(payload);
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
        try (StatefulRedisPubSubConnection<String, InternalMessage> connection = client.client().connectPubSub(InternalMessage.REDIS_CODEC)) {
            return connection.sync().publish(destinationChannel, message) > 0;
        }
    }

    private CallbackCompletable<Boolean> createAsyncCallback(InternalMessage message) {
        final CallbackCompletable<Boolean> callback = new CallbackCompletable<>();
        StatefulRedisPubSubConnection<String, InternalMessage> connection = client.client().connectPubSub(InternalMessage.REDIS_CODEC);

        try {
            connection.async().publish(destinationChannel, message)
                    .thenAccept(count -> callback.complete(count > 0))
                    .exceptionally(exception -> {
                        callback.completeWithThrowable(exception);
                        return null;
                    });
        } catch (Throwable thr) {
            callback.completeWithThrowable(thr);
        }

        callback.addWaiter((bool, ex) -> connection.closeAsync());
        return callback;
    }
}
