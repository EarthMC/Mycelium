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
     * <p>
     * The registered channel is platform-relative, which means it will be prefixed with a unique prefix. Data may be sent to this channel
     * using the {@link MessageRecipient} class and using the same channel identifier.
     *
     * @param identifier The bound channel identifier.
     * @param receiver A consumer for incoming messages on this channel.
     * @return A listener instance for use in un-registration.
     * @param <T> The type of the data being sent.
     * @throws IllegalStateException If the platform this is called on is not a proxy or server one.
     */
    <T> Listener registerPlatformChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver);

    /**
     * Registers a callback for a bound channel.
     * <p>
     * Data may be sent through this channel using {@link #message(ChannelIdentifier.Bound, Object)} with the same identifier as used here.
     *
     * @param identifier The bound channel identifier.
     * @param receiver A consumer for incoming messages on this channel.
     * @return A listener instance for use in un-registration.
     * @param <T> The type of the data being sent.
     */
    <T> Listener registerChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver);

    /**
     * Creates a new builder for an outgoing message to the specified channel.
     *
     * @param identifier The channel identifier this message will be sent from.
     * @param data The data to send.
     * @return A new outgoing message builder.
     * @param <T> The type of the data being sent.
     */
    <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data);

    /**
     * Unregisters all listeners for the given channel.
     *
     * @param identifiers The channel identifiers to unregister.
     * @return Whether any listeners were unregistered.
     */
    boolean unregisterIncomingChannels(ChannelIdentifier... identifiers);
}
