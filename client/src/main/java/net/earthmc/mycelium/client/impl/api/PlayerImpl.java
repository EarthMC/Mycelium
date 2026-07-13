package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessageRecipient;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.api.network.player.ServerTransferResult;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.model.KickPlayer;
import net.earthmc.mycelium.client.impl.model.PlayerCommandRequest;
import net.earthmc.mycelium.client.impl.model.SendJsonMessage;
import net.earthmc.mycelium.client.impl.model.SendRichMessage;
import net.earthmc.mycelium.client.impl.model.ServerTransferResultImpl;
import net.earthmc.mycelium.client.impl.model.TransferToServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerImpl implements Player {
    private final String username;
    private final UUID uuid;
    private final MyceliumClient client;

    public PlayerImpl(String username, UUID uuid, final MyceliumClient client) {
        this.username = username;
        this.uuid = uuid;
        this.client = client;
    }

    @Override
    public String username() {
        return this.username;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public @Nullable Server server() {
        final String serverId = client.redis().hget("m:" + client.network().id() + ":player:" + this.uuid, "server");
        if (serverId == null) {
            return null;
        }

        return client.network().getServerById(serverId);
    }

    @Override
    public @Nullable Proxy proxy() {
        final String proxyId = client.redis().hget("m:" + client.network().id() + ":player:" + this.uuid, "proxy");
        if (proxyId == null) {
            return null;
        }

        return client.network().getProxyById(proxyId);
    }

    @Override
    public boolean isOnline() {
        return client.redis().sismember("m:" + client.network().id() + ":players", this.uuid.toString());
    }

    @Override
    public void runCommand(Command command) {
        MessageRecipient target = null;

        if (command.target() == Command.Target.BACKEND) {
            target = this.server();
        } else if (command.target() == Command.Target.PROXY) {
            target = this.proxy();
        }

        if (target == null) {
            return;
        }

        final PlayerCommandRequest request = new PlayerCommandRequest(this.uuid, command.command());
        target.message(client.messaging().bind(ChannelIdentifier.identifier("player-command"), PlayerCommandRequest.CODEC), request).send();
    }

    @Override
    public void sendRichMessage(String message) {
        final MessageRecipient recipient = Optional.ofNullable((MessageRecipient) proxy()).orElseGet(this::server);

        if (recipient != null) {
            recipient.message(client.messaging().bind(ChannelIdentifier.identifier("send-message"), SendRichMessage.CODEC), new SendRichMessage(this.uuid, message)).send();
        }
    }

    @Override
    public void sendMessage(@NotNull final Component message) {
        final MessageRecipient recipient = Optional.ofNullable((MessageRecipient) proxy()).orElseGet(this::server);

        if (recipient != null) {
            recipient.message(client.messaging().bind(ChannelIdentifier.identifier("send-json-message"), SendJsonMessage.CODEC), new SendJsonMessage(this.uuid, GsonComponentSerializer.gson().serializeToTree(message))).send();
        }
    }

    @Override
    public CompletableFuture<ServerTransferResult> transferToServer(Server server) {
        return transferToServer(server.name());
    }

    @Override
    public CompletableFuture<ServerTransferResult> transferToServer(final String serverName) {
        final Proxy proxy = proxy();

        if (proxy != null) {
            CompletableFuture<ServerTransferResult> result = new CompletableFuture<>();

            proxy.message(client.messaging().bind(ChannelIdentifier.identifier("transfer-to-server"), TransferToServer.CODEC), new TransferToServer(this.uuid, serverName))
                .callback(options -> options.lifetime(Duration.ofSeconds(60)).onExpire(() -> result.complete(new ServerTransferResultImpl(false, Component.text("Connection timed out after 60 seconds.", NamedTextColor.RED)))), JsonCodec.simple(ServerTransferResultImpl.class), incoming -> {
                    result.complete(incoming.data());
                })
                .send();

            return result;
        } else {
            return CompletableFuture.completedFuture(new ServerTransferResultImpl(false, Component.text("You are not connected to a proxy", NamedTextColor.RED)));
        }
    }

    @Override
    public CompletableFuture<Boolean> kick(final Component reason) {
        MessageRecipient recipient = proxy();
        if (recipient == null) {
            recipient = server();
        }

        if (recipient == null) {
            return CompletableFuture.completedFuture(false);
        }

        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        recipient.message(client.messaging().bind(ChannelIdentifier.identifier("kick-player"), KickPlayer.CODEC), new KickPlayer(this.uuid, reason))
            .callback(options -> options.lifetime(Duration.ofSeconds(10)).onExpire(() -> future.complete(false)), Codecs.BOOLEAN, incoming -> {
                future.complete(incoming.data());
            })
            .send();

        return future;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerImpl player)) return false;
        return Objects.equals(username, player.username) && Objects.equals(uuid, player.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, uuid);
    }
}
