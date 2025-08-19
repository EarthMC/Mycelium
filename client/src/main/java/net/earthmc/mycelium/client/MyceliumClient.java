package net.earthmc.mycelium.client;

import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.MyceliumProvider;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.client.impl.api.NetworkImpl;
import net.earthmc.mycelium.client.impl.messaging.CallbackProvider;
import net.earthmc.mycelium.client.impl.messaging.MessagingRegistrarImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;

import java.io.Closeable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class MyceliumClient implements Mycelium, Closeable {
    private final Logger logger = LoggerFactory.getLogger(MyceliumClient.class);

    private final UnifiedJedis redisClient;

    private final MessagingRegistrarImpl messagingRegistrar;
    private final CallbackProvider callbackProvider;
    private final NetworkImpl network;
    private final Platform platform;

    private final String clientId = UUID.randomUUID().toString();

    protected MyceliumClient(final String redisURI, final Platform platform) {
        this.redisClient = new JedisPooled(redisURI);
        this.platform = platform;

        this.network = new NetworkImpl(platform.environment(), this);
        this.messagingRegistrar = new MessagingRegistrarImpl(this);
        this.callbackProvider = new CallbackProvider(this);
    }

    public UnifiedJedis redis() {
        return this.redisClient;
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
        this.callbackProvider.close();
        this.redisClient.close();
    }

    public Logger logger() {
        return this.logger;
    }

    public static class Builder {
        private final Platform platform;

        private boolean registerInstance = false;
        private String redisURI = "redis://localhost:6379/";
        private Function<MyceliumClient, Server> nativeServer = client -> null;
        private Function<MyceliumClient, Proxy> nativeProxy = client -> null;

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

        public Builder nativeServer(final Function<MyceliumClient, Server> nativeServer) {
            Objects.requireNonNull(nativeServer, "supplier may not be null");
            this.nativeServer = nativeServer;
            return this;
        }

        public Builder nativeProxy(final Function<MyceliumClient, Proxy> nativeProxy) {
            Objects.requireNonNull(nativeProxy, "supplier may not be null");
            this.nativeProxy = nativeProxy;
            return this;
        }

        // TODO: methods for connection pooling, clustering/sentinel

        public MyceliumClient build() {
            final MyceliumClient client = new MyceliumClient(this.redisURI, this.platform);

            if (this.registerInstance) {
                MyceliumProvider.register(client);
            }

            client.network.setNativeProxy(this.nativeProxy.apply(client));
            client.network.setNativeServer(this.nativeServer.apply(client));

            return client;
        }
    }
}
