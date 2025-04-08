package net.earthmc.mycelium.client.impl.messaging;

import com.google.gson.Gson;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.Listener;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.impl.serialization.GsonHelper;

import java.util.function.Consumer;

public class BoundChannelIdentifier<T> extends ChannelIdentifier.Bound<T> {
    private final JsonCodec<T> codec;
    private final MessagingRegistrar registrar;
    private final Gson gsonInstance;

    public BoundChannelIdentifier(final String channel, MessagingRegistrar registrar, JsonCodec<T> codec) {
        ChannelIdentifier.identifier(channel).super(channel);
        this.codec = codec;
        this.registrar = registrar;

        if (codec instanceof JsonCodec.Simple<T>) {
            this.gsonInstance = GsonHelper.DEFAULT_INSTANCE;
        } else {
            this.gsonInstance = GsonHelper.forCodec(codec);
        }
    }

    @Override
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
    public Listener registerPlatform(Consumer<IncomingMessage<T>> receiver) {
        return this.registrar.registerPlatformChannel(this, receiver);
    }

    @Override
    public Listener register(Consumer<IncomingMessage<T>> receiver) {
        return this.registrar.registerChannel(this, receiver);
    }
}
