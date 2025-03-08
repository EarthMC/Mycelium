package net.earthmc.mycelium.client.redis.codec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

import java.nio.ByteBuffer;

public class KryoCodec<T> implements RedisCodec<String, T> {
    private final Kryo kryo = new Kryo();
    private final Class<T> type;

    public KryoCodec(Class<T> clazz, Class<?>... extraClasses) {
        this.type = clazz;
        kryo.register(clazz);

        for (final Class<?> extraClass : extraClasses) {
            kryo.register(extraClass);
        }
    }

    @Override
    public String decodeKey(ByteBuffer bytes) {
        return StringCodec.UTF8.decodeKey(bytes);
    }

    @Override
    public T decodeValue(ByteBuffer bytes) {
        if (!bytes.hasRemaining()) {
            return null;
        }

        byte[] array = new byte[bytes.remaining()];
        bytes.get(array);
        try (Input input = new Input(array)) {
            return kryo.readObject(input, type);
        }
    }

    @Override
    public ByteBuffer encodeKey(String key) {
        return StringCodec.UTF8.encodeKey(key);
    }

    @Override
    public ByteBuffer encodeValue(T value) {
        try (Output output = new Output(512, -1)) {
            kryo.writeObject(output, value);
            return ByteBuffer.wrap(output.toBytes());
        }
    }
}
