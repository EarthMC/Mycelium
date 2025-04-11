package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.proto.Command;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player connected to the network.
 */
@NullMarked
public interface Player {
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
    @Nullable
    Proxy proxy();

    /**
     * @return {@code true} if this player is currently connected to the network.
     */
    boolean isOnline();

    /**
     * Runs a command for the player.
     * @param command The command to run, whether it's executed on the server or proxy is specified during creation.
     */
    void runCommand(Command command);

    /**
     * Sends the player a message, styled using minimessage.
     * @param message The message to send.
     */
    void sendRichMessage(String message);

    /**
     * Attempts to connect the player to the specified server, if that server is part of the proxy the player is currently connected to.
     * @param server The server to send the player to.
     */
    void transferToServer(Server server);
}
