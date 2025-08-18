package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Platform;
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

    private final MyceliumClient client = MyceliumClient.forPlatform(this).autoregister().nativeProxy(() -> new NativeProxy(this.id(), (MyceliumClient) Mycelium.get(), this)).build();

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

        client.client().sadd(RedisKey.create(client.network().id(), "proxies"), this.id());

        // TODO: cleanup prior data
    }

    @Subscribe(priority = Short.MIN_VALUE / 2)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.client.client().srem(RedisKey.create(client.network().id(), "proxies"), this.id());

        // Clean up after ourselves
        for (final Player player : this.proxy.getAllPlayers()) {
            cleanupPlayerForLogout(player);
        }
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        final UUID uuid = event.getUniqueId();
        final String username = event.getUsername().toLowerCase(Locale.ROOT);

        boolean alreadyLoggedIn;

        if (uuid != null) {
            alreadyLoggedIn = client.client().sismember(RedisKey.create(client, "players"), uuid.toString());
        } else {
            alreadyLoggedIn = client.client().exists(RedisKey.create(client, "name2uuid", username));
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

        client.client().set(RedisKey.create(client, "name2uuid", player.getUsername().toLowerCase(Locale.ROOT)), uuid);
        client.client().sadd(RedisKey.create(client, "proxy", this.id(), "players"), uuid);
        client.client().sadd(RedisKey.create(client, "players"), uuid);

        final String playerHashKey = RedisKey.create(client, "player", uuid);
        client.client().hset(playerHashKey, "name", username);
        client.client().hset(playerHashKey, "proxy", this.id());
    }

    @Subscribe
    public void postPlayerJoinServer(ServerConnectedEvent event) {
        client.client().hset(RedisKey.create(client, "player", event.getPlayer().getUniqueId().toString()), "server", event.getServer().getServerInfo().getName());
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        cleanupPlayerForLogout(event.getPlayer());
    }

    private void cleanupPlayerForLogout(final Player player) {
        // TODO: Ensure that this is not ran when we deny a player from joining due to already being connected.
        client.client().del(RedisKey.create(client, "name2uuid", player.getUsername().toLowerCase(Locale.ROOT)));

        final String uuid = player.getUniqueId().toString();
        client.client().srem(RedisKey.create(client, "proxy", this.id(), "players"), uuid);
        client.client().srem(RedisKey.create(client, "players"), uuid);
        client.client().del(RedisKey.create(client, "player", uuid));
    }

    @Override
    public Type type() {
        return Type.PROXY;
    }
}
