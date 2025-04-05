package net.earthmc.mycelium.api.network;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Represents the whole network being managed by mycelium.
 */
@NullMarked
public interface Network extends PlayerList {
    /**
     * @return The id of this network.
     */
    String id();

    /**
     * @return An unmodifiable collection of active proxies part of the network.
     */
    Collection<Proxy> proxies();

    /**
     * @param id The proxy id to search by.
     * @return The proxy with the given id, if found.
     */
    @Nullable
    Proxy getProxyById(String id);

    /**
     * @return An unmodifiable collection of active backends.
     */
    Collection<Server> backends();

    /**
     * @param id The server id to search by.
     * @return The server with the given id, if found.
     */
    @Nullable
    Server getServerById(String id);
}
