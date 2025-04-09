package net.earthmc.mycelium.client.impl.api;

import io.lettuce.core.api.StatefulRedisConnection;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.earthmc.mycelium.client.redis.collection.RedisRemoteSet;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class NetworkImpl implements Network, PlayerListImpl {
    private final String id;
    private final MyceliumClient client;

    private final RedisRemoteSet<String> proxies;
    private final RedisRemoteSet<String> servers;

    public NetworkImpl(final String id, final MyceliumClient client) {
        this.id = id;
        this.client = client;

        this.proxies = new RedisRemoteSet<>(client.client(), "m:" + id + ":proxies", Codecs.STRING);
        this.servers = new RedisRemoteSet<>(client.client(), "m:" + id + ":servers", Codecs.STRING);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Collection<Proxy> proxies() {
        return this.proxies.stream().map(id -> new ProxyImpl(id, this.client)).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @Nullable Proxy getProxyById(String id) {
        id = id.toLowerCase(Locale.ROOT);

        if (!this.proxies.contains(id)) {
            return null;
        }

        return new ProxyImpl(id, client);
    }

    @Override
    public Collection<Server> servers() {
        return this.servers.stream().map(id -> new ServerImpl(id, this.client)).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @Nullable Server getServerById(String id) {
        id = id.toLowerCase(Locale.ROOT);

        if (!this.servers.contains(id)) {
            return null;
        }

        return new ServerImpl(id, client);
    }

    @Override
    public int playerCount() {
        try (final StatefulRedisConnection<String, String> connection = client.client().connect()) {
            return Math.toIntExact(connection.sync().scard(RedisKey.create(id, "players")));
        }
    }

    @Override
    public String playerSetKey() {
        return RedisKey.create(id, "players");
    }

    @Override
    public MyceliumClient client() {
        return this.client;
    }
}
