package net.earthmc.mycelium.api;

import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;

/**
 * Mycelium, a communications library for Minecraft.
 */
public interface Mycelium {
    /**
     * {@return The mycelium api instance}
     */
    static Mycelium get() {
        return MyceliumProvider.get();
    }

    Network network();

    MessagingRegistrar messaging();
}
