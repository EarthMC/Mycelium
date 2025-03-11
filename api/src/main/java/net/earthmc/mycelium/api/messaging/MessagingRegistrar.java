package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.serialization.JsonSerializable;

import java.util.function.Consumer;

/**
 * The main interface for registering listeners to specific channels.
 */
public interface MessagingRegistrar { // TODO: rename?
    /**
     * Registers a callback for the given channel.
     *
     * @param identifier The identifier for this channel.
     * @param receiver A consumer for incoming messages on this channel.
     */
    void registerIncomingChannel(ChannelIdentifier identifier, Consumer<IncomingMessage<String>> receiver);

    /**
     * Unregisters all listeners for the given channel.
     *
     * @param identifiers The channel identifiers to unregister.
     */
    void unregisterIncomingChannels(ChannelIdentifier... identifiers);

    /**
     * Binds a channel identifier to the given codec.
     *
     * @param identifier The channel identifier to bind.
     * @param codec The codec to bind to.
     * @return A new {@link ChannelIdentifier.Bound} instance.
     * @param <T> A class extending {@link JsonSerializable}
     */
    <T extends JsonSerializable<T>> ChannelIdentifier.Bound<T> bind(ChannelIdentifier identifier, JsonCodec<T> codec);

    /**
     * Registers a callback for a bound channel.
     *
     * @param identifier The bound channel identifier.
     * @param receiver A consumer for incoming messages on this channel.
     * @param <T> A class extending {@link JsonSerializable}
     */
    <T extends JsonSerializable<T>> void registerBoundChannel(ChannelIdentifier.Bound<T> identifier, Consumer<IncomingMessage<T>> receiver);
}
