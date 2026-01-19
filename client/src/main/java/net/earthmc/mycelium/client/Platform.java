package net.earthmc.mycelium.client;

import net.earthmc.mycelium.client.util.Property;

import java.util.Locale;

public abstract class Platform {
    protected static final String UNKNOWN_ID = "unknown";

    private final String environment = Property.property("mycelium.environment", "prod");
    private final String id = Property.property("mycelium.id", UNKNOWN_ID);
    private final String keyPrefix = "m:" + environment + ":" + platformIdentifier() + ":" + id + ":";

    public String key(String fieldName) {
        return keyPrefix + fieldName;
    }

    public String environment() {
        return this.environment;
    }

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
    public abstract Type type();

    public enum Type {
        STANDALONE,
        SERVER,
        PROXY
    }
}
