package net.earthmc.mycelium.platform.paper.impl;

import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.api.PlayerImpl;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NativePlayer extends PlayerImpl implements ForwardingAudience.Single {
    private final NativeServer server;
    private final org.bukkit.Server bukkitServer;

    public NativePlayer(String username, UUID uuid, MyceliumClient client, NativeServer server) {
        super(username, uuid, client);
        this.server = server;
        this.bukkitServer = server.plugin.getServer();
    }

    @Override
    public @Nullable Server server() {
        return this.server;
    }

    @Override
    public boolean isOnline() {
        return bukkitPlayer() != null;
    }

    @Override
    public void runCommand(Command command) {
        if (command.target() != Command.Target.BACKEND) {
            super.runCommand(command);
            return;
        }

        final Player player = bukkitPlayer();
        if (player != null) {
            player.getScheduler().execute(this.server.plugin, () -> bukkitServer.dispatchCommand(player, command.command()), null, 1L);
        }
    }

    @Override
    public void sendRichMessage(String message) {
        final Player player = bukkitPlayer();
        if (player != null) {
            player.sendRichMessage(message);
        }
    }

    @Override
    public CompletableFuture<Boolean> kick(final Component reason) {
        final Player player = bukkitPlayer();
        if (player != null) {
            player.kick(reason);
        }

        return CompletableFuture.completedFuture(player != null);
    }

    @Nullable
    private Player bukkitPlayer() {
        return bukkitServer.getPlayer(this.uuid());
    }

    @Override
    public @NotNull Audience audience() {
        final Player player = bukkitPlayer();
        return player != null ? player : Audience.empty();
    }
}
