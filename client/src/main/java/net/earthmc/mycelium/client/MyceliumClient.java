package net.earthmc.mycelium.client;

import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.MyceliumProvider;
import net.earthmc.mycelium.api.event.Events;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.store.Store;
import net.earthmc.mycelium.client.impl.api.NetworkImpl;
import net.earthmc.mycelium.client.impl.event.EventsImpl;
import net.earthmc.mycelium.client.impl.messaging.callback.CallbackProvider;
import net.earthmc.mycelium.client.impl.messaging.MessagingRegistrarImpl;
import net.earthmc.mycelium.client.impl.store.StoreImpl;
import net.earthmc.mycelium.client.util.Property;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;

import java.io.Closeable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@NullMarked
public class MyceliumClient implements Mycelium, Closeable {
    private final Logger logger = LoggerFactory.getLogger(MyceliumClient.class);

    private final UnifiedJedis redisClient;

    private final MessagingRegistrarImpl messagingRegistrar;
    private final CallbackProvider callbackProvider;
    private final NetworkImpl network;
    private final Platform platform;
    private final Store storage;
    private final EventsImpl events;

    private final String clientId = UUID.randomUUID().toString();

    protected MyceliumClient(final UnifiedJedis client, final Platform platform) {
        this.redisClient = client;
        this.platform = platform;

        this.network = new NetworkImpl(platform.environment(), this);
        this.messagingRegistrar = new MessagingRegistrarImpl(this);
        this.callbackProvider = new CallbackProvider(this);
        this.storage = new StoreImpl(this);
        this.events = new EventsImpl(messagingRegistrar);
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
    public Store dataStore() {
        return this.storage;
    }

    @Override
    public Events events() {
        return this.events;
    }

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
        this.events.shutdown();
    }

    public Logger logger() {
        return this.logger;
    }

    public static class Builder {
        private final Platform platform;

        private boolean registerInstance = false;
        private Function<MyceliumClient, @Nullable Server> nativeServer = client -> null;
        private Function<MyceliumClient, @Nullable Proxy> nativeProxy = client -> null;

        private String redisHost = Property.property("mycelium.redis.host", Protocol.DEFAULT_HOST);
        private int redisPort = Property.property("mycelium.redis.port", Integer::parseInt, Protocol.DEFAULT_PORT);
        private @Nullable String redisUsername = Property.property("mycelium.redis.username");
        private @Nullable String redisPassword = Property.property("mycelium.redis.password");

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

        public Builder nativeServer(final Function<MyceliumClient, @Nullable Server> nativeServer) {
            Objects.requireNonNull(nativeServer, "supplier may not be null");
            this.nativeServer = nativeServer;
            return this;
        }

        public Builder nativeProxy(final Function<MyceliumClient, @Nullable Proxy> nativeProxy) {
            Objects.requireNonNull(nativeProxy, "supplier may not be null");
            this.nativeProxy = nativeProxy;
            return this;
        }

        public Builder redisHost(final String redisHost) {
            this.redisHost = redisHost;
            return this;
        }

        public Builder redisPort(final int redisPort) {
            this.redisPort = redisPort;
            return this;
        }

        public Builder redisUsername(final String redisUsername) {
            this.redisUsername = redisUsername;
            return this;
        }

        public Builder redisPassword(final String redisPassword) {
            this.redisPassword = redisPassword;
            return this;
        }

        public MyceliumClient build() {
            final HostAndPort hostAndPort = new HostAndPort(this.redisHost, this.redisPort);
            final JedisClientConfig config = DefaultJedisClientConfig.builder()
                    .user(this.redisUsername)
                    .password(this.redisPassword)
                    .build();

            final MyceliumClient client = new MyceliumClient(new UnifiedJedis(hostAndPort, config), this.platform);

            if (this.registerInstance) {
                MyceliumProvider.register(client);
            }

            client.network.setNativeProxy(this.nativeProxy.apply(client));
            client.network.setNativeServer(this.nativeServer.apply(client));

            return client;
        }
    }
}
