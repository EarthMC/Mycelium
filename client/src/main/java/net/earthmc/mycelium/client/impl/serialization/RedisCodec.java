package net.earthmc.mycelium.client.impl.serialization;

import com.google.gson.Gson;
import net.earthmc.mycelium.api.serialization.JsonCodec;

public record RedisCodec<T>(JsonCodec<T> codec, Gson gson) {
    public static <T> RedisCodec<T> codecFor(JsonCodec<T> codec) {
        return new RedisCodec<>(codec, GsonHelper.forCodec(codec));
    }

    public T deserialize(String input) {
        return gson.fromJson(input, codec.type());
    }

    public String serialize(T input) {
        return gson.toJson(input, codec.type());
    }
}
