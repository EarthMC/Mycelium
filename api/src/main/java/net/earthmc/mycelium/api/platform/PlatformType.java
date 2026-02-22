package net.earthmc.mycelium.api.platform;

/**
 * All possible platform types where Mycelium can be ran.
 */
public enum PlatformType {
    /**
     * A standalone platform, running separately from any server/proxy process.
     */
    STANDALONE,

    /**
     * A server platform.
     */
    SERVER,

    /**
     * A proxy platform.
     */
    PROXY
}
