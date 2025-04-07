package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;

public class ProxyImpl implements Proxy, PlayerListImpl {
    public static final JsonCodec<Proxy> CODEC = JsonCodec.simple();
    private final String id;
    private final MyceliumClient client;

    public ProxyImpl(final String id, final MyceliumClient client) {
        this.id = id;
        this.client = client;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String playerSetKey() {
        return RedisKey.create(client.network().id(), "proxy", this.id, "players");
    }

    @Override
    public MyceliumClient client() {
        return this.client;
    }
}
