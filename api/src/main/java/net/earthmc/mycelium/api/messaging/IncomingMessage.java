package net.earthmc.mycelium.api.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.serialization.JsonSerializable;

/**
 * An incoming message on a channel.
 *
 * @param <T> Class extending JsonSerializable.
 */
public interface IncomingMessage<T> {
    T data();

    CallbackCompletable<Boolean> respond(T data);

    boolean respondSync(T data);

    /**
     * Respond to this message with new data.
     *
     * @param data The new data to send.
     * @return A completable callback indicating whether the response was successfully received.
     *
     * @param <N> New class to respond with.
     */
    <N extends JsonSerializable<N>> CallbackCompletable<Boolean> respond(JsonCodec<N> codec, N data);

    <N extends JsonSerializable<N>> boolean respondSync(JsonCodec<N> codec, N data);
}
