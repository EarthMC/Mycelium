package net.earthmc.mycelium.client;

import net.earthmc.mycelium.api.platform.PlatformType;

public class StandalonePlatform extends AbstractPlatform {
    @Override
    public PlatformType type() {
        return PlatformType.STANDALONE;
    }
}
