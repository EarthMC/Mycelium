package net.earthmc.mycelium.client.impl.model;

import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.UUID;

public record SendMessage(UUID playerUUID, String message) {
    public static final JsonCodec<SendMessage> CODEC = JsonCodec.simple(SendMessage.class);
}
