package net.earthmc.mycelium.client.impl.api;

import ca.spottedleaf.concurrentutil.completable.CallbackCompletable;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.proto.ConsoleCommand;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.messaging.OutgoingMessageBuilderImpl;
import net.earthmc.mycelium.client.redis.RedisKey;

import java.util.UUID;

public class ServerImpl implements Server, PlayerListImpl {
    private final String name;
    private final MyceliumClient client;

    public ServerImpl(final String name, final MyceliumClient client) {
        this.name = name;
        this.client = client;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public void runConsoleCommand(ConsoleCommand command) {

    }

    @Override
    public String playerSetKey() {
        return RedisKey.create(client.network().id(), "server", this.name, "players");
    }

    @Override
    public MyceliumClient client() {
        return this.client;
    }

    @Override
    public <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, UUID.randomUUID().toString(), RedisKey.create(this.client.network().id(), "server", this.name, "channels", identifier.channel()), true, data, null);
    }

    @Override
    public <T> OutgoingMessageBuilder<CallbackCompletable<Boolean>, T> message(ChannelIdentifier.Bound<T> identifier, T data) {
        return new OutgoingMessageBuilderImpl<>(this.client, UUID.randomUUID().toString(), RedisKey.create(this.client.network().id(), "server", this.name, "channels", identifier.channel()), true, data, identifier.codec());
    }
}
