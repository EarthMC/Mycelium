package net.earthmc.mycelium.api;

import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;

public interface Mycelium {
    /**
     * Static getter for the mycelium api.
     * @return The mycelium api instance.
     */
    static Mycelium get() {
        return MyceliumProvider.get();
    }

    Network network();

    MessagingRegistrar messaging();
}
