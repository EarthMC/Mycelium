package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.proto.ConsoleCommand;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.messaging.OutgoingMessageBuilderImpl;
import net.earthmc.mycelium.client.redis.RedisKey;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ProxyImpl implements Proxy, PlayerListImpl {
    public static final JsonCodec<Proxy> CODEC = JsonCodec.simple();
    private final String id;
    private final MyceliumClient client;

    public ProxyImpl(final String id, final MyceliumClient client) {
        this.id = id;
        this.client = client;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void runConsoleCommand(ConsoleCommand command) {
        message(client.messaging().bind(ChannelIdentifier.identifier("console-command"), ConsoleCommand.CODEC), command).send();
    }

    @Override
    public String playerSetKey() {
        return RedisKey.create(client.network().id(), "proxy", this.id, "players");
    }

    @Override
    public MyceliumClient client() {
        return this.client;
    }

    @Override
    public <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, UUID.randomUUID().toString(), RedisKey.create(this.client.network().id(), "proxy", this.id, "channels", identifier.channel()), true, data, null);
    }

    @Override
    public <T> OutgoingMessageBuilder<CompletableFuture<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, UUID.randomUUID().toString(), RedisKey.create(this.client.network().id(), "proxy", this.id, "channels", identifier.channel()), true, data, identifier.codec());
    }
}
