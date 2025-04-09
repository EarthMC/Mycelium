package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.lettuce.core.api.StatefulRedisConnection;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.api.proto.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.proto.PlayerCommandRequest;
import net.earthmc.mycelium.client.redis.RedisKey;
import org.slf4j.Logger;

@Plugin(name = "Mycelium", id = "mycelium", version = "0.0.1", authors = "Warriorrr")
public class VelocityPlatform extends Platform {
    @Inject
    private ProxyServer proxy;

    @Inject
    private Logger logger;

    private MyceliumClient client = MyceliumClient.forPlatform(this).build();

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (this.id().equals(UNKNOWN_ID)) {
            logger.warn("No id has been set with the 'mycelium.id' or 'name' system properties!");
            return;
        }

        final MessagingRegistrar registrar = client.messaging();

        // TODO: debug logging
        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("console-command"), ConsoleCommand.CODEC), incoming -> {
            final ConsoleCommand payload = incoming.data();
            if (payload.command().isEmpty()) {
                return;
            }

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

        try (StatefulRedisConnection<String, String> connection = client.client().connect()) {
            connection.sync().sadd(RedisKey.create(client.network().id(), "proxies"), this.id());
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        try (StatefulRedisConnection<String, String> connection = client.client().connect()) {
            connection.sync().srem(RedisKey.create(client.network().id(), "proxies"), this.id());
        }
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
