package net.earthmc.mycelium.api.network;

public abstract class Platform {
    protected static final String UNKNOWN_ID = "unknown";

    private final String environment = System.getProperty("mycelium.environment", "prod");
    private final String id = System.getProperty("mycelium.id", System.getProperty("name", UNKNOWN_ID));
    private final String keyPrefix = "m:" + environment + ":" + identifier() + ":" + id + ":";

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
    public abstract String identifier();

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
