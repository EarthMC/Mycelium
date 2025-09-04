package net.earthmc.mycelium.platform.paper.impl;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.api.ServerImpl;
import net.earthmc.mycelium.platform.paper.PaperLoader;
import org.bukkit.Server;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class NativeServer extends ServerImpl {
    private final Server bukkitServer;
    public final PaperLoader plugin;

    public NativeServer(String name, MyceliumClient client, PaperLoader plugin) {
        super(name, client);
        this.plugin = plugin;
        this.bukkitServer = plugin.getServer();
    }

    @Override
    public void runConsoleCommand(ConsoleCommand command) {
        bukkitServer.getGlobalRegionScheduler().execute(this.plugin, () -> {
            bukkitServer.dispatchCommand(bukkitServer.getConsoleSender(), command.command());
        });
    }

    @Override
    public <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier identifier, T data) {
        throw new UnsupportedOperationException("Cannot message self."); // FIXME
    }

    @Override
    public <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data) {
        throw new UnsupportedOperationException("Cannot message self."); // FIXME
    }

    @Override
    public Collection<Player> players() {
        final List<Player> players = new ArrayList<>();

        for (final org.bukkit.entity.Player player : bukkitServer.getOnlinePlayers()) {
            players.add(fromBukkit(player));
        }

        return players;
    }

    @Override
    public int playerCount() {
        return bukkitServer.getOnlinePlayers().size();
    }

    @Override
    public @Nullable Player getPlayerByName(String name) {
        return Optional.ofNullable(bukkitServer.getPlayerExact(name)).map(this::fromBukkit).orElseGet(() -> super.getPlayerByName(name));
    }

    @Override
    public @Nullable Player getPlayerByUUID(UUID uuid) {
        return Optional.ofNullable(bukkitServer.getPlayer(uuid)).map(this::fromBukkit).orElseGet(() -> super.getPlayerByUUID(uuid));
    }

    @Nullable
    @Contract("!null -> !null")
    private Player fromBukkit(final org.bukkit.entity.@Nullable Player player) {
        if (player == null) {
            return null;
        }

        return new NativePlayer(player.getName(), player.getUniqueId(), client(), this);
    }
}
