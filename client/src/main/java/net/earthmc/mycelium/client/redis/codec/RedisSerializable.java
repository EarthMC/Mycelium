package net.earthmc.mycelium.client.redis.codec;

import io.lettuce.core.codec.RedisCodec;

public interface RedisSerializable<T> {
    RedisCodec<String, T> codec();
}
