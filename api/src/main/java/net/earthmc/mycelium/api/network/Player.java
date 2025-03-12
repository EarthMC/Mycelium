package net.earthmc.mycelium.api.network;

import net.earthmc.mycelium.api.messaging.MessageRecipient;
import net.earthmc.mycelium.api.proto.Command;

import java.util.UUID;

/**
 * Represents a player connected to the network.
 */
public interface Player extends MessageRecipient {
    String username();

    UUID uuid();

    void runCommand(Command command);
}
