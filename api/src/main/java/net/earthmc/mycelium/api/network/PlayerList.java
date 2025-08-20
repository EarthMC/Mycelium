package net.earthmc.mycelium.api.network;

import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents something that can hold players.
 */
@NullMarked
public interface PlayerList {
    /**
     * {@return an unmodifiable collection of players}
     */
    @Unmodifiable
    Collection<Player> players();

    /**
     * {@return the number of players currently online on the network}
     */
    int playerCount();

    /**
     * @return The player with the given name, or {@code null} if not found.
     * @param name The name of the player.
     */
    @Nullable
    Player getPlayerByName(String name);

    /**
     * @return The player with the given uuid, or {@code null} if not found.
     * @param uuid The uuid of the player.
     */
    @Nullable
    Player getPlayerByUUID(UUID uuid);

    /**
     * @return Whether we have a player with the given name.
     * @param name The name of the player.
     */
    default boolean hasPlayer(String name) {
        return getPlayerByName(name) != null;
    }

    /**
     * @return Whether we have a player with the given uuid.
     * @param uuid The uuid of the player.
     */
    default boolean hasPlayer(UUID uuid) {
        return getPlayerByUUID(uuid) != null;
    }
}
