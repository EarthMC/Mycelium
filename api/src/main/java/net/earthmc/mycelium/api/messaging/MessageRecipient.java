package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonSerializable;

public interface MessageRecipient {
    // CallbackCompletable<MessageResponse<String>> sendAndAwait(ChannelIdentifier identifier, String data);

    // <T extends JsonSerializable<T>> CallbackCompletable<MessageResponse<T>> sendAndAwait(ChannelIdentifier.Bound<T> identifier, T data);

    void sendMessage(ChannelIdentifier identifier, String data);

    <T extends JsonSerializable<T>> void sendMessage(ChannelIdentifier.Bound<T> identifier, T data);
}
