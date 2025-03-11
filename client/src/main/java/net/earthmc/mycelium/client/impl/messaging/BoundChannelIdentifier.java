package net.earthmc.mycelium.client.impl.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.serialization.JsonSerializable;

import java.util.function.Consumer;

public class BoundChannelIdentifier<T extends JsonSerializable<T>> extends ChannelIdentifier.Bound<T> {
    private static final Gson DEFAULT_GSON = newGsonBuilder().create();

    private final JsonCodec<T> codec;
    private final MessagingRegistrar registrar;
    private final Gson gsonInstance;

    public BoundChannelIdentifier(final String channel, MessagingRegistrar registrar, JsonCodec<T> codec) {
        ChannelIdentifier.identifier(channel).super(channel);
        this.codec = codec;
        this.registrar = registrar;

        if (codec instanceof JsonCodec.Simple<T>) {
            this.gsonInstance = DEFAULT_GSON;
        } else {
            this.gsonInstance = newGsonBuilder().registerTypeAdapter(codec.typeClass(), codec).create();
        }
    }

    public JsonCodec<T> codec() {
        return codec;
    }

    /**
     * @return A gson instance capable of serializing to/from this type.
     */
    public Gson gson() {
        return gsonInstance;
    }

    @Override
    public void register(Consumer<T> receiver) {
        this.registrar.registerBoundChannel(this, receiver);
    }

    private static GsonBuilder newGsonBuilder() {
        return new GsonBuilder().serializeNulls().setStrictness(Strictness.STRICT);
    }
}
