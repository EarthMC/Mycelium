package net.earthmc.mycelium.client.redis.impl;

import io.lettuce.core.codec.RedisCodec;
import net.earthmc.mycelium.client.redis.api.RedisClient;
import net.earthmc.mycelium.client.redis.api.RedisConnection;

public class RedisClientWrapper implements RedisClient {
    private final io.lettuce.core.RedisClient client;

    public RedisClientWrapper(io.lettuce.core.RedisClient client) {
        this.client = client;
    }

    @Override
    public RedisConnection<String, String> connect() {
        return new RedisConnectionWrapper<>();
    }

    @Override
    public <K, V> RedisConnection<K, V> connect(RedisCodec<K, V> codec) {
        return new RedisConnectionWrapper<>();
    }
}
