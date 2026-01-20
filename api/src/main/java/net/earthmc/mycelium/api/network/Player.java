package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.network.command.Command;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player connected to the network.
 */
public interface Player {
    /**
     * {@return the name of this player}
     */
    String username();

    /**
     * {@return the uuid of this player}
     */
    UUID uuid();

    /**
     * {@return the server this player is on, or null if this player is no longer connected}
     */
    @Nullable
    Server server();

    /**
     * {@return the proxy this player is connected through, or null if this player is not connected to the network or through a proxy}
     */
    @Nullable
    Proxy proxy();

    /**
     * {@return whether this player is currently connected to the network}
     */
    boolean isOnline();

    /**
     * Runs a command for the player.
     * @param command The command to run, whether it's executed on the server or proxy depends on the {@link Command#target()}.
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
