package net.earthmc.mycelium.api.messaging;

import net.earthmc.mycelium.api.serialization.JsonSerializable;

import java.util.Objects;
import java.util.function.Consumer;

public class ChannelIdentifier {
    private static final String DEFAULT_NAMESPACE = "mycelium";

    private final String channel;

    private ChannelIdentifier(final String channel) {
        this.channel = channel;
    }

    public static ChannelIdentifier identifier(final String channel) {
        return new ChannelIdentifier(channel);
    }

    public static ChannelIdentifier namespaced(final String namespace, final String value) {
        return new ChannelIdentifier(namespace + ":" + value);
    }

    public static ChannelIdentifier defaultNamespace(final String value) {
        return namespaced(DEFAULT_NAMESPACE, value);
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

        /**
         * Utility method to more easily register this channel.
         *
         * @param receiver A consumer for incoming messages on this channel.
         *
         * @see MessagingRegistrar#registerBoundChannel(Bound, Consumer)
         */
        public abstract void register(Consumer<T> receiver);
    }
}
