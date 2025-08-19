package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.earthmc.mycelium.client.redis.collection.RedisRemoteSet;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

@NullMarked
public class NetworkImpl implements Network, PlayerListImpl {
    private final String id;
    private final MyceliumClient client;

    private final RedisRemoteSet<String> proxies;
    private final RedisRemoteSet<String> servers;

    private @Nullable Server nativeServer;
    private @Nullable Proxy nativeProxy;

    public NetworkImpl(final String id, final MyceliumClient client) {
        this.id = id;
        this.client = client;

        this.proxies = new RedisRemoteSet<>(client, "m:" + id + ":proxies", Codecs.STRING);
        this.servers = new RedisRemoteSet<>(client, "m:" + id + ":servers", Codecs.STRING);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Collection<Proxy> proxies() {
        return this.proxies.stream().map(this::createProxy).toList();
    }

    @Override
    public @Nullable Proxy getProxyById(String id) {
        id = id.toLowerCase(Locale.ROOT);

        if (!this.proxies.contains(id)) {
            return null;
        }

        return createProxy(id);
    }

    @Override
    public Collection<Server> servers() {
        return this.servers.stream().map(this::createServer).toList();
    }

    @Override
    public @Nullable Server getServerById(String id) {
        id = id.toLowerCase(Locale.ROOT);

        if (!this.servers.contains(id)) {
            return null;
        }

        return createServer(id);
    }

    @Override
    public String playerSetKey() {
        return RedisKey.create(id, "players");
    }

    @Override
    public MyceliumClient client() {
        return this.client;
    }

    private Server createServer(final String name) {
        if (this.nativeServer != null && name.equalsIgnoreCase(this.nativeServer.name())) {
            return this.nativeServer;
        }

        return new ServerImpl(name, client);
    }

    private Proxy createProxy(final String name) {
        if (this.nativeProxy != null && name.equalsIgnoreCase(this.nativeProxy.id())) {
            return this.nativeProxy;
        }

        return new ProxyImpl(name, client);
    }

    public void setNativeServer(final @Nullable Server nativeServer) {
        this.nativeServer = nativeServer;
    }

    public void setNativeProxy(final @Nullable Proxy nativeProxy) {
        this.nativeProxy = nativeProxy;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NetworkImpl network)) return false;
        return Objects.equals(id, network.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
