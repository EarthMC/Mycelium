package net.earthmc.mycelium.api.messaging;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.function.Consumer;

/**
 * The main interface for registering listeners to specific channels.
 */
public interface MessagingRegistrar {

    /**
     * Binds a channel identifier to the given codec.
     *
     * @param identifier The channel identifier to bind.
     * @param codec The codec to bind to.
     * @return A new {@link ChannelIdentifier.Bound} instance.
     * @param <T> The type of the data being sent.
     */
    <T> ChannelIdentifier.Bound<T> bind(ChannelIdentifier identifier, JsonCodec<T> codec);

    /**
     * Registers a callback for a bound channel.
     *
     * @param identifier The bound channel identifier.
     * @param receiver A consumer for incoming messages on this channel.
     * @param <T> The type of the data being sent.
     */
    <T> void registerIncomingChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver);

    <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data);

    /**
     * Unregisters all listeners for the given channel.
     *
     * @param identifiers The channel identifiers to unregister.
     */
    void unregisterIncomingChannels(ChannelIdentifier... identifiers);
}
