package net.earthmc.mycelium.api.event.player;

import net.earthmc.mycelium.api.event.server.ServerEvent;
import net.earthmc.mycelium.api.network.Server;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface PlayerJoinedServerEvent extends PlayerEvent, ServerEvent {
    /**
     * {@return the server that this player was previously connected to}
     */
    @Nullable Server previousServer();
}
