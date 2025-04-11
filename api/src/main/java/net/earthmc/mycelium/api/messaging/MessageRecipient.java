package net.earthmc.mycelium.api.messaging;

import java.util.concurrent.CompletableFuture;

public interface MessageRecipient {
    <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier identifier, T data);

    <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data);
}
