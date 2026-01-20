package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an identifier for a channel.
 */
public class ChannelIdentifier {
    private final String channel;

    private ChannelIdentifier(final String channel) {
        this.channel = channel;
    }

    /**
     * {@return a new instance for the given channel}
     * @param channel The channel.
     */
    public static ChannelIdentifier identifier(final String channel) {
        return new ChannelIdentifier(channel);
    }

    /**
     * {@return the channel identifier}
     */
    public String channel() {
        return this.channel;
    }

    @Override
    public String toString() {
        return this.channel();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChannelIdentifier that = (ChannelIdentifier) o;
        return Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(channel);
    }

    /**
     * A bound channel identifier wraps a JSON codec to be able to send and receive more complex objects across the wire.
     *
     * @param <T> The type that is intended to be sent across this channel.
     */
    public abstract class Bound<T> extends ChannelIdentifier {
        /**
         * Constructs a new bound channel identifier.
         * @param channel The channel.
         */
        protected Bound(String channel) {
            super(channel);
        }

        /**
         * {@return the json codec that will be used for serializing data}
         */
        public abstract JsonCodec<T> codec();

        /**
         * Utility method to more easily register this channel.
         *
         * @param receiver A consumer for incoming messages on this channel.
         * @return A listener instance for use in un-registration.
         * @see MessagingRegistrar#registerPlatformChannel(Bound, Consumer)
         */
        public abstract Listener registerPlatform(Consumer<IncomingMessage<T>> receiver);

        /**
         * Utility method to more easily register this channel.
         *
         * @param receiver A consumer for incoming messages on this channel.
         * @return A listener instance for use in un-registration.
         * @see MessagingRegistrar#registerChannel(Bound, Consumer)
         */
        public abstract Listener register(Consumer<IncomingMessage<T>> receiver);
    }
}
