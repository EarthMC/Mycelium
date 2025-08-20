package net.earthmc.mycelium.api.network;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Represents the whole network being managed by mycelium.
 */
@NullMarked
public interface Network extends PlayerList {
    /**
     * {@return the id of this network}
     */
    String id();

    /**
     * {@return an unmodifiable collection of active proxies part of the network}
     */
    @Unmodifiable
    Collection<Proxy> proxies();

    /**
     * {@return The proxy with the given id, if found}
     * @param id The proxy id to search by.
     */
    @Nullable
    Proxy getProxyById(String id);

    /**
     * {@return an unmodifiable collection of active backends}
     */
    @Unmodifiable
    Collection<Server> servers();

    /**
     * {@return The server with the given id, if found}
     * @param id The server id to search by.
     */
    @Nullable
    Server getServerById(String id);
}
