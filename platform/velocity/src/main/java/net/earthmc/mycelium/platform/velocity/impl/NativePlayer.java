package net.earthmc.mycelium.platform.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.api.network.player.ServerTransferResult;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.api.PlayerImpl;
import net.earthmc.mycelium.client.impl.model.ServerTransferResultImpl;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NativePlayer extends PlayerImpl implements ForwardingAudience.Single {
    private final NativeProxy proxy;
    private final ProxyServer proxyServer;

    public NativePlayer(String username, UUID uuid, MyceliumClient client, NativeProxy proxy) {
        super(username, uuid, client);
        this.proxy = proxy;
        this.proxyServer = proxy.platform.proxy;
    }

    @Override
    public @Nullable Proxy proxy() {
        return this.proxy;
    }

    @Override
    public boolean isOnline() {
        return velocityPlayer() != null;
    }

    @Override
    public void runCommand(Command command) {
        final Player player = velocityPlayer();
        if (player != null) {
            proxyServer.getCommandManager().executeAsync(player, command.command());
        }
    }

    @Override
    public void sendRichMessage(String message) {
        final Player player = velocityPlayer();
        if (player != null) {
            player.sendRichMessage(message);
        }
    }

    @Override
    public CompletableFuture<ServerTransferResult> transferToServer(final String server) {
        final Player player = velocityPlayer();
        final RegisteredServer registeredServer = proxyServer.getServer(server).orElse(null);

        if (player != null && registeredServer != null) {
            return player.createConnectionRequest(registeredServer).connect().thenApply(result -> (ServerTransferResult) new ServerTransferResultImpl(result.isSuccessful(), result.getReasonComponent().orElse(null)))
                .exceptionally(throwable -> new ServerTransferResultImpl(false, Component.text("Connection failed with an exception: " + throwable.getMessage(), NamedTextColor.RED)));
        } else {
            return super.transferToServer(server);
        }
    }

    @Nullable
    private Player velocityPlayer() {
        return proxyServer.getPlayer(this.uuid()).orElse(null);
    }

    @Override
    public @NotNull Audience audience() {
        final Player player = velocityPlayer();
        return player != null ? player : Audience.empty();
    }
}
