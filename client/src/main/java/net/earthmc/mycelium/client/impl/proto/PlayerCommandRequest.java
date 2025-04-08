package net.earthmc.mycelium.client.impl.proto;

import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.UUID;

public record PlayerCommandRequest(UUID playerUUID, String commandLine) {
    public static final JsonCodec<PlayerCommandRequest> CODEC = JsonCodec.simple();
}
