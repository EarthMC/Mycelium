package net.earthmc.mycelium.api.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import net.earthmc.mycelium.api.serialization.JsonCodec;

/**
 * An incoming message on a channel.
 *
 * @param <T> Class extending JsonSerializable.
 */
public interface IncomingMessage<T> {
    /**
     *
     * @return
     */
    T data();

    /**
     *
     * @return
     */
    boolean acceptsResponses();

    <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(N data);

    <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(JsonCodec<N> codec, N data);

    <N> OutgoingMessageBuilder<CallbackCompletable<Boolean>, N> buildResponse(N data);

    <N> OutgoingMessageBuilder<CallbackCompletable<Boolean>, N> buildResponse(JsonCodec<N> codec, N data);
}
