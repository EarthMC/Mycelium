package net.earthmc.mycelium.client.impl.api;

import io.lettuce.core.api.StatefulRedisConnection;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessageRecipient;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.proto.PlayerCommandRequest;
import org.jspecify.annotations.Nullable;

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
        try (final StatefulRedisConnection<String, String> connection = client.client().connect()) {
            final String serverId = connection.sync().hget("m:" + client.network().id() + ":player:" + this.uuid, "server");
            if (serverId == null) {
                return null;
            }

            return client.network().getServerById(serverId);
        }
    }

    @Override
    public @Nullable Proxy proxy() {
        try (final StatefulRedisConnection<String, String> connection = client.client().connect()) {
            final String proxyId = connection.sync().hget("m:" + client.network().id() + ":player:" + this.uuid, "proxy");
            if (proxyId == null) {
                return null;
            }

            return client.network().getProxyById(proxyId);
        }
    }

    @Override
    public boolean isOnline() {
        try (final StatefulRedisConnection<String, String> connection = client.client().connect()) {
            return connection.sync().sismember("m:" + client.network().id() + ":players", this.uuid.toString());
        }
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

    }

    @Override
    public void transferToServer(Server server) {

    }
}
