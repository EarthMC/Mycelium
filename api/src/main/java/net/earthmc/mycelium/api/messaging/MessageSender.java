package net.earthmc.mycelium.api.messaging;

public interface MessageSender {
    /**
     * {@return whether this sender is the current client itself}
     */
    boolean isSelf();
}
