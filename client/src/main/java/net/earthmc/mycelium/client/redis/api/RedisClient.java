package net.earthmc.mycelium.client.redis.api;

import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

public interface RedisClient {
    RedisCommands<String, String> connect();

    <K, V> RedisCommands<K, V> connect(RedisCodec<K, V> codec);

    RedisPubSubCommands<String, String> connectPubSub();

    <K, V> RedisPubSubCommands<K, V> connectPubSub(RedisCodec<K, V> codec);
}
