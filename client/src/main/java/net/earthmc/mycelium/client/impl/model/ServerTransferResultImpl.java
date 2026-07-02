package net.earthmc.mycelium.client.impl.model;

import net.earthmc.mycelium.api.network.player.ServerTransferResult;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public record ServerTransferResultImpl(boolean successful, @Nullable Component failureMessage) implements ServerTransferResult {
}
