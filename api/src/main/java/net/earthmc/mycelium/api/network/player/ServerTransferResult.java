package net.earthmc.mycelium.api.network.player;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public interface ServerTransferResult {
    boolean successful();

    @Nullable Component failureMessage();
}
