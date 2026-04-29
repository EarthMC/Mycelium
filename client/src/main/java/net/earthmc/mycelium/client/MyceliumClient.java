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
import net.earthmc.mycelium.client.redis.RedisConfiguration;
import net.earthmc.mycelium.client.util.Property;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.UnifiedJedis;

import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@NullMarked
public class MyceliumClient implements Mycelium, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyceliumClient.class);

    private final UnifiedJedis redisClient;

    private final MessagingRegistrarImpl messagingRegistrar;
    private final CallbackProvider callbackProvider;
    private final NetworkImpl network;
    private final AbstractPlatform platform;
    private final Store storage;
    private final EventsImpl events;

    private final String clientId = UUID.randomUUID().toString();

    protected MyceliumClient(final UnifiedJedis client, final AbstractPlatform platform) {
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

    public static Builder forPlatform(final AbstractPlatform platform) {
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

    public AbstractPlatform platform() {
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
        return LOGGER;
    }

    public static class Builder {
        private final AbstractPlatform platform;

        private boolean registerInstance = false;
        private Function<MyceliumClient, @Nullable Server> nativeServer = client -> null;
        private Function<MyceliumClient, @Nullable Proxy> nativeProxy = client -> null;

        private String redisHost;
        private int redisPort;
        private @Nullable String redisUsername;
        private @Nullable String redisPassword;

        private Builder(final AbstractPlatform platform) {
            this.platform = platform;

            // read defaults if settings file is supplied
            final String settingsFile = Property.property("mycelium.settings-file");
            final Path settingsFilePath = settingsFile != null ? Path.of(settingsFile) : Optional.ofNullable(platform.dataDirectory()).map(dir -> dir.resolve("mycelium.properties")).orElse(null);
            final RedisConfiguration configuration;

            if (settingsFilePath != null) {
                if (Files.exists(settingsFilePath)) {
                    configuration = RedisConfiguration.fromFile(settingsFilePath);
                } else {
                    if (settingsFile != null) {
                        LOGGER.error("Could not find the file at path '{}' for option mycelium.settings-file", settingsFile);
                    }
                    configuration = RedisConfiguration.empty();
                }
            } else {
                configuration = RedisConfiguration.empty();
            }

            this.redisHost = configuration.host().orElseGet(() -> Property.property("mycelium.redis.host", Protocol.DEFAULT_HOST));
            this.redisPort = configuration.port().orElseGet(() -> Property.property("mycelium.redis.port", Integer::parseInt, Protocol.DEFAULT_PORT));
            this.redisUsername = configuration.username().orElseGet(() -> Property.property("mycelium.redis.username"));
            this.redisPassword = configuration.password().orElseGet(() -> Property.property("mycelium.redis.password"));
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
            final JedisClientConfig config = DefaultJedisClientConfig.builder()
                    .user(this.redisUsername)
                    .password(this.redisPassword)
                    .build();

            final MyceliumClient client = new MyceliumClient(RedisClient.builder().hostAndPort(this.redisHost, this.redisPort).clientConfig(config).build(), this.platform);

            if (this.registerInstance) {
                MyceliumProvider.register(client);
            }

            client.network.setNativeProxy(this.nativeProxy.apply(client));
            client.network.setNativeServer(this.nativeServer.apply(client));

            return client;
        }
    }
}
