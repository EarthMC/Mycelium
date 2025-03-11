package net.earthmc.mycelium.api.network;

public abstract class Platform {
    private final String environment = System.getProperty("mycelium.environment", "prod");
    private final String id = System.getProperty("mycelium.id", System.getProperty("name", "unknown"));
    private final String keyPrefix = "mycelium:" + environment + ":" + platformIdentifier() + ":" + id + ":";

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
    public abstract String platformIdentifier();
}
