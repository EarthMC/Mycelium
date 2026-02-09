package net.earthmc.mycelium.client.impl.model;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public record PlayerCommandRequest(UUID playerUUID, String commandLine) {
    public static final JsonCodec<PlayerCommandRequest> CODEC = JsonCodec.simple();

    public PlayerCommandRequest {
        commandLine = commandLine.trim();
        commandLine = (commandLine.startsWith("/") ? commandLine.substring(1) : commandLine).trim();
    }
}
