package net.earthmc.mycelium.client.impl.messaging;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.lettuce.core.codec.CompressionCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * An internal
 */
public class InternalMessage {
    public static final JsonCodec<InternalMessage> CODEC = JsonCodec.simple();

    public final String version = "1.0.0";

    /**
     * The redis pubsub channel that can be used for responses, or null if responses are not possible (or desired).
     */
    @Nullable
    public final String replyTo;

    /**
     * The uuid of the source client, this is so that we don't accidentally listen to our own messages.
     */
    public final String source;

    /**
     * The message reference, used for identifying this message in replies. (for callbacks etc.)
     */
    public final String messageReference;

    /**
     * The actual message payload.
     */
    public final String payload;

    public InternalMessage(@Nullable String replyTo, String source, String messageReference, String payload) {
        this.replyTo = replyTo;
        this.source = source;
        this.messageReference = messageReference;
        this.payload = payload;
    }

    public static final RedisCodec<String, InternalMessage> REDIS_CODEC = CompressionCodec.valueCompressor(new RedisCodec<>() {
        private static final Gson GSON = new Gson();

        @Override
        public String decodeKey(ByteBuffer bytes) {
            return StringCodec.UTF8.decodeKey(bytes);
        }

        @Override
        public InternalMessage decodeValue(ByteBuffer bytes) {
            final String decoded = StringCodec.UTF8.decodeValue(bytes);

            try {
                return GSON.fromJson(decoded, InternalMessage.class);
            } catch (JsonSyntaxException e) {
                return null;
            }
        }

        @Override
        public ByteBuffer encodeKey(String key) {
            return StringCodec.UTF8.encodeKey(key);
        }

        @Override
        public ByteBuffer encodeValue(InternalMessage value) {
            final String encoded = GSON.toJson(value, InternalMessage.class);
            return StringCodec.UTF8.encodeValue(encoded);
        }
    }, CompressionCodec.CompressionType.DEFLATE);
}
