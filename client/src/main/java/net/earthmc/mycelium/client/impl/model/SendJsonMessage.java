package net.earthmc.mycelium.client.impl.model;

import com.google.gson.JsonElement;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record SendJsonMessage(@Nullable UUID target, JsonElement messageJson) {
    public static final JsonCodec<SendJsonMessage> CODEC = JsonCodec.simple(SendJsonMessage.class);
}
