package net.earthmc.mycelium.platform.paper;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.platform.PlatformType;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.client.AbstractPlatform;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.messaging.MessagingRegistrarImpl;
import net.earthmc.mycelium.client.impl.model.PlayerCommandRequest;
import net.earthmc.mycelium.client.impl.model.SendJsonMessage;
import net.earthmc.mycelium.client.impl.model.SendRichMessage;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.util.CompareCondition;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PaperPlatform extends AbstractPlatform implements Listener {
    private final PaperLoader loader;
    private final Logger logger;
    private final Server server;

    private String serversKey;
    private boolean canRemoveServerEntry;

    public PaperPlatform(final PaperLoader loader) {
        this.loader = loader;
        this.logger = loader.getSLF4JLogger();
        this.server = loader.getServer();
    }

    public void enable() {
        if (this.id().equals(UNKNOWN_ID)) {
            throw new IllegalStateException("No id has been set with the 'mycelium.id' or 'name' system properties!");
        }

        this.serversKey = RedisKey.create(loader.client(), "servers");

        final MessagingRegistrar registrar = client().messaging();

        final String lockValue = UUID.randomUUID().toString();
        final String lockKey = RedisKey.create(loader.client(), "lock", "servers", this.id());

        try {
            // acquire lock
            while (redis().set(lockKey, lockValue, SetParams.setParams().nx().ex(10)) == null) {
                // acquiring lock failed, wait random amount of time
                try {
                    // don't need to put a limit on how long we could possibly spend retrying since it's always set with an expiration
                    Thread.sleep(ThreadLocalRandom.current().nextLong(10, 500));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (redis().sismember(serversKey, this.id())) {
                // either another server is using the same key, or this server crashed
                final MessagingRegistrarImpl internalMessaging = (MessagingRegistrarImpl) client().messaging();

                CompletableFuture<Boolean> active = new CompletableFuture<>();
                internalMessaging.internalPlatformMessage(registrar.bind(ChannelIdentifier.identifier("ping"), Codecs.BOOLEAN), true).callback(
                    options -> options.lifetime(Duration.ofSeconds(5)).onExpire(() -> active.complete(false)),
                    response -> active.complete(true)).send();

                boolean otherServerActive = false;
                try {
                    otherServerActive = active.get(5L, TimeUnit.SECONDS);
                } catch (TimeoutException ignored) {
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    throw new IllegalStateException("An exception occurred while checking if another server was still active with id '" + this.id() + "'.", e);
                }

                if (otherServerActive) {
                    throw new IllegalStateException("Another server is currently still running with the id '" + this.id() + "', shut down the other server or change the id of this one to correct this.");
                }
            }

            registerChannels(registrar);

            redis().sadd(serversKey, this.id());
        } finally {
            // attempt to release the lock
            redis().delex(lockKey, CompareCondition.valueEq(lockValue));
        }

        canRemoveServerEntry = true;
    }

    public void registerChannels(final MessagingRegistrar registrar) {

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("ping"), Codecs.BOOLEAN), incoming -> {
            if (incoming.acceptsResponses()) {
                incoming.buildResponse(true).send();
            }
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("console-command"), ConsoleCommand.CODEC), incoming -> {
            final ConsoleCommand payload = incoming.data();
            if (payload.command().isEmpty()) {
                return;
            }

            this.logger.info("Executing console command '{}'.", payload.command());
            this.server.getGlobalRegionScheduler().run(this.loader, task -> {
                this.server.dispatchCommand(this.server.getConsoleSender(), payload.command());
            });
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("player-command"), PlayerCommandRequest.CODEC), incoming -> {
            final PlayerCommandRequest payload = incoming.data();
            final Player player = this.server.getPlayer(payload.playerUUID());
            if (payload.commandLine().isEmpty() || player == null) {
                return;
            }

            this.logger.info("Executing command '{}' as player '{}'.", payload.commandLine(), player.getName());
            player.getScheduler().execute(this.loader, () -> {
                this.server.dispatchCommand(player, payload.commandLine());
            }, null, 1L);
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("send-message"), SendRichMessage.CODEC), incoming -> {
            final Player player = this.server.getPlayer(incoming.data().playerUUID());

            if (player != null) {
                player.sendRichMessage(incoming.data().message());
            }
        });

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("send-json-message"), SendJsonMessage.CODEC), incoming -> {
            final UUID target = incoming.data().target();
            final Component message = GsonComponentSerializer.gson().deserializeFromTree(incoming.data().messageJson());

            if (target == null) {
                server.sendMessage(message);
            } else {
                final Player player = server.getPlayer(target);
                if (player != null) {
                    player.sendMessage(message);
                }
            }
        });

        registrar.registerChannel(registrar.bind(ChannelIdentifier.identifier("send-json-message"), SendJsonMessage.CODEC), incoming -> {
            server.sendMessage(GsonComponentSerializer.gson().deserializeFromTree(incoming.data().messageJson()));
        });
    }

    public void disable() {
        // prevents removing the entry if we have not acquired it properly yet
        if (canRemoveServerEntry) {
            redis().srem(serversKey, this.id());
        }
    }

    public MyceliumClient client() {
        return loader.client();
    }

    public UnifiedJedis redis() {
        return client().redis();
    }

    @Override
    public PlatformType type() {
        return PlatformType.SERVER;
    }

    @Override
    public @Nullable Path dataDirectory() {
        return this.loader.getDataPath();
    }
}
