package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.serialization.JsonSerializable;

import java.util.function.Consumer;

public interface MessagingRegistrar { // TODO: rename?
    /**
     * Registers a callback for the given channel.
     *
     * @param identifier The identifier for this channel.
     * @param receiver A consumer for incoming messages on this channel.
     */
    void registerIncomingChannel(ChannelIdentifier identifier, Consumer<String> receiver);

    /**
     * Unregisters all listeners for the given channel.
     *
     * @param identifiers The channel identifiers to unregister.
     */
    void unregisterIncomingChannels(ChannelIdentifier... identifiers);

    /**
     * Binds a channel identifier to the given codec.
     *
     * @param identifier The channel identifier to bind to.
     * @param codec
     * @return A new {@link ChannelIdentifier.Bound} instance.
     * @param <T> A class extending {@link JsonSerializable}
     */
    <T extends JsonSerializable<T>> ChannelIdentifier.Bound<T> bind(ChannelIdentifier identifier, JsonCodec<T> codec);

    /**
     *
     * @param identifier
     * @param receiver A consumer for incoming messages on this channel.
     * @param <T> A class extending {@link JsonSerializable}
     */
    <T extends JsonSerializable<T>> void registerBoundChannel(ChannelIdentifier.Bound<T> identifier, Consumer<T> receiver);
}
