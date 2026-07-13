package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.platform.PlatformType;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.client.AbstractPlatform;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.event.type.player.PlayerJoinedServerEvent;
import net.earthmc.mycelium.client.impl.model.KickPlayer;
import net.earthmc.mycelium.client.impl.model.PlayerCommandRequest;
import net.earthmc.mycelium.client.impl.model.SendJsonMessage;
import net.earthmc.mycelium.client.impl.model.SendRichMessage;
import net.earthmc.mycelium.client.impl.model.ServerTransferResultImpl;
import net.earthmc.mycelium.client.impl.model.TransferToServer;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.earthmc.mycelium.platform.velocity.impl.NativeProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.SetParams;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Plugin(name = "Mycelium", id = "mycelium", version = BuildConstants.VERSION, authors = "Warriorrr")
public class VelocityPlatform extends AbstractPlatform {
    @Inject
    public ProxyServer proxy;

    @Inject
    public Logger logger;

    @Inject
    @DataDirectory
    public Path dataDirectory;

    private final MyceliumClient client = MyceliumClient.forPlatform(this).autoregister().nativeProxy(client -> new NativeProxy(this.id(), client, this)).build();

    private int playerCount = 0;
    private ScheduledTask playerCountUpdateTask = null;

    private final String proxyPlayersKey = RedisKey.create(client, "proxy", this.id(), "players");
    private final String networkPlayersKey = RedisKey.create(client, "players");

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

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("send-message"), SendRichMessage.CODEC), incoming -> this.proxy.getPlayer(incoming.data().playerUUID()).ifPresent(player -> player.sendRichMessage(incoming.data().message())));

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("transfer-to-server"), TransferToServer.CODEC), incoming -> {
            final Player player = this.proxy.getPlayer(incoming.data().playerUUID()).orElse(null);
            final RegisteredServer target = this.proxy.getServer(incoming.data().serverName()).orElse(null);

            if (player != null && target != null) {
                if (incoming.acceptsResponses()) {
                    player.createConnectionRequest(target).connect().thenAccept(result -> {
                        Component failureMessage = result.getReasonComponent().orElse(null);
                        if (!result.isSuccessful() && failureMessage == null) {
                            failureMessage = Component.text("ERR_" + result.getStatus(), NamedTextColor.RED);
                        }

                        incoming.buildResponse(new ServerTransferResultImpl(result.isSuccessful(), failureMessage)).send();
                    }).exceptionally(throwable -> {
                        incoming.buildResponse(new ServerTransferResultImpl(false, Component.text("Connection failed with an exception: " + throwable.getMessage(), NamedTextColor.RED))).send();
                        return null;
                    });
                } else {
                    player.createConnectionRequest(target).fireAndForget();
                }
            } else if (incoming.acceptsResponses()) {
                incoming.buildResponse(new ServerTransferResultImpl(false, Component.text("The player or target server was not found on this proxy instance.", NamedTextColor.RED))).send();
            }
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("send-json-message"), SendJsonMessage.CODEC), incoming -> {
            final UUID target = incoming.data().target();
            final Component message = GsonComponentSerializer.gson().deserializeFromTree(incoming.data().messageJson());

            if (target == null) {
                proxy.sendMessage(message);
            } else {
                proxy.getPlayer(target).ifPresent(player -> player.sendMessage(message));
            }
        });

        registrar.registerChannel(registrar.bind(ChannelIdentifier.identifier("send-json-message"), SendJsonMessage.CODEC), incoming -> {
            proxy.sendMessage(GsonComponentSerializer.gson().deserializeFromTree(incoming.data().messageJson()));
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("kick-player"), KickPlayer.CODEC), incoming -> {
            incoming.buildResponse(Codecs.BOOLEAN, proxy.getPlayer(incoming.data().target())
                    .map(player -> {
                        player.disconnect(incoming.data().reason());
                        return true;
                    }).orElse(false))
                .send();
        });

        client.redis().sadd(RedisKey.create(client, "proxies"), this.id());

        // Removes stale data for players who are still considered to be on this proxy and currently offline
        this.cleanupStalePlayersOnProxy(false);

        // Re-initializes the data for other players who are online, in the case of this plugin being loaded with players already online somehow.
        if (proxy.getPlayerCount() > 0) {
            try (final AbstractPipeline pipe = client.redis().pipelined()) {
                for (final Player player : proxy.getAllPlayers()) {
                    this.initializePlayerData(player, pipe);
                }
            }
        }

        // Periodically update player count, until a better supplier is added
        this.playerCountUpdateTask = this.proxy.getScheduler().buildTask(this, () -> this.playerCount = client.network().playerCount())
                .repeat(Duration.ofSeconds(3))
                .schedule();
    }

    @Subscribe(priority = Short.MIN_VALUE / 2)
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (this.playerCountUpdateTask != null) {
            this.playerCountUpdateTask.cancel();
            this.playerCountUpdateTask = null;
        }

        this.client.redis().srem(RedisKey.create(client.network().id(), "proxies"), this.id());

        final Collection<String> servers = client.network().servers().stream().map(Server::name).toList();

        // Clean up after ourselves
        if (this.proxy.getPlayerCount() > 0) {
            try (final AbstractPipeline pipe = client.redis().pipelined()) {
                for (final Player player : this.proxy.getAllPlayers()) {
                    cleanupPlayer(player.getUsername(), player.getUniqueId().toString(), servers, pipe);
                }
            }
        }

        this.cleanupStalePlayersOnProxy(true);

        this.client.close();

        logger.info("Mycelium v{} has been disabled.", BuildConstants.VERSION);
    }

    @Subscribe
    public EventTask onPreLogin(PreLoginEvent event) {
        String uuid = event.getUniqueId() != null ? event.getUniqueId().toString() : null;
        final String username = event.getUsername().toLowerCase(Locale.ROOT);

        boolean alreadyLoggedIn = false;

        if (uuid == null) {
            uuid = client.redis().get(RedisKey.create(client, "name2uuid", username));

            if (uuid != null) {
                alreadyLoggedIn = true;
            }
        }

        if (uuid != null) {
            final String proxyId = client.redis().hget(RedisKey.create(client, "player", uuid), "proxy");
            if (this.id().equals(proxyId) && proxy.getPlayer(UUID.fromString(uuid)).isEmpty()) {
                // player is considered to still be on this proxy, clean up stale data
                cleanupPlayer(username, uuid);
                return null;
            }

            alreadyLoggedIn |= client.redis().sismember(RedisKey.create(client, "players"), uuid);
        }

        if (!alreadyLoggedIn) {
            return null;
        }

        final net.earthmc.mycelium.api.network.Player player = client.network().getPlayerByUUID(UUID.fromString(uuid));
        if (player == null) {
            // player data is in an inconsistent state, since the player is both part of the set and null when we look for it.
            if (proxy.getPlayer(UUID.fromString(uuid)).isPresent()) {
                // let the proxy decide what to do with this duplicate login
                // if kicked, hopefully the disconnect event where we clean up data is run before the post login event where we also check if they're already logged in. At worst the player just needs to relog.
                // if not kicked, the data will remain untouched & cleaned up when the currently online player leaves.
                return null;
            } else {
                // not online on this proxy and inconsistent data
                cleanupPlayer(username, uuid);
                return null;
            }
        }

        // TODO: read velocity config for whether to kick the existing player or prevent login

        logger.info("Disconnecting {} for logging in from another location.", username);

        final String finalUuid = uuid;
        return EventTask.resumeWhenComplete(player.kick(Component.text(this.id() + ": ", NamedTextColor.RED).append(Component.translatable("multiplayer.disconnect.duplicate_login")))
            .thenAccept(kicked -> {
                if (!kicked) {
                    logger.warn("Failed to kick already logged in player {} ({}), rejecting login.", username, finalUuid);
                    event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(this.id() + ": You are already connected to this server.", NamedTextColor.RED)));
                    return;
                }

                // Extra cleanup in case the proxy the player got kicked from hasn't gotten to it yet.
                cleanupPlayer(username, finalUuid);
            }));
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        final Player player = event.getPlayer();
        final String uuid = player.getUniqueId().toString();
        final String username = player.getUsername().toLowerCase(Locale.ROOT);

        // Race condition check: kick the player if they connected to another proxy during login
        if (client.redis().set(RedisKey.create(client, "name2uuid", username), uuid, SetParams.setParams().nx()) == null) {
            player.disconnect(Component.text(this.id() + ": ", NamedTextColor.RED).append(Component.translatable("multiplayer.disconnect.duplicate_login")));
            return;
        }

        // TODO: if something external goes wrong between the setnx and the pipeline it could cause a desync
        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            initializePlayerData(player, pipe);
        }
    }

    public void initializePlayerData(final Player player, final AbstractPipeline pipe) {
        final String uuid = player.getUniqueId().toString();

        pipe.sadd(proxyPlayersKey, uuid);
        pipe.sadd(networkPlayersKey, uuid);

        final String playerHashKey = RedisKey.create(client, "player", uuid);
        pipe.hset(playerHashKey, "name", player.getUsername());
        pipe.hset(playerHashKey, "proxy", this.id());

        player.getCurrentServer().ifPresent(server -> pipe.sadd(RedisKey.create(client, "server", server.getServerInfo().getName().toLowerCase(Locale.ROOT), "players"), uuid));
    }

    @Subscribe
    public void postPlayerJoinServer(ServerConnectedEvent event) {
        final String serverName = event.getServer().getServerInfo().getName().toLowerCase(Locale.ROOT);
        final String uuid = event.getPlayer().getUniqueId().toString();

        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            pipe.hset(RedisKey.create(client, "player", uuid), "server", serverName);
            pipe.sadd(RedisKey.create(client, "server", serverName, "players"), uuid);

            event.getPreviousServer().ifPresent(previous -> {
                pipe.srem(RedisKey.create(client, "server", previous.getServerInfo().getName().toLowerCase(Locale.ROOT), "players"), uuid);
            });
        }

        final net.earthmc.mycelium.api.network.Player player = client.network().getPlayerByUUID(event.getPlayer().getUniqueId());
        final Server server = client.network().getServerById(serverName);

        if (player == null || server == null) {
            logger.debug("Not calling PlayerJoinedServerEvent for {} because their player or server instance is null.", event.getPlayer().getUsername());
            return;
        }

        final Server previousServer = event.getPreviousServer().map(s -> client.network().getServerById(s.getServerInfo().getName().toLowerCase(Locale.ROOT))).orElse(null);
        client.events().broadcastEvent(new PlayerJoinedServerEvent(player, previousServer, server));
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        if (event.getLoginStatus() == DisconnectEvent.LoginStatus.CONFLICTING_LOGIN) {
            return;
        }

        cleanupPlayerForLogout(event.getPlayer());
    }

    @Subscribe(priority = Short.MIN_VALUE / 2)
    public void onProxyPing(ProxyPingEvent event) {
        event.setPing(event.getPing().asBuilder().onlinePlayers(this.playerCount).build());
    }

    private void cleanupPlayerForLogout(final Player player) {
        cleanupPlayer(player.getUsername(), player.getUniqueId().toString());
    }

    private void cleanupPlayer(final String username, final String uuid) {
        final Collection<String> servers = client.network().servers().stream().map(Server::name).toList();

        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            cleanupPlayer(username, uuid, servers, pipe);
        }
    }

    private void cleanupPlayer(final String username, final String uuid, final Collection<String> servers, final AbstractPipeline pipe) {
        pipe.del(RedisKey.create(client, "name2uuid", username.toLowerCase(Locale.ROOT)));

        pipe.srem(proxyPlayersKey, uuid);
        pipe.srem(networkPlayersKey, uuid);
        pipe.del(RedisKey.create(client, "player", uuid));

        for (final String server : servers) {
            // remove uuid from the player list of each server
            pipe.srem(RedisKey.create(client, "server", server, "players"), uuid);
        }
    }

    private void cleanupStalePlayersOnProxy(final boolean shuttingDown) {
        // The set of servers from Network#servers won't contain servers that aren't on or don't have mycelium installed on the backend
        final Collection<String> serversWithPlayers = client.redis().keys(RedisKey.create(client, "server", "*", "players"));

        final Collection<String> proxyPlayerUUIDs = client.redis().smembers(proxyPlayersKey);

        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            Map<String, Response<List<String>>> playerData = new HashMap<>();

            for (final String uuid : proxyPlayerUUIDs) {
                final Response<List<String>> fields = pipe.hmget(RedisKey.create(client, "player", uuid), "name", "proxy");

                playerData.put(uuid, fields);
            }

            pipe.sync();

            // responses should be gettable now
            for (final Map.Entry<String, Response<List<String>>> entry : playerData.entrySet()) {
                final String uuid = entry.getKey();
                final List<String> fields = entry.getValue().get();

                if (fields.size() != 2) {
                    pipe.srem(proxyPlayersKey, uuid);
                    continue;
                }

                final String name = fields.get(0);
                final String proxy = fields.get(1);

                if (this.id().equals(proxy)) {
                    if (shuttingDown || this.proxy.getPlayer(name).isEmpty()) {
                        // proxy still points to this one despite player not being online, meaning this is stale data
                        cleanupPlayer(name, uuid, serversWithPlayers, pipe);
                    }
                } else {
                    // player is on another proxy but still part of this proxy's set
                    pipe.srem(proxyPlayersKey, uuid);
                }
            }
        }
    }

    @Override
    public PlatformType type() {
        return PlatformType.PROXY;
    }

    @Override
    public @Nullable Path dataDirectory() {
        return this.dataDirectory;
    }
}
