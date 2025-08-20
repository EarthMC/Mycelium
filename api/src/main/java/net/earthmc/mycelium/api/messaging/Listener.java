package net.earthmc.mycelium.api.messaging;

/**
 * Represents a registered listener for a given channel.
 */
public interface Listener {
    /**
     * {@return whether this listener has been successfully unregistered}
     */
    boolean unregister();

    /**
     * {@return the identifier for the channel this listener is for}
     */
    ChannelIdentifier identifier();
}
