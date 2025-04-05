package net.earthmc.mycelium.api.network;

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
     * @return An unmodifiable collection of players.
     */
    Collection<Player> players();

    /**
     * @return The number of players currently online on the network.
     */
    int playerCount();

    /**
     * @param name The name of the player.
     * @return The player with the given name, or {@code null} if not found.
     */
    @Nullable
    Player getPlayerByName(String name);

    /**
     * @param uuid The uuid of the player.
     * @return The player with the given uuid, or {@code null} if not found.
     */
    @Nullable
    Player getPlayerByUUID(UUID uuid);

    /**
     * @param name The name of the player.
     * @return Whether we have a player with the given name.
     */
    default boolean hasPlayer(String name) {
        return getPlayerByName(name) != null;
    }

    /**
     * @param uuid The uuid of the player.
     * @return Whether we have a player with the given uuid.
     */
    default boolean hasPlayer(UUID uuid) {
        return getPlayerByUUID(uuid) != null;
    }
}
