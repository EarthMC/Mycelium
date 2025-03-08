package net.earthmc.mycelium.api;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents something that can hold players.
 */
public interface PlayerList {
    /**
     * @return An unmodifiable collection of players.
     */
    Collection<Player> players();

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

    default boolean hasPlayer(String name) {
        return getPlayerByName(name) != null;
    }

    default boolean hasPlayer(UUID uuid) {
        return getPlayerByUUID(uuid) != null;
    }
}
