package net.earthmc.mycelium.api.network;

import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Represents the whole network being managed by mycelium.
 */
public interface Network extends PlayerList {
    /**
     * @return An unmodifiable collection of active proxies part of the network.
     */
    Collection<Proxy> proxies();

    @Nullable
    Proxy getProxyById(String id);

    /**
     * @return An unmodifiable collection of active backends.
     */
    Collection<Server> backends();
}
