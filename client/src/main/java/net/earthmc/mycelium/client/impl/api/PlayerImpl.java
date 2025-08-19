package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessageRecipient;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.model.PlayerCommandRequest;
import net.earthmc.mycelium.client.impl.model.SendMessage;
import net.earthmc.mycelium.client.impl.model.TransferToServer;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
        final String serverId = client.client().hget("m:" + client.network().id() + ":player:" + this.uuid, "server");
        if (serverId == null) {
            return null;
        }

        return client.network().getServerById(serverId);
    }

    @Override
    public @Nullable Proxy proxy() {
        final String proxyId = client.client().hget("m:" + client.network().id() + ":player:" + this.uuid, "proxy");
        if (proxyId == null) {
            return null;
        }

        return client.network().getProxyById(proxyId);
    }

    @Override
    public boolean isOnline() {
        return client.client().sismember("m:" + client.network().id() + ":players", this.uuid.toString());
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
            recipient.message(client.messaging().bind(ChannelIdentifier.identifier("send-message"), SendMessage.CODEC), new SendMessage(this.uuid, message)).send();
        }
    }

    @Override
    public void transferToServer(Server server) {
        final Proxy proxy = proxy();

        if (proxy != null) {
            proxy.message(client.messaging().bind(ChannelIdentifier.identifier("transfer-to-server"), TransferToServer.CODEC), new TransferToServer(this.uuid, server.name())).send();
        }
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
