package net.earthmc.mycelium.client;

import io.lettuce.core.RedisURI;
import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.MyceliumProvider;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.client.impl.api.NetworkImpl;
import net.earthmc.mycelium.client.impl.messaging.CallbackProvider;
import net.earthmc.mycelium.client.impl.messaging.MessagingRegistrarImpl;
import net.earthmc.mycelium.client.redis.api.RedisClient;
import net.earthmc.mycelium.client.redis.impl.RedisClientWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.UUID;

public class MyceliumClient implements Mycelium, Closeable {
    private final Logger logger = LoggerFactory.getLogger(MyceliumClient.class);

    private final RedisClient client;

    private final MessagingRegistrarImpl messagingRegistrar;
    private final CallbackProvider callbackProvider;
    private final NetworkImpl network;
    private final Platform platform;

    private final String clientId = UUID.randomUUID().toString();

    protected MyceliumClient(final String redisURI, final Platform platform) {
        this.client = new RedisClientWrapper(io.lettuce.core.RedisClient.create(RedisURI.create(redisURI)));
        this.platform = platform;

        this.network = new NetworkImpl(platform.environment(), this);
        this.messagingRegistrar = new MessagingRegistrarImpl(this);
        this.callbackProvider = new CallbackProvider(this);
    }

    public RedisClient client() {
        return this.client;
    }

    public String clientId() {
        return this.clientId;
    }

    public static Builder standalone() {
        return new Builder(new StandalonePlatform());
    }

    public static Builder forPlatform(final Platform platform) {
        return new Builder(platform);
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
        return this.platform;
    }

    public CallbackProvider callbacks() {
        return this.callbackProvider;
    }

    @Override
    public void close() {
        this.messagingRegistrar.shutdown();
    }

    public Logger logger() {
        return this.logger;
    }

    public static class Builder {
        private final Platform platform;

        private boolean registerInstance = false;
        private String redisURI = "redis://localhost:6379/";

        private Builder(final Platform platform) {
            this.platform = platform;
        }

        /**
         * Enables the instance automatically being registered with the static provider upon creation.
         * @return {@code this}
         */
        public Builder autoregister() {
            this.registerInstance = true;
            return this;
        }

        // TODO: methods for connection pooling, clustering/sentinel

        public MyceliumClient build() {
            final MyceliumClient client = new MyceliumClient(this.redisURI, this.platform);

            if (this.registerInstance) {
                MyceliumProvider.register(client);
            }

            return client;
        }
    }
}
