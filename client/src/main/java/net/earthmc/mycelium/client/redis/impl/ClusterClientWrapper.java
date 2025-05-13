package net.earthmc.mycelium.client.redis.impl;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.codec.RedisCodec;
import net.earthmc.mycelium.client.redis.api.RedisClient;
import net.earthmc.mycelium.client.redis.api.RedisConnection;

public class ClusterClientWrapper implements RedisClient {
    private final RedisClusterClient client;

    public ClusterClientWrapper(RedisClusterClient client) {
        this.client = client;
    }

    @Override
    public RedisConnection<String, String> connect() {
        return new RedisClusterConnectionWrapper<>(client.connect().sync());
    }

    @Override
    public <K, V> RedisConnection<K, V> connect(RedisCodec<K, V> codec) {
        return new RedisConnectionWrapper<>();
    }
}
