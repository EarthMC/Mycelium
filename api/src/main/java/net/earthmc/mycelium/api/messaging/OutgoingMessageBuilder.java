package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;
import java.util.function.Consumer;

@NullMarked
public interface OutgoingMessageBuilder<R, T> {
    /**
     * Sets the channel to listen for any potential replies to. By default, responses will not expect replies.
     *
     * @param identifier The channel, or {@code null} to indicate no reply is expected.
     * @return {@code this}
     *
     * @see MessagingRegistrar#registerChannel(ChannelIdentifier.Bound, Consumer)
     */
    OutgoingMessageBuilder<R, T> replyChannel(@Nullable ChannelIdentifier identifier);

    // TODO: ability to run code when the timeout is passed.
    /**
     * Attaches a callback to this reply. Callbacks are single-use and have a maximum lifetime.
     *
     * @param duration The time that this callback will be valid for.
     * @param codec The codec to use when deserializing incoming messages.
     * @param consumer The handler for the resulting data.
     * @return {@code this}
     * @param <N> The data type that you expect replies in.
     */
    <N> OutgoingMessageBuilder<R, T> callback(TemporalAmount duration, JsonCodec<N> codec, Consumer<IncomingMessage<N>> consumer);

    /**
     * Attaches a callback to this reply. Callbacks are single-use and have a maximum lifetime.
     * <p>
     * Callbacks are valid for 15 minutes by default, which is plenty of time for message handlers to return a response.
     *
     * @param codec The codec to use when deserializing incoming messages.
     * @param consumer The handler for the resulting data.
     * @return {@code this}
     * @param <N> The data type that you expect replies in.
     */
    <N> OutgoingMessageBuilder<R, T> callback(JsonCodec<N> codec, Consumer<IncomingMessage<N>> consumer);

    /**
     * Attaches a callback to this reply. Callbacks are single-use and have a maximum lifetime.
     * <p>
     * Take care that this method expects the response data to be serialized the same way as the request data. If a different data type
     * is expected, use {@link #callback(JsonCodec, Consumer)}
     *
     * @param duration The time that this callback will be valid for.
     * @param consumer The handler for the resulting data.
     * @return {@code this}
     */
    OutgoingMessageBuilder<R, T> callback(TemporalAmount duration, Consumer<IncomingMessage<T>> consumer);

    /**
     * Attaches a callback to this reply. Callbacks are single-use and have a maximum lifetime.
     * <p>
     * Callbacks are valid for 15 minutes by default, which is plenty of time for message handlers to return a response.
     * <p>
     * Take care that this method expects the response data to be serialized the same way as the request data. If a different data type
     * is expected, use {@link #callback(JsonCodec, Consumer)}
     *
     * @param consumer The handler for the resulting data.
     * @return {@code this}
     */
    OutgoingMessageBuilder<R, T> callback(Consumer<IncomingMessage<T>> consumer);

    /**
     * Clears any previously set callback.
     *
     * @return {@code this}
     */
    OutgoingMessageBuilder<R, T> clearCallback();

    /**
     * Sends this message to the receiver.
     *
     * @return A value indicating success.
     */
    R send();
}
