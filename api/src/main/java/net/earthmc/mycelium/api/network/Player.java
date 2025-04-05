package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.messaging.MessageRecipient;
import net.earthmc.mycelium.api.proto.Command;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player connected to the network.
 */
@NullMarked
public interface Player extends MessageRecipient {
    /**
     * @return The name of this player.
     */
    String username();

    /**
     * @return The uuid of this player.
     */
    UUID uuid();

    /**
     * @return The server this player is on, or {@code null} if this player is no longer connected.
     */
    @Nullable
    Server server();

    /**
     * @return The proxy this player is connected through, or {@code null} if this player is not connected to the network or through a proxy.
     */
    Proxy proxy();

    /**
     * @return {@code true} if this player is currently connected to the network.
     */
    boolean isOnline();

    /**
     * Runs a command for the player on the
     * @param command
     */
    void runCommand(Command command);
}
