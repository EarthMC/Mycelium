package net.earthmc.mycelium.client.impl.model;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public record KickPlayer(UUID target, Component reason) {
    public static final JsonCodec<KickPlayer> CODEC = JsonCodec.simple(KickPlayer.class);
}
