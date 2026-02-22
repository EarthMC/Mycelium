package net.earthmc.mycelium.api.platform;

/**
 * Represents the current platform Mycelium is running on.
 */
public interface Platform {
    /**
     * {@return the identifier of the platform}
     */
    String id();

    /**
     * {@return the current platform's type}
     */
    PlatformType type();
}
