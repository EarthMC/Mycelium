package net.earthmc.mycelium.client.impl.messaging;

import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;

import java.util.concurrent.CompletableFuture;

public class IncomingMessageImpl<T> implements IncomingMessage<T> {
    private final MyceliumClient client;
    private final InternalMessage internalMessage;
    private final JsonCodec<T> codec;
    private final T deserializedData;

    public IncomingMessageImpl(MyceliumClient client, InternalMessage internal, JsonCodec<T> codec, T data) {
        this.client = client;
        this.internalMessage = internal;
        this.codec = codec;
        this.deserializedData = data;
    }

    @Override
    public T data() {
        return this.deserializedData;
    }

    @Override
    public boolean acceptsResponses() {
        return internalMessage.replyTo != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(N data) {
        checkCanRespond();

        final Class<N> newDataClass = (Class<N>) data.getClass();
        JsonCodec<N> newDataCodec = null;
        if (newDataClass.equals(this.codec.type())) {
            newDataCodec = (JsonCodec<N>) this.codec;
        }

        return new OutgoingMessageBuilderImpl<>(this.client, internalMessage.messageReference, internalMessage.replyTo, false, data, newDataCodec);
    }

    @Override
    public <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(JsonCodec<N> codec, N data) {
        checkCanRespond();
        return new OutgoingMessageBuilderImpl<>(this.client, internalMessage.messageReference, internalMessage.replyTo, false, data, codec);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N> OutgoingMessageBuilder<CompletableFuture<Boolean>, N> buildResponse(N data) {
        checkCanRespond();

        final Class<N> newDataClass = (Class<N>) data.getClass();
        JsonCodec<N> newDataCodec = null;
        if (newDataClass.equals(this.codec.type())) {
            newDataCodec = (JsonCodec<N>) this.codec;
        }

        return new OutgoingMessageBuilderImpl<>(this.client, internalMessage.messageReference, internalMessage.replyTo, true, data, newDataCodec);
    }

    @Override
    public <N> OutgoingMessageBuilder<CompletableFuture<Boolean>, N> buildResponse(JsonCodec<N> codec, N data) {
        checkCanRespond();
        return new OutgoingMessageBuilderImpl<>(this.client, internalMessage.messageReference, internalMessage.replyTo, true, data, codec);
    }

    private void checkCanRespond() {
        if (internalMessage.replyTo == null) {
            throw new IllegalStateException("This message is not accepting responses.");
        }
    }
}
