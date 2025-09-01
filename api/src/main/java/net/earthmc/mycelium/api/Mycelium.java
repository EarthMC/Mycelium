package net.earthmc.mycelium.api;

import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.store.Store;

/**
 * Mycelium, a communications library for Minecraft.
 */
public interface Mycelium {
    /**
     * {@return the mycelium api instance}
     */
    static Mycelium api() {
        return MyceliumProvider.get();
    }

    /**
     * {@return the main interface for interacting with the network.}
     */
    Network network();

    /**
     * {@return the messaging registrar}
     */
    MessagingRegistrar messaging();

    /**
     * {@return the data store}
     */
    Store dataStore();
}
