package net.earthmc.mycelium.client.impl.messaging;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.impl.serialization.RedisCodec;
import org.jspecify.annotations.Nullable;

/**
 * An internal message that is sent across the network.
 */
public class InternalMessage {
    public static final RedisCodec<InternalMessage> REDIS_CODEC = RedisCodec.codecFor(JsonCodec.simple());

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
}
