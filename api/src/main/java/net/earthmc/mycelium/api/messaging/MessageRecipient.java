package net.earthmc.mycelium.api.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;

public interface MessageRecipient {
    <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier identifier, T data);

    <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data);
}
