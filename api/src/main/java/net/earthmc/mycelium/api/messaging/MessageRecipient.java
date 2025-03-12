package net.earthmc.mycelium.api.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import net.earthmc.mycelium.api.serialization.JsonCodec;

public interface MessageRecipient {
    <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(T data);

    <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(JsonCodec<T> identifier, T data);
}
