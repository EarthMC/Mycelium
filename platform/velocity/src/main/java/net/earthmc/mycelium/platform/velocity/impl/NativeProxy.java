package net.earthmc.mycelium.platform.velocity.impl;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.api.ProxyImpl;
import net.earthmc.mycelium.platform.velocity.VelocityPlatform;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@NullMarked
public class NativeProxy extends ProxyImpl {
    public final VelocityPlatform platform;

    public NativeProxy(String id, MyceliumClient client, VelocityPlatform platform) {
        super(id, client);
        this.platform = platform;
    }

    @Override
    public void runConsoleCommand(ConsoleCommand command) {
        platform.proxy.getCommandManager().executeAsync(platform.proxy.getConsoleCommandSource(), command.command());
    }

    @Override
    public Collection<Player> players() {
        final List<Player> players = new ArrayList<>();

        for (final com.velocitypowered.api.proxy.Player player : platform.proxy.getAllPlayers()) {
            players.add(fromVelocity(player));
        }

        return players;
    }

    @Override
    public int playerCount() {
        return this.platform.proxy.getAllPlayers().size();
    }

    @Override
    public @Nullable Player getPlayerByName(String name) {
        return fromVelocity(platform.proxy.getPlayer(name).orElse(null));
    }

    @Override
    public @Nullable Player getPlayerByUUID(UUID uuid) {
        return fromVelocity(platform.proxy.getPlayer(uuid).orElse(null));
    }

    @Nullable
    @Contract("!null -> !null")
    public Player fromVelocity(com.velocitypowered.api.proxy.@Nullable Player player) {
        if (player == null) {
            return null;
        }

        return new NativePlayer(player.getUsername(), player.getUniqueId(), client(), this);
    }
}
