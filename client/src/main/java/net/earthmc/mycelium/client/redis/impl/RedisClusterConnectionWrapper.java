package net.earthmc.mycelium.client.redis.impl;

import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import net.earthmc.mycelium.client.redis.api.RedisConnection;

public class RedisClusterConnectionWrapper<K, V> implements RedisConnection<K, V> {
    private final RedisAdvancedClusterAsyncCommands<K, V> connection;

    public RedisClusterConnectionWrapper(RedisAdvancedClusterAsyncCommands<K, V> connection) {
        this.connection = connection;
    }
}
