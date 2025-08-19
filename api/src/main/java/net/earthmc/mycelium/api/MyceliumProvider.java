package net.earthmc.mycelium.api;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * A static provider for the Mycelium API.
 */
public class MyceliumProvider {
    private static @Nullable Mycelium INSTANCE = null;

    private MyceliumProvider() {}

    /**
     * Only to be used internally.
     * @param instance The mycelium api instance.
     */
    @ApiStatus.Internal
    public static void register(Mycelium instance) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Cannot register mycelium API provider, instance is already set.");
        }

        INSTANCE = instance;
    }

    /**
     * Static getter for the mycelium api.
     * @return The mycelium api instance.
     */
    public static Mycelium get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Mycelium API provider has not been registered yet!");
        }

        return INSTANCE;
    }
}
