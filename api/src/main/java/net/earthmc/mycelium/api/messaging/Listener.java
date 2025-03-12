package net.earthmc.mycelium.api.messaging;

/**
 * Represents a registered listener for a given channel.
 */
// todo: implement
public interface Listener {
    /**
     * @return {@code true} if this listener has been successfully unregistered. Un-registration may fail if
     */
    boolean unregister();
}
