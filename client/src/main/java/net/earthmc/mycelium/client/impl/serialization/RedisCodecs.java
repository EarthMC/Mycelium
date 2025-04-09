package net.earthmc.mycelium.client.impl.serialization;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.lettuce.core.codec.CompressionCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.nio.ByteBuffer;

public class RedisCodecs {
    @SuppressWarnings("unchecked")
    public static <T> RedisCodec<String, T> codecFor(JsonCodec<T> codec) {
        // Hacky, but treat the string json codec as the normal string codec to prevent double quotes.
        if (codec == Codecs.STRING) {
            return (RedisCodec<String, T>) StringCodec.UTF8;
        }

        return new RedisCodecWrapper<>(codec, GsonHelper.forCodec(codec));
    }

    @Deprecated // not sure whether compression does much for us, most messages are short enough where it won't matter
    public static <T> RedisCodec<String, T> compressedCodec(JsonCodec<T> codec) {
        return CompressionCodec.valueCompressor(codecFor(codec), CompressionCodec.CompressionType.DEFLATE);
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
