package net.earthmc.mycelium.client;

import net.earthmc.mycelium.api.network.Platform;

public class StandalonePlatform extends Platform {
    @Override
    public String platformIdentifier() {
        return "standalone";
    }

    @Override
    public Type type() {
        return Type.STANDALONE;
    }
}
