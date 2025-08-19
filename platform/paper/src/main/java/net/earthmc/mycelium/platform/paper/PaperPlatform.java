package net.earthmc.mycelium.platform.paper;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.model.PlayerCommandRequest;
import net.earthmc.mycelium.client.impl.model.SendMessage;
import net.earthmc.mycelium.client.redis.RedisKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import redis.clients.jedis.UnifiedJedis;

public class PaperPlatform extends Platform implements Listener {
    private final PaperLoader loader;
    private final Logger logger;
    private final Server server;

    public PaperPlatform(final PaperLoader loader) {
        this.loader = loader;
        this.logger = loader.getSLF4JLogger();
        this.server = loader.getServer();
    }

    public void enable() {
        if (this.id().equals(UNKNOWN_ID)) {
            throw new IllegalStateException("No id has been set with the 'mycelium.id' or 'name' system properties!");
        }

        final MessagingRegistrar registrar = client().messaging();

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

        registrar.registerPlatformChannel(registrar.bind(ChannelIdentifier.identifier("send-message"), SendMessage.CODEC), incoming -> {
            final Player player = this.server.getPlayer(incoming.data().playerUUID());

            if (player != null) {
                player.sendRichMessage(incoming.data().message());
            }
        });

        redis().sadd(RedisKey.create(client().network().id(), "servers"), this.id());
    }

    public void disable() {
        redis().srem(RedisKey.create(client().network().id(), "servers"), this.id());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // TODO: does desync need to be checked for?
        redis().hset(RedisKey.create(client(), "player", event.getPlayer().getUniqueId().toString()), "server", this.id());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        redis().hdel(RedisKey.create(client(), "player", event.getPlayer().getUniqueId().toString()), "server");
    }

    public MyceliumClient client() {
        return loader.client();
    }

    public UnifiedJedis redis() {
        return client().redis();
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }
}
