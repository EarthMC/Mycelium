package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.concurrent.CompletableFuture;

/**
 * An incoming message on a channel.
 *
 * @param <T> The type of the incoming data.
 */
public interface IncomingMessage<T> {
    /**
     * {@return the data associated with this request}
     */
    T data();

    /**
     * {@return whether this incoming message accepts replies via the buildResponse family of methods}
     */
    boolean acceptsResponses();

    /**
     * {@return the sender of this message}
     */
    MessageSender sender();

    /**
     * Creates a new outgoing message builder that will synchronously return whether this message was successfully delivered to any listeners.
     *
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized.
     * @throws IllegalStateException if {@link #acceptsResponses()} is false.
     * @param <N> The type of the data to send in response, must be trivially serializable to not throw, use {@link #buildSyncResponse(JsonCodec, Object)} otherwise.
     */
    <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(N data);

    /**
     * Creates a new outgoing message builder that will synchronously return whether this message was successfully delivered to any listeners.
     *
     * @param codec The codec to use for serializing this data.
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized with the given codec.
     * @throws IllegalStateException if {@link #acceptsResponses()} is false.
     * @param <N> The type of the data to send in response.
     */
    <N> OutgoingMessageBuilder<Boolean, N> buildSyncResponse(JsonCodec<N> codec, N data);

    /**
     * Creates a new outgoing message builder that will asynchronously return whether this message was successfully delivered to any listeners.
     *
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized.
     * @throws IllegalStateException if {@link #acceptsResponses()} is false.
     * @param <N> The type of the data to send in response, must be trivially serializable to not throw, use {@link #buildSyncResponse(JsonCodec, Object)} otherwise.
     */
    <N> OutgoingMessageBuilder<CompletableFuture<Boolean>, N> buildResponse(N data);

    /**
     * Creates a new outgoing message builder that will asynchronously return whether this message was successfully delivered to any listeners.
     *
     * @param codec The codec to use for serializing this data.
     * @param data The data to send inside this response.
     * @return A new response builder.
     * @throws IllegalArgumentException if the provided data could not be serialized with the given codec.
     * @throws IllegalStateException if {@link #acceptsResponses()} is false.
     * @param <N> The type of the data to send in response.
     */
    <N> OutgoingMessageBuilder<CompletableFuture<Boolean>, N> buildResponse(JsonCodec<N> codec, N data);
}
