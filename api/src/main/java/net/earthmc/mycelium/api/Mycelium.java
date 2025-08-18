package net.earthmc.mycelium.api;

import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Platform;

public interface Mycelium {
    /**
     * {@return The mycelium api instance}
     */
    static Mycelium get() {
        return MyceliumProvider.get();
    }

    Network network();

    MessagingRegistrar messaging();

    /**
     * {@return The current platform's instance}
     */
    Platform platform();
}
