package net.earthmc.mycelium.client.redis.api;

import io.lettuce.core.codec.RedisCodec;

public interface RedisClient {
    RedisConnection<String, String> connect();

    <K, V> RedisConnection<K, V> connect(RedisCodec<K, V> codec);
}
