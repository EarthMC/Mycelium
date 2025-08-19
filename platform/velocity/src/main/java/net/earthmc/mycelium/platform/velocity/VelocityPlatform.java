package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.model.PlayerCommandRequest;
import net.earthmc.mycelium.client.impl.model.SendMessage;
import net.earthmc.mycelium.client.impl.model.TransferToServer;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.earthmc.mycelium.platform.velocity.impl.NativeProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.UUID;

@Plugin(name = "Mycelium", id = "mycelium", version = "0.0.1", authors = "Warriorrr")
public class VelocityPlatform extends Platform {
    @Inject
    public ProxyServer proxy;

    @Inject
    public Logger logger;

    private final MyceliumClient client = MyceliumClient.forPlatform(this).autoregister().nativeProxy(client -> new NativeProxy(this.id(), client, this)).build();

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (this.id().equals(UNKNOWN_ID)) {
            logger.error("No id has been set with the 'mycelium.id' or 'name' system properties!");
            return;
        }

        final MessagingRegistrar registrar = client.messaging();

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("console-command"), ConsoleCommand.CODEC), incoming -> {
            final ConsoleCommand payload = incoming.data();
            if (payload.command().isEmpty()) {
                return;
            }

            this.logger.info("Executing console command '{}'.", payload.command());
            this.proxy.getCommandManager().executeAsync(this.proxy.getConsoleCommandSource(), payload.command());
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("player-command"), PlayerCommandRequest.CODEC), incoming -> {
            final PlayerCommandRequest payload = incoming.data();
            final Player player = this.proxy.getPlayer(payload.playerUUID()).orElse(null);
            if (payload.commandLine().isEmpty() || player == null) {
                return;
            }

            this.logger.info("Executing command '{}' as player '{}'.", payload.commandLine(), player.getUsername());
            this.proxy.getCommandManager().executeAsync(player, payload.commandLine());
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("send-message"), SendMessage.CODEC), incoming -> this.proxy.getPlayer(incoming.data().playerUUID()).ifPresent(player -> player.sendRichMessage(incoming.data().message())));

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("transfer-to-server"), TransferToServer.CODEC), incoming -> {
            final Player player = this.proxy.getPlayer(incoming.data().playerUUID()).orElse(null);
            final RegisteredServer target = this.proxy.getServer(incoming.data().serverName()).orElse(null);

            if (player != null && target != null) {
                player.createConnectionRequest(target).fireAndForget();
            }
        });

        client.redis().sadd(RedisKey.create(client, "proxies"), this.id());

        // TODO: cleanup prior data
    }

    @Subscribe(priority = Short.MIN_VALUE / 2)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.client.redis().srem(RedisKey.create(client.network().id(), "proxies"), this.id());

        // Clean up after ourselves
        for (final Player player : this.proxy.getAllPlayers()) {
            cleanupPlayerForLogout(player);
        }

        this.client.close();
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        final String username = event.getUsername().toLowerCase(Locale.ROOT);

        boolean alreadyLoggedIn = false;

        if (uuid == null) {
            final String uuidString = client.redis().get(RedisKey.create(client, "name2uuid", username));

            if (uuidString != null) {
                uuid = UUID.fromString(uuidString);
                alreadyLoggedIn = true;
            }
        }

        if (uuid != null) {
            final String uuidString = uuid.toString();

            final String proxyId = client.redis().hget(RedisKey.create(client, "player", uuidString), "proxy");
            if (this.id().equals(proxyId) && proxy.getPlayer(uuid).isEmpty()) {
                // player is considered to still be on this proxy so something went wrong, clean up data
                cleanupPlayer(username, uuidString);
                return;
            }

            alreadyLoggedIn = client.redis().sismember(RedisKey.create(client, "players"), uuidString);
        }

        if (alreadyLoggedIn) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("You are already connected to this server.", NamedTextColor.RED)));
        }
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        final Player player = event.getPlayer();
        final String uuid = player.getUniqueId().toString();
        final String username = player.getUsername().toLowerCase(Locale.ROOT);

        // todo: check if player doesn't exist to prevent any race conditions

        client.redis().set(RedisKey.create(client, "name2uuid", player.getUsername().toLowerCase(Locale.ROOT)), uuid);
        client.redis().sadd(RedisKey.create(client, "proxy", this.id(), "players"), uuid);
        client.redis().sadd(RedisKey.create(client, "players"), uuid);

        final String playerHashKey = RedisKey.create(client, "player", uuid);
        client.redis().hset(playerHashKey, "name", player.getUsername());
        client.redis().hset(playerHashKey, "proxy", this.id());
    }

    @Subscribe
    public void postPlayerJoinServer(ServerConnectedEvent event) {
        final String serverName = event.getServer().getServerInfo().getName().toLowerCase(Locale.ROOT);
        final String uuid = event.getPlayer().getUniqueId().toString();

        client.redis().hset(RedisKey.create(client, "player", uuid), "server", serverName);
        client.redis().sadd(RedisKey.create(client, "server", serverName, "players"), uuid);

        event.getPreviousServer().ifPresent(previous -> {
            client.redis().srem(RedisKey.create(client, "server", previous.getServerInfo().getName().toLowerCase(Locale.ROOT), "players"), uuid);
        });
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        cleanupPlayerForLogout(event.getPlayer());
    }

    @Subscribe(priority = Short.MAX_VALUE / 2)
    public void onProxyPing(ProxyPingEvent event) {
        event.setPing(event.getPing().asBuilder().onlinePlayers(client.network().playerCount()).build());
    }

    private void cleanupPlayerForLogout(final Player player) {
        cleanupPlayer(player.getUsername(), player.getUniqueId().toString());
    }

    private void cleanupPlayer(final String username, final String uuid) {
        // TODO: Ensure that this is not ran when we deny a player from joining due to already being connected.
        client.redis().del(RedisKey.create(client, "name2uuid", username.toLowerCase(Locale.ROOT)));

        client.redis().srem(RedisKey.create(client, "proxy", this.id(), "players"), uuid);
        client.redis().srem(RedisKey.create(client, "players"), uuid);
        client.redis().del(RedisKey.create(client, "player", uuid));

        for (final Server server : client.network().servers()) {
            // remove uuid from the player list of each server
            client.redis().srem(RedisKey.create(client, "server", server.name(), "players"), uuid);
        }
    }

    @Override
    public Type type() {
        return Type.PROXY;
    }
}
