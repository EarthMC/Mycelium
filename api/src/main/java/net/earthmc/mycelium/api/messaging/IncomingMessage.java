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
     * @return The data associated with this request.
     */
    T data();

    /**
     * @return Whether this incoming message accepts replies via the {@link #buildResponse(Object)} family of methods.
     */
    boolean acceptsResponses();

    /**
     * Creates a new outgoing message builder that will synchronously return whether this message was successfully delivered to any listeners.
     *
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized.
     */
    <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(N data);

    /**
     * Creates a new outgoing message builder that will synchronously return whether this message was successfully delivered to any listeners.
     *
     * @param codec The codec to use for serializing this data.
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized with the given codec.
     */
    <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(JsonCodec<N> codec, N data);

    /**
     * Creates a new outgoing message builder that will asynchronously return whether this message was successfully delivered to any listeners.
     *
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized.
     */
    <N> OutgoingMessageBuilder<CallbackCompletable<Boolean>, N> buildResponse(N data);

    /**
     * Creates a new outgoing message builder that will asynchronously return whether this message was successfully delivered to any listeners.
     *
     * @param codec The codec to use for serializing this data.
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized with the given codec.
     */
    <N> OutgoingMessageBuilder<CallbackCompletable<Boolean>, N> buildResponse(JsonCodec<N> codec, N data);
}
