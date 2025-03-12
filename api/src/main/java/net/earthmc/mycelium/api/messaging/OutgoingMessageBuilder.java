package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface OutgoingMessageBuilder<R, T> {
    /**
     * Sets the channel to listen for any potential replies to. By default, responses will not expect replies.
     *
     * @param identifier The channel, or {@code null} to indicate no reply is expected.
     * @return {@code this}
     *
     * @see MessagingRegistrar#registerIncomingChannel(ChannelIdentifier.Bound, Consumer)
     */
    OutgoingMessageBuilder<R, T> replyChannel(@Nullable ChannelIdentifier identifier);

    // TODO: ability to run code when the timeout is passed.
    /**
     * Attaches a callback to this reply. Callbacks are single-use and have a required maximum lifetime.
     *
     * @param codec
     * @param expireTime
     * @param expireUnit
     * @param consumer
     * @return
     * @param <N>
     */
    <N> OutgoingMessageBuilder<R, T> callback(JsonCodec<N> codec, long expireTime, TimeUnit expireUnit, Consumer<IncomingMessage<N>> consumer);

    /**
     *
     * @param expireTime
     * @param expireUnit
     * @param consumer
     * @return
     */
    OutgoingMessageBuilder<R, T> callback(long expireTime, TimeUnit expireUnit, Consumer<IncomingMessage<T>> consumer);

    R send();
}
