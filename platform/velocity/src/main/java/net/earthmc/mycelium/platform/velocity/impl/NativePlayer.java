package net.earthmc.mycelium.platform.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.api.PlayerImpl;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class NativePlayer extends PlayerImpl {
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
    public void transferToServer(Server server) {
        final Player player = velocityPlayer();
        final RegisteredServer registeredServer = proxyServer.getServer(server.name()).orElse(null);

        if (player != null && registeredServer != null) {
            player.createConnectionRequest(registeredServer).fireAndForget();
        }
    }

    @Nullable
    private Player velocityPlayer() {
        return proxyServer.getPlayer(this.uuid()).orElse(null);
    }
}
