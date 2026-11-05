package net.earthmc.mycelium.client;

import net.earthmc.mycelium.api.platform.PlatformType;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;

public class StandalonePlatform extends AbstractPlatform {
    @Override
    public PlatformType type() {
        return PlatformType.STANDALONE;
    }

    @Override
    public @Nullable Path dataDirectory() {
        return Path.of("");
    }
}
