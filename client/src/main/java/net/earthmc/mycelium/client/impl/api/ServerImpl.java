package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;

public class ServerImpl implements Server, PlayerListImpl {
    private final String name;
    private final MyceliumClient client;

    public ServerImpl(final String name, final MyceliumClient client) {
        this.name = name;
        this.client = client;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String playerSetKey() {
        return RedisKey.create(client.network().id(), "server", this.name, "players");
    }

    @Override
    public MyceliumClient client() {
        return this.client;
    }
}
