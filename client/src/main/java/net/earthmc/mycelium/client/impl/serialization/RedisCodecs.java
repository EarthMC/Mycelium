package net.earthmc.mycelium.client.impl.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.lettuce.core.codec.CompressionCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.nio.ByteBuffer;

public class RedisCodecs {
    public static <T> RedisCodec<String , T> codecFor(JsonCodec<T> codec) {
        return CompressionCodec.valueCompressor(new RedisCodecWrapper<>(codec, GsonHelper.forCodec(codec)), CompressionCodec.CompressionType.DEFLATE);
    }

    private record RedisCodecWrapper<T>(JsonCodec<T> codec, Gson gson) implements RedisCodec<String, T> {
        @Override
        public String decodeKey(ByteBuffer bytes) {
            return StringCodec.UTF8.decodeKey(bytes);
        }

        @Override
        public T decodeValue(ByteBuffer bytes) {
            final String decoded = StringCodec.UTF8.decodeValue(bytes);

            try {
                return gson.fromJson(decoded, codec.type());
            } catch (JsonSyntaxException e) {
                return null;
            }
        }

        @Override
        public ByteBuffer encodeKey(String key) {
            return StringCodec.UTF8.encodeKey(key);
        }

        @Override
        public ByteBuffer encodeValue(T value) {
            final String encoded = gson.toJson(value, codec.type());
            return StringCodec.UTF8.encodeValue(encoded);
        }
    }
}
