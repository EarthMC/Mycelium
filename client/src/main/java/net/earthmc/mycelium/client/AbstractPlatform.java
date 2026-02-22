package net.earthmc.mycelium.client;

import net.earthmc.mycelium.api.platform.Platform;
import net.earthmc.mycelium.api.platform.PlatformType;
import net.earthmc.mycelium.client.util.Property;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;

@NullMarked
public abstract class AbstractPlatform implements Platform {
    protected static final String UNKNOWN_ID = "unknown";

    private final String environment = Property.property("mycelium.environment", "prod");
    private final String id = Property.property("mycelium.id", Property.property("name", UNKNOWN_ID));
    private final String keyPrefix = "m:" + environment + ":" + platformIdentifier() + ":" + id + ":";

    public String key(String fieldName) {
        return keyPrefix + fieldName;
    }

    public String environment() {
        return this.environment;
    }

    @Override
    public String id() {
        return id;
    }

    /**
     * @return The identifier for this platform, for use in keys in the KV store.
     */
    public String platformIdentifier() {
        return type().name().toLowerCase(Locale.ROOT);
    }

    /**
     *
     * @return The type of platform.
     */
    public abstract PlatformType type();

    public @Nullable Path dataDirectory() {
        return null;
    }
}
