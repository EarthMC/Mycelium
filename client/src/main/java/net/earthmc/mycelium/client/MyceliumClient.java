package net.earthmc.mycelium.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.client.impl.api.NetworkImpl;
import net.earthmc.mycelium.client.impl.messaging.CallbackProvider;
import net.earthmc.mycelium.client.impl.messaging.MessagingRegistrarImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.UUID;

public class MyceliumClient implements Mycelium, Closeable {
    private final Logger logger = LoggerFactory.getLogger(MyceliumClient.class);
    private String networkId = "prod";

    private final RedisClient client;

    private final MessagingRegistrarImpl messagingRegistrar;
    private final CallbackProvider callbackProvider;
    private final NetworkImpl network;

    private final String clientId = UUID.randomUUID().toString();

    protected MyceliumClient(String redisURI) {
        this.client = RedisClient.create(RedisURI.create(redisURI));
        this.messagingRegistrar = new MessagingRegistrarImpl(this);
        this.callbackProvider = new CallbackProvider(this);
        this.network = new NetworkImpl(networkId, this);
    }

    public RedisClient client() {
        return this.client;
    }

    public String clientId() {
        return this.clientId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Network network() {
        return this.network;
    }

    @Override
    public MessagingRegistrar messaging() {
        return this.messagingRegistrar;
    }

    @Override
    public Platform platform() {
        return null;
    }

    public CallbackProvider callbacks() {
        return this.callbackProvider;
    }

    @Override
    public void close() {
        this.messagingRegistrar.shutdown();
    }

    public static class Builder {
        private String redisURI = "redis://localhost:6379/";

        public MyceliumClient build() {
            return new MyceliumClient(this.redisURI);
        }
    }
}
