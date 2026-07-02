package net.earthmc.mycelium.client.impl.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.Strictness;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.lang.reflect.Type;
import java.time.Instant;

public class GsonHelper {
    public static final Gson DEFAULT_INSTANCE = newGsonBuilder().create();

    public static GsonBuilder newGsonBuilder() {
        final GsonBuilder builder = new GsonBuilder().serializeNulls();

        try {
            builder.setStrictness(Strictness.STRICT);
        } catch (NoSuchMethodError | NoClassDefFoundError ignored) {}

        builder.registerTypeHierarchyAdapter(Component.class, new JsonCodec<Component>() {
            @Override
            public JsonElement serialize(final Component src, final Type typeOfSrc, final JsonSerializationContext context) {
                return GsonComponentSerializer.gson().serializeToTree(src);
            }

            @Override
            public Component deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
                return GsonComponentSerializer.gson().deserializeFromTree(json);
            }

            @Override
            public Type type() {
                return Component.class;
            }
        });

        builder.registerTypeAdapter(Instant.class, new JsonCodec<Instant>() {
            @Override
            public JsonElement serialize(final Instant src, final Type typeOfSrc, final JsonSerializationContext context) {
                return null;
            }

            @Override
            public Instant deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
                return null;
            }

            @Override
            public Type type() {
                return Instant.class;
            }
        });

        return builder;
    }

    public static Gson forCodec(JsonCodec<?> codec) {
        if (codec == null || codec instanceof JsonCodec.Simple<?>) {
            return DEFAULT_INSTANCE;
        }

        return newGsonBuilder().registerTypeAdapter(codec.type(), codec).create();
    }
}
