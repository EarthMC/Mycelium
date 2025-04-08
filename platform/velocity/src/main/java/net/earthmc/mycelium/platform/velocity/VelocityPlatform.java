package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.api.proto.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.proto.PlayerCommandRequest;

@Plugin(name = "Mycelium", id = "mycelium", version = "0.0.1", authors = "Warriorrr")
public class VelocityPlatform extends Platform {
    @Inject
    private ProxyServer proxy;

    private MyceliumClient client = MyceliumClient.forPlatform(this).build();

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        final MessagingRegistrar registrar = client.messaging();

        // TODO: debug logging
        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("console-command"), ConsoleCommand.CODEC), incoming -> {
            final ConsoleCommand payload = incoming.data();
            if (payload.command().isEmpty()) {
                return;
            }

            // TODO: verify whether with or without / is needed.
            this.proxy.getCommandManager().executeAsync(this.proxy.getConsoleCommandSource(), payload.command());
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("player-command"), PlayerCommandRequest.CODEC), incoming -> {
            final PlayerCommandRequest payload = incoming.data();
            final Player player = this.proxy.getPlayer(payload.playerUUID()).orElse(null);
            if (payload.commandLine().isEmpty() || player == null) {
                return;
            }

            this.proxy.getCommandManager().executeAsync(player, payload.commandLine());
        });
    }

    @Subscribe
    public void onPlayerJoin(LoginEvent event) {

    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {

    }

    @Override
    public String identifier() {
        return "proxy";
    }

    @Override
    public Type type() {
        return Type.PROXY;
    }
}
