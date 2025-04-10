package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.UUID;

@Plugin(name = "Mycelium", id = "mycelium", version = "0.0.1", authors = "Warriorrr")
public class VelocityPlatform extends Platform {
    @Inject
    private ProxyServer proxy;

    @Inject
    private Logger logger;

    private final MyceliumClient client = MyceliumClient.forPlatform(this).autoregister().build();

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
    public void onPreLogin(PreLoginEvent event) {
        final UUID uuid = event.getUniqueId();
        final String username = event.getUsername().toLowerCase(Locale.ROOT);

        try (StatefulRedisConnection<String, String> connection = client.client().connect()) {
            boolean alreadyLoggedIn = false;

            if (uuid != null) {
                alreadyLoggedIn = connection.sync().sismember(RedisKey.create(client.network().id(), "players"), uuid.toString());
            } else {
                alreadyLoggedIn = connection.sync().exists(RedisKey.create(client.network().id(), "name2uuid", username)) == 1;
            }

            if (alreadyLoggedIn) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("You are already connected to this network.", NamedTextColor.RED)));
                return;
            }
        }
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        final String uuid = event.getPlayer().getUniqueId().toString();
        final String username = event.getPlayer().getUsername().toLowerCase(Locale.ROOT);

        try (StatefulRedisConnection<String, String> connection = client.client().connect()) {
            // todo: check if player doesn't exist to prevent any race conditions, then enter data
        }
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        final Player player = event.getPlayer();

        // TODO: Ensure that this is not ran when we deny a player from joining due to already being connected.
        try (StatefulRedisConnection<String, String> connection = client.client().connect()) {
            connection.sync().del(RedisKey.create(client.network().id(), "name2uuid", player.getUsername().toLowerCase(Locale.ROOT)));

            // TODO: nicer way to handle keys?
            final String uuid = player.getUniqueId().toString();
            connection.sync().srem(RedisKey.create(client.network().id(), "proxy", this.id(), "players"), uuid);
            connection.sync().srem(RedisKey.create(client.network().id(), "players"), uuid);
            connection.sync().del(RedisKey.create(client.network().id(), "player", uuid));
        }
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
