package net.earthmc.mycelium.client.impl.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import net.earthmc.mycelium.api.serialization.JsonCodec;

public class GsonHelper {
    public static final Gson DEFAULT_INSTANCE = newGsonBuilder().create();

    public static GsonBuilder newGsonBuilder() {
        final GsonBuilder builder =  new GsonBuilder().serializeNulls();

        try {
            builder.setStrictness(Strictness.STRICT);
        } catch (NoSuchMethodError ignored) {}

        return builder;
    }

    public static Gson forCodec(JsonCodec<?> codec) {
        if (codec == null || codec instanceof JsonCodec.Simple<?>) {
            return DEFAULT_INSTANCE;
        }

        return newGsonBuilder().registerTypeAdapter(codec.type(), codec).create();
    }
}
