package net.earthmc.mycelium.api;

import java.util.UUID;

/**
 * Represents a player connected to the network.
 */
public interface Player {
    String username();

    UUID uuid();
}
