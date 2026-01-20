package net.earthmc.mycelium.api.messaging;

import java.util.concurrent.CompletableFuture;

/**
 * Represents something that can receive messages.
 */
public interface MessageRecipient {
    /**
     * Creates a new outgoing message builder.
     *
     * @param identifier The channel to send the data over.
     * @param data The data to send.
     * @return A new outgoing message builder.
     * @param <T> The type of the data to send in response, must be trivially serializable to not throw, use {@link #message(ChannelIdentifier.Bound, Object)} otherwise.
     */
    <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier identifier, T data);

    /**
     * Creates a new outgoing message builder.
     *
     * @param identifier The channel to send the data over.
     * @param data The data to send.
     * @return A new outgoing message builder.
     * @param <T> The type of the data to send in response.
     */
    <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data);
}
