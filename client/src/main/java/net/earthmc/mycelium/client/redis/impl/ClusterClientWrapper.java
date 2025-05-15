package net.earthmc.mycelium.client.redis.impl;

import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import net.earthmc.mycelium.client.redis.api.RedisClient;

public class ClusterClientWrapper implements RedisClient {
    private final RedisClusterClient client;

    public ClusterClientWrapper(RedisClusterClient client) {
        this.client = client;
    }

    @Override
    public RedisCommands<String, String> connect() {
        return client.connect().sync();
    }

    @Override
    public <K, V> RedisCommands<K, V> connect(RedisCodec<K, V> codec) {
        return client.connect(codec).sync();
    }

    @Override
    public RedisPubSubCommands<String, String> connectPubSub() {
        return client.connectPubSub().sync();
    }

    @Override
    public <K, V> RedisPubSubCommands<K, V> connectPubSub(RedisCodec<K, V> codec) {
        return client.connectPubSub(codec).sync();
    }
}
