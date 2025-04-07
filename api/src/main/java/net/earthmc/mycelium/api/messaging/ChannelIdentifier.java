package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.Objects;
import java.util.function.Consumer;

public class ChannelIdentifier {
    public static final String REDIS_CHANNEL_PREFIX = "m:messaging:";

    private final String channel;

    private ChannelIdentifier(final String channel) {
        this.channel = channel;
    }

    public static ChannelIdentifier identifier(final String channel) {
        return new ChannelIdentifier(REDIS_CHANNEL_PREFIX + channel);
    }

    public static ChannelIdentifier absolute(final String channel) {
        return new ChannelIdentifier(channel);
    }

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

    public abstract class Bound<T> extends ChannelIdentifier {
        protected Bound(String channel) {
            super(channel);
        }

        public abstract JsonCodec<T> codec();

        /**
         * Utility method to more easily register this channel.
         *
         * @param receiver A consumer for incoming messages on this channel.
         *
         * @see MessagingRegistrar#registerIncomingChannel(Bound, Consumer)
         */
        public abstract void registerChannel(Consumer<IncomingMessage<T>> receiver);
    }
}
