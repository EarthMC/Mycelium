package net.earthmc.mycelium.api.messaging;

/**
 * Represents something that can send messages.
 */
public interface MessageSender {
    /**
     * {@return whether this sender is the current client itself}
     */
    boolean isSelf();
}
