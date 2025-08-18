package net.earthmc.mycelium.client.impl.model;

import net.earthmc.mycelium.api.serialization.JsonCodec;

import java.util.UUID;

public record TransferToServer(UUID playerUUID, String serverName) {
    public static final JsonCodec<TransferToServer> CODEC = JsonCodec.simple(TransferToServer.class);
}
