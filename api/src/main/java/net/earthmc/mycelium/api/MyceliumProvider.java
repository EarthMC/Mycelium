package net.earthmc.mycelium.api;

import org.jspecify.annotations.Nullable;

public class MyceliumProvider {
    private static @Nullable Mycelium INSTANCE = null;

    public static void register(Mycelium instance) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Cannot register mycelium api provider when a provider is already set.");
        }

        INSTANCE = instance;
    }

    public static Mycelium get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Mycelium api provider has not been registered yet!");
        }

        return INSTANCE;
    }
}
