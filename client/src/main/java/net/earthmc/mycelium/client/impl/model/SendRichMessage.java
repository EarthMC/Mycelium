package net.earthmc.mycelium.client.impl.model;

import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.UUID;

public record SendRichMessage(UUID playerUUID, String message) {
    public static final JsonCodec<SendRichMessage> CODEC = JsonCodec.simple(SendRichMessage.class);
}
